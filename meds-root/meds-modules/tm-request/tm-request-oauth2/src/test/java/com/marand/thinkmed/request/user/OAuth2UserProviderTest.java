package com.marand.thinkmed.request.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

public class OAuth2UserProviderTest
{
  @Test
  public void extractRolesClaimPrefix()
  {
    final OAuth2UserProvider up = new OAuth2UserProvider(null, "meds_", "sub", "ufn", null);
    final Map<String, Object> claims = new HashMap<>();
    claims.put("meds_role1", true);
    claims.put("MEDS_ROLE2", "true");
    claims.put("MEDS_role3", "TRUE");
    claims.put("MEDS_role4", false);
    claims.put("MEDS_role5", "false");

    final List<String> extractedRoles = Arrays.asList(up.extractRoles(claims));
    assertTrue(extractedRoles.contains("ROLE_meds_role1"));
    assertTrue(extractedRoles.contains("ROLE_MEDS_ROLE2"));
    assertTrue(extractedRoles.contains("ROLE_MEDS_role3"));
    assertFalse(extractedRoles.contains("ROLE_MEDS_role4"));
    assertFalse(extractedRoles.contains("ROLE_MEDS_role5"));
  }

  @Test
  public void extractRolesClaimsPath()
  {
    final OAuth2UserProvider up = new OAuth2UserProvider(Collections.singletonList(Arrays.asList("root", "level1")), null, "sub", "ufn", null);
    final Map<String, Object> claims = new HashMap<>();
    final Map<Object, Object> level1 = new HashMap<>();
    claims.put("root", level1);

    final List<Object> roles = new ArrayList<>();
    level1.put("level1", roles);
    roles.add("role1");
    roles.add("role2");
    roles.add("role3");

    final List<String> extractedRoles = Arrays.asList(up.extractRoles(claims));
    assertTrue(extractedRoles.contains("ROLE_role1"));
    assertTrue(extractedRoles.contains("ROLE_role2"));
    assertTrue(extractedRoles.contains("ROLE_role3"));
  }

  @SuppressWarnings("LocalVariableNamingConvention")
  @Test
  public void extractRolesMultipleClaimsPath()
  {
    final OAuth2UserProvider up = new OAuth2UserProvider(
        Arrays.asList(
            Arrays.asList("root_1", "level1_1"),
            Arrays.asList("root_2", "level1_2")),
        null,
        "sub",
        "ufn",
        null);

    final Map<String, Object> claims = new HashMap<>();

    // first roles path
    final Map<Object, Object> level1_1 = new HashMap<>();
    claims.put("root_1", level1_1);

    final List<Object> roles = new ArrayList<>();
    roles.add("role1");
    roles.add("role2");
    roles.add("role3");
    level1_1.put("level1_1", roles);

    // second roles path
    final Map<Object, Object> level1_2 = new HashMap<>();
    claims.put("root_2", level1_2);

    final List<Object> roles2 = new ArrayList<>();
    roles2.add("role3");
    roles2.add("role4");
    roles2.add("role5");
    level1_2.put("level1_2", roles2);

    final List<String> extractedRoles = Arrays.asList(up.extractRoles(claims));
    assertEquals(5, extractedRoles.size());
    assertTrue(extractedRoles.contains("ROLE_role1"));
    assertTrue(extractedRoles.contains("ROLE_role2"));
    assertTrue(extractedRoles.contains("ROLE_role3"));
    assertTrue(extractedRoles.contains("ROLE_role4"));
    assertTrue(extractedRoles.contains("ROLE_role5"));
  }
}
