package com.marand.thinkmed.medications.business;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import lombok.NonNull;

import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.api.internal.dto.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface MedicationsFinder
{
  List<TreeNodeData> findMedications(
      @NonNull String searchString,
      boolean startMustMatch,
      @NonNull EnumSet<MedicationFinderFilterEnum> filters,
      @NonNull DateTime when,
      Locale locale);

  List<TreeNodeData> findSimilarMedications(long medicationId, @NonNull DateTime when, Locale locale);

  List<MedicationDto> findMedicationProducts(
      long medicationId,
      @NonNull List<Long> routeIds,
      ReleaseDetailsDto releaseDetails,
      @NonNull DateTime when);
}
