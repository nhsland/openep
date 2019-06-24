package com.marand.meds.app.config.security;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import com.marand.thinkmed.request.user.OAuth2UserProvider;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Nejc Korasa
 */

public class JwtStructureConfig
{
  private static final Logger LOG = LoggerFactory.getLogger(JwtStructureConfig.class);

  @Value("${security.oauth2.authorities-claims-path:}")
  private String authClaimsPaths;

  @Value("${security.oauth2.authorities-claim-prefix:}")
  private String authClaimsPrefix;

  @Value("${security.oauth2.user-id-claim:sub}")
  private String userIdClaim;

  @Value("${security.oauth2.user-full-name-claim:name}")
  private String userFullNameClaim;

  @Value("${security.oauth2.username-claim:preferred_username}")
  private String usernameClaim;

  public void setAuthClaimsPaths(final String authClaimsPaths)
  {
    this.authClaimsPaths = authClaimsPaths;
  }

  @PostConstruct
  public void init()
  {
    LOG.info(
        "OAuth2 JWT structure config... Auth claim path: {}, User id claim: {}, User name claim: {}, Username claim {}",
        getAuthClaimsPaths(),
        userIdClaim,
        userFullNameClaim,
        usernameClaim);
  }

  public OAuth2UserProvider buildUserProvider()
  {
    return new OAuth2UserProvider(getAuthClaimsPaths(), authClaimsPrefix, userIdClaim, userFullNameClaim, usernameClaim);
  }

  public List<List<String>> getAuthClaimsPaths()
  {
    if (authClaimsPaths.isEmpty())
    {
      return null;
    }

    final List<String> paths = Arrays.asList(authClaimsPaths.split(";"));
    return paths.stream().map(p -> (List<String>)Arrays.asList(p.split(","))).collect(Collectors.toList());
  }

  public String getAuthClaimsPrefix()
  {
    return authClaimsPrefix;
  }

  public String getUserIdClaim()
  {
    return userIdClaim;
  }

  public String getUserFullNameClaim()
  {
    return userFullNameClaim;
  }

  public String getUsernameClaim()
  {
    return usernameClaim;
  }
}
