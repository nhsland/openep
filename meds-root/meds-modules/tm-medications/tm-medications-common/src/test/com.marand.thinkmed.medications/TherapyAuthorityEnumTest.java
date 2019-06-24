package com.marand.thinkmed.medications;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

public class TherapyAuthorityEnumTest
{
  @Test
  public void getMatchedAuthoritiesMap()
  {
    final Set<String> authorities = new HashSet<>(Arrays.asList(
        "role_meds_grid_view",
        "role_meds_mar_view",
        "role_meds_summary_view"
    ));

    final Map<String, Boolean> matchedAuthoritiesMap = TherapyAuthorityEnum.getMatchedAuthoritiesByLowerCase(authorities);
    assertEquals(3, matchedAuthoritiesMap.values().stream().filter(Boolean::booleanValue).count());
    assertTrue(matchedAuthoritiesMap.get(TherapyAuthorityEnum.MEDS_GRID_VIEW.getClientSideName()));
    assertTrue(matchedAuthoritiesMap.get(TherapyAuthorityEnum.MEDS_MAR_VIEW.getClientSideName()));
    assertTrue(matchedAuthoritiesMap.get(TherapyAuthorityEnum.MEDS_SUMMARY_VIEW.getClientSideName()));
  }
}
