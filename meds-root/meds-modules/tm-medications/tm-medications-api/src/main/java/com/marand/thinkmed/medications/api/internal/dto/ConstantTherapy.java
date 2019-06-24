package com.marand.thinkmed.medications.api.internal.dto;

import java.util.List;

import com.marand.maf.core.data.object.HourMinuteDto;

/**
 * @author Mitja Lapajne
 */
public interface ConstantTherapy
{
  TitrationType getTitration();

  void setTitration(TitrationType titration);

  List<HourMinuteDto> getDoseTimes();

  void setDoseTimes(List<HourMinuteDto> doseTimes);

  DosingFrequencyDto getDosingFrequency();

  void setDosingFrequency(DosingFrequencyDto dosingFrequency);
}
