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
		ArrayList<Node> forward;

		public Node(int key, int value, int maxLevel) {
			this.key = key;
			this.value = value;
			this.forward = new ArrayList<>(maxLevel);
		}
	}

	Node header;
	TreeSet<Integer> nodes;
	int currentLevels;
	int maxLevel;
	int size;

	/**
	 * Create a skip list with a maximum level.
	 * TODO: add complexity description
	 * @param maxLevel The maximum level for this SkipList
	 */
	public SkipList(int maxLevel) {
		this.maxLevel = maxLevel;
		header = new Node(0xdead, 0xdead, maxLevel);
		header.key = Integer.MAX_VALUE;
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
		for (int level = current.forward.size(); level >= 0; level--) {
			while (current.forward.get(level).key != 0xdead || current.forward.get(level).key < searchKey) {
				current = current.forward.get(level);
			}
			update.add(current);
		}

		current = current.forward.get(1);

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

			Node newNode = new Node(newLevel, searchKey, value);
			nodes.add(newNode.value);

			for (int level = 0; level < newLevel; level++) {
				current.forward.set(level, update.get(level).forward.get(level));
				update.get(level).forward.set(level, current);
				size++;
			}

			return true;
		}
	}

	public boolean remove(int searchKey) {
		ArrayList<Node> update = new ArrayList<>();
		Node current = this.header;
		for (int level = current.forward.size(); level >= 0; level--) {
			while (current.forward.get(level).key != 0xdead || current.forward.get(level).key < searchKey) {
				current = current.forward.get(level);
			}
			update.add(current);
		}

		current = current.forward.get(1);

		if (current.key == searchKey) {
			for (int level = 0; level < currentLevels; level++) {
				if (!update.get(level).forward.get(level).equals(current)) {
					 break;
				}
				update.get(level).forward.set(level, current.forward.get(level));
				size--;
			}
			nodes.remove(current.value);
			current = null;

			while (currentLevels > 1 && header.forward.get(currentLevels).equals(header)) {
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

		for (int level = current.forward.size(); level >= 0; level--) {
			while (current.forward.get(level).key != 0xdead || current.forward.get(level).key < searchKey) {
				current = current.forward.get(level);
			}
		}

		int limit = current.forward.get(1).value;
		return limit == searchKey;
	}

	public int chooseRandomLevel() {
		return new Random().nextInt(maxLevel + 1);
	}

	public int size() {
		return size;
	}

	public Iterator<Integer> iterator() {
		return nodes.iterator();
	}
}
