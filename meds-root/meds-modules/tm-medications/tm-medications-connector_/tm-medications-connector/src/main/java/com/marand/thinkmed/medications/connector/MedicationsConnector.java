package com.marand.thinkmed.medications.connector;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForTherapyReportDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Bostjan Vester
 */
public interface MedicationsConnector
{
  PatientDataForMedicationsDto getPatientData(@NonNull String patientId, String centralCaseId, @NonNull DateTime when);

  PatientDataForTherapyReportDto getPatientDataForTherapyReport(
      String patientId,
      boolean mainDiseaseTypeOnly,
      DateTime when,
      Locale locale);

  Interval getLastDischargedCentralCaseEffectiveInterval(String patientId);

  List<NamedExternalDto> getCurrentUserCareProviders();

  List<PatientDisplayWithLocationDto> getPatientDisplaysWithLocation(
      Collection<String> careProviderIds,
      Collection<String> patientIds);

  List<QuantityWithTimeDto> getBloodSugarObservations(@NonNull String patientId, @NonNull Interval interval);

  List<QuantityWithTimeDto> getLabResults(@NonNull String patientId, @NonNull String resultCode, @NonNull Interval interval);

  List<QuantityWithTimeDto> findMeanArterialPressureMeasurements(@NonNull String patientId, @NonNull Interval interval);
}
