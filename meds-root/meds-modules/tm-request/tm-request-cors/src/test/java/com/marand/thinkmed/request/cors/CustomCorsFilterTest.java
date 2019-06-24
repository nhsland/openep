package com.marand.thinkmed.request.cors;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

public class CustomCorsFilterTest
{

  @Test
  public void wildcardToRegexSuffix()
  {
    final String wildcard = "my.domain.com*";
    final CustomCorsFilter ccf = new CustomCorsFilter();
    final String regex = ccf.buildRegexFromWildcards(wildcard);

    assertTrue("my.domain.com:8080".matches(regex));
    assertTrue("my.domain.com".matches(regex));
    assertFalse("my.not.the.same.domain.com:8080".matches(regex));
  }

  @Test
  public void wildcardToRegexPrefixAndSuffix()
  {
    final String wildcard = "*.my.domain.com*";
    final CustomCorsFilter ccf = new CustomCorsFilter();
    final String regex = ccf.buildRegexFromWildcards(wildcard);

    assertTrue("sub.my.domain.com:8080".matches(regex));
    assertTrue("sub.my.domain.com".matches(regex));

    assertFalse("sub.my.wrong.domain.com:8080".matches(regex));
  }
}
