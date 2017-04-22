package com.johnstarich.ee360p.skiplist;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.rules.Timeout;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test for SkipList.
 * Created by johnstarich on 4/12/17.
 */
public class SkipListEdgeTest {
	@Rule public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);
	@Rule public TestName testName = new TestName();

	@Before
	public void setUp() {
		System.out.println("\n\nStarting test: " + testName.getMethodName());
	}

	@Test
	public void add() {
		SkipList s = new SkipList(5);
		assertTrue(s.add(1));
		assertEquals(1, s.size());
		assertFalse(s.add(1));
		assertEquals(1, s.size());
	}


	@Test
	public void remove() {
		SkipList s = new SkipList(5);
		assertEquals(0, s.size());
		assertFalse(s.remove(1));
		assertEquals(0, s.size());
		s.add(1);
		assertTrue(s.remove(1));
		assertEquals(0, s.size());
	}

	@Test
	public void contains() {
		SkipList s = new SkipList(5);
		assertFalse(s.contains(1));
		s.add(1);
		assertTrue(s.contains(1));
	}

	@Test
	public void addMultiple() {
		SkipList s = new SkipList(5);
		s.add(1);
		s.add(5);
		Iterator<Integer> i = s.iterator();
		assertEquals(1, i.next().intValue());
		assertEquals(5, i.next().intValue());

		i = s.iterator();
		s.add(2);
		i.next();
		assertEquals(2, i.next().intValue());
	}

	@Test
	public void addLots1() {
		SkipList s = new SkipList(5);
		System.out.println(s);
		s.add(5);
		System.out.println(s);
		s.add(2);
		System.out.println(s);
		s.add(4);
		System.out.println(s);
		s.add(3);
		System.out.println(s);
		Iterator<Integer> iter = s.iterator();
		assertEquals(2, iter.next().intValue());
		assertEquals(3, iter.next().intValue());
		assertEquals(4, iter.next().intValue());
		assertEquals(5, iter.next().intValue());
	}

	@Test
	public void addLots() {
		SkipList s = new SkipList(5);
		for (int i = 100; i >= 0; i -= 1) {
			s.add(i);
		}
		Iterator<Integer> iter = s.iterator();
		for (int i = 0; i <= 100; i += 1) {
			assertEquals(i, iter.next().intValue());
		}

		for (int i = 200; i > 100; i -= 1) {
			s.add(i);
		}
		
		iter = s.iterator();
		for (int i = 0; i <= 200; i += 1) {
			assertEquals(i, iter.next().intValue());
		}
	}
}
