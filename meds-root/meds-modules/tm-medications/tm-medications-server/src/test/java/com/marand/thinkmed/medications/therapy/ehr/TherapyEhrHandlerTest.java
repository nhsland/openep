package com.marand.thinkmed.medications.therapy.ehr;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Nejc Korasa
 */

@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyEhrHandlerTest
{
  @InjectMocks
  private final TherapyEhrHandler therapyEhrHandler = new TherapyEhrHandler();

  @Mock
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Test
  public void testGetOriginalTherapyId()
  {
    final InpatientPrescription originComposition = MedicationsTestUtils.buildTestInpatientPrescription(
        "uid1::1",
        null,
        null,
        "Medication order");

    final InpatientPrescription composition = MedicationsTestUtils.buildTestInpatientPrescription(
        "uid2::1",
        null,
        null,
        "Medication order");

    composition.getLinks().add(LinksEhrUtils.createLink(originComposition.getUid(), "origin", EhrLinkType.ORIGIN));

    final String originalTherapyId = therapyEhrHandler.getOriginalTherapyId(composition);
    assertEquals("uid1|Medication order", originalTherapyId);
  }

}
