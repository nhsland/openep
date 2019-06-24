package com.marand.meds.logic.eer;

import java.util.List;
import javax.annotation.Nullable;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.api.internal.dto.OutpatientPrescriptionStatus;
import com.marand.thinkmed.medications.api.internal.dto.PrescriptionDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionPackageDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionTherapyDto;
import com.marand.thinkmed.medications.ehr.model.MedicationAuthorisationSlovenia;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.OutpatientPrescription;
import com.marand.thinkmed.medications.ehr.utils.EhrValueUtils;
import com.marand.thinkmed.medications.outpatient.impl.OutpatientPrescriptionHandlerImpl;
import org.joda.time.DateTime;
import org.openehr.jaxb.rm.DvCount;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class EERPrescriptionHandlerImpl extends OutpatientPrescriptionHandlerImpl
{
  @Override
  public void updatePrescriptionStatus(
      final String patientId,
      final String compositionUid,
      @Nullable final String prescriptionTherapyId,
      final OutpatientPrescriptionStatus status,
      final DateTime when)
  {
    final OutpatientPrescription composition = getEhrMedicationsDao().loadOutpatientPrescription(patientId, compositionUid);
    for (final MedicationOrder medicationOrder : composition.getMedicationOrder())
    {
      final MedicationAuthorisationSlovenia authorisation = medicationOrder.getAuthorisationDirection();
      if (prescriptionTherapyId == null ||
          authorisation.getePrescriptionUniqueIdentifier().getValue().equals(prescriptionTherapyId))
      {
        authorisation.setPrescriptionStatus(status.getDvCodedText());
      }
    }
    getEhrMedicationsDao().saveComposition(patientId, composition, compositionUid);
  }

  @Override
  public String updatePrescriptionPackage(
      final String patientId,
      final String compositionUid,
      final List<PrescriptionDto> prescriptionDtoList,
      final DateTime when)
  {
    final OutpatientPrescription composition = getEhrMedicationsDao().loadOutpatientPrescription(patientId, compositionUid);

    for (final MedicationOrder medicationOrder : composition.getMedicationOrder())
    {
      for (final PrescriptionDto prescriptionDto : prescriptionDtoList)
      {
        final MedicationAuthorisationSlovenia authorisation = medicationOrder.getAuthorisationDirection();

        final String prescriptionTherapyId = prescriptionDto.getPrescriptionId();

        if (authorisation.getePrescriptionUniqueIdentifier().getValue().equals(prescriptionTherapyId))
        {
          final DvCount numberOfRemainingDispenses = new DvCount();

          numberOfRemainingDispenses.setMagnitude(prescriptionDto.getRemainingRepeats());

          if (prescriptionDto.getStatus() != null)
          {
            authorisation.setPrescriptionStatus(prescriptionDto.getStatus().getDvCodedText());
          }
          authorisation.setNumberOfRemainingDispenses(numberOfRemainingDispenses);
          authorisation.setUpdateTimestamp(DataValueUtils.getDateTime(when));
          break;
        }
      }
    }

    return getEhrMedicationsDao().saveComposition(patientId, composition, compositionUid);
  }

  @Override
  protected void fillAuthorisationData(
      final MedicationOrder medicationOrder,
      final PrescriptionTherapyDto prescriptionDto,
      final PrescriptionPackageDto prescriptionPackage)
  {
    if (medicationOrder.getAuthorisationDirection() != null)
    {
      final MedicationAuthorisationSlovenia authorisation = medicationOrder.getAuthorisationDirection();

      authorisation.setePrescriptionUniqueIdentifier(EhrValueUtils.getText(prescriptionDto.getPrescriptionTherapyId()));
      authorisation.setPackageEPrescriptionUniqueIdentifier(EhrValueUtils.getText(prescriptionPackage.getPrescriptionPackageId()));
      if (prescriptionDto.getPrescriptionStatus() != null)
      {
        authorisation.setPrescriptionStatus(prescriptionDto.getPrescriptionStatus().getDvCodedText());
      }

      if (prescriptionPackage instanceof EERPrescriptionPackageDto)
      {
        final EERPrescriptionPackageDto eerPrescriptionPackage = (EERPrescriptionPackageDto)prescriptionPackage;
        if (eerPrescriptionPackage.getSurchargeType() != null)
        {
          authorisation.setSurchargeType(eerPrescriptionPackage.getSurchargeType().getDvCodedText());
        }
        if (eerPrescriptionPackage.getPayer() != null)
        {
          authorisation.setPayer(eerPrescriptionPackage.getPayer().getDvCodedText());
        }
        if (eerPrescriptionPackage.getTreatmentReason() != null)
        {
          authorisation.setTreatmentReason(eerPrescriptionPackage.getTreatmentReason().getDvCodedText());
        }
      }
    }
  }
}
