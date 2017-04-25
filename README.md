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

{{Node/Layer -- Julian}}

## Performance Comparison

## Appendix A: Educational Material

### Skip-List Data Structure

A skip-list is a sorted linked list with several layers that enable searches to
skip forward various distances in the list, as shown below:

![skip-list diagram](skip_list_diagram.png)

At the lowest layer (BL), a skip-list looks very similar to a sorted linked list.
As you progress to higher layers, fewer of the elements in the list are included
in each layer. If an element is present in a given layer, it is also present in
all layers below that given layer, forming a column of entries.

Searches in the skip-list begin at the highest layer and progress down the
hierarchy. The search progresses through each layer until the algorithm either
finds target, or a value that is larger than the target. If the search finds
that the target is not present in a given layer, the search travels down a layer
in the column of the largest value that is not greater than the target. The
search continues in the new layer and repeats the process until either the target
is found or element is not found in the lowest layer. This type of search and
structure results in O(_log(n)_) searches.

As an example, let us search for the element 30 in the above list. First we look
at the highest level L3. There are no nodes in this layer yet, so we move to L2.
The first node is 7, which is less than 30, so we continue searching through L2.
We do this until we reach 53, which is the first element in L2 that is greater
than 30. We then move down into L1 in the column that came before 53, which is 25.
We then go from 25 to 42 in L1 and see that 42 is greater than 30. So we move down
to BL in the 25 column. We search BL until we reach 30, and return that we found
the target. If we were instead searching for 27, the process would be the same
until the search in the BL layer. After moving to the BL layer, we move to the
next node which is 30 and notice that 30 is greater than 27. Since we are in the
lowest layer, which includes all elements in the skip-list, we can conclude that
27 is not in the skip-list.

Adds and removes are extensions of searches (so they are also O(_log(n)_)
operations). For the add method, a search is conducted for the value to be added.
If the target is found, the add returns without modifying the skip list. If it is
not found, a node with the target value is added immediately before the first node
on the lowest layer that is larger than the target. The algorithm then randomly
decides whether to include the new node in the next highest layer or not. This
promotion continues until either it is chosen to not be promoted to the next layer
or it reaches the highest layer. This will ideally result in a Gaussian
distribution with only a few nodes in the highest layers.

To remove a node, a search is conducted for the value to be added. If the target
is not found, then remove returns without modifying the skip list. If it is found,
the node is marked as removed, but is not actually deleted from the structure.
A node that is marked as removed is no longer considered as included in the list
for further operations. The reference to the deleted node in the preceding node is
then replaced with the reference to the node that followed the deleted node.
