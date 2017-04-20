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
		this.maxLevel = maxLevel;
		header = new Node(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, maxLevel);
		for (int i = 0; i < maxLevel; i += 1) {
			header.forward.set(i, header);
		}
	}

	public boolean add(int value) {
		return insert(value, value);
	}

	private boolean insert(int searchKey, int value) {
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
			current.value = value;
			return false;
		}
		else {
			int newLevel = chooseRandomLevel();

			if (newLevel > currentLevels) {
				newLevel = currentLevels + 1;
				currentLevels = newLevel;
				update.set(newLevel, header);
			}

			Node newNode = new Node(searchKey, value, newLevel, maxLevel);

			for (int level = 0; level <= newLevel; level++) {
				newNode.forward.set(level, update.get(level).forward.get(level));
				update.get(level).forward.set(level, newNode);
			}
			size++;

			return true;
		}
	}

	/**
	 * Removes a single instance of the specified element from this collection, if it is
	 * present (optional operation).
	 * @param searchKey element to remove
	 */
	public boolean remove(int searchKey) {
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
				size--;
			}
			current = null;

			while (currentLevels > 0 && header.forward.get(currentLevels).equals(header)) {
				currentLevels--;
			}

			return true;
		}

		return false;
	}

	/**
	 * Returns true if this set contains the specified element.
	 * @param searchKey the value to find in this SkipList
	 */
	public boolean contains(int searchKey) {
		Node current = this.header;

		for (int level = currentLevels; level >= 0; level--) {
			while (current.forward.get(level).key < searchKey) {
				current = current.forward.get(level);
			}
		}

		int limit = current.forward.get(1).value;
		return limit == searchKey;
	}

	private Random levelRandom = new Random(0);

	private int chooseRandomLevel() {
		int newLevel = 0;
		while (newLevel < maxLevel && levelRandom.nextFloat() < this.p) {
			newLevel += 1;
		}
		return newLevel;
	}

	public int size() {
		return size;
	}

	public Iterator<Integer> iterator() {
		return null;
	}
}
