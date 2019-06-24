package com.marand.meds.rest.webapp;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;

import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.medications.dto.WebAppErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nejc Korasa
 */

@RestController
@RequestMapping("/medications")
public class WebAppErrorController
{
  private static final Logger WEB_APP_LOG = LoggerFactory.getLogger("web-app-logger");

  @PostMapping(value = "webAppError")
  public void logWebError(@RequestParam("errors") final String errors, final HttpServletRequest request)
  {
    Arrays
        .stream(JsonUtil.fromJson(errors, WebAppErrorDto[].class))
        .forEach(error -> {
          WEB_APP_LOG.error(
              "{}: {} - remoteAddress: {}\n{}",
              error.getLoggerName(),
              error.getMessage(),
              Opt.ofNonEmpty(request.getHeader("X-FORWARDED-FOR")).orElse(request.getRemoteAddr()),
              error.getDetails());
        });
  }
}
