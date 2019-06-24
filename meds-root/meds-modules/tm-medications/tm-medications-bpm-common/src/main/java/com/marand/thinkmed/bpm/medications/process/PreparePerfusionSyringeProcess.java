package com.marand.thinkmed.bpm.medications.process;

import com.marand.ispek.bpm.definition.ExecutionVariable;
import com.marand.ispek.bpm.definition.ProcessMessage;
import com.marand.ispek.bpm.definition.ProcessName;
import com.marand.ispek.bpm.definition.ProcessVariable;

/**
 * @author Klavdij Lapajne
 */
@ProcessName("PreparePerfusionSyringeProcess")
public enum PreparePerfusionSyringeProcess
{
  @ProcessVariable
  patientId,
  @ProcessVariable
  originalTherapyId,

  @ProcessMessage
  cancelTherapyMessage,
  @ProcessMessage
  cancelOrderMessage,
  @ProcessMessage
  medicationAdministrationMessage,

  @ExecutionVariable
  numberOfSyringes,
  @ExecutionVariable
  isUrgent,
  @ExecutionVariable
  startedDateTimeMillis,
  @ExecutionVariable
  dueDateTimeMillis,
  @ExecutionVariable
  cancelPreparation,
  @ExecutionVariable
  orderCanceled,
  @ExecutionVariable
  therapyCanceled,
  @ExecutionVariable
  undoState,
  @ExecutionVariable
  orderer,
  @ExecutionVariable
  ordererFullName,
  @ExecutionVariable
  printSystemLabel
}
