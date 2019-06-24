Class.define('app.views.medications.ordering.SaveMedicationOrderContainerDisplayProvider', 'app.views.medications.common.therapy.TherapyContainerDisplayProvider', {
  showChangeHistory: false,
  showChangeReason: false,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getStatusClass: function(dto){
    var therapy = dto.therapy;
    var therapyEndTime = therapy.end ? new Date(therapy.end) : null;
    var therapyEnded = !!therapyEndTime && therapyEndTime.getTime() <= CurrentTime.get().getTime();

    if (dto instanceof app.views.medications.ordering.dto.SaveMedicationOrder)
    {

      var enums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
      switch (dto.getActionEnum())
      {
        case enums.ABORT:
          return "aborted";
        case enums.SUSPEND:
          return "suspended";
        case enums.SUSPEND_ADMISSION:
          return "suspended";
        case enums.EDIT:
          return "changed";
        default:
          return therapyEnded ? "ended" : "normal";
      }
    }
    else
      return therapyEnded ? "ended" : "normal";
  },

  getStatusIcon: function(dto)
  {
    if (dto instanceof app.views.medications.ordering.dto.SaveMedicationOrder)
    {
      var enums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
      switch (dto.getActionEnum())
      {
        case enums.ABORT:
          return "icon_aborted";
        case enums.SUSPEND:
          return "icon_suspended";
        case enums.SUSPEND_ADMISSION:
          return "icon_suspended";
        default:
          return null;
      }
    }
    return null;
  },

  getBigIconContainerHtml: function(dto)
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums.medicationOrderActionEnum;
    var appFactory = view.getAppFactory();

    var options = this.getBigIconContainerOptions(dto);
    if (dto instanceof app.views.medications.ordering.dto.SaveMedicationOrder && dto.getActionEnum() === enums.EDIT)
    {
      options.layers.push({hpos: "left", vpos: "top", cls: "icon_changed"});
    }
    return appFactory.createLayersContainerHtml(options);
  }
});