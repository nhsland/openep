package com.marand.thinkmed.medications.task.listener;

import java.util.HashMap;
import java.util.Map;

import com.marand.thinkmed.bpm.medications.process.PharmacySupplyProcess;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.task.DispenseMedicationTaskDef;
import com.marand.thinkmed.medications.task.SupplyRequestStatus;
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
public class DispenseMedicationTaskListener implements TaskListener
{
  @Override
  public void notify(final DelegateTask delegateTask)
  {
    if (delegateTask.getEventName().equals(TaskListener.EVENTNAME_CREATE))
    {
      final DelegateExecution execution = delegateTask.getExecution();
      final String patientId = (String)execution.getVariable(PharmacySupplyProcess.patientId.name());
      final String originalTherapyId = (String)execution.getVariable(PharmacySupplyProcess.originalTherapyId.name());
      delegateTask.setName(DispenseMedicationTaskDef.INSTANCE.buildKey(String.valueOf(patientId)));
      delegateTask.setAssignee(TherapyAssigneeEnum.PHARMACIST.name());
      delegateTask.setDueDate(delegateTask.getCreateTime());

      final Map<String, Object> variablesMap = new HashMap<>();
      TaskListenerUtils.setTherapyTaskDefVariables(patientId, originalTherapyId, variablesMap);
      variablesMap.put(
          TaskConstants.TASK_EXECUTION_ID_VARIABLE_NAME,
          DispenseMedicationTaskDef.INSTANCE.getTaskExecutionId());

      final String requesterRole = (String)execution.getVariable(PharmacySupplyProcess.requesterRole.name());
      final SupplyRequestStatus requestStatus = TherapyAssigneeEnum.valueOf(requesterRole) == TherapyAssigneeEnum.NURSE
                                                ? SupplyRequestStatus.UNVERIFIED
                                                : SupplyRequestStatus.VERIFIED;
      variablesMap.put(
          DispenseMedicationTaskDef.REQUESTER_ROLE.getName(),
          requesterRole);
      variablesMap.put(
          DispenseMedicationTaskDef.REQUEST_STATUS.getName(),
          requestStatus.name());

      if (!requesterRole.equals(TherapyAssigneeEnum.NURSE.name()))
      {
        variablesMap.put(
            DispenseMedicationTaskDef.DAYS_SUPPLY.getName(),
            execution.getVariable(PharmacySupplyProcess.supplyInDays.name()));
        variablesMap.put(
            DispenseMedicationTaskDef.SUPPLY_TYPE.getName(),
            execution.getVariable(PharmacySupplyProcess.supplyType.name()));
      }

      delegateTask.setVariablesLocal(variablesMap);
    }
  }
}
