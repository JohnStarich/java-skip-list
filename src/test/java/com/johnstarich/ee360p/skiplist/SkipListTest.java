package com.johnstarich.ee360p.skiplist;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Test for SkipList.
 * Created by johnstarich on 4/12/17.
 */
public class SkipListTest {
    @Test
    public void construct() {
        assertNotNull(new SkipList());
    }
}
