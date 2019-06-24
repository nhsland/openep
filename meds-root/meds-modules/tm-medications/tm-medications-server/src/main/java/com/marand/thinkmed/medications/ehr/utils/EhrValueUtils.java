package com.marand.thinkmed.medications.ehr.utils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkehr.util.ConversionUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.openehr.jaxb.rm.DvBoolean;
import org.openehr.jaxb.rm.DvCount;
import org.openehr.jaxb.rm.DvDate;
import org.openehr.jaxb.rm.DvParsable;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.DvTime;

/**
 * @author Mitja Lapajne
 */
public class EhrValueUtils
{

  private EhrValueUtils()
  {
  }

  public static DvCount getCount(final Long magnitude)
  {
    if (magnitude == null)
    {
      return null;
    }
    final DvCount dvCount = new DvCount();
    dvCount.setMagnitude(magnitude);
    return dvCount;
  }

  public static Long getCountValue(final DvCount dvCount)
  {
    return dvCount != null ? dvCount.getMagnitude() : null;
  }


  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  public static boolean getBooleanValue(final DvBoolean dvBoolean)
  {
    return dvBoolean != null && dvBoolean.isValue();
  }

  public static DvDate getDate(final DateTime dateTime)
  {
    if (dateTime == null)
    {
      return null;
    }
    final DvDate dvDate = new DvDate();
    dvDate.setValue(ISODateTimeFormat.date().print(dateTime));
    return dvDate;
  }

  public static DateTime getDate(final DvDate date)
  {
    if (date == null)
    {
      return null;
    }
    return new DateTime(ConversionUtils.toLocalDate(date).toDate());
  }

  public static DvTime getTime(final LocalTime time)
  {
    if (time == null)
    {
      return null;
    }
    final DvTime dvTime = new DvTime();
    dvTime.setValue(time.format(DateTimeFormatter.ISO_LOCAL_TIME));
    return dvTime;
  }

  public static HourMinuteDto getTime(final DvTime time)
  {
    if (time == null)
    {
      return null;
    }
    final org.joda.time.LocalTime localTime = ConversionUtils.toLocalTime(time);
    return new HourMinuteDto(localTime.getHourOfDay(), localTime.getMinuteOfHour());
  }

  public static String getText(final DvText dvText)
  {
    return dvText != null ? dvText.getValue() : null;
  }

  public static DvText getText(final String text)
  {
    return text != null && !text.trim().isEmpty() ? DataValueUtils.getText(text) : null;
  }

  public static DvParsable getParsableHtml(final String text)
  {
    if (text == null)
    {
      return null;
    }
    return ConversionUtils.getDvParsable(text, "text/html");
  }

  public static String getParsableValue(final DvParsable parsable)
  {
    if (parsable == null)
    {
      return null;
    }
    return parsable.getValue();
  }
}
