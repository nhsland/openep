package com.marand.thinkmed.medications.administration;

import java.util.List;

import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import lombok.NonNull;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface AdministrationProvider
{
  /**
   * Finds administration compositions for prescriptions in search interval and converts them to AdministrationDto.
   *
   * @param patientId patient id.
   * @param prescriptions prescriptions for which administrations are loaded.
   * @param searchInterval search interval.
   *
   * @return List of AdministrationDto.
   */
  List<AdministrationDto> getPrescriptionsAdministrations(
      @NonNull String patientId,
      @NonNull List<InpatientPrescription> prescriptions,
      Interval searchInterval,
      boolean clinicalIntervention);
}
