Class.define('app.views.medications.ordering.UniversalMedicationDataContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: 'universal-medication-data-container',
  view: null,
  /** privates */
  _nameField: null,
  _doseFormCombo: null,
  _medicationDiluentRadioBtnGroup: null,
  _universalStrengthContainer: null,
  _strengthContainerComponent: null,
  _testCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;
    this._testCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'universal-medication-data-container-coordinator',
      view: this.getView(),
      component: this,
      manualMode: true /* issues with focus that fires after the test continues */
    });

    this._buildGui();
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self.getView().getAppFactory().createConditionTask(
          function()
          {
            self._nameField.focus();
            self._testCoordinator.insertCoordinator();
          },
          function(task)
          {
            if (!self.isRendered())
            {
              task.abort()
            }
            return self.isRendered(true) && $(self._nameField.getDom()).isVisible();
          },
          500, 50
      );
    });
  },

  _buildGui: function()
  {
    var self = this;
    var appFactory = this.getView().getAppFactory();
    var enums = app.views.medications.TherapyEnums;
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"));

    var medicationBtn = new tm.jquery.RadioButton({
      testAttribute: 'is-medication-button',
      labelText: this.getView().getDictionary("MedicationTypeEnum.MEDICATION"),
      labelAlign: "right",
      data: enums.medicationTypeEnum.MEDICATION,
      checked: true
    });

    var diluentBtn = new tm.jquery.RadioButton({
      testAttribute: 'is-diluent-button',
      labelText: this.getView().getDictionary("MedicationTypeEnum.DILUENT"),
      labelAlign: "right",
      data: enums.medicationTypeEnum.DILUENT
    });

    this._medicationDiluentRadioBtnGroup = new tm.jquery.RadioButtonGroup({
      cls: "medication-diluent-radio-btn-group",
      groupName: "horizontal-radiobutton-group",
      radioButtons: [medicationBtn, diluentBtn],
      onChange: this._onMedicationDiluentBtnChange.bind(this)
    });

    var nameLabel = new tm.jquery.Container({
      cls: "TextLabel universal-medication-label",
      html: this.getView().getDictionary("name")
    });

    this._nameField = new tm.jquery.TextField({
      testAttribute: 'name-input-field',
      cls: 'universal-medication-name-field',
      width: 450
    });

    this._nameField.onKey(new tm.jquery.event.KeyStroke({key: "tab"}), function()
    {
      self._doseFormCombo.focus();
    });

    var doseFormLabel = new tm.jquery.Container({
      cls: "TextLabel universal-medication-label",
      html: this.getView().getDictionary("dose.form")
    });

    this._doseFormCombo = new tm.jquery.TypeaheadField({
      testAttribute: 'dose-form-typeaheadfield',
      cls: "dose-form-combo",
      displayProvider: function(doseForm)
      {
        return doseForm.getName();
      },
      minLength: 1,
      width: 450,
      items: 10000
    });

    this._doseFormCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, this._onDoseFormChange.bind(this));

    this._doseFormCombo.setSource(this.getView().getDoseForms());

    this._medicationDiluentRadioBtnGroupContainer =
        appFactory.createHRadioButtonGroupContainer(this._medicationDiluentRadioBtnGroup);
    this.add(this._medicationDiluentRadioBtnGroupContainer);
    this.add(nameLabel);
    this.add(this._nameField);
    this.add(doseFormLabel);
    this.add(this._doseFormCombo);
    this._universalStrengthContainer = new app.views.medications.ordering.UniversalStrengthContainer({
      view: this.getView(),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start")
    });
    this._strengthContainerComponent = new app.views.medications.common.VerticallyTitledComponent({
      titleText: this.getView().getDictionary('strength'),
      contentComponent: this._universalStrengthContainer
    });
    this.add(this._strengthContainerComponent)
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData}
   * @private
   */
  _buildMedicationData: function()
  {
    var medication = new app.views.medications.common.dto.Medication({
      id: null,
      name: this._nameField.getValue(),
      medicationType: this._medicationDiluentRadioBtnGroup.getActiveRadioButton().data
    });

    return app.views.medications.common.dto.MedicationData.fromJson({
      doseForm: this._doseFormCombo.getSelection(),
      medication: medication,
      prescribingDose: this._buildPrescribingDose()
    });
  },

  /**
   * @returns {app.views.medications.common.dto.PrescribingDose}
   * @private
   */
  _buildPrescribingDose: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var view = this.getView();

    if (this._strengthContainerComponent.isHidden() &&
        this._medicationDiluentRadioBtnGroup.getActiveRadioButton().data === enums.medicationTypeEnum.DILUENT)
    {
      return new app.views.medications.common.dto.PrescribingDose({
        numeratorUnit: view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.ML).getDisplayName()
      });
    }
    return this._universalStrengthContainer.getPrescribingDose()
  },

  _onMedicationDiluentBtnChange: function()
  {
    var enums = app.views.medications.TherapyEnums;
    if (this._medicationDiluentRadioBtnGroup.getActiveRadioButton().getData() === enums.medicationTypeEnum.DILUENT)
    {
      this._strengthContainerComponent.hide();
    }
    else
    {
      this._strengthContainerComponent.show();
    }
  },

  /**
   * Hide {@link #_strengthContainerComponent} if selected doseForm's {@link #medicationOrderFormType} is descriptive,
   * otherwise let {@link #_universalStrengthContainer} handle required fields and preselected units
   * @private
   */
  _onDoseFormChange: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var isMedication = this._medicationDiluentRadioBtnGroup.getActiveRadioButton().getData() ===
        enums.medicationTypeEnum.MEDICATION;

    if (!!this._doseFormCombo.getSelection() && isMedication)
    {
      var medicationOrderFormType = this._doseFormCombo.getSelection().getMedicationOrderFormType();

      if (medicationOrderFormType === enums.medicationOrderFormType.DESCRIPTIVE)
      {
        this._strengthContainerComponent.hide();
      }
      else
      {
        this._strengthContainerComponent.show();

        medicationOrderFormType === enums.medicationOrderFormType.COMPLEX ?
            this._universalStrengthContainer.adjustForComplexOrderForm() :
            this._universalStrengthContainer.adjustForSimpleOrderForm();
      }
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    var validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        var medicationData = self._buildMedicationData();
        resultDataCallback(new app.views.common.AppResultData({success: true, value: medicationData}));
      },
      onValidationError: function()
      {
        resultDataCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: self.getView().getDictionary("field.value.is.required")
    });
    validationForm.reset();
    validationForm.addFormField(new tm.jquery.FormField({
      component: self._nameField,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self._nameField.getValue();
        if (value == null || value == "")
        {
          return null;
        }
        return true;
      }
    }));
    validationForm.addFormField(new tm.jquery.FormField({
      component: self._doseFormCombo,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self._doseFormCombo.getSelection();
        if (value == null)
        {
          return null;
        }
        return true;
      }
    }));
    validationForm.addFormField(new tm.jquery.FormField({
      component: self._medicationDiluentRadioBtnGroupContainer,
      required: true,
      componentValueImplementationFn: function()
      {
        var diluent =
            self._medicationDiluentRadioBtnGroup.getActiveRadioButton().data === enums.medicationTypeEnum.DILUENT;
        var simpleDoseFormType = self._doseFormCombo.getSelection() &&
            self._doseFormCombo.getSelection().medicationOrderFormType === enums.medicationOrderFormType.SIMPLE;
        if (diluent && simpleDoseFormType)
        {
          return null;
        }
        return true;
      }
    }));

    if (!this._strengthContainerComponent.isHidden())
    {
      this._universalStrengthContainer.getStrengthValidators().forEach(function addValidators(validator)
      {
        validationForm.addFormField(validator);
      })
    }

    validationForm.submit();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});
