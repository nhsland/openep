/*
 * Copyright (c) 2010-2014 Marand d.o.o. (www.marand.com)
 *
 * This file is part of Think!Med Clinical Medication Management.
 *
 * Think!Med Clinical Medication Management is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Think!Med Clinical Medication Management is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Think!Med Clinical Medication Management.  If not, see <http://www.gnu.org/licenses/>.
 */
Class.define('app.views.medications.common.therapy.TherapyDetailsContentContainer', 'app.views.medications.common.therapy.BaseTherapyDetailsContentContainer', {
  scrollable: 'vertical',

  data: null,
  dialogZIndex: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildContentContainer();
  },

  _buildContentContainer: function()
  {
    var therapy = this.getTherapy();

    if (therapy.isOrderTypeComplex())
    {
      this._addComplexTherapyOrderRows()
    }
    else if (therapy.isOrderTypeOxygen())
    {
      this._addOxygenTherapyOrderRows();
    }
    else
    {
      this._addSimpleTherapyOrderRows();
    }
    this._addCommonRows();

    if (this.getDisplayProvider().isShowAuditTrail())
    {
      this._addHistoryContentRow();
    }
    this._addLegendContentRow();
  },

  _addCommonRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();

    //dosing interval
    this._addDosingIntervalRows();

    //route
    this._addRouteRow();

    this._addReleaseDetails();

    //comment
    if (!tm.jquery.Utils.isEmpty(therapy.getComment()))
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("commentary"),
              therapy.getComment(),
              "comment"));
    }

    if (therapy.getClinicalIndication())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("indication"),
              therapy.getClinicalIndication().name,
              "indication"));
    }

    if (therapy.getInformationSources().length > 0)
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary('source'),
              therapy
                  .getInformationSources()
                  .map(function toName(source)
                  {
                    return source.getName();
                  })
                  .join(', '),
              'information-source'));
    }

    if (!!therapy.getTargetInr())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("target.inr"),
              therapy.getTargetInr(),
              "target-inr"));
    }

    if (this.getData().originalTherapyStart)
    {
      var startTimeValue = view.getDisplayableValue(new Date(this.getData().originalTherapyStart), "short.date.time");
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("from"),
              startTimeValue,
              "start-time"));
    }

    if (therapy.getEnd())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("until.low.case"),
              view.getDisplayableValue(therapy.getEnd(), "short.date.time"),
              "end-time"));
    }

    if (this.getConsecutiveDay() && this.getConsecutiveDay() >= 0)
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("therapy.day"),
              this.getConsecutiveDay(),
              "consecutive-day"));
    }

    if (therapy.getPrescriberName())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("prescribed.by"),
              therapy.getPrescriberName(),
              "prescriber-name"));
    }

    if (therapy.getComposerName() !== therapy.getPrescriberName())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("composed.by"),
              therapy.getComposerName(),
              "composer-name"));
    }

    var timeDataString = view.getDisplayableValue(therapy.getCreatedTimestamp(), "short.date.time");
    this._contentContainer.add(
        this._buildLabelDataRowContainer(
            view.getDictionary("when"),
            timeDataString,
            "when"));

    var doctorsReviewTask = this._findDoctorsReviewTaskData();
    if (doctorsReviewTask)
    {
      var doctorsReviewString = view.getDisplayableValue(new Date(doctorsReviewTask.dueTime), "short.date");

      if (doctorsReviewTask.comment)
      {
        doctorsReviewString += ",<br>" + view.getDictionary("commentary") +
            ": " + tm.jquery.Utils.escapeHtml(doctorsReviewTask.comment);
      }
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("review"),
              doctorsReviewString,
              "review",
              true));
    }
    this._addStatusReasonRow();
  },

  _addSimpleTherapyOrderRows: function()
  {
    var self = this;
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = app.views.medications.MedicationUtils;

    if (tm.jquery.Utils.isEmpty(therapy.getTimedDoseElements()))
    {
      if (!tm.jquery.Utils.isEmpty(therapy.getQuantityDisplay()))
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(
                view.getDictionary("dose"),
                utils.getFormattedDecimalNumber(therapy.getQuantityDisplay()),
                "dose",
                true));
      }
    }
    else
    {
      if (app.views.medications.MedicationUtils.isTherapyWithVariableDaysDose(therapy.getTimedDoseElements()))
      {
        var protocolButtonHandler = function()
        {
          self._showVariableDoseProtocolContainer();
        };
        this._contentContainer.add(this._createLabelButtonRow(view.getDictionary("dose"),
            view.getDictionary("protocol"), protocolButtonHandler, "protocol", "show-variable-dose-protocol"));
      }
      else
      {
        this._addTimedDoseElementsRows();
      }
    }
    //dose form name
    if (therapy.getDoseForm())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("dose.form"),
              therapy.getDoseForm().name),
          "dose-form");
    }
  },

  _addComplexTherapyOrderRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = app.views.medications.MedicationUtils;

    //heparin
    if (therapy.getAdditionalInstructionDisplay())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(" ", therapy.getAdditionalInstructionDisplay(), "instructions"));
    }

    //infusion rate
    if (tm.jquery.Utils.isEmpty(therapy.getTimedDoseElements()))
    {
      if (therapy.getSpeedDisplay())
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(
                view.getDictionary("rate"),
                utils.getFormattedDecimalNumber(therapy.getSpeedDisplay()),
                "rate",
                true));
      }
      else if (therapy.isAdjustToFluidBalance())
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(
                view.getDictionary("rate"),
                view.getDictionary("adjust.to.fluid.balance.short"),
                "adjust-to-fluid"));
      }
      if (therapy.getSpeedFormulaDisplay())
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(
                view.getDictionary("dose"),
                utils.getFormattedDecimalNumber(therapy.getSpeedFormulaDisplay()),
                "speed",
                true));
      }
    }
    else
    {
      this._addTimedDoseElementsRows();
    }

    if (therapy.isContinuousInfusion())
    {
      this._contentContainer.add(
          this._buildCheckboxLabelDataRowContainer(
              view.getDictionary("continuous.infusion"),
              "continuous-infusion"));

      if (therapy.isRecurringContinuousInfusion())
      {
        this._contentContainer.add(
            this._buildCheckboxLabelDataRowContainer(
                view.getDictionary("repeat.every.24h"),
                "repeat-24h"));
      }
    }

    if (therapy.getDurationDisplay())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("duration"),
              therapy.getDurationDisplay(),
              "duration"));
    }
  },

  /**
   * @private
   */
  _addOxygenTherapyOrderRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = app.views.medications.MedicationUtils;

    if (therapy.getMinTargetSaturation() && therapy.getMaxTargetSaturation())
    {
      var minSaturation = this._getRoundSaturationValue(therapy.getMinTargetSaturation());
      var maxSaturation = this._getRoundSaturationValue(therapy.getMaxTargetSaturation());
      var targetSaturationValue = minSaturation + '%' + ' - ' + maxSaturation + '%';

      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("target.saturation"),
              targetSaturationValue,
              "target-saturation"));
    }
    if (therapy.getSpeedDisplay())
    {
      var formattedRate = utils.getFormattedDecimalNumber(therapy.getSpeedDisplay());

      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("rate"),
              utils.getFormattedUnit(formattedRate, view),
              "rate",
              true));
    }
    if (therapy.getStartingDevice())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("device"),
              therapy.getStartingDevice().getDisplayText(view),
              "device"));
    }

    if (therapy.getWhenNeeded())
    {
      this._contentContainer.add(
          this._buildCheckboxLabelDataRowContainer(
              view.getDictionary("when.needed"),
              "when-needed"));
    }
    if (therapy.isHumidification())
    {
      this._contentContainer.add(
          this._buildCheckboxLabelDataRowContainer(
              view.getDictionary("humidification"),
              "humidification"));
    }
    if (therapy.isHighFlowOxygen())
    {
      this._contentContainer.add(
          this._buildCheckboxLabelDataRowContainer(
              view.getDictionary("high.flow.oxygen.therapy"),
              "high-flow"));
    }
  },

  _addTimedDoseElementsRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = app.views.medications.MedicationUtils;
    var isComplex = therapy.isOrderTypeComplex();
    var timedDoseElements = therapy.getTimedDoseElements();

    var timedDoseContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var labelColumn = new tm.jquery.Container({
      cls: "TextLabel row-label",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: view.getDictionary("dose")
    });
    timedDoseContainer.add(labelColumn);

    var timedDoseElementsContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    timedDoseElements.map(function(timedDoseElement)
    {
      var timeDoseElementRow = new tm.jquery.Container({
        cls: "TextData",
        testAttribute: 'time-dose-element-row',
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
      });

      var list = [];
      isComplex ? list.push(tm.jquery.Utils.escapeHtml(timedDoseElement.intervalDisplay)) :
          list.push(tm.jquery.Utils.escapeHtml(timedDoseElement.timeDisplay));
      isComplex ? list.push(utils.getFormattedDecimalNumber(timedDoseElement.speedDisplay)) :
          list.push(utils.getFormattedDecimalNumber(timedDoseElement.quantityDisplay));
      if (isComplex && timedDoseElement.speedFormulaDisplay)
      {
        list.push(utils.getFormattedDecimalNumber(timedDoseElement.speedFormulaDisplay));
      }
      var timedDoseElementString = list.map(function(item)
      {
        return '<span class="timed-dose-element-column">' +  item + '</span>'
      }).join("");
      timeDoseElementRow.setHtml(timedDoseElementString);

      timedDoseElementsContainer.add(timeDoseElementRow);
    });
    timedDoseContainer.add(timedDoseElementsContainer);

    this._contentContainer.add(timedDoseContainer);
  },

  _showVariableDoseProtocolContainer: function()
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var therapy = this.getTherapy();

    var protocolSummaryContainer = new app.views.medications.common.ProtocolSummaryContainer({
      view: view,
      timedDoseElements: therapy.getTimedDoseElements(),
      unit: therapy.quantityUnit
    });

    var protocolSummaryDialog = appFactory.createDefaultDialog(
        view.getDictionary("protocol"),
        null,
        protocolSummaryContainer,
        null,
        950,
        850
    );
    protocolSummaryDialog.setZIndex(this.dialogZIndex + 10);
    protocolSummaryDialog.setHideOnEscape(true);
    protocolSummaryDialog.setHideOnDocumentClick(true);
    protocolSummaryDialog.addTestAttribute("variable-dose-protocol-summary-dialog");
    protocolSummaryDialog.show();
  },

  _addDosingIntervalRows: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var frequencyDisplay = therapy.getFrequencyDisplay();

    if (!tm.jquery.Utils.isEmpty(frequencyDisplay))
    {
      var dosingIntervalLabel = view.getDictionary("dosing.interval");
      var dosingIntervals = [];
      dosingIntervals.push(frequencyDisplay);
      if (therapy.getDaysOfWeekDisplay())
      {
        dosingIntervals.push(therapy.getDaysOfWeekDisplay());
      }
      if (therapy.getDaysFrequencyDisplay())
      {
        dosingIntervals.push(therapy.getDaysFrequencyDisplay().toLowerCase());
      }
      if (therapy.getWhenNeeded())
      {
        dosingIntervals.push(view.getDictionary("when.needed"));
      }
      if (therapy.getStartCriterionDisplay())
      {
        dosingIntervals.push(therapy.getStartCriterionDisplay());
      }
      if (therapy.getApplicationPreconditionDisplay())
      {
        dosingIntervals.push(therapy.getApplicationPreconditionDisplay());
      }
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              dosingIntervalLabel,
              dosingIntervals.join(" - "),
              "dosing-interval"));

      if (!tm.jquery.Utils.isEmpty(therapy.getMaxDailyFrequency()))
      {
        var maxDosingIntervalLabel = view.getDictionary("dosing.max.24h");
        var dosingInterval = therapy.getMaxDailyFrequency();
        this._contentContainer.add(this._buildLabelDataRowContainer(maxDosingIntervalLabel, dosingInterval, "max-daily"));
      }
    }
    this._addDosingTimesRow();
  },

  _addDosingTimesRow: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var enums = app.views.medications.TherapyEnums;
    if (therapy.getDosingFrequency() && therapy.getDosingFrequency().type !== enums.dosingFrequencyTypeEnum.ONCE_THEN_EX &&
        therapy.getDoseTimes() && therapy.getDoseTimes().length)
    {
      var doseTimes;
      if (therapy.getDosingFrequency().type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
      {
        var frequencyKey = app.views.medications.MedicationTimingUtils.getFrequencyKey(therapy.getDosingFrequency());
        doseTimes = app.views.medications.MedicationTimingUtils.getPatternForFrequencyBetweenHours(therapy.getDoseTimes()[0],
            frequencyKey);
      }
      else
      {
        doseTimes = therapy.getDoseTimes();
      }
      var labelValue = view.getDictionary("administration.time");
      var doseTime = this._getDosingTimes(doseTimes);
      this._contentContainer.add(this._buildLabelDataRowContainer(labelValue, doseTime, "dose-times"));
    }
  },

  _getDosingTimes: function(doseTimes)
  {
    return doseTimes.map(function(doseTime)
    {
      return app.views.medications.MedicationTimingUtils.hourMinuteToString(doseTime.hour, doseTime.minute);
    }).join(" ");
  },

  _addRouteRow: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var routes = therapy.getRoutes();
    var routeNames = routes.map(function(route)
    {
      return route.name;
    }).join(", ");

    if (!tm.jquery.Utils.isEmpty(routeNames))
    {
      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("route"), routeNames, "route"));
    }
  },

  /**
   * Adds release details to the {@link #_contentContainer}. Usually only found on therapies prescribed with
   * the simple order form.
   * @private
   */
  _addReleaseDetails: function()
  {
    var view = this.getView();
    var releaseDetails = this.getTherapy().getReleaseDetails();

    if (!!releaseDetails)
    {
      this._contentContainer.add(
          this._buildCheckboxLabelDataRowContainer(
              view.getDictionary(releaseDetails.isModifiedRelease() ? 'modified.release' : 'gastro.resistant'),
              releaseDetails.isModifiedRelease() ? 'modified-release' : 'gastro-resistant'));

      if (tm.jquery.Utils.isNumeric(releaseDetails.getHours()))
      {
        this._contentContainer.add(
            this._buildLabelDataRowContainer(
                view.getDictionary('release.interval'),
                releaseDetails.getHours() + view.getDictionary('hour.unit'),
                'release-interval'));
      }
    }
  },

  _buildTherapyWarningsContainer: function(therapyContainer)
  {
    var therapy = this.getTherapy();
    var warnings = therapy.getCriticalWarnings();
    if (warnings.length)
    {
      var warningDescription = '<div class="therapy-details-warning">';
      for (var i = 0; i < warnings.length; i++)
      {
        warningDescription += '<span class="icon_warning"/>' + " " + tm.jquery.Utils.escapeHtml(warnings[i]) + '</span>';
        warningDescription += '<br>';
      }
      warningDescription += '</div>';

      var therapyWarningsContainer = new tm.jquery.Container({
        cls: "therapy-warnings-container",
        html: warningDescription,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });
      therapyContainer.add(therapyWarningsContainer);
    }
  },

  _createLabelButtonRow: function(labelValue, buttonValue, buttonHandler, cls, testAttributeName)
  {
    var contentContainerRow = new tm.jquery.Container({
      cls: cls ? null : "link-row-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0)
    });

    var rowLabel = new tm.jquery.Container({
      cls: "TextLabel row-label",
      html: labelValue,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    contentContainerRow.add(rowLabel);

    var rowButton = new tm.jquery.Button({
      type: "link",
      cls: cls ? cls : null,
      text: buttonValue,
      handler: buttonHandler,
      testAttribute: testAttributeName
    });
    contentContainerRow.add(rowButton);

    return contentContainerRow;
  },

  _addLegendContentRow: function()
  {
    var therapy = this.getTherapy();

    var detailsLegendContainer = new app.views.medications.common.therapy.TherapyDetailsLegendContainer({
      therapy: therapy,
      view: this.getView(),
      data: this.getData(),
      medicationData: this.getMedicationData(),
      displayProvider: this.getDisplayProvider()
    });

    this._contentContainer.add(detailsLegendContainer);
  },

  _createAuditTrailDialog: function()
  {
    var therapy = this.getTherapy();
    var appFactory = this.getView().getAppFactory();
    var self = this;
    this.getView().getRestApi().loadAuditTrailData(therapy).then(function(auditTrailData)
    {
      var auditTrailContainer = new app.views.medications.common.auditTrail.AuditTrailContainer({
        view: self.getView(),
        auditTrailData: auditTrailData
      });
      var auditTrailDialog = appFactory.createDataEntryDialog(
          self.getView().getDictionary("audit.trail"),
          null,
          auditTrailContainer,
          function(resultData)
          {
            //nothing changes
          },
          850,
          $(window).height() - 100);
      auditTrailDialog.setContainmentElement(self.getView().getDom());
      var footer = auditTrailDialog.getFooter();

      footer.getConfirmButton().setText(self.getView().getDictionary("close"));
      footer.getRightButtons().remove(footer.getCancelButton());
      auditTrailDialog.setHideOnDocumentClick(true);
      auditTrailDialog.show();
    });
  },

  /**
   * @param {number} saturationValue
   * @returns {number}
   * @private
   */
  _getRoundSaturationValue: function(saturationValue)
  {
    return Math.round(saturationValue * 100);
  },

  _addHistoryContentRow: function()
  {
    var self = this;
    var view = this.getView();

    this._contentContainer.add(this._createLabelButtonRow(view.getDictionary("history"),
        view.getDictionary("audit.trail"), historyButtonHandler, "view-history-details"));

    function historyButtonHandler()
    {
      self._createAuditTrailDialog();
      tm.jquery.ComponentUtils.hideAllTooltips();
    }
  },

  /**
   * Returns the doctor's review task data from the {@link #data} object, if it exist.
   * @return {Object|undefined}
   * @private
   */
  _findDoctorsReviewTaskData: function()
  {
    return tm.jquery.Utils.isArray(this.data.tasks) ?
        this.data.tasks.find(
            function(task)
            {
              return task.taskType === app.views.medications.TherapyEnums.taskTypeEnum.DOCTOR_REVIEW
            }) :
        undefined;
  },

  /**
   * Some status transitions (such as suspend/stop) require an additional explanation from the user.
   * @private
   */
  _addStatusReasonRow: function()
  {
    if (!!this.getData().getStatusReason())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              this.getView().getDictionary('therapy.status.reason.' + this.getTherapyStatus()),
              this.getData().getStatusReason(),
              'status-reason'));
    }
  },

  /**
   * Getters & Setters
   */

  getData: function()
  {
    return this.data;
  },

  getTherapyStatus: function()
  {
    return this.getData().therapyStatus;
  },

  getConsecutiveDay: function()
  {
    return this.getData().consecutiveDay;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  setDialogZIndex: function(dialogZIndex)
  {
    this.dialogZIndex = dialogZIndex;
  }
});