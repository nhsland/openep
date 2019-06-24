package com.marand.thinkmed.medications.change;

import java.util.List;
import java.util.Locale;
import lombok.NonNull;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;

/**
 * @author Mitja Lapajne
 */
public interface TherapyChangeCalculator
{
  List<TherapyChangeDto<?, ?>> calculateTherapyChanges(
      @NonNull TherapyDto therapy,
      @NonNull TherapyDto changedTherapy,
      boolean includeTherapyStartChange,
      @NonNull Locale locale);

  boolean hasTherapyChanged(
      @NonNull TherapyChangeType.TherapyChangeGroup group,
      @NonNull TherapyDto therapy,
      @NonNull TherapyDto changedTherapy,
      boolean includeTherapyEndChange,
      @NonNull Locale locale);
}
