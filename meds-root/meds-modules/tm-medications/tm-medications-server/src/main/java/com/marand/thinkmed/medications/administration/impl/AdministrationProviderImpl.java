package com.marand.thinkmed.medications.administration.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.marand.thinkmed.medications.administration.AdministrationProvider;
import com.marand.thinkmed.medications.administration.converter.AdministrationFromEhrConverter;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.MedicationAdministration;
import com.marand.thinkmed.medications.ehr.model.composition.EhrComposition;
import com.marand.thinkmed.medications.ehr.utils.PrescriptionsEhrUtils;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import lombok.NonNull;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class AdministrationProviderImpl implements AdministrationProvider
{
  private AdministrationFromEhrConverter administrationFromEhrConverter;
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Autowired
  public void setAdministrationFromEhrConverter(final AdministrationFromEhrConverter administrationFromEhrConverter)
  {
    this.administrationFromEhrConverter = administrationFromEhrConverter;
  }

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Override
  public List<AdministrationDto> getPrescriptionsAdministrations(
      final @NonNull String patientId,
      final @NonNull List<InpatientPrescription> prescriptions,
      final Interval searchInterval,
      final boolean clinicalIntervention)
  {
    final Set<String> prescriptionUids = prescriptions.stream()
        .map(EhrComposition::getUid)
        .map(TherapyIdUtils::getCompositionUidWithoutVersion)
        .collect(Collectors.toSet());

    final List<MedicationAdministration> administrations = medicationsOpenEhrDao.getMedicationAdministrations(
        patientId,
        prescriptionUids,
        searchInterval,
        clinicalIntervention);

    return administrations.stream()
        .map(a -> convertToAdministrationDto(prescriptions, a))
        .collect(Collectors.toList());
  }

  private AdministrationDto convertToAdministrationDto(
      final List<InpatientPrescription> prescriptions,
      final MedicationAdministration administration)
  {
    final InpatientPrescription prescription = extractPrescriptionForAdministration(prescriptions, administration);
    return administrationFromEhrConverter.convertToAdministrationDto(administration, prescription);
  }

  private InpatientPrescription extractPrescriptionForAdministration(
      final List<InpatientPrescription> prescriptions,
      final MedicationAdministration administration)
  {
    final String compositionUid = administration.getInstructionDetails().getInstructionId().getId().getValue();
    return PrescriptionsEhrUtils.extractPrescriptionByTherapyId(TherapyIdUtils.createTherapyId(compositionUid), prescriptions);
  }
}
