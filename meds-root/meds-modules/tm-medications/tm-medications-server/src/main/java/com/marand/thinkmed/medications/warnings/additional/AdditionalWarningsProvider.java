package com.marand.thinkmed.medications.warnings.additional;

import java.util.Locale;
import lombok.NonNull;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface AdditionalWarningsProvider
{
  Opt<AdditionalWarningsDto> getAdditionalWarnings(
      @NonNull String patientId,
      @NonNull PatientDataForMedicationsDto patientData,
      @NonNull DateTime when,
      @NonNull Locale locale);
}
