Class.define('app.views.medications.timeline.TimelineAdministrationTaskContentFactory', 'app.views.medications.common.timeline.AbstractTimelineAdministrationTaskContentFactory', {
  statics: {
    /**
     * @returns {string}
     */
    createAdministrationTaskElementId: function(administration)
    {
      var administrationId = "";
      if (administration.getAdministrationId()) //syntax error if id contains ::
      {
        var indexOfVersion = administration.getAdministrationId().indexOf("::");
        administrationId = indexOfVersion > -1 ?
            administration.getAdministrationId().substring(0, indexOfVersion) :
            administration.getAdministrationId();
      }
      return 'task-menu-' + administration.getTaskId() + administrationId;
    }
  },

  view: null,
  therapyRow: null,

  _currentRate: null,

  /**
   * Constructs a new instance of the administration task content factory used by our subview timeline for nurses.
   * This factory caches the current rate of infusion or oxygen therapies when a call to {@link #createContentContainer} is
   * made, so it should be discarded once used for a series of administration tasks!
   * The alternative to this would be to recursively search for the previous rate, which would be a lot slower when the
   * number of tasks grows.
   * @param config
   * @constructor
   */
  Constructor: function (config)
  {
    this.callSuper(config);
  },

  /**
   *
   * @param {app.views.common.AppView} view
   * @return {app.views.medications.timeline.TimelineAdministrationTaskContentFactory}
   */
  setView: function(view)
  {
    this.view = view;
    return this;
  },

  /**
   * @param {app.views.medications.timeline.TherapyRow} row
   * @return {app.views.medications.timeline.TimelineAdministrationTaskContentFactory}
   */
  setTherapyRow: function(row)
  {
    this.therapyRow = row;
    return this;
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.timeline.TherapyRow}
   */
  getTherapyRow: function()
  {
    return this.therapyRow;
  },

  /**
   * @override
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Array<*>} administrations
   */
  createContentContainer: function(therapy, administration, administrations)
  {
    var enums = app.views.medications.TherapyEnums;
    var isRateDecreased = null;
    var isDeferredTask = this._isAdministrationTaskDeferred(therapy, administration, administrations);

    if ((therapy.isContinuousInfusion() || therapy.isOrderTypeOxygen()) &&
        administration.getAdministeredDose() &&
        (administration.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION ||
            administration.getAdministrationType() === enums.administrationTypeEnum.START))
    {
      var administrationRate = administration.getAdministeredDose() ?
          administration.getAdministeredDose().getNumerator() :
          null;
      if (administration.isAdministrationAdministered() &&
          administration.getAdministrationType() !== enums.administrationTypeEnum.START)
      {
        if (!tm.jquery.Utils.isEmpty(this._currentRate) && !tm.jquery.Utils.isEmpty(administrationRate) &&
            (this._currentRate !== administrationRate))
        {
          isRateDecreased = this._currentRate < administrationRate;
        }
        else if (tm.jquery.Utils.isEmpty(this._currentRate) &&
            !tm.jquery.Utils.isEmpty(this.getTherapyRow().infusionRateAtIntervalStart) &&
            this.getTherapyRow().infusionRateAtIntervalStart !== administrationRate)
        {
          isRateDecreased = this.getTherapyRow().infusionRateAtIntervalStart < administrationRate;
        }
      }
      this._currentRate = administrationRate;
    }

    var orderState = this._getOrderStateString(administration, therapy, isRateDecreased, isDeferredTask);
    var taskId = app.views.medications.timeline.TimelineAdministrationTaskContentFactory.createAdministrationTaskElementId(
        administration);

    var additionalWarnings = this.getTherapyRow().additionalWarnings;
    var administrationClass = this._getAdministrationClass(
        administration,
        therapy,
        additionalWarnings,
        isRateDecreased,
        isDeferredTask);

    var taskStateContainer = new tm.jquery.Container({
      cls: "task-state-container",
      html: '<div class="task ' + administrationClass + '" id="' + taskId + '">'
      + '<div class="orderState">' + orderState + '</div></div>',
      testAttribute: therapy.getTherapyId()
    });

    if (this.getView().isTestMode())
    {
      taskStateContainer.addAttribute('data-planned-time',
          administration.getPlannedTime() ? moment(administration.getPlannedTime()).format() : '');
      taskStateContainer.addAttribute('data-administration-time',
          administration.getAdministrationTime() ? moment(administration.getAdministrationTime()).format() : '');
      taskStateContainer.addAttribute('data-administration-type', administration.getAdministrationType());
      taskStateContainer.addAttribute('data-is-additional', administration.isAdditionalAdministration());
    }

    taskStateContainer.doRender();

    if (!tm.jquery.ClientUserAgent.isTablet())
    {
      var hoverTooltip = this._buildTherapyAdministrationTooltip(therapy, administration, isDeferredTask);
      taskStateContainer.setTooltip(hoverTooltip);
    }

    return taskStateContainer;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Boolean} isDeferredTask
   * @returns {app.views.medications.timeline.TherapyTimelineTooltip}
   * @private
   */
  _buildTherapyAdministrationTooltip: function(therapy, administration, isDeferredTask)
  {
    var enums = app.views.medications.TherapyEnums;
    var view = this.getView();
    var additionalTaskState = [];
    var administrationClassStates = [];
    var whenNeededState = [];
    var orderStates = [];
    var doctorsOrdersState = [];
    var continuousInfusionState = [];

    if (tm.jquery.Utils.isEmpty(administration.getTaskId()) && !therapy.isContinuousInfusion())
    {
      additionalTaskState.push(view.getDictionary("additional.administration"));
    }
    if (isDeferredTask)
    {
      administrationClassStates.push(view.getDictionary("administration.defer"));
    }
    else if (!tm.jquery.Utils.isEmpty(administration.getInfusionSetChangeEnum()))
    {
      administrationClassStates.push(view.getDictionary("InfusionSetChangeEnum." + administration.getInfusionSetChangeEnum()));
    }
    else if (administration.isAdministrationCancelled())
    {
      administrationClassStates.push(view.getDictionary('cancelled'));
    }
    else if (!tm.jquery.Utils.isEmpty(administration.getAdministrationStatus()))
    {
      administrationClassStates.push(
          view.getDictionary("AdministrationStatusEnum." + administration.getAdministrationStatus()));
    }

    if (therapy.getWhenNeeded())
    {
      whenNeededState.push(view.getDictionary("when.needed"));
    }
    if (therapy.isContinuousInfusion())
    {
      if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.BOLUS)
      {
        continuousInfusionState.push(view.getDictionary("bolus.administration"));
      }
      else
      {
        continuousInfusionState.push(view.getDictionary("continuous.infusion"));
      }
    }
    if (therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.getDoctorConfirmation() === null && !tm.jquery.Utils.isEmpty(administration.getTaskId()))
    {
      doctorsOrdersState.push(view.getDictionary("by.doctors.orders.not.reviewed"));
    }
    else if (therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.getDoctorConfirmation() === false && !tm.jquery.Utils.isEmpty(administration.getTaskId()))
    {
      doctorsOrdersState.push(view.getDictionary("by.doctors.orders.do.not.administer"));
      administrationClassStates = [];
    }
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.START)
    {
      if (administration.isDifferentFromOrder())
      {
        orderStates.push(therapy.isDoseTypeWithRate() ? view.getDictionary("changed.rate") :
            view.getDictionary("changed.dose"));
      }
    }
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.STOP)
    {
      orderStates.push(view.getDictionary("stop.therapy"));
    }
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION)
    {
      orderStates.push(therapy.isOrderTypeOxygen() ?
          view.getDictionary("adjust.oxygen.rate") :
          view.getDictionary("adjust.infusion.rate"));
      if (administration.isDifferentFromOrder())
      {
        orderStates.push(view.getDictionary("changed.rate"));
      }
    }
    var taskStateStrings = [].concat(additionalTaskState, doctorsOrdersState, continuousInfusionState, whenNeededState,
        orderStates, administrationClassStates);

    var tooltipString = this._capitalizeFirstLetter(taskStateStrings.join(", "));

    return new app.views.medications.timeline.TherapyTimelineTooltip({
      title: tooltipString
    });
  },

  /**
   * @param {Object} administration
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array} additionalWarnings
   * @param {Boolean} isRateDecreased
   * @param {Boolean} isDeferredTask
   * @returns {String}
   * @private
   */
  _getAdministrationClass: function(administration, therapy, additionalWarnings, isRateDecreased, isDeferredTask)
  {
    var enums = app.views.medications.TherapyEnums;
    var administrationClass = '';
    var administrationStatus = administration.getAdministrationStatus();

    if (tm.jquery.Utils.isEmpty(administration.getTaskId()))
    {
      if (administrationStatus === enums.administrationStatusEnum.FAILED)
      {
        administrationClass = administrationStatus;
      }
      else
      {
        administrationClass = "ADDITIONAL";
      }
      if (administration.getAdministrationType() === enums.administrationTypeEnum.BOLUS)
      {
        administrationClass += " BOLUS";
      }
      if (!tm.jquery.Utils.isEmpty(isRateDecreased))
      {
        administrationClass += " RATE_CHANGE";
      }
    }
    else
    {
      administrationClass = administrationStatus;
    }
    if (administration.getInfusionSetChangeEnum())
    {
      administrationClass += " " + administration.getInfusionSetChangeEnum();
      if (!tm.jquery.Utils.isEmpty(administration.getTaskId()) || !tm.jquery.Utils.isEmpty(administration.getInfusionBag()))
      {
        administrationClass += " INFUSION_BAG_CHANGE";
      }
    }
    if (isDeferredTask)
    {
      administrationClass = "DEFER";
    }
    if (therapy.isTitrationDoseType() && (!administration.getPlannedDose() && !administration.getAdministeredDose()) &&
        administration.getAdministrationType() !== enums.administrationTypeEnum.INFUSION_SET_CHANGE &&
        administration.getAdministrationType() !== enums.administrationTypeEnum.STOP)
    {
      administrationClass += " TITRATION_DOSE_NOT_SET";
    }
    if (this._hasAdditionalWarnings(additionalWarnings))
    {
      administrationClass += " ADDITIONAL_WARNING";
    }
    else if (therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.getDoctorConfirmation() === null &&
        administration.getAdministrationStatus() !== enums.administrationStatusEnum.COMPLETED &&
        administration.getTaskId())
    {
      administrationClass += " DOCTOR_NOT_REVIEWED";
    }
    else if (therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.getDoctorConfirmation() === false)
    {
      administrationClass += " DOCTOR_DO_NOT_ADMINISTER";
    }

    if (administration.isAdministrationCancelled())
    {
      administrationClass += ' CANCELLED';
    }
    return administrationClass;
  },

  /**
   * @param {Object} administration
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean|null} [isRateDecreased=null] When null, no change is presumed. Otherwise interpreted as a increase
   * or decrease.
   * @param {Boolean} [isDeferredTask=false]
   * @returns {String}
   * @private
   */
  _getOrderStateString: function(administration, therapy, isRateDecreased, isDeferredTask)
  {
    var enums = app.views.medications.TherapyEnums;

    if ((therapy.getStartCriterion() === enums.medicationStartCriterionEnum.BY_DOCTOR_ORDERS &&
        administration.getDoctorConfirmation() === false) ||
        administration.isAdministrationCancelled())
    {
      return '&#88;';
    }
    if (isDeferredTask)
    {
      return '&#68;';
    }
    if (administration.getAdministrationType() === enums.administrationTypeEnum.INFUSION_SET_CHANGE)
    {
      if (administration.getInfusionSetChangeEnum() === enums.infusionSetChangeEnum.INFUSION_SYSTEM_CHANGE)
      {
        return "";
      }
      if (administration.getInfusionSetChangeEnum() === enums.infusionSetChangeEnum.INFUSION_SYRINGE_CHANGE)
      {
        return "";
      }
      return "";
    }
    if (isRateDecreased === true)
    {
      return '&#10548;';
    }
    else if (isRateDecreased === false)
    {
      return '&#10549;';
    }
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.START)
    {
      return administration.isDifferentFromOrder() ? '&#916;' : '';         //delta
    }
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.STOP)
    {
      return 'X';
    }
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION)
    {
      if (administration.isDifferentFromOrder())
      {
        return 'E\'';
      }
      return 'E';
    }
    return '';
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Array<*>} administrations
   * @returns {boolean}
   * @private
   */
  _isAdministrationTaskDeferred: function(therapy, administration, administrations)
  {
    var enums = app.views.medications.TherapyEnums;
    if (administration.getAdministrationResult() === enums.administrationResultEnum.DEFER)
    {
      return this._existsAdministrationAfterDefer(administration, administrations);
    }
    return false;
  },

  /**
   * @param {Object} administration
   * @param {Array<*>} administrations
   * @returns {boolean}
   * @private
   */
  _existsAdministrationAfterDefer: function(administration, administrations)
  {
    var administrationTimestamp = administration.getAdministrationTimestamp();

    var now = CurrentTime.get();
    var administrationTimestamps = administrations.map(function extractTimestamp(currentAdministration)
    {
      return currentAdministration.getAdministrationTimestamp();
    });

    var exists = true;
    administrationTimestamps.forEach(function(timestamp)
    {
      if (administrationTimestamp.getTime() !== timestamp.getTime() // skip self
          && administrationTimestamp < timestamp && timestamp < now)
      {
        exists = false;
      }
    });
    return exists;
  },

  _hasAdditionalWarnings: function(additionalWarnings)
  {
    return tm.jquery.Utils.isArray(additionalWarnings) && additionalWarnings.length > 0;
  },

  _capitalizeFirstLetter: function(stringToCapitalize)
  {
    if(!tm.jquery.Utils.isEmpty(stringToCapitalize))
    {
      return stringToCapitalize.charAt(0).toUpperCase() + stringToCapitalize.slice(1).toLocaleLowerCase();
    }
  }
});
