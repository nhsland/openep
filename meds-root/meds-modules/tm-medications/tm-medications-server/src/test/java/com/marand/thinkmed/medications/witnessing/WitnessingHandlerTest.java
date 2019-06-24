package com.marand.thinkmed.medications.witnessing;

import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.connector.data.object.AllergiesStatus;
import com.marand.thinkmed.medications.connector.data.object.PatientDataForMedicationsDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.overview.TherapyDayDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class WitnessingHandlerTest
{
  private final WitnessingHandler witnessingHandler;
  private final WitnessingProperties witnessingProperties = Mockito.mock(WitnessingProperties.class);
  private final MedicationsValueHolderProvider medicationsValueHolderProvider = Mockito.mock(MedicationsValueHolderProvider.class);

  public WitnessingHandlerTest()
  {
    witnessingHandler = new WitnessingHandler(witnessingProperties, medicationsValueHolderProvider);
  }

  @Before
  public void init()
  {
    Mockito.reset(medicationsValueHolderProvider, witnessingProperties);

    final MedicationDataDto m1 = new MedicationDataDto();
    m1.setProperty(new MedicationPropertyDto(1L, MedicationPropertyType.WITNESSING, "WITNESSING"));

    final MedicationDataDto m2 = new MedicationDataDto();
    m2.setProperty(new MedicationPropertyDto(2L, MedicationPropertyType.CONTROLLED_DRUG, "CONTROLLED_DRUG"));

    final MedicationDataDto m3 = new MedicationDataDto();

    final MedicationDataDto m4 = new MedicationDataDto();

    // medications mock

    Mockito.when(medicationsValueHolderProvider.getMedicationData(1L)).thenReturn(m1);
    Mockito.when(medicationsValueHolderProvider.getMedicationData(2L)).thenReturn(m2);
    Mockito.when(medicationsValueHolderProvider.getMedicationData(3L)).thenReturn(m3);
    Mockito.when(medicationsValueHolderProvider.getMedicationData(4L)).thenReturn(m4);

    // properties mock

    Mockito.when(witnessingProperties.isEnabled()).thenReturn(true);
    Mockito.when(witnessingProperties.isIvRequired()).thenReturn(true);
    Mockito.when(witnessingProperties.getAgeLimit()).thenReturn(18);
  }

  @Test
  public void testMedicationWitnessRequiredPatientLimit()
  {
    assertTrue(witnessingHandler.isPatientWitnessingRequired(buildPatientData(10)));
  }

  @Test
  public void testMedicationWitnessNotRequiredPatientLimit()
  {
    assertFalse(witnessingHandler.isPatientWitnessingRequired(buildPatientData(20)));
  }

  @Test
  public void testMedicationWitnessRequiredMedication()
  {
    final TherapyDayDto therapyDay = buildTherapyDay(1L, null);
    assertTrue(witnessingHandler.isTherapyWitnessingRequired(therapyDay));
  }

  @Test
  public void testMedicationWitnessRequiredCD()
  {
    final TherapyDayDto therapyDay = buildTherapyDay(2L, null);
    assertTrue(witnessingHandler.isTherapyWitnessingRequired(therapyDay));
  }

  @Test
  public void testMedicationWitnessRequiredIV()
  {
    final TherapyDayDto therapyDay = buildTherapyDay(3L, MedicationRouteTypeEnum.IV);
    assertFalse(witnessingHandler.isTherapyWitnessingRequired(therapyDay));
  }

  @Test
  public void testMedicationWitnessNotRequiredIV()
  {
    Mockito.when(witnessingProperties.isIvRequired()).thenReturn(false);
    final TherapyDayDto therapyDay = buildTherapyDay(3L, MedicationRouteTypeEnum.IV);
    assertFalse(witnessingHandler.isTherapyWitnessingRequired(therapyDay));
  }

  @Test
  public void testMedicationWitnessNotRequired()
  {
    Mockito.when(witnessingProperties.isEnabled()).thenReturn(false);
    final TherapyDayDto therapyDay = buildTherapyDay(3L, MedicationRouteTypeEnum.IV);
    assertFalse(witnessingHandler.isTherapyWitnessingRequired(therapyDay));
  }

  @Test
  public void testMedicationWitnessNotRequired2()
  {
    final TherapyDayDto therapyDay = buildTherapyDay(4L, MedicationRouteTypeEnum.ORAL);
    assertFalse(witnessingHandler.isTherapyWitnessingRequired(therapyDay));
  }

  @Test
  public void testMedicationWitnessNotRequired3()
  {
    final TherapyDayDto therapyDay = buildTherapyDay(4L, null);
    assertFalse(witnessingHandler.isTherapyWitnessingRequired(therapyDay));
  }

  private TherapyDayDto buildTherapyDay(final long medicationId, final MedicationRouteTypeEnum routeType)
  {
    final TherapyDayDto therapyDay = new TherapyDayDto();
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    therapyDay.setTherapy(therapy);

    final MedicationDto medication = new MedicationDto();
    medication.setId(medicationId);
    therapy.setMedication(medication);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(routeType);
    therapy.getRoutes().add(route);
    return therapyDay;
  }

  private PatientDataForMedicationsDto buildPatientData(final int ageInYears)
  {
    return new PatientDataForMedicationsDto(
        DateTime.now().minusYears(ageInYears),
        "Janez Novak",
        null,
        null,
        null,
        null,
        AllergiesStatus.NOT_CHECKED,
        null,
        null);
  }
}
