package com.marand.thinkmed.medications.business;

import java.util.Locale;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;

/**
 * @author Klavdij Lapajne
 */
public interface LabelDisplayValuesProvider
{
  String getPrescribedByString(String composerName, Locale locale);

  String getPreparedByString(String userName, Locale locale);

  String getTherapyDisplayValueForPerfusionSyringeLabel(final TherapyDto therapy, final Locale locale);
}
