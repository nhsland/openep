Class.define('app.views.medications.reconciliation.MedicationOnDischargeTherapyContainerDisplayProvider', 'app.views.medications.common.therapy.TherapyContainerDisplayProvider', {
  showChangeHistory: false,
  showChangeReason: true,

  /**
   * This jsClass extends the default {@Łink app.views.medications.common.therapy.TherapyContainerDisplayProvider} with
   * support for {@link app.views.medications.reconciliation.dto.MedicationOnDischarge#getTherapyStatus} which are
   * based on a different set of possible values.
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getStatusClass: function(dto)
  {
    if (!(dto instanceof app.views.medications.reconciliation.dto.MedicationOnDischarge))
    {
      return this.callSuper(dto);
    }

    var therapy = dto.therapy;
    var enums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;

    switch (dto.getStatus())
    {
      case enums.NOT_PRESCRIBED:
        return "aborted";
      case enums.EDITED_AND_PRESCRIBED:
        return "changed";
      default:
        return "normal";
    }
  },

  getStatusIcon: function(dto)
  {
    if (!(dto instanceof app.views.medications.reconciliation.dto.MedicationOnDischarge))
    {
      return this.callSuper(dto);
    }
    var enums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;
    switch (dto.getStatus())
    {
      case enums.NOT_PRESCRIBED:
        return "icon_aborted";
      default:
        return null;
    }
  },

  getBigIconContainerHtml: function(dto)
  {
    if (!(dto instanceof app.views.medications.reconciliation.dto.MedicationOnDischarge))
    {
      return this.callSuper(dto);
    }

    var view = this.getView();
    var enums = app.views.medications.TherapyEnums.medicationOnDischargeStatus;
    var appFactory = view.getAppFactory();

    var options = this.getBigIconContainerOptions(dto);
    if (dto.getStatus() === enums.EDITED_AND_PRESCRIBED)
    {
      options.layers.push({hpos: "right", vpos: "bottom", cls: "icon_changed"});
    }
    if (dto.getTherapy().isLinkedToAdmission())
    {
      options.layers.push({hpos: "left", vpos: "center", cls: "icon_linked_to_admission"});
    }
    return appFactory.createLayersContainerHtml(options);
  }
});