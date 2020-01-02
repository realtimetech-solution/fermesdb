package com.realtimetech.fermes.database.page;

public class EmptyPagePointer {
	private Page targetPage;

	private int startIndex;
	private int endIndex;
	private int currentIndex;

	public EmptyPagePointer(Page targetPage, int index) {
		this(targetPage, index, index);
	}

	public EmptyPagePointer(Page targetPage, int startIndex, int endIndex) {
		this.targetPage = targetPage;

		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.currentIndex = this.startIndex;
	}

	public Page getTargetPage() {
		return targetPage;
	}

	public int nextIndex() {
		if (this.currentIndex > this.endIndex) {
			return -1;
		} else {
			return this.currentIndex++;
		}
	}

	public boolean isDone() {
		if (this.currentIndex > this.endIndex) {
			return true;
		}

		return false;
	}
}
