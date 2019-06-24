package com.marand.thinkmed.medications.business.mapper;

import java.util.Locale;

import com.marand.thinkmed.medications.business.impl.ReleaseDetailsDisplayProvider;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

/**
 * @author Nejc Korasa
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class MedicationDataDtoMapperTest
{
  @Spy
  private final MedicationDataDtoMapper medicationDataDtoMapper = new MedicationDataDtoMapper();
  private final TherapyDisplayProvider therapyDisplayProvider = Mockito.mock(TherapyDisplayProvider.class);
  private final ReleaseDetailsDisplayProvider releaseDetailsDisplayProvider = Mockito.mock(ReleaseDetailsDisplayProvider.class);

  @Before
  public void init()
  {
    medicationDataDtoMapper.setTherapyDisplayProvider(therapyDisplayProvider);
    medicationDataDtoMapper.setReleaseDetailsDisplayProvider(releaseDetailsDisplayProvider);

    Mockito.when(therapyDisplayProvider.getMedicationWithGenericDisplay(any(), any())).thenReturn("Generic name");
  }

  @Test
  public void testBuildTitle()
  {
    Mockito.when(releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(any(MedicationDataDto.class), any())).thenReturn(null);

    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto();
    final String medicationName = "Medication name";
    medication.setName(medicationName);
    medicationData.setMedication(medication);

    final String title = medicationDataDtoMapper.buildTreeNodeTitle(medicationData, false, new Locale("en"));
    assertEquals(medicationName, title);
  }

  @Test
  public void testBuildTitleModifiedRelease()
  {
    Mockito.when(releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(any(MedicationDataDto.class), any())).thenReturn("MR");

    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto();
    final String medicationName = "Medication name";
    medication.setName(medicationName);
    medicationData.setMedication(medication);

    medicationData.setProperty(new MedicationPropertyDto(1L, MedicationPropertyType.MODIFIED_RELEASE, "MR", "24"));

    final String title = medicationDataDtoMapper.buildTreeNodeTitle(medicationData, false, new Locale("en"));
    assertEquals(medicationName + " - MR", title);
  }

  @Test
  public void testBuildTitleModifiedReleaseAndGeneric()
  {
    Mockito.when(releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(any(MedicationDataDto.class), any())).thenReturn("GR");

    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto();
    medication.setGenericName("Generic");
    final String medicationName = "Medication name";
    medication.setName(medicationName);
    medicationData.setMedication(medication);

    medicationData.setProperty(new MedicationPropertyDto(1L, MedicationPropertyType.MODIFIED_RELEASE, "MR", "24"));

    final String title = medicationDataDtoMapper.buildTreeNodeTitle(medicationData, true, new Locale("en"));
    assertEquals("Generic name" + " - GR", title);
  }
}
