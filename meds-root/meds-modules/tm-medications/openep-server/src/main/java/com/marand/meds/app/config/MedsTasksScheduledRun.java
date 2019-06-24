package com.marand.meds.app.config;

import com.marand.thinkmed.medications.automatic.confirm.AdministrationAutoTaskConfirmer;
import com.marand.thinkmed.medications.automatic.create.AdministrationAutoTaskCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Nejc Korasa
 */

@Configuration
@Import({MedsTasksScheduledRun.TaskConfirmer.class, MedsTasksScheduledRun.TaskCreator.class})
public class MedsTasksScheduledRun
{

  @Configuration
  @ConditionalOnProperty(name = "meds.auto-administration-charting-enabled")
  @Import(AdministrationAutoTaskConfirmer.class)
  public static class TaskConfirmer { }

  @Configuration
  @ConditionalOnProperty(name = "meds.auto-task-creator-enabled")
  @Import(AdministrationAutoTaskCreator.class)
  public static class TaskCreator { }
}
