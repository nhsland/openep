package com.marand.thinkmed.medications.outpatient;

import java.util.List;
import java.util.Locale;
import lombok.NonNull;

import com.marand.thinkmed.medications.api.internal.dto.OutpatientPrescriptionStatus;
import com.marand.thinkmed.medications.api.internal.dto.PrescriptionDto;
import com.marand.thinkmed.medications.api.internal.dto.prescription.PrescriptionPackageDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface OutpatientPrescriptionHandler
{
  String savePrescription(String patientId, PrescriptionPackageDto prescriptionPackageDto, DateTime when);

  void updatePrescriptionStatus(
      String patientId,
      String compositionUid,
      String prescriptionTherapyId,
      OutpatientPrescriptionStatus status,
      DateTime when);

  String updatePrescriptionPackage(
      String patientId,
      String compositionUid,
      List<PrescriptionDto> prescriptionDtoList,
      DateTime when);

  byte[] getOutpatientPrescriptionPrintout(
      @NonNull String patientId,
      @NonNull String compositionUid,
      @NonNull Locale locale,
      @NonNull DateTime when);
}
