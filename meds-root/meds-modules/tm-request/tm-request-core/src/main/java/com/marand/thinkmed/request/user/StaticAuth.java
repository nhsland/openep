package com.marand.thinkmed.request.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Nejc Korasa
 */

public class StaticAuth implements Authentication
{
  private final String username;
  private final String name;
  private final String id;
  private final Collection<String> authorities = new ArrayList<>();

  public StaticAuth(final String username, final String name, final String id)
  {
    this.username = username;
    this.name = name;
    this.id = id;
  }

  public void addAuthorities(final Collection<String> authorities)
  {
    this.authorities.addAll(authorities);
  }

  public String getUsername()
  {
    return username;
  }

  public String getId()
  {
    return id;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities()
  {
    return authorities.stream().map(a -> (GrantedAuthority)() -> a).collect(Collectors.toList());
  }

  @Override
  public Object getCredentials()
  {
    return null;
  }

  @Override
  public Object getDetails()
  {
    return null;
  }

  @Override
  public String getPrincipal()
  {
    return username;
  }

  @Override
  public boolean isAuthenticated()
  {
    return true;
  }

  @Override
  public void setAuthenticated(final boolean b) throws IllegalArgumentException
  {

  }
}
