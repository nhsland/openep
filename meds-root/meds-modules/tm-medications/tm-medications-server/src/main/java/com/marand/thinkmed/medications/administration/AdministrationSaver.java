package com.marand.thinkmed.medications.administration;

import lombok.NonNull;

import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;

/**
 * @author Nejc Korasa
 */

public interface AdministrationSaver
{
  String save(@NonNull String patientId, @NonNull MedicationAdministration composition, String administrationId);
}
