package com.marand.meds.rest.info;

import com.marand.meds.app.config.security.OAuth2ClientConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nejc Korasa
 */

@RestController
@RequestMapping("/info")
public class InfoController
{
  private OAuth2ClientConfigProvider oAuth2ClientConfigProvider;

  @Autowired
  public void setOAuth2ClientConfigProvider(final OAuth2ClientConfigProvider oAuth2ClientConfigProvider)
  {
    this.oAuth2ClientConfigProvider = oAuth2ClientConfigProvider;
  }

  @GetMapping(value = "/auth/accesstokenuri")
  public String getAccessTokeUri()
  {
    return oAuth2ClientConfigProvider.get().getTokenUri();
  }
}
