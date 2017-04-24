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
		AtomicMarkableReference<Node> current = new AtomicMarkableReference<Node>(
				this.header.getReference(), true);
		int levels = currentLevels.get();
		for (int level = levels; level >= 0; level--) {
			Node currentNode;
			do {
				currentNode = current.getReference();
				current = currentNode.forward[level];
			} while (currentNode.key < searchKey || currentNode.markedForRemoval);
			while (! current.compareAndSet(currentNode, currentNode, true, false));
			update[level] = currentNode;
		}

		Node currentNode;
		do {
			currentNode = current.getReference();
			current = currentNode.forward[0];
		} while (currentNode.markedForRemoval);

		if (currentNode.key == searchKey) {
			for (int level = 0; level < levels; level++) {
				AtomicMarkableReference<Node> link = update[level].forward[level];
				while (! link.compareAndSet(currentNode, currentNode, false, true));
			}
			return false;
		}
		else {
			int newLevel = chooseRandomLevel();

			if (newLevel > currentLevels.get()) {
				newLevel = currentLevels.incrementAndGet();
				levels = newLevel;
				update[newLevel] = header.getReference();
			}

			Node newNode = new Node(searchKey, value, newLevel, maxLevel);
			AtomicMarkableReference<Node> atomicNewNode =
					new AtomicMarkableReference<>(newNode, true);

			for (int level = 0; level <= levels; level++) {
				Node next = update[level].forward[level].getReference();
				newNode.forward[level] = atomicNewNode;
				while (! update[level].forward[level].compareAndSet(next, newNode, false, true));
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
		for (int level = levels; level >= 0; level--) {
			Node currentNode;
			do {
				currentNode = current.getReference();
				current = currentNode.forward[level];
				// TODO this may be checking the wrong node's key
			} while (currentNode.key < searchKey || currentNode.markedForRemoval);
			while (! current.compareAndSet(currentNode, currentNode, true, false));
			update[level] = currentNode;
		}

		Node currentNode = current.getReference();
		current = currentNode.forward[0];
		if (current == header || currentNode.markedForRemoval) {
			for (int level = 0; level <= levels; level++) {
				AtomicMarkableReference<Node> link = update[level].forward[level];
				while (! link.compareAndSet(currentNode, currentNode, false, true));
			}
			return false;
		}

		if (currentNode.key == searchKey) {
			currentNode.markedForRemoval = true;
			for (int level = 0; level < levels; level++) {
				AtomicMarkableReference<Node> link = update[level].forward[level];
				Node next;
				do {
					next = currentNode.forward[level].getReference();
				} while (! link.compareAndSet(currentNode, next, false, true));
			}

			size.decrementAndGet();

			while (levels > 0 && header.getReference().forward[levels] == header) {
				levels = currentLevels.decrementAndGet();
			}

			for (int level = 0; level <= levels; level++) {
				AtomicMarkableReference<Node> link = update[level].forward[level];
				while (! link.compareAndSet(currentNode, currentNode, false, true));
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

		for (int level = currentLevels.get(); level >= 0; level--) {
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
				return current.getReference().key != Integer.MAX_VALUE;
			}

			@Override
			public Integer next() {
				Node original = current.getReference();
				Node currentNode;
				do {
					currentNode = current.getReference();
					current = currentNode.forward[0];
				} while (currentNode.key != Integer.MAX_VALUE
						&& (currentNode == original || currentNode.markedForRemoval));
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
		for (Integer i : this) {
			s.append(i).append(", ");
		}
		s.append(']');
		return s.toString();
	}
}
