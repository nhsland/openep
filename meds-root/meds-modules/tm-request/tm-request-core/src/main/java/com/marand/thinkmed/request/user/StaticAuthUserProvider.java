package com.marand.thinkmed.request.user;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;

/**
 * @author Nejc Korasa
 */

public class StaticAuthUserProvider implements UserProvider
{
  @Override
  public UserDto createUser(final Authentication auth)
  {
    if (supports(auth))
    {
      final StaticAuth staticAuth = (StaticAuth)auth;
      return new UserDto(staticAuth.getUsername(), staticAuth.getUsername(), auth.getName(), auth.getAuthorities());
    }
    throw new AuthenticationServiceException("Could not cast to static authentication object!");
  }

  @Override
  public boolean supports(final Authentication auth)
  {
    return auth instanceof StaticAuth;
  }
}
