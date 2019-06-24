package com.marand.thinkmed.meds.result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
public class CmpResult<B, W>
{
  // changed
  private final List<CmpPair<B, W>> removed = new ArrayList<>();
  private final List<CmpPair<B, W>> added = new ArrayList<>();
  private final List<CmpPair<B, W>> updated = new ArrayList<>();

  // unchanged
  private final List<CmpPair<B, W>> unchanged = new ArrayList<>();

  // counts
  private final int changesCount;
  private final int differentCount;

  @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
  public CmpResult(
      final List<CmpPair<B, W>> removed,
      final List<CmpPair<B, W>> added,
      final List<CmpPair<B, W>> updated,
      final List<CmpPair<B, W>> unchanged)
  {
    this.removed.addAll(removed);
    this.added.addAll(added);
    this.updated.addAll(updated);
    this.unchanged.addAll(unchanged);

    changesCount = removed.size() + added.size() + updated.size();
    differentCount = removed.size() + added.size();
  }

  public List<CmpPair<B, W>> getAll()
  {
    return stream().collect(Collectors.toList());
  }

  public List<CmpPair<B, W>> getRemoved()
  {
    return removed;
  }

  public List<CmpPair<B, W>> getAdded()
  {
    return added;
  }

  public List<CmpPair<B, W>> getUpdated()
  {
    return updated;
  }

  public List<CmpPair<B, W>> getUnchanged()
  {
    return unchanged;
  }

  public List<CmpPair<B, W>> getChanged()
  {
    return streamChanged().collect(Collectors.toList());
  }

  public List<CmpPair<B, W>> getDifferent()
  {
    return streamDifferent().collect(Collectors.toList());
  }


  public List<W> getAddedItems()
  {
    return added.stream().map(CmpPair::getWorking).collect(Collectors.toList());
  }

  public List<B> getRemovedItems()
  {
    return removed.stream().map(CmpPair::getBase).collect(Collectors.toList());
  }

  public int getChangesCount()
  {
    return changesCount;
  }

  public boolean hasChanges()
  {
    return changesCount > 0;
  }

  public int getDifferentCount()
  {
    return differentCount;
  }

  public boolean hasDifferences()
  {
    return differentCount > 0;
  }


  public Stream<CmpPair<B, W>> stream()
  {
    return Stream.of(removed, added, updated, unchanged).flatMap(Collection::stream);
  }

  public Stream<CmpPair<B, W>> streamChanged()
  {
    return Stream.of(removed, added, updated).flatMap(Collection::stream);
  }

  public Stream<CmpPair<B, W>> streamUnchanged()
  {
    return Stream.of(unchanged).flatMap(Collection::stream);
  }

  public Stream<CmpPair<B, W>> streamDifferent()
  {
    return Stream.of(removed, added).flatMap(Collection::stream);
  }

  @Override
  public String toString()
  {
    return String.format("CmpResult{removed=%s, added=%s, updated=%s, unchanged=%s}", removed, added, updated, unchanged);
  }
}

