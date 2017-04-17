package com.johnstarich.ee360p.skiplist;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A fine-grained and lock-free skip-list implementation.
 * Created by johnstarich on 4/12/17.
 */
public class SkipList extends AbstractSet<Integer> {
	Node header;
	int maxLevel;

	public SkipList(int maxLevel) {
		this.maxLevel = maxLevel;
		header = new Node(0xdead, 0xdead, maxLevel);
		header.key = Integer.MAX_VALUE;
		for(int i = 0; i < maxLevel; i += 1) {
			header.forward.set(i, header);
		}
	}

	public void add(int value) {
	}

	public void delete(int value) {
	}

	public void search(int value) {
	}

	public int size() {
		return 0;
	}

	public Iterator<Integer> iterator() {
		return null;
	}
}

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
