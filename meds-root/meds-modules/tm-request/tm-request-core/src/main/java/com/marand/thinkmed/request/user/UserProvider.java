package com.marand.thinkmed.request.user;

import org.springframework.security.core.Authentication;

/**
 * @author Nejc Korasa
 */

@FunctionalInterface
@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
public interface UserProvider
{
  UserDto createUser(Authentication auth);

  default boolean supports(final Authentication auth)
  {
    return true;
  }
}
