Class.define('app.views.medications.timeline.administration.PlannedDoseTimeValidator', 'tm.jquery.Object', {

  administrations: null,
  ignorePastAdministrations: false,
  ignoreFutureAdministrations: false,

  _plannedTimes: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._mapPlannedTimesByAdministrations();
  },

  /**
   * @param {Array<Object> | null} administrations
   */
  setAdministrations: function(administrations)
  {
    this.administrations = administrations;
    this._mapPlannedTimesByAdministrations();
  },

  /**
   * @returns {Array<Date>}
   */
  getPlannedTimes: function()
  {
    return this._plannedTimes;
  },

  /**
   * If set to true, past administration tasks will be ignored when validating any change with respect to existing
   * administration tasks.
   * @param {boolean} value
   */
  setIgnorePastAdministrations: function(value)
  {
    this.ignorePastAdministrations = value;
  },

  /**
   * If set to true, future administration tasks will be ignored when validating any change with respect to existing
   * administration tasks.
   * @param {boolean} value
   */
  setIgnoreFutureAdministrations: function(value)
  {
    this.ignoreFutureAdministrations = value;
  },

  /**
   * Set to true if you want the validator to ignore past administrations.
   * @return {boolean}
   */
  isIgnorePastAdministrations: function()
  {
    return this.ignorePastAdministrations === true;
  },

  /**
   * Set to true if you want the validator to ignore future administrations.
   * @return {boolean}
   */
  isIgnoreFutureAdministrations: function()
  {
    return this.ignoreFutureAdministrations === true;
  },

  /**
   * @returns {Array}
   */
  getAdministrations: function()
  {
    return this.administrations;
  },

  /**
   * @param {Date | String} originalPlannedTime
   * @param {Date | String} desiredPlannedTime
   * @returns {boolean}
   */
  isChangeAcceptable: function(originalPlannedTime, desiredPlannedTime)
  {
    originalPlannedTime = tm.jquery.Utils.isDate(originalPlannedTime) ? originalPlannedTime : new Date(originalPlannedTime);
    desiredPlannedTime = tm.jquery.Utils.isDate(desiredPlannedTime) ? desiredPlannedTime : new Date(desiredPlannedTime);

    var pastAdministrations = [];
    var futureAdministrations = [];

    this.getPlannedTimes().forEach(function(plannedTime)
    {
      if (plannedTime.getTime() !== originalPlannedTime.getTime())
      {
        if (plannedTime < originalPlannedTime)
        {
          pastAdministrations.push(plannedTime);
        }
        else
        {
          futureAdministrations.push(plannedTime);
        }
      }
    }, this);

    var hasPast = !this.isIgnorePastAdministrations() && pastAdministrations.length > 0;
    var hasFuture = !this.isIgnoreFutureAdministrations() && futureAdministrations.length > 0;

    if (hasPast && hasFuture)
    {
      return desiredPlannedTime > pastAdministrations[pastAdministrations.length - 1] &&
          desiredPlannedTime < futureAdministrations[0];
    }
    else if (hasPast && !hasFuture)
    {
      return desiredPlannedTime > pastAdministrations[pastAdministrations.length - 1];
    }
    else if (!hasPast && hasFuture)
    {
      return desiredPlannedTime < futureAdministrations[0];
    }

    return true;
  },

  /**
   * Check if selected timestamp complies with prescribed dosing interval. If past administrations are to be ignored, the
   * check is irrelevant.
   *
   * @param {Number} selectedTimestamp
   * @param {app.views.medications.TherapyEnums.dosingFrequencyTypeEnum} dosingFrequency
   * @returns {boolean}
   */
  isPrnTimeTooSoon: function(selectedTimestamp, dosingFrequency)
  {
    if (this.isIgnorePastAdministrations())
    {
      return false;
    }

    var lastAdministrationTime = null;
    for (var j = 0; j < this.getAdministrations().length; j++)
    {
      var administration = this.getAdministrations()[j];

      var administrationStart = administration.getAdministrationTimestamp();
      if (!lastAdministrationTime ||
          administration.isScheduledBetween(lastAdministrationTime.getTime(), selectedTimestamp, true, true))
      {
        lastAdministrationTime = administrationStart;
      }
    }

    if (lastAdministrationTime)
    {
      var nextAllowedAdministrationTime = app.views.medications.MedicationTimingUtils.getNextAllowedAdministrationTimeForPRN(
          dosingFrequency,
          lastAdministrationTime);

      return selectedTimestamp > lastAdministrationTime && selectedTimestamp < nextAllowedAdministrationTime;
    }

    return false;
  },

  /**
   * Check if two rate change administrations are within 5 minutes of each other.
   * @param {number} timestamp
   * @param {Object} administration
   * @returns {boolean}
   */
  isRateAdministrationChangesTooClose: function(timestamp, administration)
  {
    var enums = app.views.medications.TherapyEnums;
    var futureAndPastAdministrations =
        this._getFutureAndPastAdministrations(timestamp, administration);
    var lastPastInfusionChangeAdministration = null;
    var firstFutureInfusionChangeAdministration = null;
    if (!this.isIgnorePastAdministrations() && futureAndPastAdministrations.pastAdministrations)
    {
      for (var i = futureAndPastAdministrations.pastAdministrations.length - 1; i >= 0; i--)
      {
        if (futureAndPastAdministrations.pastAdministrations[i].getAdministrationType() ===
            enums.administrationTypeEnum.ADJUST_INFUSION)
        {
          lastPastInfusionChangeAdministration = futureAndPastAdministrations.pastAdministrations[i];
          break;
        }
      }
    }
    if (!this.isIgnoreFutureAdministrations() && futureAndPastAdministrations.futureAdministrations)
    {
      for (var j = 0; j < futureAndPastAdministrations.futureAdministrations.length; j++)
      {
        if (futureAndPastAdministrations.futureAdministrations[j].getAdministrationType() ===
            enums.administrationTypeEnum.ADJUST_INFUSION)
        {
          firstFutureInfusionChangeAdministration = futureAndPastAdministrations.futureAdministrations[j];
          break;
        }
      }
    }

    return ((lastPastInfusionChangeAdministration &&
        getAbsoluteDifferenceInMinutes(lastPastInfusionChangeAdministration) < 5) ||
        (firstFutureInfusionChangeAdministration &&
            getAbsoluteDifferenceInMinutes(firstFutureInfusionChangeAdministration) < 5));

    function getAbsoluteDifferenceInMinutes(administrationToCompare)
    {
      return Math.abs(administrationToCompare.getAdministrationTimestamp().getTime() - timestamp.getTime()) / (1000 * 60)
    }
  },

  /**
   * Assert if time difference is more than factor of prescribed dosing interval
   * @param {Date | String} selectedTime
   * @param {Date | String} plannedTime
   * @param {number} allowedDifferenceFactor must be 0 < n < 1
   * @param {number} dosingFrequency
   * @returns {boolean}
   */
  isTimeDifferenceTooBig: function(selectedTime, plannedTime, allowedDifferenceFactor, dosingFrequency)
  {
    var allowedDiff = (dosingFrequency * 60) * allowedDifferenceFactor;
    return (Math.abs(selectedTime.getTime() - plannedTime.getTime())) / (1000 * 60) > allowedDiff
  },

  /**
   * @param {Date} selectedTimestamp
   * @param {Object} administration
   * @returns {boolean}
   */
  isRateAdministrationJump: function(selectedTimestamp, administration)
  {
    var originalTimestamp = administration.getAdministrationTimestamp();
    var futureAndPastAdministrations =
        this._getFutureAndPastAdministrations(originalTimestamp, administration);
    var pastAdministrations = futureAndPastAdministrations.pastAdministrations;
    var futureAdministrations = futureAndPastAdministrations.futureAdministrations;

    if (!this.isIgnorePastAdministrations() && pastAdministrations.length > 0)
    {
      var lastPastAdministration = pastAdministrations[pastAdministrations.length - 1];
      if (lastPastAdministration.isScheduledAfter(selectedTimestamp.getTime()))
      {
        return true;
      }
    }

    if (!this.isIgnoreFutureAdministrations() && futureAdministrations.length > 1 &&
        futureAdministrations[1].isScheduledBefore(selectedTimestamp.getTime()))
    {
      return true;
    }

    if (!this.isIgnoreFutureAdministrations() && futureAdministrations.length > 0)
    {
      var firstFutureAdministration = futureAdministrations[0];
      if (firstFutureAdministration.isScheduledBefore(selectedTimestamp.getTime()))
      {
        if (administration.getGroupUUId() && administration.getGroupUUId() !== firstFutureAdministration.getGroupUUId())
        {
          return true;
        }
      }
    }
    return false;

  },

  /**
   * @param {number} selectedTimestamp
   * @param {Object} selectedAdministration
   * @returns {{pastAdministrations: Array, futureAdministrations: Array}}
   * @private
   */
  _getFutureAndPastAdministrations: function(selectedTimestamp, selectedAdministration)
  {
    var pastAdministrations = [];
    var futureAdministrations = [];
    this.getAdministrations().forEach(function(administration)
    {
      if (administration !== selectedAdministration)
      {
        if (administration.isScheduledBefore(selectedTimestamp))
        {
          pastAdministrations.push(administration);
        }
        else
        {
          futureAdministrations.push(administration);
        }
      }
    });
    return {
      pastAdministrations: pastAdministrations,
      futureAdministrations: futureAdministrations
    };
  },

  /**
   * @private
   */
  _mapPlannedTimesByAdministrations: function()
  {
    var administrations = this.getAdministrations();
    this._plannedTimes = tm.jquery.Utils.isArray(administrations) ?
        administrations.map(function extractTimes(administration)
        {
          return administration.getAdministrationTimestamp();
        })
        : [];
  }
});