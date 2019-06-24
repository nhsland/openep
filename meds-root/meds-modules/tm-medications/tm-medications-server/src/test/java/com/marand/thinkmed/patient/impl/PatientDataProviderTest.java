package com.marand.thinkmed.patient.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.connector.MedicationsConnector;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayDto;
import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.patient.PatientDataProvider;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.anyCollection;

/**
 * @author Nejc Korasa
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class PatientDataProviderTest
{
  @InjectMocks
  private final PatientDataProvider patientDataProvider = new PatientDataProviderImpl();

  @Mock
  private MedicationsConnector medicationsConnector;

  @Before
  public void mock()
  {
    final List<PatientDisplayWithLocationDto> returnList = new ArrayList<>();
    returnList.add(
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto("patient1", "patient1", DateTime.now().minusYears(10), Gender.FEMALE, null),
            "careProvider",
            "roomAndBed"));

    returnList.add(
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto("patient2", "patient2", DateTime.now().minusYears(70), Gender.MALE, null),
            "careProvider",
            "roomAndBed"));

    returnList.add(
        new PatientDisplayWithLocationDto(
            new PatientDisplayDto("patient3", "patient3", DateTime.now().minusYears(30), Gender.NOT_KNOWN, null),
            "careProvider",
            "roomAndBed"));

    //noinspection unchecked
    Mockito
        .when(medicationsConnector.getPatientDisplaysWithLocation(anyCollection(), anyCollection()))
        .thenReturn(returnList);
  }

  @Test
  public void testGetPatientDemographicDataWithImagePath()
  {
    final Map<String, PatientDisplayWithLocationDto> map = patientDataProvider.getPatientDisplayWithLocationMap(
        Lists.newArrayList("patient1", "patient2"));

    Assert.assertTrue(map.values().stream().allMatch(v -> v.getPatientDisplayDto().getPatientImagePath() != null));
    Assert.assertTrue(map.get("patient1").getPatientDisplayDto().getPatientImagePath().contains("patient-06y-f.png"));
    Assert.assertTrue(map.get("patient1").getPatientDisplayDto().getPatientImagePath().contains("patient-06y-f.png"));
    Assert.assertTrue(map.get("patient3").getPatientDisplayDto().getPatientImagePath().contains("patient_anonymous_48.png"));
  }
}