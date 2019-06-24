package com.marand.thinkmed.medications.mentalhealth;

import java.util.Collection;
import lombok.NonNull;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import org.joda.time.Interval;

/**
 * @author Nejc Korasa
 */
public interface MentalHealthFormProvider
{
  Opt<MentalHealthDocumentDto> getLatestMentalHealthDocument(@NonNull String patientId);

  MentalHealthDocumentDto getMentalHealthDocument(@NonNull String patientId, @NonNull String compositionUId);

  Collection<MentalHealthDocumentDto> getMentalHealthDocuments(@NonNull String patientId, Interval interval, Integer fetchCount);
}
