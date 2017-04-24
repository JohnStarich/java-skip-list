package com.johnstarich.ee360p.skiplist;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A fine-grained and lock-free skip-list implementation.
 * Created by johnstarich on 4/12/17.
 */
public class SkipList extends AbstractSet<Integer> {
	class Node {
		int key;
		int value;
		int level;
		Node[] forward;

		AtomicBoolean fullyLinked;
		AtomicBoolean markedForRemoval;

		public Node(int key, int value, int level, int maxLevel) {
			this.key = key;
			this.value = value;
			this.level = level;
			this.forward = new Node[maxLevel];
			this.fullyLinked = new AtomicBoolean(false);
			this.markedForRemoval = new AtomicBoolean(false);
		}

		public String toString() {
			return Integer.toString(value);
		}
	}

	final Node header;
	AtomicInteger currentLevels;
	final int maxLevel;
	AtomicInteger size;
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
		header = new Node(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, maxLevel);
		for (int i = 0; i < maxLevel; i += 1) {
			header.forward[i] = header;
		}
	}

	@Override
	public boolean add(Integer value) {
		return insert(value, value);
	}

	private boolean insert(int searchKey, int value) {
		Node[] update = new Node[maxLevel];
		Node current = this.header;
		int levels = currentLevels.get();
		for (int level = levels; level >= 0; level--) {
			while (current.forward[level].key < searchKey) {
				current = current.forward[level];
			}
			update[level] = current;
			current.fullyLinked.compareAndSet(true, false);
		}

		current = current.forward[0];

		if (current.key == searchKey && !current.markedForRemoval.get()) {
			while (!current.fullyLinked.get());
			return false;
		}
		else {
			int newLevel = chooseRandomLevel();

			if (newLevel > currentLevels.get()) {
				newLevel = currentLevels.incrementAndGet();
				levels = newLevel;
				update[newLevel] = header;
			}

			Node newNode = new Node(searchKey, value, newLevel, maxLevel);

			for (int level = 0; level <= levels; level++) {
				newNode.forward[level] = update[level].forward[level];
				update[level].forward[level] = newNode;
			}

			for (int level = 0; level <= levels; level++) {
				update[level].fullyLinked.compareAndSet(false, true);
			}
			newNode.fullyLinked.compareAndSet(false, true);

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
		Node current = this.header;
		for (int level = currentLevels.get(); level >= 0; level--) {
			while (current.forward[level].key < searchKey) {
				current = current.forward[level];
			}
			update[level] = current;
			current.fullyLinked.compareAndSet(true, false);
		}

		current = current.forward[0];

		if (current.key == searchKey && !current.markedForRemoval.get()) {
			current.markedForRemoval.compareAndSet(false, true);
			int levels = currentLevels.get();
			for (int level = 0; level < levels; level++) {
				if (update[level].forward[level] != current) {
					break;
				}
				if (update[level].markedForRemoval.get()) {
					continue;
				}
				update[level].forward[level] = current.forward[level];
			}

			size.decrementAndGet();

			current = null;

			while (levels > 0 && header.forward[levels].equals(header)) {
				levels = currentLevels.decrementAndGet();	
			}

			for (int level = 0; level < levels; level++) {
				update[level].fullyLinked.compareAndSet(false, true);
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
		Node current = this.header;

		for (int level = currentLevels.get(); level >= 0; level--) {
			while (current.forward[level].key < searchKey) {
				current = current.forward[level];
			}
		}

		int limit = current.forward[0].value;
		return limit == searchKey && current.fullyLinked.get() && !current.markedForRemoval.get();
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
			private AtomicReference<Node> current = new AtomicReference<Node>(header);

			@Override
			public boolean hasNext() {
				// TODO change current here to guarantee it exists
				return current.get().key != Integer.MAX_VALUE;
			}

			@Override
			public Integer next() {
				Node original = current.get();
				Node currentNode = null;
				do {
					currentNode = current.get();
					current.compareAndSet(currentNode, currentNode.forward[0]);
				} while (currentNode.key != Integer.MAX_VALUE
						&& (currentNode == original || currentNode.markedForRemoval.get()));
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
