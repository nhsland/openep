Class.define('app.views.medications.MedicationRuleUtils', 'tm.jquery.Object', {
  /** members: configs */
  view: null,
  referenceData: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!this.referenceData)
    {
      throw Error('referenceData is undefined.');
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {String} medicationIngredientRuleEnum {@see app.views.medications.TherapyEnums.medicationRuleEnum}
   * @returns {Boolean}
   */
  isMedicationRuleSet: function(medicationData, medicationIngredientRuleEnum)
  {
    var medicationRuleExists = false;
    if (!tm.jquery.Utils.isEmpty(medicationData))
    {
      var medicationIngredients = medicationData.getMedicationIngredients();
      if (tm.jquery.Utils.isArray(medicationIngredients))
      {
        medicationIngredients.forEach(function(ingredient)
        {
          if (ingredient.getIngredientRule() === medicationIngredientRuleEnum)
          {
            medicationRuleExists = true;
          }
        });
      }
    }
    return medicationRuleExists;
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationData>} medications
   * @param {String} medicationIngredientRuleEnum {@see app.views.medications.TherapyEnums.medicationRuleEnum}
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  extractMedicationWithMedicationRule: function(medications, medicationIngredientRuleEnum) // MedicationRuleEnum.java
  {
    var self = this;
    var medicationWithRule = null;

    if (tm.jquery.Utils.isArray(medications))
    {
      medications.some(function(medication)
      {
        if (self.isMedicationRuleSet(medication, medicationIngredientRuleEnum))
        {
          medicationWithRule = medication;
          return true;
        }
        return false;
      });
    }

    return medicationWithRule;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<app.views.medications.common.dto.MedicationData>} medicationDataList
   * @returns {tm.jquery.Promise}
   */
  getParacetamolRuleForTherapy: function(therapy, medicationDataList)
  {
    var enums = app.views.medications.TherapyEnums;

    var ruleParameters = {
      medicationParacetamolRuleType: enums.medicationParacetamolRuleType.FOR_THERAPY,
      therapyDto: therapy,
      medicationDataDtoList: medicationDataList
    };

    return this.getView().getRestApi().getMedicationRule(
        enums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
        ruleParameters,
        this.getReferenceData(),
        true);
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {String} medicationIngredientRuleEnum {@see app.views.medications.TherapyEnums.medicationRuleEnum}
   * @returns {Object}
   */
  getMedicationIngredientWithIngredientRule: function(medicationData, medicationIngredientRuleEnum)
  {
    var medicationIngredient = null;
    var medicationIngredients = medicationData.getMedicationIngredients();
    if (tm.jquery.Utils.isArray(medicationIngredients))
    {
      medicationIngredients.forEach(function(ingredient)
      {
        if (ingredient.getIngredientRule() === medicationIngredientRuleEnum)
        {
          medicationIngredient = ingredient;
        }
      });
    }
    return medicationIngredient;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Date} administrationDate
   * @param {Date} administrationTime
   * @param {Number} patientId
   * @param {app.views.medications.common.dto.TherapyDose} therapyDose
   * @param {String|null} administrationId
   * @param {String} taskId
   * @returns {tm.jquery.Promise}
   */
  getParacetamolRuleForAdministration: function(medicationData,  // MedicationDataDto.java
                                                therapy,
                                                administrationDate,
                                                administrationTime,
                                                patientId,
                                                therapyDose,  // TherapyDoseDto.java
                                                administrationId,
                                                taskId)
  {
    var enums = app.views.medications.TherapyEnums;
    var deferred = tm.jquery.Deferred.create();
    var view = this.getView();

    var medicationIngredientWithRule = null;
    if (!tm.jquery.Utils.isEmpty(medicationData))
    {
      medicationIngredientWithRule = this.getMedicationIngredientWithIngredientRule(
          medicationData,
          app.views.medications.TherapyEnums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    }

    if (!tm.jquery.Utils.isEmpty(medicationIngredientWithRule) && !tm.jquery.Utils.isEmpty(administrationDate))
    {
      var selectedTimestamp = new Date(
          administrationDate.getFullYear(),
          administrationDate.getMonth(),
          administrationDate.getDate(),
          administrationTime.getHours(),
          administrationTime.getMinutes(),
          0, 0);

      var searchStart = new Date(selectedTimestamp.valueOf());
      searchStart.setHours(searchStart.getHours() - 24);
      var searchEnd = new Date(selectedTimestamp.valueOf());

      var ruleParameters =
          {
            medicationParacetamolRuleType: enums.medicationParacetamolRuleType.FOR_ADMINISTRATION,
            therapyDoseDto: therapyDose,
            taskId: taskId,
            administrationId: administrationId,
            therapyDto: therapy,
            searchInterval: {
              startMillis: searchStart.getTime(),
              endMillis: searchEnd.getTime()
            },
            patientId: view.getPatientId()
          };

      view.getRestApi()
          .getMedicationRule(
              enums.medicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE,
              ruleParameters,
              this.getReferenceData(),
              true)
          .then(
              function onSuccess(medicationRuleResult)
              {
                deferred.resolve(medicationRuleResult);
              },
              function onFailure()
              {
                deferred.reject();
              });
    }
    else
    {
      deferred.resolve();
    }
    return deferred.promise();
  },

  /**
   * @returns {tm.views.medications.TherapyView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {app.views.medications.common.patient.AbstractReferenceData}
   */
  getReferenceData: function()
  {
    return this.referenceData;
  }
});
