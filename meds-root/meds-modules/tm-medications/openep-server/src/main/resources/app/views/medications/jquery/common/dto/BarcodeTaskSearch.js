Class.define('app.views.medications.common.dto.BarcodeTaskSearch', 'tm.jquery.Object', {
  statics: {
    TASK_FOUND: "TASK_FOUND",
    NO_MEDICATION: "NO_MEDICATION",
    NO_TASK: "NO_TASK",
    MULTIPLE_TASKS: "MULTIPLE_TASKS"
  },
  barcodeSearchResult: null,
  medicationId: null,
  taskId: null,

  /**
   * @returns {String}
   */
  getBarcodeSearchResult: function()
  {
    return this.barcodeSearchResult;
  },

  /**
   * @param {String} barcodeSearchResult
   */
  setBarcodeSearchResult: function(barcodeSearchResult)
  {
    this.barcodeSearchResult = barcodeSearchResult;
  },

  /**
   * @returns {Number}
   */
  getMedicationId: function()
  {
    return this.medicationId;
  },

  /**
   * @param {Number} medicationId
   */
  setMedicationId: function(medicationId)
  {
    this.medicationId = medicationId;
  },

  /**
   * @returns {Number}
   */
  getTaskId: function()
  {
    return this.taskId;
  },

  /**
   * @param {Number} taskId
   */
  setTaskId: function(taskId)
  {
    this.taskId = taskId;
  },

  /**
   * @returns {boolean}
   */
  isFailed: function()
  {
    return this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.NO_MEDICATION ||
        this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.NO_TASK ||
        this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.MULTIPLE_TASKS
  },

  /**
   * @returns {boolean}
   */
  isTaskFound: function()
  {
    return this.getBarcodeSearchResult() === app.views.medications.common.dto.BarcodeTaskSearch.TASK_FOUND;
  },

  /**
   * @returns {String}
   */
  getFailedMessageKey: function()
  {
    switch (this.getBarcodeSearchResult())
    {
      case app.views.medications.common.dto.BarcodeTaskSearch.MULTIPLE_TASKS:
        return 'scanned.medication.multiple.administrations';
      case app.views.medications.common.dto.BarcodeTaskSearch.NO_TASK:
        return "scanned.medication.no.due.administration";
      case app.views.medications.common.dto.BarcodeTaskSearch.NO_MEDICATION:
        return "scanned.code.not.in.database";
    }
  }
});