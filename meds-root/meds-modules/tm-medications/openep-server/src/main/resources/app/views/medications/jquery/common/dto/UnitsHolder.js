Class.define('app.views.medications.common.dto.UnitsHolder', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      var config = jQuery.extend(true, {}, jsonObject);
      for (var type in config.types)
      {
        if (config.types.hasOwnProperty(type))
        {
          config.types[type] = new app.views.medications.common.dto.MedicationUnitType(config.types[type]);
        }
      }
      return new app.views.medications.common.dto.UnitsHolder(config);
    },
    LIQUID_UNIT: "LIQUID_UNIT",
    MASS_UNIT: "MASS_UNIT",
    TIME_UNIT: "TIME_UNIT",
    SURFACE_UNIT: "SURFACE_UNIT"
  },
  types: null,
  unitsWithType: null,
  allUnits: null,

  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {Object}
   */
  getTypes: function()
  {
    return this.types;
  },

  /**
   * @param {Object} types
   */
  setTypes: function(types)
  {
    this.types = types;
  },

  /**
   * @returns {Object}
   */
  getUnitsWithType: function()
  {
    return this.unitsWithType;
  },

  /**
   * @param {Object} unitsWithType
   */
  setUnitsWithType: function(unitsWithType)
  {
    this.unitsWithType = unitsWithType;
  },

  /**
   * @returns {Array<String>}
   */
  getAllUnits: function()
  {
    return this.allUnits;
  },

  /**
   * @param {Array<String>} allUnits
   */
  setAllUnits: function(allUnits)
  {
    this.allUnits = allUnits
  },

  /**
   * @param {String} unit
   * @returns {boolean}
   */
  isUnitInLiquidGroup: function(unit)
  {
    return this._isUnitInGroup(unit, app.views.medications.common.dto.UnitsHolder.LIQUID_UNIT);
  },

  /**
   * @param {String} unit
   * @returns {boolean}
   */
  isUnitInMassGroup: function(unit)
  {
    return this._isUnitInGroup(unit, app.views.medications.common.dto.UnitsHolder.MASS_UNIT);
  },

  /**
   * @param {String} unit
   * @returns {boolean}
   */
  isUnitInTimeGroup: function(unit)
  {
    return this._isUnitInGroup(unit, app.views.medications.common.dto.UnitsHolder.TIME_UNIT);
  },

  /**
   * @param {String} unit
   * @returns {boolean}
   */
  isUnitInSurfaceGroup: function(unit)
  {
    return this._isUnitInGroup(unit, app.views.medications.common.dto.UnitsHolder.SURFACE_UNIT);
  },

  /**
   * @param {String} unit
   * @returns {app.views.medications.common.dto.MedicationUnitType|null}
   */
  findKnownUnitType: function(unit)
  {
    if (!tm.jquery.Utils.isEmpty(this.getUnitsWithType()[unit]))
    {
      return this.getTypes()[this.getUnitsWithType()[unit]];
    }
    return this.findKnownUnitByDisplayName(unit);
  },

  /**
   * @param {String} unitDisplayName
   * @returns {app.views.medications.common.dto.MedicationUnitType|null}
   */
  findKnownUnitByDisplayName: function(unitDisplayName)
  {
    var self = this;
    return this._findUnit(function(type)
    {
      return self.getTypes()[type].getDisplayName() === unitDisplayName;
    });
  },

  /**
   * @param {app.views.medications.TherapyEnums.knownUnitType|*} unitName
   * @returns {app.views.medications.common.dto.MedicationUnitType|null}
   */
  findKnownUnitByName: function(unitName)
  {
    var self = this;
    return this._findUnit(function(type)
    {
      return self.getTypes()[type].getName() === unitName;
    });
  },

  /**
   * Use to convert unknown units
   * @param {Number} value
   * @param {String} fromUnit
   * @param {String} toUnit
   * @returns {Number|null}
   */
  convertToUnit: function(value, fromUnit, toUnit)
  {
    if (tm.jquery.Utils.isEmpty(value) || (fromUnit && fromUnit === toUnit))
    {
      return value;
    }

    var from = this.findKnownUnitType(fromUnit);
    var to = this.findKnownUnitType(toUnit);

    return this._convert(value, from, to);
  },

  /**
   * Only use when converting from unknown unit to known unit type (app.views.medications.TherapyEnums.knownUnitType)
   * @param {Number} value
   * @param {String} fromUnit
   * @param {app.views.medications.TherapyEnums.knownUnitType|*} toUnit
   * @returns {Number|null}
   */
  convertToKnownUnit: function(value, fromUnit, toUnit)
  {
    if (tm.jquery.Utils.isEmpty(value) || (fromUnit && fromUnit === toUnit))
    {
      return value;
    }

    var from = this.findKnownUnitType(fromUnit);
    var to = this.findKnownUnitByName(toUnit);

    return this._convert(value, from, to);
  },

  /**
   * Only use when converting from known unit type (app.views.medications.TherapyEnums.knownUnitType) to unknown unit
   * @param {Number} value
   * @param {app.views.medications.TherapyEnums.knownUnitType|*} fromUnit
   * @param {String} toUnit
   * @returns {Number|null}
   */
  convertFromKnownUnit: function(value, fromUnit, toUnit)
  {
    if (tm.jquery.Utils.isEmpty(value) || (fromUnit && fromUnit === toUnit))
    {
      return value;
    }

    var from = this.findKnownUnitByName(fromUnit);
    var to = this.findKnownUnitType(toUnit);

    return this._convert(value, from, to);
  },

  /**
   * Only use when converting from known unit type (app.views.medications.TherapyEnums.knownUnitType) to
   * known unit (app.views.medications.TherapyEnums.knownUnitType)
   * @param {Number} value
   * @param {app.views.medications.TherapyEnums.knownUnitType|*} fromUnit
   * @param {app.views.medications.TherapyEnums.knownUnitType|*} toUnit
   * @returns {Number|null}
   */
  convertKnownUnits: function(value, fromUnit, toUnit)
  {
    if (tm.jquery.Utils.isEmpty(value) || (fromUnit && fromUnit === toUnit))
    {
      return value;
    }

    var from = this.findKnownUnitByName(fromUnit);
    var to = this.findKnownUnitByName(toUnit);

    return this._convert(value, from, to);
  },

  /**
   * @param {Number} value
   * @param {app.views.medications.common.dto.MedicationUnitType} from
   * @param {app.views.medications.common.dto.MedicationUnitType} to
   * @returns {Number|null}
   * @private
   */
  _convert: function(value, from, to)
  {
    if (tm.jquery.Utils.isEmpty(value))
    {
      return value;
    }

    if (from && to)
    {
      if (from.getGroup() === to.getGroup())
      {
        return value * from.getFactor() / to.getFactor();
      }
    }
    return null;
  },

  /**
   * @param {String} unit
   * @param {app.views.medications.TherapyEnums.unitGroup|*} group
   * @returns {boolean}
   * @private
   */
  _isUnitInGroup: function(unit, group)
  {
    if (this.getUnitsWithType().hasOwnProperty(unit) && this.getTypes().hasOwnProperty(this.getUnitsWithType()[unit]))
    {
      return this.getTypes()[this.getUnitsWithType()[unit]].getGroup() === group;
    }
    else if (this.findKnownUnitByName(unit))
    {
      return this.findKnownUnitByName(unit).getGroup() === group;
    }
    return false;
  },

  /**
   * @param {function} predicate
   * @returns {app.views.medications.common.dto.MedicationUnitType|null}
   * @private
   */
  _findUnit: function(predicate)
  {
    for (var type in this.getTypes())
    {
      if (predicate(type))
      {
        return this.getTypes()[type];
      }
    }
    return null;
  }
});