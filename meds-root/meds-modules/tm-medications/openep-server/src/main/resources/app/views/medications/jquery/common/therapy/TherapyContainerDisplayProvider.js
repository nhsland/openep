Class.define('app.views.medications.common.therapy.TherapyContainerDisplayProvider', 'tm.jquery.Object', {
  statics: {
    /** @type string */
    CONSECUTIVE_DAY_ICON_TAG: 'consecutive-day-icon-tag',
    /** @type string */
    THERAPY_STATUS_ICON_TAG: 'therapy-status-icon-tag',
    /** @type string */
    CRITICAL_WARNINGS_ICON_TAG: 'critical-warnings-icon-tag'
  },
  /** members: configs */
  view: null,

  showChangeHistory: true,
  showChangeReason: true,
  showMaxDose: true,
  showTherapyExpiring: true,
  showAuditTrail: true,
  showValidationIssues: true,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {Object} dto
   * @returns {app.views.medications.TherapyEnums.therapyStatusEnum|null}
   */
  getTherapyStatus: function(dto)
  {
    var therapyStatus = tm.jquery.Utils.isFunction(dto.getTherapyStatus) ? dto.getTherapyStatus() : dto.therapyStatus;
    return tm.jquery.Utils.isEmpty(dto.changeType) ? therapyStatus : dto.changeType;
  },

  /**
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} dto
   * @returns {String|null}
   */
  getStatusIcon: function(dto)
  {
    var status = this.getTherapyStatus(dto);
    var enums = app.views.medications.TherapyEnums;
    if (status === enums.therapyStatusEnum.ABORTED)
    {
      return "icon_aborted";
    }
    if (status === enums.therapyStatusEnum.CANCELLED)
    {
      return "icon_cancelled";
    }
    if (status === enums.therapyStatusEnum.LATE)
    {
      return "icon_late";
    }
    if (status === enums.therapyStatusEnum.VERY_LATE)
    {
      return "icon_very_late";
    }
    if (status === enums.therapyStatusEnum.SUSPENDED)
    {
      if (dto.therapyChangeReasonEnum === enums.therapyChangeReasonEnum.TEMPORARY_LEAVE)
      {
        return "icon_suspended_temporary_leave";
      }
      return "icon_suspended";
    }
    if (status === enums.pharmacistTherapyChangeType.ABORT)
    {
      return "icon_aborted";
    }
    if (status === enums.pharmacistTherapyChangeType.SUSPEND)
    {
      return "icon_suspended";
    }
    if (dto.additionalWarnings && dto.additionalWarnings.length)
    {
      return "high_alert_medication_icon";
    }
    return null;
  },

  getStatusDescription: function(dto)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var status = this.getTherapyStatus(dto);

    if (status === enums.therapyStatusEnum.ABORTED)
    {
      return view.getDictionary('stopped.therapy');
    }
    if (status === enums.therapyStatusEnum.CANCELLED)
    {
      return view.getDictionary('cancelled');
    }
    if (status === enums.therapyStatusEnum.LATE)
    {
      return view.getDictionary('delay.of.confirmation');
    }
    if (status === enums.therapyStatusEnum.VERY_LATE)
    {
      return view.getDictionary('delay.of.confirmation.long');
    }
    if (status === enums.therapyStatusEnum.SUSPENDED)
    {
      if (dto.therapyChangeReasonEnum === enums.therapyChangeReasonEnum.TEMPORARY_LEAVE)
      {
        return view.getDictionary('patient.on.temporary.leave');
      }
      return view.getDictionary('suspended.therapy');
    }
    if (status === enums.pharmacistTherapyChangeType.ABORT)
    {
      return view.getDictionary('stopped.therapy');
    }
    if (status === enums.pharmacistTherapyChangeType.SUSPEND)
    {
      return view.getDictionary('suspended.therapy');
    }
    if (dto.additionalWarnings && dto.additionalWarnings.length)
    {
      return view.getDictionary('additional.warnings');
    }
    return null;
  },

  getStatusClass: function(dto)
  {
    var enums = app.views.medications.TherapyEnums;
    var status = this.getTherapyStatus(dto);
    var therapy = dto.therapy;
    var therapyEndTime = therapy.end ? new Date(therapy.end) : null;
    var therapyEnded = therapyEndTime != null && therapyEndTime.getTime() <= CurrentTime.get().getTime();

    var containerStyles = [];

    if (dto.hasOwnProperty("active") && dto.active !== true) // DayTherapy
    {
      containerStyles.push("inactive");
    }
    if (dto.additionalWarnings && dto.additionalWarnings.length)
    {
      containerStyles.push("additional-warning")
    }
    if (status === enums.therapyStatusEnum.ABORTED
        || status === enums.therapyStatusEnum.CANCELLED)
    {
      containerStyles.push("aborted");
    }
    else if (status === enums.therapyStatusEnum.SUSPENDED)
    {
      containerStyles.push("suspended")
    }
    else if (dto.modifiedFromLastReview === true && !therapyEnded)
    {
      containerStyles.push("changed")
    }

    containerStyles.push(therapyEnded ? "ended" : "normal");

    return containerStyles.join(" ");
  },

  getPharmacistReviewIcon: function(status)
  {
    var enums = app.views.medications.TherapyEnums;
    if (status === enums.therapyPharmacistReviewStatusEnum.REVIEWED)
    {
      return null;
    }
    if (status === enums.therapyPharmacistReviewStatusEnum.NOT_REVIEWED)
    {
      return "icon_pharmacist_not_reviewed";
    }
    if (status === enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK)
    {
      return "icon_pharmacist_refer_back";
    }

  },

  getPharmacistReviewDescription: function(status)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    if (status === enums.therapyPharmacistReviewStatusEnum.REVIEWED)
    {
      return null;
    }
    if (status === enums.therapyPharmacistReviewStatusEnum.NOT_REVIEWED)
    {
      return view.getDictionary('pharmacist.review.waiting');
    }
    if (status === enums.therapyPharmacistReviewStatusEnum.REVIEWED_REFERRED_BACK)
    {
      return view.getDictionary('pharmacist.review.referred');
    }
  },

  getSelfAdminStatusIcon: function(selfAdminActionEnum, therapyStatus)
  {
    var enums = app.views.medications.TherapyEnums;
    var isTherapyActive = !(tm.jquery.Utils.isEmpty(therapyStatus) || therapyStatus === enums.therapyStatusEnum.ABORTED
        || therapyStatus === enums.therapyStatusEnum.CANCELLED || therapyStatus === enums.therapyStatusEnum.SUSPENDED);

    if (isTherapyActive)
    {
      if (selfAdminActionEnum === enums.selfAdministeringActionEnum.AUTOMATICALLY_CHARTED)
      {
        return "icon_self_admin_automatic";
      }
      else if (selfAdminActionEnum === enums.selfAdministeringActionEnum.CHARTED_BY_NURSE)
      {
        return "icon_self_admin_nurse";
      }
    }
    return null;
  },

  /**
   * @param {String} selfAdminActionEnum of type {@link app.views.medications.TherapyEnums.selfAdministeringActionEnum}
   * @return {String|null}
   */
  getSelfAdminStatusDescription: function(selfAdminActionEnum)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;

    var selfAdministrationDescription = view.getDictionary('self.administered');
    if (selfAdminActionEnum === enums.selfAdministeringActionEnum.AUTOMATICALLY_CHARTED)
    {
      return selfAdministrationDescription + ' - ' + view.getDictionary('automatically.charted');
    }
    else if (selfAdminActionEnum === enums.selfAdministeringActionEnum.CHARTED_BY_NURSE)
    {
      return selfAdministrationDescription + ' - ' + view.getDictionary('charted.by.nurse');
    }
    return null;
  },

  getMedicationNameDisplay: function(medication, showGeneric)
  {
    var nameDisplay = "";
    var name = medication.name;

    if (showGeneric && medication.genericName)
    {
      nameDisplay += "<span class='TextDataBold'>" + medication.genericName + "</span>";
      nameDisplay += "<span class='TextData'> (" + name + ")";
    }
    else
    {
      nameDisplay += "<span class='TextData'>" + name;
    }
    nameDisplay += "</span>";
    return nameDisplay;
  },

  /**
   * Creates an object with background and additional therapy icons. Hpos and vpos define therapy icon location regarding
   * the background. Tag allows for identification of high priority icons.
   * @param {app.views.medications.common.therapy.AbstractTherapyContainerData} dto
   * @returns {{background: {cls: (String)}, layers: Array}}
   */
  getBigIconContainerOptions: function(dto)
  {
    var view = this.getView();
    var therapy = dto.therapy;

    var options = {
      background: {cls: this.getTherapyIcon(therapy)},
      layers: []
    };
    var statusIcon = this.getStatusIcon(dto);
    var pharmacistReviewStatusIcon = null;
    var selfAdminStatusIcon = null;

    if (view.getTherapyAuthority().isShowPharmacistReviewStatus() && !tm.jquery.Utils.isEmpty(dto.therapyPharmacistReviewStatus))
    {
      pharmacistReviewStatusIcon = this.getPharmacistReviewIcon(dto.therapyPharmacistReviewStatus);
    }

    if (!tm.jquery.Utils.isEmpty(therapy.selfAdministeringActionEnum))
    {
      selfAdminStatusIcon = this.getSelfAdminStatusIcon(therapy.selfAdministeringActionEnum, this.getTherapyStatus(dto));
      if (!tm.jquery.Utils.isEmpty(selfAdminStatusIcon))
      {
        options.layers.push({hpos: "right", vpos: "center", cls: "status-icon " + selfAdminStatusIcon});
      }
    }

    if (therapy != null && therapy.linkName)
    {
      var link = therapy.linkName;
      if (link.length <= 3)
      {
        options.layers.push({hpos: "left", vpos: "bottom", cls: "icon_link", html: link});
      }
    }
    if (dto.modifiedFromLastReview || !dto.isValid() || !therapy.isCompleted())
    {
      options.layers.push({hpos: "left", vpos: "top", cls: "icon_changed"});
    }
    if (dto.showConsecutiveDay)
    {
      options.layers.push({
        hpos: "right",
        vpos: "top",
        cls: "icon_day_number",
        html: dto.consecutiveDay,
        tag: app.views.medications.common.therapy.TherapyContainerDisplayProvider.CONSECUTIVE_DAY_ICON_TAG
      });
    }
    if (!tm.jquery.Utils.isEmpty(therapy.criticalWarnings) && therapy.criticalWarnings.length > 0)
    {
      options.layers.push({
        hpos: "center",
        vpos: "center",
        cls: "icon_warning",
        tag: app.views.medications.common.therapy.TherapyContainerDisplayProvider.CRITICAL_WARNINGS_ICON_TAG
      });
    }

    if (!tm.jquery.Utils.isEmpty(statusIcon))
    {
      options.layers.push({
        hpos: "right",
        vpos: "bottom",
        cls: statusIcon,
        tag: app.views.medications.common.therapy.TherapyContainerDisplayProvider.THERAPY_STATUS_ICON_TAG
      });
    }

    if (!tm.jquery.Utils.isEmpty(pharmacistReviewStatusIcon))
    {
      options.layers.push({hpos: "center", vpos: "bottom", cls: "status-icon " + pharmacistReviewStatusIcon});
    }

    if (dto.getTherapy().isLinkedToAdmission())
    {
      options.layers.push({hpos: "left", vpos: "center", cls: "icon_linked_to_admission"});
    }

    return options;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy|app.views.medications.common.dto.OxygenTherapy} therapy
   * @returns {String}
   */
  getTherapyIcon: function(therapy)
  {
    if (!tm.jquery.Utils.isEmpty(therapy) && therapy.getRoutes().length > 0)
    {
      var routeType = !tm.jquery.Utils.isEmpty(therapy.getRoutes()[0].type) ? therapy.getRoutes()[0].type : null;
      var therapyEnums = app.views.medications.TherapyEnums;

      if (therapy.hasBloodProduct())
      {
        return "icon_blood_product";
      }
      if (routeType === therapyEnums.medicationRouteTypeEnum.IV)
      {
        if (therapy.isBaselineInfusion())
        {
          return "icon_baseline_infusion";
        }
        if (therapy.isContinuousInfusion())
        {
          return "icon_continuous_infusion";
        }
        if (therapy.getSpeedDisplay())
        {
          if (therapy.isRateTypeBolus())
          {
            return "icon_bolus";
          }
          return "icon_infusion";
        }
        return "icon_injection"
      }
      if (routeType === therapyEnums.medicationRouteTypeEnum.IM)
      {
        return "icon_injection"
      }
      if (routeType === therapyEnums.medicationRouteTypeEnum.INHAL)
      {
        return "icon_inhalation"
      }
      if (therapy.getDoseForm() &&
          therapy.getDoseForm().getDoseFormType() === app.views.medications.TherapyEnums.doseFormType.TBL)
      {
        return "icon_pills";
      }
    }
    return "icon_other_medication";
  },

  getBigIconContainerHtml: function(dto)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();
    var options = this.getBigIconContainerOptions(dto);

    return appFactory.createLayersContainerHtml(options);
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {app.views.medications.common.dto.TherapyChange[]} changeData
   * @return {tm.jquery.Container}
   * @private
   */
  createChangeHistoryDetailsDescription: function(therapy, changeData)
  {
    var view = this.getView();

    var changeHistoryContainer = new tm.jquery.Container({
      cls: 'change-history',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    changeHistoryContainer.add(new tm.jquery.Container({
      cls: 'TextData history-title',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: view.getDictionary("history.of.changes")
    }));

    changeHistoryContainer.add(new app.views.medications.common.auditTrail.TherapyChangesContainer({
      view: view,
      therapyChanges: changeData,
      prevQuantityUnit: therapy.getQuantityUnit(),
      currentQuantityUnit: therapy.getQuantityUnit()
    }));

    return changeHistoryContainer;
  },

  /**
   * @param {String} changeType
   * @returns {tm.jquery.Container}
   * @private
   */
  createChangeTypeDescription: function(changeType)
  {
    var htmlString = '<p class="TextData history-title">' + this.getView().getDictionary("history.of.changes") + '</p>';
    var description = undefined;

    if (changeType === "ABORT")
    {
      description = this.getView().getDictionary("stop.past");
    }
    else if (changeType === "SUSPEND")
    {
      description = this.getView().getDictionary("suspend.past");
    }
    else
    {
      console.warn('Change type not supported:', changeType);
    }

    if (!tm.jquery.Utils.isEmpty(description))
    {
      htmlString += '<span class="TextLabel MedicationLabel">' + this.getView().getDictionary("action") + ' </span>';
      htmlString += '<span class="TextData new-data">' + description + '</span><br />';
    }

    return new tm.jquery.Container({
      cls: 'change-history',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: description ? htmlString : null
    });
  },

  /**
   * @param {app.views.medications.common.dto.TherapyChangeReason} changeReasonDto
   * @returns {tm.jquery.Container}
   * @private
   */
  createChangeReasonContent: function(changeReasonDto)
  {
    var view = this.getView();

    var reasonName = changeReasonDto.getReason().name;
    var comment = changeReasonDto.getComment();

    var reasonText = tm.jquery.Utils.isEmpty(reasonName) ? "" : reasonName;

    if (!tm.jquery.Utils.isEmpty(comment))
    {
      reasonText += " - " + comment;
    }

    var html = '<div class="TextLabel MedicationLabel">' + view.getDictionary("reason") +
        '</div><div class="TextData">' + tm.jquery.Utils.escapeHtml(reasonText) + '</div>';

    return new tm.jquery.Container({
      cls: 'change-reason',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: html
    });
  },

  /**
   * @param {Array<String>} validationIssues of {@link app.views.medications.TherapyEnums.validationIssueEnum}
   * @returns {string}
   */
  createValidationIssueDescription: function(validationIssues)
  {
    return validationIssues
        .map(
            /**
             * The dictionary prefix doesn't match the enum name due to the naming being very vague which means there's a
             * high chance somebody would reuse it.
             * @param {string} validationIssue of {@link app.views.medications.TherapyEnums.validationIssueEnum}
             * @return {string}
             */
            function(validationIssue)
            {
              return this.getView().getDictionary('therapy.validation.issue.' + validationIssue);
            },
            this)
        .join('. ') + '.';
  },

  /**
   * @return {tm.jquery.Component}
   */
  createRecordAdministrationDescription: function()
  {
    return new tm.jquery.Component({
      html: this.view.getDictionary('record.administration'),
      cls: 'TextLabel MedicationLabel'
    })
  },

  /**
   * @return {boolean}
   */
  isShowChangeHistory: function()
  {
    return this.showChangeHistory === true;
  },

  /**
   * @return {boolean}
   */
  isShowChangeReason: function()
  {
    return this.showChangeReason === true;
  },

  setShowChangeReason: function(value)
  {
    this.showChangeReason = value;
  },

  getShowMaxDose: function()
  {
    return this.showMaxDose === true;
  },

  setShowMaxDose: function(value)
  {
    this.showMaxDose = value;
  },
  setShowChangeHistory: function(value)
  {
    this.showChangeHistory = value;
  },

  /**
   * @returns {boolean}
   */
  isShowAuditTrail: function()
  {
    return this.showAuditTrail === true;
  },

  /**
   * @return {boolean}
   */
  isShowValidationIssues: function()
  {
    return this.showValidationIssues === true;
  },

  getView: function()
  {
    return this.view;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @returns {String}
   */
  getRemainingDurationDescription: function(therapy)
  {
    var remainingDurationInMinutes = therapy.getRemainingDurationInMinutes();

    if (remainingDurationInMinutes >= 60)
    {
      var remainingDurationInHours = Math.ceil(remainingDurationInMinutes / 60);
      return tm.jquery.Utils.formatMessage(this.getView().getDictionary("therapy.will.stop.hours"),
          remainingDurationInHours.toString());
    }
    else if (remainingDurationInMinutes < 60)
    {
      return tm.jquery.Utils.formatMessage(this.getView().getDictionary("therapy.will.stop.minutes"),
          remainingDurationInMinutes);
    }
  },

  /**
   * @returns {boolean}
   */
  getShowTherapyExpiring: function()
  {
    return this.showTherapyExpiring === true;
  }
});