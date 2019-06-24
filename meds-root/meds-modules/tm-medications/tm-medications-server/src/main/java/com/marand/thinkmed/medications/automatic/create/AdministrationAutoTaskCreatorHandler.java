package com.marand.thinkmed.medications.automatic.create;

import java.util.List;
import java.util.Map;
import lombok.NonNull;

import com.marand.thinkmed.medications.dto.AutomaticAdministrationTaskCreatorDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface AdministrationAutoTaskCreatorHandler
{
  void createAdministrationTasksOnAutoCreate(
      @NonNull AutomaticAdministrationTaskCreatorDto automaticAdministrationTaskCreatorDto,
      @NonNull DateTime actionTimestamp);

  List<AutomaticAdministrationTaskCreatorDto> getAutoAdministrationTaskCreatorDtos(
      @NonNull DateTime when,
      @NonNull Map<InpatientPrescription, String> activePrescriptionsWithPatientIds);
}
