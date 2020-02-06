package com.realtimetech.fermes.viewer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JMenu;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.FermesDB;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.exception.DatabaseCloseException;
import com.realtimetech.fermes.database.exception.DatabaseReadException;
import com.realtimetech.fermes.database.item.Item;
import com.realtimetech.fermes.database.root.RootItem;
import com.realtimetech.kson.KsonContext;
import com.realtimetech.kson.element.JsonArray;
import com.realtimetech.kson.element.JsonObject;
import com.realtimetech.kson.exception.SerializeException;
import com.realtimetech.kson.transform.Transformer;

import javax.swing.JSplitPane;
import javax.swing.JTree;

public class MainFrame extends JFrame {
	private Database database;

	/**
	 * 
	 */
	private static final long serialVersionUID = -8692608603333287426L;
	private JTree tree;

	private JTree treeJson;

	private KsonContext ksonContext;

	private JsonObject targetObject;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame("example_db/");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * 
	 * @throws FermesDatabaseException
	 */
	public MainFrame(String databaseDirectory) {
		this.ksonContext = new KsonContext();
		this.ksonContext.registerTransformer(Link.class, new Transformer<Link<? extends Item>>() {
			@Override
			public Object serialize(KsonContext ksonContext, Link<? extends Item> value) {
				return "GID <" + value.getGid() + ">";
			}

			@Override
			public Link<? extends Item> deserialize(KsonContext ksonContext, Class<?> object, Object value) {
				return null;
			}
		});

		setTitle("FermesViewer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 934, 552);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu menuDatabase = new JMenu("Database");
		menuBar.add(menuDatabase);

		JMenuItem menuLoad = new JMenuItem("Load");
		menuDatabase.add(menuLoad);

		menuLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				jFileChooser.setDialogTitle("Select an FermesDB config file");
				jFileChooser.setCurrentDirectory(new File("./"));
				jFileChooser.setAcceptAllFileFilterUsed(false);
				FileFilter filenameFilter = new FileFilter() {
					@Override
					public boolean accept(File file) {
						return file.isDirectory() || file.getName().equals("database.config");
					}

					@Override
					public String getDescription() {
						return "FermesDB Config File";
					}
				};
				jFileChooser.addChoosableFileFilter(filenameFilter);

				int returnValue = jFileChooser.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					if (jFileChooser.getSelectedFile().getParentFile().isDirectory()) {
						File parentFile = jFileChooser.getSelectedFile().getParentFile();

						try {
							database = FermesDB.loadDatabase(parentFile);
							updateTree();
						} catch (DatabaseReadException e) {
							e.printStackTrace();
						}

					}
				}

			}
		});

		JMenuItem menuClose = new JMenuItem("Close");
		menuDatabase.add(menuClose);

		menuClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (database != null) {
					try {
						database.close();
					} catch (DatabaseCloseException e) {
						e.printStackTrace();
					}
				}

				database = null;
				targetObject = null;

				updateTree();
				updateViewerTree();
			}
		});

		JMenu menuSearch = new JMenu("Search");
		menuBar.add(menuSearch);

		JMenu menuHelp = new JMenu("Help");
		menuBar.add(menuHelp);

		JMenuItem menuAbout = new JMenuItem("About");
		menuHelp.add(menuAbout);

		menuAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				JOptionPane.showMessageDialog(null, "FERMES DB VIEWER 0.1V");
			}
		});

		getContentPane().setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.CENTER);

		tree = new JTree();
		splitPane.setLeftComponent(tree);

		treeJson = new JTree();
		splitPane.setRightComponent(treeJson);

		updateTree();
		updateViewerTree();

		TreeWillExpandListener treeWillExpandListener = new TreeWillExpandListener() {
			public void treeWillCollapse(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException {
			}

			public void treeWillExpand(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException {

				TreePath path = treeExpansionEvent.getPath();

				if (path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();

					if (parent instanceof ItemTreeNode) {
						parent.removeAllChildren();
						ItemTreeNode itemTreeNode = (ItemTreeNode) path.getLastPathComponent();
						Link<? extends Item> link = itemTreeNode.getLink();
						Collection<Long> childLinks = link.getChildLinks();
						int index = 0;
						for (Long gid : childLinks) {
							Link<? extends Item> childLink = link.getDatabase().getLinkByGid(gid);

							if (childLink.get() == null) {
								System.out.println("NULL");
							}

							readDatabaseChild(parent, index++, childLink);
						}

					}
				}

			}
		};

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent treeSelectionEvent) {

				TreePath path = treeSelectionEvent.getPath();

				if (path.getLastPathComponent() instanceof DefaultMutableTreeNode) {
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();

					if (parent instanceof ItemTreeNode) {
						ItemTreeNode itemTreeNode = (ItemTreeNode) parent;

						Link<? extends Item> link = itemTreeNode.getLink();

						link.lock();

						try {
							Item object = link.get();
							targetObject = (JsonObject) ksonContext.fromObject(object);
						} catch (SerializeException e) {
							e.printStackTrace();
						}

						link.unlock();

						updateViewerTree();
					}
				}
			}
		});
		tree.addTreeWillExpandListener(treeWillExpandListener);
	}

	public void updateViewerTree() {
		treeJson.setModel(null);
		treeJson.removeAll();

		if (database != null) {
			if (this.targetObject != null) {
				DefaultMutableTreeNode root = new DefaultMutableTreeNode(this.targetObject.toString());
				DefaultTreeModel rootTree = new DefaultTreeModel(root);

				readJsonChild(root, this.targetObject);

				treeJson.setModel(rootTree);
			}
		}
	}

	public void readJsonChild(DefaultMutableTreeNode parent, Object object) {
		parent.removeAllChildren();

		if (object instanceof JsonObject) {
			JsonObject jsonObject = (JsonObject) object;

			for (Object key : jsonObject.keySet()) {
				JsonTreeNode keyNode = new JsonTreeNode(key.toString());

				readJsonChild(keyNode, jsonObject.get(key));

				parent.add(keyNode);
			}
		} else if (object instanceof JsonArray) {
			JsonArray jsonArray = (JsonArray) object;

			int index = 0;
			for (Object value : jsonArray) {
				JsonTreeNode keyNode = new JsonTreeNode("[" + (index++) + "]");

				readJsonChild(keyNode, value);

				parent.add(keyNode);
			}
		} else {
			parent.add(new DefaultMutableTreeNode(object.toString()));
		}
	}

	public void updateTree() {
		tree.setModel(null);
		tree.removeAll();

		if (database != null) {
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(database.getDatabaseDirectory().getName());
			DefaultTreeModel rootTree = new DefaultTreeModel(root);
			Link<RootItem> rootItem = database.getRootItem();

			HashMap<String, Link<? extends Item>> linkMap = rootItem.get().getLinkMap();

			int childIndex = 0;
			for (String key : linkMap.keySet()) {
				Link<? extends Item> link = linkMap.get(key);

				link.get();
				readDatabaseChild(root, childIndex++, link);
			}

			tree.setModel(rootTree);
		}
	}

	public void readDatabaseChild(DefaultMutableTreeNode parent, int index, Link<? extends Item> link) {
		ItemTreeNode itemTreeNode = new ItemTreeNode(index, link);

		Collection<Long> childLinks = link.getChildLinks();

		int childIndex = 0;
		for (Long gid : childLinks) {
			Link<? extends Item> linkByGid = link.getDatabase().getLinkByGid(gid);

			readDatabaseChild(itemTreeNode, childIndex++, linkByGid);
		}

		parent.add(itemTreeNode);
	}
}
