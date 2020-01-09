package com.realtimetech.fermes.database.lock;

public class Lock {
	private Thread owner;
	private boolean locked;

	public Lock() {
		this.locked = false;
		this.owner = null;
	}

	public synchronized boolean isLocked() {
		return locked;
	}

	public synchronized boolean tryLock() {
		if (!this.locked) {
			this.owner = Thread.currentThread();
			this.locked = true;

			return true;
		}

		return false;
	}

	public synchronized void waitLock() {
		if (this.owner != Thread.currentThread()) {
			while (this.locked) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void lock() {
		this.waitLock();

		if (!this.locked) {
			this.owner = Thread.currentThread();
			this.locked = true;
		}
	}

	public synchronized void unlock() {
		this.locked = false;
		notify();
	}
}
