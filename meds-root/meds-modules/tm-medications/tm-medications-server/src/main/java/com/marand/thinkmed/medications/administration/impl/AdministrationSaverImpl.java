package com.marand.thinkmed.medications.administration.impl;

import com.marand.thinkmed.medications.administration.AdministrationSaver;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class AdministrationSaverImpl implements AdministrationSaver
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Override
  public String save(
      final @NonNull String patientId,
      final @NonNull MedicationAdministration composition,
      final String administrationId)
  {
    return medicationsOpenEhrDao.saveMedicationAdministration(patientId, composition, administrationId);
  }
}
