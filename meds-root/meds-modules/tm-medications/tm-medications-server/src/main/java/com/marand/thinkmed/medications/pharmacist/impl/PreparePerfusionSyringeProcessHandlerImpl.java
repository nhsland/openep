package com.marand.thinkmed.medications.pharmacist.impl;

import com.google.common.base.Preconditions;
import com.marand.ispek.bpm.service.BpmService;
import com.marand.maf.core.Pair;
import com.marand.thinkmed.bpm.medications.process.PreparePerfusionSyringeProcess;
import com.marand.thinkmed.medications.business.MedicationsBo;
import com.marand.thinkmed.medications.pharmacist.PreparePerfusionSyringeProcessHandler;
import com.marand.thinkmed.medications.therapy.ehr.TherapyEhrHandler;
import com.marand.thinkmed.request.user.RequestUser;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Klavdij Lapajne
 */
@Component
public class PreparePerfusionSyringeProcessHandlerImpl implements PreparePerfusionSyringeProcessHandler
{
  private BpmService bpmService;
  private MedicationsBo medicationsBo;
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
  public void setTherapyEhrHandler(final TherapyEhrHandler therapyEhrHandler)
  {
    this.therapyEhrHandler = therapyEhrHandler;
  }

  @Override
  public void handlePreparationRequest(
      final @NonNull String patientId,
      final @NonNull String therapyCompositionUid,
      final @NonNull String ehrOrderName,
      final int numberOfSyringes,
      final boolean urgent,
      final @NonNull DateTime dueTime,
      final @NonNull String userName,
      final boolean printSystemLabel)
  {
    final String originalTherapyId = therapyEhrHandler.getOriginalTherapyId(patientId, therapyCompositionUid);

    Preconditions.checkArgument(
        !bpmService.isProcessInExecution(originalTherapyId, PreparePerfusionSyringeProcess.class),
        "Only one active prepare perfusion syringe request allowed for therapy!");

    bpmService.startProcess(
        originalTherapyId,
        PreparePerfusionSyringeProcess.class,
        Pair.of(PreparePerfusionSyringeProcess.patientId, patientId),
        Pair.of(PreparePerfusionSyringeProcess.originalTherapyId, originalTherapyId),
        Pair.of(PreparePerfusionSyringeProcess.numberOfSyringes, numberOfSyringes),
        Pair.of(PreparePerfusionSyringeProcess.isUrgent, urgent),
        Pair.of(PreparePerfusionSyringeProcess.undoState, false),
        Pair.of(PreparePerfusionSyringeProcess.cancelPreparation, false),
        Pair.of(PreparePerfusionSyringeProcess.orderCanceled, false),
        Pair.of(PreparePerfusionSyringeProcess.therapyCanceled, false),
        Pair.of(PreparePerfusionSyringeProcess.dueDateTimeMillis, dueTime.getMillis()),
        Pair.of(PreparePerfusionSyringeProcess.orderer, RequestUser.getId()),
        Pair.of(PreparePerfusionSyringeProcess.ordererFullName, userName),
        Pair.of(PreparePerfusionSyringeProcess.printSystemLabel , printSystemLabel)
    );
  }

  @Override
  public void handleOrderCancellationMessage(
      final @NonNull String patientId,
      final @NonNull String therapyCompositionUid,
      final @NonNull String ehrOrderName)
  {
    final String originalTherapyId = therapyEhrHandler.getOriginalTherapyId(patientId, therapyCompositionUid);

    Preconditions.checkArgument(
        bpmService.isProcessInExecution(originalTherapyId, PreparePerfusionSyringeProcess.class),
        "Prepare perfusion syringe process not in active state!"
    );

    bpmService.messageEventReceived(
        PreparePerfusionSyringeProcess.cancelOrderMessage,
        originalTherapyId,
        PreparePerfusionSyringeProcess.class,
        Pair.of(PreparePerfusionSyringeProcess.patientId, patientId),
        Pair.of(PreparePerfusionSyringeProcess.originalTherapyId, originalTherapyId),
        Pair.of(PreparePerfusionSyringeProcess.cancelPreparation, true),
        Pair.of(PreparePerfusionSyringeProcess.therapyCanceled, false),
        Pair.of(PreparePerfusionSyringeProcess.orderCanceled, true));
  }

  @Override
  public void handleTherapyCancellationMessage(
      final @NonNull String patientId,
      final @NonNull String therapyCompositionUid,
      final @NonNull String ehrOrderName)
  {
    final String originalTherapyId = therapyEhrHandler.getOriginalTherapyId(patientId, therapyCompositionUid);
    handleTherapyCancellationMessage(patientId, originalTherapyId);
  }

  @Override
  public void handleTherapyCancellationMessage(final @NonNull String patientId, final @NonNull String originalTherapyId)
  {
    if (bpmService.isProcessInExecution(originalTherapyId, PreparePerfusionSyringeProcess.class))
    {
      bpmService.messageEventReceived(
          PreparePerfusionSyringeProcess.cancelTherapyMessage,
          originalTherapyId,
          PreparePerfusionSyringeProcess.class,
          Pair.of(PreparePerfusionSyringeProcess.patientId, patientId),
          Pair.of(
              PreparePerfusionSyringeProcess.originalTherapyId,
              originalTherapyId),
          Pair.of(PreparePerfusionSyringeProcess.cancelPreparation, true),
          Pair.of(PreparePerfusionSyringeProcess.therapyCanceled, true),
          Pair.of(PreparePerfusionSyringeProcess.orderCanceled, false));
    }
  }

  @Override
  public void handleMedicationAdministrationMessage(
      final @NonNull String patientId,
      final @NonNull String therapyCompositionUid,
      final @NonNull String ehrOrderName)
  {
    final String originalTherapyId = therapyEhrHandler.getOriginalTherapyId(patientId, therapyCompositionUid);

    Preconditions.checkArgument(
        bpmService.isProcessInExecution(originalTherapyId, PreparePerfusionSyringeProcess.class),
        "Prepare perfusion syringe process not in active state!"
    );

    bpmService.messageEventReceived(
        PreparePerfusionSyringeProcess.medicationAdministrationMessage,
        originalTherapyId,
        PreparePerfusionSyringeProcess.class,
        Pair.of(PreparePerfusionSyringeProcess.patientId, patientId),
        Pair.of(
            PreparePerfusionSyringeProcess.originalTherapyId,
            originalTherapyId));
    //Pair.<PreparePerfusionSyringeProcess, Object>of(PreparePerfusionSyringeProcess.cancelPreparation, false),
    //Pair.<PreparePerfusionSyringeProcess, Object>of(PreparePerfusionSyringeProcess.undoState, false));
  }
}
