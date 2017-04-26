package com.johnstarich.ee360p.skiplist;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * A fine-grained and lock-free skip-list implementation.
 * Created by johnstarich on 4/12/17.
 */
public class SkipList extends AbstractSet<Integer> {
	class Node {
		final int key;
		final int value;
		final int level;
		final AtomicMarkableReference<Node>[] forward;

		boolean markedForRemoval;

		@SuppressWarnings("unchecked")
		public Node(int key, int value, int level, int maxLevel) {
			this.key = key;
			this.value = value;
			this.level = level;
			this.forward = (AtomicMarkableReference<Node>[])
				new AtomicMarkableReference<?>[maxLevel];
			this.markedForRemoval = false;
		}

		public String toString() {
			return Integer.toString(value);
		}
	}

	final AtomicMarkableReference<Node> header;
	final AtomicInteger currentLevels;
	final AtomicInteger size;
	final int maxLevel;
	final float p = 0.5f;

	/**
	 * Create a skip list with a maximum level.
	 * TODO: add complexity description
	 * @param maxLevel The maximum level for this SkipList
	 */
	public SkipList(int maxLevel) {
		this.currentLevels = new AtomicInteger(0);
		this.size = new AtomicInteger(0);
		this.maxLevel = maxLevel;
		Node headerNode = new Node(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, maxLevel);
		header = new AtomicMarkableReference<>(headerNode, true);
		for (int level = 0; level < maxLevel; level += 1) {
			headerNode.forward[level] = header;
		}
	}

	@Override
	public boolean add(Integer value) {
		return insert(value, value);
	}

	@SuppressWarnings("unchecked")
	private boolean insert(int searchKey, int value) {
		Node[] update = new Node[maxLevel];
		AtomicMarkableReference<Node> current = header;
		int levels = currentLevels.get();
		Node currentNode = header.getReference();
		Node nextNode = currentNode;
		for (int level = levels - 1; level >= 0; level--) {
			current = header;
			do {
				currentNode = current.getReference();
				current = currentNode.forward[level];
				nextNode = current.getReference();
			} while (nextNode.key < searchKey || nextNode.markedForRemoval);
			update[level] = currentNode;
		}
		if (levels > 0) {
			while (! current.compareAndSet(nextNode, nextNode, true, false));
		}

		do {
			current = currentNode.forward[0];
			currentNode = current.getReference();
		} while (currentNode.markedForRemoval);

		if (currentNode.key == searchKey) {
			for (int level = 0; level < levels; level++) {
				Node beforeNode = update[level].forward[level].getReference();
				update[level].forward[level].attemptMark(beforeNode, true);
			}
			return false;
		}
		else {
			int newLevel = chooseRandomLevel();

			if (newLevel >= levels) {
				levels = currentLevels.incrementAndGet();
				newLevel = levels - 1;
				update[newLevel] = header.getReference();
				header.attemptMark(header.getReference(), false);
			}

			Node newNode = new Node(searchKey, value, newLevel, maxLevel);
			AtomicMarkableReference<Node> atomicNewNode =
					new AtomicMarkableReference<>(newNode, true);

			for (int level = 0; level < levels; level++) {
				newNode.forward[level] = update[level].forward[level];
				Node beforeNode = update[level].forward[level].getReference();
				update[level].forward[level].attemptMark(beforeNode, true);
				update[level].forward[level] = atomicNewNode;
			}

			size.incrementAndGet();

			return true;
		}
	}

	@Override
	public boolean remove(Object value) {
		if (! (value instanceof Integer)) {
			return false;
		}
		int searchKey = (Integer)value;
		Node[] update = new Node[maxLevel];
		AtomicMarkableReference<Node> current = this.header;
		int levels = currentLevels.get();
		Node currentNode = header.getReference();
		Node nextNode = currentNode;
		for (int level = levels - 1; level >= 0; level--) {
			current = header;
			do {
				currentNode = current.getReference();
				current = currentNode.forward[level];
				nextNode = current.getReference();
			} while (nextNode.key < searchKey || nextNode.markedForRemoval);
			update[level] = currentNode;
		}
		if (levels > 0) {
			while (! current.compareAndSet(nextNode, nextNode, true, false));
		}

		do {
			current = currentNode.forward[0];
			currentNode = current.getReference();
		} while (currentNode.markedForRemoval);

		if (current == header) {
			for (int level = 0; level < levels; level++) {
				AtomicMarkableReference<Node> link = update[level].forward[level];
				link.attemptMark(currentNode, true);
			}
			return false;
		}

		if (currentNode.key == searchKey) {
			currentNode.markedForRemoval = true;
			for (int level = 0; level < levels; level++) {
				update[level].forward[level] = currentNode.forward[level];
				if (update[level].forward[level] == null) {
					update[level].forward[level] = header;
				}
			}

			size.decrementAndGet();

			int oldLevels = levels;
			while (levels > 1 && header.getReference().forward[levels] == header) {
				levels = currentLevels.decrementAndGet();
			}

			for (int level = 0; level < oldLevels; level++) {
				AtomicMarkableReference<Node> link = update[level].forward[level];
				Node next;
				do {
					next = link.getReference();
				} while (! link.attemptMark(next, true));
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean contains(Object value) {
		if (! (value instanceof Integer)) {
			return false;
		}
		int searchKey = (Integer)value;
		AtomicMarkableReference<Node> current = this.header;

		for (int level = currentLevels.get() - 1; level >= 0; level--) {
			Node currentNode;
			do {
				currentNode = current.getReference();
				current = currentNode.forward[level];
				// TODO this may be checking the wrong node's key
			} while (currentNode.key < searchKey || currentNode.markedForRemoval);
		}

		Node currentNode;
		do {
			currentNode = current.getReference();
			current = currentNode.forward[0];
		} while (currentNode.markedForRemoval);
		int limit = currentNode.value;
		return limit == searchKey;
	}

	private Random levelRandom = new Random(0);

	private int chooseRandomLevel() {
		int newLevel = 0;
		while (newLevel < maxLevel - 1 && levelRandom.nextFloat() < this.p) {
			newLevel += 1;
		}
		return newLevel;
	}

	public int size() {
		return size.get();
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private AtomicMarkableReference<Node> current = header;

			@Override
			public boolean hasNext() {
				// TODO change current here to guarantee it exists
				return current.getReference().forward[0].getReference().key != Integer.MAX_VALUE;
			}

			@Override
			public Integer next() {
				Node currentNode = current.getReference();
				do {
					current = currentNode.forward[0];
					currentNode = current.getReference();
				} while (current != header && currentNode.markedForRemoval);
				return currentNode.value;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("[");
		Iterator<Integer> iter = iterator();
		while (iter.hasNext()) {
			s.append(iter.next());
			if (iter.hasNext()) s.append(", ");
		}
		s.append(']');
		return s.toString();
	}
}
