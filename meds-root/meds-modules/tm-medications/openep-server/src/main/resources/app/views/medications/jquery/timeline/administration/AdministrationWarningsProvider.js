Class.define('app.views.medications.timeline.administration.AdministrationWarningsProvider', 'tm.jquery.Object', {
  view: null,
  plannedDoseTimeValidator: null,
  administration: null,
  administrations: null,
  administrationType: null,
  therapy: null,
  infusionActive: true,
  therapyReviewedUntil: null,

  _nextAdministrationDisplayValue: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._assertInfusionActive();
  },

  /**
   * Fixes infusionActive property for variable infusions with rate and start/stop tasks
   * @private
   */
  _assertInfusionActive: function()
  {
    var enums = app.views.medications.TherapyEnums;
    if (!this.getAdministration() ||
        (this.getAdministration().getAdministrationType() !== enums.administrationTypeEnum.START &&
        this.getAdministration().getAdministrationType() !== enums.administrationTypeEnum.STOP))
    {
      if (!this.getTherapy().isContinuousInfusion())
      {
        this.infusionActive = this._isStartTaskConfirmed();
      }
    }
    else
    {
      this.infusionActive = true;
    }
  },

  /**
   * Returns a list of administrations scheduled in the last 24 hours from provided time. Ignores failed administrations
   * and infusion stop administrations.
   * @param {Array<app.views.medications.timeline.administration.dto.Administration>|null} administrations
   * @param {Date} when
   * @returns {Array<app.views.medications.timeline.administration.dto.Administration>}
   * @private
   */
  _getGivenStartTaskFor24HoursFromTime: function(administrations, when)
  {
    var enums = app.views.medications.TherapyEnums;
    if (administrations == null || administrations.length === 0)
    {
      return [];
    }

    var whenMillis = when.getTime();
    var twentyFourHoursInMillis = 86400000;

    var tasksInInterval = [];
    for (var i = 0; i < administrations.length; i++)
    {
      var administration = administrations[i];
      if (administration.getAdministrationStatus() !== enums.administrationStatusEnum.FAILED &&
          administration.getAdministrationType() !== enums.administrationTypeEnum.STOP)
      {
        if (administration.isScheduledBetween((whenMillis - twentyFourHoursInMillis), whenMillis, false, true))
        {
          tasksInInterval.push(administration);
        }
      }
      if (administration.isScheduledAfter(whenMillis))
      {
        break;
      }
    }
    return tasksInInterval;
  },

  /**
   * @param {Array} administrations
   * @param {Date} when
   * @returns {Array}
   * @private
   */
  _getAdministrationsTimestampsFor24HourInterval: function(administrations, when)
  {
    var timestamps = [];
    var timePlus24Hours = when.getTime() + 86400000;
    for (var i = 0; i < administrations.length; i++)
    {
      var administration = administrations[i];
      var administrationTime = administration.getAdministrationTimestamp();
      if (administration.isScheduledBetween(when.getTime(), timePlus24Hours, true, true))
      {
        timestamps.push(administrationTime);
      }
      if (administration.isScheduledAfter(timePlus24Hours))
      {
        break;
      }
    }
    return timestamps;
  },

  /**
   * @param {Array} tasksInInterval
   * @param {Date} selectedTimestamp
   * @private
   */
  _findNextAllowedAdministrationTime: function(tasksInInterval, selectedTimestamp)
  {
    var view = this.getView();
    var timingUtils = app.views.medications.MedicationTimingUtils;
    //gets first task for interval where maxDailyFrequency isn't reached
    tasksInInterval.sort(app.views.medications.timeline.administration.dto.Administration.compareByTimestamp);

    while (tasksInInterval.length > this.getTherapy().getMaxDailyFrequency())
    {
      tasksInInterval.shift();
      tasksInInterval = this._getGivenStartTaskFor24HoursFromTime(tasksInInterval, selectedTimestamp);
    }
    var nextAdministration = new Date(tasksInInterval[0].getAdministrationTimestamp());
    nextAdministration.setDate(nextAdministration.getDate() + 1);

    var tasksInNextInterval = this._getGivenStartTaskFor24HoursFromTime(this.getAdministrations(), nextAdministration);
    //assertNumberOfAllowedAdministrations for the calculated nextAdministration time - repeat until you find appropriate value
    if (tasksInNextInterval.length >= this.getTherapy().getMaxDailyFrequency())
    {
      this._findNextAllowedAdministrationTime(tasksInNextInterval, nextAdministration);
    }
    else
    {
      this._nextAdministrationDisplayValue = view.getDisplayableValue(nextAdministration, "short.date.time");
    }
  },

  /**
   * @returns {boolean}
   * @private
   */
  _isStartTaskConfirmed: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var firstPreviousStartAdministration = null;
    if (this.getAdministration() && this.getAdministration().getAdministrationType() === enums.administrationTypeEnum.START)
    {
      return true;
    }
    else
    {
      for (var i = this.getAdministrations().indexOf(this.getAdministration()) - 1; i >= 0; i--)
      {
        var administration = this.getAdministrations()[i];
        if (administration.getAdministrationType() === enums.administrationTypeEnum.START)
        {
          firstPreviousStartAdministration = administration;
          break;
        }
      }
      if (firstPreviousStartAdministration)
      {
        return firstPreviousStartAdministration.isAdministrationCompleted();
      }
      else
      {
        return true;
      }
    }
  },

  /**
   * Returns true if selected time is before any previous or after any next planned administration, with two exceptions:
   * 1.) A new infusion set change administration can be administered at any time, regardless of previous and following
   *     administrations.
   * 2.) Administrations for therapies with rate have a specific validator, since we need to allow start infusion tasks
   *     to jump their complementary stop tasks.
   * @param {Date} selectedTimestamp
   * @returns {boolean}
   */
  isAdministrationJump: function(selectedTimestamp)
  {
    var enums = app.views.medications.TherapyEnums;

    if ((this.getTherapy().isContinuousInfusion() || this.getTherapy().isOrderTypeOxygen()) &&
        !this.getAdministration() &&
        this.getAdministrationType() !== enums.administrationTypeEnum.INFUSION_SET_CHANGE)
    {
      return !this.getPlannedDoseTimeValidator().isChangeAcceptable(
          CurrentTime.get(), selectedTimestamp);
    }

    if (this.getAdministration())
    {
      // Jump check for infusion with rate, since we must allow for start task to jump its complementary end task
      if (this.getTherapy().isInfusionTypeRate())
      {
        return this.getPlannedDoseTimeValidator().isRateAdministrationJump(selectedTimestamp, this.getAdministration());
      }


      var administrationTime = this.getAdministration().getAdministrationTimestamp();

      if (administrationTime)
      {
        return !this.getPlannedDoseTimeValidator().isChangeAcceptable(
            administrationTime, selectedTimestamp);
      }
    }

    return false;
  },

  /**
   * @param {Date} selectedTimestamp
   * @returns {boolean}
   */
  isAdministrationTooFarInFuture: function(selectedTimestamp)
  {
    var limitTimestamp = CurrentTime.get();
    limitTimestamp.setMinutes(limitTimestamp.getMinutes() + 5);
    return limitTimestamp < selectedTimestamp;
  },

  /**
   * @param {Date} selectedTimestamp
   * @returns {boolean}
   */
  isNumberOfAllowedAdministrationsReached: function(selectedTimestamp)
  {
    if (!tm.jquery.Utils.isEmpty(this.getTherapy().getMaxDailyFrequency()) &&
        !tm.jquery.Utils.isEmpty(this.getAdministrations()))
    {
      if (selectedTimestamp)
      {
        var administrationTimestamps =
            this._getAdministrationsTimestampsFor24HourInterval(this.getAdministrations(), selectedTimestamp);
        administrationTimestamps.unshift(selectedTimestamp);

        for (var i = 0; i < administrationTimestamps.length; i++)
        {
          var administrationTimestamp = administrationTimestamps[i];
          var tasksInInterval = this._getGivenStartTaskFor24HoursFromTime(this.getAdministrations(), administrationTimestamp);
          if (tasksInInterval.length >= this.getTherapy().getMaxDailyFrequency())
          {
            this._findNextAllowedAdministrationTime(tasksInInterval, administrationTimestamp);
            return true;
          }
        }
      }
    }
    return false;
  },

  /**
   * @param {Date} selectedTimestamp
   * @returns {boolean}
   */
  isTherapyNotReviewed: function(selectedTimestamp)
  {
    return !(this.getTherapyReviewedUntil() >= selectedTimestamp);
  },

  /**
   * @param {Date} selectedTimestamp
   * @param {boolean} withJumpWarning
   * @param {boolean} withFutureWarning
   * @param {boolean} withMaxAdministrationsReachedWarning
   * @param {boolean} withTherapyNotReviewedWarning
   * @returns {app.views.medications.timeline.administration.AdministrationWarnings}
   */
  getRestrictiveAdministrationWarnings: function(selectedTimestamp,
                                                 withJumpWarning,
                                                 withFutureWarning,
                                                 withMaxAdministrationsReachedWarning,
                                                 withTherapyNotReviewedWarning)
  {
    var enums = app.views.medications.TherapyEnums;

    var warnings = new app.views.medications.timeline.administration.AdministrationWarnings();

    if (withJumpWarning)
    {
      if (this.isAdministrationJump(selectedTimestamp))
      {
        warnings.setJumpWarning(this.getView().getDictionary('therapy.administration.jump.check'));
      }
    }

    if (withFutureWarning)
    {
      if (this.isAdministrationTooFarInFuture(selectedTimestamp))
      {
        warnings.setAdministrationInFutureWarning(
            this.getView().getDictionary('therapy.administration.is.in.future.warning'));
      }
    }

    if (withMaxAdministrationsReachedWarning)
    {
      if (this.isNumberOfAllowedAdministrationsReached(selectedTimestamp))
      {
        warnings.setMaxAdministrationsWarning(this.getView().getDictionary('dosing.max.24h.warning') + " " +
            "<span><b>" + this._nextAdministrationDisplayValue + "</b></span>");
      }
    }

    if (this.getTherapy().isOrderTypeComplex())
    {
      if (this.getInfusionActive() === false)
      {
        warnings.setInfusionInactiveWarning((this.getTherapy().isContinuousInfusion() ?
            this.getView().getDictionary("infusion.not.active.warning") :
            this.getView().getDictionary("first.administration.not.confirmed.warning")));
      }
    }
    if (withTherapyNotReviewedWarning && this.getView().isDoctorReviewEnabled())
    {
      if (this.isTherapyNotReviewed(selectedTimestamp) &&
          (!this.getAdministration() ||
          this.getAdministration().getAdministrationType() !== enums.administrationTypeEnum.STOP))
      {
        warnings.setTherapyNotReviewedWarning(this.getView().getDictionary('therapy.administration.not.reviewed.warning'));
      }
    }
    return warnings;
  },

  /**
   * @param {app.views.medications.warnings.dto.ParacetamolRuleResult} paracetamolRuleResult
   * @returns {null | string}
   */
  getMedicationIngredientRuleHtml: function(paracetamolRuleResult)
  {
    var view = this.getView();
    if (!tm.jquery.Utils.isEmpty(paracetamolRuleResult))
    {
      var paracetamolWarning = [];
      if (!paracetamolRuleResult.isQuantityOk() && tm.jquery.Utils.isEmpty(paracetamolRuleResult.getErrorMessage()))
      {
        paracetamolWarning.push(view.getDictionary("paracetamol.daily.dose.24") + " " +
            tm.jquery.Utils.escapeHtml(paracetamolRuleResult.getHighestRulePercentage()) + "%.");
      }

      if (!paracetamolRuleResult.isBetweenDosesTimeOk() &&
          !tm.jquery.Utils.isEmpty(paracetamolRuleResult.getLastTaskTimestamp()))
      {
        var dictionaryEntry = paracetamolRuleResult.isLastTaskAdministered() ?
            view.getDictionary("medication.with.ingredient.already.given") :
            view.getDictionary("medication.with.ingredient.already.scheduled");

        paracetamolWarning.push(tm.jquery.Utils.formatMessage(
            dictionaryEntry,
            ["Paracetamol",
              "4",
              view.getDisplayableValue(paracetamolRuleResult.getLastTaskTimestamp(), "short.time")]
        ));
      }

      return paracetamolWarning.join("<br> <br>");
    }
    return null;
  },

  /**
   * @returns {object}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns Array{object}
   */
  getAdministrations: function()
  {
    return this.administrations;
  },

  /**
   * @returns {object | null}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  /**
   * @returns {string | null} app.views.medications.TherapyEnums.administrationTypeEnum
   */
  getAdministrationType: function()
  {
    return this.administrationType;
  },

  /**
   * @returns {object}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.timeline.administration.PlannedDoseTimeValidator|null}
   */
  getPlannedDoseTimeValidator: function()
  {
    return this.plannedDoseTimeValidator;
  },

  /**
   * @returns {boolean}
   */
  getInfusionActive: function()
  {
    return this.infusionActive;
  },

  /**
   * @returns {Date}
   */
  getTherapyReviewedUntil: function()
  {
    return this.therapyReviewedUntil;
  }

});