package com.marand.thinkmed.meds;

import java.util.function.Function;

/**
 * @author Nejc Korasa
 */

public class EqualityPair<B, W>
{
  private final Function<B, ?> baseEquality;
  private final Function<W, ?> workingEquality;

  private EqualityPair(final Function<B, ?> baseEquality, final Function<W, ?> workingEquality)
  {
    this.baseEquality = baseEquality;
    this.workingEquality = workingEquality;
  }

  public static <B, W> EqualityPair<B, W> of(final Function<B, ?> baseEquality, final Function<W, ?> workingEquality)
  {
    return new EqualityPair<>(baseEquality, workingEquality);
  }

  public Function<B, ?> getBaseEquality()
  {
    return baseEquality;
  }

  public Function<W, ?> getWorkingEquality()
  {
    return workingEquality;
  }
}
