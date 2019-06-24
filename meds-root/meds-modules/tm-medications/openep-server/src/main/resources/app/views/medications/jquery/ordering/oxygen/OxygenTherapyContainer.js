Class.define('app.views.medications.ordering.OxygenTherapyContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_CANCEL_EDIT: new tm.jquery.event.EventType({
      name: 'oxygenTherapyContainerCancelEdit', delegateName: null
    }),
    EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE: new tm.jquery.event.EventType({
      name: 'oxygenTherapyContainerEnableSaveTimestampChange', delegateName: null
    })
  },
  scrollable: 'vertical',
  cls: 'oxygen-therapy-container',
  /** @type tm.views.medications.TherapyView */
  view: null,
  medicationData: null,
  orderMode: true,
  changeReasonAvailable: false,
  orderingBehaviour: null,
  /** @type Array<app.views.medications.common.dto.InformationSource> */
  availableInformationSources: null,
  /** @type function(app.views.medications.ordering.ConfirmOrderEventData) */
  confirmOrderEventCallback: null,
  /** @type function(app.views.medications.ordering.SaveOrderToTemplateEventData)|null */
  saveOrderToTemplateEventCallback: null,

  _medicationField: null,
  _medicationInfoContainer: null,
  _therapyIntervalPane: null,
  _saturationInputContainer: null,
  _routeRowContainer: null,
  _startingFlowRateField: null,
  _supplyContainer: null,
  _highFlowCheckBox: null,
  _humidificationCheckBox: null,
  /** @type app.views.medications.ordering.CommentIndicationPane */
  _commentIndicationPane: null,
  _validationForm: null,
  _addToBasketButton: null,
  _administrationPreviewTimeline: null,
  _therapyNextAdministrationLabel: null,
  _previewRefreshTimer: null,
  _changeReasonContainer: null,
  /** @type app.views.medications.ordering.InformationSourceContainer|null */
  _informationSourceContainer: null,
  _flowRateValidator: null,
  _showInformationSourceButton: null,
  _originalTherapy: null,

  /** Persists the value of {@link Therapy#admissionId} when editing an existing therapy. */
  _medicationOnAdmissionId: null,
  /** Persists the value of {@link Therapy#informationSources} when editing an existing therapy. */
  _informationSources: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.registerEventTypes('app.views.medications.ordering.OxygenTherapyContainer', [
      {eventType: app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_CANCEL_EDIT},
      {eventType: app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE}
    ]);

    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined.');
    }

    this._flowRateValidator = new app.views.medications.ordering.oxygen.OxygenFlowRateValidator({
      view: this.getView()
    });
    this.availableInformationSources = tm.jquery.Utils.isArray(this.availableInformationSources) ?
        this.availableInformationSources :
        [];
    this._informationSources = [];

    this._buildGui();
    this._buildValidationForm();
  },

  _buildGui: function()
  {
    var view = this.getView();
    var self = this;

    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch'));

    this.add(this._buildMedicationRowComponent());

    var doseRowContainer = new tm.jquery.Container({
      cls: 'dose-row-container',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      layout: new tm.jquery.HFlexboxLayout({align: 'right'}),
      scrollable: 'visible'
    });
    this.add(doseRowContainer);

    if (this.isOrderMode())
    {
      var cancelEditButton = new tm.jquery.Container({
        cls: 'remove-icon clear-button',
        width: 30,
        height: 30
      });
      cancelEditButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
      {
        self._fireCancelEditEvent();
      });
      doseRowContainer.add(cancelEditButton);
    }

    if (this.getOrderingBehaviour().isOxygenSaturationAvailable())
    {
      var saturationRowComponent = this._buildSaturationRowComponent();
      this._saturationInputContainer = saturationRowComponent.getContentComponent();
    }

    this._therapyIntervalPane = this._buildTherapyIntervalRowComponent();

    var routeRowComponent = this._buildOxygenRouteRowComponent();
    this._routeRowContainer = routeRowComponent.getContentComponent();

    var startingFlowRateRowComponent = new app.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      titleText: view.getDictionary('rate'),
      contentComponent: new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
        layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center')
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this._startingFlowRateField = new tm.jquery.NumberField({
      width: 70,
      cls: 'start-flow-rate-input',
      formatting: {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2}
    });
    startingFlowRateRowComponent.getContentComponent().add(this._startingFlowRateField);
    startingFlowRateRowComponent.getContentComponent().add(new tm.jquery.Component({
      cls: 'start-flow-rate-label',
      html: 'L/min'
    }));

    if (this.getOrderingBehaviour().isSupplyAvailable())
    {
      this._supplyContainer = new app.views.medications.ordering.supply.TherapySupplyContainer({
        view: view,
        required: this.getOrderingBehaviour().isSupplyRequired(),
        whenNeededSupported: false
      });
    }

    var additionalInformationRow = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('additional.information'),
      contentComponent: new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
        layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch')
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this._highFlowCheckBox = new tm.jquery.CheckBox({
      cls: 'high-flow-checkbox',
      labelText: view.getDictionary('high.flow.oxygen.therapy'),
      labelCls: 'TextData',
      checked: false,
      labelAlign: 'right',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      nowrap: true
    });

    this._humidificationCheckBox = new tm.jquery.CheckBox({
      cls: 'humidification-checkBox',
      labelText: view.getDictionary('humidification'),
      labelCls: 'TextData',
      checked: false,
      labelAlign: 'right',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      nowrap: true
    });

    additionalInformationRow.getContentComponent().add(this._highFlowCheckBox);
    additionalInformationRow.getContentComponent().add(this._humidificationCheckBox);

    if (this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      this._administrationPreviewTimeline = new app.views.medications.ordering.timeline.AdministrationPreviewTimeline({
        view: view,
        autoDraw: this.isOrderMode()
      });
    }

    this._commentIndicationPane = new app.views.medications.ordering.CommentIndicationPane({
      orderingBehaviour: this.getOrderingBehaviour(),
      view: view,
      saveDateTimePaneEvent: function()
      {
        self._fireEnableSaveTimestampChange();
      }
    });

    this._therapyNextAdministrationLabel = new app.views.medications.ordering.TherapyNextAdministrationLabelPane({
      hidden: !this.getOrderingBehaviour().isStartEndTimeAvailable(),
      view: view
    });

    this._changeReasonContainer = new app.views.medications.ordering.ChangeReasonContainer({
      view: view,
      hidden: !this.isChangeReasonAvailable()
    });

    if (this.getOrderingBehaviour().isInformationSourceAvailable())
    {
      this._informationSourceContainer = new app.views.medications.ordering.InformationSourceContainer({
        view: view,
        availableInformationSources: this.availableInformationSources,
        required: this.getOrderingBehaviour().isInformationSourceRequired(),
        hidden: !this.getOrderingBehaviour().isInformationSourceRequired()
      });
    }

    if (saturationRowComponent)
    {
      this.add(saturationRowComponent);
    }
    this.add(startingFlowRateRowComponent);
    this.add(routeRowComponent);
    this.add(additionalInformationRow);
    this.add(this._therapyIntervalPane);
    if (!!this._supplyContainer)
    {
      this.add(this._supplyContainer);
    }
    this.add(this._therapyNextAdministrationLabel);
    if (!!this._administrationPreviewTimeline)
    {
      this.add(this._administrationPreviewTimeline);
    }
    this.add(this._commentIndicationPane);
    this.add(this._changeReasonContainer);
    if (!!this._informationSourceContainer)
    {
      this.add(this._informationSourceContainer);
    }
    if (this.isOrderMode())
    {
      this.add(this._buildNavigationContainer());
    }

    this._applyChangeReasonContainerVisibility();
  },

  _buildValidationForm: function()
  {
    var self = this;

    this._validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._confirmOrder();
      },
      onValidationError: function()
      {
        self.confirmOrderEventCallback(new app.views.medications.ordering.ConfirmOrderEventData({validationPassed: false}));

        if (self._addToBasketButton)
        {
          self._addToBasketButton.setEnabled(true);
        }
      },
      requiredFieldValidatorErrorMessage: this.getView().getDictionary('field.value.is.required')
    });
  },

  /**
   * @returns {tm.jquery.Container}
   * @private
   */
  _buildMedicationRowComponent: function()
  {
    var view = this.getView();
    var self = this;
    var appFactory = view.getAppFactory();

    var medicationRowContainer = new tm.jquery.Container({
      scrollable: 'visible',
      cls: 'medication-container',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start')
    });

    var medicationDetailsPane = new app.views.medications.therapy.MedicationDetailsCardPane({view: view});
    var detailsCardTooltip = appFactory.createDefaultPopoverTooltip(
        view.getDictionary('medication'),
        null,
        medicationDetailsPane
    );
    detailsCardTooltip.onShow = function()
    {
      medicationDetailsPane.setMedicationData(self.getMedicationData());
    };

    this._medicationField = new app.views.medications.common.MedicationSearchField({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      enabled: false,
      dropdownWidth: "stretch",
      dropdownAppendTo: view.getAppFactory().getDefaultRenderToElement(), /* due to the dialog use */
      searchResultFormatter: this.getOrderingBehaviour().getMedicationSearchResultFormatter()
    });

    this._medicationField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
    {
      self._onMedicationFieldSelection(component.getSelectionMedication());
    });

    medicationRowContainer.add(this._medicationField);

    this._medicationInfoContainer = new tm.jquery.Container({
      cls: 'info-icon pointer-cursor medication-info',
      width: 25,
      height: 30,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      tooltip: detailsCardTooltip
    });
    medicationRowContainer.add(this._medicationInfoContainer);

    return medicationRowContainer;
  },

  /**
   * @returns {app.views.medications.common.VerticallyTitledComponent}
   * @private
   */
  _buildSaturationRowComponent: function()
  {
    var view = this.getView();

    var saturationInputContainer = new app.views.medications.ordering.oxygen.OxygenSaturationInputContainer({
      view: view
    });

    return new app.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      titleText: view.getDictionary('target.saturation'),
      contentComponent: saturationInputContainer,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
  },

  /**
   * @returns {app.views.medications.common.VerticallyTitledComponent}
   * @private
   */
  _buildOxygenRouteRowComponent: function()
  {
    var view = this.getView();

    var oxygenRouteContainer = new app.views.medications.ordering.oxygen.OxygenRouteContainer({
      view: view
    });

    return new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('device'),
      scrollable: 'visible',
      contentComponent: oxygenRouteContainer,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
  },

  /**
   * @returns {app.views.medications.ordering.TherapyIntervalPane}
   * @private
   */
  _buildTherapyIntervalRowComponent: function()
  {
    var self = this;
    var intervalPane = new app.views.medications.ordering.TherapyIntervalPane({
      view: this.getView(),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      restrictedStartHourSelection: false,
      presetCurrentTime: true,
      byDoctorsOrderSupported: false,
      whenNeededSupported: false,
      maxDailyFrequencyAvailable: false,
      orderingBehaviour: this.getOrderingBehaviour(),
      hidden: this.getOrderingBehaviour().isSupplyAvailable(),
      getDosingPatternFunction: function()
      {
        return [];
      }
    });
    intervalPane.on(app.views.medications.ordering.TherapyIntervalPane.EVENT_TYPE_INTERVAL_CHANGE,
        function(component, componentEvent)
        {
          var eventData = componentEvent.eventData;
          self._onTherapyIntervalChange(eventData.start);
        });
    return intervalPane;
  },

  /**
   * @returns {tm.jquery.Container}
   * @private
   */
  _buildNavigationContainer: function()
  {
    var self = this;
    var view = this.getView();

    var navigationContainer = new tm.jquery.Container({
      cls: 'navigation-container',
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start', 20),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    var cancelEditButton = new tm.jquery.Button({
      cls: 'cancel-edit-button',
      text: view.getDictionary('empty.form'),
      type: 'link',
      handler: function()
      {
        self._fireCancelEditEvent();
      }
    });

    if (this.getOrderingBehaviour().isAddToTemplateAvailable() && view.getTherapyAuthority().isManageAnyTemplatesAllowed())
    {
      var saveToTemplateButton = new tm.jquery.Button({
        cls: "save-to-template-button",
        text: view.getDictionary('add.to.order.set'),
        type: "link",
        handler: this._onAddToTemplate.bind(this)
      });
    }

    var addToBasketButton = new tm.jquery.Button({
      cls: 'add-to-basket-button',
      text: view.getDictionary('add'),
      handler: function(component)
      {
        component.setEnabled(false);
        self.validateAndConfirmOrder();
      }
    });

    navigationContainer.add(cancelEditButton);
    if (!!saveToTemplateButton)
    {
      navigationContainer.add(saveToTemplateButton);
    }
    if (!!this._informationSourceContainer && !this.getOrderingBehaviour().isInformationSourceRequired())
    {
      this._showInformationSourceButton = new tm.jquery.Button({
        text: view.getDictionary('show.source'),
        type: "link",
        handler: function()
        {
          self._applyInformationSourceVisibility(true);
        }
      });

      navigationContainer.add(this._showInformationSourceButton);
    }
    navigationContainer.add(addToBasketButton);

    this._addToBasketButton = addToBasketButton;

    return navigationContainer;
  },

  _applyChangeReasonContainerVisibility: function()
  {
    if (this.isChangeReasonAvailable())
    {
      this.isRendered() ? this._changeReasonContainer.show() : this._changeReasonContainer.setHidden(false);
    }
    else
    {
      this.isRendered() ? this._changeReasonContainer.hide() : this._changeReasonContainer.setHidden(true);
    }
  },

  _fireCancelEditEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_CANCEL_EDIT
    }), null);
  },

  _fireEnableSaveTimestampChange: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.ordering.OxygenTherapyContainer.EVENT_TYPE_ENABLE_SAVE_TIMESTAMP_CHANGE
    }), null);
  },

  _setupValidation: function()
  {
    this._validationForm.reset();

    if (this.getOrderingBehaviour().isOxygenSaturationAvailable())
    {
      this._addValidationFormFields(this._saturationInputContainer.getFormFieldValidators());
    }
    if (this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      this._addValidationFormFields(this._therapyIntervalPane.getTherapyIntervalPaneValidations());
    }
    if (this.getOrderingBehaviour().isSupplyAvailable())
    {
      this._addValidationFormFields(this._supplyContainer.getFormValidations());
    }
    if (this._isCommentRequired())
    {
      this._addValidationFormFields(this._commentIndicationPane.getCommentFieldValidation());
    }
    if (!this.orderingBehaviour.isIndicationAlwaysOptional())
    {
      this._addValidationFormFields(this._commentIndicationPane.getIndicationValidations());
    }
    if (!this._changeReasonContainer.isHidden())
    {
      this._changeReasonContainer.attachRemoteFormValidation(
          this.getOriginalTherapy(),
          this._buildTherapy(),
          this._validationForm);
    }
    if (!!this._informationSourceContainer)
    {
      this._addValidationFormFields(this._informationSourceContainer.getFormValidations());
    }
    this._addValidationFormFields(this._flowRateValidator.getAsFormFieldValidators(this._startingFlowRateField));
  },

  /**
   * @param {Array<tm.jquery.FormField>} formFields
   * @private
   */
  _addValidationFormFields: function(formFields)
  {
    for (var i = 0; i < formFields.length; i++)
    {
      this._validationForm.addFormField(formFields[i]);
    }
  },

  /**
   * Based on the current state of the form.
   * @returns {app.views.medications.common.dto.OxygenTherapy}
   * @private
   */
  _buildTherapy: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var saturation = this.getOrderingBehaviour().isOxygenSaturationAvailable() ?
        this._saturationInputContainer.getSaturation() :
        null;
    var medicationData = this.getMedicationData();

    return new app.views.medications.common.dto.OxygenTherapy({
      medication: medicationData ? medicationData.getMedication() : null,
      variable: false,
      start: !this._therapyIntervalPane.isHidden() ? this._therapyIntervalPane.getStart() : null,
      end: !this._therapyIntervalPane.isHidden() ? this._therapyIntervalPane.getEnd() : null,
      flowRate: this._startingFlowRateField.getValue(),
      flowRateMode: this._highFlowCheckBox.isChecked() ? enums.flowRateMode.HIGH_FLOW : enums.flowRateMode.LOW_FLOW,
      startingDevice: this._routeRowContainer.getStartingDevice(),
      minTargetSaturation: saturation ? saturation.getMin() : null,
      maxTargetSaturation: saturation ? saturation.getMax() : null,
      humidification: this._humidificationCheckBox.isChecked(),
      comment: this._commentIndicationPane.getComment(),
      clinicalIndication: this._commentIndicationPane.getIndication(),
      dispenseDetails: this.getOrderingBehaviour().isSupplyAvailable() ? this._supplyContainer.getDispenseDetails() : null,
      admissionId: this._medicationOnAdmissionId,
      informationSources: !!this._informationSourceContainer ?
          this._informationSourceContainer.getSelections() :
          this._informationSources
    });
  },

  /**
   * Based on the current state of the form. Includes a new instance of a therapy.
   * @return {app.views.medications.ordering.TherapyOrder}
   * @private
   */
  _buildTherapyOrder: function()
  {
    return new app.views.medications.ordering.TherapyOrder()
        .setTherapy(this._buildTherapy())
        .setTherapyChangeReason(
            !this._changeReasonContainer.isHidden() ?
                this._changeReasonContainer.getTherapyChangeReason() :
                null);
  },

  _confirmOrder: function()
  {
    var view = this.getView();

    var confirmEventData = new app.views.medications.ordering.ConfirmOrderEventData({
      therapyOrder: this._buildTherapyOrder()
    });

    var confirmSuccess = this.confirmOrderEventCallback(confirmEventData);
    if (!confirmSuccess)
    {
      if (this._addToBasketButton)
      {
        this._addToBasketButton.setEnabled(true);
      }
    }
    else if (!!confirmEventData.getTherapyOrder().getTherapy().getLinkName())
    {
      view.setPatientLastLinkNamePrefix(confirmEventData.getTherapyOrder().getTherapy().getLinkName().substring(0, 1));
    }

    if (view.getViewMode() === 'ORDERING_PAST')
    {
      view.setPresetDate(confirmEventData.getTherapyOrder().getTherapy().getStart());
    }
  },

  /**
   * Event handler for the {@link #saveToTemplateButton} click.
   * @private
   */
  _onAddToTemplate: function()
  {
    if (!this.saveOrderToTemplateEventCallback)
    {
      return;
    }

    this._setupValidation();

    this.saveOrderToTemplateEventCallback(
        new app.views.medications.ordering.SaveOrderToTemplateEventData({
          therapyOrder: this._buildTherapyOrder(),
          validationPassed: !this._validationForm.hasFormErrors()
        }));
  },

  /**
   * @param {Date|null} startTime
   * @private
   */
  _onTherapyIntervalChange: function(startTime)
  {
    this._therapyNextAdministrationLabel.setNextAdministration(startTime);
    this.refreshAdministrationPreview();
  },

  /**
   * @param {app.views.medications.common.dto.Medication|null} selection
   * @private
   */
  _onMedicationFieldSelection: function(selection)
  {
    var self = this;

    if (selection && (!this.getMedicationData() || this.getMedicationData().getMedication().getId() !== selection.getId()))
    {
      this.getView().getRestApi().loadMedicationData(selection.getId()).then(function setData(medicationData)
      {
          self.setMedicationData(medicationData);
      });
    }
  },

  /**
   * Applies the visibility of the source of information input field and the corresponding button that toggles it's
   * visibility, when the source of information input is not required. Does nothing if the source of information
   * field is either not available or the input is required. If the field should be hidden, it's selection is also
   * cleared to prevent accidental application of a hidden value.
   * @param {boolean} visible
   * @private
   */
  _applyInformationSourceVisibility: function(visible)
  {
    if (!this._informationSourceContainer || this.getOrderingBehaviour().isInformationSourceRequired())
    {
      return;
    }

    if (visible === true)
    {
      this._informationSourceContainer.isRendered() ?
          this._informationSourceContainer.show() :
          this._informationSourceContainer.setHidden(false);
      this._showInformationSourceButton.isRendered() ?
          this._showInformationSourceButton.hide() :
          this._showInformationSourceButton.setHidden(true);
    }
    else
    {
      this._informationSourceContainer.isRendered() ?
          this._informationSourceContainer.hide() :
          this._informationSourceContainer.setHidden(true);
      this._showInformationSourceButton.isRendered() ?
          this._showInformationSourceButton.show() :
          this._showInformationSourceButton.setHidden(false);
    }
  },

  /**
   * When formulary medication lists are configured, prescribing medications not classified as formulary is discouraged and
   * a reason for such a selection must be entered into the comment field.
   * @returns {boolean}
   * @private
   */
  _isCommentRequired: function()
  {
    return !this.orderingBehaviour.isCommentAlwaysOptional() &&
        (this.view.isFormularyFilterEnabled() ? !this.medicationData.isFormulary() : false);
  },

  clear: function()
  {
    this._originalTherapy = null;
    this.medicationData = null;
    this._medicationField.setSelection(null, true);
    if (this.getOrderingBehaviour().isOxygenSaturationAvailable())
    {
      this._saturationInputContainer.clear();
    }
    this._therapyIntervalPane.clear(true);
    this._routeRowContainer.clear();
    this._humidificationCheckBox.setChecked(false, true);
    this._highFlowCheckBox.setChecked(false, true);
    this._startingFlowRateField.setValue(null, true);
    this._commentIndicationPane.clear();
    this._changeReasonContainer.clear();
    if (!!this._informationSourceContainer)
    {
      this._applyInformationSourceVisibility(false);
      this._informationSourceContainer.clear();
    }
    this._therapyNextAdministrationLabel.clear();
    this._validationForm.reset();
    if (this._addToBasketButton)
    {
      this._addToBasketButton.setEnabled(true);
    }
    if (this.getOrderingBehaviour().isStartEndTimeAvailable())
    {
      this._administrationPreviewTimeline.clear();
    }
    if (this.getOrderingBehaviour().isSupplyAvailable())
    {
      this._supplyContainer.clear();
    }
    this._medicationOnAdmissionId = null;
    this._informationSources = [];
  },

  /**
   * @param {Date} startDate
   */
  setTherapyStart: function(startDate)
  {
    this._therapyIntervalPane.setStart(startDate, true);
    this._therapyNextAdministrationLabel.setNextAdministration(startDate);
    this.refreshAdministrationPreview();
  },

  /**
   * @param {function(app.views.medications.ordering.ConfirmOrderEventData)} callback
   */
  setConfirmOrderEventCallback: function(callback)
  {
    this.confirmOrderEventCallback = callback;
  },

  validateAndConfirmOrder: function()
  {
    this._setupValidation();
    this._validationForm.submit();
  },

  refreshAdministrationPreview: function()
  {
    if (!this._administrationPreviewTimeline)
    {
      return;
    }

    var self = this;
    /* add a small delay so we don't call it too often*/
    clearTimeout(this._previewRefreshTimer);

    this._previewRefreshTimer = setTimeout(function()
    {
      if (self.isRendered())
      {
        self._administrationPreviewTimeline.refreshData(
            self._therapyIntervalPane.getStart(),
            self._buildTherapy());
      }
    }, 150);
  },

  /**
   * @returns {boolean}
   */
  isChangeReasonAvailable: function()
  {
    return this.changeReasonAvailable === true;
  },

  /**
   * Override.
   */
  destroy: function()
  {
    clearTimeout(this._previewRefreshTimer);
    this.callSuper();
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  setMedicationData: function(medicationData)
  {
    this.medicationData = medicationData;
    this._medicationField.setSelection(medicationData.getMedication());
    if (!!this._supplyContainer)
    {
      this._supplyContainer.setOptionsByMedication(medicationData);
    }
  },

  /**
   * @param {app.views.medications.common.dto.OxygenTherapy|app.views.medications.common.dto.Therapy} oxygenTherapy
   * @param {Boolean} [showPreviousAdministrations=false]
   * @param {Boolean} [copying=false] Are we copying the therapy as a new prescription?
   */
  setOxygenTherapy: function(oxygenTherapy, showPreviousAdministrations, copying)
  {
    this._originalTherapy = oxygenTherapy;

    var therapyHasAlreadyStarted = oxygenTherapy.getStart() < moment(CurrentTime.get()).startOf('minute');

    var startTime =
        !therapyHasAlreadyStarted || this.getOrderingBehaviour().isPastMode() ?
            oxygenTherapy.getStart() :
            app.views.medications.MedicationTimingUtils.getTimestampRoundedUp(CurrentTime.get(), 5);

    this._therapyIntervalPane.setStart(startTime, true);
    if (!copying)
    {
      this._therapyIntervalPane.setEnd(oxygenTherapy.getEnd());
    }
    this._startingFlowRateField.setValue(oxygenTherapy.getFlowRate(), true);
    this._highFlowCheckBox.setChecked(oxygenTherapy.isHighFlowOxygen());
    this._humidificationCheckBox.setChecked(oxygenTherapy.isHumidification());
    if (this.getOrderingBehaviour().isOxygenSaturationAvailable())
    {
      this._saturationInputContainer.setSaturation(
              app.views.medications.common.dto.Range.createStrict(
                  oxygenTherapy.getMinTargetSaturation(),
                  oxygenTherapy.getMaxTargetSaturation()
              )
      );
    }
    this._routeRowContainer.setStartingDevice(oxygenTherapy.getStartingDevice());
    this._commentIndicationPane.setComment(oxygenTherapy.getComment());
    this._commentIndicationPane.setIndication(oxygenTherapy.getClinicalIndication());
    this.refreshAdministrationPreview();

    if (this.getOrderingBehaviour().isSupplyAvailable())
    {
      this._supplyContainer.setDispenseDetails(oxygenTherapy.getDispenseDetails());
    }
    if (!this.isOrderMode())
    {
      this._medicationField.setLimitBySimilar(oxygenTherapy.getMedication());
      if (showPreviousAdministrations)
      {
        this._therapyNextAdministrationLabel.setOldTherapyId(
            oxygenTherapy.getCompositionUid(),
            oxygenTherapy.getEhrOrderName(),
            true);
      }
    }

    this._medicationOnAdmissionId = !copying ? oxygenTherapy.getAdmissionId() : null;

    if (!!this._informationSourceContainer)
    {
      this._informationSourceContainer.setSelections(oxygenTherapy.getInformationSources());
      this._applyInformationSourceVisibility(oxygenTherapy.getInformationSources().length > 0);
    }
    else if (!copying)
    {
      this._informationSources = oxygenTherapy.getInformationSources().slice(0);
    }
  },

  /**
   * @param {app.views.medications.ordering.AbstractTherapyOrder|
   *         app.views.medications.common.therapy.AbstractTherapyContainerData} therapyOrder
   * @param {Boolean} [showPreviousAdministrations=false]
   * @param {Boolean} [copying=false] Are we copying the therapy as a new prescription?
   */
  setOxygenTherapyFromOrder: function(therapyOrder, showPreviousAdministrations, copying)
  {
    this.setOxygenTherapy(therapyOrder.getTherapy(), showPreviousAdministrations, copying);
    this._changeReasonContainer.setTherapyChangeReason(therapyOrder.getTherapyChangeReason());
  },

  /**
   * Sets the availability of the change reason input fields and ensures the correct availability of the input fields.
   * @param {boolean} available
   */
  setChangeReasonAvailable: function(available)
  {
    this.changeReasonAvailable = available === true;
    this._applyChangeReasonContainerVisibility();
  },

  /**
   * @see app.views.medications.ordering.InformationSourceContainer#setDefaultSelections
   * @param {Array<app.views.medications.common.dto.InformationSource>} sources
   * @param {boolean} [overrideCurrentSelection=false]
   */
  setDefaultInformationSources: function(sources, overrideCurrentSelection)
  {
    if (!this._informationSourceContainer)
    {
      return;
    }

    this._informationSourceContainer.setDefaultSelections(sources, overrideCurrentSelection === true);
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {boolean}
   */
  isOrderMode: function()
  {
    return this.orderMode === true;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /**
   * @protected
   * @return {app.views.medications.common.dto.OxygenTherapy|app.views.medications.common.dto.Therapy|null} the instance
   * of the original therapy in case of the edit or copy operation, otherwise null. Do not change this instance, it might
   * still be used by the owner!
   */
  getOriginalTherapy: function()
  {
     return this._originalTherapy;
  },

  /**
   * Limits the earliest possible therapy start to provided time. If no time is provided, therapy start will not be limited.
   * Useful when we want to limit the start time when editing the therapy.
   * @param {Date|null} minimumStartTime
   */
  setMinimumStartTime: function(minimumStartTime)
  {
    this._therapyIntervalPane.limitMinStartDateTimePickers(minimumStartTime);
  },

  /**
   * Should only be used when we want to set the earliest possible therapy start to provided time. If called, the
   * {@link #_therapyIntervalPane} will set its internal timeField to at least one minute in the future to prevent editing
   * a therapy in the past. If no edit time is provided, current time is being used as the earliest possible edit time.
   * Useful when we want to limit the start time when editing the therapy.
   * @param {Date|null} editTime
   */
  adjustStartTimeToOrderTime: function(editTime)
  {
    this._therapyIntervalPane.adjustStartTimeToOrderTime(editTime)
  }
});
