package com.marand.thinkmed.medications.therapy.converter;

import java.util.List;

import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.therapy.converter.fromehr.TherapyFromEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.toehr.TherapyToEhrConverter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Bostjan Vester
 */

@Component
public class TherapyConverterSelector
{
  private List<TherapyToEhrConverter<?>> toEhrConverters;
  private List<TherapyFromEhrConverter<?>> fromEhrConverters;

  @Autowired
  public void setToEhrConverters(final List<TherapyToEhrConverter<?>> toEhrConverters)
  {
    this.toEhrConverters = toEhrConverters;
  }

  @Autowired
  public void setFromEhrConverters(final List<TherapyFromEhrConverter<?>> fromEhrConverters)
  {
    this.fromEhrConverters = fromEhrConverters;
  }

  public TherapyToEhrConverter<?> getConverter(final @NonNull TherapyDto therapy)
  {
    return toEhrConverters
        .stream()
        .filter(c -> c.isFor(therapy))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException(
            "No medication converter to EHR found for [" + therapy.getClass().getSimpleName() + "]!"));
  }

  public TherapyFromEhrConverter<?> getConverter(final @NonNull MedicationOrder medicationOrder)
  {
    return fromEhrConverters
        .stream()
        .filter(c -> c.isFor(medicationOrder))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException(
            "No therapy converter from EHR found for medicationOrder " + JsonUtil.toJson(medicationOrder)));
  }
}
