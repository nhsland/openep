package com.marand.thinkmed.medications.administration;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.medications.dto.NotAdministeredReasonEnum;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationResultEnum;
import com.marand.thinkmed.medications.dto.overview.TherapyRowDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.request.user.UserDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface AdministrationHandler
{
  void confirmTherapyAdministration(
      @NonNull String therapyCompositionUid,
      @NonNull String patientId,
      @NonNull String userId,
      @NonNull AdministrationDto administrationDto,
      boolean edit,
      boolean requestSupply,
      String centralCaseId,
      String careProviderId,
      @NonNull Locale locale,
      @NonNull DateTime when);

  void confirmAdministrationTask(
      @NonNull String patientId,
      @NonNull InpatientPrescription prescription,
      @NonNull TaskDto task,
      AutomaticChartingType autoChartingType,
      UserDto recordingUser,
      @NonNull DateTime currentTime);

  String confirmTherapyAdministration(
      @NonNull InpatientPrescription inpatientPrescription,
      @NonNull String patientId,
      @NonNull String userId,
      @NonNull AdministrationDto administrationDto,
      @NonNull AdministrationResultEnum administrationResult,
      boolean edit,
      String centralCaseId,
      String careProviderId,
      @NonNull DateTime when);

  void addAdministrationsToTimelines(
      @NonNull List<AdministrationDto> administrations,
      @NonNull Map<String, TherapyRowDto> therapyTimelineRowsMap,
      @NonNull Map<String, String> modifiedTherapiesMap,
      @NonNull Interval tasksInterval);

  void deleteAdministration(
      @NonNull String patientId,
      @NonNull AdministrationDto administration,
      @NonNull TherapyDoseTypeEnum therapyDoseType,
      @NonNull String therapyId,
      String comment);

  void cancelAdministrationTask(
      @NonNull String patientId,
      @NonNull AdministrationDto administration,
      @NonNull NotAdministeredReasonEnum notAdministeredReason,
      String comment);

  List<String> uncancelAdministrationTask(@NonNull String patientId, @NonNull AdministrationDto administration);
}
