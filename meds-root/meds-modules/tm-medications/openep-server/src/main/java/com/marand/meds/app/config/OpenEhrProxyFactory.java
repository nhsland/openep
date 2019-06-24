package com.marand.meds.app.config;

import com.marand.maf.core.openehr.service.OpenEhrInvokerProxyFactory;
import com.marand.thinkehr.form.service.FormService;
import com.marand.thinkehr.query.service.QueryService;
import com.marand.thinkehr.service.ThinkEhrService;
import com.marand.thinkehr.tagging.service.TaggingService;
import com.marand.thinkehr.tdd.TemplateDocumentConversionService;
import com.marand.thinkehr.templates.service.TemplateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @author Primoz Delopst
 * @author Boris Marn
 */

@Configuration
public class OpenEhrProxyFactory
{
  @Bean
  @Lazy
  public OpenEhrInvokerProxyFactory ehrQueryService()
  {
    return createOpenEhrInvokerProxyFactory(QueryService.class);
  }

  @Bean
  @Lazy
  public OpenEhrInvokerProxyFactory taggingService()
  {
    return createOpenEhrInvokerProxyFactory(TaggingService.class);
  }

  @Bean
  @Lazy
  public OpenEhrInvokerProxyFactory templateService()
  {
    return createOpenEhrInvokerProxyFactory(TemplateService.class);
  }

  @Bean
  @Lazy
  public OpenEhrInvokerProxyFactory formService()
  {
    return createOpenEhrInvokerProxyFactory(FormService.class);
  }

  @Bean
  @Lazy
  public OpenEhrInvokerProxyFactory templateDocumentConversionService()
  {
    return createOpenEhrInvokerProxyFactory(TemplateDocumentConversionService.class);
  }

  @Bean
  @Lazy
  public OpenEhrInvokerProxyFactory thinkEhrService()
  {
    return createOpenEhrInvokerProxyFactory(ThinkEhrService.class);
  }

  private OpenEhrInvokerProxyFactory createOpenEhrInvokerProxyFactory(final Class<?> serviceInterface)
  {
    final OpenEhrInvokerProxyFactory openEhrInvokerProxyFactory = new OpenEhrInvokerProxyFactory();
    openEhrInvokerProxyFactory.setServiceInterface(serviceInterface);
    return openEhrInvokerProxyFactory;
  }
}
