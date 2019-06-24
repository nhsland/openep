package com.marand.thinkmed.elmdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Vid Kumse
 */
@Component
@ConfigurationProperties(prefix="elmdoc")
public class ElmdocProperties
{
  private String elmdocRestUri;
  private String username;
  private String password;

  public String getElmdocRestUri()
  {
    return elmdocRestUri;
  }

  public void setElmdocRestUri(final String elmdocRestUri)
  {
    this.elmdocRestUri = elmdocRestUri;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(final String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(final String password)
  {
    this.password = password;
  }
}
