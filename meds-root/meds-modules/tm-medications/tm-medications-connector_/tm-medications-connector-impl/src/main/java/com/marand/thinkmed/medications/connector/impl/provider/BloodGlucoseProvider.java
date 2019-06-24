package com.marand.thinkmed.medications.connector.impl.provider;

import java.util.List;

import com.marand.thinkmed.medications.connector.data.object.ObservationDto;
import lombok.NonNull;
import org.joda.time.Interval;

/**
 * @author Mitja Lapajne
 */
public interface BloodGlucoseProvider
{
  List<ObservationDto> getPatientBloodGlucoseMeasurements(@NonNull String patientId, @NonNull Interval interval);
}
