package com.marand.thinkmed.medications.task.listener;

import java.util.HashMap;
import java.util.Map;

import com.marand.thinkmed.bpm.medications.process.PreparePerfusionSyringeProcess;
import com.marand.thinkmed.medications.task.PerfusionSyringeStartPreparationTaskDef;
import com.marand.thinkmed.medications.task.PerfusionSyringeTaskDef;
import com.marand.thinkmed.medications.task.TaskListenerUtils;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * @author Klavdij Lapajne
 */

@Component
public class PerfusionSyringeStartPreparationTaskListener implements TaskListener
{
  @Override
  public void notify(final DelegateTask delegateTask)
  {
    if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE))
    {
      //delegateTask.setAssignee(); //todo which group gets assigned ?
      final Map<String, Object> variablesMap = new HashMap<>();
      delegateTask.getExecution().removeVariable(PreparePerfusionSyringeProcess.startedDateTimeMillis.name());
      TaskListenerUtils.setPerfusionSyringeTaskDefVariables(
          delegateTask,
          PerfusionSyringeStartPreparationTaskDef.INSTANCE.getTaskExecutionId(),
          PerfusionSyringeStartPreparationTaskDef.INSTANCE.getTaskTypeEnum(),
          variablesMap);
      delegateTask.setVariablesLocal(variablesMap);
    }
    else if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_COMPLETE))
    {
      final DelegateExecution execution = delegateTask.getExecution();
      final Long completionTimeMillis = new DateTime().getMillis();
      execution.setVariable(PreparePerfusionSyringeProcess.startedDateTimeMillis.name(), completionTimeMillis);
      delegateTask.setVariable(PerfusionSyringeTaskDef.PREPARATION_STARTED_TIME_MILLIS.getName(), completionTimeMillis);
    }
  }
}
