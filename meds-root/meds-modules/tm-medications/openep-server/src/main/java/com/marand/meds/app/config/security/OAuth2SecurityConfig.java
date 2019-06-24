package com.marand.meds.app.config.security;

import javax.annotation.PostConstruct;

import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.StaticAuthUserProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * @author Nejc Korasa
 */

@Profile("oauth2-auth")
@Configuration
@EnableResourceServer
@Import(JwtStructureConfig.class)
public class OAuth2SecurityConfig
{
  private final JwtStructureConfig jwtStructureConfig;

  public OAuth2SecurityConfig(final JwtStructureConfig jwtStructureConfig)
  {
    this.jwtStructureConfig = jwtStructureConfig;
  }

  @PostConstruct
  public void initialize()
  {
    RequestUser.init(jwtStructureConfig.buildUserProvider(), new StaticAuthUserProvider());
  }

  @SuppressWarnings("PackageVisibleInnerClass")
  @Configuration
  static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter
  {
    @Override
    public void configure(final WebSecurity web)
    {
      WebSecurityConfigurator.configureWebSecurity(web);
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception
    {
      http
          .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
          .and()
          .httpBasic().disable()
          .csrf().disable()
          .antMatcher("/**").authorizeRequests().anyRequest().authenticated();
    }
  }
}