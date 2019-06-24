package com.marand.thinkmed.medications.warnings.additional.allergies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.marand.maf.core.Opt;
import com.marand.thinkmed.api.demographics.data.Gender;
import com.marand.thinkmed.medications.allergies.AllergiesHandler;
import com.marand.thinkmed.medications.connector.data.object.AllergiesStatus;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.allergies.CheckNewAllergiesTaskDto;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsDto;
import com.marand.thinkmed.medications.task.MedicationsTasksProvider;
import com.marand.thinkmed.medications.warnings.additional.allergies.AllergiesAdditionalWarningsProvider;
import com.marand.thinkmed.process.service.ProcessService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("TooBroadScope")
@RunWith(SpringJUnit4ClassRunner.class)
public class AllergiesAdditionalWarningsProviderTest
{
  @InjectMocks
  private AllergiesAdditionalWarningsProvider allergiesAdditionalWarnings = new AllergiesAdditionalWarningsProvider();

  @Mock
  private MedicationsTasksProvider medicationsTasksProvider;

  @Mock
  private AllergiesHandler allergiesHandler;

  @Mock
  private ProcessService processService;

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Before
  public void setUp()
  {
    final DateTime date = new DateTime(2017, 2, 3, 0, 0);
    final String patient1 = "patient1";
    final String patient2 = "patient2";

    final List<CheckNewAllergiesTaskDto> allergyTasks = new ArrayList<>();
    final Set<IdNameDto> allergies = new HashSet<>();

    allergies.add(new IdNameDto("1", "allergy1"));
    allergies.add(new IdNameDto("2", "allergy2"));
    allergies.add(new IdNameDto("3", "allergy3"));
    allergies.add(new IdNameDto("4", "allergy4"));
    allergies.add(new IdNameDto("5", "allergy5"));

    allergyTasks.add(new CheckNewAllergiesTaskDto("task1", allergies));

    Mockito.when(medicationsTasksProvider.findNewAllergiesTasks(patient1)).thenReturn(allergyTasks);
    Mockito.when(medicationsTasksProvider.findNewAllergiesTasks(patient2)).thenReturn(Collections.emptyList());
  }

  @Test
  public void testGetAdditionalAllergyWarnings()
  {
    final DateTime date = new DateTime(2017, 2, 3, 0, 0);
    final String patientId = "patient1";
    final Opt<AdditionalWarningsDto> warnings = allergiesAdditionalWarnings.getAdditionalWarnings(
        patientId,
        createPatientData(),
        date,
        new Locale("en_GB"));

    assertNotNull(warnings);

    assertTrue(warnings.get().getTaskIds().contains("task1"));
  }

  @Test
  public void testNoAllergyTasks()
  {
    final DateTime date = new DateTime(2017, 2, 3, 0, 0);
    final String patientId = "patient2";
    final Opt<AdditionalWarningsDto> additionalWarnings = allergiesAdditionalWarnings.getAdditionalWarnings(
        patientId,
        createPatientData(),
        date,
        new Locale("en_GB"));

    assertTrue(additionalWarnings.isAbsent());
  }

  private PatientDataForMedicationsDto createPatientData()
  {
    return new PatientDataForMedicationsDto(
        new DateTime(),
        "Janez Novak",
        10.0,
        10.0,
        Gender.FEMALE,
        Collections.emptyList(),
        AllergiesStatus.NO_KNOWN_ALLERGY,
        Collections.emptyList(),
        null);
  }
}
