package com.marand.thinkmed.request.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings({"StaticVariableUsedBeforeInitialization", "StaticVariableMayNotBeInitialized"})
public class RequestUser
{
  private static final List<UserProvider> userProviders = new ArrayList<>();

  private RequestUser() { }

  public static void init(final UserProvider... userProviders)
  {
    RequestUser.userProviders.addAll(Arrays.asList(userProviders));
  }

  public static UserDto getUser()
  {
    final Authentication auth = getAuthentication();
    return userProviders.stream()
        .filter(p -> p.supports(auth))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No user provider for Authentication :" + auth + " found"))
        .createUser(auth);
  }

  public static String getId()
  {
    return getUser().getId();
  }

  public static String getFullName()
  {
    return getUser().getFullName();
  }

  private static Authentication getAuthentication()
  {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  public static Collection<GrantedAuthority> getAuthorities()
  {
    return getUser().getAuthorities();
  }
}
