package com.marand.thinkmed.medications.task.listener;

import java.util.HashMap;
import java.util.Map;

import com.marand.thinkmed.bpm.medications.process.PharmacySupplyProcess;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.task.SupplyReviewTaskDef;
import com.marand.thinkmed.medications.task.TaskListenerUtils;
import com.marand.thinkmed.process.TaskConstants;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

/**
 * @author Klavdij Lapajne
 */

@Component
public class SupplyReviewTaskListener implements TaskListener
{
  @Override
  public void notify(final DelegateTask delegateTask)
  {
    if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE))
    {
      final DelegateExecution execution = delegateTask.getExecution();
      final String patientId = (String)execution.getVariable(PharmacySupplyProcess.patientId.name());
      final String originalTherapyId = (String)execution.getVariable(PharmacySupplyProcess.originalTherapyId.name());
      delegateTask.setName(SupplyReviewTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));
      delegateTask.setAssignee(TherapyAssigneeEnum.PHARMACIST.name());

      final Map<String, Object> variablesMap = new HashMap<>();
      TaskListenerUtils.setTherapyTaskDefVariables(patientId, originalTherapyId, variablesMap);

      variablesMap.put(TaskConstants.TASK_EXECUTION_ID_VARIABLE_NAME, SupplyReviewTaskDef.INSTANCE.getTaskExecutionId());

      variablesMap.put(
          SupplyReviewTaskDef.ALREADY_DISPENSED.getName(),
          false);
      variablesMap.put(
          SupplyReviewTaskDef.REQUESTER_ROLE.getName(),
          execution.getVariable(PharmacySupplyProcess.requesterRole.name()));

      delegateTask.setVariablesLocal(variablesMap);
    }
  }
}
