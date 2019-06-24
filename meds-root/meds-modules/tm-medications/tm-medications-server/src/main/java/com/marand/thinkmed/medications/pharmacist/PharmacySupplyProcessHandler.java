package com.marand.thinkmed.medications.pharmacist;

import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;

/**
 * @author Klavdij Lapajne
 */
public interface PharmacySupplyProcessHandler
{
  void handleSupplyRequest(
      final String patientId,
      final TherapyAssigneeEnum requesterRole,
      final String therapyCompositionUid,
      final String ehrOrderName,
      final Integer supplyInDays,
      final MedicationSupplyTypeEnum supplyType);

  void handleSupplyRequest(
      final String patientId,
      final TherapyAssigneeEnum requesterRole,
      final String originalTherapyId,
      final MedicationSupplyTypeEnum supplyType,
      final Integer supplyInDays);
}
