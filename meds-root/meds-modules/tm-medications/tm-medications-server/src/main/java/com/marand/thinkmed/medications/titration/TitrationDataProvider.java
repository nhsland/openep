package com.marand.thinkmed.medications.titration;

import java.util.List;
import java.util.Locale;
import lombok.NonNull;

import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.connector.data.object.QuantityWithTimeDto;
import com.marand.thinkmed.medications.dto.TitrationDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */
public interface TitrationDataProvider
{
  TitrationDto getDataForTitration(
      @NonNull String patientId,
      @NonNull String therapyId,
      @NonNull TitrationType titrationType,
      @NonNull DateTime searchStart,
      @NonNull DateTime searchEnd,
      @NonNull DateTime when,
      @NonNull Locale locale);

  List<QuantityWithTimeDto> getObservationResults(
      @NonNull String patientId,
      @NonNull TitrationType titrationType,
      @NonNull Interval interval);
}
