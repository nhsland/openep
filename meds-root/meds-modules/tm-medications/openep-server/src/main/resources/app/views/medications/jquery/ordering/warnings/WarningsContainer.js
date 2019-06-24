Class.define('app.views.medications.ordering.warnings.WarningsContainer', 'app.views.medications.warnings.BaseWarningsContainer', {
  /** @type app.views.medications.common.patient.AbstractReferenceData */
  referenceData: null,
  /** @type boolean */
  overrideAllowed: true,

  /** @type tm.jquery.Container|null */
  _header: null,
  /** @type tm.jquery.Container|null */
  _warningsCounterContainer: null,
  /** @type Array<app.views.medications.ordering.warnings.WarningOverride> */
  _overriddenWarnings: null,
  /** @type Array<app.views.medications.common.dto.Therapy> */
  _patientMedications: null,
  /** @type boolean */
  _loadingWarnings: false,
  /** @type boolean */
  _destroying: false,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!this.referenceData)
    {
      throw new Error('referenceData must be defined.');
    }
    this._overriddenWarnings = [];
    this._patientMedications = [];
  },

  /**
   * @param {app.views.medications.warnings.dto.MedicationsWarning} item
   * @Override
   */
  buildListRow: function(item)
  {
    var self = this;
    var listRow = new app.views.medications.warnings.WarningsContainerRow({
      view: this.getView(),
      warning: item,
      override: self._findOverrideForWarningByDescription(item),
      overrideAllowed: this.isOverrideAllowed()
    });

    listRow.on(
        app.views.medications.warnings.WarningsContainerRow.EVENT_TYPE_NEW_WARNING_OVERRIDE,
        function eventHandler(eventData)
        {
          self._handleOverrideReasonEntered(eventData.override);
        });

    return listRow;
  },

  /**
   * New warning override event handler - will basically just cache the override so that it can be
   * reapplied if the warnings are reloaded (which happens when a new therapy is added to the basket).
   *
   * @param {app.views.medications.ordering.warnings.WarningOverride} override
   * @private
   */
  _handleOverrideReasonEntered: function(override)
  {
     this._overriddenWarnings.push(override);
  },

  /**
   * Searches for the given warning, by matching description, inside the overridden warning list.
   *
   * @param {app.views.medications.warnings.dto.MedicationsWarning} warning
   * @returns {app.views.medications.ordering.warnings.WarningOverride|null|undefined}
   * @private
   */
  _findOverrideForWarningByDescription: function(warning)
  {
    return app.views.medications.MedicationUtils.findInArray(
        this._overriddenWarnings, function isDescriptionMatch(override)
        {
          return warning.getDescription() === override.getWarning().getDescription();
        });
  },

  _handleLoadingMaskVisibility: function()
  {
    if (!this.isDataLoading())
    {
      this.hideLoadingMask();
    }
    else
    {
      this.showLoadingMask()
    }
  },

  /**
   * Keep warning overrides for those warnings which already have an entered override. The match is made
   * by description, since the warnings are reladed and thus new object instances. If a matching warning
   * is found, we also fix the link between the override and warning via the setter
   * {@link app.views.medications.ordering.warnings.WarningOverride#setWarning}.
   *
   * @param {Array<app.views.medications.warnings.dto.MedicationsWarning>} warnings
   * @private
   */
  _refreshOverriddenWarnings: function(warnings)
  {
    this._overriddenWarnings = this._overriddenWarnings.filter(function(overriddenWarning)
    {
      var matchingByDescription =
          app.views.medications.MedicationUtils.findInArray(
              warnings,
              function isMatchByDescription(warning){
                return warning.getDescription() === overriddenWarning.getWarning().getDescription();
              });
      if (matchingByDescription)
      {
        overriddenWarning.setWarning(matchingByDescription);
      }
      return !tm.jquery.Utils.isEmpty(matchingByDescription)
    });
  },

  /**
   * True, if the ability to override a warning is allowed.
   * @return {boolean}
   */
  isOverrideAllowed: function()
  {
    return this.overrideAllowed === true;
  },

  /**
   * @param {Array<app.views.medications.common.dto.Therapy>} basketTherapies
   * @param {Boolean} includeInpatientTherapies
   */
  refreshWarnings: function(basketTherapies, includeInpatientTherapies)
  {
    var self = this;
    this._loadingWarnings = true;
    // cache the value in case the severity filter changes and we need to refresh
    this._patientMedications = tm.jquery.Utils.isArray(basketTherapies) ? basketTherapies : [];
    this._handleLoadingMaskVisibility();
    this.getView()
        .getRestApi()
        .loadMedicationWarnings(
            this._patientMedications,
            {taskName: 'REFRESH_MEDICATIONS'},
            includeInpatientTherapies,
            true)
        .then(
            function onLoad(warningsDto)
            {
              if (!self._destroying)
              {
                self.setMedicationWarnings(warningsDto);
                self._refreshOverriddenWarnings(self.getMedicationWarnings().getAllWarnings());
                self.handleLowSeverityWarningsBtnVisibility();
                self.refreshWarningsList();
                self._loadingWarnings = false;
                self._handleLoadingMaskVisibility();
              }
            }
        );
  },

  /**
   * @override
   */
  clear: function()
  {
    this.callSuper();
    this._patientMedications = [];
    this._refreshOverriddenWarnings([]);
  },

  /**
   * @returns {Array<app.views.medications.ordering.warnings.WarningOverride>}
   */
  getOverriddenWarnings: function()
  {
    return this._overriddenWarnings;
  },

  /**
   * @return {boolean}
   */
  isEveryCriticalWarningOverridden: function()
  {
    this.getValidationForm().reset();
    for (var i = 0; i < this.getList().getDataSource().length; i++)
    {
      var singleWarningsRow = this.getList().getItemTemplateByRowIndex(i);
      var singleWarningsRowValidator = singleWarningsRow.getOverrideReasonValidation();
      if (!!singleWarningsRowValidator)
      {
        this.getValidationForm().addFormField(singleWarningsRowValidator);
      }
    }
    this.getValidationForm().submit();
    return !this.getValidationForm().hasFormErrors();
  },

  /**
   * @returns {boolean}
   */
  isDataLoading: function()
  {
    return this._loadingWarnings;
  },

  destroy: function()
  {
    this._destroying = true;
    this.callSuper();
  }
});

