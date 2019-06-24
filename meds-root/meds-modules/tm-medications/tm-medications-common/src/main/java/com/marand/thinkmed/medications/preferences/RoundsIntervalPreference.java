package com.marand.thinkmed.medications.preferences;

import com.marand.maf.core.prefs.AbstractPreference;
import com.marand.maf.core.prefs.MafPrefsStorageType;
import com.marand.maf.core.prefs.ObjectStringConverter;
import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Mitja Lapajne
 */
public class RoundsIntervalPreference extends AbstractPreference<RoundsIntervalDto>
{
  public RoundsIntervalPreference(
      final String key,
      final MafPrefsStorageType storageType,
      final boolean required,
      final RoundsIntervalDto defaultValue)
  {
    super(key, storageType, required, new RoundsIntervalConverter(), defaultValue);
  }

  private static class RoundsIntervalConverter implements ObjectStringConverter<RoundsIntervalDto>
  {
    @Override
    public RoundsIntervalDto convertFromString(final String stringValue)
    {
      return StringUtils.isBlank(stringValue) ? null : JsonUtil.fromJson(stringValue, RoundsIntervalDto.class);
    }

    @Override
    public String convertToString(final RoundsIntervalDto value)
    {
      return value == null ? "" : JsonUtil.toJson(value);
    }
  }
}