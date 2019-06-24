Class.define('app.views.medications.warnings.dto.MedicationsWarning', 'tm.jquery.Object', {

  description: null,
  severity: null,
  externalType: null,
  type: null,
  externalSeverity: null,
  monographHtml: null,
  medications: null,

  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject))
      {
        return null;
      }
      var config = jQuery.extend(true, {}, jsonObject);
      return new app.views.medications.warnings.dto.MedicationsWarning(config);
    }
  },
  /**
   * @param {Object} config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.medications = tm.jquery.Utils.isArray(this.medications ) ? this.medications : [];
  },

  /**
   *
   * @returns {String|null}
   */
  getDescription: function()
  {
    return this.description;
  },
  /**
   * @returns {app.views.medications.TherapyEnums.warningSeverityEnum|null}
   */
  getSeverity: function()
  {
    return this.severity;
  },
  /**
   * @returns {app.views.medications.TherapyEnums.warningType|null}
   */
  getType: function()
  {
    return this.type;
  },

  /**
   * @returns {String|null}
   */
  getExternalType: function()
  {
    return this.externalType;
  },

  /**
   * @returns {String|null}
   */
  getExternalSeverity: function()
  {
    return this.externalSeverity;
  },

  /**
   * @returns {String|null}
   */
  getMonographHtml: function()
  {
    return this.monographHtml;
  },
  /**
   * @returns {Array<{name: String, id: String}>}
   */
  getMedications: function()
  {
    return this.medications;
  },

  /**
   * @param {String|null} value
   */
  setDescription: function(value)
  {
    this.description = value;
  },
  /**
   * @param {app.views.medications.TherapyEnums.warningSeverityEnum|null} value
   */
  setSeverity: function(value)
  {
    return this.severity = value;
  },
  /**
   * @param {app.views.medications.TherapyEnums.warningType|null} value
   */
  setType: function(value)
  {
    this.type = value;
  },

  /**
   * @param {String} externalSeverity
   */
  setExternalSeverity: function(externalSeverity)
  {
    this.externalSeverity = externalSeverity;
  },

  /**
   * @param {String|null} value
   */
  setMonographHtml: function(value)
  {
    this.monographHtml = value;
  },
  /**
   * @param {Array<{name: String, id: String}>} value
   */
  setMedications: function(value)
  {
    this.medications = value;
  },

  /**
   * @returns {string}
   */
  getFormattedDescription: function()
  {
    var formattedWarning = "";
    if (this.getExternalType())
    {
      formattedWarning += '<strong>' + tm.jquery.Utils.escapeHtml(this.getExternalType()) + '</strong>';
    }
    if (this.getExternalSeverity())
    {
      formattedWarning +=
          '<strong>' +
          "(" + tm.jquery.Utils.escapeHtml(this.getExternalSeverity()) + ")" +
          '</strong>';
    }
    if (this.getExternalType() || this.getExternalSeverity())
    {
      formattedWarning += ": ";
    }
    formattedWarning += tm.jquery.Utils.escapeHtml(this.getDescription());

    return formattedWarning
  },

  /**
   * @returns {String|null}
   */
  getFormattedWarningDescription: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var formattedWarningDescription = this.getFormattedDescription();
    if (!tm.jquery.Utils.isEmpty(formattedWarningDescription) &&
        formattedWarningDescription.length > 170 && !tm.jquery.Utils.isEmpty(this.getMonographHtml()) &&
        (this.getSeverity() !== enums.warningSeverityEnum.HIGH &&
            this.getSeverity() !== enums.warningSeverityEnum.HIGH_OVERRIDE))
    {
      formattedWarningDescription = formattedWarningDescription.substring(0, 170);
      formattedWarningDescription += " ...";
    }
    for (var i = 0; i < this.getMedications().length; i++)
    {
      var medicationForWarningDto = this.getMedications()[i];
      // escape the name in case of universal medication order and XSS prevention
      formattedWarningDescription = formattedWarningDescription.replaceAll(medicationForWarningDto.name,
          '<strong>' + tm.jquery.Utils.escapeHtml(medicationForWarningDto.name) + '</strong>', null, true);
    }
    return formattedWarningDescription;
  },

  /**
   * Returns true if any medication on the list of warning's medications has the same id as the provided medication
   * @param {app.views.medications.common.dto.Medication} medication
   * @returns {boolean}
   */
  hasMatchingMedication: function(medication)
  {
    return this.getMedications().some(function(warningMedication)
    {
      // noinspection EqualityComparisonWithCoercionJS warningMedication is a named identity whose id is a string
      return medication.getId() == warningMedication.id
    });
  }
});
