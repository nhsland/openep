package com.marand.meds.app.config;

import com.marand.thinkmed.elmdoc.config.ElmdocProperties;
import com.marand.thinkmed.medispan.config.MedispanProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * @author Vid Kumse
 */

@Configuration
public class WarningsConfig
{
  @Configuration
  @Profile("fdb")
  @ComponentScan(
      value = {
          "com.marand.thinkmed.fdb"
      }
  )
  public static class Fdb { }

  @Configuration
  @Profile("medispan")
  @EnableConfigurationProperties({MedispanProperties.class})
  @ComponentScan(
      value = {
          "com.marand.thinkmed.medispan"
      }
  )
  public static class Medispan { }

  @Configuration
  @Profile("elmdoc")
  @EnableConfigurationProperties({ElmdocProperties.class})
  @ComponentScan(
      value = {
          "com.marand.thinkmed.elmdoc"
      }
  )
  public static class Elmdoc { }
}
