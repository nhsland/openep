package com.marand.thinkmed.medications.business.impl;

import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import lombok.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class ReleaseDetailsDisplayProvider
{
  public String buildReleaseDetailsDisplay(final @NonNull MedicationDataDto medication, final @NonNull Locale locale)
  {
    if (medication.getMedicationLevel() == MedicationLevelEnum.VTM)
    {
      return null;
    }

    if (medication.getMedicationLevel() == MedicationLevelEnum.VMP)
    {
      final MedicationPropertyDto mrProperty = medication.getProperty(MedicationPropertyType.MODIFIED_RELEASE);
      //noinspection VariableNotUsedInsideIf
      if (mrProperty != null)
      {
        return Dictionary.getEntry("modified.release.short", locale);
      }
    }
    else
    {
      final MedicationPropertyDto mrProperty = medication.getProperty(MedicationPropertyType.MODIFIED_RELEASE_TIME);
      if (mrProperty != null)
      {
        return Dictionary.getEntry("modified.release.short", locale) + mrProperty.getValue();
      }
    }

    final MedicationPropertyDto grProperty = medication.getProperty(MedicationPropertyType.GASTRO_RESISTANT);
    //noinspection VariableNotUsedInsideIf
    if (grProperty != null)
    {
      return Dictionary.getEntry("gastro.resistant.short", locale);
    }

    return null;
  }

  public String buildReleaseDetailsDisplay(final ReleaseDetailsDto releaseDetails, final Locale locale)
  {
    if (releaseDetails != null)
    {
      if (releaseDetails.getType() == ReleaseType.MODIFIED_RELEASE)
      {
        final String mrDisplay = Dictionary.getEntry("modified.release.short", locale);
        return releaseDetails.getHours() != null ? mrDisplay + releaseDetails.getHours() : mrDisplay;
      }
      if (releaseDetails.getType() == ReleaseType.GASTRO_RESISTANT)
      {
        return Dictionary.getEntry("gastro.resistant.short", locale);
      }
    }

    return null;
  }
}
