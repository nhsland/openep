Class.define('app.views.medications.timeline.administration.SelfAdministrationDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls:'self-administration-container',
  /** configs */
  view: null,
  therapy: null,
  patientId: null,

  /** privates */
  validationForm: null,
  preselectedValue: null,
  selfAdminButtonGroup: null,
  stopAutomaticCharting: null,
  nurseCharting: null,
  autoCharting: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!tm.jquery.Utils.isEmpty(this.therapy) && !tm.jquery.Utils.isEmpty(this.therapy.selfAdministeringActionEnum))
    {
      this._setPreselectedValue(this.therapy.selfAdministeringActionEnum);
    }
    this._buildGui();
  },

  _buildGui: function()
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    this.autoCharting = new tm.jquery.Button({
      cls: "btn-bubble",
      text: self.view.getDictionary("self.administering.action." + enums.selfAdministeringActionEnum.AUTOMATICALLY_CHARTED),
      data: enums.selfAdministeringActionEnum.AUTOMATICALLY_CHARTED
    });

    this.nurseCharting = new tm.jquery.Button({
      cls: "btn-bubble",
      text: self.view.getDictionary("self.administering.action." + enums.selfAdministeringActionEnum.CHARTED_BY_NURSE),
      data: enums.selfAdministeringActionEnum.CHARTED_BY_NURSE
    });

    this.stopAutomaticCharting = new tm.jquery.Button({
      cls: "btn-bubble",
      text: self.view.getDictionary("self.administering.action." + enums.selfAdministeringActionEnum.STOP_SELF_ADMINISTERING),
      data: enums.selfAdministeringActionEnum.STOP_SELF_ADMINISTERING
    });

    var buttons = [this.autoCharting, this.nurseCharting];
    if (!tm.jquery.Utils.isEmpty(this.preselectedValue))
    {
      if (this.preselectedValue === enums.selfAdministeringActionEnum.AUTOMATICALLY_CHARTED)
      {
        buttons.push(this.stopAutomaticCharting);
        this.autoCharting.setPressed(true, true);
      }
      else if (this.preselectedValue === enums.selfAdministeringActionEnum.CHARTED_BY_NURSE)
      {
        buttons.push(this.stopAutomaticCharting);
        this.nurseCharting.setPressed(true, true);
      }
    }

    this.selfAdminButtonGroup = new tm.jquery.ButtonGroup({
      cls: "btn-group-bubble",
      buttons: buttons,
      orientation: 'horizontal',
      type: 'radio'
    });

    this.add(this.selfAdminButtonGroup);

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function ()
      {
        self._updateTherapySelfAdministeringType();
      },
      onValidationError: function ()
      {
        self.resultCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: this.view.getDictionary("field.value.is.required")
    });
  },

  _setPreselectedValue: function(value)
  {
    this.preselectedValue = value;
  },

  _getResult: function()
  {
    var selection = this.selfAdminButtonGroup.getSelection();
    return selection[0].data;
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();
    this.validationForm.addFormField(new tm.jquery.FormField({
      component: self.selfAdminButtonGroup,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self.selfAdminButtonGroup.getSelection();
        if (value == null || value.length <= 0)
        {
          return null;
        }
        return true;
      }
    }));
  },

  _updateTherapySelfAdministeringType: function()
  {
    var self = this;
    var selectedValue = this._getResult();

    if (!tm.jquery.Utils.isEmpty(this.preselectedValue) && this.preselectedValue === selectedValue)
    {
      var resultData = new app.views.common.AppResultData({success: true});
      self.resultCallback(resultData);
    }
    else
    {
      var patientId = this.patientId;
      var compositionUid = this.therapy.compositionUid;

      var params = {
        patientId: patientId,
        compositionUid: compositionUid,
        selfAdministeringActionType: selectedValue
      };

      var url = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_UPDATE_SELF_ADMINISTERING_STATUS;
      this.view.sendPostRequest(url, params, function()
      {
        var resultData = new app.views.common.AppResultData({success: true});
        self.resultCallback(resultData);
      }, function()
      {
        var resultData = new app.views.common.AppResultData({success: false});
        self.resultCallback(resultData);
      });
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit()
  }
});