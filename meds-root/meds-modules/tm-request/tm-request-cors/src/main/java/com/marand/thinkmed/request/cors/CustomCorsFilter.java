package com.marand.thinkmed.request.cors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Nejc Korasa
 */

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CustomCorsFilter extends OncePerRequestFilter
{
  private static final Logger LOG = LoggerFactory.getLogger(CustomCorsFilter.class);

  private final List<String> originRegexes = new ArrayList<>();

  @Value("${cors.avoid-wildcards:false}")
  private Boolean avoidWildcards;

  @Value("${cors.allow-origin:*}")
  private String allowOrigin;

  @Value("${cors.allow-methods:GET, POST, PUT, DELETE, OPTIONS}")
  private String allowMethods;

  @Value("${cors.allow-headers:*}")
  private String allowHeaders;

  @Value("${cors.allow-credentials:true}")
  private String allowCredentials;

  @Value("${cors.max-age:3600}")
  private String maxAge;

  @PostConstruct
  public void init()
  {
    //noinspection DynamicRegexReplaceableByCompiledPattern
    final List<String> regexes = Arrays
        .stream(allowOrigin.replaceAll("\\s+", "").split(","))
        .map(this::buildRegexFromWildcards)
        .collect(Collectors.toList());

    LOG.debug("Allow origin regexes: " + regexes);
    originRegexes.addAll(regexes);
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException
  {
    response.addHeader("Access-Control-Allow-Origin", buildAllowOriginHeader(request.getHeader("Origin")));

    if ("OPTIONS".equalsIgnoreCase(request.getMethod()))
    {
      response.addHeader("Access-Control-Allow-Methods", allowMethods);
      response.addHeader("Access-Control-Allow-Headers", allowHeaders);
      response.addHeader("Access-Control-Allow-Credentials", allowCredentials);
      response.addHeader("Access-Control-Max-Age", maxAge);
      response.setStatus(200);
    }
    else
    {
      filterChain.doFilter(request, response);
    }
  }

  String buildRegexFromWildcards(final String origin)
  {
    final StringBuffer sb = new StringBuffer();
    final Matcher matcher = Pattern.compile("[^*]+|(\\*)").matcher(origin);
    while (matcher.find())
    {
      if (matcher.group(1) != null)
      {
        matcher.appendReplacement(sb, ".*");
      }
      else
      {
        matcher.appendReplacement(sb, "\\\\Q" + matcher.group(0) + "\\\\E");
      }
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  private String buildAllowOriginHeader(final String origin)
  {
    if (avoidWildcards)
    {
      if (originRegexes.size() == 1 && !"*".equals(allowOrigin))
      {
        return allowOrigin;
      }
      else
      {
        return matchesOrigin(origin) ? origin : allowOrigin;
      }
    }
    return allowOrigin;
  }

  @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
  private boolean matchesOrigin(final String origin)
  {
    return origin != null && ("*".equals(allowOrigin) || originRegexes.stream().anyMatch(origin::matches));
  }
}
