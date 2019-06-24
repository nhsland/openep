package com.marand.thinkmed.meds;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Nejc Korasa
 */

public final class CollectionCmpPartitioner
{
  private CollectionCmpPartitioner() { }

  public static <O> boolean canPartition(final Collection<O> collection, final Function<O, Serializable> keyExtractor)
  {
    final Map<Serializable, O> partition = buildPartition(collection, keyExtractor);
    return partition.keySet().size() == collection.size();
  }

  public static <O> Map<Serializable, O> buildPartition(final Collection<O> collection, final Function<O, Serializable> keyExtractor)
  {
    final Map<Serializable, O> partition = new HashMap<>();
    collection.forEach(item -> partition.computeIfAbsent(keyExtractor.apply(item), k -> item));
    return partition;
  }
}
