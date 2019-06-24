package com.marand.thinkmed.medications.dao.hibernate;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Years;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)

public class ConvertDaysToMonthsAndYearsTest
{
  @Test
  public void testDaysToMonths()
  {
    final DateTime now = new DateTime(2018, 1, 1, 0, 0);
    assertEquals(0, Months.monthsBetween(now.minusDays(1), now).getMonths());
    assertEquals(0, Months.monthsBetween(now.minusDays(15), now).getMonths());
    assertEquals(0, Months.monthsBetween(now.minusDays(29), now).getMonths());
    assertEquals(0, Months.monthsBetween(now.minusDays(30), now).getMonths());
    assertEquals(1, Months.monthsBetween(now.minusDays(31), now).getMonths());
    assertEquals(1, Months.monthsBetween(now.minusDays(45), now).getMonths());
    assertEquals(12, Months.monthsBetween(now.minusDays(365), now).getMonths());
    assertEquals(985, Months.monthsBetween(now.minusDays(30000), now).getMonths());
  }

  @Test
  public void testDaysToYears()
  {
    final DateTime now = new DateTime(2018, 1, 1, 0, 0);
    assertEquals(0, Years.yearsBetween(now.minusDays(1), now).getYears());
    assertEquals(0, Years.yearsBetween(now.minusDays(29), now).getYears());
    assertEquals(0, Years.yearsBetween(now.minusDays(31), now).getYears());
    assertEquals(1, Years.yearsBetween(now.minusDays(365), now).getYears());
    assertEquals(1, Years.yearsBetween(now.minusDays(720), now).getYears());
    assertEquals(2, Years.yearsBetween(now.minusDays(740), now).getYears());
    assertEquals(2, Years.yearsBetween(now.minusDays(1000), now).getYears());
    assertEquals(82, Years.yearsBetween(now.minusDays(30000), now).getYears());
  }
}
