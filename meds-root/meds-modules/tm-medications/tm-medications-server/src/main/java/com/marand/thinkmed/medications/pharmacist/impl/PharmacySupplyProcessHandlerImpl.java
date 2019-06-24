package com.marand.thinkmed.medications.pharmacist.impl;

import java.util.List;

import com.marand.ispek.bpm.service.BpmService;
import com.marand.maf.core.Pair;
import com.marand.thinkmed.bpm.medications.process.PharmacySupplyProcess;
import com.marand.thinkmed.medications.TherapyAssigneeEnum;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.dto.pharmacist.review.MedicationSupplyTypeEnum;
import com.marand.thinkmed.medications.pharmacist.PharmacistTaskProvider;
import com.marand.thinkmed.medications.pharmacist.PharmacySupplyProcessHandler;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.process.dto.TaskDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Klavdij Lapajne
 */
@Component
public class PharmacySupplyProcessHandlerImpl implements PharmacySupplyProcessHandler
{
  private BpmService bpmService;
  private MedicationsBo medicationsBo;
  private PharmacistTaskProvider pharmacistTaskProvider;
  private TherapyEhrHandler therapyEhrHandler;

  @Autowired
  public void setBpmService(final BpmService bpmService)
  {
    this.bpmService = bpmService;
  }

  @Autowired
  public void setMedicationsBo(final MedicationsBo medicationsBo)
  {
    this.medicationsBo = medicationsBo;
  }

  @Autowired
  public void setPharmacistTaskProvider(final PharmacistTaskProvider pharmacistTaskProvider)
  {
    this.pharmacistTaskProvider = pharmacistTaskProvider;
  }

  @Autowired
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  @Override
  public void handleSupplyRequest(
      final String patientId,
      final TherapyAssigneeEnum requesterRole,
      final String therapyCompositionUid,
      final String ehrOrderName,
      final Integer supplyInDays,
      final MedicationSupplyTypeEnum supplyType)
  {
    final String originalTherapyId = therapyEhrHandler.getOriginalTherapyId(patientId, therapyCompositionUid);
    handleSupplyRequest(patientId, requesterRole, originalTherapyId, supplyType, supplyInDays);
  }

  @Override
  public void handleSupplyRequest(
      final String patientId,
      final TherapyAssigneeEnum requesterRole,
      final String originalTherapyId,
      final MedicationSupplyTypeEnum supplyType,
      final Integer supplyInDays)
  {
    final boolean supplyProcessExists = bpmService.isProcessInExecution(originalTherapyId, PharmacySupplyProcess.class);

    if (supplyProcessExists)
    {
      if (requesterRole == TherapyAssigneeEnum.PHARMACIST)
      {
        throw new IllegalStateException("Only one active supply process allowed for therapy!");
      }
      else if (requesterRole == TherapyAssigneeEnum.NURSE)
      {
        final List<TaskDto> nurseSupplyTasks = pharmacistTaskProvider.findNurseSupplyTasksForTherapy(
            patientId,
            originalTherapyId);
        if (nurseSupplyTasks != null && !nurseSupplyTasks.isEmpty())
        {
          throw new IllegalStateException("Only one active nurse resupply request allowed for therapy!");
        }
        createNurseResupplyRequest(originalTherapyId, patientId);
      }
    }
    else
    {
      final String supplyTypeName = supplyType != null ? supplyType.name() : null;
      bpmService.startProcess(
          originalTherapyId,
          PharmacySupplyProcess.class,
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.patientId, patientId),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.originalTherapyId, originalTherapyId),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.requesterRole, requesterRole.name()),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.createResupplyReminder, requesterRole == TherapyAssigneeEnum.PHARMACIST),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.supplyInDays, supplyInDays),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.supplyType, supplyTypeName),
          Pair.<PharmacySupplyProcess, Object>of(
              PharmacySupplyProcess.dispenseMedication,
              requesterRole == TherapyAssigneeEnum.NURSE || supplyType != MedicationSupplyTypeEnum.PATIENTS_OWN));
    }
  }

  private void createNurseResupplyRequest(final String originalTherapyId, final String patientId)
  {
    bpmService.messageEventReceived(
        PharmacySupplyProcess.nurseResupplyMessage,
        originalTherapyId,
        PharmacySupplyProcess.class,
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.patientId, patientId),
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.originalTherapyId, originalTherapyId),
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.requesterRole, TherapyAssigneeEnum.NURSE.name()),
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.confirmResupply, true),
        Pair.<PharmacySupplyProcess, Object>of(PharmacySupplyProcess.dispenseMedication, true));
  }
}
