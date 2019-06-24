package com.marand.thinkmed.medications;

import java.util.Locale;
import java.util.ResourceBundle;

import com.marand.thinkmed.api.core.Dictionary;
import com.marand.thinkmed.api.core.GrammaticalGender;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Mitja Lapajne
 */
public class MedicationsTestDictionary implements Dictionary
{
  private  static final String BUNDLE_BASE_NAME = "MedicationsTestDictionary";
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_BASE_NAME);

  @Override
  public String getEntry(final String key, final GrammaticalGender gender, final Locale locale)
  {
    if (StringUtils.isBlank(key))
    {
      return null;
    }
    else
    {
      final ResourceBundle bundle = locale != null ? getBundle(locale) : BUNDLE;
      return bundle.getString(key);
    }
  }

  private static ResourceBundle getBundle(final Locale locale)
  {
    return ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
  }
}
