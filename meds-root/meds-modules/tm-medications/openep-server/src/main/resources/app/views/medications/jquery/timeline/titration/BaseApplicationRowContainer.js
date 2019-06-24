Class.define('app.views.medications.timeline.titration.BaseApplicationRowContainer', 'tm.jquery.Container', {
  therapyForTitration: null,
  administration: null,
  allAdministrations: null,
  medicationData: null,
  latestTherapyId: null,
  administrationType: null,
  scheduleAdditional: false,
  applyUnplanned: false,
  stopFlow: false,
  therapyReviewedUntil: null,

  /**privates**/
  _administrationWarnings: null,
  _timePicker: null,
  _plannedDoseTimeValidator: null,
  _markAsGivenCheckBox: null,
  _administrationWarningsProvider: null,
  _warningContainer: null,
  _commentField: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._configureValidators();
    this.buildGui();
  },

  _configureValidators: function()
  {
    this._plannedDoseTimeValidator = new app.views.medications.timeline.administration.PlannedDoseTimeValidator({
      administrations: this.getAllAdministrations()
    });

    this._administrationWarningsProvider = new app.views.medications.timeline.administration.AdministrationWarningsProvider({
      view: this.getView(),
      plannedDoseTimeValidator: this._plannedDoseTimeValidator,
      administration: this.getAdministration(),
      administrations: this.getAllAdministrations(),
      administrationType: this.getAdministrationType(),
      therapy: this.getTherapyForTitration().getTherapy(),
      infusionActive: this.isActiveContinuousInfusion(),
      therapyReviewedUntil: this.getTherapyReviewedUntil()
    });
  },

  _setCurrentTimeAsPlannedDoseTime: function()
  {
    this._timePicker.setDate(CurrentTime.get())
  },

  buildGui: function()
  {
    var view = this.getView();
    var plannedDoseTime = this.getAdministration() ? new Date(this.getAdministration().plannedTime) : CurrentTime.get();

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    var verticalWrapperMarkerLine = new tm.jquery.Container({
      cls: "wrapper-marker-line",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "5px"),
      alignSelf: "stretch"
    });

    var verticalContentWrapper = new tm.jquery.Container({
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var applicationConditionsRow = new tm.jquery.Container({
      cls: "application-conditions-row",
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var applicationPlannedTimeColumn = new app.views.medications.common.VerticallyTitledComponent({
      cls: "planned-time-container",
      titleText: view.getDictionary("planned.time"),
      scrollable: 'visible',
      contentComponent: new tm.jquery.DateTimePicker({
        date: plannedDoseTime,
        showType: "focus",
        scrollable: 'visible',
        style: "overflow: visible;", /* remove once scrollable starts working */
        nowButton: {
          text: this.view.getDictionary("asap")
        },
        currentTimeProvider: function()
        {
          return CurrentTime.get();
        }
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this._timePicker = applicationPlannedTimeColumn.getContentComponent();

    this._timePicker.getDatePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        this._setCurrentTimeAsPlannedDoseTime.bind(this));
    this._timePicker.getTimePicker().getField().onKey(
        new tm.jquery.event.KeyStroke({key: "d", altKey: false, ctrlKey: true, shiftKey: false}),
        this._setCurrentTimeAsPlannedDoseTime.bind(this));
    this._timePicker.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, this.assertAdministrationChange.bind(this));

    var commentColumn = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("commentary"),
      scrollable: 'visible',
      contentComponent: new tm.jquery.TextArea({
        width: 400,
        maxLength: 60,
        cls: 'comment-field',
        rows: 1,
        placeholder: view.getDictionary('commentary') + "...",
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this._commentField = commentColumn.getContentComponent();

    var warningsRow = new app.views.medications.timeline.administration.WarningContainer({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    this._warningContainer = warningsRow;

    applicationConditionsRow.add(applicationPlannedTimeColumn);
    applicationConditionsRow.add(this.buildApplicationOptionsColumn());

    var applicationDosingRow = this.buildApplicationDosingRow();
    applicationDosingRow.add(commentColumn);

    verticalContentWrapper.add(applicationConditionsRow);
    verticalContentWrapper.add(applicationDosingRow);
    if (this.isContinuousInfusion() && view.isInfusionBagEnabled())
    {
      verticalContentWrapper.add(this.buildInfusionBagRow());
    }
    verticalContentWrapper.add(warningsRow);

    this.add(verticalWrapperMarkerLine);
    this.add(verticalContentWrapper);
  },

  /**
   * @returns {app.views.medications.common.VerticallyTitledComponent}
   */
  buildApplicationOptionsColumn: function()
  {
    return new app.views.medications.common.VerticallyTitledComponent({
      contentComponent: new tm.jquery.Container({
        layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });
  },

  /**
   * @returns {tm.jquery.Container}
   */
  buildApplicationDosingRow: function()
  {
    var applicationDosingRow = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0)
    });
    return applicationDosingRow;
  },

  /**
   * @returns {boolean}
   */
  isActiveContinuousInfusion: function()
  {
    return this.activeContinuousInfusion === true;
  },

  /**
   * @returns {Boolean}
   */
  isMarkedGiven: function()
  {
    return this._markAsGivenCheckBox && this._markAsGivenCheckBox.isChecked();
  },

  /**
   * @returns {Boolean}
   */
  isContinuousInfusion: function()
  {
    return this.getTherapyForTitration().getTherapy().isContinuousInfusion() === true;
  },

  /**
   * @returns {Boolean}
   */
  isAdjustInfusion: function()
  {
    return this.getAdministrationType() === app.views.medications.TherapyEnums.administrationTypeEnum.ADJUST_INFUSION;
  },

  /**
   * @returns {Boolean}
   */
  isScheduleAdditional: function()
  {
    return this.scheduleAdditional === true;
  },

  /**
   * @returns {Boolean}
   */
  isApplyUnplanned: function()
  {
    return this.applyUnplanned === true;
  },

  /**
   * @returns {Boolean}
   */
  isStopFlow: function()
  {
    return this.stopFlow === true;
  },

  /**
   * @returns {Boolean}
   */
  isBolusAdministration: function()
  {
    return this.getAdministrationType() === app.views.medications.TherapyEnums.administrationTypeEnum.BOLUS;
  },

  /**
   * @param {app.views.medications.common.dto.TherapyDose} plannedDose
   * @param {Date} plannedTime
   * @param {Boolean} markGiven
   * @param {String} applicationComment
   * @param {Date|null} setUntilDate
   * @returns {tm.jquery.Promise}
   */
  applyAdministration: function(plannedDose, plannedTime, markGiven, applicationComment, setUntilDate)
  {
    var view = this.getView();
    var therapy = this.getTherapyForTitration().getTherapy();

    var administration = this.getAdministration() ?
        this.getAdministration().clone() :
        new app.views.medications.timeline.administration.dto.Administration();

    if (!this.getAdministration() && (this.isApplyUnplanned() ||
            (this.isContinuousInfusion() && (this.isAdjustInfusion() || this.isBolusAdministration()))))
    {
      administration.setAdministrationTime(plannedTime);
      administration.setAdministeredDose(plannedDose);
      administration.setAdditionalAdministration(true);
      administration.setAdministrationType(this.getAdministrationType());
      administration.setComment(applicationComment);
      administration.setAdministrationResult(app.views.medications.TherapyEnums.administrationResultEnum.GIVEN);

      return view.getRestApi().confirmAdministrationTask(therapy, administration, false, false, true);
    }
    else if (this.isScheduleAdditional())
    {
      administration.setPlannedTime(plannedTime);
      administration.setPlannedDose(plannedDose);

      if (markGiven)
      {
        administration.setAdministrationTime(plannedTime);
        administration.setAdministeredDose(plannedDose);
        administration.setComment(applicationComment);
        administration.setAdministrationResult(app.views.medications.TherapyEnums.administrationResultEnum.GIVEN);
      }
      else
      {
        administration.setDoctorsComment(applicationComment);
      }
      return view.getRestApi().createAdministrationTask(
          therapy,
          administration,
          false,
          true);
    }
    else
    {
      administration.setPlannedDose(plannedDose);
      administration.setPlannedTime(plannedTime);

      if (markGiven)
      {
        administration.setAdministrationTime(plannedTime);
        administration.setAdministeredDose(plannedDose);
        administration.setComment(applicationComment);
        if (this.isContinuousInfusion() && this._bagField && this._bagField.getValue())
        {
          administration.setInfusionBag({quantity: this._bagField.getValue(), unit: "mL"});
        }
      }
      else
      {
        administration.setDoctorsComment(applicationComment);
      }
      return view.getRestApi().setAdministrationTitrationDose(
          this.getLatestTherapyId(),
          administration,
          markGiven,
          setUntilDate,
          true);
    }
  },

  /**
   * Assert the given administration change / state with {@link #_administrationWarningsProvider} and set the results to
   * {@link #setAdministrationWarnings}. If we're also marking this dose as given, the review status of the therapy is
   * checked along with the 'next in line' check to prevent skipping a scheduled administration task.
   * @private
   */
  assertAdministrationChange: function()
  {
    var administrationDate = this._timePicker.getDate();

    if (!tm.jquery.Utils.isDate(administrationDate))
    {
      return;
    }

    var markAsGiven = this._markAsGivenCheckBox.isChecked();
    var checkNextInLine = markAsGiven && administrationDate > CurrentTime.get();
    var normalizedAdministrationDate = new Date(
        administrationDate.getFullYear(),
        administrationDate.getMonth(),
        administrationDate.getDate(),
        administrationDate.getHours(),
        administrationDate.getMinutes(),
        0, 0);

    var warnings = this._administrationWarningsProvider.getRestrictiveAdministrationWarnings(
        normalizedAdministrationDate,
        true,
        checkNextInLine,
        false,
        markAsGiven
    );

    this._warningContainer.setRestrictiveWarnings(warnings);
    this.setAdministrationWarnings(warnings);
  },


  /**
   * Getters & Setters
   */

  /**
   * @param {app.views.medications.timeline.administration.AdministrationWarnings|null} warnings
   * @private
   */
  setAdministrationWarnings: function(warnings)
  {
    this._administrationWarnings = warnings;
  },

  /**
   * @returns {null|app.views.medications.TherapyEnums.administrationTypeEnum}
   */
  getAdministrationType: function()
  {
    return this.administrationType;
  },

  /**
   * @returns {app.views.medications.timeline.titration.dto.TherapyForTitration}
   */
  getTherapyForTitration: function()
  {
    return this.therapyForTitration;
  },

  /**
   * @returns {Object}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {String}
   */
  getDoctorsComment: function()
  {
    return tm.jquery.Utils.isEmpty(this._commentField.getValue()) ? null : this._commentField.getValue();
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @returns {Array<Object>}
   */
  getAllAdministrations: function()
  {
    return tm.jquery.Utils.isArray(this.allAdministrations) ? this.allAdministrations : [];
  },

  /**
   * @returns {String}
   */
  getLatestTherapyId: function()
  {
    return this.latestTherapyId;
  },

  /**
   * @returns {app.views.medications.timeline.administration.AdministrationWarnings|null}
   */
  getAdministrationWarnings: function()
  {
    return this._administrationWarnings;
  },

  /**
   * @returns {Date|null}
   */
  getAdministrationDateTime: function()
  {
    return this._timePicker.getDate();
  },

  /**
   * @return {Date|null}
   */
  getTherapyReviewedUntil: function()
  {
    return this.therapyReviewedUntil;
  },

  /**
   * Returns true if either a jump or future warning is present, otherwise false.
   * @return {boolean}
   */
  hasAdministrationTimeRelatedWarnings: function()
  {
    var administrationWarnings = this.getAdministrationWarnings();
    return administrationWarnings ?
        administrationWarnings.getAdministrationInFutureWarning() || administrationWarnings.getJumpWarning() :
        false;
  }
});
