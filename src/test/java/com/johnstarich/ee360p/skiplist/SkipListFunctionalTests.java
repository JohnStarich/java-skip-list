package com.johnstarich.ee360p.skiplist;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Random;
import java.util.Iterator;
import java.util.Collections;
import java.util.Arrays;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Alec on 4/19/2017.
 */

/*apparent bugs
    remove does not cause change in iterator or toString
    toString has extra , on last element
    contains, add, and remove need to consider null input
    sometimes hanging in parallel operations
 */
public class SkipListFunctionalTests {

    private final Random rand = new Random();
    private final int numThreads = 20, defaultEntries = 1000, numLevels = 3;
    private static CyclicBarrier start, done;
    private boolean parallelPass;

    private void makeEqualLists(SkipList skipList, ConcurrentSkipListSet<Integer> benchmark){
        Integer toAdd;
        for(int i = 0; i < defaultEntries; i++){
            do{
                toAdd = rand.nextInt();
            }while(!benchmark.add(toAdd));
            skipList.add(toAdd);
        }
    }
    private SkipList createSkipList(int entries){
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
    public void addEqualIteratorTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        boolean addResult;
        Integer toAdd;
        Iterator benchIterate;
        Iterator skipListIterate;

        for(int i = 0; i < defaultEntries; i++){
            do{
                toAdd = rand.nextInt();
                addResult = benchmark.add(toAdd);
                skipList.add(toAdd);
            }while(!addResult);
            benchIterate = benchmark.iterator();
            skipListIterate = skipList.iterator();
            while(benchIterate.hasNext()){
                assertEquals(benchIterate.next(),skipListIterate.next());
            }
        }
    }

    @Test
    public void addEqualToStringTest(){
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
    public void removeEqualToStringTest(){
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
    public void removeEqualIteratorTest(){
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        makeEqualLists(skipList,benchmark);
        Object[] benchmarkArr = benchmark.toArray();
        Collections.shuffle(Arrays.asList(benchmarkArr));
        Iterator benchIterate;
        Iterator skipListIterate;

        int size = skipList.size();
        for(int i = 0; i < size; i++){
            skipList.remove(benchmarkArr[i]);
            benchmark.remove(benchmarkArr[i]);
            benchIterate = benchmark.iterator();
            skipListIterate = skipList.iterator();
            while(benchIterate.hasNext()){
                assertEquals(benchIterate.next(),skipListIterate.next());
            }
        }
    }

    @Test
    public void removeContainsTest(){
        SkipList skipList = createSkipList(0);
        skipList.add(0);
        skipList.remove(0);
        assertFalse(skipList.contains(0));
    }

    @Test
    public void removeAddTest(){
        SkipList skipList = createSkipList(0);
        skipList.add(0);
        skipList.remove(0);
        assertTrue(skipList.add(0));
    }

    @Test
    public void doubleRemoveReturnTest(){
        SkipList skipList = createSkipList(0);
        skipList.add(1);
        skipList.remove(1);
        assertFalse(skipList.remove(1));
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

    @Test
    public void parallelAddOrderTest(){
        Thread toRun;
        SkipList skipList = createSkipList(0);
        parallelPass = false;
        start = new CyclicBarrier(numThreads);
        class AddOrderThread extends Thread{
            public void run(){
                try {
                    start.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                Integer toAdd = rand.nextInt();
                skipList.add(toAdd);
                parallelPass = orderInvariant(skipList);
                try {
                    done.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        done = new CyclicBarrier(numThreads + 1);
        for(int i = 0; i < numThreads; i++){
            toRun = new AddOrderThread();
            toRun.start();
        }
        try {
            done.await();
        }catch(BrokenBarrierException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        assertTrue(parallelPass);
    }

    @Test
    public void parallelAddContentTest(){
        Thread toRun;
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        start = new CyclicBarrier(numThreads);
        class AddContentThread extends Thread{
            public void run(){
                try {
                    start.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                Integer toAdd = rand.nextInt();
                skipList.add(toAdd);
                benchmark.add(toAdd);
                try {
                    done.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        done = new CyclicBarrier(numThreads + 1);
        for(int i = 0; i < numThreads; i++){
            toRun = new AddContentThread();
            toRun.start();
        }
        try {
            done.await();
        }catch(BrokenBarrierException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        Iterator skipListIterate = skipList.iterator();
        Iterator benchIterate = benchmark.iterator();
        while(benchIterate.hasNext()){
            assertEquals(benchIterate.next(),skipListIterate.next());
        }
    }

    @Test
    public void parallelRemoveContentTest(){
        Thread toRun;
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        makeEqualLists(skipList,benchmark);
        Object[] benchmarkArr = benchmark.toArray();
        int size = benchmarkArr.length;
        start = new CyclicBarrier(numThreads);
        class RemoveContentThread extends Thread{
            public void run(){
                try {
                    start.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                Integer toRemove = rand.nextInt(size);
                skipList.remove(benchmarkArr[toRemove]);
                benchmark.remove(benchmarkArr[toRemove]);
                try {
                    done.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        done = new CyclicBarrier(numThreads + 1);
        for(int i = 0; i < numThreads; i++){
            toRun = new RemoveContentThread();
            toRun.start();
        }
        try {
            done.await();
        }catch(BrokenBarrierException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        Iterator skipListIterate = skipList.iterator();
        Iterator benchIterate = benchmark.iterator();
        while(benchIterate.hasNext()){
            assertEquals(benchIterate.next(),skipListIterate.next());
        }
    }

    @Test
    public void parallelAddSizeTest() {
        Thread toRun;
        SkipList skipList = createSkipList(0);
        start = new CyclicBarrier(numThreads);
        class AddSizeThread extends Thread {
            public void run() {
                try {
                    start.await();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while(!skipList.add(rand.nextInt()));
                try {
                    done.await();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        done = new CyclicBarrier(numThreads + 1);
        for(int i = 0; i < numThreads; i++){
            toRun = new AddSizeThread();
            toRun.start();
        }
        try {
            done.await();
        }catch(BrokenBarrierException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        assertEquals(skipList.size(),numThreads);
    }

    @Test
    public void parallelRemoveSizeTest(){
        Thread toRun;
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> benchmark = createConcurrentSkipListSet(0);
        makeEqualLists(skipList,benchmark);
        Object[] benchmarkArr = benchmark.toArray();
        int size = benchmarkArr.length;
        start = new CyclicBarrier(numThreads);
        class RemoveSizeThread extends Thread{
            public void run(){
                try {
                    start.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                Integer toRemove = rand.nextInt(size);
                skipList.remove(benchmarkArr[toRemove]);
                benchmark.remove(benchmarkArr[toRemove]);
                try {
                    done.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        done = new CyclicBarrier(numThreads + 1);
        for(int i = 0; i < numThreads; i++){
            toRun = new RemoveSizeThread();
            toRun.start();
        }
        try {
            done.await();
        }catch(BrokenBarrierException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        assertEquals(benchmark.size(),skipList.size());
    }

    @Test
    public void parallelContainsTest(){
        Thread toRun;
        parallelPass = true;
        SkipList skipList = createSkipList(0);
        ConcurrentSkipListSet<Integer> allList = createConcurrentSkipListSet(0);
        int[] contained = new int[numThreads];
        int[] notContained = new int[numThreads];
        Integer toAdd;
        for(int i = 0; i < numThreads; i++){
            do{
                toAdd = rand.nextInt();
            }while(!skipList.add(toAdd));
            allList.add(toAdd);
            contained[i] = toAdd;
        }
        for(int j = 0; j < numThreads; j++){
            do{
                toAdd = rand.nextInt();
            }while(!allList.add(toAdd));
            notContained[j] = toAdd;
        }
        start = new CyclicBarrier(2 * numThreads);
        done = new CyclicBarrier(2 * numThreads + 1);
        class ContainsThread extends Thread{
            public void run(){
                try {
                    start.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                parallelPass = skipList.contains(contained[rand.nextInt(numThreads)]);
                try {
                    done.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        class NotContainsThread extends Thread{
            public void run(){
                try {
                    start.await();
                }catch(BrokenBarrierException e){
                    e.printStackTrace();
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                parallelPass = !skipList.contains(notContained[rand.nextInt(numThreads)]);
                    try {
                        done.await();
                    }catch(BrokenBarrierException e){
                        e.printStackTrace();
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
            }
        }

        for(int i = 0; i < 2 * numThreads; i++){
            toRun = new ContainsThread();
            toRun.start();
        }
        try {
            done.await();
        }catch(BrokenBarrierException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        assertTrue(parallelPass);
    }
}