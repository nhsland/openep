package com.marand.meds.app.config.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

/**
 * @author Nejc Korasa
 */

public interface WebSecurityConfigurator
{
  static void configureWebSecurity(final WebSecurity web)
  {
    web
        .ignoring()
        .antMatchers(

            // EHR Events
            "/ehrevents/**",

            // Endpoint to retrieve service configs (auth server access token uri)
            "/info/**",

            // Static files
            "/ui/**",
            "/resources/**",
            "/htmldocument/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v2/api-docs",
            "/medications/externalcallview/**",
            "/medications/tm.application.View**"
        )
        .mvcMatchers(HttpMethod.OPTIONS, "/**");
  }
}
