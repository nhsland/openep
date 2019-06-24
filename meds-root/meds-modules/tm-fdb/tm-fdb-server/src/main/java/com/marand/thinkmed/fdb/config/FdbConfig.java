package com.marand.thinkmed.fdb.config;

import com.marand.thinkmed.fdb.rest.FdbRestService;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author Boris Marn.
 */
@Component
@Configuration
@EnableConfigurationProperties({FdbProperties.class})
public class FdbConfig
{
  @Autowired
  private FdbProperties fdbProperties;

  @Bean
  public FdbRestService restService()
  {
    final HttpClientContext context = HttpClientContext.create();

    final ResteasyClient client = new ResteasyClientBuilder().httpEngine(
        new ApacheHttpClient4Engine(
            HttpClientBuilder.create().setConnectionManager(
                new PoolingHttpClientConnectionManager()).build(), context)).build();
    final ResteasyWebTarget target = client.target(fdbProperties.getRestUri());
    return target.proxy(FdbRestService.class);
  }
}
