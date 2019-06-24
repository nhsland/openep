package com.marand.thinkmed.medications.document;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentTypeEnum;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public interface TherapyDocumentProviderPlugin
{
  List<TherapyDocumentDto> getTherapyDocuments(String patientId, Integer numberOfResults, DateTime when, Locale locale);

  TherapyDocumentDto getTherapyDocument(String patientId, String contentId, Locale locale);

  Collection<TherapyDocumentTypeEnum> getPluginDocumentTypes();
}
