package com.marand.thinkmed.medications.therapy.report;

import java.text.Collator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.Pair;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.time.DateTimeFormatters;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.medications.TherapyReportStatusEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantTherapy;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTypeEnum;
import com.marand.thinkmed.medications.dto.administration.DoseAdministration;
import com.marand.thinkmed.medications.dto.overview.OxygenTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.RateTherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayElementReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayPastElementDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportAdministrationDateGroupDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportAdministrationDto;
import com.marand.thinkmed.medications.dto.report.TherapyDayReportDto;
import com.marand.thinkmed.medications.dto.report.TherapyReportHourDoseTimeDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportDto;
import com.marand.thinkmed.medications.dto.report.TherapySurgeryReportElementDto;
import com.marand.thinkmed.medications.dto.report.TherapyTemplateReportDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.preferences.MedicationPreferencesUtil;
import com.marand.thinkmed.medications.report.TherapyReportDataProvider;
import com.marand.thinkmed.medications.titration.TitrationDataProvider;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class TherapyReportDataProviderImpl implements TherapyReportDataProvider
{
  private MedicationsDao medicationsDao;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private MedicationsConnector medicationsConnector;
  private OverviewContentProvider overviewContentProvider;
  private AdministrationProvider administrationProvider;
  private RequestDateTimeHolder requestDateTimeHolder;
  private MedsProperties medsProperties;
  private TherapyDisplayProvider therapyDisplayProvider;
  private TitrationDataProvider titrationDataProvider;

  @Autowired
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setMedicationsConnector(final MedicationsConnector medicationsConnector)
  {
    this.medicationsConnector = medicationsConnector;
  }

  @Autowired
  public void setOverviewContentProvider(final OverviewContentProvider overviewContentProvider)
  {
    this.overviewContentProvider = overviewContentProvider;
  }

  @Autowired
  public void setAdministrationProvider(final AdministrationProvider administrationProvider)
  {
    this.administrationProvider = administrationProvider;
  }

  @Autowired
  public void setRequestDateTimeHolder(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setTitrationDataProvider(final TitrationDataProvider titrationDataProvider)
  {
    this.titrationDataProvider = titrationDataProvider;
  }

  @Override
  public TherapyDayReportDto getActiveAndPastTherapiesReportData(
      final @NonNull String patientId,
      final @NonNull Locale locale)
  {
    final DateTime now = requestDateTimeHolder.getRequestTimestamp();
    int pastDays = medsProperties.getPrintPastAdministrationDays();
    if (pastDays > 31)
    {
      pastDays = 31;
    }
    return getTherapyReportData(patientId, locale, now.minusDays(pastDays), now, now, false);
  }

  @Override
  public TherapyDayReportDto getPastTherapiesReportData(
      final @NonNull String patientId,
      final @NonNull Locale locale,
      final @NonNull DateTime startDate,
      final @NonNull DateTime endDate)
  {
    return getTherapyReportData(patientId, locale, startDate, endDate, requestDateTimeHolder.getRequestTimestamp(), true);
  }

  @Override
  public TherapyTemplateReportDto getTemplateReport(
      final @NonNull String patientId,
      final int numberOfPages,
      final @NonNull Locale locale)
  {
    final DateTime now = requestDateTimeHolder.getRequestTimestamp();

    final PatientDataForTherapyReportDto patientData = getTherapyDayPatientReportData(
        patientId,
        true,
        locale,
        now);

    if (patientData == null)
    {
      return null;
    }

    return new TherapyTemplateReportDto(numberOfPages, patientData);
  }

  private TherapyDayReportDto getTherapyReportData(
      final @NonNull String patientId,
      final @NonNull Locale locale,
      final @NonNull DateTime administrationsIntervalStart,
      final @NonNull DateTime administrationsIntervalEnd,
      final @NonNull DateTime when,
      final boolean historicElementsOnly)
  {
    final DateTime now = requestDateTimeHolder.getRequestTimestamp();

    // Load patient data

    final PatientDataForTherapyReportDto patientData = getTherapyDayPatientReportData(
        patientId,
        true,
        locale,
        now);

    if (patientData == null)
    {
      return null;
    }

    final DateTime reportGeneratingTime =
        historicElementsOnly
        ? administrationsIntervalEnd
        : requestDateTimeHolder.getRequestTimestamp();

    final Interval searchInterval =
        historicElementsOnly ?
        new Interval(administrationsIntervalStart, administrationsIntervalEnd) :
        Intervals.infiniteFrom(administrationsIntervalStart);

    final String careProviderId = patientData.getCareProviderId();

    // Load therapies

    final List<InpatientPrescription> inpatientPrescriptions =
        medicationsOpenEhrDao.findInpatientPrescriptions(patientId, searchInterval);

    final List<AdministrationDto> administrations = administrationProvider.getPrescriptionsAdministrations(
        patientId,
        inpatientPrescriptions,
        null,
        false);

    List<TherapyRowDto> activeTherapies = Collections.emptyList();
    if (!historicElementsOnly)
    {
      activeTherapies = overviewContentProvider.buildTherapyRows(
          patientId,
          inpatientPrescriptions,
          administrations,
          Collections.emptyList(),
          TherapySortTypeEnum.CREATED_TIME_ASC,
          true,
          Collections.emptyList(),
          null,
          Intervals.infiniteFrom(now.minusDays(1)),
          MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
          locale,
          now);
    }

    final List<TherapyRowDto> pastTherapies = overviewContentProvider.buildTherapyRows(
        patientId,
        inpatientPrescriptions,
        administrations,
        Collections.emptyList(),
        TherapySortTypeEnum.CREATED_TIME_ASC,
        false,
        Collections.emptyList(),
        null,
        new Interval(administrationsIntervalStart, administrationsIntervalEnd),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        locale,
        now);

    final Map<String, Long> activeTherapyIdWithMedicationIdMap = getTherapyIdWithMedicationIdsMap(activeTherapies);

    final Map<Long, Pair<String, Integer>> activeCustomGroupNameSortOrderMap =
        careProviderId != null
        ? medicationsDao.getCustomGroupNameSortOrderMap(
            careProviderId,
            activeTherapyIdWithMedicationIdMap.values())
        : new HashMap<>();

    final List<TherapyDayElementReportDto> activeElements = activeTherapies.stream()
        .map(e -> mapTherapyRowToTherapyDayElement(
            patientId,
            e,
            activeCustomGroupNameSortOrderMap,
            activeTherapyIdWithMedicationIdMap,
            reportGeneratingTime,
            locale))
        .collect(Collectors.toList());

    final List<TherapyDayElementReportDto> protocolTitrationSimpleElementsList = activeElements.stream()
        .filter(pt -> checkIfElementIsTitration(pt.getOrder()) || checkIfElementIsProtocol(pt.getOrder()))
        .filter(pt -> pt.getOrder() instanceof SimpleTherapyDto)
        .collect(Collectors.toList());

    final List<TherapyDayElementReportDto> protocolTitrationComplexElementsList = activeElements.stream()
        .filter(pt -> checkIfElementIsTitration(pt.getOrder()) || checkIfElementIsProtocol(pt.getOrder()))
        .filter(pt -> pt.getOrder() instanceof ComplexTherapyDto)
        .collect(Collectors.toList());

    final List<TherapyDayElementReportDto> simpleElementsList = activeElements.stream()
        .filter(e -> checkIfElementIsSimple(e) || checkIfElementIsVariablAndNonProtocol(e))
        .collect(Collectors.toList());

    final List<TherapyDayElementReportDto> complexElementsList = activeElements.stream()
        .filter(this::checkIfElementIsComplex)
        .collect(Collectors.toList());

    final List<TherapyDayElementReportDto> oxygenElementsList = activeElements.stream()
        .filter(this::checkIfElementIsOxygen)
        .collect(Collectors.toList());

    final List<TherapyDayElementReportDto> prnSimpleElementsList = activeElements.stream()
        .filter(el -> checkIfElementIsPRN(el.getOrder()))
        .filter(a -> a.getOrder() instanceof SimpleTherapyDto || a.getOrder() instanceof OxygenTherapyDto)
        .collect(Collectors.toList());

    final List<TherapyDayElementReportDto> prnComplexElementsList = activeElements.stream()
        .filter(el -> checkIfElementIsPRN(el.getOrder()))
        .filter(a -> a.getOrder() instanceof ComplexTherapyDto)
        .collect(Collectors.toList());

    final List<TherapyDayPastElementDto> pastElementsSimpleList = pastTherapies.stream()
        .filter(pt -> !pt.getAdministrations().isEmpty())
        .filter(pt -> pt.getTherapy() instanceof SimpleTherapyDto || pt.getTherapy() instanceof OxygenTherapyDto)
        .map(pt -> mapTherapyRowToTherapyDayPastTherapyElement(pt, when, locale))
        .collect(Collectors.toList());

    final List<TherapyDayPastElementDto> pastElementsComlexList = pastTherapies.stream()
        .filter(pt -> !pt.getAdministrations().isEmpty())
        .filter(pt -> pt.getTherapy() instanceof ComplexTherapyDto)
        .map(pt -> mapTherapyRowToTherapyDayPastTherapyElement(pt, when, locale))
        .collect(Collectors.toList());

    sortSimpleTherapyReportElements(simpleElementsList);
    sortComplexTherapyReportElements(complexElementsList);

    return new TherapyDayReportDto(
        false,
        0,
        patientData,
        simpleElementsList,
        complexElementsList,
        oxygenElementsList,
        prnSimpleElementsList,
        prnComplexElementsList,
        protocolTitrationSimpleElementsList,
        protocolTitrationComplexElementsList,
        pastElementsSimpleList,
        pastElementsComlexList);
  }

  private TherapyDayPastElementDto mapTherapyRowToTherapyDayPastTherapyElement(
      final TherapyRowDto therapyRowDto,
      final DateTime when,
      final Locale locale)
  {
    final List<AdministrationDto> administrations = therapyRowDto.getAdministrations();
    final TherapyDto therapyDto = therapyRowDto.getTherapy();

    final List<AdministrationDto> givenAdministrationDtos = administrations.stream()
        .filter(a -> AdministrationResultEnum.ADMINISTERED.contains(a.getAdministrationResult()))
        .collect(Collectors.toList());

    final List<AdministrationDto> notGivenAdministrationDtos = administrations.stream()
        .filter(a -> a.getAdministrationResult() == AdministrationResultEnum.NOT_GIVEN)
        .collect(Collectors.toList());

    final List<AdministrationDto> deferredAdministrationDtos = administrations.stream()
        .filter(a -> a.getAdministrationResult() == AdministrationResultEnum.DEFER)
        .collect(Collectors.toList());

    final List<TherapyDayReportAdministrationDateGroupDto> givenAdministrations = mapAdministrations(
        givenAdministrationDtos,
        locale);

    final List<TherapyDayReportAdministrationDateGroupDto> notGivenAdministrations = mapAdministrations(
        notGivenAdministrationDtos,
        locale);

    final List<TherapyDayReportAdministrationDateGroupDto> deferredAdministrations = mapAdministrations(
        deferredAdministrationDtos,
        locale);

    final DateTime therapyEnd = therapyDto.getEnd();

    return new TherapyDayPastElementDto(
        therapyRowDto.getTherapy(),
        DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(therapyDto.getStart()),
        DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(therapyDto.getEnd()),
        mapCurrentRate(therapyRowDto, locale),
        null,
        mapTherapyReportStatus(therapyEnd, when, therapyRowDto.getTherapyStatus()),
        String.valueOf(therapyRowDto.getConsecutiveDay()),
        therapyRowDto.isShowConsecutiveDay(),
        givenAdministrations,
        notGivenAdministrations,
        deferredAdministrations,
        mapConsecutiveDays(),
        mapCurrentOxygenDevice(therapyRowDto, locale));
  }

  List<TherapyDayReportAdministrationDateGroupDto> mapAdministrations(
      final List<AdministrationDto> administrations,
      final Locale locale)
  {
    final Map<String, TherapyDayReportAdministrationDateGroupDto> administrationsByDate = new LinkedHashMap<>();

    for (final AdministrationDto administration : administrations)
    {
      final String date = mapToDateOfAdministration(administration, locale);
      if (administrationsByDate.get(date) == null)
      {
        administrationsByDate.put(date, new TherapyDayReportAdministrationDateGroupDto(date));
      }
      administrationsByDate.get(date).getTherapyDayReportAdministrationDtos().add(mapAdministration(administration, locale));
    }

    return new ArrayList<>(administrationsByDate.values());
  }

  private TherapyDayReportAdministrationDto mapAdministration(
      final AdministrationDto administration,
      final Locale locale)
  {
    final String comment = administration.getComment() != null ? administration.getComment() : "";
    return new TherapyDayReportAdministrationDto(mapToTimeDoseForTherapyDayReport(administration, locale), comment);
  }

  private String mapToTimeDoseForTherapyDayReport(final AdministrationDto administrationDto, final Locale locale)
  {
    final String time = String.valueOf(DateTimeFormatters.shortTime(locale)
                                           .withLocale(locale)
                                           .print(administrationDto.getAdministrationTime()));

    String dose = null;
    if (administrationDto instanceof DoseAdministration && administrationDto.getAdministrationType() != AdministrationTypeEnum.STOP)
    {
      final TherapyDoseDto administeredDose = ((DoseAdministration)administrationDto).getAdministeredDose();
      if (administeredDose != null)
      {
        dose = therapyDisplayProvider.decimalToString(administeredDose.getNumerator(), locale) + administeredDose.getNumeratorUnit();
      }
    }

    if (administrationDto.getAdministrationType() == AdministrationTypeEnum.STOP)
    {
      dose = Dictionary.getEntry("stopped.therapy", locale);
    }

    if (dose != null)
    {
      return time + " - " + dose;
    }
    return time;
  }

  @SuppressWarnings("ConfusingElseBranch")
  String mapToTimeDoseForTherapySurgeryReport(final AdministrationDto administrationDto, final Locale locale)
  {
    final String time = DateTimeFormatters.shortTime(locale)
        .withLocale(locale)
        .print(administrationDto.getAdministrationTime());

    if (!(administrationDto instanceof DoseAdministration))
    {
      return "";
    }

    if (AdministrationResultEnum.ADMINISTERED.contains(administrationDto.getAdministrationResult()))
    {
      final TherapyDoseDto administeredDose = ((DoseAdministration)administrationDto).getAdministeredDose();
      if (administeredDose != null)
      {
        if (administeredDose.getNumerator() == null || administeredDose.getNumeratorUnit() == null)
        {
          return time;
        }
        return time + " - " + therapyDisplayProvider.decimalToString(administeredDose.getNumerator(), locale) +
            " " + administeredDose.getNumeratorUnit();
      }
      return time;
    }
    else if (administrationDto.getAdministrationResult() == AdministrationResultEnum.DEFER)
    {
      final String comment = administrationDto.getComment();
      if (comment != null)
      {
        return time + " - " + Dictionary.getEntry("administration.deferred", locale).toUpperCase() + " - " + comment;
      }
      return time + " - " + Dictionary.getEntry("administration.deferred", locale).toUpperCase();
    }
    else if (administrationDto.getAdministrationResult() == AdministrationResultEnum.NOT_GIVEN)
    {
      if (administrationDto.getNotAdministeredReason() != null)
      {
        final String reason = administrationDto.getNotAdministeredReason().getName();
        return time + " - " + Dictionary.getEntry("not.given", locale).toUpperCase() + " - " + reason;
      }
      return time + " - " + Dictionary.getEntry("not.given", locale).toUpperCase();
    }
    return "";
  }

  private String mapToDateOfAdministration(
      final AdministrationDto administration,
      final Locale locale)
  {
    return DateTimeFormat.forPattern(Dictionary.getEntry("date.formatter.day.month", locale))
        .print(administration.getAdministrationTime());
  }

  private TherapyDayElementReportDto mapTherapyRowToTherapyDayElement(
      final String patientId, final TherapyRowDto therapyRow,
      final Map<Long, Pair<String, Integer>> customGroupNameSortOrderMap,
      final Map<String, Long> activeTherapyIdWithMedicationIdMap,
      final DateTime reportGeneratingTime,
      final Locale locale)
  {
    final TherapyDto therapy = therapyRow.getTherapy();

    if (therapy != null)
    {
      return createTherapyDayElementReportDto(
          patientId,
          therapy,
          therapyRow.getConsecutiveDay(),
          therapyRow.isShowConsecutiveDay(),
          therapyRow.getTherapyStatus(),
          customGroupNameSortOrderMap,
          activeTherapyIdWithMedicationIdMap.get(therapyRow.getTherapyId()),
          reportGeneratingTime,
          locale,
          mapCurrentRate(therapyRow, locale),
          mapCurrentOxygenDevice(therapyRow, locale));
    }
    return new TherapyDayElementReportDto();
  }

  private String mapCurrentRate(final TherapyRowDto therapyRow, final Locale locale)
  {
    if (therapyRow instanceof RateTherapyRowDto && ((RateTherapyRowDto)therapyRow).getCurrentInfusionRate() != null)
    {
      return therapyDisplayProvider.decimalToString(((RateTherapyRowDto)therapyRow).getCurrentInfusionRate(), locale);
    }

    return null;
  }

  private String mapCurrentOxygenDevice(final TherapyRowDto therapyRow, final Locale locale)
  {
    if (therapyRow instanceof OxygenTherapyRowDtoDto)
    {
      return therapyDisplayProvider.buildOxygenStartingDeviceDisplay(
          ((OxygenTherapyRowDtoDto)therapyRow).getCurrentStartingDevice(),
          locale);
    }

    return null;
  }

  private boolean checkIfElementIsProtocol(final TherapyDto therapyDto)
  {
    if (!(therapyDto instanceof VariableSimpleTherapyDto))
    {
      return false;
    }

    if (checkIfElementIsPRN(therapyDto))
    {
      return false;
    }

    final VariableSimpleTherapyDto variableSimpleTherapy = (VariableSimpleTherapyDto)therapyDto;

    return !variableSimpleTherapy.getTimedDoseElements().stream()
        .filter(tde -> tde.getDate() != null)
        .collect(Collectors.toList())
        .isEmpty();
  }

  private boolean checkIfElementIsTitration(final TherapyDto therapy)
  {
    return therapy instanceof ConstantTherapy && ((ConstantTherapy)therapy).getTitration() != null;
  }

  private boolean checkIfElementIsSimple(final TherapyDayElementReportDto element)
  {
    final TherapyDto therapy = element.getOrder();

    if (!(therapy instanceof SimpleTherapyDto))
    {
      return false;
    }

    if (checkIfElementIsProtocol(element.getOrder()))
    {
      return false;
    }

    if (checkIfElementIsTitration(element.getOrder()))
    {
      return false;
    }

    return !checkIfElementIsPRN(element.getOrder());
  }

  private boolean checkIfElementIsComplex(final TherapyDayElementReportDto element)
  {
    if (checkIfElementIsTitration(element.getOrder()))
    {
      return false;
    }

    final TherapyDto therapy = element.getOrder();

    if (checkIfElementIsPRN(element.getOrder()))
    {
      return false;
    }

    return therapy instanceof ComplexTherapyDto;
  }

  private boolean checkIfElementIsOxygen(final TherapyDayElementReportDto element)
  {
    final TherapyDto therapy = element.getOrder();

    if (checkIfElementIsPRN(therapy))
    {
      return false;
    }

    return therapy instanceof OxygenTherapyDto;
  }

  private boolean checkIfElementIsVariablAndNonProtocol(final TherapyDayElementReportDto element)
  {
    final TherapyDto therapy = element.getOrder();

    if (checkIfElementIsProtocol(therapy))
    {
      return false;
    }

    return therapy instanceof VariableSimpleTherapyDto;
  }

  private boolean checkIfElementIsPRN(final TherapyDto therapyDto)
  {
    return therapyDto.getWhenNeeded() != null && therapyDto.getWhenNeeded();
  }

  private void sortComplexTherapyReportElements(final List<TherapyDayElementReportDto> complexElementsList)
  {
    final Collator collator = Collator.getInstance();
    complexElementsList.sort((o1, o2) -> {
      final boolean firstOrderIsBaselineInfusion = ((ComplexTherapyDto)o1.getOrder()).isBaselineInfusion();
      final boolean secondOrderIsBaselineInfusion = ((ComplexTherapyDto)o2.getOrder()).isBaselineInfusion();

      //inactive therapies last
      final boolean firstTherapyActive = !isTherapyFinished(o1);
      final boolean secondTherapyActive = !isTherapyFinished(o2);
      if (firstTherapyActive && !secondTherapyActive)
      {
        return -1;
      }
      if (!firstTherapyActive && secondTherapyActive)
      {
        return 1;
      }
      //baseline infusions first
      if (firstOrderIsBaselineInfusion && !secondOrderIsBaselineInfusion)
      {
        return -1;
      }
      if (!firstOrderIsBaselineInfusion && secondOrderIsBaselineInfusion)
      {
        return 1;
      }
      return collator.compare(o1.getOrder().getTherapyDescription(), o2.getOrder().getTherapyDescription());
    });
  }

  private boolean isTherapyFinished(final TherapyDayElementReportDto reportDto)
  {
    return reportDto.getTherapyReportStatusEnum() == TherapyReportStatusEnum.FINISHED;
  }

  private void sortSimpleTherapyReportElements(final List<TherapyDayElementReportDto> simpleElementsList)
  {
    final Collator collator = Collator.getInstance();
    simpleElementsList.sort((o1, o2) -> {
      //inactive therapies last
      final boolean firstTherapyActive = !isTherapyFinished(o1);
      final boolean secondTherapyActive = !isTherapyFinished(o2);
      if (firstTherapyActive && !secondTherapyActive)
      {
        return -1;
      }
      if (!firstTherapyActive && secondTherapyActive)
      {
        return 1;
      }
      return collator.compare(o1.getOrder().getTherapyDescription(), o2.getOrder().getTherapyDescription());
    });
  }

  @Override
  public TherapySurgeryReportDto getTherapySurgeryReport(
      final @NonNull String patientId,
      final @NonNull Locale locale,
      final @NonNull DateTime when)
  {
    final PatientDataForTherapyReportDto patientReportData = getTherapyDayPatientReportData(
        patientId,
        false,
        locale,
        when);

    if (patientReportData == null)
    {
      return null;
    }

    final List<InpatientPrescription> inpatientPrescriptions =
        medicationsOpenEhrDao.findInpatientPrescriptions(patientId, Intervals.infiniteFrom(when.minusDays(1)));

    final List<AdministrationDto> administrations = administrationProvider.getPrescriptionsAdministrations(
        patientId,
        inpatientPrescriptions,
        null,
        false);

    final List<TherapyRowDto> activeTherapies = overviewContentProvider.buildTherapyRows(
        patientId,
        inpatientPrescriptions,
        administrations,
        Collections.emptyList(),
        TherapySortTypeEnum.CREATED_TIME_ASC,
        true,
        Collections.emptyList(),
        null,
        Intervals.infiniteFrom(when.minusDays(1)),
        MedicationPreferencesUtil.getDefaultRoundsIntervalDto(),
        locale,
        when);

    activeTherapies
        .forEach(t -> t.setAdministrations(extractOnlyLastAdministration(t.getAdministrations())));

    return mapTherapyRowDtoToTherapySurgeryReportDto(
        activeTherapies,
        locale,
        when,
        patientReportData);
  }

  List<AdministrationDto> extractOnlyLastAdministration(final List<AdministrationDto> allAdministrations)
  {
    return allAdministrations.stream()
        .filter(a -> a.getAdministrationType() != AdministrationTypeEnum.STOP)
        .filter(a -> a instanceof DoseAdministration)
        .sorted(Comparator.comparing(AdministrationDto::getAdministrationTime).reversed())
        .limit(1)
        .collect(Collectors.toList());
  }

  TherapySurgeryReportDto mapTherapyRowDtoToTherapySurgeryReportDto(
      final List<TherapyRowDto> activeTherapies,
      final Locale locale,
      final DateTime when,
      final PatientDataForTherapyReportDto patientReportData)
  {
    final List<TherapySurgeryReportElementDto> therapies = activeTherapies.stream()
        .map(therapyRow -> mapToTherapySurgeryReportElement(therapyRow, locale, when))
        .collect(Collectors.toList());

    final List<TherapySurgeryReportElementDto> simpleTherapies = therapies.stream()
        .filter(element -> element.getTherapy() instanceof SimpleTherapyDto || element.getTherapy() instanceof OxygenTherapyDto)
        .collect(Collectors.toList());

    final List<TherapySurgeryReportElementDto> complexTherapies = therapies.stream()
        .filter(element -> element.getTherapy() instanceof ComplexTherapyDto)
        .collect(Collectors.toList());

    return new TherapySurgeryReportDto(
        patientReportData,
        simpleTherapies,
        complexTherapies,
        DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(when));
  }

  private TherapySurgeryReportElementDto mapToTherapySurgeryReportElement(
      final TherapyRowDto therapyRow,
      final Locale locale,
      final DateTime when)
  {
    final String therapyStart = mapTherapyStart(therapyRow.getTherapy(), locale);
    final String therapyEnd = mapTherapyEnd(therapyRow.getTherapy().getEnd(), locale);

    final AdministrationDto administration = therapyRow.getAdministrations().isEmpty()
                                             ? null
                                             : therapyRow.getAdministrations().get(0);

    return new TherapySurgeryReportElementDto(
        administration != null ? mapToTimeDoseForTherapySurgeryReport(administration, locale) : null,
        administration != null ? mapToDateOfAdministration(administration, locale) : "",
        therapyRow.getTherapy(),
        therapyStart,
        therapyEnd,
        mapCurrentRate(therapyRow, locale),
        mapTherapyReportStatus(therapyRow.getTherapy().getEnd(), when, therapyRow.getTherapyStatus()),
        String.valueOf(therapyRow.getConsecutiveDay()),
        therapyRow.isShowConsecutiveDay(),
        mapConsecutiveDays());
  }

  private PatientDataForTherapyReportDto getTherapyDayPatientReportData(
      final String patientId,
      final boolean mainDiseaseTypeOnly,
      final Locale locale,
      final DateTime when)
  {
    final PatientDataForTherapyReportDto patientData =
        medicationsConnector.getPatientDataForTherapyReport(patientId, mainDiseaseTypeOnly, when, locale);
    if (patientData != null)
    {
      final Double referenceWeight =
          medicationsOpenEhrDao.getPatientLastReferenceWeight(patientId, Intervals.infiniteTo(when));
      patientData.setWeight(referenceWeight != null ? (referenceWeight + " " + "kg") : null);
    }

    return patientData;
  }

  private Map<String, Long> getTherapyIdWithMedicationIdsMap(final List<TherapyRowDto> therapyRowDtoList)
  {
    final Map<String, Long> therapyIdWithMedicationIdMap = new HashMap<>();

    for (final TherapyRowDto therapyRowDto : therapyRowDtoList)
    {
      final Long mainMedicationId = therapyRowDto.getTherapy().getMainMedicationId();
      if (mainMedicationId != null)
      {
        therapyIdWithMedicationIdMap.put(therapyRowDto.getTherapyId(), mainMedicationId);
      }
    }

    return therapyIdWithMedicationIdMap;
  }

  private TherapyDayElementReportDto createTherapyDayElementReportDto(
      final String patientId,
      final TherapyDto therapy,
      final int therapyConsecutiveDay,
      final boolean showTherapyConsecutiveDay,
      final TherapyStatusEnum therapyStatus,
      final Map<Long, Pair<String, Integer>> customGroupNameSortOrderMap,
      final Long mainMedicationId,
      final DateTime when,
      final Locale locale,
      final String currentRate,
      final String currentOxygenDevice)
  {
    final TherapyDayElementReportDto reportElement = new TherapyDayElementReportDto();
    reportElement.setTherapyConsecutiveDay(String.valueOf(therapyConsecutiveDay));
    reportElement.setShowTherapyConsecutiveDay(showTherapyConsecutiveDay);
    reportElement.setOrder(therapy);
    reportElement.setTherapyStart(mapTherapyStart(therapy, locale));
    reportElement.setCurrentRate(currentRate);
    reportElement.setCurrentOxygenDevice(currentOxygenDevice);
    reportElement.setHourDoseTime(extractHourDoseTimes(therapy, locale));
    reportElement.setDateDoseTime(extractDateDoseTime(therapy, when, locale));
    reportElement.setConsecutiveDayLabel(mapConsecutiveDays());

    final DateTime therapyEnd = therapy.getEnd();

    reportElement.setTherapyEnd(mapTherapyEnd(therapyEnd, locale));

    reportElement.setTherapyReportStatusEnum(mapTherapyReportStatus(therapyEnd, when, therapyStatus));

    final Long medicationId = mainMedicationId == null ? therapy.getMainMedicationId() : mainMedicationId;
    if (medicationId != null)
    {
      final Pair<String, Integer> customGroupNameSortOrderPair = customGroupNameSortOrderMap.get(medicationId);

      if (customGroupNameSortOrderPair != null)
      {
        reportElement.setCustomGroupName(customGroupNameSortOrderPair.getFirst());
        reportElement.setCustomGroupSortOrder(
            customGroupNameSortOrderPair.getSecond() != null ? customGroupNameSortOrderPair.getSecond() : Integer.MAX_VALUE);
      }
      else
      {
        reportElement.setCustomGroupSortOrder(Integer.MAX_VALUE);
      }
    }
    else
    {
      reportElement.setCustomGroupSortOrder(Integer.MAX_VALUE);
    }

    reportElement.setLastInrResult(extractLastTitrationResult(patientId, therapy, when, locale));
    return reportElement;
  }

  private String extractLastTitrationResult(
      final String patientId,
      final TherapyDto therapy,
      final DateTime when,
      final Locale locale)
  {
    if (therapy instanceof ConstantTherapy)
    {
      final ConstantTherapy constantTherapy = (ConstantTherapy)therapy;
      if (constantTherapy.getTitration() != null)
      {
        final List<QuantityWithTimeDto> titrationResults = titrationDataProvider.getObservationResults(
            patientId,
            ((ConstantTherapy)therapy).getTitration(),
            Intervals.infiniteFrom(when.minusDays(7)));

        return titrationResults.stream()
            .max(Comparator.comparing(QuantityWithTimeDto::getTime))
            .map(r -> titrationResultToString(r, constantTherapy.getTitration(), locale))
            .orElse(null);
      }
    }
    return null;
  }

  private String titrationResultToString(
      final QuantityWithTimeDto result,
      final TitrationType titration,
      final Locale locale)
  {
    final String label = Dictionary.getEntry("last", locale) + " " + Dictionary.getEntry(titration, locale);
    final String value = therapyDisplayProvider.decimalToString(result.getQuantity(), locale) +
        " (" + DateTimeFormatters.shortDateTime(locale).print(result.getTime()) + ")";
    return label + ": " + value;
  }

  private String mapConsecutiveDays()
  {
    return medsProperties.getAntimicrobialDaysCountStartsWithOne() ?
           Dictionary.getEntry("day.of.antimicrobials") :
           Dictionary.getEntry("completed.days.of.antimicrobials");
  }

  private String mapTherapyEnd(final DateTime therapyEnd, final Locale locale)
  {
    if (therapyEnd != null)
    {
      return DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(therapyEnd);
    }
    return null;
  }

  private String mapTherapyStart(final TherapyDto therapyDto, final Locale locale)
  {
    return DateTimeFormatters.shortDateTime(locale).withLocale(locale).print(therapyDto.getStart());
  }

  private TherapyReportStatusEnum mapTherapyReportStatus(
      final DateTime therapyEnd,
      final DateTime when,
      final TherapyStatusEnum therapyStatus)
  {
    final boolean therapyExpired = therapyEnd != null && therapyEnd.isBefore(when);
    if (therapyExpired || therapyStatus == TherapyStatusEnum.CANCELLED || therapyStatus == TherapyStatusEnum.ABORTED)
    {
      return TherapyReportStatusEnum.FINISHED;
    }
    else if (therapyStatus == TherapyStatusEnum.SUSPENDED)
    {
      return TherapyReportStatusEnum.SUSPENDED;
    }
    else
    {
      return TherapyReportStatusEnum.ACTIVE;
    }
  }

  private Map<DateTime, List<TherapyReportHourDoseTimeDto>> extractDateDoseTime(
      final TherapyDto therapyDto,
      final DateTime when, final Locale locale)
  {
    if (therapyDto instanceof VariableSimpleTherapyDto)
    {
      final VariableSimpleTherapyDto variableSimpleTherapyDto = (VariableSimpleTherapyDto)therapyDto;
      final long dateCount = variableSimpleTherapyDto.getTimedDoseElements().stream()
          .filter(tde -> tde.getDate() != null)
          .count();

      if (dateCount > 0)
      {
        final List<DateTime> dates = variableSimpleTherapyDto.getTimedDoseElements().stream()
            .map(TimedSimpleDoseElementDto::getDate)
            .distinct()
            .collect(Collectors.toList());

        final Map<DateTime, List<TherapyReportHourDoseTimeDto>> dateTimeDoses = new HashMap<>();

        dates.forEach(
            date -> dateTimeDoses.put(
                date,
                variableSimpleTherapyDto.getTimedDoseElements().stream()
                    .filter(tde -> tde.getDate().equals(date))
                    .filter(tde -> tde.getQuantityDisplay() != null)
                    .map(tde -> buildTherapyReportHourDoseTimeDto(tde.getDoseTime(), tde.getQuantityDisplay(), null, locale))
                    .collect(Collectors.toList()))
        );

        return dateTimeDoses;
      }
    }

    if (checkIfElementIsTitration(therapyDto))
    {
      final Map<DateTime, List<TherapyReportHourDoseTimeDto>> dateTimeDoses = new HashMap<>();
      DateTime iteratingDate = when.withTimeAtStartOfDay();
      while (!when.withTimeAtStartOfDay().plusDays(4).equals(iteratingDate))
      {
        final ConstantTherapy constantTherapy = (ConstantTherapy)therapyDto;

        final List<TherapyReportHourDoseTimeDto> timeDoses = addEmptyElements(
            constantTherapy.getDoseTimes().stream()
                .map(dt -> buildTherapyReportHourDoseTimeDto(dt, null, constantTherapy.getTitration(), locale))
                .collect(Collectors.toList()),
            3);

        dateTimeDoses.put(iteratingDate, timeDoses);
        iteratingDate = iteratingDate.plusDays(1);
      }
      return dateTimeDoses;
    }
    return null;
  }

  private List<TherapyReportHourDoseTimeDto> extractHourDoseTimes(final TherapyDto therapyDto, final Locale locale)
  {
    if (therapyDto instanceof ConstantSimpleTherapyDto)
    {
      return mapConstantSimpleTherapyTimeDoses(therapyDto, locale);
    }

    if (therapyDto instanceof VariableSimpleTherapyDto)
    {
      final VariableSimpleTherapyDto variableSimpleTherapy = (VariableSimpleTherapyDto)therapyDto;

      if (variableSimpleTherapy.getTimedDoseElements().stream()
          .filter(tde -> tde.getDate() != null)
          .collect(Collectors.toList())
          .isEmpty())
      {
        return mapVariableSimpleTherapyTimeDoses(variableSimpleTherapy, locale);
      }
    }

    if (therapyDto instanceof ConstantComplexTherapyDto)
    {
      return mapConstantComplexTherapyTimeDoses(therapyDto, locale);
    }

    if (therapyDto instanceof VariableComplexTherapyDto)
    {
      return mapVariableComplexTherapyTimeDoses(therapyDto, locale);
    }

    if (therapyDto instanceof OxygenTherapyDto)
    {
      return mapOxygenTherapyTimeDoses(therapyDto);
    }
    return null;
  }

  private List<TherapyReportHourDoseTimeDto> mapVariableSimpleTherapyTimeDoses(
      final VariableSimpleTherapyDto variableSimpleTherapyDto,
      final Locale locale)
  {
    final List<TimedSimpleDoseElementDto> doseElements = variableSimpleTherapyDto.getTimedDoseElements();
    return addEmptyElements(
        doseElements.stream()
            .map(de -> buildTherapyReportHourDoseTimeDto(de.getDoseTime(), de.getQuantityDisplay(), null, locale))
            .collect(Collectors.toList()),
        3);
  }

  private List<TherapyReportHourDoseTimeDto> mapConstantSimpleTherapyTimeDoses(
      final TherapyDto therapyDto,
      final Locale locale)
  {
    final ConstantSimpleTherapyDto constantSimpleTherapy = (ConstantSimpleTherapyDto)therapyDto;
    final SimpleDoseElementDto doseElement = constantSimpleTherapy.getDoseElement();

    final String doseDisplay = doseElement.getDoseDescription() != null || doseElement.getDoseRange() != null
                               ? null
                               : constantSimpleTherapy.getQuantityDisplay();

    return mapConstantTherapyTimeDoses(constantSimpleTherapy, doseDisplay, locale);
  }

  List<TherapyReportHourDoseTimeDto> mapConstantComplexTherapyTimeDoses(
      final TherapyDto therapyDto,
      final Locale locale)
  {
    final ConstantComplexTherapyDto constantComplexTherapy = (ConstantComplexTherapyDto)therapyDto;

    final String doseDisplay;
    if (constantComplexTherapy.getSpeedDisplay() != null)
    {
      if (constantComplexTherapy.isContinuousInfusion())
      {
        //time is never used, but must exist
        final TherapyReportHourDoseTimeDto dto = new TherapyReportHourDoseTimeDto(new HourMinuteDto(0, 0), "");
        return Lists.newArrayList(dto, dto, dto);
      }
      doseDisplay = constantComplexTherapy.getSpeedDisplay();
    }
    else
    {
      if (constantComplexTherapy.getVolumeSumDisplay() != null)
      {
        doseDisplay = constantComplexTherapy.getVolumeSumDisplay();
      }

      else if (constantComplexTherapy.getIngredientsList().size() == 1)
      {
        doseDisplay = constantComplexTherapy.getIngredientsList().get(0).getQuantityDisplay();
      }
      else
      {
        doseDisplay = null;
      }
    }
    return mapConstantTherapyTimeDoses(constantComplexTherapy, doseDisplay, locale);
  }

  private List<TherapyReportHourDoseTimeDto> mapConstantTherapyTimeDoses(
      final ConstantTherapy constantTherapy,
      final String doseDisplay,
      final Locale locale)
  {
    final DosingFrequencyDto dosingFrequency = constantTherapy.getDosingFrequency();
    if (dosingFrequency != null && dosingFrequency.getType() == DosingFrequencyTypeEnum.BETWEEN_DOSES)
    {
      return mapTimeDosesForBetweenDosesFrequency(constantTherapy, doseDisplay, dosingFrequency, locale);
    }
    return addEmptyElements(
        constantTherapy.getDoseTimes().stream()
            .map(t -> buildTherapyReportHourDoseTimeDto(t, doseDisplay, constantTherapy.getTitration(), locale))
            .collect(Collectors.toList()),
        3);
  }

  private List<TherapyReportHourDoseTimeDto> mapTimeDosesForBetweenDosesFrequency(
      final ConstantTherapy constantTherapy,
      final String doseDisplay,
      final DosingFrequencyDto dosingFrequency,
      final Locale locale)
  {
    final Double betweenDoses = dosingFrequency.getValue();
    final int numberOfDosesPerDay = (int)(24 / betweenDoses);
    if (24 % betweenDoses == 0)
    {
      final HourMinuteDto firstDoseTime = constantTherapy.getDoseTimes().get(0);

      final List<HourMinuteDto> doseTimes = new ArrayList<>();

      final int firstDoseMinute = firstDoseTime.getHour() * 60 + firstDoseTime.getMinute();
      for (int minute = firstDoseMinute; minute < firstDoseMinute + 24 * 60; minute += betweenDoses * 60)
      {
        doseTimes.add(new HourMinuteDto((minute / 60) % 24, minute % 60));
      }

      return addEmptyElements(
          doseTimes.stream()
              .sorted(Comparator.comparing(d -> d.getHour() * 60 + d.getMinute()))
              .map(t -> buildTherapyReportHourDoseTimeDto(t, doseDisplay, constantTherapy.getTitration(), locale))
              .collect(Collectors.toList()),
          3);
    }
    return addEmptyElements(Lists.newArrayList(), Math.max(numberOfDosesPerDay, 3));
  }

  private List<TherapyReportHourDoseTimeDto> mapVariableComplexTherapyTimeDoses(
      final TherapyDto therapyDto,
      final Locale locale)
  {
    final VariableComplexTherapyDto variableComplexTherapy = (VariableComplexTherapyDto)therapyDto;
    return addEmptyElements(
        variableComplexTherapy.getTimedDoseElements().stream()
            .map(cde -> buildTherapyReportHourDoseTimeDto(cde.getDoseTime(), cde.getSpeedDisplay(), null, locale))
            .collect(Collectors.toList()),
        3);
  }

  private List<TherapyReportHourDoseTimeDto> mapOxygenTherapyTimeDoses(final TherapyDto therapyDto)
  {
    //time is never used, but must exist
    final TherapyReportHourDoseTimeDto dto = new TherapyReportHourDoseTimeDto(new HourMinuteDto(0, 0), "");
    return Arrays.asList(dto, dto, dto, dto, dto, dto);
  }

  private List<TherapyReportHourDoseTimeDto> addEmptyElements(
      final List<TherapyReportHourDoseTimeDto> inputList,
      final int minimumNumberOfElements)
  {
    final List<TherapyReportHourDoseTimeDto> outputList = new ArrayList<>(inputList);
    while (outputList.size() < minimumNumberOfElements)
    {
      outputList.add(new TherapyReportHourDoseTimeDto(null, ""));
    }
    return outputList;
  }

  private TherapyReportHourDoseTimeDto buildTherapyReportHourDoseTimeDto(
      final HourMinuteDto hourMinute,
      final String dose,
      final TitrationType titrationType,
      final Locale locale)
  {
    final StringBuilder sb = new StringBuilder();
    if (dose != null)
    {
      sb.append(dose);
      sb.append("\n");
    }
    sb.append(hourMinute.prettyPrint());

    if (titrationType != null)
    {
      sb.append("\n");
      sb.append("\n");
      sb.append(Dictionary.getEntry("TitrationType." + titrationType.name() + ".short", locale));
      sb.append(":___");
    }
    return new TherapyReportHourDoseTimeDto(hourMinute, sb.toString());
  }
}
