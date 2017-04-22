package com.johnstarich.ee360p.skiplist;

import java.util.AbstractSet;
import java.util.ArrayList;
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
		ArrayList<Node> forward;

		public Node(int key, int value, int level, int maxLevel) {
			this.key = key;
			this.value = value;
			this.level = level;
			this.forward = new ArrayList<>(maxLevel);

			for (int i = 0; i < maxLevel; i += 1) {
				this.forward.add(null);
			}
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
			header.forward.set(i, header);
		}
	}

	@Override
	public boolean add(Integer value) {
		return insert(value, value);
	}

	private boolean insert(int searchKey, int value) {
		ArrayList<Node> update = new ArrayList<>();
		for (int i = 0; i <= currentLevels; i += 1) {
			update.add(null);
		}
		Node current = this.header;
		for (int level = currentLevels; level >= 0; level--) {
			while (current.forward.get(level).key < searchKey) {
				current = current.forward.get(level);
			}
			update.set(level, current);
		}

		current = current.forward.get(0);

		if (current.key == searchKey) {
			current.value = value;
			return false;
		}
		else {
			int newLevel = chooseRandomLevel();

			if (newLevel > currentLevels) {
				newLevel = currentLevels + 1;
				currentLevels = newLevel;
				update.add(header);
			}

			Node newNode = new Node(searchKey, value, newLevel, maxLevel);

			for (int level = 0; level <= currentLevels; level++) {
				newNode.forward.set(level, update.get(level).forward.get(level));
				update.get(level).forward.set(level, newNode);
			}
			size++;

			return true;
		}
	}

	@Override
	public boolean remove(Object value) {
		if (! (value instanceof Integer)) {
			return false;
		}
		int searchKey = (Integer)value;
		ArrayList<Node> update = new ArrayList<>();
		Node current = this.header;
		for (int level = currentLevels; level >= 0; level--) {
			while (current.forward.get(level).key < searchKey) {
				current = current.forward.get(level);
			}
			update.add(current);
		}

		current = current.forward.get(0);

		if (current.key == searchKey) {
			for (int level = 0; level < currentLevels; level++) {
				if (!update.get(level).forward.get(level).equals(current)) {
					break;
				}
				update.get(level).forward.set(level, current.forward.get(level));
			}
			size--;
			current = null;

			while (currentLevels > 0 && header.forward.get(currentLevels).equals(header)) {
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
			while (current.forward.get(level).key < searchKey) {
				current = current.forward.get(level);
			}
		}

		int limit = current.forward.get(0).value;
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
		return size;
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			private Node current = header.forward.get(0);

			@Override
			public boolean hasNext() {
				return current.key != Integer.MAX_VALUE;
			}

			@Override
			public Integer next() {
				Integer value = current.value;
				current = current.forward.get(0);
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
