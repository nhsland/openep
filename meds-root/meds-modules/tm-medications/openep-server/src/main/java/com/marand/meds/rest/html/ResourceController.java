package com.marand.meds.rest.html;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.marand.meds.app.prop.HtmlProperties;
import com.marand.thinkmed.html.RestServletHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

/**
 * @author Boris Marn
 */
@RestController
@RequestMapping
public class ResourceController
{
  private final HtmlProperties htmlProperties;

  @Value("${server.context-path}")
  private String contextPath;

  @Autowired
  public ResourceController(final HtmlProperties htmlProperties)
  {
    this.htmlProperties = htmlProperties;
  }

  @GetMapping(path = "/resources/views/**",consumes ={ "text/css", "application/javascript" })
  public ResponseEntity resources(
      @RequestParam(value = "format", required = false) final String format,
      @RequestParam(value = "framework", required = false) final String framework,
      @RequestParam(value = "theme", defaultValue = "fresh") final String theme,
      @RequestParam(value = "language", defaultValue = "en") final String language,
      final HttpServletRequest request) throws UnsupportedEncodingException
  {
    final String path = request.getAttribute(
        HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString().replaceFirst("/resources/", "");

    return getResourceContent(path, format, framework, theme, language);
  }

  @GetMapping(path = "/ui/**")
  public ResponseEntity ui(
      @RequestParam(value = "format", required = false) final String format,
      @RequestParam(value = "framework", required = false) final String framework,
      @RequestParam(value = "theme", defaultValue = "fresh") final String theme,
      @RequestParam(value = "language", defaultValue = "en") final String language,
      final HttpServletRequest request) throws UnsupportedEncodingException
  {
    final String path = request.getAttribute(
        HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString().replaceFirst("/ui/", "");

    return getResourceContent(path, format, framework, theme, language);
  }

  private ResponseEntity getResourceContent(
      final String path,
      final String format,
      final String framework,
      final String theme,
      final String language) throws UnsupportedEncodingException
  {
    if (path.endsWith(".map"))
    {
      return ResponseEntity.noContent().build();
    }

    final Response response;

    final String contextPath = this.contextPath.replace("/", "");

    if (RestServletHelper.isApplicationLibraryResource(path))
    {
      response = RestServletHelper.getLibraryResourceContent(
          htmlProperties.isDevelopmentMode(), htmlProperties.getResourceCacheMaxAge(),
          contextPath,
          path,
          framework,
          theme,
          language);
    }
    else
    {
      response = RestServletHelper.getResourceContent(
          htmlProperties.isDevelopmentMode(),
          htmlProperties.getResourceCacheMaxAge(),
          contextPath,
          path,
          format,
          theme);
    }

    final int maxAge = htmlProperties.isDevelopmentMode() ? -1 : htmlProperties.getResourceCacheMaxAge();
    return ResponseEntity.ok()
        .cacheControl(CacheControl.maxAge(maxAge, TimeUnit.MILLISECONDS))
        .contentType(MediaType.valueOf(response.getMediaType().getType() + "/" + response.getMediaType().getSubtype()))
        .body(response.getEntity());
  }
}
