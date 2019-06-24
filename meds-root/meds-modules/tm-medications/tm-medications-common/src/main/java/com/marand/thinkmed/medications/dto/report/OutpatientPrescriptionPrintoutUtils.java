package com.marand.thinkmed.medications.dto.report;

import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.thinkmed.api.demographics.data.Gender;
import lombok.NonNull;
import org.joda.time.DateTime;

/**
 * @author Vid Kumse
 */
public class OutpatientPrescriptionPrintoutUtils
{
  private OutpatientPrescriptionPrintoutUtils()
  {
  }

  public static String getPatientNameSurnameLabel(final @NonNull Locale locale)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.patient.name.surname", locale) + ": ";
  }

  public static String getPatientDateOfBirthLabel(final @NonNull Locale locale)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.patient.birth.date", locale) + ": ";
  }

  public static String getPatientGender(final @NonNull Locale locale, final @NonNull Gender gender)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.patient.gender", locale) + ": "
        +Dictionary.getEntry(gender, locale);
  }

  public static String getNumberOfPrescriptionGroupLabel(final @NonNull Locale locale)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.prescription.group.number", locale) + ": ";
  }

  public static String getPrescriberLabel(final @NonNull Locale locale)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.prescripber", locale) + ": ";
  }

  public static String getWardLabel(final @NonNull Locale locale)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.ward", locale) + ": ";
  }

  public static String getPrescriptionDateLabel(final @NonNull Locale locale)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.Date.of.prescription", locale) + ": ";
  }

  public static String getHeader(final @NonNull Locale locale)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.header", locale) + ": ";
  }

  private static String getPrescriptionNumberLabel(final @NonNull Locale locale)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.prescription.number", locale) + ": ";
  }

  public static String getSizeOfList(final @NonNull Locale locale, final String prescriptionId)
  {
    return prescriptionId;
  }

  public static String getMedicationNameLabel(final @NonNull Locale locale)
  {
    return Dictionary.getEntry("report.outpatientPrescriptionPrintout.medication.name", locale) + ": ";
  }

  public static String getFormattedDate(final @NonNull Locale locale, final @NonNull DateTime date)
  {
    return date.getDayOfMonth()+"."+date.getMonthOfYear()+"."+date.getYear();
  }

  public static String getFormattedName(final @NonNull Locale locale, final @NonNull String name)
  {
    return "<b>"+name+"</b>";
  }

  private static String exceptionToString(final Exception e)
  {
    return "<span style='color:red; font-size:10px;'>" + e.toString() + "</span>";
  }
}
