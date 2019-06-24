package com.marand.thinkmed.medications.connector.impl.provider;

import lombok.NonNull;

/**
 * @author Mitja Lapajne
 */
public interface HeightProvider
{
  Double getPatientHeight(@NonNull String patientId);
}
