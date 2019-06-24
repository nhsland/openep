package com.marand.thinkmed.medications.document.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.document.TherapyDocumentProviderPlugin;
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentDto;
import com.marand.thinkmed.medications.api.internal.dto.document.TherapyDocumentTypeEnum;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthDocumentType;
import com.marand.thinkmed.medications.mentalhealth.impl.ConsentFormFromEhrProvider;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

/**
 * @author Nejc Korasa
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class MentalHealthDocumentProviderPluginTest
{
  @Mock
  private ConsentFormFromEhrProvider consentFormFromEhrProvider;

  @InjectMocks
  private final TherapyDocumentProviderPlugin mentalHealthDocumentProviderPlugin = new MentalHealthDocumentProviderPluginImpl();

  private void setUpMocks(final int numberOfMentalHealthForms)
  {
    Mockito.reset();
    final List<MentalHealthDocumentDto> list = new ArrayList<>();
    for (int i = 0; i < numberOfMentalHealthForms; i++)
    {
      list.add(new MentalHealthDocumentDto(
          "composition",
          new DateTime(),
          new NamedExternalDto("creator", "creator"),
          "patientId",
          new NamedExternalDto("careProvider", "careProvider"),
          i % 2 == 0 ? MentalHealthDocumentType.T2 : MentalHealthDocumentType.T3,
          100,
          Collections.emptyList(),
          Collections.emptyList()));
    }

    Mockito.when(consentFormFromEhrProvider.getMentalHealthDocuments(anyString(), any(Interval.class), anyInt()))
        .thenReturn(list);

    Mockito.when(consentFormFromEhrProvider.getMentalHealthDocument(anyString(), anyString()))
        .thenReturn(list.isEmpty() ? null : list.get(0));
  }

  @Test
  public void testGetTherapyDocumentsNoDocuments()
  {
    setUpMocks(0);
    final List<TherapyDocumentDto> documents = mentalHealthDocumentProviderPlugin.getTherapyDocuments(
        "1",
        10,
        new DateTime(2015, 1, 15, 12, 0),
        new Locale("en"));

    Assert.assertEquals(0, documents.size());
  }

  @Test
  public void testGetTherapyDocuments()
  {
    setUpMocks(10);
    final List<TherapyDocumentDto> documents = mentalHealthDocumentProviderPlugin.getTherapyDocuments(
        "1",
        10,
        new DateTime(2015, 1, 15, 12, 0),
        new Locale("en"));

    Assert.assertEquals(TherapyDocumentTypeEnum.T2, documents.get(0).getDocumentType());
    Assert.assertEquals(new NamedExternalDto("careProvider", "careProvider"), documents.get(0).getCareProvider());
    Assert.assertEquals(new NamedExternalDto("creator", "creator"), documents.get(0).getCreator());

    Assert.assertEquals(TherapyDocumentTypeEnum.T3, documents.get(1).getDocumentType());
    Assert.assertEquals(new NamedExternalDto("careProvider", "careProvider"), documents.get(1).getCareProvider());
    Assert.assertEquals(new NamedExternalDto("creator", "creator"), documents.get(1).getCreator());
  }

  @Test
  public void testGetTherapyDocument()
  {
    setUpMocks(1);
    final TherapyDocumentDto document = mentalHealthDocumentProviderPlugin.getTherapyDocument(
        "1",
        "1",
        new Locale("en"));

    Assert.assertEquals(TherapyDocumentTypeEnum.T2, document.getDocumentType());
    Assert.assertEquals(new NamedExternalDto("careProvider", "careProvider"), document.getCareProvider());
    Assert.assertEquals(new NamedExternalDto("creator", "creator"), document.getCreator());
  }
}