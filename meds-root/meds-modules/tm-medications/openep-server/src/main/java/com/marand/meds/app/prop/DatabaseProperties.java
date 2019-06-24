package com.marand.meds.app.prop;

import java.sql.Driver;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.dialect.Dialect;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Boris Marn.
 */

@SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion", "BooleanMethodNameMustStartWithQuestion"})
@ConfigurationProperties(prefix = "db")
@Validated
public class DatabaseProperties
{

  @Valid private final Datasource datasource = new Datasource();
  @NotNull private Boolean useJdbcMetadataDefaults;
  @NotNull private Boolean showSql;
  @NotNull private Boolean formatSql;
  @NotNull private Class<? extends RegionFactory> regionCacheFactoryClass;
  @NotNull private Class<? extends Dialect> dialect;
  @NotNull private Class<? extends ImplicitNamingStrategy> implicitNamingStrategy;
  @NotNull private Class<? extends PhysicalNamingStrategy> physicalNamingStrategy;
  @NotNull private Boolean useSqlComments;
  @NotBlank private String typeActiviti;
  @NotBlank private String schemaUpdate;

  public Datasource getDatasource()
  {
    return datasource;
  }

  public Boolean getUseJdbcMetadataDefaults()
  {
    return useJdbcMetadataDefaults;
  }

  public void setUseJdbcMetadataDefaults(final Boolean useJdbcMetadataDefaults)
  {
    this.useJdbcMetadataDefaults = useJdbcMetadataDefaults;
  }

  public Boolean getShowSql()
  {
    return showSql;
  }

  public void setShowSql(final Boolean showSql)
  {
    this.showSql = showSql;
  }

  public Boolean getFormatSql()
  {
    return formatSql;
  }

  public void setFormatSql(final Boolean formatSql)
  {
    this.formatSql = formatSql;
  }

  public Boolean getUseSqlComments()
  {
    return useSqlComments;
  }

  public void setUseSqlComments(final Boolean useSqlComments)
  {
    this.useSqlComments = useSqlComments;
  }

  public Boolean isUseJdbcMetadataDefaults()
  {
    return useJdbcMetadataDefaults;
  }

  public Boolean isShowSql()
  {
    return showSql;
  }

  public Boolean isFormatSql()
  {
    return formatSql;
  }

  public Class<? extends RegionFactory> getRegionCacheFactoryClass()
  {
    return regionCacheFactoryClass;
  }

  public void setRegionCacheFactoryClass(final Class<? extends RegionFactory> regionCacheFactoryClass)
  {
    this.regionCacheFactoryClass = regionCacheFactoryClass;
  }

  public Class<? extends Dialect> getDialect()
  {
    return dialect;
  }

  public void setDialect(final Class<? extends Dialect> dialect)
  {
    this.dialect = dialect;
  }

  public Class<? extends ImplicitNamingStrategy> getImplicitNamingStrategy()
  {
    return implicitNamingStrategy;
  }

  public void setImplicitNamingStrategy(final Class<? extends ImplicitNamingStrategy> implicitNamingStrategy)
  {
    this.implicitNamingStrategy = implicitNamingStrategy;
  }

  public Class<? extends PhysicalNamingStrategy> getPhysicalNamingStrategy()
  {
    return physicalNamingStrategy;
  }

  public void setPhysicalNamingStrategy(final Class<? extends PhysicalNamingStrategy> physicalNamingStrategy)
  {
    this.physicalNamingStrategy = physicalNamingStrategy;
  }

  public Boolean isUseSqlComments()
  {
    return useSqlComments;
  }

  public String getTypeActiviti()
  {
    return typeActiviti;
  }

  public void setTypeActiviti(final String typeActiviti)
  {
    this.typeActiviti = typeActiviti;
  }

  public String getSchemaUpdate()
  {
    return schemaUpdate;
  }

  public void setSchemaUpdate(final String schemaUpdate)
  {
    this.schemaUpdate = schemaUpdate;
  }

  public static class Datasource
  {
    @NotEmpty private String url;
    @NotEmpty private String username;
    @NotEmpty private String password;
    @NotNull private Class<? extends Driver> driverClass;
    private String defaultSchema;
    private String name;

    private int maxPoolSize;

    public String getUrl()
    {
      return url;
    }

    public void setUrl(final String url)
    {
      this.url = url;
    }

    public String getUsername()
    {
      return username;
    }

    public void setUsername(final String username)
    {
      this.username = username;
    }

    public String getPassword()
    {
      return password;
    }

    public void setPassword(final String password)
    {
      this.password = password;
    }

    public String getDriverClassName()
    {
      return driverClass.getName();
    }

    public Class<? extends Driver> getDriverClass()
    {
      return driverClass;
    }

    public void setDriverClass(final Class<? extends Driver> driverClass)
    {
      this.driverClass = driverClass;
    }

    public String getDefaultSchema()
    {
      return defaultSchema;
    }

    public void setDefaultSchema(final String defaultSchema)
    {
      this.defaultSchema = defaultSchema;
    }

    public int getMaxPoolSize()
    {
      return maxPoolSize;
    }

    public void setMaxPoolSize(final int maxPoolSize)
    {
      this.maxPoolSize = maxPoolSize;
    }

    public String getName()
    {
      return name;
    }

    public void setName(final String name)
    {
      this.name = name;
    }
  }
}
