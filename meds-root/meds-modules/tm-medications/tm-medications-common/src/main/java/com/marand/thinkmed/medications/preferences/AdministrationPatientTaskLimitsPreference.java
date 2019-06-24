package com.marand.thinkmed.medications.preferences;

import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.prefs.AbstractPreference;
import com.marand.maf.core.prefs.MafPrefsStorageType;
import com.marand.maf.core.prefs.ObjectStringConverter;
import com.marand.thinkmed.medications.dto.AdministrationPatientTaskLimitsDto;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Klavdij Lapajne
 */
public class AdministrationPatientTaskLimitsPreference extends AbstractPreference<AdministrationPatientTaskLimitsDto>
{
  public AdministrationPatientTaskLimitsPreference(
      final String key,
      final MafPrefsStorageType storageType,
      final boolean required,
      final AdministrationPatientTaskLimitsDto defaultValue)
  {
    super(key, storageType, required, new AdministrationPatientTaskLimitsConverter(), defaultValue);
  }

  private static class AdministrationPatientTaskLimitsConverter
      implements ObjectStringConverter<AdministrationPatientTaskLimitsDto>
  {
    @Override
    public AdministrationPatientTaskLimitsDto convertFromString(final String stringValue)
    {
      return StringUtils.isBlank(stringValue)
             ? null
             : JsonUtil.fromJson(stringValue, AdministrationPatientTaskLimitsDto.class);
    }

    @Override
    public String convertToString(final AdministrationPatientTaskLimitsDto value)
    {
      return value == null ? "" : JsonUtil.toJson(value);
    }
  }
}