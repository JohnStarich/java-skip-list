package com.johnstarich.ee360p.skiplist;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Random;

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

		boolean fullyLinked;
		boolean markedForRemoval;
		boolean markedForInsertion;

		public Node(int key, int value, int level, int maxLevel) {
			this.key = key;
			this.value = value;
			this.level = level;
			this.forward = new Node[maxLevel];
			this.fullyLinked = false;
		}

		public String toString() {
			return Integer.toString(value);
		}
	}

	Node header;
	int currentLevels;
	int maxLevel;
	int size;
	float p = 0.5f;

	/**
	 * Create a skip list with a maximum level.
	 * TODO: add complexity description
	 * @param maxLevel The maximum level for this SkipList
	 */
	public SkipList(int maxLevel) {
		this.currentLevels = 0;
		this.size = 0;
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
		while (true) {
			Node[] update = new Node[maxLevel];
			Node current = this.header;
			for (int level = currentLevels; level >= 0; level--) {
				while (current.forward[level].key < searchKey) {
					current = current.forward[level];
				}
				update[level] = current;
				current.fullyLinked = false;
			}

			current = current.forward[0];

			if (current.key == searchKey && !current.markedForRemoval) {
				while (!current.fullyLinked);
				return false;
			}
			else {
				int newLevel = chooseRandomLevel();

				if (newLevel > currentLevels) {
					newLevel = currentLevels + 1;
					currentLevels = newLevel;
					update[newLevel] = header;
				}

				Node newNode = new Node(searchKey, value, newLevel, maxLevel);

				for (int level = 0; level <= currentLevels; level++) {
					newNode.forward[level] = update[level].forward[level];
					update[level].forward[level] = newNode;
				}

				for (int level = 0; level <= currentLevels; level++) {
					update[level].fullyLinked = true;
				}
				newNode.fullyLinked = true;

				size++;

				return true;
			}
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
		for (int level = currentLevels; level >= 0; level--) {
			while (current.forward[level].key < searchKey) {
				current = current.forward[level];
			}
			update[level] = current;
		}

		current = current.forward[0];

		if (current.key == searchKey) {
			for (int level = 0; level < currentLevels; level++) {
				if (!update[level].forward[level].equals(current)) {
					break;
				}
				update[level].forward[level] = current.forward[level];
			}
			size--;
			current = null;

			while (currentLevels > 0 && header.forward[currentLevels].equals(header)) {
				currentLevels--;
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

		for (int level = currentLevels; level >= 0; level--) {
			while (current.forward[level].key < searchKey) {
				current = current.forward[level];
			}
		}

		int limit = current.forward[0].value;
		return limit == searchKey && current.fullyLinked && !current.markedForRemoval;
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
		return size;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private Node current = header.forward[0];

			@Override
			public boolean hasNext() {
				return current.key != Integer.MAX_VALUE;
			}

			@Override
			public Integer next() {
				Integer value = current.value;
				current = current.forward[0];
				return value;
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
