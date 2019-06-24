package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.List;

import com.marand.thinkmed.medications.connector.data.object.DiseaseDto;
import lombok.NonNull;

/**
 * @author Mitja Lapajne
 */
public interface DiseasesProvider
{
  List<DiseaseDto> getPatientDiseases(@NonNull String patientId);
}
