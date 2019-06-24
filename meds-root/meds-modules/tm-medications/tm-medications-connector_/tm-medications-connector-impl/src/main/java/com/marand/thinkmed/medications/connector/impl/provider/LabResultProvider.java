package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.List;

import com.marand.thinkmed.medications.connector.data.object.ObservationDto;
import lombok.NonNull;
import org.joda.time.Interval;

/**
 * @author Vid Kumse
 */

public interface LabResultProvider
{
  List<ObservationDto> getLabResults(@NonNull String patientId, @NonNull String resultCode, @NonNull Interval interval);
}
