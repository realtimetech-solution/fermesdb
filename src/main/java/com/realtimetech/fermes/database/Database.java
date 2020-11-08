package com.realtimetech.fermes.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.realtimetech.fermes.database.exception.DatabaseBackupException;
import com.realtimetech.fermes.database.exception.DatabaseCloseException;
import com.realtimetech.fermes.database.exception.DatabaseReadException;
import com.realtimetech.fermes.database.exception.DatabaseWriteException;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.item.exception.ItemDeserializeException;
import com.realtimetech.fermes.database.item.exception.ItemSerializeException;
import com.realtimetech.fermes.database.link.exception.LinkCreateException;
import com.realtimetech.fermes.database.link.exception.LinkRemoveException;
import com.realtimetech.fermes.database.lock.Lock;
import com.realtimetech.fermes.database.memory.exception.MemoryManageException;
import com.realtimetech.fermes.database.page.EmptyPagePointer;
import com.realtimetech.fermes.database.page.Page;
import com.realtimetech.fermes.database.page.Page.PageSerializer;
import com.realtimetech.fermes.database.page.exception.BlockReadException;
import com.realtimetech.fermes.database.page.exception.BlockWriteException;
import com.realtimetech.fermes.database.page.exception.PageCreateException;
import com.realtimetech.fermes.database.page.file.impl.MemoryFileWriter;
import com.realtimetech.fermes.database.root.RootItem;
import com.realtimetech.fermes.database.root.RootItem.ItemCreator;
import com.realtimetech.fermes.database.zip.ZipUtils;
import com.realtimetech.kson.builder.KsonBuilder;
import com.realtimetech.kson.element.JsonObject;
import com.realtimetech.kson.element.JsonValue;
import com.realtimetech.kson.exception.DeserializeException;
import com.realtimetech.kson.exception.SerializeException;
import com.realtimetech.kson.util.pool.KsonPool;

public class Database {
	private Charset charset;

	private ArrayList<Page> pages;

	private File databaseDirectory;

	private int pageId;

	private int pageSize;
	private int blockSize;

	private long maxMemory;
	private long currentMemory;

	private Link<? extends Item> headObject;
	private Link<? extends Item> tailObject;

	private Queue<EmptyPagePointer> emptyPagePointers;

	private KsonPool ksonPool;

	private PageSerializer pageSerializer;

	private Link<RootItem> rootItem;

	private Lock diskLock;
	private Lock processLock;

	private List<Link<? extends Item>> frozeLinks;

	private boolean useInstrumentation;

	private ByteArrayOutputStream byteArrayOutputStream;
	private ObjectOutputStream objectOutputStream;

	protected Database(File databaseDirectory, long maxMemory) throws DatabaseReadException {
		this();

		this.databaseDirectory = databaseDirectory;
		this.maxMemory = maxMemory;
		
		load();

		if (this.blockSize == -1) {
			throw new DatabaseReadException("Can't load database, not exist database.");
		}
	}

	protected Database(File databaseDirectory, int pageSize, int blockSize, long maxMemory) throws DatabaseReadException {
		this();

		this.databaseDirectory = databaseDirectory;

		if (databaseDirectory.isDirectory() && databaseDirectory.exists()) {
			File configFile = new File(databaseDirectory, "database.config");

			if (configFile.exists()) {
				throw new DatabaseReadException("Can't create database with parameters.");
			}
		}

		this.pageSize = pageSize;
		this.blockSize = blockSize;
		this.maxMemory = maxMemory;

		load();
	}

	Database() {
		if (FermesDB.getGlobalInstrumentation() != null) {
			this.useInstrumentation = true;
		} else {
			this.useInstrumentation = false;
		}

		this.diskLock = new Lock();
		this.processLock = new Lock();

		this.charset = Charset.forName("UTF-8");

		this.frozeLinks = new LinkedList<Link<? extends Item>>();
		this.pages = new ArrayList<Page>();
		this.emptyPagePointers = new LinkedList<EmptyPagePointer>();
		this.pageSerializer = new PageSerializer(this);

		this.pageId = 0;
		this.currentMemory = 0;

		KsonBuilder ksonBuilder = new KsonBuilder();
		ksonBuilder.registerTransformer(Link.class, new LinkTransformer(this));
		this.ksonPool = new KsonPool(ksonBuilder);

		this.pageSize = 0;
		this.blockSize = -1;
		this.maxMemory = -1;

		try {
			this.byteArrayOutputStream = new ByteArrayOutputStream();
			this.objectOutputStream = new ObjectOutputStream(this.byteArrayOutputStream);
		} catch (IOException e) {
			this.byteArrayOutputStream = null;
			this.objectOutputStream = null;
		}
	}

	public boolean isUseInstrumentation() {
		return useInstrumentation;
	}

	/**
	 * Getting root links
	 */

	public <T extends Item> Link<T> getLink(String name, ItemCreator<T> creator) throws LinkCreateException {
		return this.rootItem.get().getLink(name, creator);
	}

	public Link<RootItem> getRootItem() {
		return rootItem;
	}

	/**
	 * Save and load methods
	 */

	@SuppressWarnings("unchecked")
	private void load() throws DatabaseReadException {
		try {
			this.processLock.waitLock();
			this.diskLock.lock();

			if (!databaseDirectory.isDirectory() || !databaseDirectory.exists()) {
				this.databaseDirectory.mkdirs();
			}

			File configFile = new File(databaseDirectory, "database.config");

			if (!configFile.exists()) {
				try {
					configFile.createNewFile();
				} catch (IOException e) {
					throw new DatabaseReadException("Can't create database, access denied when create config file.");
				}

				JsonObject jsonObject = new JsonObject();

				jsonObject.put("pageId", 0);
				jsonObject.put("pageSize", this.pageSize);
				jsonObject.put("blockSize", this.blockSize);

				try {
					Files.write(configFile.toPath(), jsonObject.toKsonString().getBytes(this.charset));
				} catch (IOException e) {
					throw new DatabaseReadException("Can't create database, access denied when create config file.");
				}

				try {
					this.rootItem = this.createLink(null, new RootItem());
				} catch (LinkCreateException e) {
					throw new DatabaseReadException(e, "Can't create database, failure to create root link(0).");
				}
			}

			JsonObject jsonObject;

			try {
				jsonObject = (JsonObject) ksonPool.get().fromString(new String(Files.readAllBytes(configFile.toPath()), charset));
			} catch (IOException e) {
				throw new DatabaseReadException(e, "Can't load database, IOException in parse json config.");
			}

			this.pageSize = (int) jsonObject.get("pageSize");
			this.blockSize = (int) jsonObject.get("blockSize");

			for (int index = 0; index < (int) jsonObject.get("pageId"); index++) {
				try {
					Page page = this.createPageWithoutEmptyPointer();
					this.pages.add(page);

					MemoryFileWriter pageBuffer = new MemoryFileWriter(page.getPageFile());
					pageBuffer.load();

					pageSerializer.read(page, pageBuffer);
				} catch (PageCreateException | IOException e) {
					throw new DatabaseReadException(e, "Can't load database, IOException in parse page files.");
				}
			}
			System.gc();

			this.rootItem = (Link<RootItem>) this.getLinkByGid(0);

			if (this.rootItem == null) {
				throw new DatabaseReadException("Can't create database, failure load root link(0).");
			}
		} finally {
			this.diskLock.unlock();
		}
	}

	public void save() throws DatabaseWriteException {
		try {
			this.processLock.waitLock();
			this.diskLock.lock();

			JsonObject jsonObject = new JsonObject();

			jsonObject.put("pageId", this.pageId);
			jsonObject.put("pageSize", this.pageSize);
			jsonObject.put("blockSize", this.blockSize);

			try {
				Files.write(new File(databaseDirectory, "database.config").toPath(), jsonObject.toKsonString().getBytes(this.charset));
			} catch (IOException e) {
				throw new DatabaseWriteException(e, "Can't save database, access denied when save config file.");
			}

			for (Page page : this.pages) {
				page.enableBlocksDirectly();

				for (Link<? extends Item> link : page.getLinks()) {
					if (link != null) {
						try {
							writeLinkBlocks(link);
						} catch (ItemSerializeException | BlockWriteException e) {
							throw new DatabaseWriteException(e, "Can't save database, write link.");
						}
					}
				}

				try {
					MemoryFileWriter pageBuffer = new MemoryFileWriter(pageSerializer.getWriteLength(page), page.getPageFile());
					pageSerializer.write(page, pageBuffer);
					pageBuffer.save();
				} catch (IOException e) {
					throw new DatabaseWriteException(e, "Can't save database, IOException in save page files.");
				}

				try {
					page.disableBlocksDirectly();
				} catch (IOException e) {
					throw new DatabaseWriteException(e, "Can't save database, IOException in save buffer.");
				}
			}
		} finally {
			this.diskLock.unlock();
		}
	}

	public void saveAndBackup(File backupFile) throws DatabaseWriteException, DatabaseBackupException {
		try {
			this.processLock.waitLock();
			this.diskLock.lock();

			ZipUtils.zipFolder(databaseDirectory, backupFile);

			save();
		} catch (IOException e) {
			throw new DatabaseBackupException(e, "Failure to backup, because occurred compress zip exception.");
		} finally {
			this.diskLock.unlock();
		}

	}

	public void close() throws DatabaseCloseException {
		try {
			this.processLock.waitLock();
			this.diskLock.lock();

			for (Page page : this.pages) {
				int index = 0;
				for (Link<? extends Item> link : page.getLinks()) {
					if (link != null) {
						try {
							unloadLink(link, true);
						} catch (ItemSerializeException | BlockWriteException e) {
							throw new DatabaseCloseException(e, "Failure to close, because occurred unload link.");
						}
						page.getLinks()[index] = null;
					}

					index++;
				}
			}

			this.pages.clear();

			System.gc();
		} finally {
			this.diskLock.unlock();
		}
	}

	/**
	 * Getter for options
	 */

	public File getDatabaseDirectory() {
		return databaseDirectory;
	}

	public int getPageSize() {
		return pageSize;
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public long getCurrentMemory() {
		return currentMemory;
	}

	/**
	 * Access method using gid methods
	 */

	public Link<? extends Item> getLinkByGid(long gid) {
		this.diskLock.waitLock();

		if (gid == -1)
			return null;

		Page pageByGid = this.getPageByGid(gid);

		if (pageByGid == null)
			return null;

		return pageByGid.getLinkByIndex((int) (gid % this.pageSize));
	}

	public Page getPageByGid(long gid) {
		this.diskLock.waitLock();

		int index = (int) (gid / this.pageSize);
		return this.pages.size() > index ? this.pages.get(index) : null;
	}

	/**
	 * Serialize / deserialize item methods
	 */

	private byte[] serializeItem(Item item) throws ItemSerializeException {
		try {
			JsonObject jsonObject = new JsonObject();
			jsonObject.put("class", item.getClass().getName());
			jsonObject.put("item", ksonPool.get().fromObject(item));

			return ksonPool.writer().toString(jsonObject).getBytes(charset);
		} catch (SerializeException | IOException e) {
			throw new ItemSerializeException(e, "Can't serialize object (item to bytes).");
		}
	}

	private Item deserializeItem(byte[] bytes) throws ItemDeserializeException {
		try {
			JsonObject jsonObject = (JsonObject) ksonPool.get().fromString(new String(bytes, charset));

			Object object = ksonPool.get().toObject(Database.class.getClassLoader().loadClass((String) jsonObject.get("class")), (JsonValue) jsonObject.get("item"));

			return (Item) object;
		} catch (IOException | ClassNotFoundException | DeserializeException e) {
			throw new ItemDeserializeException(e, "Can't deserialize object (bytes to item).");
		}
	}

	synchronized void fitMemory(long size) throws MemoryManageException {
		if(this.maxMemory != -1) {
			this.frozeLinks.clear();
			while (this.currentMemory + size > this.maxMemory) {
				Link<? extends Item> object = this.tailObject;

				if (object == null) {
					throw new MemoryManageException("Can't unload object anymore, but still memory not enough.");
				}

				if (!object.accessed && !object.froze) {
					try {
						this.unloadLink(object, false);
					} catch (ItemSerializeException | BlockWriteException e) {
						throw new MemoryManageException(e, "Failure to unload item from memory.");
					}
				} else {
					object.accessed = false;
					remove(object);

					if (object.froze) {
						this.frozeLinks.add(object);
					} else {
						join(object);
					}
				}
			}

			for (Link<? extends Item> object : this.frozeLinks) {
				join(object);
			}
		}
	}

	/**
	 * Load, unload, update link methods
	 */

	@SuppressWarnings("unchecked")
	protected <R extends Item> void loadLink(Link<R> link) throws MemoryManageException, BlockReadException, ItemDeserializeException {
		synchronized (link) {
			if (!link.isLoaded()) {
				try {
					this.processLock.tryLock();
					this.diskLock.waitLock();

					byte[] bytes = link.getPage().readBlocks(link.blockIds, link.itemLength);

					this.fitMemory(bytes.length);

					link.itemLength = bytes.length;
					link.item = (R) deserializeItem(bytes);

					link.item.onLoad(link);

					if(this.maxMemory != -1) {
						synchronized (this) {
							join(link);
							this.currentMemory += link.itemLength;
						}
					}
				} finally {
					this.processLock.unlock();
				}
			}
		}
	}

	private void unloadLink(Link<? extends Item> link, boolean justMemory) throws ItemSerializeException, BlockWriteException {
		synchronized (link) {
			if (link.isLoaded()) {
				try {
					this.processLock.tryLock();
					this.diskLock.waitLock();

					if (!justMemory) {
						writeLinkBlocks(link);
					}
					link.item = null;

					if(this.maxMemory != -1) {
						synchronized (this) {
							remove(link);
							this.currentMemory -= link.itemLength;
						}
					}
				} finally {
					this.processLock.unlock();
				}
			}
		}
	}

	private void writeLinkBlocks(Link<? extends Item> link) throws ItemSerializeException, BlockWriteException {
		if (link.isLoaded()) {
			byte[] bytes = serializeItem(link.item);

			link.itemLength = bytes.length;
			link.blockIds = link.getPage().fitBlockIds(link.blockIds, link.itemLength);
			link.getPage().writeBlocks(link.blockIds, bytes);
		}
	}

	protected <R extends Item> void updateLinkLength(Link<R> link) throws ItemSerializeException, MemoryManageException {
		if(this.maxMemory != -1) {
			int length = -1;

			if (this.useInstrumentation) {
				length = (int) FermesDB.getGlobalInstrumentation().getObjectSize(link.item);
			} else {
				if (this.objectOutputStream != null) {
					synchronized (this.objectOutputStream) {
						try {
							this.objectOutputStream.writeObject(link.itemLength);
							length = this.byteArrayOutputStream.size();
							this.byteArrayOutputStream.reset();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

			if (length == -1) {
				byte[] bytes = serializeItem(link.item);
				length = bytes.length;
			}

			this.fitMemory(length);
			link.itemLength = length;

			synchronized (this) {
				join(link);
				this.currentMemory += link.itemLength;
			}
		}
	}

	/**
	 * Create or remove link methods
	 */

	protected <R extends Item> Link<R> createLink(Link<? extends Item> parentLink, R item) throws LinkCreateException {
		try {
			this.processLock.tryLock();
			this.diskLock.waitLock();

			Page page = null;
			int nextIndex = 0;

			synchronized (this.emptyPagePointers) {
				if (this.emptyPagePointers.isEmpty()) {
					try {
						this.pages.add(createPage());
					} catch (PageCreateException e) {
						throw new LinkCreateException(e, "Failure to create link, because occured page create exception.");
					}
				}

				EmptyPagePointer pointer = this.emptyPagePointers.peek();

				page = pointer.getTargetPage();
				nextIndex = pointer.nextIndex();

				if (pointer.isDone()) {
					this.emptyPagePointers.poll();
				}
			}

			long gid = page.getId() * this.getPageSize() + nextIndex;
			Link<R> link = new Link<R>(this, page, parentLink == null ? -1 : parentLink.gid, gid);
			link.item = item;

			item.onCreate((Link<R>) link);
			item.onLoad(link);

			page.setLinkByIndex(nextIndex, link);

			if (parentLink != null) {
				parentLink.createChildLinksIfNotExist();

				synchronized (parentLink.childLinks) {
					parentLink.childLinks.add(gid);
				}
			}

			try {
				this.updateLinkLength(link);
			} catch (ItemSerializeException | MemoryManageException e) {
				throw new LinkCreateException(e, "Failure to create link, because occured update length exception.");
			}

			return link;
		} finally {
			this.processLock.unlock();
		}
	}

	protected boolean removeLink(Link<? extends Item> link) throws LinkRemoveException {
		try {
			this.processLock.tryLock();
			this.diskLock.waitLock();

			if (!link.removed) {
				link.removed = true;

				if (link.isLoaded()) {
					try {
						this.unloadLink(link, true);
					} catch (ItemSerializeException | BlockWriteException e) {
						throw new LinkRemoveException(e, "Failure to remove link, because occured unload exception.");
					}
				}

				Link<? extends Item> parentLink = this.getLinkByGid(link.parentLink);

				if (parentLink != null && parentLink.childLinks != null) {

					synchronized (parentLink.childLinks) {
						parentLink.childLinks.remove(link.gid);
					}
				}

				if (link.childLinks != null) {
					synchronized (link.childLinks) {
						for (long childLinkGid : link.childLinks) {
							Link<? extends Item> childLink = this.getLinkByGid(childLinkGid);

							if (childLink != null) {
								this.removeLink(childLink);
							}
						}
					}
				}

				long gid = link.getGid();
				int index = (int) (gid % this.pageSize);

				Page page = this.pages.get((int) (gid / this.pageSize));

				page.removeLinkByIndex(link.blockIds, index);

				this.addEmptyPagePointer(new EmptyPagePointer(page, index));

				return true;
			}

			return false;
		} finally {
			this.processLock.unlock();
		}
	}

	/**
	 * Empty pointer methods
	 */

	public void addEmptyPagePointer(EmptyPagePointer emptyPagePointer) {
		synchronized (this.emptyPagePointers) {
			this.emptyPagePointers.add(emptyPagePointer);
		}
	}

	/**
	 * Page control methods
	 */

	public Page createPage() throws PageCreateException {
		synchronized (this.emptyPagePointers) {
			Page page = createPageWithoutEmptyPointer();

			this.addEmptyPagePointer(new EmptyPagePointer(page, 0, this.pageSize - 1));

			return page;
		}
	}

	public Page createPageWithoutEmptyPointer() throws PageCreateException {
		synchronized (this.pages) {
			try {
				return new Page(this, this.pageId++, this.pageSize, this.blockSize);
			} catch (IOException e) {
				throw new PageCreateException(e, "Failure to create page.");
			}

		}
	}

	/**
	 * Used tree methods
	 */

	synchronized void join(Link<? extends Item> object) {
		if (this.headObject == null) {
			object.prevObject = null;
			object.nextObject = null;

			this.tailObject = object;
			this.headObject = object;
		} else {
			object.prevObject = null;
			object.nextObject = this.headObject;
			this.headObject.prevObject = object;
			this.headObject = object;
		}
	}

	synchronized void remove(Link<? extends Item> object) {
		if (object.nextObject != null) {
			object.nextObject.prevObject = object.prevObject;
		}

		if (object.prevObject != null) {
			object.prevObject.nextObject = object.nextObject;
		}

		if (this.tailObject == object) {
			this.tailObject = object.prevObject;
		}

		if (this.headObject == object) {
			this.headObject = object.nextObject;
		}

		object.nextObject = null;
		object.prevObject = null;
	}
}
