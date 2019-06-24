Class.define('app.views.medications.common.dto.Therapy', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.start = tm.jquery.Utils.isEmpty(jsonObject.start) ? null : new Date(jsonObject.start);
      config.end = tm.jquery.Utils.isEmpty(jsonObject.end) ? null : new Date(jsonObject.end);
      config.createdTimestamp = tm.jquery.Utils.isEmpty(jsonObject.createdTimestamp) ? null :
          new Date(jsonObject.createdTimestamp);
      config.medication = !tm.jquery.Utils.isEmpty(jsonObject.medication) ?
          new app.views.medications.common.dto.Medication(jsonObject.medication) :
          null;
      config.doseForm = !tm.jquery.Utils.isEmpty(jsonObject.doseForm) ?
          new app.views.medications.common.dto.DoseForm(jsonObject.doseForm) :
          null;
      if (!tm.jquery.Utils.isEmpty(jsonObject.ingredientsList))
      {
        jsonObject.ingredientsList.forEach(function(ingredient)
        {
          ingredient.medication = new app.views.medications.common.dto.Medication(ingredient.medication);
        });
        config.ingredientsList = jsonObject.ingredientsList;
      }

      // Only TimedDoseElements for protocol include "date" property. Has to be converted from string to Date
      if (tm.jquery.Utils.isArray(config.timedDoseElements) && config.timedDoseElements.length > 0 &&
          config.timedDoseElements[0].date)
      {
        config.timedDoseElements.forEach(function(timedDoseElement)
        {
          timedDoseElement.date = new Date(timedDoseElement.date);
        });
      }

      if (tm.jquery.Utils.isArray(jsonObject.routes) && jsonObject.routes.length > 0)
      {
        config.routes = jsonObject.routes.map(function(route)
        {
          return new app.views.medications.common.dto.MedicationRoute(route);
        });
      }
      config.releaseDetails = !!config.releaseDetails ?
          new app.views.medications.common.dto.ReleaseDetails(config.releaseDetails) :
          null;

      if (tm.jquery.Utils.isArray(jsonObject.informationSources))
      {
        config.informationSources = jsonObject.informationSources.map(function toObject(source)
        {
          return new app.views.medications.common.dto.InformationSource(source);
        });
      }

      config.dispenseDetails = !!jsonObject.dispenseDetails ?
          app.views.medications.common.dto.DispenseDetails.fromJson(jsonObject.dispenseDetails) :
          null;

      config.pastTherapyStart = !!jsonObject.pastTherapyStart ?
          new Date(jsonObject.pastTherapyStart) :
          null;

      return new app.views.medications.common.dto.Therapy(config);
    },
    RATE_TYPE_BOLUS: 'BOLUS'
  },

  compositionUid: null, /* string */
  ehrOrderName: null, /* string */
  medicationOrderFormType: null, /* enum MedicationOrderFormType */
  variable: false,
  therapyDescription: null, /* string */
  routes: null, /* array - route dto */
  dosingFrequency: null, /* dosingfrequency dto */
  dosingDaysFrequency: null, /* integer */
  daysOfWeek: null, /* array */
  start: null, /* date */
  end: null, /* date */
  whenNeeded: null, /* boolean */
  comment: null, /* string */
  clinicalIndication: null, /* NamedIdentityDto */
  prescriberName: null, /* string */
  composerName: null, /* string */
  startCriterion: null, /* string */
  applicationPrecondition: null, /* string */
  reviewReminderDays: null, /* integer */
  reviewReminderComment: null, /* String */

  speedDisplay: null, /* sting */
  frequencyDisplay: null, /* string */
  daysFrequencyDisplay: null, /* string */
  whenNeededDisplay: null, /* string */
  startCriterionDisplay: null, /* string */
  daysOfWeekDisplay: null, /* string */
  applicationPreconditionDisplay: null, /* string */

  formattedTherapyDisplay: null, /* string */
  pastDaysOfTherapy: null, /* integer */

  linkName: null, /* string */

  maxDailyFrequency: null, /* integer */
  maxDosePercentage: null, /* integer */

  createdTimestamp: null, /* Date */
  criticalWarnings: null, /* array */
  admissionId: null, /* string */
  completed: null, /* boolean, used for the ordering container */
  medication: null, /* Medication.js */
  titration: null, /* TitrationType */
  continuousInfusion: null, /* Boolean */
  recurringContinuousInfusion: null, /* Boolean */
  doseType: null, /* TherapyDoseTypeEnum */
  ingredientsList: null, /* InfusionIngredientDto */
  doseElement: null, /* ComplexDoseElementDto */
  timedDoseElements: null, /* Array */
  doseTimes: null, /* array */

  quantityUnit: null, /* string */
  doseForm: null, /* Object */
  volumeSum: null, /* number */
  volumeSumUnit: null, /* string */
  volumeSumDisplay: null, /* string */
  quantityDisplay: null, /* string */
  dispenseDetails: null, /* DispenseDetailsDto object */
  adjustToFluidBalance: null, /* Boolean */
  speedFormulaDisplay: null, /* String */
  additionalInstruction: null, /* String */
  additionalInstructionDisplay: null, /* String */
  durationDisplay: null, /* String */
  selfAdministeringActionEnum: null, /* String */
  releaseDetails: null,
  addToDischargeLetter: null, /* Boolean */
  informationSources: null,
  targetInr: null, /* number */
  pastTherapyStart: null, /* Date */

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.daysOfWeek = tm.jquery.Utils.isArray(this.daysOfWeek) ? this.daysOfWeek : [];
    this.criticalWarnings = tm.jquery.Utils.isArray(this.criticalWarnings) ? this.criticalWarnings : [];
    this.informationSources = tm.jquery.Utils.isArray(this.informationSources) ? this.informationSources : [];
    this.completed = this.getConfigValue("completed", true);
    this.routes = tm.jquery.Utils.isArray(this.routes) ? this.routes : [];
  },

  getCompositionUid: function()
  {
    return this.compositionUid;
  },
  getEhrOrderName: function()
  {
    return this.ehrOrderName;
  },
  getMedicationOrderFormType: function()
  {
    return this.medicationOrderFormType;
  },
  /**
   * @returns {boolean}
   */
  isOrderTypeOxygen: function()
  {
    return this.medicationOrderFormType === app.views.medications.TherapyEnums.medicationOrderFormType.OXYGEN;
  },
  /**
   * @returns {boolean}
   */
  isOrderTypeComplex: function()
  {
    return this.medicationOrderFormType === app.views.medications.TherapyEnums.medicationOrderFormType.COMPLEX;
  },
  /**
   * @returns {boolean}
   */
  isOrderTypeSimple: function()
  {
    return this.medicationOrderFormType === app.views.medications.TherapyEnums.medicationOrderFormType.SIMPLE;
  },
  /**
   * @returns {boolean}
   */
  isOrderTypeDescriptive: function()
  {
    return this.medicationOrderFormType === app.views.medications.TherapyEnums.medicationOrderFormType.DESCRIPTIVE;
  },
  /**
   * @returns {boolean}
   */
  isVariable: function()
  {
    return this.variable === true &&
        tm.jquery.Utils.isArray(this.getTimedDoseElements()) &&
        this.getTimedDoseElements().length > 0;
  },
  /**
   * @returns {boolean}
   */
  isRecurringContinuousInfusion: function()
  {
    return this.recurringContinuousInfusion === true;
  },
  /**
   * @returns {Boolean}
   */
  isAdjustToFluidBalance: function()
  {
    return this.adjustToFluidBalance === true;
  },
  getTherapyDescription: function()
  {
    return this.therapyDescription;
  },
  /**
   * @returns {Array<app.views.medications.common.dto.MedicationRoute>}
   */
  getRoutes: function()
  {
    return this.routes;
  },
  getDosingFrequency: function()
  {
    return this.dosingFrequency;
  },
  getDosingDaysFrequency: function()
  {
    return this.dosingDaysFrequency;
  },
  getDaysOfWeek: function()
  {
    return this.daysOfWeek;
  },
  getStart: function()
  {
    return this.start;
  },
  getEnd: function()
  {
    return this.end;
  },
  getWhenNeeded: function()
  {
    return this.whenNeeded === true;
  },
  getComment: function()
  {
    return this.comment;
  },
  getClinicalIndication: function()
  {
    return this.clinicalIndication;
  },
  getPrescriberName: function()
  {
    return this.prescriberName;
  },
  getComposerName: function()
  {
    return this.composerName;
  },
  getStartCriterion: function()
  {
    return this.startCriterion;
  },
  getApplicationPrecondition: function()
  {
    return this.applicationPrecondition;
  },
  getReviewReminderDays: function()
  {
    return this.reviewReminderDays;
  },

  /**
   * @returns {String|null}
   */
  getReviewReminderComment: function()
  {
    return this.reviewReminderComment;
  },
  /**
   * @returns {Number|null}
   */
  getVolumeSum: function()
  {
    return this.volumeSum;
  },
  /**
   * @returns {String|null}
   */
  getVolumeSumUnit: function()
  {
    return this.volumeSumUnit;
  },
  /**
   * @returns {String|null}
   */
  getVolumeSumDisplay: function()
  {
    return this.volumeSumDisplay;
  },
  /**
   * @returns {String|null}
   */
  getQuantityDisplay: function()
  {
    return this.quantityDisplay;
  },
  /**
   * @returns {app.views.medications.common.dto.DoseForm|null}
   */
  getDoseForm: function()
  {
    return this.doseForm;
  },

  /**
   * Coded text value.
   * @returns {String|null}
   */
  getAdditionalInstruction: function()
  {
    return this.additionalInstruction;
  },

  /**
   * Description value of the coded text value stored under {@link additionalInstruction}.
   * @returns {String|null}
   */
  getAdditionalInstructionDisplay: function()
  {
    return this.additionalInstructionDisplay;
  },
  /**
   * @returns {String|null}
   */
  getSpeedFormulaDisplay: function()
  {
    return this.speedFormulaDisplay;
  },
  /**
   * @returns {String|null}
   */
  getSpeedDisplay: function()
  {
    return this.speedDisplay;
  },
  /**
   * @returns {String|null}
   */
  getDurationDisplay: function()
  {
    return this.durationDisplay;
  },
  getFrequencyDisplay: function()
  {
    return this.frequencyDisplay;
  },
  getDaysFrequencyDisplay: function()
  {
    return this.daysFrequencyDisplay;
  },
  getWhenNeededDisplay: function()
  {
    return this.whenNeededDisplay;
  },
  getStartCriterionDisplay: function()
  {
    return this.startCriterionDisplay;
  },
  getDaysOfWeekDisplay: function()
  {
    return this.daysOfWeekDisplay;
  },
  getApplicationPreconditionDisplay: function()
  {
    return this.applicationPreconditionDisplay;
  },

  getFormattedTherapyDisplay: function()
  {
    return this.formattedTherapyDisplay;
  },
  getPastDaysOfTherapy: function()
  {
    return this.pastDaysOfTherapy;
  },

  getLinkName: function()
  {
    return this.linkName;
  },

  getMaxDailyFrequency: function()
  {
    return this.maxDailyFrequency;
  },

  getCreatedTimestamp: function()
  {
    return this.createdTimestamp;
  },

  getCriticalWarnings: function()
  {
    return this.criticalWarnings;
  },

  getSelfAdministeringActionEnum: function()
  {
    return this.selfAdministeringActionEnum;
  },

  /**
   * @returns {Array<app.views.medications.common.dto.InformationSource>} the list of information sources on which
   * this therapy is based.
   */
  getInformationSources: function()
  {
    return this.informationSources;
  },

  /**
   * @return {string|null}
   */
  getAdmissionId: function()
  {
    return this.admissionId;
  },

  /**
   * @param {string|null} value
   */
  setAdmissionId: function(value)
  {
    this.admissionId = value;
  },

  /**
   * @return {boolean}
   */
  isLinkedToAdmission: function()
  {
    return !tm.jquery.Utils.isEmpty(this.admissionId);
  },

  /**
   * @return {boolean}
   */
  isCompleted: function()
  {
    return this.completed === true;
  },

  /**
   * @return {boolean}
   */
  isAddToDischargeLetter: function()
  {
    return this.addToDischargeLetter === true;
  },

  /**
   * @param {string|null} value
   * @return {app.views.medications.common.dto.Therapy}
   */
  setCompositionUid: function(value)
  {
    this.compositionUid = value;
    return this;
  },

  setEhrOrderName: function(value)
  {
    this.ehrOrderName = value;
  },
  setMedicationOrderFormType: function(value)
  {
    this.medicationOrderFormType = value;
  },
  setVariable: function(value)
  {
    this.variable = value;
  },
  setTherapyDescription: function(value)
  {
    this.therapyDescription = value;
  },
  /**
   * @param {Array<app.views.medications.common.dto.MedicationRoute>} routes
   */
  setRoutes: function(routes)
  {
    this.routes = routes;
  },
  setDosingFrequency: function(value)
  {
    this.dosingDaysFrequency = value;
  },
  setDosingDaysFrequency: function(value)
  {
    this.dosingDaysFrequency = value;
  },
  setDaysOfWeek: function(values)
  {
    this.daysOfWeek = tm.jquery.Utils.isArray(values) ? values : [];
  },
  setStart: function(value)
  {
    this.start = value;
  },
  setEnd: function(value)
  {
    this.end = value;
  },
  setWhenNeeded: function(value)
  {
    this.whenNeeded = value;
  },
  setComment: function(value)
  {
    this.comment = value;
  },
  setClinicalIndication: function(value)
  {
    this.clinicalIndication = value;
  },
  setPrescriberName: function(value)
  {
    this.prescriberName = value;
  },
  setComposerName: function(value)
  {
    this.composerName = value;
  },
  setStartCriterion: function(value)
  {
    this.startCriterion = value;
  },
  setApplicationPrecondition: function(value)
  {
    this.applicationPrecondition = value;
  },
  setReviewReminderDays: function(value)
  {
    this.reviewReminderDays = value;
  },

  /**
   * @param {String|null} value
   */
  setReviewReminderComment: function(value)
  {
    this.reviewReminderComment = value;
  },
  /**
   * @param {Number|null} value
   */
  setVolumeSum: function(value)
  {
    this.volumeSum = value;
  },
  /**
   * @param {String|null} value
   */
  setVolumeSumUnit: function(value)
  {
    this.volumeSumUnit = value;
  },
  /**
   * @param {String} value
   */
  setSpeedDisplay: function(value)
  {
    this.speedDisplay = value;
  },
  setFrequencyDisplay: function(value)
  {
    this.frequencyDisplay = value;
  },
  setDaysFrequencyDisplay: function(value)
  {
    this.daysFrequencyDisplay = value;
  },
  setWhenNeededDisplay: function(value)
  {
    this.whenNeededDisplay = value;
  },
  setStartCriterionDisplay: function(value)
  {
    this.startCriterionDisplay = value;
  },
  setDaysOfWeekDisplay: function(value)
  {
    this.daysOfWeekDisplay = value;
  },
  setApplicationPreconditionDisplay: function(value)
  {
    this.applicationPreconditionDisplay = value;
  },

  setFormattedTherapyDisplay: function(value)
  {
    this.formattedTherapyDisplay = value;
  },
  setPastDaysOfTherapy: function(value)
  {
    this.pastDaysOfTherapy = value;
  },



  /**
   * @param {string|null} value
   * @return {app.views.medications.common.dto.Therapy}
   */
  setLinkName: function(value)
  {
    this.linkName = value;
    return this;
  },

  setMaxDailyFrequency: function(value)
  {
    this.maxDailyFrequency = value;
  },

  setCreatedTimestamp: function(value)
  {
    this.createdTimestamp = value;
  },
  setTags: function(values)
  {
    this.tags = tm.jquery.Utils.isArray(value) ? values : [];
  },
  setCriticalWarnings: function(values)
  {
    this.criticalWarnings = tm.jquery.Utils.isArray(values) ? values : [];
    return this;
  },

  /**
   * @param {boolean} value
   */
  setCompleted: function(value)
  {
    this.completed = value;
  },

  getTherapyId: function()
  {
    return !!this.compositionUid ?
        app.views.medications.MedicationUtils.getUidWithoutVersion(this.compositionUid) + '|' + this.ehrOrderName :
        null;
  },

  /**
   *
   * @param {app.views.medications.common.dto.Medication|null} medication
   */
  setMedication: function(medication)
  {
    this.medication = medication;
  },

  /**
   *
   * @returns {app.views.medications.common.dto.Medication|null}
   */
  getMedication: function()
  {
    return this.medication;
  },

  /**
   * @returns {boolean}
   */
  isTitrationDoseType: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getTitration());
  },

  /**
   * @param {string} type
   */
  setTitration: function(type)
  {
    if (type && !app.views.medications.TherapyEnums.therapyTitrationTypeEnum.hasOwnProperty(type))
    {
      console.warn("Unknown titration type enum set to Therapy.js.");
    }
    this.titration = type;
  },

  /**
   * @returns {string|null}
   */
  getTitration: function()
  {
    return this.titration;
  },

  /**
   * @returns {boolean}
   */
  isContinuousInfusion: function()
  {
    return this.continuousInfusion === true;
  },

  /**
   * @returns {boolean}
   */
  isBaselineInfusion: function()
  {
    return this.baselineInfusion === true;
  },

  /**
   * @param {boolean} value
   */
  setContinuousInfusion: function(value)
  {
    this.continuousInfusion = value;
  },

  /**
   * @param {boolean} value
   */
  setBaselineInfusion: function(value)
  {
    this.baselineInfusion = value;
  },

  /**
   * @returns {TherapyDoseTypeEnum}
   */
  getDoseType: function()
  {
    return this.doseType;
  },

  /**
   * @param {TherapyDoseTypeEnum} value
   */
  setDoseType: function(value)
  {
    this.doseType = value;
  },

  /**
   * @returns {array}
   */
  getIngredientsList: function()
  {
    return this.ingredientsList;
  },

  /**
   * @param {array} ingredientsList
   */
  setIngredientsList: function(ingredientsList)
  {
    this.ingredientsList = ingredientsList;
  },

  /**
   * @returns {Object|null}
   */
  getDoseElement: function()
  {
    return this.doseElement;
  },

  /**
   * @param {Object|null} doseElement
   */
  setDoseElement: function(doseElement)
  {
    this.doseElement = doseElement;
  },
  /**
   * @returns {array|null}
   */
  getTimedDoseElements: function()
  {
    return this.timedDoseElements;
  },

  /**
   * @param {Array<*>|null} timedDoseElements
   */
  setTimedDoseElements: function(timedDoseElements)
  {
    this.timedDoseElements = timedDoseElements;
  },

  /**
   * Set the information source on which this therapy is based.
   * @param {Array<app.views.medications.common.dto.InformationSource>} values
   * @return {app.views.medications.common.dto.Therapy}
   */
  setInformationSources: function(values)
  {
    this.informationSources = tm.jquery.Utils.isArray(values) ? values : [];
    return this;
  },

  /**
   * @returns {boolean}
   */
  hasUniversalIngredient: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.getMedication()))
    {
      if (this.getMedication().isMedicationUniversal())
      {
        return true;
      }
    }
    else if (!tm.jquery.Utils.isEmpty(this.getIngredientsList()))
    {
      for (var i = 0; i < this.getIngredientsList().length; i++)
      {
        if (this.getIngredientsList()[i].medication.isMedicationUniversal())
        {
          return true;
        }
      }
    }
    return false;
  },

  /**
   * @returns {boolean}
   */
  hasNonUniversalIngredient: function()
  {
    if (!tm.jquery.Utils.isEmpty(this.getMedication()))
    {
      if (!this.getMedication().isMedicationUniversal())
      {
        return true;
      }
    }
    else if (!tm.jquery.Utils.isEmpty(this.getIngredientsList()))
    {
      for (var i = 0; i < this.getIngredientsList().length; i++)
      {
        if (!this.getIngredientsList()[i].medication.isMedicationUniversal())
        {
          return true;
        }
      }
    }
    return false;
  },

  /**
   * @returns {String}
   */
  getQuantityUnit: function()
  {
    return this.quantityUnit;
  },

  /**
   * @param {String} quantityUnit
   */
  setQuantityUnit: function(quantityUnit)
  {
    this.quantityUnit = quantityUnit;
  },

  /**
   * @returns {Array|null}
   */
  getDoseTimes: function()
  {
    return this.doseTimes;
  },
  /**
   * @param {Array|null} value
   */
  setDoseTimes: function(value)
  {
    this.doseTimes = value;
  },

  /**
   * @returns {app.views.medications.common.dto.DispenseDetails|null}
   */
  getDispenseDetails: function()
  {
    return this.dispenseDetails;
  },

  /**
   * @param {app.views.medications.common.dto.DispenseDetails|null} value
   */
  setDispenseDetails: function(value)
  {
    this.dispenseDetails = value;
  },

  /**
   * @returns {Number|null}
   */
  getMaxDosePercentage: function()
  {
    return this.maxDosePercentage;
  },
  /**
   * @param {Number|null} value
   */
  setMaxDosePercentage: function(value)
  {
    this.maxDosePercentage = value;
  },

  /**
   * @returns {String|null}
   */
  getRateString: function()
  {
    return this.rateString;
  },

  /**
   * @param {String|null} value
   */
  setRateString: function(value)
  {
    this.rateString = value;
  },

  /**
   * @return {app.views.medications.common.dto.ReleaseDetails|null}
   */
  getReleaseDetails: function()
  {
    return this.releaseDetails;
  },

  /**
   * @param {app.views.medications.common.dto.ReleaseDetails} value
   */
  setReleaseDetails: function(value)
  {
    this.releaseDetails = value;
  },

  /**
   * @returns {Number|null}
   */
  getTargetInr: function()
  {
    return this.targetInr;
  },

  /**
   * @param {Number|null} targetInr
   */
  setTargetInr: function(targetInr)
  {
    this.targetInr = targetInr;
  },

  /**
   * @returns {Date|null}
   */
  getPastTherapyStart: function()
  {
    return this.pastTherapyStart;
  },

  /**
   * @param {Date|null} pastTherapyStart
   */
  setPastTherapyStart: function(pastTherapyStart)
  {
    this.pastTherapyStart = pastTherapyStart;
  },

  /**
   * @returns {boolean}
   */
  isNormalVariableInfusion: function()
  {
    return this.isOrderTypeComplex() &&
        tm.jquery.Utils.isArray(this.getTimedDoseElements()) &&
        this.getTimedDoseElements().length > 0 && !this.isContinuousInfusion();
  },

  /**
   * @returns {app.views.medications.common.dto.Medication|null}
   */
  getMainMedication: function()
  {
    if (this.isOrderTypeComplex())
    {
      return this.getIngredientsList()[0].medication;
    }

    return this.getMedication();
  },

  /**
   * @returns {Array<String>}
   */
  getAllIngredientIds: function()
  {
    var idsArray = [];
    if (this.getIngredientsList())
    {
      this.getIngredientsList().forEach(function(ingredient)
      {
        if (!ingredient.medication.isMedicationUniversal())
        {
          idsArray.push(ingredient.medication.getId());
        }
      });
    }
    else
    {
      if (this.getMedication() && !this.getMedication().isMedicationUniversal())
      {
        idsArray.push(this.getMedication().getId());
      }
    }
    return idsArray;
  },

  /**
   * @returns {boolean}
   */
  isTherapyWithDurationAdministrations: function()
  {
    return this.isOrderTypeComplex() && this.isDoseTypeWithRate();
  },

  /**
   * @returns {boolean}
   */
  isDoseTypeWithRate: function()
  {
    var enums = app.views.medications.TherapyEnums;

    return this.getDoseType() === enums.therapyDoseTypeEnum.RATE ||
        this.getDoseType() === enums.therapyDoseTypeEnum.RATE_QUANTITY ||
        this.getDoseType() === enums.therapyDoseTypeEnum.RATE_VOLUME_SUM;
  },

  /**
   * Returns true if the therapy is an infusion of type rate (as opposed to a continuous infusion, bolus or injection)
   * @returns {boolean}
   */
  isInfusionTypeRate: function()
  {
    return this.isDoseTypeWithRate() &&
        !this.isContinuousInfusion() &&
        !this.isOrderTypeOxygen();
  },

  /**
   * @return {boolean}
   */
  isRateTypeBolus: function()
  {
    return this.isOrderTypeComplex() && this.getRateString() === app.views.medications.common.dto.Therapy.RATE_TYPE_BOLUS;
  },

  /**
   * Fixes therapy start to corresponding date, fixes therapy end accordingly unless specified otherwise
   * @param {Boolean} clearEnd
   */
  rescheduleTherapyTimings: function(clearEnd)
  {
    var timingUtils = app.views.medications.MedicationTimingUtils;
    var enums = app.views.medications.TherapyEnums;

    var nextAdministrationTimestamp;
    if (this.isVariable())
    {
      if (this.isDoseTypeWithRate())
      {
        nextAdministrationTimestamp =
            timingUtils.getNextAdministrationTimestampForVarioWithRate(this.getTimedDoseElements());
      }
      else
      {
        nextAdministrationTimestamp =
            timingUtils.getNextAdministrationTimestampForVario(this.getTimedDoseElements())
      }
    }
    else
    {
      var pattern = null;
      if (!tm.jquery.Utils.isEmpty(this.getDosingFrequency()) &&
          this.getDosingFrequency().type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        var frequencyKey = timingUtils.getFrequencyKey(this.getDosingFrequency());
        pattern = timingUtils.getPatternForFrequencyBetweenHours(this.getDoseTimes()[0], frequencyKey);
      }
      else
      {
        pattern = this.getDoseTimes();
      }
      var nextTime = timingUtils.getNextTimeFromPattern(pattern, this.getDaysOfWeek());
      nextAdministrationTimestamp = nextTime ? nextTime.time : timingUtils.getTimestampRoundedUp(CurrentTime.get(), 5);
    }
    var initialTherapyDuration = this.getTherapyDurationInMilliseconds();

    this.setStart(nextAdministrationTimestamp ? nextAdministrationTimestamp : CurrentTime.get());

    if (!tm.jquery.Utils.isEmpty(this.getDosingFrequency()) &&
        enums.dosingFrequencyTypeEnum.ONCE_THEN_EX === this.getDosingFrequency().type)
    {
      if (!tm.jquery.Utils.isEmpty(this.getDoseElement()) &&
          !tm.jquery.Utils.isEmpty(this.getDoseElement().duration))
      {
        this.setEnd(moment(this.getStart()).add(this.getDoseElement().duration, 'minutes').toDate());
      }
      else
      {
        this.setEnd(new Date(this.getStart().getTime()));
      }
    }
    else if (clearEnd)
    {
      this.setEnd(null);
    }
    else if (!app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(this.getTimedDoseElements()))
    {
      if (initialTherapyDuration)
      {
        this.setEnd(new Date(this.getStart().getTime() + initialTherapyDuration));
      }
      else
      {
        this.setEnd(null);
      }
    }
    else
    {
      this.rescheduleVariableDaysDoseTherapyTiming();
    }
  },

  /**
   * This method modifies the existing Therapy instance! It reschedules the dates of the timedDoseElements for variable days
   * dose data (protocol). Does nothing in case of descriptive variable days doses, since there is no structured timing
   * information. It calculates the difference between the current day and initially prescribed date and reschedules
   * according to difference.
   */
  rescheduleVariableDaysDoseTherapyTiming: function()
  {
    if (app.views.medications.MedicationUtils.isTherapyWithDescriptiveVariableDaysDose(this.getTimedDoseElements()))
    {
       return;
    }

    var firstDate = null;
    for (var i = 0; i < this.getTimedDoseElements().length; i++)
    {
      var date = new Date(this.getTimedDoseElements()[i].date);
      if (firstDate === null || date < firstDate)
      {
        firstDate = date;
      }
    }
    var today = CurrentTime.get();
    today.setHours(0, 0, 0, 0);
    var offset = today.getTime() - firstDate.getTime();
    var lastElement = this.getTimedDoseElements()[this.getTimedDoseElements().length - 1];
    var lastElementTimestamp = new Date(
        lastElement.date.getFullYear(),
        lastElement.date.getMonth(),
        lastElement.date.getDate(),
        lastElement.doseTime.hour,
        lastElement.doseTime.minute);

    var endOffset = this.getEnd() ? this.getEnd().getTime() - lastElementTimestamp.getTime() : null;

    for (var j = 0; j < this.getTimedDoseElements().length; j++)
    {
      this.getTimedDoseElements()[j].date = new Date(new Date(this.getTimedDoseElements()[j].date).getTime() + offset);
    }

    var initialProtocolInterval =
        app.views.medications.MedicationTimingUtils.getVariableDaysTherapyInterval(this.getTimedDoseElements());
    this.setStart(initialProtocolInterval.start);
    if (this.getEnd())
    {
      if (endOffset)
      {
        this.setEnd(new Date(initialProtocolInterval.end.getTime() + endOffset));
      }
      else
      {
        this.setEnd(initialProtocolInterval.end);
      }
    }
  },

  /**
   * Returns therapy duration in milliseconds
   * @returns {Long}
   */
  getTherapyDurationInMilliseconds: function()
  {
    return this.getStart() && this.getEnd() ? this.getEnd() - this.getStart() : null;
  },

  /**
   * Returns true if the therapy is presumed to have already been started.
   * @returns {boolean}
   */
  isStarted: function()
  {
    return !!this.getStart() && this.getStart() < CurrentTime.get();
  },

  /**
   * Returns a copy of timedDoseElements for continuous infusion with variable rate.
   * Calculates difference between initial prescription times and creates new array with rescheduled doseTime hours/minutes.
   * @returns {Array<Object>}
   */
  getRescheduledVariableRateTiming: function()
  {
    var timingUtils = app.views.medications.MedicationTimingUtils;
    var timedDoseElements = [];
    var initialStart = timingUtils.hourMinuteToDate(this.getTimedDoseElements()[0].doseTime);
    var nextPossibleStart = timingUtils.getTimestampRoundedUp(CurrentTime.get(), 5);
    var diff = Math.abs(initialStart.getTime() - nextPossibleStart.getTime());

    this.getTimedDoseElements().forEach(function(timedDoseElement)
    {
      var initialDoseTime = timingUtils.hourMinuteToDate(timedDoseElement.doseTime);
      var adjustedTime = timingUtils.getTimestampRoundedUp(initialDoseTime.getTime() + diff, 5);

      var rescheduledTimedDoseElement = jQuery.extend(true, {}, timedDoseElement);
      rescheduledTimedDoseElement.doseTime.hour = adjustedTime.getHours();
      rescheduledTimedDoseElement.doseTime.minute = adjustedTime.getMinutes();

      timedDoseElements.push(rescheduledTimedDoseElement);
    });
    return timedDoseElements;
  },

  /**
   * @returns {Number}
   */
  getRemainingDurationInMinutes: function()
  {
    var currentFormatted = moment(CurrentTime.get()).set({'seconds': 0, 'millisecond': 0});
    var endFormatted = moment(this.getEnd()).set({'seconds': 0, 'millisecond': 0});

    return endFormatted.diff(currentFormatted, 'minutes');
  },

  /**
   * To return true therapy has to have an end and has to be active &&
   * therapy duration has to be longer than 24 hours && therapy has to have remaining duration less or same to 24 hours.
   * @param {Date} originalTherapyStart
   * @returns {Boolean}
   */
  isTherapyExpiring: function(originalTherapyStart)
  {
    var therapyEnd = this.getEnd();

    if (!tm.jquery.Utils.isEmpty(therapyEnd))
    {
      var endFormatted = moment(therapyEnd).set({'seconds': 0, 'millisecond': 0});
      var startFormatted = moment(originalTherapyStart).set({'seconds': 0, 'millisecond': 0});
      var currentFormatted = moment(CurrentTime.get()).set({'seconds': 0, 'millisecond': 0});

      var therapyDurationInHours = endFormatted.diff(startFormatted, 'hours');

      if (therapyDurationInHours > 24 && currentFormatted.isSameOrBefore(endFormatted) &&
          this.getRemainingDurationInMinutes() <= 1440)
      {
        return true;
      }
    }
    return false;
  },

  /**
   * Returns first infusion ingredient medication id for complex, or therapy medication id for other therapies
   * @returns {Number}
   */
  getMedicationId: function()
  {
    if (this.isOrderTypeComplex())
    {
      return this.getIngredientsList()[0].medication.getId();
    }
    else
    {
      return this.getMedication().getId();
    }
  },

  /**
   * Check if any medication on therapy is {@link app.views.medications.TherapyEnums.medicationTypeEnum.BLOOD_PRODUCT}
   * @returns {boolean}
   */
  hasBloodProduct: function()
  {
    var medicationTypeEnums = app.views.medications.TherapyEnums.medicationTypeEnum;
    if (this.isOrderTypeComplex())
    {
      return this.getIngredientsList().some(function(ingredient)
      {
        return ingredient.medication.getMedicationType() === medicationTypeEnums.BLOOD_PRODUCT;
      });
    }
    return this.getMedication() ?
        this.getMedication().getMedicationType() === medicationTypeEnums.BLOOD_PRODUCT :
        false;
  },

  /**
   * Are any therapy ingredients not diluents?
   * @returns {boolean}
   */
  hasNonDiluentIngredient: function()
  {
    var medicationTypeEnums = app.views.medications.TherapyEnums.medicationTypeEnum;
    if (this.isOrderTypeComplex())
    {
      return this.getIngredientsList().some(function(ingredient)
      {
        return ingredient.medication.getMedicationType() !== medicationTypeEnums.DILUENT;
      });
    }
    return this.getMedication() ?
        this.getMedication().getMedicationType() !== medicationTypeEnums.DILUENT :
        false;
  },

  /**
   * Helper for retrieving the dosing information regardless of the order form type
   * @returns {{doseNumerator: Number|null, doseDenominator: Number|null}}
   */
  getTherapyDose: function()
  {
    var doseNumerator = null;
    var doseDenominator = null;

    if (!tm.jquery.Utils.isEmpty(this.getDoseElement()) && !tm.jquery.Utils.isEmpty(this.getDoseElement().quantity))
    {
      doseNumerator = this.getDoseElement().quantity;
      if (!tm.jquery.Utils.isEmpty(this.getDoseElement().quantityDenominator))
      {
        doseDenominator = this.getDoseElement().quantityDenominator;
      }
    }
    else if (!tm.jquery.Utils.isEmpty(this.getIngredientsList()) &&
        !tm.jquery.Utils.isEmpty(this.getIngredientsList()[0].quantity))
    {
      doseNumerator = this.getIngredientsList()[0].quantity;
      if (!tm.jquery.Utils.isEmpty(this.getIngredientsList()[0].quantityDenominator))
      {
        doseDenominator = this.getIngredientsList()[0].quantityDenominator;
      }
    }
    return {doseNumerator: doseNumerator, doseDenominator: doseDenominator}
  },

  /**
   * Cloning capability - until we figure out there's an issue with this solution and we revert to using constructors.
   * Preserves the correct constructor, so instanceof checks work. Override in any extended classes.
   *
   * Does not (yet) clone child objects which aren't plane objects (such as instances of
   * app.views.medications.common.dto.Medication and  app.views.medications.common.dto.DoseForm). If such need arises,
   * we need to manually clone them before returning.
   *
   * @params {boolean} [deep=true]
   * @returns {app.views.medications.common.dto.Therapy}
   */
  clone: function(deep)
  {
    return deep !== false ?
        jQuery.extend(true, new app.views.medications.common.dto.Therapy(), this) :
        jQuery.extend(new app.views.medications.common.dto.Therapy(), this);
  }
});