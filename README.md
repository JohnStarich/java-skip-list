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

{{Atomic -- John}}

#### Fine-grained

To ensure the layers of the skip-list are sublists of lower layer lists,
modifications to the skip-list should only occur once all locks are obtained for
nodes needing modification. As a result of the internally layered structure of a
skip-list, locks are retrieved for all predecessor nodes up to and including the
highest layer of occurrence of a node to add or remove. The predecessor nodes
point to either the correct location for a new node (add() operations), or to
the node to remove (remove() operations). Once a thread finishes their
modification, the thread unlocks all locks belonging to it, allowing other
threads to acquire those locks or locks passed them.

This implementation guarantees deadlock freedom; when a thread locks a node with
a search key _k_, it will never acquire a lock on a node with a search key >=
_k_. From an implementation perspective, this means locks are acquired from the
lowest layer upwards. Furthermore, concurrent modifications are guaranteed as
long as there aren't overlapping search key values.

On the other hand, the implementation is blocking; it prevents other threads
from completing operations on the skip list which don't have the locks. This
results in a significant time/memory overhead: a thread must retry its operation
until it can successfully acquire the lock(s) it needs, and every node must have
their own instance of a ReentrantLock.

## Performance Comparison
