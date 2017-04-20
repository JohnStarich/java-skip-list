package com.johnstarich.ee360p.skiplist;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JavaConcurrentSkipListSetTest {

    @Test
    public void helloTest() throws Exception {
        System.out.print("こんにちわ");
    }


    // Benchmarks
    private Map<String, List<Long>> bench(List<Integer> rounds, BenchRunner br) {
        Map<String, AbstractSet<Integer>> sets = new HashMap<>();
        sets.put("JavaIMPL", new ConcurrentSkipListSet<>());
        sets.put("OurIMPL", new SkipList(10)); // TODO Remove max level

        Map<String, List<Long>> results = new HashMap<>();
        for(Map.Entry<String, AbstractSet<Integer>> entry: sets.entrySet()) {
            results.put(entry.getKey(), new ArrayList<>());
            for(int round: rounds) {
                try {
                    long res = br.bench(sets.get(entry.getKey()), round);
                    results.get(entry.getKey()).add(res);
                } catch (Exception e) {
                    e.printStackTrace();
                    results.get(entry.getKey()).add(-1L);
                }
            }
        }

        return results;
    }

    private Map<String, List<Long>> benchConcurrent(List<Integer> rounds, long nodes, BenchRunner br) {
        Map<String, AbstractSet<Integer>> sets = new HashMap<>();
        sets.put("JavaIMPL", new ConcurrentSkipListSet<>());
        sets.put("OurIMPL", new SkipList(10)); // TODO Remove max level

        Map<String, List<Long>> results = new HashMap<>();
        for(Map.Entry<String, AbstractSet<Integer>> entry: sets.entrySet()) {
            results.put(entry.getKey(), new ArrayList<>());
            for(int round: rounds) {
                long resultStream[] = LongStream.range(0L,nodes).parallel().map(n -> {
                    try {
                        return br.bench(sets.get(entry.getKey()), (int)(round/nodes));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return -1;
                    }
                }).toArray();

                long sum = 0;
                for(long n: resultStream) {
                    sum += n;
                    if(n == -1L) {
                        results.get(entry.getKey()).add(-1L);
                    }
                }
                results.get(entry.getKey()).add(sum);
            }
        }

        return results;
    }

    private void benchRunner(String name, int n, BenchRunner br) {
        System.out.println("\n\nStarting " + name + " Benchmark Sync");
        for(int number = 0; number < 10; number++) {
            List<Integer> rounds = new ArrayList<>();
            for(int i = 0; i < n; i ++) {
                rounds.add((int)Math.pow(2, i));
            }

            for(Map.Entry<String, List<Long>> entry: bench(rounds, br).entrySet()) {
                System.out.print(entry.getKey() + " " + number);
                for(int i = 0; i < entry.getValue().size(); i++) {
                    System.out.print("\t" + entry.getValue().get(i));
                }
                System.out.println();

                assertFalse(entry.getValue().parallelStream().anyMatch(a -> a < 0));
            }
        }

        for (int nodes = 0; nodes < 4; nodes++) {
            long powNodes = (long)Math.pow(2, nodes);
            System.out.println("\n\nStarting " + name + " Benchmark Concurrent " + powNodes);
            for(int number = 0; number < 10; number++) {
                List<Integer> rounds = new ArrayList<>();
                for(int i = 0; i < n; i ++) {
                    rounds.add((int)Math.pow(2, i));
                }

                for(Map.Entry<String, List<Long>> entry: benchConcurrent(rounds, powNodes, br).entrySet()) {
                    System.out.print(entry.getKey() + " " + number);
                    for(int i = 0; i < entry.getValue().size(); i++) {
                        System.out.print("\t" + entry.getValue().get(i));
                    }
                    System.out.println();

                    assertFalse(entry.getValue().parallelStream().anyMatch(a -> a < 0));
                }
            }
        }
    }

    @Test
    public void helloBenchmarkTest() throws Exception {
        BenchRunner simple = (list, rounds) -> 42;
        assertEquals(simple.bench(new ConcurrentSkipListSet<>(), 0), 42);

        BenchRunner trivial = (list, rounds) -> list.size() * rounds;
        assertEquals(trivial.bench(new ConcurrentSkipListSet<>(Arrays.asList(10, 11, 12)), 10), 30);
    }

    @Test
    public void InsertTimingBenchmarkTest() throws Exception {
        BenchRunner insertBR = (list, rounds) -> {
            long time = System.nanoTime();

            for(int i = 0; i < rounds; i++) {
                list.add(i);
            }

            return System.nanoTime() - time;
        };

        benchRunner("Insert", 20, insertBR);
    }

    @Test
    public void RemoveHeadTimingBenchmarkTest() throws Exception {
        BenchRunner insertBR = (list, rounds) -> {
            for(int i = 0; i < rounds; i++) {
                list.add(i);
            }

            long time = System.nanoTime();

            for(int i = 0; i < rounds; i++) {
                list.remove(i);
            }

            return System.nanoTime() - time;
        };

        benchRunner("RemoveHead", 20, insertBR);
    }

    @Test
    public void RemoveTailTimingBenchmarkTest() throws Exception {
        BenchRunner insertBR = (list, rounds) -> {
            for(int i = 0; i < rounds; i++) {
                list.add(i);
            }

            long time = System.nanoTime();

            for(int i = 0; i < rounds; i++) {
                list.remove((int)rounds - 1 - i);
            }

            return System.nanoTime() - time;
        };

        benchRunner("RemoveTail", 20, insertBR);
    }


    @Test
    public void RemoveRandomTimingBenchmarkTest() throws Exception {
        BenchRunner insertBR = (list, rounds) -> {
            for(int i = 0; i < rounds; i++) {
                list.add(i);
            }



            List<Integer> toRemove = new ArrayList<>();
            IntStream.range(0,rounds).forEach(toRemove::add);
            Collections.shuffle(toRemove, new Random(12345678));

            long time = System.nanoTime();

            toRemove.forEach(list::remove);

            return System.nanoTime() - time;
        };

        benchRunner("RemoveRandom", 20, insertBR);
    }


    @Test
    public void ContainsRandomTimingBenchmarkTest() throws Exception {
        BenchRunner insertBR = (list, rounds) -> {
            for(int i = 0; i < rounds; i++) {
                list.add(i);
            }



            List<Integer> toCheck = new ArrayList<>();
            IntStream.range(0,rounds).forEach(toCheck::add);
            Collections.shuffle(toCheck, new Random(23456789));

            long time = System.nanoTime();

            list.containsAll(toCheck);

            return System.nanoTime() - time;
        };

        benchRunner("ContainsRandom", 20, insertBR);
    }
}

@FunctionalInterface
interface BenchRunner {
    long bench(AbstractSet<Integer> list, int rounds) throws Exception;
}