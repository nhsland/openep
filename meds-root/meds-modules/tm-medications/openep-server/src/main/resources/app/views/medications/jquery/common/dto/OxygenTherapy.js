Class.define('app.views.medications.common.dto.OxygenTherapy', 'app.views.medications.common.dto.Therapy', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      config.start = tm.jquery.Utils.isEmpty(jsonObject.start) ? null : new Date(jsonObject.start);
      config.end = tm.jquery.Utils.isEmpty(jsonObject.end) ? null : new Date(jsonObject.end);
      config.createdTimestamp = tm.jquery.Utils.isEmpty(jsonObject.createdTimestamp) ? null :
          new Date(jsonObject.createdTimestamp);
      config.medication = !tm.jquery.Utils.isEmpty(jsonObject.medication) ?
          new app.views.medications.common.dto.Medication(jsonObject.medication) :
          null;
      config.informationSources = tm.jquery.Utils.isArray(jsonObject.informationSources) ?
          jsonObject.informationSources.map(function toObject(source)
          {
            return new app.views.medications.common.dto.InformationSource(source);
          }) :
          [];
      config.dispenseDetails = !!jsonObject.dispenseDetails ?
          app.views.medications.common.dto.DispenseDetails.fromJson(jsonObject.dispenseDetails) :
          null;
      config.startingDevice = !!jsonObject.startingDevice ?
          new app.views.medications.common.dto.OxygenStartingDevice(jsonObject.startingDevice) :
          null;

      return new app.views.medications.common.dto.OxygenTherapy(config);
    }
  },
  flowRate: null,
  flowRateUnit: null,
  flowRateMode: null,
  startingDevice: null,
  minTargetSaturation: null,
  maxTargetSaturation: null,
  humidification: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!this.flowRateUnit)
    {
      this.flowRateUnit = 'l/min';
      /* default, so it's not absent anywhere */
    }
    this.setMedicationOrderFormType(app.views.medications.TherapyEnums.medicationOrderFormType.OXYGEN);
  },

  /**
   * @param {Number|null] value
   */
  setFlowRate: function(value)
  {
    this.flowRate = value;
  },
  setFlowRateUnit: function(value)
  {
    this.flowRateUnit = value;
  },
  setFlowRateMode: function(value)
  {
    this.flowRateMode = value;
  },
  /**
   * @param {app.views.medications.common.dto.OxygenStartingDevice|null} value
   */
  setStartingDevice: function(value)
  {
    this.startingDevice = value;
  },
  setMinTargetSaturation: function(value)
  {
    this.minTargetSaturation = value;
  },
  setMaxTargetSaturation: function(value)
  {
    this.maxTargetSaturation = value;
  },
  setHumidification: function(value)
  {
    this.humidification = value;
  },

  getFlowRate: function()
  {
    return this.flowRate;
  },
  getFlowRateUnit: function()
  {
    return this.flowRateUnit;
  },
  getFlowRateMode: function()
  {
    return this.flowRateMode;
  },
  /**
   * @returns {app.views.medications.common.dto.OxygenStartingDevice|null}
   */
  getStartingDevice: function()
  {
    return this.startingDevice;
  },
  getMinTargetSaturation: function()
  {
    return this.minTargetSaturation;
  },
  getMaxTargetSaturation: function()
  {
    return this.maxTargetSaturation;
  },
  /**
   * @returns {boolean}
   */
  isHumidification: function()
  {
    return this.humidification === true;
  },

  /**
   * @returns {boolean}
   */
  isHighFlowOxygen: function()
  {
    return this.flowRateMode === app.views.medications.TherapyEnums.flowRateMode.HIGH_FLOW;
  },

  /**
   * Override, anything other is impossible anyway.
   * @returns {boolean}
   */
  isOrderTypeOxygen: function()
  {
    return true;
  },

  /**
   * Override
   * @returns {boolean}
   */
  isTherapyWithDurationAdministrations: function()
  {
    return true;
  },

  /**
   * @override
   * @param {boolean} [deep=true]
   * @returns {app.views.medications.common.dto.OxygenTherapy}
   */
  clone: function(deep)
  {
    return deep !== false ?
        jQuery.extend(true, new app.views.medications.common.dto.OxygenTherapy(), this) :
        jQuery.extend(new app.views.medications.common.dto.OxygenTherapy(), this);
  }
});
