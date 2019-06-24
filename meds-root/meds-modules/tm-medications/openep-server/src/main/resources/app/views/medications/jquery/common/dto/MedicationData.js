Class.define('app.views.medications.common.dto.MedicationData', 'tm.jquery.Object', {
  statics: {
    /**
     * Use this method for special conversion from json (dates etc...)
     * @param jsonObject
     * @returns {object}
     */
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;
      var config = jQuery.extend(true, {}, jsonObject);
      config.medication = !tm.jquery.Utils.isEmpty(jsonObject.medication) ?
          new app.views.medications.common.dto.Medication(jsonObject.medication) :
          null;
      config.doseForm = !tm.jquery.Utils.isEmpty(jsonObject.doseForm) ?
          new app.views.medications.common.dto.DoseForm(jsonObject.doseForm) :
          null;
      if (tm.jquery.Utils.isArray(jsonObject.properties))
      {
        config.properties = jsonObject.properties.map(function(property)
        {
          return new app.views.medications.common.dto.MedicationProperty(property);
        });
      }
      if (tm.jquery.Utils.isArray(jsonObject.medicationIngredients))
      {
        config.medicationIngredients = jsonObject.medicationIngredients.map(function(ingredient)
        {
          return new app.views.medications.common.dto.MedicationIngredient(ingredient);
        });
      }
      if (!tm.jquery.Utils.isEmpty(jsonObject.prescribingDose))
      {
        config.prescribingDose = new app.views.medications.common.dto.PrescribingDose(jsonObject.prescribingDose)
      }
      if (tm.jquery.Utils.isArray(jsonObject.routes))
      {
        config.routes = jsonObject.routes.map(function(route)
        {
          return new app.views.medications.common.dto.MedicationRoute(route);
        });
      }
      if (!tm.jquery.Utils.isEmpty(jsonObject.defaultRoute))
      {
        config.defaultRoute = new app.views.medications.common.dto.MedicationRoute(jsonObject.defaultRoute);
      }
      config.medicationDocuments = tm.jquery.Utils.isArray(jsonObject.medicationDocuments) ?
          jsonObject.medicationDocuments.map(function toDocument(value)
          {
            return new app.views.medications.common.dto.MedicationDocument(value)
          }) :
          [];
      return new app.views.medications.common.dto.MedicationData(config);
    }
  },
  vtmId: null, /* Long */
  vmpId: null, /* Long */
  ampId: null, /* Long */
  validFrom: null, /* DateTime */
  validTo: null, /* DateTime */
  atcGroupCode: null, /* String */
  atcGroupName: null, /* String */
  medicationLevel: null, /* String */
  medication: null, /* MedicationDto */
  orderable: null, /* boolean */
  defaultRoute: null, /* MedicationRouteDto */
  doseForm: null, /* DoseFormDto */
  administrationUnit: null, /* String */
  administrationUnitFactor: null, /* Number */
  supplyUnit: null, /* String */
  supplyUnitFactor: null, /* Double */
  properties: null, /* MedicationPropertyDto[] */
  inpatientMedication: null, /* boolean */
  outpatientMedication: null, /* boolean */
  formulary: null, /* boolean */
  formularyCareProviders: null, /* String[] */
  medicationIngredients: null, /* MedicationIngredientDto[] */
  routes: null, /* MedicationRouteDto[] */
  medicationDocuments: null, /* MedicationDocumentDto[] */
  indications: null, /* IndicationDto[] */
  titration: null, /* String enum from TitrationType */
  roundingFactor: null, /* Double */
  medicationPackaging: null, /* String */
  prescribingDose: null, /* PrescribingDose */
  descriptiveDose: null, /* boolean */

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.medicationDocuments = tm.jquery.Utils.isArray(this.medicationDocuments) ? this.medicationDocuments : [];
  },

  /**
   * @param {String} medicationLevel
   */
  setMedicationLevel: function(medicationLevel)
  {
    this.medicationLevel = medicationLevel;
  },

  /**
   * @returns {String} medicationLevel
   */
  getMedicationLevel: function()
  {
    return this.medicationLevel;
  },

  /**
   * @param {String[]} formularyCareProviders
   */
  setFormularyCareProviders: function(formularyCareProviders)
  {
    this.formularyCareProviders = formularyCareProviders;
  },

  /**
   * @returns {String[]} formularyCareProviders
   */
  getFormularyCareProviders: function()
  {
    return this.formularyCareProviders;
  },

  /**
   * @param {String} atcGroupName
   */
  setAtcGroupName: function(atcGroupName)
  {
    this.atcGroupName = atcGroupName;
  },

  /**
   * @returns {String} atcGroupName
   */
  getAtcGroupName: function()
  {
    return this.atcGroupName;
  },

  /**
   * @param {String} atcGroupCode
   */
  setAtcGroupCode: function(atcGroupCode)
  {
    this.atcGroupCode = atcGroupCode;
  },

  /**
   * @returns {String} atcGroupCode
   */
  getAtcGroupCode: function()
  {
    return this.atcGroupCode;
  },

  /**
   * @param {Date} validFrom
   */
  setValidFrom: function(validFrom)
  {
    this.validFrom = validFrom;
  },

  /**
   * @returns {Date} validFrom
   */
  getValidFrom: function()
  {
    return this.validFrom;
  },

  /**
   * @param {Date} validTo
   */
  setValidTo: function(validTo)
  {
    this.validTo = validTo;
  },

  /**
   * @returns {Date} validTo
   */
  getValidTo: function()
  {
    return this.validTo;
  },

  /**
   * @param {Number} vtmId
   */
  setVtmId: function(vtmId)
  {
    this.vtmId = vtmId;
  },

  /**
   * @returns {Number} vtmId
   */
  getVtmId: function()
  {
    return this.vtmId;
  },

  /**
   * @param {Number} vmpId
   */
  setVmpId: function(vmpId)
  {
    this.vmpId = vmpId;
  },

  /**
   * @returns {Number} vmpId
   */
  getVmpId: function()
  {
    return this.vmpId;
  },
  /**
   * @param {Number} ampId
   */
  setAmpId: function(ampId)
  {
    this.ampId = ampId;
  },

  /**
   * @returns {Number} ampId
   */
  getAmpId: function()
  {
    return this.ampId;
  },

  /**
   * @param {app.views.medications.common.dto.Medication|null} medication
   */
  setMedication: function(medication)
  {
    this.medication = medication;
  },

  /**
   * @returns {app.views.medications.common.dto.Medication|null}
   */
  getMedication: function()
  {
    return this.medication;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationRoute} defaultRoute
   */
  setDefaultRoute: function(defaultRoute)
  {
    this.defaultRoute = defaultRoute;
  },
  /**
   * @returns {app.views.medications.common.dto.MedicationRoute}
   */
  getDefaultRoute: function()
  {
    return this.defaultRoute;
  },
  /**
   * @param {app.views.medications.common.dto.DoseForm|null} doseForm
   */
  setDoseForm: function(doseForm)
  {
    this.doseForm = doseForm;
  },

  /**
   * @returns {app.views.medications.common.dto.DoseForm|null}
   */
  getDoseForm: function()
  {
    return this.doseForm;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationProperty[]|null} properties
   */
  setProperties: function(properties)
  {
    this.properties = properties;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationProperty[]}
   */
  getProperties: function()
  {
    return this.properties ? this.properties : [];
  },

  /**
   * @param {String|null} supplyUnit
   */
  setSupplyUnit: function(supplyUnit)
  {
    this.supplyUnit = supplyUnit;
  },

  /**
   * @returns {String|null}
   */
  getSupplyUnit: function()
  {
    return this.supplyUnit;
  },

  /**
   * @param {Number} supplyUnitFactor
   */
  setSupplyUnitFactor: function(supplyUnitFactor)
  {
    this.supplyUnitFactor = supplyUnitFactor;
  },

  /**
   * @returns {Number}
   */
  getSupplyUnitFactor: function()
  {
    return this.supplyUnitFactor;
  },

  /**
   * @param {String|null} administrationUnit
   */
  setAdministrationUnit: function(administrationUnit)
  {
    this.administrationUnit = administrationUnit;
  },

  /**
   * @returns {String|null}
   */
  getAdministrationUnit: function()
  {
    return this.administrationUnit;
  },

  /**
   * @param {Number} administrationUnitFactor
   */
  setAdministrationUnitFactor: function(administrationUnitFactor)
  {
    this.administrationUnitFactor = administrationUnitFactor;
  },

  /**
   * @returns {Number}
   */
  getAdministrationUnitFactor: function()
  {
    return this.administrationUnitFactor;
  },

  /**
   * @param {boolean} value
   */
  setInpatientMedication: function(value)
  {
    this.inpatientMedication = value;
  },
  /**
   * @param {boolean} value
   */
  setOutpatientMedication: function(value)
  {
    this.outpatientMedication = value;
  },

  /**
   * @returns {boolean}
   */
  isInpatientMedication: function()
  {
    return this.inpatientMedication === true;
  },

  /**
   * @returns {boolean}
   */
  isOrderable: function()
  {
    return this.orderable === true;
  },

  /**
   * @returns {boolean}
   */
  isOutpatientMedication: function()
  {
    return this.outpatientMedication === true;
  },

  /**
   * @returns {boolean}
   */
  isFormulary: function()
  {
    return this.formulary === true;
  },

  /**
   * @returns {boolean}
   */
  setFormulary: function(formulary)
  {
    this.formulary = formulary;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationIngredient[]|null} medicationIngredients
   */
  setMedicationIngredients: function(medicationIngredients)
  {
    this.medicationIngredients = medicationIngredients;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationIngredient[]|null}
   */
  getMedicationIngredients: function()
  {
    return this.medicationIngredients;
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationRoute>|null} routes
   */
  setRoutes: function(routes)
  {
    this.routes = routes;
  },

  /**
   * @returns {Array<app.views.medications.common.dto.MedicationRoute>|null}
   */
  getRoutes: function()
  {
    return this.routes;
  },

  /**
   * @returns {Number}
   */
  getRoundingFactor: function()
  {
    return this.roundingFactor;
  },

  /**
   * @param {Number} value
   */
  setRoundingFactor: function(value)
  {
    this.roundingFactor = value;
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationDocument>} medicationDocuments
   */
  setMedicationDocuments: function(medicationDocuments)
  {
    this.medicationDocuments = medicationDocuments;
  },

  /**
   * @returns {Array<app.views.medications.common.dto.MedicationDocument>}
   */
  getMedicationDocuments: function()
  {
    return this.medicationDocuments;
  },

  /**
   * @param {Array} indications
   */
  setIndications: function(indications)
  {
    this.indications = indications;
  },

  /**
   * @returns {Array}
   */
  getIndications: function()
  {
    return this.indications;
  },

  /**
   * @returns {String|null} of {@link app.views.medications.TherapyEnums.therapyTitrationTypeEnum}
   */
  getTitration: function()
  {
    return this.titration;
  },

  /**
   * @return {boolean} true, if this medication's dose can be titrated based on targeted INR.
   */
  isInrBasedTitrationSupported: function()
  {
    return this.titration === app.views.medications.TherapyEnums.therapyTitrationTypeEnum.INR;
  },

  /**
   * @returns {String|null}
   */
  getMedicationPackaging: function()
  {
    return this.medicationPackaging;
  },

  /**
   * @param {String|null} value
   */
  setMedicationPackaging: function(value)
  {
    this.medicationPackaging = value;
  },

  /**
   * @returns {boolean}
   */
  isDoseFormDescriptive: function()
  {
    return this.isDescriptiveDose() ||
        (this.getDoseForm() && this.getDoseForm().getMedicationOrderFormType() ===
            app.views.medications.TherapyEnums.medicationOrderFormType.DESCRIPTIVE);
  },

  /**
   * @returns {app.views.medications.common.dto.PrescribingDose|null}
   */
  getDefiningIngredient: function()
  {
    if (this.getAdministrationUnit())
    {
      return this._buildPrescribingDoseForAdministrationUnit();
    }
    return this.getPrescribingDose();

  },

  /**
   * @returns {String|null}
   */
  getStrengthNumeratorUnit: function()
  {
    if (this.getAdministrationUnit())
    {
      return this.getAdministrationUnit();
    }
    return !tm.jquery.Utils.isEmpty(this.getPrescribingDose()) ? this.getPrescribingDose().getNumeratorUnit() : null;
  },

  /**
   * @returns {String|null}
   */
  getStrengthDenominatorUnit: function()
  {
    if (this.getAdministrationUnit())
    {
      return null;
    }
    var prescribingDose = this.getPrescribingDose();
    if (!tm.jquery.Utils.isEmpty(prescribingDose) && !tm.jquery.Utils.isEmpty(prescribingDose.getDenominatorUnit()))
    {
      return prescribingDose.getDenominatorUnit();
    }
    return null;
  },

  /**
   * @returns {boolean}
   */
  isControlledDrug: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.CONTROLLED_DRUG);
  },

  /**
   * @return {boolean}
   */
  isCriticalDrug: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.CRITICAL_DRUG);
  },

  /**
   * @returns {boolean}
   */
  isBlackTriangleMedication: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.BLACK_TRIANGLE_MEDICATION);
  },

  /**
   * @returns {boolean}
   */
  isUnlicensedMedication: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.UNLICENSED_MEDICATION);
  },

  /**
   * @returns {boolean}
   */
  isHighAlertMedication: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.HIGH_ALERT_MEDICATION);
  },

  /**
   * @returns {boolean}
   */
  isClinicalTrialMedication: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.CLINICAL_TRIAL_MEDICATION);
  },

  /**
   * @returns {boolean}
   */
  isExpensiveDrug: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.EXPENSIVE_DRUG);
  },

  /**
   * @returns {boolean}
   */
  isReviewReminder: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.REVIEW_REMINDER);
  },

  /**
   * @returns {boolean}
   */
  isAntibiotic: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.ANTIBIOTIC);
  },

  /**
   * @returns {boolean}
   */
  isMentalHealthDrug: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.MENTAL_HEALTH_DRUG);
  },

  /**
   * @returns {boolean}
   */
  isSuggestSwitchToOral: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.SUGGEST_SWITCH_TO_ORAL);
  },

  /**
   * @returns {boolean} True, if the medication's characteristic indicate it's delivery is delayed after administration.
   */
  isModifiedRelease: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.MODIFIED_RELEASE);
  },

  /**
   * @return {boolean} True, if the modified release characteristic of the prescription are configurable. Such a decision
   * can only be made on the VTM level, provided the medication contains the required property, marking it as a
   * possible modified release prescription. {@see #isModifiedRelease}.
   */
  isModifiedReleaseConfigurable: function()
  {
    return this.isVtm() && this.isModifiedRelease();
  },

  /**
   * @returns {boolean} True, if the medication is designed to temporarily withstand attack by stomach acid.
   */
  isGastroResistant: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.GASTRO_RESISTANT);
  },

  /**
   * @returns {boolean} True, if the gastro resistant characteristics of the prescription is configurable by the user. Such a
   * decision can be made for VTM level, when the medicine contains the required property, marking it as possible
   * gastro resistant prescription. {@see #isGastroResistant}.
   */
  isGastroResistantConfigurable: function()
  {
    return this.isVtm() && this.isGastroResistant();
  },

  /**
   * @returns {boolean} True, if the medication data represents a VTM level medication.
   */
  isVtm: function()
  {
    return this.getMedicationLevel() === app.views.medications.TherapyEnums.medicationLevelEnum.VTM;
  },

  /**
   * @returns {boolean} True, if the medication data represents a VMP level medication.
   */
  isVmp: function()
  {
    return this.getMedicationLevel() === app.views.medications.TherapyEnums.medicationLevelEnum.VMP;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationProperty}
   */
  getMedicationPricingDetails: function()
  {
    return this._filterPropertiesByType(app.views.medications.TherapyEnums.medicationPropertyType.PRICE)[0];
  },

  /**
   * @returns {Boolean}
   */
  hasMedicationPrice: function()
  {
    return this._isPropertyTypePresent(app.views.medications.TherapyEnums.medicationPropertyType.PRICE);
  },

  /**
   * @param {app.views.medications.common.dto.PrescribingDose} prescribingDose
   */
  setPrescribingDose: function(prescribingDose)
  {
    this.prescribingDose = prescribingDose;
  },

  /**
   * @returns {app.views.medications.common.dto.PrescribingDose|null}
   */
  getPrescribingDose: function()
  {
    return this.prescribingDose;
  },

  /**
   * @returns {Boolean}
   */
  isDescriptiveDose: function()
  {
    return this.descriptiveDose;
  },

  /**
   * @param {Boolean} value
   */
  setDescriptiveDose: function(value)
  {
    this.descriptiveDose = value;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationProperty[]} the list of all modified release duration properties.
   */
  getModifiedReleaseDurationProperties: function()
  {
    return this._filterPropertiesByType(app.views.medications.TherapyEnums.medicationPropertyType.MODIFIED_RELEASE_TIME);
  },

  /**
   * @returns {boolean} true, if the modified release duration can be configured. Returns false if there are no
   * available properties or if the medication level is AMP, where the characteristics are predefined.
   */
  isModifiedReleaseDurationConfigurable: function()
  {
    return (this.isVtm() || this.isVmp()) && this.getModifiedReleaseDurationProperties().length > 0;
  },

  /**
   * Check if routes on medication data contain the provided route, and if said route has max dose defined.
   * @param {app.views.medications.common.dto.MedicationRoute} route
   * @returns {boolean}
   */
  hasMatchingRouteWithMaxDose: function(route)
  {
    return tm.jquery.Utils.isArray(this.getRoutes()) && this.getRoutes().some(function(medicationDataRoute)
    {
      return medicationDataRoute.getId() === route.getId() && !!medicationDataRoute.getMaxDose();
    })
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @returns {boolean}
   */
  hasMatchingNumeratorUnit: function(medicationData)
  {
    return this.getStrengthNumeratorUnit() === medicationData.getStrengthNumeratorUnit();
  },

  /**
   * Cloning capability - until we figure out there's an issue with this solution and we revert to using constructors.
   * Preserves the correct constructor, so instanceof checks work. Override in any extended classes.
   *
   * Does not (yet) clone child objects which aren't plane objects (such as instances of
   * app.views.medications.common.dto.DoseForm). If such need arises, we need to manually clone them before returning.
   *
   * @params {boolean} [deep=true]
   * @returns {app.views.medications.common.dto.MedicationData}
   */
  clone: function(deep)
  {
    return deep !== false ?
        jQuery.extend(true, app.views.medications.common.dto.MedicationData() , this) :
        jQuery.extend(new app.views.medications.common.dto.MedicationData(), this);
  },

  /**
   * In some instances, medicationData is missing vital information, causing errors either on GUI or server side.
   * @returns {boolean} true, if medicationData has at least one route and at least the numerator unit. If this
   * information is missing, we should prevent the user from selecting this medication.
   */
  isValid: function()
  {
    return this._areRoutesValid() && this._areUnitsValid();
  },

  /**
   * If administration unit exists, it, and only it, must be used to describe medication.
   * @returns {app.views.medications.common.dto.PrescribingDose}
   */
  _buildPrescribingDoseForAdministrationUnit: function()
  {
    return new app.views.medications.common.dto.PrescribingDose({
      numerator: null,
      numeratorUnit: this.getAdministrationUnit(),
      denominator: null,
      denominatorUnit: null
    });
  },

  /**
   * @param {string} type {@see app.views.medications.TherapyEnums.medicationPropertyType}
   * @returns {boolean}
   * @private
   */
  _isPropertyTypePresent: function(type)
  {
    return this.getProperties().some(function(property)
    {
      return property.getType() === type;
    });
  },

  /**
   * @param {string} type {@see app.views.medications.TherapyEnums.medicationPropertyType}
   * @returns {app.views.medications.common.dto.MedicationProperty[]}
   * @private
   */
  _filterPropertiesByType: function(type)
  {
    return this.getProperties().filter(function byType(property) {
      return property.getType() === type;
    })
  },

  /**
   * At least the numerator unit is required for medicationData units to be valid, unless dose form is descriptive.
   * @returns {boolean}
   * @private
   */
  _areUnitsValid: function()
  {
    return this.isDoseFormDescriptive() || !!this.getStrengthNumeratorUnit();
  },

  /**
   * At least one route is required for medicationData routes to be valid, unless medication is universal
   * @returns {boolean}
   * @private
   */
  _areRoutesValid: function()
  {
    return this.getMedication().isMedicationUniversal() ||
        tm.jquery.Utils.isArray(this.getRoutes()) && this.getRoutes().length > 0;
  }
});
