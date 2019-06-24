package com.marand.thinkmed.medications.dto;

import com.marand.maf.core.data.object.HourMinuteDto;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mitja Lapajne
 */
public class HourMinuteDtoTest
{
  @Test
  public void testCombine()
  {
    final DateTime combined = new HourMinuteDto(2, 30).combine(new DateTime(2017, 3, 25, 0, 0));
    Assert.assertTrue(combined.isEqual(new DateTime(2017, 3, 25, 2, 30)));
  }

  @Test
  public void testCombineStartOfDstGap()
  {
    final DateTime combined = new HourMinuteDto(2, 30).combine(new DateTime(2017, 3, 26, 0, 0));
    Assert.assertTrue(combined.isEqual(new DateTime(2017, 3, 26, 3, 0)));
  }

  @Test
  public void testCombineInDstGap()
  {
    final DateTime combined = new HourMinuteDto(2, 30).combine(new DateTime(2017, 3, 26, 0, 0));
    Assert.assertTrue(combined.isEqual(new DateTime(2017, 3, 26, 3, 0)));
  }

  @Test
  public void testCombineEndOfDstGap()
  {
    final DateTime combined = new HourMinuteDto(2, 30).combine(new DateTime(2017, 3, 26, 0, 0));
    Assert.assertTrue(combined.isEqual(new DateTime(2017, 3, 26, 3, 0)));
  }
}
