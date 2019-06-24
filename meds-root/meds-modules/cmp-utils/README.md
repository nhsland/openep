
### Collections compare

#### Basics

It provides comparing and finding differences between two collection - _base_ and _working_ collection. Items in collections can be of same or different type, both are supported. Two steps are important to understand how comparison is made and differences are found:

1. Matching

   First, items from both collections are matched together by their keys. Each item is assigned it's own **key**. Keys are computed using provided **keyExtractor** functions. When 2 items match they form a **Pair**.

2. Comparing

   Each pair is compared using provided **equalsFunction**, **equalities** or **equalityPairs**.

#### Matching

Matching is computed using **keyExtractor** functions. For example:

```java
CollectionCmp
        .ofSame(baseList, workingList)
        .compare(item -> item.getId()); // keyExtractor function
```
or when comparing collections of different item types:

```java
CollectionCmp
        .ofDifferent(baseList, workingList)
        .compare(baseItem -> baseItem.getId(), workingItem -> workingItem.getId()); // keyExtractor functions for base and working items
```

> **keyExtractors** are not optional and must always be provided.

#### Comparing

Comparing is performed on items that are matched together (they form a Pair). This is done by **equalsFunction** that can be defined in a few different ways:

_equalsFunction_

```java
CollectionCmp
        .ofSame(baseList, workingList)
        .withEquals((item1, item2) -> item1.getName().equals(item2.getName())) // equalsFunction
        .compare(item -> item.getId()); // keyExtractor
```

_equalities_ or _equalityPairs_

```java
CollectionCmp
        .ofSame(baseList, workingList)
        .withEqualities(
          item -> item.getName(), 
          item -> item.getCode(), 
          item -> item.getDescription()) // equalities
        .compare(item -> item.getId()); // keyExtractor
```

In example above, items are considered equal when **name**, **code** and **description** fields are equal. Similarly with collections of different types, **equalityPairs** are used:

```java
CollectionCmp
        .ofDifferent(baseList, workingList)
        .withEqualityPairs(
            EqualityPair.of(baseItem -> baseItem.getName(), workingItem -> workingItem.getData().getName()),
            EqualityPair.of(baseItem -> baseItem.getCode(), workingItem -> workingItem.getData().getCode())) // equalityPairs
        .compare(item -> item.getId()); // keyExtractor
```
Now, items are considered equal when **name**, **code** properties are equal. Because base and working items are not of same type,properties may exist on different paths.

> **equalsFunction** is optional, if nothing is provided, `Objects.equals()` is used to compare matched items.

#### Result

Compare result of collections is presented with clear separation of **added**, **removed**, **updated** and **unchanged** items. Result object has a few useful functions to help you analyze result data:

```java
CmpResult<O, O> compareResult = CollectionCmp
        .ofSame(baseList, workingList)
        .withEquality(item -> item.getName())
        .compare(item -> item.getId()); // keyExtractor

boolean hasChanges = compareResult.hasChanges();
int changesCount = compareResult.getChangesCount();

// different items are added and removed items ...
boolean hasDifferences = compareResult.hasDifferences();
int differentCount = compareResult.getDifferentCount();

compareResult.getAll();
compareResult.getAdded();
compareResult.getUdpated();
...

// changed items are added and removed or updated items ...
compareResult.getChanged();
compareResult.getUncanged();

// stream through changed, unchanged, added, different items ...
compareResult.streamChanged()
        .map( ... )
        ...
```

All result data is provided in Pairs, containing matched base and working item as well as difference type:

```java
CmpPair<B, W> pair = ...

B base = pair.getBase();
W working = pair.getWorking();

Diff diff = pair.getDiff(); // UNCHANGED, UPDATED, ADDED, REMOVED
Serializable key = pair.getKey(); // key by which items are matched together
```

#### Partitioning 

Matching must be a **injective** function (in both ways) == there must be at most one item with the same key in each collection. If that condition is not met, collection cannot be partitioned and collections compare result might not be correct.

You can check if collection can be partitioned using:

```java
boolean canPartition = CollectionCmpPartitioner.canPartition(collection, keyExtractor)
```
