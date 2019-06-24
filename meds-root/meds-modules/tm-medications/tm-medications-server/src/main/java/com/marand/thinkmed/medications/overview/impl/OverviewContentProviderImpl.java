package com.marand.thinkmed.medications.overview.impl;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.CollectionUtils;
import com.marand.maf.core.Opt;
import com.marand.maf.core.daterule.service.MafDateRuleService;
import com.marand.maf.core.openehr.util.DvUtils;
import com.marand.maf.core.time.DayType;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.PrescriptionGroupEnum;
import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.administration.AdministrationTaskConverter;
import com.marand.thinkmed.medications.administration.AdministrationUtils;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationDataForTherapyDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyChangeReasonDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationStatusEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.overview.ContinuousInfusionTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.OxygenTherapyRowDtoDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.TherapyPharmacistReviewStatusEnum;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.dto.task.TherapyTaskSimpleDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningSimpleDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsType;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.pharmacist.PharmacyReviewReport;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.MedicationsEhrUtils;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.overview.OverviewContentProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacistUtils;
import com.marand.thinkmed.medications.preferences.MedicationPreferencesUtil;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import com.marand.thinkmed.medications.warnings.additional.AdditionalWarningsDelegator;
import com.marand.thinkmed.medications.witnessing.WitnessingHandler;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.openehr.jaxb.rm.DvText;
import org.openehr.jaxb.rm.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */
@Component
public class OverviewContentProviderImpl implements OverviewContentProvider
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private AdministrationTaskConverter administrationTaskConverter;
  private MedicationsTasksProvider medicationsTasksProvider;
  private AdministrationHandler administrationHandler;
  private MedicationsBo medicationsBo;
  private AdministrationUtils administrationUtils;
  private MafDateRuleService mafDateRuleService;
  private AdditionalWarningsDelegator additionalWarningsDelegator;
  private TherapyEhrHandler therapyEhrHandler;
  private WitnessingHandler witnessingHandler;
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  private MedsProperties medsProperties;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setAdministrationTaskConverter(final AdministrationTaskConverter administrationTaskConverter)
  {
    this.administrationTaskConverter = administrationTaskConverter;
  }

  @Autowired
  public void setMedicationsTasksProvider(final MedicationsTasksProvider medicationsTasksProvider)
  {
    this.medicationsTasksProvider = medicationsTasksProvider;
  }

  @Autowired
  public void setAdministrationHandler(final AdministrationHandler administrationHandler)
  {
    this.administrationHandler = administrationHandler;
  }

  @Autowired
  public void setAdministrationUtils(final AdministrationUtils administrationUtils)
  {
    this.administrationUtils = administrationUtils;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setMafDateRuleService(final MafDateRuleService mafDateRuleService)
  {
    this.mafDateRuleService = mafDateRuleService;
  }

  @Autowired
  public void setAdditionalWarningsDelegator(final AdditionalWarningsDelegator additionalWarningsDelegator)
  {
    this.additionalWarningsDelegator = additionalWarningsDelegator;
  }

  @Autowired
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  @Autowired
  public void setWitnessingHandler(final WitnessingHandler witnessingHandler)
  {
    this.witnessingHandler = witnessingHandler;
  }

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProvider medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Autowired
  public void setMedsProperties(final MedsProperties medsProperties)
  {
    this.medsProperties = medsProperties;
  }

  @Override
  public TherapyFlowDto getTherapyFlow(
      final String patientId,
      final String centralCaseId,
      final Double patientHeight,
      final DateTime startDate,
      final int dayCount,
      final Integer todayIndex,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      @Nullable final String careProviderId,
      final DateTime currentTime,
      final Locale locale)
  {
    final TherapyFlowDto therapyFlow = new TherapyFlowDto();

    final DateTime startDateAtMidnight = startDate.withTimeAtStartOfDay();
    DateTime searchEnd = startDateAtMidnight.plusDays(dayCount);

    //show therapies that start in the future
    if ((todayIndex != null && todayIndex == dayCount - 1) || startDate.plusDays(dayCount - 1).isAfter(currentTime))
    {
      searchEnd = Intervals.INFINITE.getEnd();
    }

    final Interval searchInterval = new Interval(startDateAtMidnight, searchEnd);
    final List<InpatientPrescription> inpatientPrescriptions =
        medicationsOpenEhrDao.findInpatientPrescriptions(patientId, searchInterval);

    final Map<Integer, Double> referenceWeightsByDay = new HashMap<>();
    for (int i = 0; i < dayCount; i++)
    {
      final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(
          patientId, Intervals.infiniteTo(startDateAtMidnight.plusDays(i).plusDays(1)));
      if (referenceWeight != null)
      {
        referenceWeightsByDay.put(i, referenceWeight);
      }
    }
    therapyFlow.setReferenceWeightsDayMap(referenceWeightsByDay);
    final List<TherapyFlowRowDto> therapyRows = buildTherapyFlow(
        patientId,
        centralCaseId,
        patientHeight,
        inpatientPrescriptions,
        startDateAtMidnight,
        dayCount,
        todayIndex,
        roundsInterval,
        therapySortTypeEnum,
        referenceWeightsByDay,
        careProviderId,
        currentTime,
        locale);

    therapyFlow.setTherapyRows(therapyRows);
    return therapyFlow;
  }

  private List<TherapyFlowRowDto> buildTherapyFlow(
      final String patientId,
      final String centralCaseId,
      final Double patientHeight,
      final List<InpatientPrescription> inpatientPrescriptions,
      final DateTime startDate,
      final int dayCount,
      final Integer todayIndex,
      final RoundsIntervalDto roundsInterval,
      final TherapySortTypeEnum therapySortTypeEnum,
      final Map<Integer, Double> referenceWeightsByDay,
      @Nullable final String careProviderId,
      final DateTime currentTime,
      final Locale locale)
  {
    if (inpatientPrescriptions.isEmpty())
    {
      return new ArrayList<>();
    }
    medicationsBo.sortTherapiesByMedicationTimingStart(inpatientPrescriptions, false);

    final Map<InpatientPrescription, String> linksMap = buildLinksMap(inpatientPrescriptions);

    final Map<String, String> therapyIdOriginalTherapyIdMap = new HashMap<>(); //therapy id, original therapy id

    final Map<TherapyFlowRowDto, List<InpatientPrescription>> therapyRowsMap = new LinkedHashMap<>();

    final Map<Long, MedicationDataForTherapyDto> medicationsMap =
        medicationsBo.getMedicationDataForInpatientPrescriptions(inpatientPrescriptions, careProviderId);

    for (final InpatientPrescription inpatientPrescription : inpatientPrescriptions)
    {
      final MedicationOrder medicationOrder = inpatientPrescription.getMedicationOrder();

      final String originalTherapyId = PrescriptionsEhrUtils.getOriginalTherapyId(inpatientPrescription);
      final String therapyId = TherapyIdUtils.createTherapyId(inpatientPrescription.getUid());
      therapyIdOriginalTherapyIdMap.put(therapyId, originalTherapyId);

      TherapyFlowRowDto therapyRowDto = findModifiedInpatientPrescriptionForFlow(inpatientPrescription, therapyRowsMap);
      if (therapyRowDto == null)
      {
        therapyRowDto = new TherapyFlowRowDto();

        final Long mainMedicationId = MedicationsEhrUtils.getMainMedicationId(medicationOrder);
        final MedicationDataForTherapyDto mainMedicationData = medicationsMap.get(mainMedicationId);
        if (mainMedicationData != null)
        {
          therapyRowDto.setCustomGroup(mainMedicationData.getCustomGroupName());
          therapyRowDto.setCustomGroupSortOrder(mainMedicationData.getCustomGroupSortOrder());
          therapyRowDto.setAtcGroupCode(mainMedicationData.getAtcGroupCode());
          therapyRowDto.setAtcGroupName(mainMedicationData.getAtcGroupName());
        }
      }

      if (!therapyRowsMap.containsKey(therapyRowDto))
      {
        therapyRowsMap.put(therapyRowDto, new ArrayList<>());
      }

      therapyRowsMap.get(therapyRowDto).add(inpatientPrescription);

      if (CollectionUtils.isEmpty(therapyRowDto.getRoutes()))
      {
        therapyRowDto.setRoutes(
            medicationOrder.getRoute()
                .stream()
                .map(DvText::getValue)
                .collect(Collectors.toList()));
      }

      final Interval medicationOrderInterval = MedicationsEhrUtils.getMedicationOrderInterval(medicationOrder);

      for (int i = 0; i < dayCount; i++)
      {
        Interval therapyDay = Intervals.wholeDay(startDate.plusDays(i));
        //if today or future, show therapies that start in the future
        final boolean isToday = todayIndex != null && todayIndex == i;
        if (isToday || startDate.plusDays(i).isAfter(currentTime))
        {
          therapyDay = Intervals.infiniteFrom(therapyDay.getStart());
        }

        if (medicationOrderInterval.overlap(therapyDay) != null || medicationOrderInterval.getStart().equals(therapyDay.getStart()))
        {
          final Double referenceWeight = referenceWeightsByDay.get(i);
          final TherapyDto therapy =
              medicationsBo.convertMedicationOrderToTherapyDto(
                  inpatientPrescription,
                  inpatientPrescription.getMedicationOrder(),
                  referenceWeight,
                  patientHeight,
                  isToday,
                  locale);

          therapy.setAddToDischargeLetter(isMarkedToAddToDischargeLetter(centralCaseId, inpatientPrescription));

          final TherapyDayDto dayDto = new TherapyDayDto();
          fillTherapyDayState(
              dayDto,
              patientId,
              careProviderId,
              therapy,
              inpatientPrescription,
              roundsInterval,
              medicationsMap,
              therapyDay,
              currentTime);

          if (isToday)
          {
            therapy.setLinkName(linksMap.get(inpatientPrescription));
          }
          therapyRowDto.setPrescriptionGroup(getPrescriptionGroup(therapy));
          therapyRowDto.getTherapyFlowDayMap().put(i, dayDto);
        }
      }
    }

    final List<TherapyFlowRowDto> therapiesList = new ArrayList<>(therapyRowsMap.keySet());
    sortTherapyFlowRows(therapiesList, therapySortTypeEnum);

    if (todayIndex != null && todayIndex > -1)
    {
      final List<TherapyDayDto> todayTherapies = new ArrayList<>();
      for (final TherapyFlowRowDto therapyFlowRowDto : therapiesList)
      {
        if (therapyFlowRowDto.getTherapyFlowDayMap().containsKey(todayIndex))
        {
          final TherapyDayDto therapyDayDto = therapyFlowRowDto.getTherapyFlowDayMap().get(todayIndex);
          todayTherapies.add(therapyDayDto);
        }
      }

      fillPharmacyReviewDate(patientId, todayTherapies);
      fillReminderTaskData(patientId, todayTherapies, therapyIdOriginalTherapyIdMap, currentTime);
    }

    return therapiesList;
  }

  private boolean isMarkedToAddToDischargeLetter(
      final String centralCaseId,
      final InpatientPrescription inpatientPrescription)
  {
    if (StringUtils.isNotBlank(centralCaseId))
    {
      return false;
    }

    return Opt.resolve(
        () -> inpatientPrescription.getMedicationOrder()
            .getAdditionalDetails()
            .getAddToDischargeLetter()
            .getValue()
            .equals(centralCaseId)).orElse(false);
  }

  private void fillPharmacyReviewDate(final String patientId, final List<? extends TherapyDayDto> therapiesList)
  {
    if (!therapiesList.isEmpty())
    {
      DateTime firstTherapyCreateTimestamp = null;
      for (final TherapyDayDto therapy : therapiesList)
      {
        if (firstTherapyCreateTimestamp == null ||
            therapy.getTherapy().getCreatedTimestamp().isBefore(firstTherapyCreateTimestamp))
        {
          firstTherapyCreateTimestamp = therapy.getTherapy().getCreatedTimestamp();
        }
      }

      final List<PharmacyReviewReport> reviewReports = medicationsOpenEhrDao.findPharmacistsReviewReports(
          patientId,
          firstTherapyCreateTimestamp);

      final Set<String> referredBackTherapiesCompositionUids = PharmacistUtils.extractReferredBackTherapiesCompositionUids(reviewReports);
      final DateTime lastReviewTime = PharmacistUtils.extractLastReviewTime(reviewReports);

      for (final TherapyDayDto therapyDayDto : therapiesList)
      {
        if (therapyDayDto != null)
        {
          if (referredBackTherapiesCompositionUids.contains(TherapyIdUtils.getCompositionUidWithoutVersion(therapyDayDto.getTherapy().getCompositionUid())))
          {
            therapyDayDto.setTherapyPharmacistReviewStatus(TherapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK);
          }
          else if (therapyDayDto.isBasedOnPharmacyReview())
          {
            therapyDayDto.setTherapyPharmacistReviewStatus(TherapyPharmacistReviewStatusEnum.REVIEWED);
          }
          else if (lastReviewTime == null || therapyDayDto.getLastModifiedTimestamp().isAfter(lastReviewTime))
          {
            therapyDayDto.setTherapyPharmacistReviewStatus(TherapyPharmacistReviewStatusEnum.NOT_REVIEWED);
          }
          else
          {
            therapyDayDto.setTherapyPharmacistReviewStatus(TherapyPharmacistReviewStatusEnum.REVIEWED);
          }
        }
      }
    }
  }

  private void fillReminderTaskData(
      final String patientId,
      final List<? extends TherapyDayDto> therapiesList,
      final Map<String, String> therapyIdOriginalTherapyIdMap,
      final DateTime when) //therapy id, original therapy id
  {
    final Map<String, List<TherapyTaskSimpleDto>> tasksMap =
        medicationsTasksProvider.findSimpleTasksForTherapies(patientId, therapyIdOriginalTherapyIdMap.values(), when);

    for (final TherapyDayDto therapyDayDto : therapiesList)
    {
      final String therapyId =
          TherapyIdUtils.createTherapyId(
              therapyDayDto.getTherapy().getCompositionUid(), therapyDayDto.getTherapy().getEhrOrderName());
      final String originalTherapyId =
          therapyIdOriginalTherapyIdMap.containsKey(therapyId) ? therapyIdOriginalTherapyIdMap.get(therapyId) : therapyId;
      final List<TherapyTaskSimpleDto> tasksList = tasksMap.get(originalTherapyId);
      if (tasksList != null)
      {
        therapyDayDto.getTasks().addAll(tasksList);
      }
    }
  }

  private Map<InpatientPrescription, String> buildLinksMap(final List<InpatientPrescription> inpatientPrescriptions)
  {
    final Map<InpatientPrescription, String> linksMap = new HashMap<>();

    for (final InpatientPrescription inpatientPrescription : inpatientPrescriptions)
    {
      final List<Link> followLinks = LinksEhrUtils.getLinksOfType(inpatientPrescription.getLinks(), EhrLinkType.FOLLOW);
      if (!followLinks.isEmpty())
      {
        final Link link = followLinks.get(0);
        final String linkName = DvUtils.getString(link.getMeaning());
        linksMap.put(inpatientPrescription, linkName);

        if (linkName.length() == 2 && linkName.endsWith("2"))
        {
          for (final InpatientPrescription compareInpatientPrescription : inpatientPrescriptions)
          {
            final boolean linkExists = inpatientPrescription.getLinks()
                .stream()
                .anyMatch(l -> LinksEhrUtils.isLinkToComposition(
                    compareInpatientPrescription.getUid(),
                    l,
                    EhrLinkType.FOLLOW));

            if (linkExists)
            {
              final String previousLinkName = LinksEhrUtils.getPreviousLinkName(linkName);
              linksMap.put(compareInpatientPrescription, previousLinkName);
              break;
            }
          }
        }
      }
    }
    return linksMap;
  }

  private void sortTherapyFlowRows(
      final List<TherapyFlowRowDto> therapyRows,
      final TherapySortTypeEnum therapySortTypeEnum)
  {
    final Map<String, TherapyDayDto> firstTherapyWithLinkPrefixMap = new HashMap<>();
    for (final TherapyFlowRowDto therapyFlowRow : therapyRows)
    {
      final TherapyDayDto therapyRow = therapyFlowRow.getTherapyFlowDayMap().values().iterator().next();
      if (therapyRow.getTherapy().getLinkName() != null)
      {
        final String linkPrefix = therapyRow.getTherapy().getLinkName().substring(0, 1);
        if (firstTherapyWithLinkPrefixMap.get(linkPrefix) != null)
        {
          final int compare = sortByLinkName(
              therapyRow, firstTherapyWithLinkPrefixMap.get(linkPrefix));
          if (compare < 0)
          {
            firstTherapyWithLinkPrefixMap.put(linkPrefix, therapyFlowRow.getTherapyFlowDayMap().values().iterator().next());
          }
        }
        else
        {
          firstTherapyWithLinkPrefixMap.put(linkPrefix, therapyFlowRow.getTherapyFlowDayMap().values().iterator().next());
        }
      }
    }
    Collections.sort(
        therapyRows, (firstTherapyFlowRow, secondTherapyFlowRow) -> {
          final TherapyDayDto firstTherapyDay = firstTherapyFlowRow.getTherapyFlowDayMap().values().iterator().next();
          final TherapyDayDto secondTherapyDay = secondTherapyFlowRow.getTherapyFlowDayMap().values().iterator().next();
          return compareTherapies(firstTherapyDay, secondTherapyDay, therapySortTypeEnum, firstTherapyWithLinkPrefixMap);
        }
    );
  }

  public void sortTherapyRowsAndAdministrations(
      final List<TherapyRowDto> timelineRows,
      final TherapySortTypeEnum therapySortTypeEnum)
  {
    final Map<String, TherapyDayDto> firstTherapyWithLinkPrefixMap = new HashMap<>();
    for (final TherapyRowDto timelineRow : timelineRows)
    {
      timelineRow.getAdministrations().sort(Comparator.comparing(administrationUtils::getAdministrationTime));

      if (timelineRow.getTherapy().getLinkName() != null)
      {
        final String linkPrefix = timelineRow.getTherapy().getLinkName().substring(0, 1);
        if (firstTherapyWithLinkPrefixMap.get(linkPrefix) != null)
        {
          final int compare = sortByLinkName(timelineRow, firstTherapyWithLinkPrefixMap.get(linkPrefix));
          if (compare < 0)
          {
            firstTherapyWithLinkPrefixMap.put(linkPrefix, timelineRow);
          }
        }
        else
        {
          firstTherapyWithLinkPrefixMap.put(linkPrefix, timelineRow);
        }
      }
    }

    Collections.sort(
        timelineRows, (firstTherapyTimelineRow, secondTherapyTimelineRow) -> compareTherapies(
            firstTherapyTimelineRow,
            secondTherapyTimelineRow,
            therapySortTypeEnum,
            firstTherapyWithLinkPrefixMap)
    );
  }

  public int compareTherapies(
      final TherapyDayDto firstTherapyDayRow,
      final TherapyDayDto secondTherapyDayRow,
      final TherapySortTypeEnum therapySortTypeEnum,
      final Map<String, TherapyDayDto> firstTherapyWithLinkPrefixMap)
  {
    TherapyDayDto firstTherapyDay = firstTherapyDayRow;
    TherapyDayDto secondTherapyDay = secondTherapyDayRow;
    final String firstTherapyLinkPrefix = firstTherapyDay.getTherapy().getLinkName() != null ?
                                          firstTherapyDay.getTherapy().getLinkName().substring(0, 1) : null;
    final String secondTherapyLinkPrefix = secondTherapyDay.getTherapy().getLinkName() != null ?
                                           secondTherapyDay.getTherapy().getLinkName().substring(0, 1) : null;
    if (firstTherapyLinkPrefix != null && secondTherapyLinkPrefix != null && firstTherapyLinkPrefix.equals(
        secondTherapyLinkPrefix))
    {
      return sortByLinkName(firstTherapyDay, secondTherapyDay);
    }
    if (firstTherapyLinkPrefix != null && firstTherapyWithLinkPrefixMap.keySet().contains(firstTherapyLinkPrefix))
    {
      firstTherapyDay = firstTherapyWithLinkPrefixMap.get(firstTherapyLinkPrefix);
    }
    if (secondTherapyLinkPrefix != null && firstTherapyWithLinkPrefixMap.keySet().contains(secondTherapyLinkPrefix))
    {
      secondTherapyDay = firstTherapyWithLinkPrefixMap.get(secondTherapyLinkPrefix);
    }

    return sortTherapies(firstTherapyDay, secondTherapyDay, therapySortTypeEnum);
  }

  private int sortTherapies(
      final TherapyDayDto firstDayTherapy,
      final TherapyDayDto secondDayTherapy,
      final TherapySortTypeEnum therapySortTypeEnum)
  {
    final TherapyDto firstTherapy = firstDayTherapy.getTherapy();
    final TherapyDto secondTherapy = secondDayTherapy.getTherapy();
    final Collator collator = Collator.getInstance();

    if (therapySortTypeEnum == TherapySortTypeEnum.CREATED_TIME_ASC)
    {
      final int compareResult = firstTherapy.getCreatedTimestamp().compareTo(secondTherapy.getCreatedTimestamp());
      return compareResult == 0 ? collator.compare(
          firstTherapy.getTherapyDescription(),
          secondTherapy.getTherapyDescription()) : compareResult;
    }
    if (therapySortTypeEnum == TherapySortTypeEnum.CREATED_TIME_DESC)
    {
      final int compareResult = secondTherapy.getCreatedTimestamp().compareTo(firstTherapy.getCreatedTimestamp());
      return compareResult == 0 ? collator.compare(
          firstTherapy.getTherapyDescription(),
          secondTherapy.getTherapyDescription()) : compareResult;
    }
    if (therapySortTypeEnum == TherapySortTypeEnum.DESCRIPTION_ASC)
    {
      return medicationsBo.compareTherapiesForSort(firstTherapy, secondTherapy, collator);
    }
    if (therapySortTypeEnum == TherapySortTypeEnum.DESCRIPTION_DESC)
    {
      return medicationsBo.compareTherapiesForSort(secondTherapy, firstTherapy, collator);
    }
    else
    {
      throw new IllegalArgumentException("Unsupported therapy sort type!");
    }
  }

  private int sortByLinkName(
      final TherapyDayDto firstDayTherapy,
      final TherapyDayDto secondDayTherapy)
  {
    final TherapyDto firstTherapy = firstDayTherapy.getTherapy();
    final TherapyDto secondTherapy = secondDayTherapy.getTherapy();
    final Collator collator = Collator.getInstance();
    final String firstTherapyLinkName = firstDayTherapy.getTherapy().getLinkName();
    final String secondTherapyLinkName = secondDayTherapy.getTherapy().getLinkName();
    if (firstTherapyLinkName != null && secondTherapyLinkName != null)
    {
      final int sortByLink = firstTherapyLinkName.compareTo(secondTherapyLinkName);
      if (sortByLink == 0)
      {
        return medicationsBo.compareTherapiesForSort(firstTherapy, secondTherapy, collator);
      }
      return sortByLink;
    }
    else if (firstTherapyLinkName == null && secondTherapyLinkName == null)
    {
      return medicationsBo.compareTherapiesForSort(firstTherapy, secondTherapy, collator);
    }
    else if (firstTherapyLinkName != null)
    {
      return -1;
    }
    else
    {
      return 1;
    }
  }

  public List<TherapyRowDto> removeOldCompletedTherapies(final List<TherapyRowDto> timelineRows, final DateTime now)
  {
    final List<TherapyRowDto> activeTherapyRows = new ArrayList<>();
    for (final TherapyRowDto therapyRow : timelineRows)
    {
      final TherapyDto therapy = therapyRow.getTherapy();

      final boolean allRecentTasksCompleted = therapyRow.getAdministrations().stream()
          .filter(a -> AdministrationStatusEnum.PENDING.contains(a.getAdministrationStatus()))
          .noneMatch(a -> a.getPlannedTime() != null && a.getPlannedTime().isAfter(now.minusHours(8)));

      final boolean onlyOnceTherapy = therapy.getDosingFrequency() != null &&
          therapy.getDosingFrequency().getType() == DosingFrequencyTypeEnum.ONCE_THEN_EX;

      final Interval recentInterval = onlyOnceTherapy
                                      ? Intervals.infiniteFrom(now.minusHours(24))
                                      : Intervals.infiniteFrom(now.minusMinutes(medsProperties.getCompletedTherapiesShownMinutes()));

      final boolean recentTherapy = therapy.getEnd() == null || recentInterval.contains(therapy.getEnd());
      if (recentTherapy || !allRecentTasksCompleted)
      {
        activeTherapyRows.add(therapyRow);
      }
    }
    return activeTherapyRows;
  }

  private TherapyFlowRowDto findModifiedInpatientPrescriptionForFlow(
      final InpatientPrescription inpatientPrescription,
      final Map<TherapyFlowRowDto, List<InpatientPrescription>> therapyRowsMap)
  {
    for (final TherapyFlowRowDto therapyFlowRow : therapyRowsMap.keySet())
    {
      for (final InpatientPrescription compareInpatientPrescription :
          therapyRowsMap.get(therapyFlowRow))
      {
        final boolean therapiesLinkedByEdit = inpatientPrescription.getLinks()
            .stream()
            .anyMatch(l -> LinksEhrUtils.isLinkToComposition(
                compareInpatientPrescription.getUid(),
                l,
                EhrLinkType.UPDATE));

        if (therapiesLinkedByEdit)
        {
          return therapyFlowRow;
        }
      }
    }
    return null;
  }

  @Override
  public TherapyTimelineDto getTherapyTimeline(
      final @NonNull String patientId,
      final @NonNull List<AdministrationDto> administrations,
      final @NonNull List<AdministrationTaskDto> administrationTasks,
      final @NonNull List<InpatientPrescription> inpatientPrescription,
      final @NonNull TherapySortTypeEnum therapySortTypeEnum,
      final boolean hidePastTherapies,
      final @NonNull PatientDataForMedicationsDto patientData,
      final Interval interval,
      final RoundsIntervalDto roundsInterval,
      final Locale locale,
      final @NonNull DateTime when)
  {
    final Opt<AdditionalWarningsDto> additionalWarnings = additionalWarningsDelegator.getAdditionalWarnings(
        Arrays.asList(AdditionalWarningsType.values()),
        patientId,
        patientData,
        when,
        locale);

    //noinspection SimplifyStreamApiCallChains
    final List<TherapyAdditionalWarningDto> therapyAdditionalWarnings = additionalWarnings
        .map(a -> a.getWarnings().stream().collect(Collectors.toList()))
        .orElse(Collections.emptyList());

    final EnumSet<AdditionalWarningsType> additionalWarningTypes = EnumSet.noneOf(AdditionalWarningsType.class);
    therapyAdditionalWarnings
        .stream()
        .flatMap(therapyAdditionalWarning -> therapyAdditionalWarning.getWarnings().stream())
        .map(AdditionalWarningDto::getAdditionalWarningsType)
        .forEach(additionalWarningTypes::add);

    final List<TherapyRowDto> rows = buildTherapyRows(
        patientId,
        inpatientPrescription,
        administrations,
        administrationTasks,
        therapySortTypeEnum,
        hidePastTherapies,
        therapyAdditionalWarnings,
        patientData,
        interval,
        roundsInterval,
        locale,
        when);

    return new TherapyTimelineDto(rows, additionalWarnings.get(), additionalWarningTypes);
  }

  @Override
  public List<TherapyRowDto> buildTherapyRows(
      final @NonNull String patientId,
      final @NonNull List<InpatientPrescription> inpatientPrescriptions,
      final @NonNull List<AdministrationDto> administrations,
      final @NonNull List<AdministrationTaskDto> administrationTasks,
      final @NonNull TherapySortTypeEnum therapySortTypeEnum,
      final boolean hidePastTherapies,
      final @NonNull List<TherapyAdditionalWarningDto> therapyAdditionalWarnings,
      final PatientDataForMedicationsDto patientData,
      final Interval interval,
      final RoundsIntervalDto roundsInterval,
      final Locale locale,
      final @NonNull DateTime when)
  {
    final List<InpatientPrescription> sortedPrescriptions = new ArrayList<>(inpatientPrescriptions);
    medicationsBo.sortTherapiesByMedicationTimingStart(sortedPrescriptions, true);

    final Map<String, TherapyRowDto> therapyRowsMap = new HashMap<>();
    final Map<String, String> modifiedTherapiesMap = new HashMap<>(); //therapy id, latest modified therapy id
    final Map<String, String> therapyIdOriginalTherapyIdMap = new HashMap<>(); //therapy id, original therapy id

    final String centralCaseId = Opt.resolve(() -> patientData.getCentralCaseDto().getCentralCaseId()).orElse(null);

    fillTherapyRowsMaps(
        patientId,
        patientData,
        sortedPrescriptions,
        therapyRowsMap,
        modifiedTherapiesMap,
        therapyIdOriginalTherapyIdMap,
        roundsInterval,
        interval,
        centralCaseId,
        when,
        locale);

    if (!administrations.isEmpty())
    {
      administrations.sort(Comparator.comparing(administrationUtils::getAdministrationTime));
      administrationHandler.addAdministrationsToTimelines(administrations, therapyRowsMap, modifiedTherapiesMap, interval);
    }

    if (!administrationTasks.isEmpty())
    {
      addAdministrationTasksToTimeline(administrationTasks, therapyRowsMap, modifiedTherapiesMap, when);
    }

    List<TherapyRowDto> therapyRows = new ArrayList<>(therapyRowsMap.values());
    if (hidePastTherapies)
    {
      therapyRows = removeOldCompletedTherapies(therapyRows, when);
    }
    sortTherapyRowsAndAdministrations(therapyRows, therapySortTypeEnum);

    fillPharmacyReviewDate(patientId, therapyRows);
    fillReminderTaskData(patientId, therapyRows, therapyIdOriginalTherapyIdMap, when);

    therapyRows.forEach(r -> r.setAdditionalWarnings(getAdditionalWarningsForTherapy(
        r.getTherapyId(),
        therapyAdditionalWarnings)));

    return therapyRows;
  }

  List<AdditionalWarningSimpleDto> getAdditionalWarningsForTherapy(
      final String therapyId,
      final Collection<TherapyAdditionalWarningDto> therapyAdditionalWarnings)
  {
    return therapyAdditionalWarnings
        .stream()
        .filter(t -> t.getTherapy().getTherapyId().equals(therapyId))
        .flatMap(t -> t.getWarnings().stream())
        .map(a -> new AdditionalWarningSimpleDto(a.getWarning().getDescription(), a.getAdditionalWarningsType()))
        .collect(Collectors.toList());
  }

  private void addAdministrationTasksToTimeline(
      final List<AdministrationTaskDto> tasks,
      final Map<String, TherapyRowDto> therapyTimelineRowsMap,
      final Map<String, String> modifiedTherapiesMap,
      final DateTime when)
  {
    for (final AdministrationTaskDto task : tasks)
    {
      final String therapyId = task.getTherapyId();
      final String latestTherapyId = modifiedTherapiesMap.getOrDefault(therapyId, therapyId);
      final TherapyRowDto timelineRow = therapyTimelineRowsMap.get(latestTherapyId);
      if (timelineRow != null)
      {
        final AdministrationDto administration = administrationTaskConverter.buildAdministrationFromTask(task, when);
        administration.setTherapyId(task.getTherapyId());
        timelineRow.getAdministrations().add(administration);
      }
    }
  }

  private void fillTherapyRowsMaps(
      final String patientId,
      final PatientDataForMedicationsDto patientData,
      final List<InpatientPrescription> inpatientPrescriptions,
      final Map<String, TherapyRowDto> therapyTimelineRowsMap,
      final Map<String, String> modifiedTherapiesMap,
      final Map<String, String> therapyIdOriginalTherapyIdMap,
      final RoundsIntervalDto roundsInterval,
      @Nullable final Interval interval,
      final String centralCaseId,
      final DateTime when,
      final Locale locale)
  {
    final Map<InpatientPrescription, String> linksMap = buildLinksMap(inpatientPrescriptions);

    final Map<String, InpatientPrescription> processedInpatientPrescriptionsMap = new HashMap<>();
    final Map<Long, MedicationDataForTherapyDto> medicationsDataMap = medicationsBo.getMedicationDataForInpatientPrescriptions(
        inpatientPrescriptions,
        Opt.resolve(() -> patientData.getCentralCaseDto().getCareProvider().getId()).orElse(null));

    final Double referenceWeight = medicationsOpenEhrDao.getPatientLastReferenceWeight(
        patientId,
        Intervals.infiniteTo(when));

    for (final InpatientPrescription inpatientPrescription : inpatientPrescriptions)
    {
      final String originalTherapyId = PrescriptionsEhrUtils.getOriginalTherapyId(inpatientPrescription);
      final String therapyId = TherapyIdUtils.createTherapyId(inpatientPrescription.getUid());
      therapyIdOriginalTherapyIdMap.put(therapyId, originalTherapyId);

      final String latestModifiedTherapyId = findModifiedInpatientPrescriptionForTimeline(processedInpatientPrescriptionsMap, inpatientPrescription);
      if (latestModifiedTherapyId != null)
      {
        modifiedTherapiesMap.put(therapyId, latestModifiedTherapyId);
        processedInpatientPrescriptionsMap.put(latestModifiedTherapyId, inpatientPrescription);
      }
      else
      {
        final TherapyRowDto timelineRow =
            buildTherapyRowDto(
                patientData,
                referenceWeight,
                roundsInterval,
                interval,
                when,
                locale,
                medicationsDataMap,
                inpatientPrescription.getMedicationOrder(),
                inpatientPrescription,
                therapyId,
                patientId);


        timelineRow.getTherapy().setAddToDischargeLetter(isMarkedToAddToDischargeLetter(centralCaseId, inpatientPrescription));

        therapyTimelineRowsMap.put(therapyId, timelineRow);

        timelineRow.getTherapy().setLinkName(linksMap.get(inpatientPrescription));

        processedInpatientPrescriptionsMap.put(therapyId, inpatientPrescription);
      }
    }
  }

  private TherapyRowDto buildTherapyRowDto(
      final PatientDataForMedicationsDto patientData,
      final Double referenceWeight,
      final RoundsIntervalDto roundsInterval,
      @Nullable final Interval interval,
      final DateTime when,
      final Locale locale,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final MedicationOrder medicationOrder,
      final InpatientPrescription inpatientPrescription,
      final String therapyId,
      final String patientId)
  {
    //noinspection Convert2MethodRef
    final Double patientHeight = Opt.resolve(() -> patientData.getHeightInCm()).orElse(null);

    final TherapyDto therapy = medicationsBo.convertMedicationOrderToTherapyDto(
        inpatientPrescription,
        inpatientPrescription.getMedicationOrder(),
        referenceWeight,
        patientHeight,
        true,
        locale);

    final TherapyRowDto timelineRow = createEmptyTherapyRow(medicationOrder);
    timelineRow.setTherapyId(therapyId);


    final Long mainMedicationId = therapy.getMainMedicationId();
    final MedicationDataForTherapyDto mainMedicationData = medicationsDataMap.get(mainMedicationId);
    if (mainMedicationData != null)
    {
      timelineRow.setCustomGroup(mainMedicationData.getCustomGroupName());
      timelineRow.setCustomGroupSortOrder(mainMedicationData.getCustomGroupSortOrder());
      timelineRow.setAtcGroupCode(mainMedicationData.getAtcGroupCode());
      timelineRow.setAtcGroupName(mainMedicationData.getAtcGroupName());
    }

    timelineRow.setPrescriptionGroup(getPrescriptionGroup(therapy));

    //use today if interval contains today, otherwise use last day
    final Interval therapyDay = interval == null || interval.contains(when)
                                ? Intervals.wholeDay(when)
                                : Intervals.wholeDay(interval.getEnd().minusMinutes(1));

    fillTherapyDayState(
        timelineRow,
        patientId,
        Opt.resolve(() -> patientData.getCentralCaseDto().getCareProvider().getId()).orElse(null),
        therapy,
        inpatientPrescription,
        roundsInterval,
        medicationsDataMap,
        therapyDay,
        when);

    // set witnessing
    timelineRow.setWitnessingRequired(witnessingHandler.isTherapyWitnessingRequired(timelineRow));

    return timelineRow;
  }

  PrescriptionGroupEnum getPrescriptionGroup(final TherapyDto therapy)
  {
    final List<MedicationDataDto> medications = therapy.getMedicationIds().stream()
        .map(i -> medicationsValueHolderProvider.getMedicationData(i))
        .collect(Collectors.toList());

    if (medications.stream().anyMatch(MedicationDataDto::isAntibiotic))
    {
      return PrescriptionGroupEnum.ANTIMICROBIALS;
    }

    if (medications.stream().anyMatch(MedicationDataDto::isAnticoagulant))
    {
      return PrescriptionGroupEnum.ANTICOAGULANTS;
    }

    if (medications.stream().anyMatch(MedicationDataDto::isInsulin))
    {
      return PrescriptionGroupEnum.INSULINS;
    }

    if (medications.stream().anyMatch(MedicationDataDto::isFluid))
    {
      return PrescriptionGroupEnum.FLUIDS;
    }

    if (medications.stream().anyMatch(m -> m.getMedication().getMedicationType() == MedicationTypeEnum.BLOOD_PRODUCT))
    {
      return PrescriptionGroupEnum.BLOOD_PRODUCTS;
    }

    if (medications.stream().anyMatch(m -> m.getMedication().getMedicationType() == MedicationTypeEnum.MEDICINAL_GAS))
    {
      return PrescriptionGroupEnum.MEDICINAL_GASES;
    }

    final DosingFrequencyDto dosingFrequency = therapy.getDosingFrequency();
    if (dosingFrequency != null && dosingFrequency.getType() == DosingFrequencyTypeEnum.ONCE_THEN_EX)
    {
      return PrescriptionGroupEnum.STAT_DOSES;
    }

    if (therapy.isWhenNeeded())
    {
      return PrescriptionGroupEnum.PRN;
    }
    return PrescriptionGroupEnum.REGULAR;
  }

  private TherapyRowDto createEmptyTherapyRow(final MedicationOrder medicationOrder)
  {
    if (MedicationsEhrUtils.isContinuousInfusion(medicationOrder))
    {
      return new ContinuousInfusionTherapyRowDtoDto();
    }
    if (MedicationsEhrUtils.isOxygen(medicationOrder))
    {
      return new OxygenTherapyRowDtoDto();
    }
    return new TherapyRowDto();
  }

  private String findModifiedInpatientPrescriptionForTimeline(
      final Map<String, InpatientPrescription> processedInpatientPrescriptionsMap,
      final InpatientPrescription inpatientPrescription)
  {
    for (final String therapyId : processedInpatientPrescriptionsMap.keySet())
    {
      final InpatientPrescription compareInpatientPrescription = processedInpatientPrescriptionsMap.get(therapyId);
      final boolean laterLinkedTherapyFound = medicationsBo.areInpatientPrescriptionsLinkedByUpdate(
          inpatientPrescription,
          compareInpatientPrescription);
      if (laterLinkedTherapyFound)
      {
        return therapyId;
      }
    }
    return null;
  }

  @Override
  public Map<String, TherapyDayDto> getCompositionUidAndTherapyDayDtoMap(
      final Map<String, String> inpatientPrescriptionUidAndPatientIdMap,
      final DateTime when,
      final Locale locale)
  {
    final List<InpatientPrescription> inpatientPrescriptions =
        medicationsOpenEhrDao.loadInpatientPrescriptions(inpatientPrescriptionUidAndPatientIdMap.keySet());

    final Map<String, InpatientPrescription> inpatientPrescriptionsMap = new HashMap<>();
    for (final InpatientPrescription inpatientPrescription : inpatientPrescriptions)
    {
      inpatientPrescriptionsMap.put(
          TherapyIdUtils.getCompositionUidWithoutVersion(inpatientPrescription.getUid()),
          inpatientPrescription);
    }

    return getCompositionUidAndTherapyDayDtoMap(
        inpatientPrescriptionUidAndPatientIdMap,
        inpatientPrescriptions,
        inpatientPrescriptionsMap,
        when,
        locale);
  }

  @Override
  public Map<String, TherapyDayDto> getOriginalCompositionUidAndLatestTherapyDayDtoMap(
      final Map<String, String> originalTherapyCompositionUidAndPatientIdMap,
      final int searchIntervalInWeeks,
      final DateTime when,
      final Locale locale)
  {
    final Set<String> originalCompositionUids = originalTherapyCompositionUidAndPatientIdMap.keySet();
    final Set<String> patientIds = Sets.newHashSet(originalTherapyCompositionUidAndPatientIdMap.values());

    final Map<String, InpatientPrescription> originalCompositionUidAndLatestCompositionMap =
        medicationsOpenEhrDao.getLatestCompositionsForOriginalCompositionUids(
            originalCompositionUids,
            patientIds,
            searchIntervalInWeeks,
            when);

    final List<InpatientPrescription> medicationOrders = new ArrayList<>();
    for (final Map.Entry<String, InpatientPrescription> entrySet : originalCompositionUidAndLatestCompositionMap.entrySet())
    {
      medicationOrders.add(entrySet.getValue());
    }
    return getCompositionUidAndTherapyDayDtoMap(
        originalTherapyCompositionUidAndPatientIdMap,
        medicationOrders,
        originalCompositionUidAndLatestCompositionMap,
        when,
        locale);
  }

  private Map<String, TherapyDayDto> getCompositionUidAndTherapyDayDtoMap(
      final Map<String, String> therapyIdAndPatientIdMap,
      final List<InpatientPrescription> inpatientPrescriptions,
      final Map<String, InpatientPrescription> compositionUidAndCompositionMap,
      final DateTime when,
      final Locale locale)
  {
    final Map<String, TherapyDayDto> originalCompositionUdAndTherapyDayDtoMap = new HashMap<>();
    final Map<Long, MedicationDataForTherapyDto> medicationDataForTherapies = medicationsBo.getMedicationDataForInpatientPrescriptions(
        inpatientPrescriptions,
        null);

    final Map<String, List<TherapyDayDto>> patientIdAndTherapyDayDtosMap = new HashMap<>();
    for (final Map.Entry<String, InpatientPrescription> entrySet : compositionUidAndCompositionMap.entrySet())
    {
      final TherapyDayDto therapyDayDto = new TherapyDayDto();
      final InpatientPrescription inpatientPrescription = entrySet.getValue();

      final TherapyDto therapy =
          medicationsBo.convertMedicationOrderToTherapyDto(
              inpatientPrescription,
              inpatientPrescription.getMedicationOrder(),
              null,  //todo supply - get from client
              null,
              true,
              locale);

      final String patientId = therapyIdAndPatientIdMap.get(entrySet.getKey());

      fillTherapyDayState(
          therapyDayDto,
          patientId,
          null,
          therapy,
          inpatientPrescription,
          null,        //todo supply - load it
          medicationDataForTherapies,
          Intervals.wholeDay(when),
          when);

      originalCompositionUdAndTherapyDayDtoMap.put(entrySet.getKey(), therapyDayDto);

      if (patientIdAndTherapyDayDtosMap.containsKey(patientId))
      {
        patientIdAndTherapyDayDtosMap.get(patientId).add(therapyDayDto);
      }
      else
      {
        final List<TherapyDayDto> therapyDayDtos = new ArrayList<>();
        therapyDayDtos.add(therapyDayDto);
        patientIdAndTherapyDayDtosMap.put(patientId, therapyDayDtos);
      }
    }

    for (final String patientId : patientIdAndTherapyDayDtosMap.keySet())
    {
      fillPharmacyReviewDate(patientId, patientIdAndTherapyDayDtosMap.get(patientId));
    }

    return originalCompositionUdAndTherapyDayDtoMap;
  }

  private void fillTherapyDayState(
      final TherapyDayDto therapyDayDto,
      final String patientId,
      final String careProviderId,
      final TherapyDto therapy,
      final InpatientPrescription inpatientPrescription,
      final RoundsIntervalDto roundsInterval,
      final Map<Long, MedicationDataForTherapyDto> medicationsDataMap,
      final Interval therapyDay,
      final DateTime currentTime)
  {
    final List<MedicationManagement> actions = inpatientPrescription.getActions();

    therapyDayDto.setTherapy(therapy);
    final DateTime lastModifiedTimestamp = PrescriptionsEhrUtils.getLastModifiedTimestamp(inpatientPrescription);

    if (lastModifiedTimestamp != null)
    {
      therapyDayDto.setLastModifiedTimestamp(lastModifiedTimestamp);
    }
    else
    {
      therapyDayDto.setLastModifiedTimestamp(therapy.getCreatedTimestamp());
    }

    therapyDayDto.setBasedOnPharmacyReview(!LinksEhrUtils.getLinksOfType(
        inpatientPrescription.getLinks(),
        EhrLinkType.BASED_ON).isEmpty());

    final DateTime originalTherapyStart = therapyEhrHandler.getOriginalTherapyStart(patientId, inpatientPrescription);
    therapyDayDto.setOriginalTherapyStart(originalTherapyStart);
    final DateTime therapyEnd = therapy.getEnd() != null ? therapy.getEnd() : Intervals.INFINITE.getEnd();

    final DateTime therapyReviewedUntil = getTherapyReviewedUntil(careProviderId, actions, roundsInterval);
    therapyDayDto.setReviewedUntil(therapyReviewedUntil);
    final boolean doctorReviewNeeded = isDoctorReviewNeeded(therapyReviewedUntil, roundsInterval, currentTime);
    therapyDayDto.setDoctorReviewNeeded(doctorReviewNeeded);

    final TherapyStatusEnum therapyStatus = getTherapyStatusFromMedicationAction(
        actions,
        originalTherapyStart,
        roundsInterval,
        currentTime,
        therapyReviewedUntil);

    final boolean modifiedFromLastReview = PrescriptionsEhrUtils.isInpatientPrescriptionModifiedFromLastReview(inpatientPrescription);

    final boolean containsAntibiotic = containsAntibiotic(therapy.getMedicationIds(), medicationsDataMap);

    final int consecutiveDay = getTherapyConsecutiveDay(
        therapy.getPastTherapyStart(),
        originalTherapyStart,
        therapy.getEnd(),
        therapyDay,
        currentTime);

    final boolean therapyEndsBeforeNextRounds =
        roundsInterval != null && doesTherapyEndBeforeNextRounds(therapyEnd, roundsInterval, currentTime);
    final boolean therapyIsActive =
        medicationsBo.isTherapyActive(
            therapy.getDaysOfWeek(),
            therapy.getDosingDaysFrequency(),
            new Interval(originalTherapyStart, therapyEnd),
            therapyDay.getStart());

    if (therapyStatus == TherapyStatusEnum.SUSPENDED)
    {
      final TherapyChangeReasonDto suspendReason = PrescriptionsEhrUtils.getSuspendReason(inpatientPrescription);
      if (suspendReason != null)
      {
        therapyDayDto.setStatusReason(extractStatusReason(suspendReason));
      }
    }

    if (TherapyStatusEnum.STOPPED.contains(therapyStatus))
    {
      final TherapyChangeReasonDto stopReason = PrescriptionsEhrUtils.getStoppedReason(inpatientPrescription);
      if (stopReason != null)
      {
        therapyDayDto.setStatusReason(extractStatusReason(stopReason));
      }
    }

    therapyDayDto.setActive(therapyIsActive);
    therapyDayDto.setModified(!inpatientPrescription.getLinks().isEmpty());
    therapyDayDto.setTherapyEndsBeforeNextRounds(therapyEndsBeforeNextRounds);
    therapyDayDto.setTherapyStatus(therapyStatus);
    therapyDayDto.setShowConsecutiveDay(containsAntibiotic && therapyDay.getStart().isBefore(currentTime));
    therapyDayDto.setModifiedFromLastReview(modifiedFromLastReview);
    therapyDayDto.setConsecutiveDay(consecutiveDay);
    therapyDayDto.setMedicationProperties(extractMedicationProperties(therapy));
    therapyDayDto.setContainsNonFormularyMedications(doesTherapyContainNonFormularyMedications(therapy));

    final boolean therapySuspended = wasTherapySuspendedWholeDay(actions, therapyDay);
    therapyDayDto.setActiveAnyPartOfDay(therapyIsActive && !therapySuspended);
  }

  String extractStatusReason(final TherapyChangeReasonDto therapyChangeReasonDto)
  {
    return Opt.resolve(therapyChangeReasonDto::getComment)
        .or(Opt.resolve(() -> therapyChangeReasonDto.getChangeReason().getName()))
        .orElse(null);
  }

  int getTherapyConsecutiveDay(
      final DateTime pastTherapyStart,
      final @NonNull DateTime therapyStart,
      final DateTime therapyEnd,
      final @NonNull Interval therapyDay,
      final @NonNull DateTime currentTime)
  {
    final int pastDays = medsProperties.getAntimicrobialDaysCountStartsWithOne() ? 1 : 0;
    final DateTime fromTime = pastTherapyStart != null ? pastTherapyStart : therapyStart;

    //today
    if (therapyDay.contains(currentTime))
    {
      final DateTime toTime = therapyEnd != null && therapyEnd.isBefore(currentTime) ? therapyEnd : currentTime;
      return Days.daysBetween(fromTime, toTime).getDays() + pastDays;
    }
    final DateTime therapyDayEnd = therapyDay.getEnd();
    final DateTime toTime = therapyEnd != null && therapyEnd.isBefore(therapyDayEnd) ? therapyEnd : therapyDayEnd;
    return Days.daysBetween(fromTime, toTime).getDays() + pastDays;
  }

  //extracts distinct properties based on type
  private Set<MedicationPropertyDto> extractMedicationProperties(final TherapyDto therapy)
  {
    final Map<MedicationPropertyType, MedicationPropertyDto> properties = new HashMap<>();
    therapy.getMedications().stream()
        .map(MedicationDto::getId)
        .filter(Objects::nonNull)
        .map(i -> medicationsValueHolderProvider.getMedicationData(i))
        .filter(Objects::nonNull)
        .flatMap(this::extractPropertiesWithType)
        .forEach(p -> properties.put(p.getType(), p));
    return new HashSet<>(properties.values());
  }

  private Stream<MedicationPropertyDto> extractPropertiesWithType(final MedicationDataDto medicationData)
  {
    return medicationData.getProperties().stream()
        .filter(p -> p.getType() != null);
  }

  private boolean doesTherapyContainNonFormularyMedications(final TherapyDto therapy)
  {
    return therapy.getMedications().stream()
        .map(MedicationDto::getId)
        .filter(Objects::nonNull)
        .map(i -> medicationsValueHolderProvider.getMedicationData(i))
        .filter(Objects::nonNull)
        .anyMatch(MedicationDataDto::isFormulary);
  }

  DateTime getTherapyReviewedUntil(
      final String careProviderId,
      final List<MedicationManagement> therapyActions,
      final RoundsIntervalDto roundsInterval)
  {
    if (roundsInterval == null)
    {
      return null;
    }

    final DateTime lastReviewTimestamp = therapyActions
        .stream()
        .filter(a -> MedicationActionEnum.THERAPY_REVIEW_ACTIONS.contains(MedicationActionEnum.getActionEnum(a)))
        .map(a -> DataValueUtils.getDateTime(a.getTime()))
        .max(Comparator.naturalOrder())
        .orElse(null);

    if (lastReviewTimestamp == null)
    {
      return null;
    }

    final DateTime startOfReviewDayRounds =
        lastReviewTimestamp.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getStartHour())
            .plusMinutes(roundsInterval.getStartMinute());

    final DateTime endOfReviewDayRounds =
        lastReviewTimestamp.withTimeAtStartOfDay()
            .plusHours(roundsInterval.getEndHour())
            .plusMinutes(roundsInterval.getEndMinute());

    final boolean lastReviewBeforeRounds = lastReviewTimestamp.isBefore(startOfReviewDayRounds);
    DateTime reviewedUntil = lastReviewBeforeRounds ? endOfReviewDayRounds : endOfReviewDayRounds.plusDays(1);

    if (!MedicationPreferencesUtil.isReviewOnWeekendEnabled(careProviderId))
    {
      while (!mafDateRuleService.isDateOfType(reviewedUntil.withTimeAtStartOfDay(), DayType.WORKING_DAY))
      {
        reviewedUntil = reviewedUntil.plusDays(1);
      }
    }

    return reviewedUntil;
  }

  private boolean wasTherapySuspendedWholeDay(
      final List<MedicationManagement> actions,  //actions sorted by time ascending
      final Interval dayInterval)
  {
    boolean suspended = false;
    for (final MedicationManagement action : actions)
    {
      final MedicationActionEnum actionEnum = MedicationActionEnum.getActionEnum(action);
      if (actionEnum == MedicationActionEnum.SUSPEND &&
          DataValueUtils.getDateTime(action.getTime()).isBefore(dayInterval.getStart()))
      {
        suspended = true;
      }
      else if (actionEnum == MedicationActionEnum.REISSUE &&
          DataValueUtils.getDateTime(action.getTime()).isBefore(dayInterval.getEnd()))
      {
        suspended = false;
      }
    }
    return suspended;
  }

  private boolean containsAntibiotic(
      final Collection<Long> medicationIds,
      final Map<Long, MedicationDataForTherapyDto> medicationsMap)
  {
    for (final Long medicationId : medicationIds)
    {
      final MedicationDataForTherapyDto mainMedicationData = medicationsMap.get(medicationId);
      if (mainMedicationData != null && mainMedicationData.isAntibiotic())
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      final String patientId,
      final String careProviderId,
      final String compositionUid,
      final String ehrOrderName,
      final RoundsIntervalDto roundsInterval,
      final DateTime when)
  {
    final InpatientPrescription inpatientPrescription = medicationsOpenEhrDao.loadInpatientPrescription(
        patientId,
        compositionUid);
    final MedicationOrder medicationOrder = inpatientPrescription.getMedicationOrder();

    final TherapyReloadAfterActionDto reloadDto = new TherapyReloadAfterActionDto();
    reloadDto.setEhrCompositionId(inpatientPrescription.getUid());
    reloadDto.setEhrOrderName(medicationOrder.getName().getValue());
    final DateTime originalTherapyStart = therapyEhrHandler.getOriginalTherapyStart(patientId, inpatientPrescription);
    final DateTime therapyEnd = DataValueUtils.getDateTime(medicationOrder.getOrderDetails().getOrderStopDateTime());

    final TherapyChangeReasonDto suspendReason = PrescriptionsEhrUtils.getSuspendReason(inpatientPrescription);
    if (suspendReason != null)
    {
      reloadDto.setStatusReason(extractStatusReason(suspendReason));
    }

    reloadDto.setTherapyStart(originalTherapyStart);
    reloadDto.setTherapyEnd(therapyEnd);

    final List<MedicationManagement> actions = inpatientPrescription.getActions();

    final DateTime therapyReviewedUntil = getTherapyReviewedUntil(careProviderId, actions, roundsInterval);
    final boolean doctorReviewNeeded = isDoctorReviewNeeded(therapyReviewedUntil, roundsInterval, when);
    reloadDto.setDoctorReviewNeeded(doctorReviewNeeded);

    reloadDto.setTherapyStatus(getTherapyStatusFromMedicationAction(
        actions,
        originalTherapyStart,
        roundsInterval,
        when,
        therapyReviewedUntil));

    final boolean therapyEndsBeforeNextRounds = doesTherapyEndBeforeNextRounds(therapyEnd, roundsInterval, when);
    reloadDto.setTherapyEndsBeforeNextRounds(therapyEndsBeforeNextRounds);
    return reloadDto;
  }

  private boolean doesTherapyEndBeforeNextRounds(
      final DateTime therapyEnd, final RoundsIntervalDto roundsInterval, final DateTime currentTime)
  {
    if (therapyEnd == null)
    {
      return false;
    }
    final DateTime startOfNextDaysRounds =
        currentTime.withTimeAtStartOfDay()
            .plusDays(1)
            .plusHours(roundsInterval.getStartHour())
            .plusMinutes(roundsInterval.getStartMinute());

    return therapyEnd.isBefore(startOfNextDaysRounds);
  }

  @Override
  public TherapyStatusEnum getTherapyStatus(final @NonNull List<MedicationManagement> actions)
  {
    return getTherapyStatusFromMedicationAction(actions, null, null, null, null);
  }

  TherapyStatusEnum getTherapyStatusFromMedicationAction(
      final List<MedicationManagement> actions,  //sorted by time asc
      final DateTime therapyStart,
      final RoundsIntervalDto roundsInterval,
      final DateTime when,
      final DateTime therapyReviewedUntil)
  {
    final Set<MedicationActionEnum> actionEnumSet =
        actions.stream()
            .map(MedicationActionEnum::getActionEnum)
            .collect(Collectors.toSet());

    if (actionEnumSet.contains(MedicationActionEnum.CANCEL))
    {
      return TherapyStatusEnum.CANCELLED;
    }
    if (actionEnumSet.contains(MedicationActionEnum.ABORT))
    {
      return TherapyStatusEnum.ABORTED;
    }
    if (PrescriptionsEhrUtils.isTherapySuspended(actions))
    {
      return TherapyStatusEnum.SUSPENDED;
    }

    if (roundsInterval != null && when != null)
    {
      final DateTime startOfNextDaysRounds =
          Opt.of(roundsInterval)
              .map(r -> when.withTimeAtStartOfDay()
                  .plusDays(1)
                  .plusHours(r.getStartHour()).plusMinutes(r.getStartMinute()))
              .orElse(null);

      if (startOfNextDaysRounds != null && therapyStart != null && therapyStart.isAfter(startOfNextDaysRounds))
      {
        return TherapyStatusEnum.FUTURE;
      }
    }

    if (medsProperties.getDoctorReviewEnabled() && therapyReviewedUntil != null)
    {
      if (therapyReviewedUntil.isBefore(when))
      {
        return TherapyStatusEnum.VERY_LATE;
      }
      if (therapyReviewedUntil.minusHours(1).isBefore(when))
      {
        return TherapyStatusEnum.LATE;
      }
    }

    return TherapyStatusEnum.NORMAL;
  }

  private boolean isDoctorReviewNeeded(
      final DateTime reviewedUntil,
      final RoundsIntervalDto roundsInterval,
      final DateTime currentTime)
  {
    if (reviewedUntil != null)
    {
      final DateTime startOfReviewDayRounds =
          reviewedUntil.withTimeAtStartOfDay()
              .plusHours(roundsInterval.getStartHour())
              .plusMinutes(roundsInterval.getStartMinute());

      return currentTime.isAfter(startOfReviewDayRounds);
    }
    return true;
  }
}
