package com.marand.thinkmed.medications.dto.report;

import java.util.Date;
import java.util.Locale;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.assertj.core.api.Assertions.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyDayReportUtilsTest
{
  private com.marand.thinkmed.api.core.Dictionary dictionary;

  @Before
  public void setup()
  {
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));

    dictionary = Preconditions.checkNotNull(Dictionary.getDelegate());
  }

  @Test
  public void getXActiveInsideIntervalTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setEnd(new DateTime(2019, 1, 7, 1, 0));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 2, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("");
  }

  @Test
  public void getXActiveBeforeStartTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapyDto = new ConstantSimpleTherapyDto();
    constantSimpleTherapyDto.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapyDto.setEnd(new DateTime(2019, 1, 7, 1, 0));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(118, 1, 2, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapyDto,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXActiveAfterEndTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapyDto = new ConstantSimpleTherapyDto();
    constantSimpleTherapyDto.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapyDto.setEnd(new DateTime(2019, 1, 7, 1, 0));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 1, 2, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapyDto,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXColumnAfterEndTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setEnd(new DateTime(2019, 1, 7, 1, 0));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 6, 5, 0);

    final int column = 2;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXHourBeforeStartTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setEnd(new DateTime(2019, 1, 7, 1, 0));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(1, 0), "01:00");
    final Date firstColumnDate = new Date(119, 0, 6, 1, 0);

    final int column = 2;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXHourAfterEndTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setEnd(new DateTime(2019, 1, 7, 1, 0));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(3, 0), "03:00");
    final Date firstColumnDate = new Date(119, 0, 6, 3, 0);

    final int column = 1;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXSuspendedTherapyInsideIntervalTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setEnd(new DateTime(2019, 1, 7, 1, 0));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.SUSPENDED;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 2, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXFinishedTherapyInsideIntervalTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setEnd(new DateTime(2019, 1, 7, 1, 0));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.FINISHED;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 2, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXInfiniteTherapyEveryXDaysInsideIntervalTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setDosingDaysFrequency(5);
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 11, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("");
  }

  @Test
  public void getXInfiniteTherapyEveryXDaysOutsideIntervalTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setDosingDaysFrequency(5);
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 12, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXInfiniteTherapyDaysOfWeekInsideDaysTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setDaysOfWeek(Lists.newArrayList("MONDAY", "FRIDAY"));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 14, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("");
  }

  @Test
  public void getXInfiniteTherapyDaysOfWeekInsideDaysColumnIndexTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setDaysOfWeek(Lists.newArrayList("MONDAY", "FRIDAY"));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 12, 5, 0);

    final int column = 2;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("");
  }

  @Test
  public void getXInfiniteTherapyDaysOfWeekOutsideDaysTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setDaysOfWeek(Lists.newArrayList("MONDAY", "FRIDAY"));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 12, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXInfiniteTherapyDaysOfWeekOutsideDaysColumnIndexTest()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setDaysOfWeek(Lists.newArrayList("MONDAY", "FRIDAY"));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime =
        new TherapyReportHourDoseTimeDto(new HourMinuteDto(5, 0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 14, 5, 0);

    final int column = 1;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXRowTimeIsNull()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime = new TherapyReportHourDoseTimeDto(null, "");
    final Date firstColumnDate = new Date(119, 0, 14, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("");
  }

  @Test
  public void getXStatDoseToday()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setEnd(new DateTime(2019, 1, 1, 2, 0));
    final DosingFrequencyDto dosingFrequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX);
    constantSimpleTherapy.setDosingFrequency(dosingFrequency);
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime = new TherapyReportHourDoseTimeDto(new HourMinuteDto(5,0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 1, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("");
  }

  @Test
  public void getXStatDoseTomorrow()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 1, 2, 0));
    constantSimpleTherapy.setEnd(new DateTime(2019, 1, 1, 2, 0));
    final DosingFrequencyDto dosingFrequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX);
    constantSimpleTherapy.setDosingFrequency(dosingFrequency);
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime = new TherapyReportHourDoseTimeDto(new HourMinuteDto(5,0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 1, 5, 0);

    final int column = 1;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void getXStatDoseBefore()
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = new ConstantSimpleTherapyDto();
    constantSimpleTherapy.setStart(new DateTime(2019, 1, 2, 2, 0));
    constantSimpleTherapy.setEnd(new DateTime(2019, 1, 2, 2, 0));
    final DosingFrequencyDto dosingFrequency = new DosingFrequencyDto(DosingFrequencyTypeEnum.ONCE_THEN_EX);
    constantSimpleTherapy.setDosingFrequency(dosingFrequency);
    final TherapyReportStatusEnum therapyReportStatusEnum = TherapyReportStatusEnum.ACTIVE;
    final TherapyReportHourDoseTimeDto rowTime = new TherapyReportHourDoseTimeDto(new HourMinuteDto(5,0), "05:00");
    final Date firstColumnDate = new Date(119, 0, 1, 5, 0);

    final int column = 0;

    final String result = TherapyDayReportUtils.getX(
        therapyReportStatusEnum,
        constantSimpleTherapy,
        rowTime,
        firstColumnDate,
        column);

    assertThat(result).isEqualTo("X");
  }

  @Test
  public void generateFrequencyDisplayDaysOfWeekDisplayTest()
  {
    final TherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setFrequencyDisplay("2x per day");
    therapy.setDaysOfWeekDisplay("Mon, Wed, Sat");

    final String result = TherapyDayReportUtils.generateFrequencyDisplay(therapy);

    assertThat(result).isEqualTo("2x per day - Mon, Wed, Sat");
  }

  @Test
  public void generateFrequencyDisplayDaysFrequencyDisplay()
  {
    final TherapyDto therapy = new ConstantSimpleTherapyDto();
    therapy.setFrequencyDisplay("2x per day");
    therapy.setDaysFrequencyDisplay("Weekly");

    final String result = TherapyDayReportUtils.generateFrequencyDisplay(therapy);

    assertThat(result).isEqualTo("2x per day - Weekly");
  }
}
