package com.marand.thinkmed.medications.dto.report;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.marand.maf.core.formatter.NumberFormatters;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.thinkmed.api.core.Dictionary;
import com.marand.thinkmed.api.core.GrammaticalGender;
import com.marand.thinkmed.api.core.time.CurrentTimeProvider;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.FlowRateMode;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.connector.data.object.AllergiesStatus;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.dto.SimpleMedicationOrderDoseDto;
import com.marand.thinkmed.medications.dto.administration.AdjustInfusionAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.InfusionSetChangeDto;
import com.marand.thinkmed.medications.dto.administration.StartAdministrationDto;
import com.marand.thinkmed.medications.dto.administration.StopAdministrationDto;
import com.marand.thinkmed.medications.dto.utils.TherapyTimingUtils;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

/**
 * @author Primoz Prislan
 */
@SuppressWarnings("unused")
public class TherapyDayReportUtils
{
  private static final String SPACER = " ";
  private static final String VALUES_DELIMITER_BEFORE_AFTER = SPACER + "-" + SPACER;
  private static final String LABEL_COLOR = "#666666";
  private static Dictionary dictionary = null;

  private TherapyDayReportUtils() { }

  public static void init(final Dictionary dictionary)
  {
    if (TherapyDayReportUtils.dictionary != null)
    {
      throw new IllegalArgumentException(TherapyDayReportUtils.class.getSimpleName() + " already initialized!");
    }
    TherapyDayReportUtils.dictionary = Preconditions.checkNotNull(dictionary);
  }

  public static String getLegend(final int column, final @NonNull Locale locale)
  {
    switch (column)
    {
      case 1:
        return
            createLegend("Strt", "Start", false)
                + "<BR>" + createLegend("Stp", "Stop", false);
      case 2:
        return
            createLegend("G", "Given", false)
                + "<BR>" + createLegend("L", "Late", false);
      case 3:
        return
            createLegend("D", "Defer", false)
                + "<BR>" + createLegend("pna", "patient not avaliable", true)
                + "<BR>" + createLegend("mna", "medication not avaliable", true)
                + "<BR>" + createLegend("cr", "clinical reason", true);
      case 4:
        return
            createLegend("SA", "Self-Administer", false)
                + "<BR>" + createLegend("cn", "charted by nurse", true)
                + "<BR>" + createLegend("ac", "automatically charted", true);
      case 5:
        return
            createLegend("NG", "Not given", false)
                + "<BR>" + createLegend("pr", "patient refuse", true)
                + "<BR>" + createLegend("nm", "nill by mouth", true)
                + "<BR>" + createLegend("mu", "medicine unavaliable", true);
      case 6:
        return
            ""
                + "<BR>" + createLegend("pu", "patient unavaliable", true)
                + "<BR>" + createLegend("cr", "clinical reason", true)
                + "<BR>" + createLegend("mfr", "medicine free interval", true);
      default:
        return "";
    }
  }

  public static String getDateLabel(final @NonNull Locale locale)
  {
    return dictionary.getEntry("report.date", GrammaticalGender.UNDEFINED, locale);
  }

  private static String createLegend(final String key, final String value, final boolean child)
  {
    return (child ? "&nbsp;&nbsp;&nbsp;" : "") + "<B>" + key + "</B>&thinsp;&ndash;&thinsp;" + value;
  }

  public static String getPatientDataDisplay(
      final @NonNull PatientDataForTherapyReportDto patientData,
      final @NonNull Locale locale)
  {
    try
    {
      final Gender gender = patientData.getGender();
      return
          "<font size=\"3\">" +
              getValueHtml(
                  patientData.getPatientName(),
                  true,
                  false,
                  false,
                  false,
                  locale) +
              "</font>" +
              getValueHtml(
                  dictionary.getEntry("report.outpatientPrescriptionPrintout.patient.birth.date", gender.getGrammaticalGender(), locale),
                  patientData.getBirthDateAndAge(),
                  true,
                  false,
                  false,
                  true,
                  locale) +
              VALUES_DELIMITER_BEFORE_AFTER +
              getValueHtml(
                  dictionary.getEntry(gender.getShortEntryKey(), gender.getGrammaticalGender(), locale),
                  true,
                  false,
                  false,
                  false,
                  locale) +
              getValueHtml(
                  "address",
                  patientData.getAddress(),
                  true,
                  false,
                  false,
                  true,
                  locale)+
              getValueHtml(
                  patientData.getPatientIdentificatorType(),
                  patientData.getPatientIdentificator(),
                  true,
                  false,
                  false,
                  true,
                  locale) +
              getValueHtml(
                  "case",
                  patientData.getCentralCaseIdNumber(),
                  true,
                  false,
                  false,
                  true,
                  locale) +
              getValueHtml(
                  "weight",
                  patientData.getWeight(),
                  true,
                  false,
                  false,
                  true,
                  locale);
    }
    catch (Exception e)
    {
      return exceptionToString(e);
    }
  }

  public static String getPatientHospitalizationDataDisplayForSurgeryReport(
      final @NonNull PatientDataForTherapyReportDto patientData, final @NonNull Locale locale
  )
  {
    try
    {
      final StringBuilder strBuilder = new StringBuilder();

      strBuilder.append(getPatientHospitalizationDataDisplay(patientData, locale));

      strBuilder.append(patientData.getOrganization()!= null ? getValueHtml(
          "ward",
          patientData.getOrganization(),
          true,
          true,
          false,
          true,
          locale) : "");

      return strBuilder.toString();
    }
    catch (final RuntimeException e)
    {
      return exceptionToString(e);
    }
  }

  public static String getPatientHospitalizationDataDisplay(
      final @NonNull PatientDataForTherapyReportDto patientData, final @NonNull Locale locale)
  {
    try
    {
      final StringBuilder strBuilder = new StringBuilder();
      strBuilder.append(
          getValueHtml(
              patientData.isInpatient() ? "admission.date" : "encounter",
              patientData.getAdmissionDate(),
              true,
              true,
              false,
              false,
              locale));
      if (patientData.isInpatient())
      {
        final String hospitalizationConsecutive =
            patientData.getHospitalizationConsecutiveDay() != null ?
            String.valueOf(patientData.getHospitalizationConsecutiveDay()) : null;
        strBuilder.append(
            getValueHtml(
                "hospitalization.day",
                hospitalizationConsecutive,
                true,
                false,
                false,
                true,
                locale));
      }

      return strBuilder.toString();
    }
    catch (Exception e)
    {
      return exceptionToString(e);
    }
  }

  public static String getDose(final @NonNull SimpleTherapyDto order)
  {

    return getQuantityDisplay(order);
  }

  public static String getRate(final @NonNull Locale locale, final @NonNull ComplexTherapyDto order)
  {

    return getQuantityDisplay(order, locale);
  }

  public static String getBarcodeLabel(final @NonNull Locale locale)
  {
    return dictionary.getEntry("barcode", GrammaticalGender.UNDEFINED, locale);
  }

  public static String getDoseTimeLabel(
      final @NonNull Locale locale,
      final @NonNull List<TherapyDayElementReportDto> therapies)
  {
    if (!therapies.isEmpty())
    {
      if (therapies.get(0).getOrder() instanceof SimpleTherapyDto)
      {
        return dictionary.getEntry("dose.time", GrammaticalGender.UNDEFINED, locale);
      }
      return dictionary.getEntry("rate.time", GrammaticalGender.UNDEFINED, locale);
    }
    return dictionary.getEntry("time", GrammaticalGender.UNDEFINED, locale);
  }

  public static String getDoseTimeLabelForEmptyReport(
      final @NonNull Locale locale)
  {
    return dictionary.getEntry("dose.time", GrammaticalGender.UNDEFINED, locale);
  }

  public static ComplexTherapyDto castToComplexTherapyDto(final @NonNull TherapyDto order)
  {
    return order instanceof ComplexTherapyDto ? (ComplexTherapyDto)order : null;
  }

  public static String getSimpleCombinedDisplay(
      final @NonNull SimpleTherapyDto order,
      final String consecutiveDay,
      final boolean showConsecutiveDay,
      final String therapyStart,
      final String therapyEnd,
      final TherapyReportStatusEnum therapyReportStatus,
      final @NonNull Locale locale,
      final String consecutiveDayLabel,
      final String lastTitrationResult)
  {
    try
    {
      final StringBuilder strBuilder = new StringBuilder();

      return
          strBuilder
              .append(getMedicationDisplay(order.getMedication()))
              .append(buildReleaseDetailsDisplay(order.getReleaseDetails(), locale))
              .append(
                  getValueHtml(
                      "dose",
                      getQuantityDisplay(order),
                      true,
                      true,
                      false,
                      strBuilder.length() > 0,
                      locale))
              .append(
                  getValueHtml(
                      "dosing.interval",
                      generateFrequencyDisplay(order),
                      true,
                      false,
                      false,
                      strBuilder.length() > 0,
                      locale))
              .append(getAdditionalDoseDisplay(order, locale))
              .append(
                  getValueHtml(
                      "from",
                      getTherapyIntervalDisplay(therapyStart, therapyEnd, locale),
                      true,
                      true,
                      false,
                      strBuilder.length() > 0,
                      locale))
              .append(showConsecutiveDay ? getValueHtml(consecutiveDayLabel,
                                   consecutiveDay,
                                   true,
                                   false,
                                   false,
                                   strBuilder.length() > 0,
                                   locale) : "")
              .append(getTherapyStatusDisplay(therapyReportStatus, locale))
              .append(getValueHtml("commentary",
                  extractComment(order,locale),
                                   true,
                                   false,
                                   false,
                                   strBuilder.length() > 0,
                                   locale))
              .append(getValueHtml("indication",
                  extractIndication(order,locale),
                                   true,
                                   false,
                                   false,
                                   strBuilder.length() > 0,
                                   locale))
              .append(order.getTargetInr() != null ? getValueHtml(
                  "target.inr",
                  String.valueOf(order.getTargetInr()),
                  true,
                  false,
                  false,
                  strBuilder.length() > 0,
                  locale
              ) : "")
              .append(order.getMaxDailyFrequency() != null ? getValueHtml(
                  "dosing.max.24h",
                  String.valueOf(order.getMaxDailyFrequency()),
                  true,
                  false,
                  false,
                  strBuilder.length() > 0,
                  locale
              ) : "")
              .append(lastTitrationResult != null ? getValueHtml(
                  lastTitrationResult,
                  false,
                  false,
                  false,
                  strBuilder.length() > 0,
                  locale) : "")
              .append(order.getPrescriberName() != null ? getValueHtml(
                  "prescriber",
                  order.getPrescriberName(),
                  true,
                  false,
                  false,
                  strBuilder.length() > 0,
                  locale) : "")
              .toString();
    }
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  public static String buildReleaseDetailsDisplay(final ReleaseDetailsDto releaseDetails, final Locale locale)
  {
    if (releaseDetails != null)
    {
      if (releaseDetails.getType() == ReleaseType.MODIFIED_RELEASE)
      {
        final String mrDisplay = " - " + getDictionaryEntry("modified.release.short", locale);
        return releaseDetails.getHours() != null ? mrDisplay + releaseDetails.getHours() : mrDisplay;
      }
      if (releaseDetails.getType() == ReleaseType.GASTRO_RESISTANT)
      {
        return " - " + getDictionaryEntry("gastro.resistant.short", locale);
      }
    }

    return "";
  }

  public static List<SimpleMedicationOrderDoseDto> getSimpleMedicationDoseElements(
      final @NonNull SimpleTherapyDto order,
      final @NonNull Locale locale)
  {
    final List<SimpleMedicationOrderDoseDto> elements = new ArrayList<>();

    if (order instanceof ConstantSimpleTherapyDto)
    {
      elements.add(new SimpleMedicationOrderDoseDto(order.getQuantityDisplay()));
    }
    else
    {
      for (final TimedSimpleDoseElementDto element : ((VariableSimpleTherapyDto)order).getTimedDoseElements())
      {
        elements.add(new SimpleMedicationOrderDoseDto(element.getTimeDisplay() + SPACER + '-' + SPACER + element.getQuantityDisplay()));
      }
    }

    return elements;
  }

  public static String getApplicationGivenChecked(
      final int column,
      final @NonNull Locale locale)
  {
    if (column == 0)
    {
      return getDictionaryEntry("administration.given", locale);
    }

    if (column == 1)
    {
      return getDictionaryEntry("administration.checked", locale);
    }

    else
    {
      return exceptionToString(new UnsupportedOperationException());
    }
  }

  // column = 0 based
  public static String getTherapyApplicationColumnLabel(
      final @NonNull Date startDate,
      final int column)
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.DATE, column);

    final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    final int month = calendar.get(Calendar.MONTH)+1;
    final int year = calendar.get(Calendar.YEAR);

    return dayOfMonth + "." + month + "." + year;
  }

  // column = 0 based
  public static String getTherapyApplicationColumnValue(
      List<AdministrationDto> therapyAdministrations,
      final @NonNull TherapyDto order,
      final @NonNull Date startDate,
      final int column,
      @NonNull Locale locale)
  {
    final DateTime day = new DateTime(startDate).plusDays(column).withTimeAtStartOfDay();
    final StringBuilder administrationString = new StringBuilder();

    if (therapyAdministrations != null)
    {
      Collections.sort(therapyAdministrations, Comparator.comparing(AdministrationDto::getAdministrationTime));
      for (final AdministrationDto administration : therapyAdministrations)
      {
        final DateTime administrationTime = administration.getAdministrationTime();
        if (administrationTime != null && administrationTime.withTimeAtStartOfDay().isEqual(day))
        {
          final StringBuilder dose = new StringBuilder();

          if (administration.getAdministrationResult() == AdministrationResultEnum.DEFER)
          {
            dose.append(StringUtils.capitalize(getDictionaryEntry("administration.defer", locale)));

            if (administration.getNotAdministeredReason() != null
                && administration.getNotAdministeredReason().getName() != null)
            {
              dose.append(" - ").append(administration.getNotAdministeredReason().getName());
            }
          }
          else if (administration.getAdministrationResult() == AdministrationResultEnum.NOT_GIVEN)
          {
            dose.append(StringUtils.capitalize(getDictionaryEntry("administration.not.given", locale)));

            if (administration.getNotAdministeredReason() != null
                && administration.getNotAdministeredReason().getName() != null)
            {
              dose.append(" - ").append(administration.getNotAdministeredReason().getName());
            }
          }
          else
          {
            if (administration instanceof StartAdministrationDto)
            {
              final TherapyDoseDto administeredDose = ((StartAdministrationDto)administration).getAdministeredDose();
              if (administeredDose != null &&
                  administeredDose.getNumerator() != null &&
                  administeredDose.getNumeratorUnit() != null)
              {
                try
                {
                  dose.append(NumberFormatters.doubleFormatter2(locale)
                                  .valueToString(administeredDose.getNumerator()) + " " + administeredDose.getNumeratorUnit());
                }
                catch (final ParseException e)
                {
                  e.printStackTrace();
                }
              }
            }
            else if (administration instanceof AdjustInfusionAdministrationDto)
            {
              final TherapyDoseDto administeredDose = ((AdjustInfusionAdministrationDto)administration).getAdministeredDose();
              if (administeredDose.getNumerator() != null && administeredDose.getNumeratorUnit() != null)
              {
                try
                {
                  dose.append(
                      NumberFormatters.doubleFormatter2(locale).valueToString(administeredDose.getNumerator())
                          + " " + administeredDose.getNumeratorUnit());
                }
                catch (ParseException e)
                {
                  e.printStackTrace();
                }
              }
            }
            else if (administration instanceof InfusionSetChangeDto)
            {
              //noinspection SwitchStatement
              switch (((InfusionSetChangeDto)administration).getInfusionSetChangeEnum())
              {
                case INFUSION_SYRINGE_CHANGE:
                  dose.append(getDictionaryEntry("infusion.syringe.change", locale));
                  break;

                case INFUSION_SYSTEM_CHANGE:
                  dose.append(getDictionaryEntry("infusion.system.change", locale));
                  break;
              }
            }
            else if (administration instanceof StopAdministrationDto)
            {
              dose.append(getDictionaryEntry("infusion.stopped", locale));
            }
          }

          if (administration.getComment() != null)
          {
            dose.append(" - ").append(administration.getComment());
          }

          administrationString.append(String.format(
              "<b>%02d:%02d<br></b>%s<br><br>",
              administrationTime.getHourOfDay(),
              administrationTime.getMinuteOfHour(),
              dose));
        }
      }
    }

    return administrationString.toString();
  }

  private static String extractComment(
      final @NonNull TherapyDto order,
      final Locale locale)
  {
    final StringBuilder comment = new StringBuilder();

    if (order.getComment() != null)
    {
      comment.append(order.getComment()).append("     ");
  }

    return comment.toString();
  }

  private static String extractIndication(
      final @NonNull TherapyDto order,
      final Locale locale)
  {
    final StringBuilder indication = new StringBuilder();

    if (order.getClinicalIndication() != null)
    {
      indication.append(order.getClinicalIndication().getName()).append("     ");
    }
    return indication.toString();
  }

  private static String extractCommentAndIndication(
      final @NonNull TherapyDto order,
      final Locale locale)
  {
    final StringBuilder comment = new StringBuilder();

    if (order.getComment() != null)
    {
      final String commentString = StringUtils.capitalize(getDictionaryEntry("comment", locale));
      comment.append(commentString).append(": <B>").append(order.getComment()).append("</B>     ");
    }
    if (order.getClinicalIndication() != null)
    {
      final String indicationString = StringUtils.capitalize(getDictionaryEntry("indication", locale));
      comment.append(indicationString).append(": <B>").append(order.getClinicalIndication().getName()).append("</B>     ");
    }
    return comment.toString();
  }

  public static String getWarnings(final TherapyDto order, final Locale locale)
  {
    final StringBuilder warning = new StringBuilder();
    final String warningOverriddenString = StringUtils.capitalize(getDictionaryEntry("warning.overridden", locale));
    warning.append(warningOverriddenString);
    if (!order.getCriticalWarnings().isEmpty())
    {
      for (final String criticalWarning : order.getCriticalWarnings())
      {
        warning.append(": <B>").append(criticalWarning).append("</B>");
        if (order.getCriticalWarnings().size() > 1)
        {
          warning.append("<br>");
        }
      }
    }
    else
    {
      warning.append(": <B>/</B>");
    }
    return warning.toString();
  }

  public static String getX(
      final TherapyReportStatusEnum therapyReportStatusEnum,
      final @NonNull TherapyDto therapy,
      final @NonNull TherapyReportHourDoseTimeDto rowTime,
      final @NonNull Date firstColumnDate,
      final int column)
  {
    final Boolean shouldDisplayX = shouldDisplayX(therapyReportStatusEnum, therapy, rowTime, firstColumnDate, column);
    return shouldDisplayX ? "X" : "";
  }

  public static String getPrnX(final TherapyReportStatusEnum therapyReportStatusEnum)
  {
    if ((therapyReportStatusEnum == TherapyReportStatusEnum.SUSPENDED) || (therapyReportStatusEnum == TherapyReportStatusEnum.FINISHED))
    {
      return "X";
    }

    return "";
  }

  @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
  private static boolean shouldDisplayX(
      final TherapyReportStatusEnum therapyReportStatusEnum,
      final @NonNull TherapyDto therapy,
      final @NonNull TherapyReportHourDoseTimeDto rowTime,
      final @NonNull Date firstColumnDate,
      final int column)
  {
    if ((therapyReportStatusEnum == TherapyReportStatusEnum.SUSPENDED) ||
        (therapyReportStatusEnum == TherapyReportStatusEnum.FINISHED))
    {
      return true;
    }

    final DateTime columnDate = new DateTime(firstColumnDate).withTimeAtStartOfDay().plusDays(column);

    if (therapy.getDosingFrequency() != null)
    {
      if (therapy.getDosingFrequency().getType() == DosingFrequencyTypeEnum.ONCE_THEN_EX)
      {
        return shouldDisplayXForStatDoses(therapy, columnDate);
      }
    }

    final boolean isConstantContinuousInfusion = therapy instanceof ConstantComplexTherapyDto &&
        ((ConstantComplexTherapyDto)therapy).isContinuousInfusion();
    final boolean isOxygenTherapy = therapy instanceof OxygenTherapyDto;

    if (isConstantContinuousInfusion || isOxygenTherapy)
    {
      final DateTime start = therapy.getStart().withTimeAtStartOfDay();
      if (columnDate.isBefore(start))
      {
        return true;
      }

      if (therapy.getEnd() != null)
      {
        final DateTime end = therapy.getEnd().plusDays(1).withTimeAtStartOfDay();
        if (columnDate.isAfter(end) || columnDate.isEqual(end))
        {
          return true;
        }
      }
      return false;
    }

    if (rowTime.getTime() != null)
    {
      final DateTime cellDateTime = columnDate
          .plusHours(rowTime.getTime().getHour())
          .plusMinutes(rowTime.getTime().getMinute());

      if (cellDateTime.isBefore(therapy.getStart()))
      {
        return true;
      }

      if (therapy.getEnd() != null && (cellDateTime.isAfter(therapy.getEnd()) || cellDateTime.isEqual(therapy.getEnd())))
      {
        return true;
      }

      if (!TherapyTimingUtils.isInValidDaysOfWeek(cellDateTime, therapy.getDaysOfWeek()))
      {
        return true;
      }
      return !TherapyTimingUtils.isInValidDaysFrequency(therapy.getStart(), cellDateTime, therapy.getDosingDaysFrequency());
    }
    return false;
  }

  private static boolean isOutsideTherapyInterval(final TherapyDto therapy, final DateTime date)
  {
    if (date.isBefore(therapy.getStart()))
    {
      return true;
    }

    if (therapy.getEnd() != null && (date.isAfter(therapy.getEnd()) || date.isEqual(therapy.getEnd())))
    {
      return true;
    }

    return false;
  }

  @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
  private static boolean shouldDisplayXForStatDoses(final TherapyDto therapy, final DateTime columnDate)
  {
    final DateTime startTherapyTime = therapy.getStart().withTimeAtStartOfDay();
    return !columnDate.equals(startTherapyTime);
  }

  public static String getGivenCheckedShorted(final @NonNull Locale locale, final int column)
  {
    if (column == 0)
    {
      return dictionary.getEntry(
          "administration.given.shorted",
          GrammaticalGender.UNDEFINED,
          locale);
    }
    if (column == 1)
    {
      return dictionary.getEntry(
          "administration.checked.shorted",
          GrammaticalGender.UNDEFINED,
          locale);
    }
    return null;
  }

  public static String getTimeDoseLabel(final @NonNull Locale locale)
  {
    return dictionary.getEntry(
        "timedose",
        GrammaticalGender.UNDEFINED,
        locale);
  }

  public static String getProtocolDoseTimeDisplay(
      final @NonNull Date startDate,
      final int column,
      final int row,
      final Map<DateTime, List<TherapyReportHourDoseTimeDto>> dateDoseTimes)
  {
    final TherapyReportHourDoseTimeDto protocolDoseTimeDto = getProtocolTime(startDate, column, row, dateDoseTimes);
    return protocolDoseTimeDto != null ? protocolDoseTimeDto.getDoseTimeDisplay() : "";
  }

  public static TherapyReportHourDoseTimeDto getProtocolTime(
      final @NonNull Date startDate,
      final int column,
      final int row,
      final Map<DateTime, List<TherapyReportHourDoseTimeDto>> dateDoseTimes
  )
  {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(startDate);
    calendar.add(Calendar.DATE, column);

    final DateTime currentTime = new DateTime(calendar.getTime()).withTimeAtStartOfDay();

    final List<TherapyReportHourDoseTimeDto> doseTimes = dateDoseTimes.get(currentTime);

    if (doseTimes != null && row < doseTimes.size())
    {
      return doseTimes.get(row);
    }
    return null;
  }

  public static int calculateMaxSizeOfColumnsInMatrix(final Map<DateTime, List<TherapyReportHourDoseTimeDto>> map)
  {
   return map.values().stream()
        .map(List::size)
        .mapToInt(v -> v)
        .max()
        .orElse(0);
  }

  public static String getPastTherapyCombinedDisplay(
      final @NonNull TherapyDto order,
      final String therapyStart,
      final String therapyEnd,
      final TherapyReportStatusEnum therapyReportStatus,
      final @NonNull Locale locale,
      final String currentRate,
      final String consecutiveDay,
      final boolean showConsecutiveDay,
      final String consecutiveDayLabel,
      final String currentOxygenDevice,
      final String lastInrResult)
  {
    if (order instanceof SimpleTherapyDto)
    {
      return getSimpleCombinedDisplay((SimpleTherapyDto)order,
                                      consecutiveDay,
                                      showConsecutiveDay,
                                      therapyStart,
                                      therapyEnd,
                                      therapyReportStatus,
                                      locale,
                                      consecutiveDayLabel,
                                      lastInrResult);
    }

    if (order instanceof ComplexTherapyDto)
    {
      return getComplexCombinedDisplay((ComplexTherapyDto)order,
                                       therapyStart,
                                       therapyEnd,
                                       therapyReportStatus,
                                       locale,
                                       currentRate,
                                       consecutiveDay,
                                       showConsecutiveDay,
                                       consecutiveDayLabel,
                                       lastInrResult);
    }

    if (order instanceof OxygenTherapyDto)
    {
      return getOxygenCombinedDisplay((OxygenTherapyDto)order,
                                      therapyStart,
                                      therapyEnd,
                                      therapyReportStatus,
                                      locale,
                                      currentRate,
                                      consecutiveDay,
                                      showConsecutiveDay,
                                      consecutiveDayLabel,
                                      currentOxygenDevice);
    }

    return null;
  }

  public static String getTimeOfCreationLabelAndDate(final @NonNull Locale locale)
  {
    final DateTime currentTime = CurrentTimeProvider.get();

    final String currentTimeFormatted = DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(currentTime);

    return dictionary.getEntry("time.of.creation", GrammaticalGender.UNDEFINED, locale).toUpperCase()
        + " "
        + currentTimeFormatted;
  }

  public static String getComplexCombinedDisplay(
      final @NonNull ComplexTherapyDto order,
      final String therapyStart,
      final String therapyEnd,
      final TherapyReportStatusEnum therapyReportStatus,
      final @NonNull Locale locale,
      final String currentRate,
      final String consecutiveDay,
      final boolean showConsecutiveDay,
      final String consecutiveDayLabel,
      final String lastTitrationResult)
  {
    try
    {
      // for empty report
      if (order.getIngredientsList().isEmpty())
      {
        return "<BR><BR><BR><BR><BR><BR><BR><BR>";
      }

      final String volumeDisplay = getVolumeDisplay(order, locale);
      final String additionalInstructionDisplay = order.getAdditionalInstructionDisplay();

      final StringBuilder strBuilder = new StringBuilder();

      return
          strBuilder
              .append(
                  getValueHtml(
                      volumeDisplay,
                      false,
                      false,
                      false,
                      strBuilder.length() > 0,
                      locale))
              .append(
                  getValueHtml(
                      "rate",
                      getQuantityDisplay(order, locale),
                      true,
                      false,
                      strBuilder.length() > 0
                          && StringUtils.isNotBlank(volumeDisplay)
                          && StringUtils.isBlank(additionalInstructionDisplay)
                          && order instanceof ConstantComplexTherapyDto,
                      strBuilder.length() > 0,
                      locale))
              //.append(strBuilder.length() > 0 && order instanceof CombinedComplexMedicationOrderDto ? "<br> " : "")
              .append(
                  order instanceof VariableComplexTherapyDto
                  ? ""
                  : getValueHtml(
                      "dose",
                      order.getSpeedFormulaDisplay(),
                      true,
                      false,
                      strBuilder.length() > 0,
                      false,
                      locale))
              .append(
                  order instanceof ConstantComplexTherapyDto
                  ? getValueHtml(
                      "duration",
                      ((ConstantComplexTherapyDto)order).getDurationDisplay(),
                      true,
                      false,
                      strBuilder.length() > 0,
                      false,
                      locale)
                  : "")
              .append(getRecurringContinuousInfusionDisplay(order, locale))
              .append(
                  getValueHtml(
                      order.isContinuousInfusion() ? "continuous.infusion" : "dosing.interval",
                      generateFrequencyDisplay(order),
                      true,
                      true,
                      false,
                      strBuilder.length() > 0,
                      locale))
              .append(getAdditionalDoseDisplay(order, locale))
              .append(
                  getValueHtml(
                      "from",
                      getTherapyIntervalDisplay(therapyStart, therapyEnd, locale),
                      true,
                      true,
                      false,
                      strBuilder.length() > 0,
                      locale))
              .append(showConsecutiveDay ? getValueHtml(consecutiveDayLabel,
                                   consecutiveDay,
                                   true,
                                   false,
                                   false,
                                   strBuilder.length() > 0,
                                   locale) : "")
              .append(currentRate != null ?
                  getValueHtml(
                      dictionary.getEntry(
                          "current.rate",
                          GrammaticalGender.UNDEFINED,
                          locale),
                      currentRate + " mL/h",
                      true,
                      true,
                      false,
                      strBuilder.length() > 0,
                      locale)
                  : "")
              .append(getValueHtml("commentary",
                                   extractComment(order,locale),
                                   true,
                                   false,
                                   false,
                                   strBuilder.length() > 0,
                                   locale))
              .append(getValueHtml("indication",
                                   extractIndication(order,locale),
                                   true,
                                   false,
                                   false,
                                   strBuilder.length() > 0,
                                   locale))
              .append(order.getMaxDailyFrequency() != null ? getValueHtml(
                  "dosing.max.24h",
                  String.valueOf(order.getMaxDailyFrequency()),
                  true,
                  false,
                  false,
                  strBuilder.length() > 0,
                  locale
              ) : "")
              .append(lastTitrationResult != null ? getValueHtml(
                  lastTitrationResult,
                  false,
                  false,
                  false,
                  strBuilder.length() > 0,
                  locale) : "")
              .append(order.getPrescriberName() != null ? getValueHtml(
                  "prescriber",
                  order.getPrescriberName(),
                  true,
                  false,
                  false,
                  strBuilder.length() > 0,
                  locale) : "")
              .append(getTherapyStatusDisplay(therapyReportStatus, locale))

              // ugly hack to prevent jasper from showing only one therapy description if therapies are identical
              .append("<span style='font-size:1px;color:white'>")
              .append(order.hashCode())
              .append("</span>")
              .toString();
    }
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  public static String getOxygenCombinedDisplay(
      final @NonNull OxygenTherapyDto order,
      final String therapyStart,
      final String therapyEnd,
      final TherapyReportStatusEnum therapyReportStatus,
      final @NonNull Locale locale,
      final String currentRate,
      final String consecutiveDay,
      final boolean showConsecutiveDay,
      final String consecutiveDayLabel,
      final String currentOxygenDevice)
  {
    final StringBuilder strBuilder = new StringBuilder();
    final String oxygenDevice = currentOxygenDevice != null ? currentOxygenDevice : order.getStartingDeviceDisplay();
    return strBuilder
        .append(getMedicationDisplay(order.getMedication()))
        .append(getValueHtml(
            "target.saturation",
            order.getSaturationDisplay(),
            true,
            false,
            false,
            strBuilder.length() > 0,
            locale))
        .append(getValueHtml(
            "rate",
            order.getSpeedDisplay(),
            true,
            false,
            false,
            strBuilder.length() > 0,
            locale))
        .append(oxygenDevice != null ? getValueHtml(
            "device",
            oxygenDevice,
            true,
            false,
            false,
            strBuilder.length() > 0,
            locale) : "")
        .append(order.isHumidification() ? getValueHtml(
            dictionary.getEntry("humidification", GrammaticalGender.UNDEFINED, locale),
            true,
            false,
            false,
            strBuilder.length() > 0,
            locale) : "")
        .append(order.getFlowRateMode() == FlowRateMode.HIGH_FLOW ?
                getValueHtml(
                    dictionary.getEntry("high.flow.oxygen.therapy", GrammaticalGender.UNDEFINED, locale),
                    true,
                    false,
                    false,
                    strBuilder.length() > 0,
                    locale) : "")
        .append(
            getValueHtml(
                "from",
                getTherapyIntervalDisplay(therapyStart, therapyEnd, locale),
                true,
                true,
                false,
                strBuilder.length() > 0,
                locale))
        .append(showConsecutiveDay ? getValueHtml(consecutiveDayLabel,
                             consecutiveDay,
                             true,
                             false,
                             false,
                             strBuilder.length() > 0,
                             locale): "")
        .append(currentRate != null ?
                getValueHtml("current.rate",
                    currentRate + " L/min",
                    true,
                    true,
                    false,
                    strBuilder.length() > 0,
                    locale)
                                    : "")
        .append(getValueHtml("commentary",
                             extractComment(order,locale),
                             true,
                             false,
                             false,
                             strBuilder.length() > 0,
                             locale))
        .append(getValueHtml("indication",
                             extractIndication(order,locale),
                             true,
                             false,
                             false,
                             strBuilder.length() > 0,
                             locale))
        .append(getTherapyStatusDisplay(therapyReportStatus, locale))
        .append(order.getPrescriberName() != null ? getValueHtml(
            "prescriber",
            order.getPrescriberName(),
            true,
            false,
            false,
            strBuilder.length() > 0,
            locale) : "")
        .toString();
  }

  public static String extractDoseTimeDisplay(final Object therapyReportHourDoseTimeDto)
  {
    return therapyReportHourDoseTimeDto != null
           ? ((TherapyReportHourDoseTimeDto)therapyReportHourDoseTimeDto).getDoseTimeDisplay()
           : null;
  }

  public static String getActiveReportPrecaution(final Locale locale)
  {
    return dictionary.getEntry("active.reports.precaution", GrammaticalGender.UNDEFINED, locale);
  }

  private static String getRecurringContinuousInfusionDisplay(final ComplexTherapyDto therapyDto, final Locale locale)
  {
    if (therapyDto instanceof VariableComplexTherapyDto &&
        ((VariableComplexTherapyDto)therapyDto).isRecurringContinuousInfusion())
    {
      return "<BR>" + getLabelHtml("repeat.every.24h", locale);
    }
    return "";
  }

  private static String getAdditionalDoseDisplay(final TherapyDto order, final Locale locale)
  {
    final StringBuilder strBuilder = new StringBuilder();

    final String display = getWhenNeededOrStartCriterionDisplay(order);
    if (StringUtils.isNotBlank(display))
    {
      strBuilder
          .append(VALUES_DELIMITER_BEFORE_AFTER)
          .append(getValueHtml(display, false, true, false, false, locale));
    }
    if (StringUtils.isNotBlank(order.getApplicationPreconditionDisplay()))
    {
      strBuilder
          .append(VALUES_DELIMITER_BEFORE_AFTER)
          .append(getValueHtml(order.getApplicationPreconditionDisplay(), false, true, false, false, locale));
    }

    if (!order.getRoutes().isEmpty())
    {
      strBuilder.append(VALUES_DELIMITER_BEFORE_AFTER);
      strBuilder.append(
          order.getRoutes()
              .stream()
              .map(route -> getValueHtml(route.getName(), true, true, false, false, locale))
              .collect(Collectors.joining(", ")));
    }

    return strBuilder.toString();
  }

  private static String getVolumeDisplay(final ComplexTherapyDto order, final Locale locale)
  {
    final StringBuilder strBuilder = new StringBuilder();

    strBuilder.append(
        getValueHtml("volume.total", order.getVolumeSumDisplay(), true, false, false, false, locale));

    final String additionalInstructionDisplay = order.getAdditionalInstructionDisplay();
    if (StringUtils.isNotBlank(additionalInstructionDisplay))
    {
      strBuilder.append(
          getValueHtml(" + " + additionalInstructionDisplay, false, strBuilder.length() > 0, false, false, locale));
    }

    return strBuilder.toString();
  }

  public static String getMedicationDisplay(final @NonNull MedicationDto medication)
  {
    try
    {
      return
          medication.getGenericName() == null || medication.getMedicationType() == MedicationTypeEnum.DILUENT
          ? medication.getName()
          : "<b>" + medication.getGenericName() + "</b>" + SPACER + '(' + medication.getName() + ')';
    }
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  public static String getWhenNeededOrStartCriterionDisplay(final @NonNull TherapyDto order)
  {
    try
    {
      final StringBuilder strBuilder = new StringBuilder();
      if (StringUtils.isNotBlank(order.getWhenNeededDisplay()))
      {
        strBuilder.append(order.getWhenNeededDisplay());
      }
      if (StringUtils.isNotBlank(order.getStartCriterionDisplay()))
      {
        strBuilder.append(order.getStartCriterionDisplay());
      }
      return strBuilder.toString();
    }
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  private static String getTherapyIntervalDisplay(final String therapyStart, final String therapyEnd, final Locale locale)
  {
    return therapyStart + (therapyEnd == null ? "" : "&nbsp;" + getLabelHtml("to", locale) + "&nbsp;" + therapyEnd);
  }

  private static String getTherapyStatusDisplay(final TherapyReportStatusEnum therapyReportStatus, final Locale locale)
  {
    if (therapyReportStatus == TherapyReportStatusEnum.FINISHED)
    {
      return "<BR>" + getLabelHtml("status", locale) + " " + "<b>" + dictionary.getEntry(
          "stopped.therapy",
          GrammaticalGender.UNDEFINED,
          locale)+"</b>";
    }
    else if (therapyReportStatus == TherapyReportStatusEnum.SUSPENDED)
    {
      return "<BR>" + getLabelHtml("status", locale) + " " + "<b>" + dictionary.getEntry(
          "suspended.therapy",
          GrammaticalGender.UNDEFINED,
          locale)+"</b>";
    }
    return "";
  }

  private static String getQuantityDisplay(final SimpleTherapyDto order)
  {
    if (order instanceof ConstantSimpleTherapyDto)
    {
      return order.getQuantityDisplay();
    }
    else
    {
      final StringBuilder strBuilder = new StringBuilder();
      final List<TimedSimpleDoseElementDto> timedDoseElements = ((VariableSimpleTherapyDto)order).getTimedDoseElements()
          .stream()
          .filter(element -> element.getQuantityDisplay() != null)
          .collect(Collectors.toList());

      final boolean simpleVariable = timedDoseElements.get(0).getDate() == null;

      if (simpleVariable)
      {
        timedDoseElements
            .forEach(element -> strBuilder.append(combineTimeAndQuantity(
                null,
                element.getTimeDisplay(),
                element.getQuantityDisplay())));
      }
      else
      {
        Collections.sort(timedDoseElements, Comparator.comparing(TimedSimpleDoseElementDto::getDate));

        DateTime currentDate = null;
        for (final TimedSimpleDoseElementDto timedDoseElement : timedDoseElements)
        {
          if (timedDoseElement.getDate().equals(currentDate))
          {
            strBuilder.append(combineTimeAndQuantity(
                null,
                timedDoseElement.getTimeDisplay(),
                timedDoseElement.getQuantityDisplay()));
          }
          else
          {
            strBuilder.append(combineTimeAndQuantity(
                timedDoseElement.getDate(),
                timedDoseElement.getTimeDisplay(),
                timedDoseElement.getQuantityDisplay()));

            currentDate = timedDoseElement.getDate();
          }
        }
      }

      return strBuilder.toString();
    }
  }

  private static String getQuantityDisplay(final ComplexTherapyDto order, final Locale locale)
  {
    if (order.isAdjustToFluidBalance())
    {
      return dictionary.getEntry("adjust.to.fluid.balance.short", GrammaticalGender.UNDEFINED, locale);
    }
    if (order instanceof ConstantComplexTherapyDto)
    {
      return order.getSpeedDisplay();
    }
    else
    {
      final StringBuilder strBuilder = new StringBuilder();
      if (order instanceof VariableComplexTherapyDto)
      {
        for (final TimedComplexDoseElementDto element : ((VariableComplexTherapyDto)order).getTimedDoseElements())
        {
          final String speedFormulaDisplay = element.getSpeedFormulaDisplay() != null
                                             ? " (" + element.getSpeedFormulaDisplay() + ")"
                                             : "";

          strBuilder.append(
              combineTimeAndQuantity(null, element.getIntervalDisplay(), element.getSpeedDisplay() + speedFormulaDisplay));
        }
        return strBuilder.toString();
      }
      else
      {
        return "";
      }
    }
  }

  private static String combineTimeAndQuantity(final DateTime date, final String time, final String quantity)
  {
    final StringBuilder sb = new StringBuilder();

    if (date != null)
    {
      sb.append("<br>" + date.getDayOfMonth() + "." + date.getMonthOfYear());
    }

    sb.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;" + time + "&nbsp;-&nbsp;" + quantity);

    return sb.toString();
  }

  static String generateFrequencyDisplay(final TherapyDto order)
  {
    final String frequencyDisplay = order.getFrequencyDisplay();
    final String daysOfWeekDisplay = order.getDaysOfWeekDisplay();
    final String daysFrequencyDisplay = order.getDaysFrequencyDisplay();

    if (!StringUtils.isBlank(daysOfWeekDisplay))
    {
      return
          StringUtils.defaultString(frequencyDisplay)
              + (StringUtils.isBlank(daysOfWeekDisplay) ? "" : VALUES_DELIMITER_BEFORE_AFTER + daysOfWeekDisplay);
    }

    if (daysFrequencyDisplay != null)
    {
      return StringUtils.defaultString(frequencyDisplay) + VALUES_DELIMITER_BEFORE_AFTER + daysFrequencyDisplay;
    }

    return frequencyDisplay;
  }

  private static String getValueHtml(
      final String value,
      final boolean highlightValue,
      final boolean addEmptyValue,
      final boolean addSpacerBefore,
      final boolean addNewLineBefore,
      final Locale locale)
  {
    return getValueHtml(null, value, highlightValue, addEmptyValue, addSpacerBefore, addNewLineBefore, locale);
  }

  private static String getValueHtml(
      final String labelOrKey,
      final String value,
      final boolean highlightValue,
      final boolean addEmptyValue,
      final boolean addSpacerBefore,
      final boolean addNewLineBefore,
      final Locale locale)
  {
    if (!addEmptyValue && StringUtils.isBlank(value))
    {
      return "";
    }

    final String label = getLabelHtml(labelOrKey, locale);
    return
        (addSpacerBefore ? VALUES_DELIMITER_BEFORE_AFTER : "")
            + (!addSpacerBefore && addNewLineBefore ? "<br>" : "")
            + (StringUtils.isNotBlank(label) ? label + "&nbsp;" : "")
            + (highlightValue ? "<b>" : "")
            + StringUtils.defaultString(value)
            + (highlightValue ? "</b>" : "");
  }

  private static String getLabelHtml(final String labelOrKey, final Locale locale)
  {
    if (StringUtils.isBlank(labelOrKey))
    {
      return "";
    }
    String entry;
    try
    {
      entry = dictionary.getEntry(labelOrKey, GrammaticalGender.UNDEFINED, locale);
    }
    catch (final MissingResourceException ex)
    {
      entry = labelOrKey;
    }
    return "<span style=\"color:" + LABEL_COLOR + "; font-size:5px;\">"
               + entry != null ? entry.toUpperCase() : labelOrKey.toUpperCase()
               + "</span>";
  }

  public static String getDictionaryEntry(final String key, final Locale locale)
  {
    try
    {
      return dictionary.getEntry(key, GrammaticalGender.UNDEFINED, locale);
    }
    catch (final Exception e)
    {
      return exceptionToString(e);
    }
  }

  private static String exceptionToString(final Exception e)
  {
    return "<span style='color:red; font-size:10px;'>" + e.toString() + "</span>";
  }

  public static String getPatientAllergies(final PatientDataForTherapyReportDto patientData,
                                           @NonNull Locale locale)
  {
    if (patientData.getAllergiesStatus() == AllergiesStatus.PRESENT)
    {
      return patientData.getAllergies()
          .toString().replace("[", "").replace(", ", "<BR>").replace("]", "");
    }

    return com.marand.ispek.common.Dictionary.getEntry(patientData.getAllergiesStatus(), locale);
  }
}