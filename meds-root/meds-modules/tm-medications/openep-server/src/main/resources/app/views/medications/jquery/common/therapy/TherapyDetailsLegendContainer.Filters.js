Class.define('app.views.medications.common.therapy.TherapyDetailsLegendContainer.Filters', 'tm.jquery.Object', {
  isMedicationNonFormulary: function(medicationData)
  {
    return !medicationData.isFormulary();
  },

  isControlledDrug: function(medicationData)
  {
    return medicationData.isControlledDrug();
  },

  isCriticalDrug: function(medicationData)
  {
    return medicationData.isCriticalDrug();
  },

  isBlackTriangleMedication: function(medicationData)
  {
    return medicationData.isBlackTriangleMedication();
  },

  isUnlicensedMedication: function(medicationData)
  {
    return medicationData.isUnlicensedMedication();
  },

  isHighAlertMedication: function(medicationData)
  {
    return medicationData.isHighAlertMedication();
  },

  isClinicalTrialMedication: function(medicationData)
  {
    return medicationData.isClinicalTrialMedication();
  },
  isExpensiveDrug: function(medicationData)
  {
    return medicationData.isExpensiveDrug();
  },

  isTaskLate: function(task)
  {
    return CurrentTime.get().getTime() - new Date(task.dueTime).getTime() > 24 * 60 * 60 * 1000;
  },

  isDoctorReviewTaskActive: function(task)
  {
    var enums = app.views.medications.TherapyEnums;
    return task.taskType === enums.taskTypeEnum.DOCTOR_REVIEW;
  },

  isSwitchToOralTaskActive: function(task)
  {
    var enums = app.views.medications.TherapyEnums;
    return task.taskType === enums.taskTypeEnum.SWITCH_TO_ORAL;
  },

  isPerfusionSyringeStartTaskActive: function(task)
  {
    var enums = app.views.medications.TherapyEnums;
    return task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_START;
  },

  isPerfusionSyringeCompleteTaskActive: function(task)
  {
    var enums = app.views.medications.TherapyEnums;
    return task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_COMPLETE;
  },

  isPerfusionSyringeDispenseTaskActive: function(task)
  {
    var enums = app.views.medications.TherapyEnums;
    return task.taskType === enums.taskTypeEnum.PERFUSION_SYRINGE_DISPENSE;
  },

  isSupplyReminderTaskActive: function(task)
  {
    var enums = app.views.medications.TherapyEnums;
    return task.taskType === enums.taskTypeEnum.SUPPLY_REMINDER;
  },

  isSupplyReviewTaskActive: function(task)
  {
    var enums = app.views.medications.TherapyEnums;
    return task.taskType === enums.taskTypeEnum.SUPPLY_REVIEW;
  }
});