package com.marand.meds.app.config;

import java.util.Properties;
import javax.sql.DataSource;

import com.marand.maf.core.hibernate.audit.AuditInfoProvider;
import com.marand.maf.core.hibernate.audit.AuditInterceptor;
import com.marand.maf.core.server.jdbc.DatabaseSupport;
import com.marand.maf.core.server.jdbc.MSSQLDatabaseSupport;
import com.marand.maf.core.server.jdbc.OracleDatabaseSupport;
import com.marand.meds.app.prop.DatabaseProperties;
import com.marand.thinkmed.request.time.RequestDateTimeHolder;
import com.marand.thinkmed.request.user.RequestUser;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Boris Marn.
 */
@Configuration
@EnableAutoConfiguration(
    exclude = {JpaRepositoriesAutoConfiguration.class, HibernateJpaAutoConfiguration.class}
)
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties({DatabaseProperties.class})
public class MedsDatabaseConfiguration
{
  private final RequestDateTimeHolder requestDateTimeHolder;

  @Autowired
  public MedsDatabaseConfiguration(final RequestDateTimeHolder requestDateTimeHolder)
  {
    this.requestDateTimeHolder = requestDateTimeHolder;
  }

  @Bean
  public AuditInterceptor auditInterceptor()
  {
    final AuditInterceptor interceptor = new AuditInterceptor();
    interceptor.setAuditInfoProvider(auditInfoProvider());
    return interceptor;
  }

  @Bean
  public AuditInfoProvider auditInfoProvider()
  {
    return new AuditInfoProvider()
    {
      @Override
      public DateTime getAuditTimestamp()
      {
        return requestDateTimeHolder.getRequestTimestamp();
      }

      @Override
      public String getAuditUserId()
      {
        return RequestUser.getId();
      }
    };
  }

  @Bean
  @Primary
  public DataSource dataSource(final DatabaseProperties databaseProperties)
  {
    final DatabaseProperties.Datasource propertiesDatasource = databaseProperties.getDatasource();
    final HikariDataSource dataSource = new HikariDataSource();

    dataSource.setDriverClassName(propertiesDatasource.getDriverClassName());
    dataSource.setJdbcUrl(propertiesDatasource.getUrl());
    dataSource.setUsername(propertiesDatasource.getUsername());
    dataSource.setPassword(propertiesDatasource.getPassword());
    dataSource.setMaximumPoolSize(propertiesDatasource.getMaxPoolSize());

    return dataSource;
  }

  public LocalSessionFactoryBean createHibernateSessionFactory(
      final DatabaseProperties databaseProperties,
      final DataSource dataSource,
      final AuditInterceptor auditInterceptor)
      throws ClassNotFoundException
  {
    final LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();

    sessionFactoryBean.setDataSource(dataSource);
    sessionFactoryBean.setPackagesToScan(
        "com.marand.thinkmed.medications.model.impl",

        "com.marand.maf.core.hibernate.entity", // you don't want to see what is in this package
        "com.marand.maf.core.valueholder",
        "com.marand.maf.core.prefs.model",
        "com.marand.maf.core.daterule.model.impl",
        "com.marand.ispek.bpm.model.impl"
    );

    sessionFactoryBean.setEntityInterceptor(auditInterceptor);
    sessionFactoryBean.setHibernateProperties(hibernateProperties(databaseProperties));
    return sessionFactoryBean;
  }

  @SuppressWarnings("CallToNumericToString")
  private Properties hibernateProperties(final DatabaseProperties dbProps)
  {
    final Properties properties = new Properties();
    properties.setProperty("hibernate.temp.use_jdbc_metadata_defaults", dbProps.isUseJdbcMetadataDefaults().toString());
    properties.setProperty("hibernate.show_sql", dbProps.isShowSql().toString());
    properties.setProperty("hibernate.format_sql", dbProps.isFormatSql().toString());
    properties.setProperty("hibernate.cache.use_query_cache", "false");

    properties.setProperty("hibernate.cache.use_second_level_cache", "false");
    properties.setProperty("hibernate.integration.envers.enabled", "false");
    properties.setProperty("hibernate.cache.region.factory_class", dbProps.getRegionCacheFactoryClass().getName());
    if (dbProps.getDatasource().getDefaultSchema() != null)
    {
      properties.setProperty("hibernate.default_schema", dbProps.getDatasource().getDefaultSchema());
    }
    properties.setProperty("hibernate.dialect", dbProps.getDialect().getName());
    properties.setProperty("hibernate.implicit_naming_strategy", dbProps.getImplicitNamingStrategy().getName());
    properties.setProperty("hibernate.physical_naming_strategy", dbProps.getPhysicalNamingStrategy().getName());
    properties.setProperty("use_sql_comments", dbProps.isUseSqlComments().toString());

    properties.setProperty("hibernate.jdbc.fetch_size", "512");
    properties.setProperty("hibernate.jdbc.batch_size", "512");

    return properties;
  }

  @Bean
  public HibernateTemplate hibernateTemplate(final SessionFactory sessionFactory)
  {
    return new HibernateTemplate(sessionFactory);
  }

  @Bean
  public HibernateTransactionManager transactionManager(final SessionFactory sessionFactory)
  {
    return new HibernateTransactionManager(sessionFactory);
  }

  @Configuration
  @Profile("oracle-db")
  public class OracleDbSupportConfig
  {
    @Bean
    @Autowired
    public DatabaseSupport databaseSupport(final DataSource dataSource)
    {
      final OracleDatabaseSupport dbSupport = new OracleDatabaseSupport();
      dbSupport.setDataSource(dataSource);
      return dbSupport;
    }

    @Bean
    public LocalSessionFactoryBean hibernateSessionFactory(
        final DatabaseProperties databaseProperties,
        final DataSource dataSource,
        final AuditInterceptor auditInterceptor)
        throws ClassNotFoundException
    {
      return createHibernateSessionFactory(databaseProperties, dataSource, auditInterceptor);
    }
  }

  @Configuration
  @Profile("mssql-db")
  public class MsSqlDbSupportConfig
  {
    @Bean
    public DatabaseSupport databaseSupport(
        final DataSource dataSource,
        final DatabaseProperties databaseProperties)
    {
      final MSSQLDatabaseSupport dbSupport = new MSSQLDatabaseSupport();
      dbSupport.setDatabaseName(checkNotNull(databaseProperties.getDatasource().getName(), "DB name must not be null!"));
      dbSupport.setDataSource(dataSource);
      return dbSupport;
    }

    @Bean
    public LocalSessionFactoryBean hibernateSessionFactory(
        final DatabaseProperties databaseProperties,
        final DataSource dataSource,
        final AuditInterceptor auditInterceptor)
        throws ClassNotFoundException
    {
      return createHibernateSessionFactory(databaseProperties, dataSource, auditInterceptor);
    }
  }
}
