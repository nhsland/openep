package com.marand.thinkmed.medications.ehr.utils;

import java.util.Arrays;
import java.util.List;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.ehr.model.Medication;
import com.marand.thinkmed.medications.ehr.model.MedicationCategory;
import org.junit.Test;
import org.openehr.jaxb.rm.DvText;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

public class MedicationsEhrUtilsTest
{
  @Test
  public void getMedicationIdsAdHoxMixture()
  {
    final Medication medication = buildMedication(MedicationCategory.AD_HOC_MIXTURE.getDvCodedText(), null);
    medication.setConstituent(Arrays.asList(
        buildMedication(null, DataValueUtils.getLocalCodedText("1", "name 1")),
        buildMedication(null, DataValueUtils.getLocalCodedText("2", "name 2")),
        buildMedication(null, DataValueUtils.getText("name 3"))
    ));

    final List<Long> medicationIds = MedicationsEhrUtils.getMedicationIds(medication);
    assertEquals(2, medicationIds.size());
    assertTrue(medicationIds.contains(1L));
    assertTrue(medicationIds.contains(2L));
  }

  @Test
  public void getMedicationIdsUncodedMedication()
  {
    final Medication medication = buildMedication(null, DataValueUtils.getText("uncoded medication"));

    final List<Long> medicationIds = MedicationsEhrUtils.getMedicationIds(medication);
    assertEquals(0, medicationIds.size());
  }

  @Test
  public void getMedicationIdsCodedMedication()
  {
    final Medication medication = buildMedication(null, DataValueUtils.getLocalCodedText("1","coded medication"));

    final List<Long> medicationIds = MedicationsEhrUtils.getMedicationIds(medication);
    assertEquals(1, medicationIds.size());
    assertTrue(medicationIds.contains(1L));
  }

  private Medication buildMedication(final DvText category, final DvText componentName)
  {
    final Medication medication = new Medication();
    medication.setCategory(category);
    medication.setComponentName(componentName);
    return medication;
  }
}