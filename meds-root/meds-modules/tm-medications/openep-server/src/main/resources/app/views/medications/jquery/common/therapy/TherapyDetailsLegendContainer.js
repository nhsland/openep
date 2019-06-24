Class.define('app.views.medications.common.therapy.TherapyDetailsLegendContainer', 'tm.jquery.Container', {
  cls: 'legend-details-container',
  scrollable: 'vertical',

  therapy: null,
  medicationData: null,
  data: null,
  view: null,

  _contentContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildLegendContainer();
  },

  _buildLegendContainer: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'stretch', 0));

    var view = this.getView();
    var therapy = this.getTherapy();

    this._contentContainer = new tm.jquery.Container({
      cls: 'legend-content-container',
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'stretch', 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      hidden: true
    });

    var labelColumn = new tm.jquery.Container({
      cls: 'TextLabel row-label',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      html: view.getDictionary('legend')
    });
    this._contentContainer.add(labelColumn);

    var descriptionColumn = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    var maxDoseWarningsCls =
        app.views.medications.warnings.WarningsHelpers.getImageClsForMaximumDosePercentage(therapy.getMaxDosePercentage());
    if (!!maxDoseWarningsCls)
    {
      descriptionColumn.add(
          this._createLegendIconDescriptionRow(maxDoseWarningsCls,
              ' ' + this._getMaximumRecommendedDosePercentageDescription()));
    }
    this._addTherapyStatusLegendContent(descriptionColumn);

    if (tm.jquery.Utils.isArray(this.getData().tasks))
    {
      this._addTaskRemindersLegendContent(descriptionColumn);
    }

    this._addConflictedMedicationsLegendContent(descriptionColumn);

    if (therapy.isLinkedToAdmission())
    {
      descriptionColumn.add(
          this._createLegendIconDescriptionRow(
              'icon_linked_to_admission',
              view.getDictionary('therapy.from.admission')))
    }

    this._contentContainer.add(descriptionColumn);
    this.add(this._contentContainer);
  },

  _addTherapyStatusLegendContent: function(descriptionColumn)
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var data = this.getData();
    var displayProvider = this.getDisplayProvider();

    if (displayProvider.getStatusIcon(data))
    {
      var therapyStatusIcon = displayProvider.getStatusIcon(data);
      var therapyStatusDescription = displayProvider.getStatusDescription(data);
      descriptionColumn.add(this._createLegendIconDescriptionRow(therapyStatusIcon, ' ' + therapyStatusDescription));
    }

    if (data.modifiedFromLastReview || data.completed === false || therapy.completed === false)
    {
      var therapyModifiedDescription = view.getDictionary('modified');
      descriptionColumn.add(this._createLegendIconDescriptionRow('icon_changed', ' ' + therapyModifiedDescription));
    }

    if (therapy !== null && therapy.getLinkName())
    {
      var link = therapy.getLinkName();
      if (link.length <= 3)
      {
        var therapyLinkedDescription = view.getDictionary('linked.infusion');
        descriptionColumn.add(this._createLegendIconDescriptionRow('icon_link', ' ' + therapyLinkedDescription, link));
      }
    }
    if (data.showConsecutiveDay)
    {
      descriptionColumn.add(
          this._createLegendIconDescriptionRow('icon_day_number',
              " " + app.views.medications.MedicationUtils.createConsecutiveDayLabel(view),
              data.consecutiveDay));
    }
    if (data.therapy.isTherapyExpiring(data.originalTherapyStart))
    {
      var therapyExpireDescription = displayProvider.getRemainingDurationDescription(therapy);
      descriptionColumn.add(this._createLegendIconDescriptionRow('icon_therapy_expire', ' ' + therapyExpireDescription));
    }

    if (!tm.jquery.Utils.isEmpty(therapy.getCriticalWarnings()) && therapy.getCriticalWarnings().length > 0)
    {
      var criticalWarningsDescription = view.getDictionary('critical.warnings');
      descriptionColumn.add(this._createLegendIconDescriptionRow('icon_warning', ' ' + criticalWarningsDescription));
    }

    if (view.getTherapyAuthority().isShowPharmacistReviewStatus() && this.getTherapyPharmacistReviewStatus() &&
        displayProvider.getPharmacistReviewIcon(this.getTherapyPharmacistReviewStatus()))
    {
      var pharmacistReviewStatus = this.getTherapyPharmacistReviewStatus();
      var pharmacistReviewIcon = displayProvider.getPharmacistReviewIcon(pharmacistReviewStatus);
      var pharmacistReviewDescription = displayProvider.getPharmacistReviewDescription(pharmacistReviewStatus);
      descriptionColumn.add(this._createLegendIconDescriptionRow(pharmacistReviewIcon, ' ' + pharmacistReviewDescription));
    }

    if (displayProvider.getSelfAdminStatusIcon(therapy.getSelfAdministeringActionEnum(), this.getTherapyStatus()))
    {
      var selfAdminStatusIcon = displayProvider.getSelfAdminStatusIcon(therapy.getSelfAdministeringActionEnum(),
          this.getTherapyStatus());
      var selfAdminStatusDescription =
          displayProvider.getSelfAdminStatusDescription(therapy.getSelfAdministeringActionEnum());
      descriptionColumn.add(this._createLegendIconDescriptionRow(selfAdminStatusIcon, ' ' + selfAdminStatusDescription));
    }
  },

  _addTaskRemindersLegendContent: function(descriptionColumn)
  {
    var view = this.getView();
    var data = this.getData();
    var filterBy = new app.views.medications.common.therapy.TherapyDetailsLegendContainer.Filters;

    var isTaskLate = data.tasks.some(filterBy.isTaskLate);

    if (data.tasks.some(filterBy.isDoctorReviewTaskActive))
    {
      var doctorReviewIcon = isTaskLate ? 'icon-notification-urgent' : 'icon-notification';
      descriptionColumn.add(this._createLegendIconDescriptionRow(doctorReviewIcon, ' ' +
          view.getDictionary('therapy.review.reminder'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isSwitchToOralTaskActive))
    {
      var switchToOralIcon = isTaskLate ? 'icon-switch-to-oral-late' : 'icon-switch-to-oral';
      descriptionColumn.add(this._createLegendIconDescriptionRow(switchToOralIcon, '  ' +
          view.getDictionary('switch.IV.to.oral'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isPerfusionSyringeStartTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow('perfusion-syringe-icon start', ' ' +
          view.getDictionary('perfusion.syringe.preparation'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isPerfusionSyringeCompleteTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow('perfusion-syringe-icon complete', ' ' +
          view.getDictionary('perfusion.syringe.task.in.progress'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isPerfusionSyringeDispenseTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow('perfusion-syringe-icon dispense', ' ' +
          view.getDictionary('perfusion.syringe.task.closed'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isSupplyReminderTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow('icon-supply-reminder', ' ' +
          view.getDictionary('supply.reminder'), null, 24, 24));
    }

    if (data.tasks.some(filterBy.isSupplyReviewTaskActive))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow('icon-nurse-supply', ' ' +
          view.getDictionary('nurse.resupply.request'), null, 24, 24));
    }
  },

  _addConflictedMedicationsLegendContent: function(descriptionColumn)
  {
    var view = this.getView();
    var filterBy = new app.views.medications.common.therapy.TherapyDetailsLegendContainer.Filters;
    var medicationDataList = this.getMedicationData();
    var enums = app.views.medications.TherapyEnums;

    if (medicationDataList.some(filterBy.isControlledDrug))
    {
      var controlledDrugEnum = enums.medicationPropertyType.CONTROLLED_DRUG;
      descriptionColumn.add(this._createLegendIconDescriptionRow(controlledDrugEnum.toLowerCase() + '_icon', ' ' +
          view.getDictionary('MedicationPropertyType.' + controlledDrugEnum)));
    }

    if (medicationDataList.some(filterBy.isCriticalDrug))
    {
      var criticalDrugEnum = enums.medicationPropertyType.CRITICAL_DRUG;
      descriptionColumn.add(this._createLegendIconDescriptionRow(criticalDrugEnum.toLowerCase() + '_icon', ' ' +
          view.getDictionary('MedicationPropertyType.' + criticalDrugEnum)));
    }

    if (view.isFormularyFilterEnabled() && medicationDataList.some(filterBy.isMedicationNonFormulary))
    {
      descriptionColumn.add(this._createLegendIconDescriptionRow('non_formulary_icon', ' ' +
          view.getDictionary('non.formulary.medication')));
    }

    if (medicationDataList.some(filterBy.isBlackTriangleMedication))
    {
      var blackTriangleEnum = enums.medicationPropertyType.BLACK_TRIANGLE_MEDICATION;
      descriptionColumn.add(this._createLegendIconDescriptionRow(blackTriangleEnum.toLowerCase() + '_icon', ' ' +
          view.getDictionary('MedicationPropertyType.' + blackTriangleEnum)));
    }

    if (medicationDataList.some(filterBy.isUnlicensedMedication))
    {
      var unlicensedMedicationEnum = enums.medicationPropertyType.UNLICENSED_MEDICATION;
      descriptionColumn.add(this._createLegendIconDescriptionRow(unlicensedMedicationEnum.toLowerCase() + '_icon', ' ' +
          view.getDictionary('MedicationPropertyType.' + unlicensedMedicationEnum)));
    }

    if (medicationDataList.some(filterBy.isHighAlertMedication))
    {
      var highAlertEnum = enums.medicationPropertyType.HIGH_ALERT_MEDICATION;
      descriptionColumn.add(this._createLegendIconDescriptionRow(highAlertEnum.toLowerCase() + '_icon', ' ' +
          view.getDictionary('MedicationPropertyType.' + highAlertEnum)));
    }

    if (medicationDataList.some(filterBy.isClinicalTrialMedication))
    {
      var clinicalTrialEnum = enums.medicationPropertyType.CLINICAL_TRIAL_MEDICATION;
      descriptionColumn.add(this._createLegendIconDescriptionRow(clinicalTrialEnum.toLowerCase() + '_icon', ' ' +
          view.getDictionary('MedicationPropertyType.' + clinicalTrialEnum)));
    }

    if (medicationDataList.some(filterBy.isExpensiveDrug))
    {
      var expensiveDrugEnum = enums.medicationPropertyType.EXPENSIVE_DRUG;
      descriptionColumn.add(this._createLegendIconDescriptionRow(expensiveDrugEnum.toLowerCase() + '_icon', ' ' +
          view.getDictionary('MedicationPropertyType.' + expensiveDrugEnum)));
    }
  },

  /**
   * @param {String} legendIcon
   * @param {String} iconDescription
   * @param {String|null} [html=null]
   * @param {Number|null} [width=16]
   * @param {Number|null} [height=16]
   * @private
   */
  _createLegendIconDescriptionRow: function(legendIcon, iconDescription, html, width, height)
  {
    if (this._contentContainer.isHidden())
    {
      this._contentContainer.isRendered() ? this._contentContainer.show() : this._contentContainer.setHidden(false);
    }
    var descriptionRow = new tm.jquery.Container({
      cls: 'description-row-container',
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'stretch', 0)
    });

    var descriptionIcon = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      cls: 'row-icon ' + legendIcon,
      alignSelf: 'center',
      html: html,
      width: width ? width : 16,
      height: height ? height : 16
    });
    descriptionRow.add(descriptionIcon);

    var descriptionValue = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      cls: 'TextData legend-description',
      html: iconDescription
    });
    descriptionRow.add(descriptionValue);

    return descriptionRow;
  },

  /**
   * @returns {String}
   * @private
   */
  _getMaximumRecommendedDosePercentageDescription: function()
  {
    var percentage = this.getTherapy().getMaxDosePercentage();
    if (percentage < 50)
    {
      return this.getView().getDictionary('maximum.recommended.dose.low');
    }
    if (percentage < 100)
    {
      return this.getView().getDictionary('maximum.recommended.dose.medium');
    }
    return this.getView().getDictionary('maximum.recommended.dose.high');
  },

  /**
   * Getters & Setters
   */
  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  getData: function()
  {
    return this.data;
  },

  /**
   * @returns {app.views.medications.common.therapy.TherapyContainerDisplayProvider}
   */
  getDisplayProvider: function()
  {
    return this.displayProvider;
  },

  getTherapyPharmacistReviewStatus: function()
  {
    return this.getData().therapyPharmacistReviewStatus;
  },

  getTherapyStatus: function()
  {
    return this.getData().therapyStatus;
  }
});