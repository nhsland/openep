package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.List;

import com.marand.thinkmed.medications.connector.data.object.AllergiesDto;
import lombok.NonNull;

/**
 * @author Mitja Lapajne
 */
public interface AllergiesProvider
{
  AllergiesDto getAllergies(@NonNull String patientId);

  AllergiesDto getAllergies(@NonNull String ehrId, @NonNull String uId);

  List<String> getLatestAllergyUIds(@NonNull String ehrId, int numberOfIds);

  String getPreviousAllergyUId(@NonNull String ehrId, @NonNull String compositionUId);
}
