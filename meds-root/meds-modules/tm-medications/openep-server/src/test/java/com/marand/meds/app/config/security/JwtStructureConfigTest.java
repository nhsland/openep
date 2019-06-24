package com.marand.meds.app.config.security;

import java.util.List;

import org.junit.Test;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

/**
 * @author Nejc Korasa
 */

public class JwtStructureConfigTest
{
  @Test
  public void getAuthClaimsPaths()
  {
    final JwtStructureConfig jwtConf = new JwtStructureConfig();
    jwtConf.setAuthClaimsPaths("this,is,path,one;and,this,is,path,two");
    final List<List<String>> authClaimsPaths = jwtConf.getAuthClaimsPaths();

    assertEquals(2, authClaimsPaths.size());
    assertEquals("this,is,path,one", authClaimsPaths.get(0).stream().collect(joining(",")));
    assertEquals("and,this,is,path,two", authClaimsPaths.get(1).stream().collect(joining(",")));
  }
}