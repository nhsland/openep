package com.marand.thinkmed.medications.therapy.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.jgroups.util.Util.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyIdUtilTest
{
  @Test
  public void testIsValidTherapyIdNull()
  {
    assertFalse(TherapyIdUtils.isValidTherapyId(null));
  }

  @Test
  public void testIsValidTherapyIdNoPipe()
  {
    assertFalse(TherapyIdUtils.isValidTherapyId("abc"));
  }

  @Test
  public void testIsValidTherapyIdNotAUUID()
  {
    assertFalse(TherapyIdUtils.isValidTherapyId("abc|Medication instruction"));
  }

  @Test
  public void testIsValidTherapyIdValid()
  {
    assertTrue(TherapyIdUtils.isValidTherapyId("123e4567-e89b-12d3-a456-426655440000|Medication instruction"));
  }
}
