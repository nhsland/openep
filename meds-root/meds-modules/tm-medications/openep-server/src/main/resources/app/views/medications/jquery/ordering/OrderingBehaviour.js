Class.define('app.views.medications.ordering.OrderingBehaviour', 'tm.jquery.Object', {
  /** @type boolean */
  pastMode: false,
  /** @type boolean */
  templateOnlyMode: false,
  /** @type boolean */
  startEndTimeAvailable: true,
  /** @type boolean and forces startEndTimeAvailable to false when set to true */
  supplyAvailable: false,
  /** @type boolean and forces startEndTimeAvailable to false when set to true */
  supplyRequired: false,
  /** @type boolean */
  oxygenSaturationAvailable: true,
  /** @type boolean */
  reviewReminderAvailable: true,
  /** @type boolean */
  informationSourceAvailable: false,
  /** @type boolean */
  informationSourceRequired: false,
  /** @type boolean */
  referenceDataBasedDoseCalculationAvailable: true,
  /** @type boolean */
  universalOrderFormAvailable: true,
  /** @type boolean */
  heparinAvailable: true,
  /** @type boolean */
  infusionRateTypeSelectionAvailable: true,
  /** @type boolean */
  variableDoseAvailable: true,
  /** @type boolean */
  daysButtonAvailable: true,
  /** @type boolean */
  pastDaysOfTherapyVisible: true,
  /** @type boolean */
  indicationAvailable: true,
  /** @type boolean */
  dosingTimePatternAvailable: true,
  /** @type boolean */
  titratedDoseModeAvailable: true,
  /** @type boolean */
  filterTemplatesByActivePatient: true,
  /** @type boolean */
  addToTemplateAvailable: true,
  /** @type boolean */
  doctorsOrderAvailable: true,
  /** @type boolean */
  doseRequired: true,
  /** @type boolean */
  doseFrequencyRequired: true,
  /** @type boolean */
  doseCalculationsAvailable: false,
  /** @type boolean */
  routeOfAdministrationRequired: true,
  /** @type boolean */
  targetInrRequired: true,
  /** @type boolean */
  indicationAlwaysOptional: false,
  /** @type boolean */
  commentAlwaysOptional: false,
  /** @type boolean and requires startEndTimeAvailable to be true */
  recordAdministrationAvailable: true,
  /** @type app.views.medications.common.MedicationSearchResultFormatter|null */
  medicationSearchResultFormatter: null,

  Constructor: function (config)
  {
    this.callSuper(config);

    if (this.isSupplyAvailable() || this.isSupplyRequired())
    {
      // not compatible due to when needed dosing
      this.startEndTimeAvailable = false;
    }
    if (this.isSupplyRequired())
    {
      this.supplyAvailable = true;
    }
    if (!this.isStartEndTimeAvailable())
    {
      this.recordAdministrationAvailable = false;
    }
  },

  /**
   * @return {boolean}
   */
  isPastMode: function()
  {
    return this.pastMode === true;
  },

  /**
   * @return {boolean} true, if the ordering limited to the use of templates only. In this mode, there's no ability to
   * search for medication, and no ability to add with edit.
   */
  isTemplateOnlyMode: function()
  {
     return this.templateOnlyMode === true;
  },

  /**
   * @return {boolean} true, if the therapy templates should be loaded with respect to the current active patient. If not,
   * all templates will be loaded.
   */
  isFilterTemplatesByActivePatient: function()
  {
    return this.filterTemplatesByActivePatient === true;
  },

  /**
   * @return {boolean} true, if setting the start and end time available. If set to false, no timing fields should be
   * available.
   */
  isStartEndTimeAvailable: function()
  {
    return this.startEndTimeAvailable === true;
  },

  /**
   * @return {boolean} true if defining the therapy supply information is available. Usually intended for outpatient
   * prescribing. Setting this option to true will set {@link #startEndTimeAvailable} to false.
   */
  isSupplyAvailable: function()
  {
    return this.supplyAvailable === true;
  },

  /**
   * @return {boolean} true if the supply information is mandatory, otherwise false. Does not affect controlled drug
   * supply, which always is mandatory, when {@link #isSupplyAvailable} is true.
   */
  isSupplyRequired: function()
  {
    return this.supplyRequired === true;
  },

  /**
   * @return {boolean} true, if setting oxygen saturation is available.
   */
  isOxygenSaturationAvailable: function()
  {
    return this.oxygenSaturationAvailable === true;
  },

  /**
   * @return {boolean} true, if the review reminder functionality available? Other limitations may apply, such as the
   * configuration of the selected medication.
   */
  isReviewReminderAvailable: function()
  {
    return this.reviewReminderAvailable === true;
  },

  /**
   * @return {boolean} true, if setting the source of information for the medication / therapy available. Usually when
   * creating the medication on admission list.
   */
  isInformationSourceAvailable: function()
  {
    return this.informationSourceAvailable === true;
  },

  /**
   * @return {boolean} true, if defining the source of information is mandatory. Works in conjunction with
   * {@link #isInformationSourceAvailable}. When false, the field for selection is expected to be initially hidden,
   * and shown by an additional UI action (click of the appropriate button).
   */
  isInformationSourceRequired: function()
  {
    return this.informationSourceRequired === true;
  },

  /**
   * @return {boolean}
   */
  isHeparinAvailable: function()
  {
    return this.heparinAvailable === true;
  },

  /**
   * @return {boolean}
   */
  isInfusionRateTypeSelectionAvailable: function()
  {
    return this.infusionRateTypeSelectionAvailable === true;
  },

  /**
   * @return {boolean} true, if the variable dosing mode available (simple order form).
   */
  isVariableDoseAvailable: function()
  {
    return this.variableDoseAvailable === true;
  },

  /**
   * @return {boolean} true, if the dosing frequency options include limiting the dosing to specific days of the week.
   */
  isDaysButtonAvailable: function()
  {
    return this.daysButtonAvailable === true;
  },

  /**
   * @return {boolean}
   */
  isPastDaysOfTherapyVisible: function()
  {
    return this.pastDaysOfTherapyVisible === true;
  },

  /**
   * @return {boolean} true, if the indication field is available.
   */
  isIndicationAvailable: function()
  {
    return this.indicationAvailable === true;
  },

  /**
   * @return {boolean} true, if the dosing time pattern available.
   */
  isDosingTimePatternAvailable: function()
  {
    return this.dosingTimePatternAvailable === true;
  },

  /**
   * @return {boolean} true, if the titrated dose mode available.
   */
  isTitratedDoseModeAvailable: function()
  {
    return this.titratedDoseModeAvailable === true;
  },

  /**
   * @return {boolean} true, if the dose calculation that is based on patient's data is available.
   */
  isReferenceDataBasedDoseCalculationAvailable: function()
  {
    return this.referenceDataBasedDoseCalculationAvailable === true;
  },

  /**
   * @return {boolean} true, if the universal order form available.
   */
  isUniversalOrderFormAvailable: function()
  {
    return this.universalOrderFormAvailable === true;
  },

  /**
   * @return {boolean} true, if the ability to add the current therapy in edit to templates available. This option is
   * viable only when {@link #templateOnlyMode} is set to false, since otherwise the order form will never be displayed.
   */
  isAddToTemplateAvailable: function()
  {
    return this.addToTemplateAvailable;
  },

  /**
   * Is the ability to prescribe therapy with administrations by doctors order available?
   * @returns {boolean}
   */
  isDoctorsOrderAvailable: function()
  {
    return this.doctorsOrderAvailable === true;
  },

  /**
   * You will probably want the dose input to be mandatory.
   * @return {boolean}
   */
  isDoseRequired: function()
  {
    return this.doseRequired === true;
  },

  /**
   * You will probably want the dose frequency to be mandatory.
   * @return {boolean}
   */
  isDoseFrequencyRequired: function()
  {
    return this.doseFrequencyRequired === true;
  },

  /**
   * Are therapy dose and infusion rate calculations available?
   * @returns {boolean}
   */
  isDoseCalculationsAvailable: function()
  {
    return this.doseCalculationsAvailable === true;
  },

  /**
   * You will probably want the route of administration to be mandatory.
   * @return {boolean}
   */
  isRouteOfAdministrationRequired: function()
  {
    return this.routeOfAdministrationRequired === true;
  },

  /**
   * You will probably want the targeted INR value if the dose mode is set to titration.
   * @return {boolean}
   */
  isTargetedInrRequired: function()
  {
    return this.targetInrRequired === true;
  },

  /**
   * Since this is a safety feature you will probably want to leave it set to false and let the order form determine when
   * the input is required.
   * @return {boolean}
   */
  isIndicationAlwaysOptional: function()
  {
    return this.indicationAlwaysOptional === true;
  },

  /**
   * Some prescription types require a mandatory comment, meaning you will probably want to leave it set to false and
   * let the order form determine the requirements.
   * @return {boolean}
   */
  isCommentAlwaysOptional: function()
  {
    return this.commentAlwaysOptional === true;
  },

  /**
   * Also requires the {@link app.views.medications.ordering.OrderingBehaviour#isStartEndTimeAvailable} to be true, as it
   * otherwise makes no sense to allow auto-administering a prescription without the start date and time.
   * @return {boolean}
   */
  isRecordAdministrationAvailable: function()
  {
    return this.recordAdministrationAvailable === true;
  },

  /**
   * Returns an optional implementation of the builder to be used when displaying medication search results.
   * @see app.views.medications.common.MedicationSearchField
   * @return {app.views.medications.common.MedicationSearchResultFormatter|null}
   */
  getMedicationSearchResultFormatter: function()
  {
    return this.medicationSearchResultFormatter;
  }
});
