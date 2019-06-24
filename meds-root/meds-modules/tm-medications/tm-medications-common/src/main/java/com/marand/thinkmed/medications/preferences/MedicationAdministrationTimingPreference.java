package com.marand.thinkmed.medications.preferences;

import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.prefs.AbstractPreference;
import com.marand.maf.core.prefs.MafPrefsStorageType;
import com.marand.maf.core.prefs.ObjectStringConverter;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Mitja Lapajne
 */
public class MedicationAdministrationTimingPreference extends AbstractPreference<AdministrationTimingDto>
{
  public MedicationAdministrationTimingPreference(
      final String key,
      final MafPrefsStorageType storageType,
      final boolean required,
      final AdministrationTimingDto defaultValue)
  {
    super(key, storageType, required, new MedicationAdministrationTimingConverter(), defaultValue);
  }

  private static class MedicationAdministrationTimingConverter implements ObjectStringConverter<AdministrationTimingDto>
  {
    @Override
    public AdministrationTimingDto convertFromString(final String stringValue)
    {
      return StringUtils.isBlank(stringValue) ? null : JsonUtil.fromJson(stringValue, AdministrationTimingDto.class);
    }

    @Override
    public String convertToString(final AdministrationTimingDto value)
    {
      return value == null ? "" : JsonUtil.toJson(value);
    }
  }
}