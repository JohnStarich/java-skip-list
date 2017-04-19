package com.johnstarich.ee360p.skiplist;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

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
	int currentLevels;
	int maxLevel;

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
			while (current.forward.get(level).key < searchKey) {
				current = current.forward.get(level);
			}
			update.add(current);
		}
		return false; // TODO
	}

	public void remove(int value, int key) {
	}

	/**
	 * Returns true if this set contains the specified element.
	 * @param searchKey the value to find in this SkipList
	 */
	public boolean contains(int searchKey) {
		Node current = this.header;

		for (int level = current.forward.size(); level >= 0; level--) {
			while (current.forward.get(level).key < searchKey) {
				current = current.forward.get(level);
			}
		}
	
		int limit = current.forward.get(1).value;
		return limit == searchKey;
	}

	public int size() {
		return 0;
	}

	public Iterator<Integer> iterator() {
		return null;
	}
}
