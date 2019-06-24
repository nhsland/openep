Class.define('app.views.medications.common.timeline.TimelineContentBuilder', 'tm.jquery.Object', {
  view: null,
  administrationTaskContentFactory: null,
  administrations: null,
  intervalStart: null,
  intervalEnd: null,

  _groupId: 1,
  _therapy: null,
  _infusionRateAtIntervalStart: null,
  _currentInfusionRate: null,
  _rateUnit: null,
  _currentStartingDevice: null,
  _showAddAdditionalAdministrationButton: false,

  /**
   * Returns a new instance of the Vis.Js timeline content builder, which will return the correct array of JSON objects.
   * Call {@link #build} when you finish with the configuration. The public properties are required, while the private
   * are optional. Since the way we display administration tasks varies greatly by our use case scenario (preview inside
   * the order dialogs vs the subview of our module etc), the creation of the content is delegated to the
   * configured {@link administrationTaskContentFactory}.
   * @constructor
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {app.views.medications.common.timeline.AbstractTimelineAdministrationTaskContentFactory|*} factory
   * @returns {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setAdministrationTaskContentFactory: function(factory)
  {
    this.administrationTaskContentFactory = factory;
    return this;
  },

  /**
   * @param {Date} date
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setIntervalStart: function(date)
  {
    this.intervalStart = date;
    return this;
  },

  /**
   * @param {String|Number} id
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setGroupId: function(id)
  {
    this._groupId = id;
    return this;
  },

  /**
   * @param {Date} date
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setIntervalEnd: function(date)
  {
    this.intervalEnd = date;
    return this;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setTherapy: function(therapy)
  {
    this._therapy = therapy;
    return this;
  },

  /**
   * @param {Array<*>|null|undefined} administrations
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setAdministrations: function(administrations)
  {
    this.administrations = tm.jquery.Utils.isArray(administrations) ? administrations : [];
    return this;
  },

  /**
   * @param {app.views.common.AppView} view
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setView: function(view)
  {
    this.view = view;
    return this;
  },

  /**
   * @param {Number|null|undefined} rate
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setInfusionRateAtIntervalStart: function(rate)
  {
    this._infusionRateAtIntervalStart = rate;
    return this;
  },

  /**
   * @param {Number|undefined|null} rate
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setCurrentInfusionRate: function(rate)
  {
    this._currentInfusionRate = rate;
    return this;
  },

  /**
   * Used by infusions, not used by oxygen based therapy.
   * @param {String|null} unit
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setRateUnit: function(unit)
  {
    this._rateUnit = unit;
    return this;
  },

  /**
   * @param {app.views.medications.common.dto.OxygenStartingDevice|null|undefined} device
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setCurrentStartingDevice: function(device)
  {
    this._currentStartingDevice = device;
    return this;
  },

  /**
   * Set to true if you want an additional administration button (+) added to the current timeline. Only viable for
   * {@link #setTherapy} whose {@link app.views.medications.common.dto.Therapy#getWhenNeeded} returns true.
   * @param {boolean} value
   * @return {app.views.medications.common.timeline.TimelineContentBuilder}
   */
  setShowAdditionalAdministrationButton: function(value)
  {
    this._showAddAdditionalAdministrationButton = value === true;
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
   * @return {Date}
   */
  getIntervalStart: function()
  {
    return this.intervalStart;
  },

  /**
   * @return {Date}
   */
  getIntervalEnd: function()
  {
    return this.intervalEnd;
  },

  /**
   * @return {String|Number}
   */
  getGroupId: function()
  {
    return this._groupId;
  },

  /**
   * @return {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this._therapy;
  },

  /**
   * @return {Array<app.views.medications.timeline.administration.dto.Administration>}
   */
  getAdministrations: function()
  {
    return this.administrations;
  },

  /**
   * @return {app.views.medications.common.timeline.AbstractTimelineAdministrationTaskContentFactory|*} the factory
   * used to create the administration task contents.
   */
  getAdministrationTaskContentFactory: function()
  {
    return this.administrationTaskContentFactory;
  },

  /**
   * Returns an array of JSON objects as defined by the Vis.Js Timeline library, along with additional properties
   * we usually require to identify an item once inside the timeline component. When no {@link #setTherapy} is set,
   * only the workday background and an empty height holder element is returned.
   * @return {Array<Object>}
   */
  build: function()
  {
    if (!this.getView())
    {
      throw new Error('view not defined');
    }
    if (!this.getAdministrationTaskContentFactory())
    {
      throw new Error('administrationTaskContentFactory not defined');
    }
    if (!tm.jquery.Utils.isDate(this.intervalStart))
    {
      throw new Error('intervalStart not defined');
    }
    if (!tm.jquery.Utils.isDate(this.intervalEnd))
    {
      throw new Error('intervalEnd not defined');
    }

    var timelineElements = [];

    this._addWorkdayBackgroundElement(timelineElements);
    this._addHeightGuardElement(timelineElements);

    if (!!this.getTherapy())
    {
      this._addAdministrationTaskElements(timelineElements);
      if (this._showAddAdditionalAdministrationButton)
      {
        this._addAdditionalAdministrationButton(timelineElements);
      }
      this._addInfusionDurationElements(timelineElements);
      this._addWhenNeededElements(timelineElements);
      this._addModifiedReleaseDurationElements(timelineElements);
    }

    return timelineElements;
  },

  /**
   * @param {Array<*>} timelineElements
   * @private
   */
  _addWorkdayBackgroundElement: function(timelineElements)
  {
    var workdayInterval = app.views.medications.MedicationTimingUtils.getWorkdayInterval(
        this.getView().getRoundsInterval(),
        CurrentTime.get()
    );

    if (workdayInterval)
    {
      timelineElements.push(this._createElementData(
          workdayInterval.start,
          workdayInterval.end,
          null,
          null,
          null,
          "work-hours",
          "background"));
    }
  },

  /**
   * @param {Array<*>} timelineElements
   * @private
   */
  _addHeightGuardElement: function(timelineElements)
  {
    timelineElements.push(
        this._createElementData(
            this.getIntervalStart(),
            this.getIntervalEnd(),
            '<div class="task"><div></div><div class="timeLabel TextData">empty</div></div>',
            null,
            null,
            "empty-task"));
  },

  /**
   * Adds administration task elements to timelineElements.
   * If administrationIsInCluster === true, administration is in a cluster of administrations less than 30 min. apart.
   * If administrationIsAtSameTime === true, administration is at same time as previous administration.
   * administrationClusterStart marks the time of first administration in a cluster, also marking the time for
   * timeline element with zoom functionality.
   * multipleAdministrationsTime marks a timestamp of multiple administrations. Also marks the time of element
   * marking multiple administrations overlap.
   * @param {Array<*>} timelineElements
   * @private
   */
  _addAdministrationTaskElements: function(timelineElements)
  {
    var administrationIsInCluster = false;
    var administrationIsAtSameTime = false;

    var administrationClusterStart = null;
    var multipleAdministrationsTime = null;

    for (var j = 0; j < this.getAdministrations().length; j++)
    {
      var administration = this.getAdministrations()[j];
      var administrationTimestamp = administration.getAdministrationTimestamp();

      if (administrationTimestamp.getTime() < this.getIntervalStart().getTime())
      {
        continue;
      }
      var taskStateContainer =
          this.getAdministrationTaskContentFactory()
              .createContentContainer(
                  this.getTherapy(),
                  administration,
                  this.getAdministrations());

      var administrationTaskElement = this._createAdministrationTaskElementData(
          administrationTimestamp,
          taskStateContainer,
          this.getTherapy(),
          administration
      );
      timelineElements.push(administrationTaskElement);

      var nextAdministration = j < this.getAdministrations().length - 1 ? this.getAdministrations()[j + 1] : null;
      var nextAdministrationTimestamp = nextAdministration ? nextAdministration.getAdministrationTimestamp() : null;

      var nextAdministrationAtSameTime = administration.isAtSameTimeAs(nextAdministrationTimestamp);
      var nextAdministrationIsTooClose = !!nextAdministrationTimestamp &&
          !nextAdministrationAtSameTime &&
          (((nextAdministrationTimestamp - administrationTimestamp) / 60 / 1000) <= 30);

      if (administrationIsInCluster === false && nextAdministrationIsTooClose === true)
      {
        administrationClusterStart = new Date(administrationTaskElement.start);
      }

      if (administrationIsAtSameTime === false && nextAdministrationAtSameTime === true)
      {
        multipleAdministrationsTime = new Date(administrationTaskElement.start);
      }

      if (administrationIsInCluster === true && nextAdministrationIsTooClose === false)
      {
        timelineElements.push(
            this._createElementData(
                administrationClusterStart,
                null,
                this.getView().getDictionary('overlap.click.to.zoom'),
                null,
                null,
                "overlap-info-zoom",
                "box",
                true));
        administrationClusterStart = null;
      }

      if (administrationIsAtSameTime === true && nextAdministrationAtSameTime === false)
      {
        timelineElements.push(
            this._createElementData(
                multipleAdministrationsTime,
                null,
                this.getView().getDictionary('overlap.same.time'),
                null,
                null,
                "overlap-info",
                "box"
            ));
        multipleAdministrationsTime = null;
      }

      administrationIsInCluster = nextAdministrationIsTooClose;
      administrationIsAtSameTime = nextAdministrationAtSameTime;
    }
  },

  /**
   * Adds the record additional administration button on the timeline's current time line, if the therapy is marked
   * to be given as needed.
   * @param {Array<*>} timelineElements timelineElements
   * @private
   */
  _addAdditionalAdministrationButton: function(timelineElements)
  {
    if (this.getTherapy().getWhenNeeded() === true &&
        this.getView().getTherapyAuthority().isRecordPrnAdministrationAllowed())
    {
      timelineElements.push(
          this._createElementData(
              new Date(CurrentTime.get()),
              null,
              "+",
              this.getTherapy(),
              null,
              "task timeline-add-prn-task",
              "box"));
    }
  },

  /**
   * @param {Array<*>} timelineElements
   * @private
   */
  _addInfusionDurationElements: function(timelineElements)
  {
    if (!this.getTherapy().isTherapyWithDurationAdministrations())
    {
      return;
    }

    var utils = app.views.medications.MedicationUtils;
    var enums = app.views.medications.TherapyEnums;
    var therapyStart = this.getTherapy().getStart();
    var therapyEnd = this.getTherapy().getEnd() ? this.getTherapy().getEnd() : null;

    var administrationIntervals = [];
    var hasStart = false;
    var therapyWithoutStartOrEndAdministration = true;
    var previousAdministrationWithZeroRate = false;

    for (var j = 0; j < this.getAdministrations().length; j++)
    {
      var administration = this.getAdministrations()[j];  // [AdministrationDto.java]

      if (administration.getAdministrationType() !== enums.administrationTypeEnum.INFUSION_SET_CHANGE)
      {
        var administrationWithZeroRate = administration.isWithZeroRate();

        var adjustInfusionTaskIsStart = !administrationWithZeroRate &&
            (previousAdministrationWithZeroRate || (j === 0 && this._infusionRateAtIntervalStart === 0)) &&
            administration.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION;
        if ((administration.getAdministrationType() === enums.administrationTypeEnum.START || adjustInfusionTaskIsStart) &&
            administration.getAdministrationResult() !== enums.administrationResultEnum.NOT_GIVEN)
        {
          therapyWithoutStartOrEndAdministration = false;
          hasStart = true;
          var intervalStart = {};
          intervalStart.administrationStart = administration.getAdministrationTimestamp();
          intervalStart.administrationEnd =
              (!tm.jquery.Utils.isEmpty(therapyEnd) && therapyEnd < this.getIntervalEnd()) ?
                  therapyEnd :
                  this.getIntervalEnd(); //if end exists it will be overwritten later
          administrationIntervals.push(intervalStart);
        }

        if ((administration.getAdministrationType() === enums.administrationTypeEnum.STOP || administrationWithZeroRate) &&
            administration.getAdministrationResult() !== enums.administrationResultEnum.NOT_GIVEN)
        {
          therapyWithoutStartOrEndAdministration = false;
          var administrationEnd = administration.getAdministrationTimestamp();

          var intervalEnd = hasStart ? administrationIntervals[(administrationIntervals.length - 1)] : {};
          intervalEnd.administrationEnd = administrationEnd;

          if (hasStart === false && !previousAdministrationWithZeroRate && this._infusionRateAtIntervalStart !== 0)
          {
            intervalEnd.administrationStart =
                !tm.jquery.Utils.isEmpty(therapyStart) && therapyStart > this.getIntervalStart() ?
                    therapyStart :
                    this.getIntervalStart();
            administrationIntervals.push(intervalEnd);
          }
          hasStart = false;
        }
        previousAdministrationWithZeroRate = administrationWithZeroRate;

        if (administration.getAdministeredDose() &&
            (administration.getAdministrationType() === enums.administrationTypeEnum.START ||
                (administration.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION
                    && !administration.getStartingDevice())))
        {
          var administrationTime = administration.getAdministrationTime();
          if (administration.isAdministrationAdministered() &&
              administrationTime < new Date(CurrentTime.get().getTime() - 2 * 60 * 60 * 1000)) // if at least two hours old
          {
            var displayRate =
                app.views.medications.MedicationUtils.buildAdministeredDoseDisplayString(administration, true, this.getView());

            timelineElements.push(
                this._createElementData(
                    administrationTime,
                    null,
                    displayRate,
                    null,
                    null,
                    "infusion-rate-label",
                    "box"));
          }
        }
      }
    }

    if (therapyWithoutStartOrEndAdministration && !!this._infusionRateAtIntervalStart &&
        this._infusionRateAtIntervalStart !== 0)
    {

      var administrationIntervalInfinite = {
        administrationStart: therapyStart > this.getIntervalStart() && this.getAdministrations().length === 0 ?
            therapyStart : this.getIntervalStart(),
        administrationEnd: (therapyEnd && therapyEnd < this.getIntervalEnd() && this.getAdministrations().length === 0) ?
            therapyEnd : this.getIntervalEnd()
      };
      administrationIntervals.push(administrationIntervalInfinite);
    }
    for (var k = 0; k < administrationIntervals.length; k++)
    {
      timelineElements.push(
          this._createElementData(
              administrationIntervals[k].administrationStart,
              administrationIntervals[k].administrationEnd,
              '<div></div>',
              null,
              null,
              "duration-line",
              "range"));
    }
    if (!tm.jquery.Utils.isEmpty(this._currentInfusionRate))
    {
      var infusionRateUnit = this.getTherapy().isOrderTypeOxygen() ?
          this.getTherapy().getFlowRateUnit() :
          this._rateUnit;

      var infusionRateString = utils.getFormattedDecimalNumber(
          utils.doubleToString(this._currentInfusionRate, 'n2')) + " " +
          app.views.medications.MedicationUtils.getFormattedUnit(infusionRateUnit, this.getView());

      if (this._currentStartingDevice)
      {
        infusionRateString = [infusionRateString, this._currentStartingDevice.getDisplayText(this.getView())].join(', ');
      }

      timelineElements.push(
          this._createElementData(
              CurrentTime.get(),
              null,
              infusionRateString,
              null,
              null,
              "infusion-rate-label",
              "box"));
    }
  },

  /**
   * @param {Array<*>} timelineElements
   * @private
   */
  _addWhenNeededElements: function(timelineElements)
  {
    if (!this.getTherapy().getWhenNeeded())
    {
      return;
    }

    var lastAdministrationTime = null;
    for (var j = 0; j < this.getAdministrations().length; j++)
    {
      var administration = this.getAdministrations()[j];  // [AdministrationDto.java]

      var administrationStart = administration.getAdministrationTimestamp();
      if (!lastAdministrationTime || administrationStart.getTime() > lastAdministrationTime.getTime())
      {
        lastAdministrationTime = administrationStart;
      }
    }

    if (lastAdministrationTime)
    {
      var nextAllowedAdministrationTime =
          app.views.medications.MedicationTimingUtils.getNextAllowedAdministrationTimeForPRN(
              this.getTherapy().getDosingFrequency(),
              lastAdministrationTime);

      if (!tm.jquery.Utils.isEmpty(nextAllowedAdministrationTime))
      {
        timelineElements.push(
            this._createElementData(
                lastAdministrationTime,
                nextAllowedAdministrationTime,
                null,
                null,
                null,
                "when-needed-line",
                "range"));
      }
    }
  },

  /**
   * @param {Array<*>} timelineElements
   * @private
   */
  _addModifiedReleaseDurationElements: function(timelineElements)
  {
    var releaseDetails = this.getTherapy().getReleaseDetails();
    if (!releaseDetails || !tm.jquery.Utils.isNumeric(releaseDetails.getHours()))
    {
      return;
    }

    var enums = app.views.medications.TherapyEnums;

    this.getAdministrations()
        .filter(
            function byType(administration)
            {
              return administration.getAdministrationType() === enums.administrationTypeEnum.START;
            })
        .forEach(
            function addDuration(administration)
            {
              var durationStart = administration.getAdministrationTimestamp();
              var durationEnd = moment(durationStart).add(releaseDetails.getHours(), 'hours');

              timelineElements.push(
                  this._createElementData(
                      durationStart,
                      durationEnd > this.getIntervalEnd() ? this.getIntervalEnd() : durationEnd,
                      '<div></div>',
                      null,
                      null,
                      "release-duration-line",
                      "range"));
            },
            this);
  },

  /**
   * @param {Date} from
   * @param {Date} to
   * @param {String|Element} taskContent
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {String} className
   * @param {String} [type='point']
   * @param {Boolean} [changeRangeOnClick=false]
   * @returns {{start: *, end: *, content: string, group: *, therapy: *, administration: *, className: *, type: null, changeRangeOnClick: boolean}}
   * @private
   */
  _createElementData: function(from, to, taskContent, therapy, administration, className, type, changeRangeOnClick)
  {
    return {
      start: from,
      end: to,
      content: !taskContent ? '' : taskContent,
      group: this.getGroupId(),
      therapy: therapy,
      administration: administration,
      className: className,
      type: tm.jquery.Utils.isEmpty(type) ? 'point' : type,
      changeRangeOnClick: changeRangeOnClick === true ? changeRangeOnClick : false
    };
  },

  /**
   * @param {Date} administrationTime
   * @param {tm.jquery.Container|tm.jquery.Component} taskContainer
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @return {{start: *, content: *|HTMLElement, group: *|String|Number, therapy: *, administration: *, taskContainer: *, type: string}}
   * @private
   */
  _createAdministrationTaskElementData: function(administrationTime, taskContainer, therapy, administration)
  {
    return {
      start: administrationTime,
      content: taskContainer.getDom(),
      group: this.getGroupId(),
      therapy: therapy,
      administration: administration,
      taskContainer: taskContainer,
      type: 'point'
    };
  }
});