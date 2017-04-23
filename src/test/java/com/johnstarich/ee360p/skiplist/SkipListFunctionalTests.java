package com.johnstarich.ee360p.skiplist;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Random;
import java.util.Iterator;
import java.util.Collections;
import java.util.Arrays;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by Alec on 4/19/2017.
 */
public class SkipListFunctionalTests {


    private final int defaultEntries = 1000;
    private final Random rand = new Random();

    private void makeEqualLists(SkipList skipList, ConcurrentSkipListSet<Integer> benchmark){
        Integer toAdd;
        for(int i = 0; i < defaultEntries; i++){
            do{
                toAdd = rand.nextInt();
                skipList.add(toAdd);
            }while(!benchmark.add(toAdd));
        }
    }
    private SkipList createSkipList(int entries){
        int numLevels = 3;
        SkipList skipList = new SkipList(numLevels);
        for(int i = 0; i < entries; i++){
            while(!skipList.add(rand.nextInt()));
        }
        return skipList;
    }

    private ConcurrentSkipListSet<Integer> createConcurrentSkipListSet(int entries){
        ConcurrentSkipListSet<Integer> skipList = new ConcurrentSkipListSet<Integer>();
        for(int i = 0; i < entries; i++){
            while(!skipList.add(rand.nextInt()));
        }
        return skipList;
    }

    private boolean orderInvariant(SkipList skipList){
        int size = skipList.size();
        if(size <= 1){
            return true;
        }

        Iterator iterator = skipList.iterator();
        int previous = (Integer)iterator.next();
        int next;
        for(int i = 1; i < size; i++){
            next = (Integer)iterator.next();
            if(previous > next){
                return false;
            }
            previous = next;
        }
        return true;
    }

    @Test
    public void addReturnTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        boolean addResult;
        Integer toAdd;

        for(int i = 0; i < defaultEntries; i++){
            do{
                toAdd = rand.nextInt();
                addResult = benchmark.add(toAdd);
                assertEquals(addResult, skipList.add(toAdd));
            }while(!addResult);
        }
    }

    @Test
    public void addNullTest(){
        SkipList skipList = createSkipList(0);
        Throwable ex = null;
        try {
            skipList.add(null);
        }catch(NullPointerException e){
            ex = e;
        }
        assertNotNull(ex);
    }

    @Test
    public void addSizeTest(){
        SkipList skipList = createSkipList(0);
        for(int i = 0; i < defaultEntries; i++){
            while(!skipList.add(rand.nextInt()));
            assertEquals(i+1,skipList.size());
        }
    }

    @Test
    public void addOrderTest(){
        SkipList skipList = createSkipList(0);
        for(int i = 0; i < defaultEntries; i++){
            while(!skipList.add(rand.nextInt()));
            assertTrue(orderInvariant(skipList));
        }
    }

    @Test
    public void addEqualTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        boolean addResult;
        Integer toAdd;

        for(int i = 0; i < defaultEntries; i++){
            do{
                toAdd = rand.nextInt();
                addResult = benchmark.add(toAdd);
                skipList.add(toAdd);
            }while(!addResult);
            assertEquals(skipList.toString(), benchmark.toString());
        }
    }

    @Test
    public void createEmptyIteratorTest(){
        SkipList skipList = createSkipList(0);
        Throwable ex = null;
        try{
            Iterator iterator = skipList.iterator();
            assertFalse(iterator.hasNext());
        }catch(Exception e){
            ex = e;
        }
        assertNull(ex);
    }

    @Test
    public void createIteratorTest(){
        SkipList skipList = createSkipList(defaultEntries);
        Iterator iterator = skipList.iterator();
        for(int i = 0; i < defaultEntries; i++){
            assertTrue(iterator.hasNext());
            iterator.next();
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    public void containsReturnTest(){
        SkipList skipList = createSkipList(0);
        boolean addResult;
        Integer toAdd;

        for(int i = 0; i < defaultEntries; i++){
            do{
                toAdd = rand.nextInt();
                addResult = skipList.contains(toAdd);
                assertNotEquals(addResult, skipList.add(toAdd));
            }while(addResult);
        }
    }

    @Test
    public void containsNullTest(){
        SkipList skipList = createSkipList(0);
        Throwable ex = null;
        try{
            skipList.contains(null);
        }catch(Exception e){
            ex = e;
        }
        assertNotNull(ex);
    }

    @Test
    public void containsNotIntTest(){
        SkipList skipList = createSkipList(0);
        assertFalse(skipList.contains(new Double(20.0)));
    }

    @Test
    public void zeroSizeTest(){
        SkipList skipList = createSkipList(0);
        assertEquals(0,skipList.size());
    }

    @Test
    public void removeReturnTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        makeEqualLists(skipList,benchmark);
        for(int i = 0; i < defaultEntries; i++){
            while(!benchmark.add(rand.nextInt()));
        }
        Object[] benchmarkArr = benchmark.toArray();
        Collections.shuffle(Arrays.asList(benchmarkArr));

        int size = benchmarkArr.length;
        for(int i = 0; i < size; i++){
            assertEquals(skipList.contains((Integer)benchmarkArr[i]), skipList.remove((Integer)benchmarkArr[i]));
        }
    }

    @Test
    public void removeNullTest(){
        SkipList skipList = createSkipList(0);
        Throwable ex = null;
        try {
            skipList.remove(null);
        }catch(NullPointerException e){
            ex = e;
        }
        assertNotNull(ex);
    }


    @Test
    public void removeNotIntTest(){
        SkipList skipList = createSkipList(0);
        assertFalse(skipList.remove(new Double(20.0)));
    }

    @Test
    public void removeSizeTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        makeEqualLists(skipList,benchmark);
        for(int i = 0; i < defaultEntries; i++){
            while(!benchmark.add(rand.nextInt()));
        }
        Object[] benchmarkArr = benchmark.toArray();
        Collections.shuffle(Arrays.asList(benchmarkArr));

        int benchSize = benchmarkArr.length;
        int skipListSize = skipList.size();
        for(int i = 0; i < benchSize; i++){
            if(skipList.remove((Integer)benchmarkArr[i])) {
                skipListSize--;
            }
            assertEquals(skipList.size(),skipListSize);
        }
    }

    @Test
    public void removeOrderTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        makeEqualLists(skipList,benchmark);
        Object[] benchmarkArr = benchmark.toArray();
        Collections.shuffle(Arrays.asList(benchmarkArr));

        int size = skipList.size();
        for(int i = 0; i < size; i++){
            skipList.remove(benchmarkArr[i]);
            assertTrue(orderInvariant(skipList));
        }
    }

    @Test
    public void removeEqualTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        makeEqualLists(skipList,benchmark);
        Object[] benchmarkArr = benchmark.toArray();
        Collections.shuffle(Arrays.asList(benchmarkArr));

        int size = skipList.size();
        for(int i = 0; i < size; i++){
            skipList.remove(benchmarkArr[i]);
            benchmark.remove(benchmarkArr[i]);
            assertEquals(skipList.toString(),benchmark.toString());
        }
    }

    @Test
    public void emptyToStringTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        assertEquals(skipList.toString(), benchmark.toString());
    }

    @Test
    public void toStringTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        makeEqualLists(skipList,benchmark);
        assertEquals(benchmark.toString(), skipList.toString());
    }
}
