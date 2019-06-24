Class.define('app.views.medications.timeline.TherapyRow', 'app.views.medications.grid.dto.TherapyDay', {
  statics: {
    fromJson: function(jsonObject){
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.therapy = app.views.medications.common.TherapyJsonConverter.convert(config.therapy);
      config.medicationProperties = config.medicationProperties.map(
          function(property)
          {
            return new app.views.medications.common.dto.MedicationProperty(property);
          });

      config.administrations = jsonObject.administrations.map(
          function(administration)
          {
            return app.views.medications.timeline.administration.dto.Administration.fromJson(administration)
          });

      return new app.views.medications.timeline.TherapyRow(config);
    }
  },
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  /** @type Array<app.views.medications.timeline.administration.dto.Administration> */
  administrations: null,
  /** @type Array<app.views.medications.common.dto.MedicationProperty> */
  medicationProperties: null,
  /** @type boolean */
  containsNonFormularyMedications: false,
  /** @type string of {@link app.views.medications.TherapyEnums.prescriptionGroupEnum} */
  prescriptionGroup: app.views.medications.TherapyEnums.prescriptionGroupEnum.REGULAR,
  /** @type string|null */
  atcGroupName: null,
  /** @type string */
  atcGroupCode: '',
  /** @type string|null */
  customGroup: null,
  /** @type number|null */
  customGroupSortOrder: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.medicationProperties = tm.jquery.Utils.isArray(this.medicationProperties) ? this.medicationProperties : [];
  },

  /**
   * @returns {Array<app.views.medications.timeline.administration.dto.Administration>}
   */
  getAdministrations: function()
  {
    return this.administrations;
  },

  /**
   * @returns {Array<app.views.medications.common.dto.MedicationProperty>}
   */
  getMedicationProperties: function()
  {
    return this.medicationProperties;
  },

  /**
   * @return {string} of {@link app.views.medications.TherapyEnums.prescriptionGroupEnum}
   */
  getPrescriptionGroup: function()
  {
    return this.prescriptionGroup;
  },

  /**
   * @returns {string|null}
   */
  getAtcGroupName: function()
  {
    return this.atcGroupName;
  },

  /**
   * @returns {string}
   */
  getAtcGroupCode: function()
  {
    return this.atcGroupCode;
  },

  /**
   * @returns {string|null}
   */
  getCustomGroup: function()
  {
    return this.customGroup
  },

  /**
   * @returns {number|null}
   */
  getCustomGroupSortOrder: function()
  {
    return this.customGroupSortOrder;
  },

  /**
   * @returns {boolean}
   */
  hasNonFormularyMedications: function()
  {
    return this.containsNonFormularyMedications === true;
  },

  /**
   * @returns {boolean}
   */
  isTherapyActive: function()
  {
    var therapyStatus = this.getTherapyStatus();
    var therapyStatusEnum = app.views.medications.TherapyEnums.therapyStatusEnum;
    return therapyStatus !== therapyStatusEnum.ABORTED &&
        therapyStatus !== therapyStatusEnum.CANCELLED &&
        therapyStatus !== therapyStatusEnum.SUSPENDED
  },

  /**
   * Cloning capability - until we figure out there's an issue with this solution and we revert to using constructors.
   * Preserves the correct constructor, so instanceof checks work. Override in any extended classes.
   *
   * Does not (yet) clone child objects which aren't plane objects (such as instances of
   * app.views.medications.common.dto.Medication and  app.views.medications.common.dto.DoseForm). If such need arises,
   * we need to manually clone them before returning.
   *
   * @params {boolean} [deep=true]
   * @returns {app.views.medications.common.dto.Therapy}
   */
  clone: function(deep)
  {
    return deep !== false ?
        jQuery.extend(true, new app.views.medications.timeline.TherapyRow(), this, { therapy: this.getTherapy().clone()}) :
        jQuery.extend(new app.views.medications.timeline.TherapyRow(), this, { therapy: this.getTherapy().clone()});
  }
});