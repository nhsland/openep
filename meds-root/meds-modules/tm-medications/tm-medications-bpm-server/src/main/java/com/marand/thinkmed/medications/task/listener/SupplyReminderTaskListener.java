package com.marand.thinkmed.medications.task.listener;

import java.util.HashMap;
import java.util.Map;

import com.marand.thinkmed.bpm.medications.process.PharmacySupplyProcess;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.task.SupplyReminderTaskDef;
import com.marand.thinkmed.medications.task.TaskListenerUtils;
import com.marand.thinkmed.process.TaskConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * @author Klavdij Lapajne
 */

@Component
public class SupplyReminderTaskListener implements TaskListener
{
  @Override
  public void notify(final DelegateTask delegateTask)
  {
    if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE))
    {
      final DelegateExecution execution = delegateTask.getExecution();
      final String patientId = (String)execution.getVariable(PharmacySupplyProcess.patientId.name());
      final String originalTherapyId = (String)execution.getVariable(PharmacySupplyProcess.originalTherapyId.name());
      delegateTask.setName(SupplyReminderTaskDef.INSTANCE.buildKey(patientId));
      delegateTask.setAssignee(TherapyAssigneeEnum.PHARMACIST.name());
      final DateTime createTime = new DateTime(delegateTask.getCreateTime());
      delegateTask.setDueDate(
          createTime.plusDays((Integer)execution.getVariable(PharmacySupplyProcess.supplyInDays.name())).toDate());

      final Map<String, Object> variablesMap = new HashMap<>();
      TaskListenerUtils.setTherapyTaskDefVariables(patientId, originalTherapyId, variablesMap);
      variablesMap.put(TaskConstants.TASK_EXECUTION_ID_VARIABLE_NAME, SupplyReminderTaskDef.INSTANCE.getTaskExecutionId());
      variablesMap.put(
          SupplyReminderTaskDef.DAYS_SUPPLY.getName(),
          execution.getVariable(PharmacySupplyProcess.supplyInDays.name()));
      variablesMap.put(
          SupplyReminderTaskDef.IS_DISMISSED.getName(),
          false);
      variablesMap.put(
          SupplyReminderTaskDef.SUPPLY_TYPE.getName(),
          execution.getVariable(PharmacySupplyProcess.supplyType.name()));

      delegateTask.setVariablesLocal(variablesMap);
    }
  }
}
