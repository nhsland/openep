package com.marand.thinkmed.request.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

/**
 * @author Nejc Korasa
 */

public class OAuth2UserProvider implements UserProvider
{
  private static final String[] STRINGS = new String[0];

  private final List<List<String>> authClaimPaths;
  private final String authClaimPrefix;
  private final String userIdClaim;
  private final String userFullNameClaim;
  private final String usernameClaim;

  public OAuth2UserProvider(
      final List<List<String>> authClaimPaths,
      final String authClaimPrefix,
      final String userIdClaim,
      final String userFullNameClaim,
      final String usernameClaim)
  {
    Preconditions.checkNotNull(userIdClaim, "userIdClaim must be set!");
    Preconditions.checkNotNull(userFullNameClaim, "userFullNameClaim must be set!");
    //noinspection OverlyComplexBooleanExpression
    Preconditions.checkArgument(
        (authClaimPaths == null || authClaimPaths.isEmpty()) ^ (authClaimPrefix == null || authClaimPrefix.isEmpty()),
        "Either authClaimPaths or authClaimPrefix must be set!");

    //noinspection AssignmentOrReturnOfFieldWithMutableType
    this.authClaimPaths = authClaimPaths;
    this.authClaimPrefix = authClaimPrefix;
    this.userIdClaim = userIdClaim;
    this.userFullNameClaim = userFullNameClaim;
    this.usernameClaim = usernameClaim;
  }

  @Override
  public UserDto createUser(final Authentication auth)
  {
    if (supports(auth))
    {
      final Map<String, Object> claims = extractClaims(auth);
      final List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(extractRoles(claims));

      return new UserDto(
          getUserId(claims),
          getUsername(claims),
          getFullName(claims),
          authorityList);
    }
    throw new AuthenticationServiceException("Could not cast to OAuth2 token!");
  }

  public String getUserId(final Map<String, Object> claims)
  {
    final Object userId = claims.get(userIdClaim);
    return userId != null ? userId.toString() : claims.get("sub").toString();
  }

  public String getUsername(final Map<String, Object> claims)
  {
    final Object username = claims.get(usernameClaim);
    return username != null ? username.toString() : null;
  }

  public String getFullName(final Map<String, Object> claims)
  {
    final Object userFullName = claims.get(userFullNameClaim);
    if (userFullName != null)
    {
      return userFullName.toString();
    }
    final Object username = claims.get(usernameClaim);
    if (username != null)
    {
      return username.toString();
    }
    return null;
  }

  @Override
  public boolean supports(final Authentication auth)
  {
    return auth instanceof OAuth2Authentication;
  }

  public static Map<String, Object> extractClaims(final Authentication auth)
  {
    final JsonParser parser = JsonParserFactory.getJsonParser();
    return parser.parseMap(
        JwtHelper.decode(((OAuth2AuthenticationDetails)auth.getDetails()).getTokenValue()).getClaims());
  }

  String[] extractRoles(final Map<String, Object> claims)
  {
    if (authClaimPrefix != null && !authClaimPrefix.isEmpty())
    {
      return claims.keySet()
          .stream()
          .filter(key -> key.toLowerCase().startsWith(authClaimPrefix.toLowerCase()))
          .filter(key -> {

            final Object value = claims.get(key);
            if (value instanceof Boolean)
            {
              return (Boolean)value;
            }
            if (value instanceof String)
            {
              return "true".equals(((String)value).toLowerCase());
            }
            return false;
          })
          .map(key -> "ROLE_" + key)
          .collect(Collectors.toList()).toArray(STRINGS);
    }
    else
    {
      final Set<String> roleList = new HashSet<>();
      for (final List<String> paths : authClaimPaths)
      {
        Map<String, Object> node = claims;

        for (final String element : paths)
        {
          final Object value = node.get(element);
          if (value instanceof Collection)
          {
            ((Collection<String>)value).stream().map(e -> "ROLE_" + e).forEach(roleList::add);
            break;
          }
          else if (value instanceof String)
          {
            roleList.add("ROLE_" + value);
          }
          else if (value instanceof Map)
          {
            node = (Map<String, Object>)value;
          }
          else
          {
            break;
          }
        }
      }

      return roleList.toArray(STRINGS);
    }
  }
}
