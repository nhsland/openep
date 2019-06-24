Class.define('app.views.medications.common.dto.TherapyChange', 'tm.jquery.Object', {
  statics: {
    fromJson: function (jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject))
      {
        return null;
      }

      var config = jQuery.extend(true, {}, jsonObject);

      if (app.views.medications.common.dto.TherapyChange._VARIABLE_DOSE_CHANGE_COLLECTION.indexOf(config.type) > -1)
      {
        config.oldValue = convertVariableDoseDaysStringDatesToDate(jsonObject.oldValue);
        config.newValue = convertVariableDoseDaysStringDatesToDate(jsonObject.newValue);
      }

      return new app.views.medications.common.dto.TherapyChange(config);

      function convertVariableDoseDaysStringDatesToDate(value)
      {
        return tm.jquery.Utils.isArray(value) ?
            value.map(
                function stringDateToVariableDoseDate(element)
                {
                  if (!!element.date)
                  {
                    // noinspection JSCheckFunctionSignatures
                    element.date = new Date(element.date);
                  }
                  return element;
                }) :
            value
      }
    },
    /**
     * A private static list of possible change type enumerations related to variable dose change.
     * @private */
    _VARIABLE_DOSE_CHANGE_COLLECTION: [
      app.views.medications.TherapyEnums.therapyChangeTypeEnum.VARIABLE_DOSE,
      app.views.medications.TherapyEnums.therapyChangeTypeEnum.VARIABLE_DOSE_TO_DOSE,
      app.views.medications.TherapyEnums.therapyChangeTypeEnum.DOSE_TO_VARIABLE_DOSE
    ],
    /**
     * A private static list of possible change type enumerations related to variable rate change.
     * @private */
    _VARIABLE_RATE_CHANGE_COLLECTION: [
      app.views.medications.TherapyEnums.therapyChangeTypeEnum.VARIABLE_RATE,
      app.views.medications.TherapyEnums.therapyChangeTypeEnum.VARIABLE_RATE_TO_RATE,
      app.views.medications.TherapyEnums.therapyChangeTypeEnum.RATE_TO_VARIABLE_RATE
    ]
  },
  type: null, /* String */
  newValue: null, /* String|Object|null */
  oldValue: null, /* String|Object|null */
  
  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * 
   * @returns {String}
   */
  getType: function()
  {
    return this.type;
  },

  /**
   * @param {String} type
   */
  setType: function(type)
  {
    this.type = type;
  },
  
  /**
   * @returns {String|Object|null}
   */
  getNewValue: function()
  {
    return this.newValue;
  },
  
  /**
   * @param {String|Object|null} newValue
   */
  setNewValue: function(newValue)
  {
    this.newValue = newValue;
  },
  
  /**
   * @returns {String|Object|null}
   */
  getOldValue: function()
  {
    return this.oldValue;
  },
  
  /**
   * @param {String|Object|null} oldValue
   */
  setOldValue: function(oldValue)
  {
    this.oldValue = oldValue;
  },

  /**
   * @returns {boolean}
   */
  isChangeTypeVariableDose: function()
  {
    return app.views.medications.common.dto.TherapyChange._VARIABLE_DOSE_CHANGE_COLLECTION.indexOf(this.getType()) > -1;
  },

  /**
   * @returns {boolean}
   */
  isChangeTypeVariableRate: function()
  {
    return app.views.medications.common.dto.TherapyChange._VARIABLE_RATE_CHANGE_COLLECTION.indexOf(this.getType()) > -1;
  }
});