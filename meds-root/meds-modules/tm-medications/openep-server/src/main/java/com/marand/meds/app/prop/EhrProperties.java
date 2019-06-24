package com.marand.meds.app.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Boris Marn.
 */
@ConfigurationProperties(prefix = "ehr")
public class EhrProperties
{
  private String url;
  private String commiter;
  private String userId;
  private String password;
  private String subjectNamespace;

  public String getUrl()
  {
    return url;
  }

  public void setUrl(final String url)
  {
    this.url = url;
  }

  public String getCommiter()
  {
    return commiter;
  }

  public void setCommiter(final String commiter)
  {
    this.commiter = commiter;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(final String userId)
  {
    this.userId = userId;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(final String password)
  {
    this.password = password;
  }

  public String getSubjectNamespace()
  {
    return subjectNamespace;
  }

  public void setSubjectNamespace(final String subjectNamespace)
  {
    this.subjectNamespace = subjectNamespace;
  }
}
