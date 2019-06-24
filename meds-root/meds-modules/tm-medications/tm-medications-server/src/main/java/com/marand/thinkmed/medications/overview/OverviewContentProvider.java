package com.marand.thinkmed.medications.overview;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

import com.marand.thinkmed.medications.TherapySortTypeEnum;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.dto.TherapyReloadAfterActionDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.overview.TherapyFlowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.dto.overview.TherapyTimelineDto;
import com.marand.thinkmed.medications.dto.warning.TherapyAdditionalWarningDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface OverviewContentProvider
{
  TherapyFlowDto getTherapyFlow(
      String patientId,
      String centralCaseId,
      Double patientHeight,
      DateTime startDate,
      int dayCount,
      Integer todayIndex,
      RoundsIntervalDto roundsInterval,
      TherapySortTypeEnum therapySortTypeEnum,
      @Nullable String careProviderId,
      DateTime currentTime,
      Locale locale);

  TherapyTimelineDto getTherapyTimeline(
      @NonNull String patientId,
      @NonNull List<AdministrationDto> administrations,
      @NonNull List<AdministrationTaskDto> administrationTasks,
      @NonNull List<InpatientPrescription> inpatientPrescriptions,
      @NonNull TherapySortTypeEnum therapySortTypeEnum,
      boolean hidePastTherapies,
      @NonNull PatientDataForMedicationsDto patientData,
      Interval interval,
      RoundsIntervalDto roundsInterval,
      Locale locale,
      @NonNull DateTime when);

  List<TherapyRowDto> buildTherapyRows(
      @NonNull String patientId,
      @NonNull List<InpatientPrescription> inpatientPrescriptions,
      @NonNull List<AdministrationDto> administrations,
      @NonNull List<AdministrationTaskDto> administrationTasks,
      @NonNull TherapySortTypeEnum therapySortTypeEnum,
      boolean hidePastTherapies,
      @NonNull List<TherapyAdditionalWarningDto> additionalWarnings,
      PatientDataForMedicationsDto patientData,
      Interval interval,
      RoundsIntervalDto roundsInterval,
      Locale locale,
      @NonNull DateTime when);

  Map<String, TherapyDayDto> getCompositionUidAndTherapyDayDtoMap(
      final Map<String, String> therapyCompositionUidAndPatientIdMap,
      final DateTime when,
      final Locale locale);

  Map<String, TherapyDayDto> getOriginalCompositionUidAndLatestTherapyDayDtoMap(
      Map<String, String> originalTherapyCompositionUidAndPatientIdMap,
      int searchIntervalInWeeks,
      DateTime when,
      Locale locale);

  TherapyReloadAfterActionDto reloadSingleTherapyAfterAction(
      String patientId,
      String careProviderId,
      String compositionUid,
      String ehrOrderName,
      RoundsIntervalDto roundsInterval,
      DateTime when);

  TherapyStatusEnum getTherapyStatus(@NonNull List<MedicationManagement> actions);
}
