package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.Collection;
import java.util.List;

import com.marand.thinkmed.medications.connector.data.object.PatientDemographicsDto;
import lombok.NonNull;

/**
 * @author Mitja Lapajne
 */
public interface PatientDemographicsProvider
{
  List<PatientDemographicsDto> getPatientsDemographics(@NonNull Collection<String> patientsIds);
}
