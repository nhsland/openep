package com.marand.thinkmed.medications.infusion;

import lombok.NonNull;

import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import org.joda.time.DateTime;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */
public interface InfusionBagHandler
{
  /**
   * Recalculates planned infusion bag change time for therapy
   */

  @Transactional
  void recalculateInfusionBagChange(
      @NonNull String patientId,
      @NonNull String therapyId,
      final AdministrationDto administrationDto,
      final String administrationId,
      @NonNull DateTime actionTimestamp);

  /**
   * Calculates remaining infusion bag quantity for therapy at given time
   * @return Double
   */
  @Transactional
  Double getRemainingInfusionBagQuantity(@NonNull String patientId, @NonNull String therapyId, @NonNull DateTime when);
}
