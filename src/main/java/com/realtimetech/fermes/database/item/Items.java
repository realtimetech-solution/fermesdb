package com.realtimetech.fermes.database.item;

import java.util.Iterator;

import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.database.item.Items.ItemIterable.ItemWhere;
import com.realtimetech.fermes.database.link.exception.LinkCreateException;
import com.realtimetech.fermes.database.link.exception.LinkRemoveException;

@SuppressWarnings("unchecked")
public abstract class Items<T extends Item> extends SelfItem<T> {
	public static class LinkIterable<V extends Item> implements Iterable<Link<V>> {
		private Iterable<Long> iterable;
		private Items<V> items;

		public LinkIterable(Items<V> items, Iterable<Long> iterable) {
			this.iterable = iterable;
			this.items = items;
		}

		@Override
		public Iterator<Link<V>> iterator() {
			final Iterator<Long> iterator = this.iterable.iterator();

			return new Iterator<Link<V>>() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public Link<V> next() {
					return items.getItemByGid(iterator.next());
				}
			};
		}
	}

	public static class ItemIterable<V extends Item> implements Iterable<Link<V>> {
		@FunctionalInterface
		public static interface ItemWhere<V extends Item> {
			public boolean checkCondition(V item);
		}

		private Iterable<Link<V>> iterable;
		private ItemWhere<V> where;

		public ItemIterable(Iterable<Link<V>> iterable, ItemWhere<V> where) {
			this.iterable = iterable;
			this.where = where;
		}

		public ItemIterable<V> where(ItemWhere<V> where) {
			return new ItemIterable<V>((Iterable<Link<V>>) this, where);
		}

		@Override
		public Iterator<Link<V>> iterator() {
			return new ItemIterator<V>(this.iterable.iterator(), this.where);
		}
	}

	public static class ItemIterator<V extends Item> implements Iterator<Link<V>> {
		private Iterator<Link<V>> iterator;
		private ItemWhere<V> where;

		private Link<V> lastLink;

		public ItemIterator(Iterator<Link<V>> iterator, ItemWhere<V> where) {
			this.iterator = iterator;
			this.where = where;

			this.consume();
		}

		@Override
		public boolean hasNext() {
			return this.lastLink != null;
		}

		@Override
		public Link<V> next() {
			Link<V> link = this.lastLink;

			this.consume();

			return link;
		}

		private void consume() {
			while (iterator.hasNext()) {
				Link<V> link = iterator.next();
				
				if (where.checkCondition(link.get())) {
					this.lastLink = link;
					return;
				}
			}

			if (!iterator.hasNext()) {
				this.lastLink = null;
			}
		}
	}

	protected Link<T> addItem(T item) throws LinkCreateException {
		return this.current().createChildLink(item);
	}

	protected boolean removeItem(Link<T> link) throws LinkRemoveException {
		return this.current().removeChildLink(link);
	}

	protected Link<T> getItemByGid(long gid) {
		return (Link<T>) this.current().getDatabase().getLinkByGid(gid);
	}

	protected Link<T> getItem(int index) {
		return (Link<T>) this.current().getChildLinkItem(index);
	}

	protected Iterable<Long> getItemGids() {
		return this.current().getChildLinks();
	}

	protected Iterable<Link<T>> getItems() {
		return new LinkIterable<T>(this, this.current().getChildLinks());
	}

	public ItemIterable<T> where(ItemWhere<T> where) {
		return new ItemIterable<T>(getItems(), where);
	}

	protected int getItemCount() {
		return this.current().getChildCount();
	}

	@Deprecated
	protected Link<T>[] getItemArray() {
		Iterable<Long> childLinks = this.current().getChildLinks();

		synchronized (childLinks) {
			Link<T>[] items = new Link[this.current().getChildCount()];

			int index = 0;

			for (Long gid : childLinks) {
				items[index] = (Link<T>) this.current().getDatabase().getLinkByGid(gid);
				index++;
			}

			return items;
		}
	}
}
