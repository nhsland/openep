Class.define('app.views.medications.ordering.HighRiskMedicationIconsContainer', 'tm.jquery.Container', {
  cls: 'high-alert-medication-icons-container',

  /** configs */
  view: null,

  /** privates: components */
  _blackTriangleIconContainer: null,
  _trialMedicationIconContainer: null,
  _controlledDrugIconContainer: null,
  _criticalDrugIconContainer: null,
  _highAlertIconContainer: null,
  _nonFormularyIconContainer: null,
  _unlicensedMedicationIconContainer: null,
  _expensiveDrugIconContainer: null,
  /** @type app.views.medications.ordering.TherapyIconContainerFactory */
  _iconContainerFactory: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._iconContainerFactory = new app.views.medications.ordering.TherapyIconContainerFactory({
      view: this.getView()
    });
    this._buildGui();
  },

  /** private methods */
  _buildGui: function()
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    var propertyTypeEnum = app.views.medications.TherapyEnums.medicationPropertyType;
    var iconContainerFactory = this._iconContainerFactory;

    this._blackTriangleIconContainer = iconContainerFactory.createPropertyTypeIconContainer(
        propertyTypeEnum.BLACK_TRIANGLE_MEDICATION, true, true);
    this._trialMedicationIconContainer = iconContainerFactory.createPropertyTypeIconContainer(
        propertyTypeEnum.CLINICAL_TRIAL_MEDICATION, true, true);
    this._controlledDrugIconContainer = iconContainerFactory.createPropertyTypeIconContainer(
        propertyTypeEnum.CONTROLLED_DRUG, true, true);
    this._criticalDrugIconContainer = iconContainerFactory.createPropertyTypeIconContainer(
        propertyTypeEnum.CRITICAL_DRUG, true, true);
    this._highAlertIconContainer = iconContainerFactory.createPropertyTypeIconContainer(
        propertyTypeEnum.HIGH_ALERT_MEDICATION, true, true);
    this._unlicensedMedicationIconContainer = iconContainerFactory.createPropertyTypeIconContainer(
        propertyTypeEnum.UNLICENSED_MEDICATION, true, true);
    this._expensiveDrugIconContainer = iconContainerFactory.createPropertyTypeIconContainer(
        propertyTypeEnum.EXPENSIVE_DRUG, true, true);
    this._nonFormularyIconContainer = iconContainerFactory.createIconContainer(
        'high-risk-icon non_formulary_icon',
        view.getDictionary('non.formulary.medication'),
        true);

    this.add(this._blackTriangleIconContainer);
    this.add(this._trialMedicationIconContainer);
    this.add(this._controlledDrugIconContainer);
    this.add(this._criticalDrugIconContainer);
    this.add(this._highAlertIconContainer);
    this.add(this._unlicensedMedicationIconContainer);
    this.add(this._nonFormularyIconContainer);
    this.add(this._expensiveDrugIconContainer);
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   */
  presentHighAlertIcons: function(medicationData)
  {
    if (!medicationData) return;

    if (medicationData.isControlledDrug())
    {
      this._controlledDrugIconContainer.show();
    }
    if (medicationData.isCriticalDrug())
    {
      this._criticalDrugIconContainer.show();
    }
    if (medicationData.isClinicalTrialMedication())
    {
      this._trialMedicationIconContainer.show();
    }
    if (medicationData.isUnlicensedMedication())
    {
      this._unlicensedMedicationIconContainer.show();
    }
    if (medicationData.isHighAlertMedication())
    {
      this._highAlertIconContainer.show();
    }
    if (medicationData.isBlackTriangleMedication())
    {
      this._blackTriangleIconContainer.show();
    }
    if (this.getView().isFormularyFilterEnabled() && !medicationData.isFormulary())
    {
      this._nonFormularyIconContainer.show();
    }
    if (medicationData.isExpensiveDrug())
    {
      this._expensiveDrugIconContainer.show();
    }
  },

  /**
   * Creates a new instance of {@Link app.views.medications.common.dto.MedicationData} that only holds
   * {@Link MedicationData#properties} and {@Link MedicationData#formulary} values to be passed to
   * {@Link #presentHighAlertIcons}
   * @param {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData} medicationData
   */
  presentHighAlertIconsForMultipleMedicationData: function(medicationData)
  {
    var self = this;
    var medicationDataArray = tm.jquery.Utils.isArray(medicationData) ? medicationData : [medicationData];
    var mockedMedicationData = new app.views.medications.common.dto.MedicationData({
      //Default to true, will be overridden if we find a non-formulary medicationData
      formulary: true
    });

    var highAlertPropertyTypes = [
      app.views.medications.TherapyEnums.medicationPropertyType.CONTROLLED_DRUG,
      app.views.medications.TherapyEnums.medicationPropertyType.CRITICAL_DRUG,
      app.views.medications.TherapyEnums.medicationPropertyType.CLINICAL_TRIAL_MEDICATION,
      app.views.medications.TherapyEnums.medicationPropertyType.UNLICENSED_MEDICATION,
      app.views.medications.TherapyEnums.medicationPropertyType.HIGH_ALERT_MEDICATION,
      app.views.medications.TherapyEnums.medicationPropertyType.BLACK_TRIANGLE_MEDICATION,
      app.views.medications.TherapyEnums.medicationPropertyType.EXPENSIVE_DRUG
    ];
    var uniquePropertiesMap = new tm.jquery.HashMap();

    medicationDataArray
        .forEach(function(medicationData)
        {
          medicationData
              .getProperties()
              .filter(function relevantTypeAndMissing(property)
              {
                return highAlertPropertyTypes.indexOf(property.getType()) > -1 &&
                    !uniquePropertiesMap.containsKey(property.getType());
              })
              .forEach(function map(property)
              {
                uniquePropertiesMap.put(
                    property.getType(),
                    new app.views.medications.common.dto.MedicationProperty({
                      type: property.getType(),
                      name: property.getName(),
                      value: property.getValue()
                    }));
              });

          if (self.getView().isFormularyFilterEnabled())
          {
            mockedMedicationData.setFormulary(mockedMedicationData.isFormulary() && medicationData.isFormulary())
          }
        });

    mockedMedicationData.setProperties(uniquePropertiesMap.values());
    this.presentHighAlertIcons(mockedMedicationData);
  },

  clear: function()
  {
    this._controlledDrugIconContainer.hide();
    this._criticalDrugIconContainer.hide();
    this._trialMedicationIconContainer.hide();
    this._unlicensedMedicationIconContainer.hide();
    this._highAlertIconContainer.hide();
    this._blackTriangleIconContainer.hide();
    this._nonFormularyIconContainer.hide();
    this._expensiveDrugIconContainer.hide();
  },

  /**
   * @returns {tm.jquery.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});