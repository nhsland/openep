package com.marand.meds.app.config.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marand.thinkmed.medications.TherapyAuthorityEnum;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.StaticAuthUserProvider;
import com.marand.thinkmed.request.user.UserDto;
import com.marand.thinkmed.request.user.UserProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Nejc Korasa
 */

@Profile("no-auth")
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class DemoSecurityConfig extends WebSecurityConfigurerAdapter
{
  @Value("${auth.no-auth.userid:id}")
  private String userId;

  @Value("${auth.no-auth.username:test}")
  private String username;

  @PostConstruct
  public void initialize()
  {
    final UserProvider userProvider = new UserProvider()
    {
      @Override
      public UserDto createUser(final Authentication auth)
      {
        return (UserDto)auth.getPrincipal();
      }

      @Override
      public boolean supports(final Authentication auth)
      {
        return auth.getPrincipal() instanceof UserDto;
      }
    };

    RequestUser.init(userProvider, new StaticAuthUserProvider());
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception
  {
    http
        .cors().and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .headers().frameOptions().disable().and()
        .httpBasic().disable()
        .authorizeRequests()
        .anyRequest().authenticated()
        .and()
        .httpBasic()
        .and()
        .addFilterAt(new NoAuthenticationFilter(), BasicAuthenticationFilter.class);

    http.csrf().disable();

  }

  private class NoAuthenticationFilter extends OncePerRequestFilter
  {

    @Override
    protected void doFilterInternal(
        final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
        throws ServletException, IOException
    {

      final UserDto userDto = new UserDto(userId, username, username, Arrays.stream(TherapyAuthorityEnum.values())
          .filter(a -> a != TherapyAuthorityEnum.MEDS_USER_WITNESSING_REQUIRED)
          .map(a -> "ROLE_" + a.getCode()).map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList())
      );

      SecurityContextHolder.clearContext();
      SecurityContextHolder.getContext().setAuthentication(new TicketAuth("ticket", userDto));

      filterChain.doFilter(request, response);
    }
  }

  private static class TicketAuth extends AbstractAuthenticationToken
  {
    private final String credentials;
    private final Object principal;

    private TicketAuth(final String ticket, final UserDto userInfo)
    {
      super(new ArrayList<>(userInfo.getAuthorities()));

      principal = userInfo;
      credentials = ticket;
      setAuthenticated(true);
    }

    @Override
    public String getCredentials()
    {
      return credentials;
    }

    @Override
    public Object getPrincipal()
    {
      return principal;
    }
  }
}