package com.marand.thinkmed.bpm.medications.process;

import com.marand.ispek.bpm.definition.ExecutionVariable;
import com.marand.ispek.bpm.definition.ProcessMessage;
import com.marand.ispek.bpm.definition.ProcessName;
import com.marand.ispek.bpm.definition.ProcessVariable;

/**
 * @author Mitja Lapajne
 */
@ProcessName("PharmacistReviewProcess")
public enum PharmacistReviewProcess
{
  @ProcessVariable
  patientId,
  @ProcessVariable
  taskTimeout,
  @ProcessMessage
  updatePharmacistReviewTaskMessage,

  @ExecutionVariable
  firstAdministrationTimestampMillis,
  @ExecutionVariable
  lastEditorName,
  @ExecutionVariable
  lastEditTimestampMillis,
  @ExecutionVariable
  changeType,
  @ExecutionVariable
  status,
  @ExecutionVariable
  taskAssigneeCode
}
