Class.define('app.views.medications.common.overview.header.WestToolbarContainer', 'app.views.medications.common.overview.header.AbstractToolbarContainer', {
  statics: {
    EVENT_TYPE_BARCODE_SCANNED: new tm.jquery.event.EventType({
      name: 'westToolbarContainerMedicationBarcodeScanned', delegateName: null
    }),
    EVENT_TYPE_START_NEW_RECONCILIATION: new tm.jquery.event.EventType({
      name: 'westToolbarContainerStartNewReconciliation', delegateName: null
    })
  },
  cls: 'west-container',
  scrollable: 'visible',
  /** @type app.views.medications.TherapyView */
  view: null,
  /* Optional configuration - permissions and other restrictions still apply. */
  /** @type boolean */
  withOutpatientPrescribing: false,
  /** @type boolean */
  withInpatientPrescribing: false,
  /** @type boolean */
  withPreviousHospitalization: false,
  /** @type boolean */
  withReferenceWeight: false,
  /** @type boolean */
  withReportPrinting: false,
  /** @type boolean */
  withMedicationBarcodeInput: false,
  /** @type boolean */
  withMedicationReconciliation: false,
  /** @type boolean */
  withWarningScan: false,

  /** @type number that specifies the time, in milliseconds, after which a clicked button will be enabled again.*/
  _preventDoubleClickTimeoutLength: 250,
  /** @type tm.jquery.Button|null */
  _previousHospitalizationTherapiesButton: null,
  /** @type number|undefined */
  _outpatientButtonDoubleClickTimer: undefined,
  /** @type number|undefined */
  _orderButtonDoubleClickTimer: undefined,
  /** @type app.views.medications.common.overview.header.DataEntryTooltipFactory */
  _dataEntryTooltipFactory: null,
  /** @type tm.jquery.Component|null */
  _lastReconciliationStartDateNotificationComponent: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.registerEventTypes('app.views.medications.common.overview.header.WestToolbarContainer', [
      {eventType: app.views.medications.common.overview.header.WestToolbarContainer.EVENT_TYPE_BARCODE_SCANNED},
      {eventType: app.views.medications.common.overview.header.WestToolbarContainer.EVENT_TYPE_START_NEW_RECONCILIATION}
    ]);
    this._dataEntryTooltipFactory = new app.views.medications.common.overview.header.DataEntryTooltipFactory({
      view: this.view
    });
    this._buildGui();
  },

  /**
   * Sets the focus to the medication barcode input field. Does nothing if {@link #withMedicationBarcodeInput} is not
   * set to true.
   */
  applyBarcodeFieldFocus: function()
  {
    if (this._medicationBarcodeField)
    {
      this._medicationBarcodeField.focus();
    }
  },

  /**
   * Updates the contents of the last reconciliation start date notification, if the toolbar is configured with
   * {@link withMedicationReconciliation}. When no date is given the notification component is hidden to avoid it taking up
   * empty space from any possible margins or padding.
   * @param {Date|null} date
   */
  applyLastReconciliationStartDate: function(date)
  {
    if (!this._lastReconciliationStartDateNotificationComponent)
    {
      return;
    }

    if (!!date)
    {
      this._lastReconciliationStartDateNotificationComponent.setHtml(
          tm.jquery.Utils.formatMessage(
              this.view.getDictionary('medication.reconciliation.started.on'),
              this.view.getDisplayableValue(date, "short.date")));
      this._lastReconciliationStartDateNotificationComponent.isRendered() ?
          this._lastReconciliationStartDateNotificationComponent.show() :
          this._lastReconciliationStartDateNotificationComponent.setHidden(false);
    }
    else
    {
      this._lastReconciliationStartDateNotificationComponent.isRendered() ?
          this._lastReconciliationStartDateNotificationComponent.hide() :
          this._lastReconciliationStartDateNotificationComponent.setHidden(true);
      this._lastReconciliationStartDateNotificationComponent.setHtml(null);
    }
  },

  /**
   * Toggle the visibility for the toolbar button which shows previous hospitalization therapies. Does nothing
   * if {@link #withPreviousHospitalization} is not set to true.
   * @param visible
   */
  applyPreviousHospitalizationTherapiesButtonVisibility: function(visible)
  {
    if (this._previousHospitalizationTherapiesButton)
    {
      if (visible)
      {
        this._previousHospitalizationTherapiesButton.isRendered() ?
            this._previousHospitalizationTherapiesButton.show() :
            this._previousHospitalizationTherapiesButton.setHidden(false);
      }
      else
      {
        this._previousHospitalizationTherapiesButton.isRendered() ?
            this._previousHospitalizationTherapiesButton.hide() :
            this._previousHospitalizationTherapiesButton.setHidden(true);
      }
    }
  },

  /**
   * @param {app.views.medications.common.overview.AbstractSubViewContainer} subView
   */
  attachEventsToSubView: function(subView)
  {
    subView.attachOverviewHeaderWestToolbarEvents(this);
  },

  /**
   * @Override
   */
  destroy: function()
  {
    this.callSuper();
    clearTimeout(this._orderButtonDoubleClickTimer);
    clearTimeout(this._outpatientButtonDoubleClickTimer);
  },

  /**
   * @private
   */
  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 5));

    var self = this;
    var view = this.view;

    if (this.withOutpatientPrescribing && view.getTherapyAuthority().isManageOutpatientPrescriptionsAllowed())
    {
      this.add(new tm.jquery.Button({
        cls: 'btn-flat',
        iconCls: 'icon-add-outpatient-prescription',
        tooltip: app.views.medications.MedicationUtils.createTooltip(
            view.getDictionary('new.outpatient.prescription'), 'bottom', view),
        handler: function(component)
        {
          component.setEnabled(false);
          view.openOutpatientOrderingDialog();
          clearTimeout(self._outpatientButtonDoubleClickTimer);
          self._outpatientButtonDoubleClickTimer = setTimeout(
              function()
              {
                component.setEnabled(true);
              },
              self._preventDoubleClickTimeoutLength);
        }
      }));
    }

    if (this.withInpatientPrescribing)
    {
      var orderPopupMenu = new tm.jquery.PopupMenu();

      var orderButton = new tm.jquery.SplitButton({
        cls: 'order-medications-button splitbutton-flat',
        iconCls: 'icon-add',
        popupMenu: orderPopupMenu,
        handler: function(component)
        {
          if (!view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() &&
              !view.getTherapyAuthority().isPrescribeByTemplatesAllowed())
          {
            return;
          }

          component.setEnabled(false);
          view.openMedicationOrderingDialog();
          clearTimeout(self._orderButtonDoubleClickTimer);
          self._orderButtonDoubleClickTimer = setTimeout(
              function()
              {
                component.setEnabled(true);
              },
              self._preventDoubleClickTimeoutLength);
        }
      });

      if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() ||
          view.getTherapyAuthority().isPrescribeByTemplatesAllowed())
      {
        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          cls: 'order-therapy-menu-item',
          iconCls: 'icon-add',
          text: view.getDictionary('prescribe.medications'),
          handler: function()
          {
            orderButton.closePopupMenu();
            view.openMedicationOrderingDialog();
          }
        }));
      }

      if (view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() && this.withPreviousHospitalization)
      {
        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          cls: 'previous-host-therapies-menu-item',
          iconCls: 'icon-add-to-24',
          text: view.getDictionary('therapy.transfer'),
          handler: function()
          {
            orderButton.closePopupMenu();
            view.orderPreviousHospitalizationTherapies();
          }
        }));
      }

      if (view.getTherapyAuthority().isManageAllTemplatesAllowed())
      {
        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          cls: 'manage-inpatient-templates-menu-item',
          iconCls: 'icon-add',
          text: view.getDictionary('manage.therapy.inpatient.templates'),
          handler: function()
          {
            orderButton.closePopupMenu();
            var dialogBuilder = new app.views.medications.ordering.templates.ManageTemplatesDialogBuilder({
              view: view
            });

            dialogBuilder
                .configureForInpatient()
                .create()
                .show();
          }
        }));

        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          cls: 'manage-discharge-templates-menu-item',
          iconCls: 'icon-add',
          text: view.getDictionary('manage.therapy.discharge.templates'),
          handler: function()
          {
            orderButton.closePopupMenu();
            var dialogBuilder = new app.views.medications.ordering.templates.ManageTemplatesDialogBuilder({
              view: view
            });

            dialogBuilder
                .configureForOutpatient()
                .create()
                .show();
          }
        }));
      }

      if (view.getTherapyAuthority().isManageOutpatientPrescriptionsAllowed())
      {
        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          iconCls: 'icon-add-outpatient-prescription',
          text: view.getDictionary('outpatient.prescription'),
          handler: function()
          {
            orderButton.closePopupMenu();
            view.openOutpatientOrderingDialog();
          }
        }));
      }

      if (view.isMentalHealthReportEnabled() === true && view.getTherapyAuthority().isMedicationConsentT2T3Allowed())
      {
        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          iconCls: 'icon-add',
          text: 'T2',
          handler: function()
          {
            orderButton.closePopupMenu();
            view.openT2T3OrderingDialog(app.views.medications.TherapyEnums.mentalHealthDocumentType.T2);
          }
        }));
        orderPopupMenu.addMenuItem(new tm.jquery.MenuItem({
          iconCls: 'icon-add',
          text: 'T3',
          handler: function()
          {
            orderButton.closePopupMenu();
            view.openT2T3OrderingDialog(app.views.medications.TherapyEnums.mentalHealthDocumentType.T3);
          }
        }));
      }

      if (orderPopupMenu.hasMenuItems())
      {
        this.add(orderButton);
      }
    }

    if (this.withPreviousHospitalization && view.getTherapyAuthority().isTimelineViewEnabled() &&
        view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
    {
      this._previousHospitalizationTherapiesButton = new tm.jquery.Button({
        cls: 'previous-hosp-therapies-button btn-flat',
        iconCls: 'icon-add-to-24',
        hidden: true, /* visible based on patient hospitalization data, which is loaded later */
        tooltip: app.views.medications.MedicationUtils.createTooltip(view.getDictionary('therapy.transfer'), 'bottom', view),
        handler: function()
        {
          view.orderPreviousHospitalizationTherapies();
        }
      });
      this.add(this._previousHospitalizationTherapiesButton);
    }

    if (this.withReferenceWeight)
    {
      this.add(new tm.jquery.Button({
        cls: 'reference-weight-button btn-flat',
        iconCls: 'icon-reference-weight',
        tooltip: app.views.medications.MedicationUtils.createTooltip(view.getDictionary('reference.weight'), 'bottom', view),
        handler: function()
        {
          view.openReferenceWeightDialog();
        }
      }));
    }

    if (this.withReportPrinting && view.getTherapyAuthority().isTherapyReportEnabled())
    {
      var printPopupMenu = new tm.jquery.PopupMenu();

      printPopupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: 'print-report-menu-item',
        iconCls: 'icon-print',
        text: view.getDictionary('medication.administration.record.active'),
        handler: this._printActiveMedicationAdministrationRecord.bind(this)
      }));

      printPopupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: 'print-report-menu-item',
        iconCls: 'icon-print',
        text: view.getDictionary('medication.administration.record.report'),
        handler: function()
        {
          self._displayMedicationAdministrationHistoryReportRequestDataEntryTooltip(printMenuButton);
        }
      }));

      printPopupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: 'print-report-menu-item',
        iconCls: 'icon-print',
        text: view.getDictionary('medication.administration.record.empty'),
        handler: function()
        {
          self._displayMedicationAdministrationReportTemplate(printMenuButton);
        }
      }));

      printPopupMenu.addMenuItem(new tm.jquery.MenuItem({
        cls: 'print-surgery-report-menu-item',
        iconCls: 'icon-print',
        text: view.getDictionary('medication.administration.record.one.day'),
        handler: function()
        {
          self._printOneDayMedicationAdministrationRecord();
        }
      }));

      var printMenuButton = new tm.jquery.SplitButton({
        cls: 'print-button splitbutton-flat',
        iconCls: 'icon-print',
        popupMenu: printPopupMenu
      });

      printMenuButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, this._onPrintButtonClick.bind(this));

      this.add(printMenuButton);
    }

    if (this.withOutpatientPrescribing && view.isSwingApplication() &&
        view.getTherapyAuthority().isManageOutpatientPrescriptionsAllowed())
    {
      this.add(new tm.jquery.Button({
        cls: 'btn-flat',
        iconCls: 'icon-show-external-outpatient-prescriptions',
        tooltip: app.views.medications.MedicationUtils.createTooltip(
            view.getDictionary('get.outpatient.prescriptions.from.eer'),
            'bottom',
            view),
        handler: this._displayExternalOutpatientPrescriptionRequestDataEntryTooltip.bind(this)
      }));
    }

    if (this.withMedicationReconciliation && view.getTherapyAuthority().isManageMedicationOnAdmissionAllowed())
    {
      this.add(new tm.jquery.Button({
        cls: 'btn-flat',
        text: view.getDictionary('start.new.reconciliation'),
        iconCls: 'icon-start-new-reconciliation',
        tooltip: app.views.medications.MedicationUtils.createTooltip(
            view.getDictionary('start.new.reconciliation'),
            'bottom',
            view),
        handler: this._onStartNewReconciliationButtonClick.bind(this)
      }));

      this._lastReconciliationStartDateNotificationComponent = new tm.jquery.Component({
        cls: 'last-reconciliation-start-notification',
        hidden: true // becomes visible once there's data to show, to avoid consuming space
      });
      this.add(this._lastReconciliationStartDateNotificationComponent);
    }

    if (this.withMedicationBarcodeInput && view.getTherapyAuthority().isMedicationIdentifierScanningAllowed() &&
        view.getTherapyAuthority().isManageAdministrationsAllowed())
    {
      this._medicationBarcodeField = new tm.jquery.TextField({
        cls: 'medication-barcode-field',
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
        placeholder: view.getDictionary('barcode'),
        width: 120
      });
      this._medicationBarcodeField.onKey(new tm.jquery.event.KeyStroke({key: 'return'}),
          this._onMedicationBarcodeFieldReturnKeyPress.bind(this));
      this.add(this._medicationBarcodeField);
    }

    if (this.withWarningScan)
    {
      this.add(new tm.jquery.Button({
        cls: 'btn-flat',
        iconCls: 'icon-show-current-therapies-warnings',
        tooltip: app.views.medications.MedicationUtils.createTooltip(
            view.getDictionary('show.all.warnings'),
            'bottom',
            view),
        handler: this._onWarningScanButtonClick.bind(this)
      }));
    }
  },

  /**
   * Return key press event handler for {@link #_medicationBarcodeField}. Calls the Ajax method to retrieve the
   * administration tasks for the given barcode and fires the {@link #EVENT_TYPE_BARCODE_SCANNED} event if successful.
   * Disables the input field until the ajax call is processed.
   *
   * @param component
   * @private
   */
  _onMedicationBarcodeFieldReturnKeyPress: function(component)
  {
    var self = this;
    var barcode = component.getValue();
    if (barcode)
    {
      component.setEnabled(false);
      self.view
          .getRestApi()
          .getAdministrationTaskForBarcode(barcode)
          .then(
              function onSuccess(barcodeTaskSearch)
              {
                self._fireBarcodeScannedEvent(barcodeTaskSearch, barcode);
                component.setEnabled(true);
                component.setValue(null, true);
              },
              function onFailure()
              {
                component.setEnabled(true);
              }
          );
    }
  },

  _onPrintButtonClick: function()
  {
    this.view.getTherapyAuthority().isTherapyReportEnabled() ?
        this._printActiveMedicationAdministrationRecord() :
        this._printOneDayMedicationAdministrationRecord();
  },

  /**
   * Event handler for the warning scan button click. Loads any possible warnings form the server API and displays
   * a dialog with the results.
   * @private
   */
  _onWarningScanButtonClick: function()
  {
    var factory = new app.views.medications.warnings.CurrentTherapiesWarningsDialogFactory({
      view: this.view
    });
    factory.show();
  },

  /**
   * @param {Object} barcodeTaskSearch
   * @param {String} barcode
   * @private
   */
  _fireBarcodeScannedEvent: function(barcodeTaskSearch, barcode)
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.common.overview.header.WestToolbarContainer.EVENT_TYPE_BARCODE_SCANNED,
      eventData: {
        barcodeTaskSearch: barcodeTaskSearch,
        barcode: barcode
      }
    }), null);
  },

  _fireStartNewReconciliationEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.common.overview.header.WestToolbarContainer.EVENT_TYPE_START_NEW_RECONCILIATION
    }), null);
  },

  /**
   * Click event handler for the active MAR report print.
   * @private
   */
  _printActiveMedicationAdministrationRecord: function()
  {
    this.view
        .getRestApi()
        .downloadActiveMedicationAdministrationRecord();
  },

  /**
   * Click event handler for the one day MAR report print.
   * @private
   */
  _printOneDayMedicationAdministrationRecord: function()
  {
    this.view
        .getRestApi()
        .downloadOneDayMedicationAdministrationRecord();
  },

  _onStartNewReconciliationButtonClick: function()
  {
    this._fireStartNewReconciliationEvent();
  },

  /**
   * Displays data entry tooltip which enables viewing outpatient prescriptions from the external system.
   * @param {tm.jquery.Component} button
   * @private
   */
  _displayExternalOutpatientPrescriptionRequestDataEntryTooltip: function(button)
  {
    this._applyAndShowTooltip(
        button,
        this._dataEntryTooltipFactory.buildViewExternalOutpatientPrescriptionsDataEntryTooltip());
  },

  /**
   * Displays the date interval entry tooltip and triggers the report download once the user confirms the end date.
   * @param {tm.jquery.Component|*} button
   * @private
   */
  _displayMedicationAdministrationHistoryReportRequestDataEntryTooltip: function(button)
  {
    this._applyAndShowTooltip(
        button,
        this._dataEntryTooltipFactory.buildDownloadMedicationAdministrationHistoryReportDataEntryTooltip());
  },

  /**
   * Displays the number of pages entry tooltip and triggers the report download once the user confirms the input.
   * @param {tm.jquery.Component|*} button
   * @private
   */
  _displayMedicationAdministrationReportTemplate: function(button)
  {
    this._applyAndShowTooltip(button, this._dataEntryTooltipFactory.buildDownloadMedicationAdministrationReportTemplate());
  },

  /**
   * @param {tm.jquery.Component} component
   * @param {tm.jquery.Tooltip} popoverTooltip
   * @private
   */
  _applyAndShowTooltip: function(component, popoverTooltip)
  {
    component.setTooltip(popoverTooltip);

    setTimeout(function()
    {
      if (component.isRendered())
      {
        popoverTooltip.show();
      }
    }, 0);
  }
});
