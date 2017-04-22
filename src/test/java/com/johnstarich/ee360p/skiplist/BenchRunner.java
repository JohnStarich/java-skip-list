package com.johnstarich.ee360p.skiplist;

import java.util.AbstractSet;

@FunctionalInterface
interface BenchRunner {
    long bench(AbstractSet<Integer> list, int rounds) throws Exception;
}
