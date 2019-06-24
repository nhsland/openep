package com.marand.meds.app;

import java.awt.Image;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.PreferencesFactory;
import javax.annotation.PostConstruct;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.marand.auditing.auditing.EnableAuditing;
import com.marand.ispek.common.Dictionary;
import com.marand.ispek.common.IspekFormatterFactory;
import com.marand.ispek.print.common.PrintContext;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.daterule.dao.hibernate.HibernateMafDateRuleDao;
import com.marand.maf.core.daterule.service.impl.MafDateRuleServiceImpl;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.maf.core.eventbus.ApplicationEventDispatcher;
import com.marand.maf.core.openehr.dao.openehr.TaggingOpenEhrDao;
import com.marand.maf.core.prefs.DatabasePreferencesSpi;
import com.marand.maf.core.prefs.MafPreferencesFactory;
import com.marand.maf.core.prefs.dao.PreferencesDao;
import com.marand.maf.core.prefs.dao.impl.HibernatePreferencesDao;
import com.marand.maf.core.security.dao.hibernate.HibernateDomainPermissionDao;
import com.marand.maf.core.server.entity.dao.hibernate.HibernateEntityDao;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.meds.app.config.BpmConfig;
import com.marand.meds.app.config.MedsConnectorConfig;
import com.marand.meds.app.config.MedsDatabaseConfiguration;
import com.marand.meds.app.config.MedsEhrConfiguration;
import com.marand.meds.app.config.MedsHolderScheduledRun;
import com.marand.meds.app.config.MedsTasksScheduledRun;
import com.marand.meds.app.config.OpenEhrProxyFactory;
import com.marand.meds.app.config.SwaggerConfig;
import com.marand.meds.app.config.WarningsConfig;
import com.marand.meds.app.config.security.DemoSecurityConfig;
import com.marand.meds.app.config.security.OAuth2ClientConfigProvider;
import com.marand.meds.app.config.security.OAuth2SecurityConfig;
import com.marand.meds.app.prop.HtmlProperties;
import com.marand.meds.app.prop.OpenepProperties;
import com.marand.meds.app.prop.SslProperties;
import com.marand.meds.logic.eer.EERPrescriptionDocumentProviderPluginImpl;
import com.marand.meds.logic.eer.EERPrescriptionHandlerImpl;
import com.marand.thinkmed.medications.MedsJsonDeserializer;
import com.marand.thinkmed.medications.config.MedsProperties;
import com.marand.thinkmed.medications.connector.impl.config.FhirProperties;
import com.marand.thinkmed.medications.connector.impl.rest.TcProperties;
import com.marand.thinkmed.medications.hl7.A03Processor;
import com.marand.thinkmed.medications.hl7.Hl7Listener;
import com.marand.thinkmed.medications.hl7.Hl7Properties;
import com.marand.thinkmed.medications.task.config.MedicationBpmModuleConfig;
import com.marand.thinkmed.medications.witnessing.WitnessingProperties;
import com.marand.thinkmed.meds.config.updaters.conf.UpdatersConfig;
import com.marand.thinkmed.organizational.util.PrefixedParentFinderStrategy;
import com.marand.thinkmed.process.business.impl.DefaultTaskBo;
import com.marand.thinkmed.process.service.impl.ProcessServiceImpl;
import com.marand.thinkmed.request.auth.OAuth2RestTemplateFactory;
import com.marand.thinkmed.request.cors.CustomCorsFilter;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.time.RequestScopeTime;
import com.marand.thinkmed.request.user.RequestUser;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.spring.web.json.Json;

/**
 * @author Boris Marn.
 */

@SpringBootApplication(exclude = {
    HibernateJpaAutoConfiguration.class,
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    JacksonAutoConfiguration.class
})
@PropertySources({
    @PropertySource("classpath:default.properties")
})
@EnableConfigurationProperties({
    OpenepProperties.class,
    HtmlProperties.class,
    MedsProperties.class,
    FhirProperties.class,
    SslProperties.class,
    TcProperties.class,
    WitnessingProperties.class,
    Hl7Properties.class
})
@Import({

    MedicationsApplication.WebConfig.AuditingConfig.class,
    MedicationsApplication.WebConfig.class,

    // config
    MedsConnectorConfig.class,
    MedsDatabaseConfiguration.class,
    MedsEhrConfiguration.class,
    MedsHolderScheduledRun.class,
    MedsTasksScheduledRun.class,
    UpdatersConfig.class,
    WarningsConfig.class,
    SwaggerConfig.class,
    MedicationBpmModuleConfig.class,
    BpmConfig.class,
    Hl7Listener.class,

    // security
    DemoSecurityConfig.class,
    OAuth2SecurityConfig.class,
    OAuth2ClientConfigProvider.class,

    // OAuth2 rest template factory
    OAuth2RestTemplateFactory.class,

    // beans
    RequestDateTimeHolder.class,
    RequestScopeTime.class,

    // filter
    CustomCorsFilter.class,

    ApplicationEventDispatcher.class,
    MafDateRuleServiceImpl.class,
    HibernateMafDateRuleDao.class,
    TaggingOpenEhrDao.class,
    HibernateEntityDao.class,
    HibernateDomainPermissionDao.class,
    PrefixedParentFinderStrategy.class,
    HibernatePreferencesDao.class,
    MafPreferencesFactory.class,
    ProcessServiceImpl.class,
    OpenEhrProxyFactory.class,

    //eer
    EERPrescriptionHandlerImpl.class,
    EERPrescriptionDocumentProviderPluginImpl.class,

    A03Processor.class
})
@EnableAsync
@ImportResource({"classpath:/ac-alfresco-repository.xml"})
@ComponentScan(
    value = {
        "com.marand.meds.rest",
        "com.marand.thinkmed.medications",
        "com.marand.thinkmed.patient",
        "com.marand.ispek.bpm.service",
        "com.marand.ispek.bpm.dao",
        "com.marand.ispek.bpm.updater",
        "com.marand.thinkmed.meds.config.validation.validators"
    }
)
@EnableScheduling
@EnableRetry
public class MedicationsApplication
{
  private final OpenepProperties openepProperties;
  private final SslProperties sslProperties;

  @Autowired
  public MedicationsApplication(final OpenepProperties openepProperties, final SslProperties sslProperties)
  {
    this.openepProperties = openepProperties;
    this.sslProperties = sslProperties;
  }

  public static void main(final String[] args)
  {
    JsonUtil.setFormatterFactory(IspekFormatterFactory.newInstance());

    final DatabasePreferencesSpi databasePreferencesSpi = new DatabasePreferencesSpi();

    final ConfigurableApplicationContext applicationContext = SpringApplication.run(MedicationsApplication.class, args);

    final MafPreferencesFactory bean = applicationContext.getBean(MafPreferencesFactory.class);
    bean.setPreferencesSpi(databasePreferencesSpi);
    databasePreferencesSpi.setPreferencesDao(applicationContext.getBean(PreferencesDao.class));
    databasePreferencesSpi.setMafPreferencesFactory(bean);
    databasePreferencesSpi.setMafPreferencesFactory(applicationContext.getBean(PreferencesFactory.class));
  }

  @PostConstruct
  public void init()
  {
    final Locale locale = new Locale(openepProperties.getLocale(), openepProperties.getCountry());
    Locale.setDefault(locale);
    DefinedLocaleHolder.INSTANCE.setLocale(locale);

    // Setup Dictionary (for now maf dictionary is used) // TODO new git - use your own dictionary
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", locale));

    PrintContext.INSTANCE.store(
        "Think!Meds",
        locale,
        new PrintContext.ValuesProvider()
        {
          @Override
          public String getLoggedUserName()
          {
            return RequestUser.getFullName();
          }

          @Override
          public Image getPageHeaderLogoImage()
          {
            return null;
          }
        },
        false);

    if (sslProperties != null && sslProperties.getTrustStore() != null)
    {
      System.setProperty("javax.net.ssl.trustStore", sslProperties.getTrustStore());
      System.setProperty("javax.net.ssl.trustStorePassword", sslProperties.getTrustStorePassword());
    }
  }

  @Bean
  public DefaultTaskBo defaultTaskBo()
  {
    final DefaultTaskBo defaultTaskBo = new DefaultTaskBo();
    defaultTaskBo.setUserIdSupplier(() -> RequestUser.getUser().getId());
    return defaultTaskBo;
  }

  @Configuration
  public static class WebConfig extends WebMvcConfigurerAdapter
  {

    private static final JsonUtil.TypeAdapterPair INTERVAL_SERIALIZER = new JsonUtil.TypeAdapterPair(
        Interval.class,
        (JsonSerializer<Interval>)(src, typeOfSrc, context) ->
        {
          final JsonObject result = new JsonObject();
          result.add("startMillis", context.serialize(src.getStartMillis()));
          result.add("endMillis", context.serialize(src.getEndMillis()));

          return result;
        });

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters)
    {
      converters.add(gsonHttpMessageConverter());
      super.configureMessageConverters(converters);
    }

    @Bean
    public GsonHttpMessageConverter gsonHttpMessageConverter()
    {
      final List<JsonUtil.TypeAdapterPair> typeAdapterPairs = new ArrayList<>();
      typeAdapterPairs.add(INTERVAL_SERIALIZER);
      typeAdapterPairs.addAll(MedsJsonDeserializer.INSTANCE.getTypeAdapters());
      typeAdapterPairs.add(new JsonUtil.TypeAdapterPair(Json.class, new SpringfoxJsonToGsonAdapter()));

      final Gson gson = JsonUtil.getGson(null, typeAdapterPairs);

      final GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
      gsonConverter.setGson(gson);
      return gsonConverter;
    }

    private static class SpringfoxJsonToGsonAdapter implements JsonSerializer<Json>
    {
      private static final JsonParser parser = new JsonParser();

      @Override
      public JsonElement serialize(final Json json, final Type type, final JsonSerializationContext context)
      {
        return parser.parse(json.value());
      }
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry)
    {
      registry.addResourceHandler("ui/externals/**").addResourceLocations("classpath:externals/");
      registry.addResourceHandler("ui/externals/**").addResourceLocations("classpath:externals/");
      registry.addResourceHandler("ui/framework/**").addResourceLocations("classpath:framework/");
      registry.addResourceHandler("resources/images/**").addResourceLocations("classpath:images/");
      registry.addResourceHandler("resources/fonts/**").addResourceLocations("classpath:fonts/");
    }

    @EnableAuditing
    @ConditionalOnProperty(name = "auditing.strategy")
    public static class AuditingConfig { }
  }
}
