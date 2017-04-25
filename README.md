# Lock-free Skip List

\doublespacing

\abstract{
  In concurrent applications with big data, the ability to modify large lists
  concurrently becomes critical. Using traditional, globally-locked,
  concurrent data structures we can achieve concurrency but, at the cost of
  modification speed as the entire structure must be locked during a
  modification. In this paper we explore creating a Fine-gained and lock-free
  Skip Lists and compare their performance.
}

<!-- \newpage -->

## Introduction

A Skip List is a data structure designed to allow for fast searching like a
B-Tree, but also allow for fine-gained concurrency like a Linked List allows.
We implemented a Lock-free and fine-gained Skip-Lists, showing that we can get
comparable performance between our Implementations. Lock-free means that we use
atomic actions instead of locks (or semaphores), we expect that this will give
us a performance improvement as we will not have to perform lock arbitration.
Fine-gained means that a small subset of the list will block other
modifications, instead of the entire list blocking.

| Operation        | Linked List      | Binary Tree      | Skip List           |
|------------------|------------------|------------------|---------------------|
| Access           | $\Theta(n)$      | $\Theta(log(n))$ | $\Theta(log(n))$    |
| Search           | $\Theta(n)$      | $\Theta(log(n))$ | $\Theta(log(n))$    |
| Insert           | $\Theta(1)$      | $\Theta(log(n))$ | $\Theta(log(n))$    |
| Remove           | $\Theta(1)$      | $\Theta(log(n))$ | $\Theta(log(n))$    |
| Space Complexity | $\Theta(n)$      | $\Theta(n)$      | $\Theta(n\:log(n))$ |
Table: Bit-O of Linked List, Binary Tree, and Skip List

In Table 1 we compare the various speeds of Linked Lists, Binary Trees, and Skip
Lists. Linked Lists are slow to access and search as we have to traverse each
node in the list to get to the next. However, it is very fast to insert and
remove a given node as we can get right to it, and swap the pointers around.
Binary Trees are all moderately fast, $\Theta(log(n))$, and a good in between in
performance of linked list and arrays. However, the entire tree must be blocked
off during an insert, delete, or modification. When we make a change to node
$n$, $n$ has a fairly good chance of moving to a different place in the list.
This move forces the rest of the list to rebalance, and would force another
process to start over from the new tree. A Skip List solves both of these issues
by making modifications to the list like a linked list, but at the same time
having a layered structure internally. From this, it is moderately fast and
allows for fine-gained locking.

## Implementation

A skip-list is a sorted linked list with several layers that enable searches to
skip forward various distances in the list, as shown below:

![skip-list diagram](skip_list_diagram.png)

We implemented the probabilistic skip-list where the insertion of an element has
some probability _p_ that it will be inserted in the current level vs the next
one. This allows for a probabalistically even distribution of links such that we
can obtain O(_log(n)_) insertion time.

## Design Alternatives

In this paper we explore one major design decision, whether to make a lock-free
or fine-gained skip list, however other decisions must also be considered. For the
lock-free version, we made the decisions to use Atomic References as opposed to
Atomic updates to the nodes. For the fine-gained version we considered creating
locks on the node, or the node/layer pair.

In the lock free skip list we debated on wether we should atomically update a
boolean that says if the node is valid, or if we should use markable references
to forward nodes. If we were to invalidate the entire node, we could quickly
stop traversal through the list, as we don't need to iterate through the forward
node array to determine if we should stop iterating.  On the other hand if we
mark the references, then we can stop before accessing a node that is being updated.
Additionally, if we mark the reference, then we can traverse layers that above the modification,
removing an effective lock from a large portion of the list. We believe that
using atomic, markable references is the better choice.

To ensure the layers of the fine-grained skip-list are sublists of lower layer lists,
modifications to the skip-list should only occur once all locks are obtained for
nodes needing modification. As a result of the internally layered structure of a
skip-list, locks are retrieved for all predecessor nodes up to and including the
highest layer of occurrence of a node to add or remove. The predecessor nodes
point to either the correct location for a new node (add() operations), or to
the node to remove (remove() operations). Once a thread finishes their
modification, the thread unlocks all locks belonging to it, allowing other
threads to acquire those locks or locks passed them.
This implementation guarantees deadlock freedom; when a thread locks a node with
a search key $k$ it will never acquire a lock on a node with a search key $>=k$
. From an implementation perspective, this means locks are acquired from the
lowest layer upwards. Furthermore, concurrent modifications are guaranteed as
long as there aren't overlapping search key values.
On the other hand, the implementation is blocking; it prevents other threads
from completing operations on the skip list which don't have the locks. This
results in a significant time/memory overhead: a thread must retry its operation
until it can successfully acquire the lock(s) it needs, and every node must have
their own instance of a ReentrantLock.

## Performance Comparison

In the figures below, we show the performance of our two implementations as
compared to the Java's built-in $java.util.concurrent.ConcurrentSkipListSet$.
As we can see, we achieve similar performance to Java, with some minor differences.
For example, we can see that our Fine-gained solution is slower on average than
a lock-free implementation like java's.

\begin{figure}[H]
  \centering
  \begin{tikzpicture}
  \begin{axis}[
      title={Remove all elements in Random order},
      xlabel={Operations ($log_2(n)$)},
      ylabel={Completion Time ($long_2(ns)$)},
      xmin=0, xmax=20,
      ymin=0, ymax=30,
      xtick={1,5,10,15,20},
      ytick={0,10,20,30},
      legend pos=south east,
      ymajorgrids=true,
      grid style=dashed,
  ]

  \addplot[ color=blue, mark=none, ]
      coordinates {
        (0, 9.96) 	(1, 8.41) 	(2, 9.19) 	(3, 9.98) 	(4, 10.85) 	(5, 12.12) 	(6, 13.17) 	(7, 14.52) 	(8, 15.46) 	(9, 16.57) 	(10, 17.78) 	(11, 18.96) 	(12, 20.05) 	(13, 21.31) 	(14, 22.59) 	(15, 23.87) 	(16, 25.15) 	(17, 26.48) 	(18, 27.78) 	(19, 28.98)
      };

  \addplot[ color=red, mark=none, ]
    coordinates {
      (0, 10.95) 	(1, 8.99) 	(2, 9.95) 	(3, 10.39) 	(4, 11.33) 	(5, 12.35) 	(6, 13.42) 	(7, 14.53) 	(8, 15.55) 	(9, 16.7) 	(10, 17.8) 	(11, 18.99) 	(12, 20.24) 	(13, 21.41) 	(14, 22.75) 	(15, 24.23) 	(16, 25.6) 	(17, 27.3) 	(18, 29.03) 	(19, 31.08)
    };

  \addplot[ color=green, mark=none, ]
    coordinates {
      (0, 1) (1, 1) (2, 1) (3, 1) (4, 1) (5, 1) (6, 1) (7, 1) (8, 1) (9, 1) (10, 1) 	(11, 1) (12, 1) (13, 1) (14, 1) (15, 1) (16, 1) (17, 1) (18, 1) (19, 1)
    };

  \legend{Java, Fine-gained, Lock-free}
  \end{axis}
  \end{tikzpicture}

  \label{fig:removeRandom}
  \caption{Remove Random}
\end{figure}

### Lock-free

{{Performace Argument}}

### Fine-gained

The fine-gained implementation performs worse the the built in implementation,
a lock-free implementation. We see that on average our implementation is slower,
and sees seems to have a low $\Theta(n)$ coefficient. Our argument is that all
operations in a skip list are $\Theta(log_2(n))$.

\begin{figure}[H]
  \centering
  \begin{tikzpicture}
  \begin{axis}[
      title={Remove all elements in Random order 8 Threads},
      xlabel={Operations ($log_2(n)$)},
      ylabel={Completion Time ($long_2(ns)$)},
      xmin=0, xmax=20,
      ymin=0, ymax=30,
      xtick={1,5,10,15,20},
      ytick={0,10,20,30},
      legend pos=south east,
      ymajorgrids=true,
      grid style=dashed,
  ]

  \addplot[ color=blue, mark=none, ]
      coordinates {
        ((0, 9.12) 	(1, 8.77) 	(2, 8.89) 	(3, 11.45) 	(4, 10.95) 	(5, 11.87) 	(6, 13.56) 	(7, 14.72) 	(8, 15.74) 	(9, 17.05) 	(10, 18.33) 	(11, 19.71) 	(12, 21.01) 	(13, 21.91) 	(14, 22.96) 	(15, 24.31) 	(16, 24.82) 	(17, 25.86) 	(18, 27.22) 	(19, 28.49)
      };

  \addplot[ color=red, mark=none, ]
    coordinates {
      (0, 9.17) 	(1, 9.18) 	(2, 9.16) 	(3, 11.75) 	(4, 12.84) 	(5, 14.16) 	(6, 14.66) 	(7, 16.5) 	(8, 17.63) 	(9, 18.85) 	(10, 19.16) 	(11, 19.93) 	(12, 20.81) 	(13, 21.77) 	(14, 22.89) 	(15, 23.89) 	(16, 25.09) 	(17, 26.37) 	(18, 27.47) 	(19, 28.73)

    };

  \legend{Java, Fine-gained}
  \end{axis}
  \end{tikzpicture}

  \label{fig:removeRandom}
  \caption{Remove Random 8 Threads}
\end{figure}

We examined the performance of the skip-list by varying the number of rows
modified and the number of thread in contention. From the data we gathered we
could see that the performance of the data structure followed closely with that
of Java's implementation. We ran our tests ten times, ignoring the first few runs
to allow from the JVM to compile and optimize the code. Then, increased the number
of threads running the test. Each thread performs insert and remove tasks that
duplicate or preempt each other. This allows us to examine the performance gain
in the locking mechanism alone.

# Conclusion
