package com.marand.meds.app.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import com.marand.ispek.bpm.model.ProcessBusinessKey;
import com.marand.ispek.bpm.model.ProcessInstance;
import com.marand.ispek.bpm.updater.ProcessInstanceUpdaterStrategy;
import com.marand.ispek.dto.ProcessInstanceBusinessKeyDto;
import com.marand.maf.core.server.entity.updater.temporal.TemporalEntityUpdater;
import com.marand.meds.app.prop.DatabaseProperties;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate5.HibernateTransactionManager;

/**
 * @author Primoz Delopst
 */

@Configuration
public class BpmConfig
{
  private final DatabaseProperties databaseProperties;
  private final DataSource dataSource;
  private final HibernateTransactionManager transactionManager;
  private final ProcessInstanceUpdaterStrategy processInstanceUpdaterStrategy;
  private final Resource pharmacySupplyProcess;
  private final Resource preparePerfusionSyringeProcess;

  @Autowired
  public BpmConfig(
      final DatabaseProperties databaseProperties,
      final DataSource dataSource,
      final HibernateTransactionManager transactionManager,
      final ProcessInstanceUpdaterStrategy processInstanceUpdaterStrategy,
      @Value("classpath:/PharmacySupplyProcess.bpmn20.xml") final Resource pharmacySupplyProcess,
      @Value("classpath:/PreparePerfusionSyringeProcess.bpmn20.xml") final Resource preparePerfusionSyringeProcess)
  {
    this.databaseProperties = databaseProperties;
    this.dataSource = dataSource;
    this.transactionManager = transactionManager;
    this.processInstanceUpdaterStrategy = processInstanceUpdaterStrategy;
    this.pharmacySupplyProcess = pharmacySupplyProcess;
    this.preparePerfusionSyringeProcess = preparePerfusionSyringeProcess;
  }

  @Bean
  public Resource[] bpmDeploymentResources()
  {
    final List<Resource> resources = new ArrayList<>();
    resources.add(pharmacySupplyProcess);
    resources.add(preparePerfusionSyringeProcess);
    return resources.toArray(new Resource[resources.size()]);
  }

  @Bean
  public SpringProcessEngineConfiguration processEngineConfiguration()
  {
    final SpringProcessEngineConfiguration springProcessEngineConfiguration = new SpringProcessEngineConfiguration();
    springProcessEngineConfiguration.setDatabaseType(databaseProperties.getTypeActiviti());
    springProcessEngineConfiguration.setDataSource(dataSource);
    springProcessEngineConfiguration.setTransactionManager(transactionManager);
    springProcessEngineConfiguration.setDatabaseSchemaUpdate(databaseProperties.getSchemaUpdate());
    springProcessEngineConfiguration.setJobExecutorActivate(true);
    springProcessEngineConfiguration.setDeploymentResources(bpmDeploymentResources());
    springProcessEngineConfiguration.setCustomSessionFactories(Collections.emptyList());
    springProcessEngineConfiguration.setCreateDiagramOnDeploy(false);
    return springProcessEngineConfiguration;
  }

  @Bean
  public ProcessEngineFactoryBean processEngine()
  {
    final ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
    processEngineFactoryBean.setProcessEngineConfiguration(processEngineConfiguration());
    return processEngineFactoryBean;
  }

  @Bean
  public RepositoryService repositoryService() throws Exception
  {
    return processEngine().getObject().getRepositoryService();
  }

  @Bean
  public RuntimeService runtimeService() throws Exception
  {
    return processEngine().getObject().getRuntimeService();
  }

  @Bean
  public TaskService taskService() throws Exception
  {
    return processEngine().getObject().getTaskService();
  }

  @Bean
  public HistoryService historyService() throws Exception
  {
    return processEngine().getObject().getHistoryService();
  }

  @Bean
  public ManagementService managementService() throws Exception
  {
    return processEngine().getObject().getManagementService();
  }

  @Bean
  public IdentityService identityService() throws Exception
  {
    return processEngine().getObject().getIdentityService();
  }

  @Bean
  public TemporalEntityUpdater<ProcessBusinessKey, ProcessInstance, ProcessInstanceBusinessKeyDto> processInstanceUpdater()
  {
    final TemporalEntityUpdater<ProcessBusinessKey, ProcessInstance, ProcessInstanceBusinessKeyDto> temporalEntityUpdater = new TemporalEntityUpdater<>();
    temporalEntityUpdater.setTemporalUpdaterStrategy(processInstanceUpdaterStrategy);
    return temporalEntityUpdater;
  }
}