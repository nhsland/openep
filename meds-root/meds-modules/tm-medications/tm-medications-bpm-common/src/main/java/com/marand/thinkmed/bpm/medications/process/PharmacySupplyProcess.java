package com.marand.thinkmed.bpm.medications.process;

import com.marand.ispek.bpm.definition.ExecutionVariable;
import com.marand.ispek.bpm.definition.ProcessMessage;
import com.marand.ispek.bpm.definition.ProcessName;
import com.marand.ispek.bpm.definition.ProcessVariable;

/**
 * @author Klavdij Lapajne
 */
@ProcessName("PharmacySupplyProcess")
public enum PharmacySupplyProcess
{
  @ProcessVariable
  patientId,
  @ProcessVariable
  originalTherapyId,
  @ProcessMessage
  nurseResupplyMessage,

  @ExecutionVariable
  confirmResupply,
  @ExecutionVariable
  createResupplyReminder,
  @ExecutionVariable
  dispenseMedication,
  @ExecutionVariable
  requesterRole,
  @ExecutionVariable
  supplyDispenseStatus,
  @ExecutionVariable
  supplyRequestStatus,
  @ExecutionVariable
  supplyRequestComment,
  @ExecutionVariable
  supplyInDays,
  @ExecutionVariable
  supplyType
}
