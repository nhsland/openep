package com.marand.thinkmed.request.user;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;

/**
 * @author Nejc Korasa
 */

public class KeycloakUserProvider implements UserProvider
{
  @Override
  public UserDto createUser(final Authentication auth)
  {
    if (supports(auth))
    {
      //noinspection rawtypes
      final AccessToken token = ((KeycloakPrincipal)auth.getPrincipal()).getKeycloakSecurityContext().getToken();
      return new UserDto(token.getPreferredUsername(), token.getPreferredUsername(), token.getName(), auth.getAuthorities());
    }
    throw new AuthenticationServiceException("Could not cast to keycloak token!");
  }

  @Override
  public boolean supports(final Authentication auth)
  {
    return auth instanceof KeycloakAuthenticationToken;
  }
}
