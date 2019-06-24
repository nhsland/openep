package com.marand.thinkmed.medications.business.impl;

import java.util.Locale;

import com.marand.thinkmed.medications.MedicationLevelEnum;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Nejc Korasa
 */

public class ReleaseDetailsDisplayProviderTest
{
  private final ReleaseDetailsDisplayProvider releaseDetailsDisplayProvider = new ReleaseDetailsDisplayProvider();

  @Test
  public void testMedicationReleaseDetailsDisplayMR24()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto();
    final String medicationName = "Medication name";
    medication.setName(medicationName);
    medicationData.setMedication(medication);

    medicationData.setProperty(new MedicationPropertyDto(1L, MedicationPropertyType.MODIFIED_RELEASE_TIME, "MR", "24"));

    assertEquals("MR24", releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(medicationData, new Locale("en")));
  }

  @Test
  public void testTherapyReleaseDetailsDisplayMR24()
  {
    assertEquals("MR24",
                 releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(new ReleaseDetailsDto(
                     ReleaseType.MODIFIED_RELEASE,
                     24), null));
  }

  @Test
  public void testTherapyReleaseDetailsDisplayMR()
  {
    assertEquals("MR",
                 releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(new ReleaseDetailsDto(
                     ReleaseType.MODIFIED_RELEASE,
                     null), null));
  }

  @Test
  public void testTherapyReleaseDetailsDisplayGR()
  {
    assertEquals("GR",
                 releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(new ReleaseDetailsDto(
                     ReleaseType.GASTRO_RESISTANT,
                     null), null));
  }

  @Test
  public void testMedicationReleaseDetailsDisplayMR()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto();
    final String medicationName = "Medication name";
    medication.setName(medicationName);
    medicationData.setMedication(medication);

    medicationData.setMedicationLevel(MedicationLevelEnum.VMP);

    medicationData.setProperty(new MedicationPropertyDto(1L, MedicationPropertyType.MODIFIED_RELEASE, "MR", null));

    assertEquals("MR", releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(medicationData, new Locale("en")));
  }

  @Test
  public void testMedicationReleaseDetailsDisplayVtmMR()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto();
    final String medicationName = "Medication name";
    medication.setName(medicationName);
    medicationData.setMedication(medication);

    medicationData.setMedicationLevel(MedicationLevelEnum.VTM);

    medicationData.setProperty(new MedicationPropertyDto(1L, MedicationPropertyType.MODIFIED_RELEASE, "MR", null));

    assertNull(releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(medicationData, new Locale("en")));
  }

  @Test
  public void testMedicationReleaseDetailsDisplayGR()
  {
    final MedicationDataDto medicationData = new MedicationDataDto();
    final MedicationDto medication = new MedicationDto();
    final String medicationName = "Medication name";
    medication.setName(medicationName);
    medicationData.setMedication(medication);

    medicationData.setProperty(new MedicationPropertyDto(1L, MedicationPropertyType.GASTRO_RESISTANT, "MR", null));

    assertEquals("GR", releaseDetailsDisplayProvider.buildReleaseDetailsDisplay(medicationData, new Locale("en")));
  }

}
