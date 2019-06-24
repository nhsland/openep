Class.define('app.views.medications.common.overview.ReferenceWeightDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "reference-weight-pane",
  /** configs */
  view: null,
  weight: null,
  /** privates */
  resultCallback: null,
  validationForm: null,
  /** privates: components */
  weightField: null,
  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    var self = this;
    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'reference-weight-pane-coordinator',
      view: this.view,
      component: this
    });

    this.setLayout(new tm.jquery.HFlexboxLayout({gap: 5}));
    this._buildComponents();
    this._buildGui();
    this.weightField.setValue(this.weight);

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self.weightField.focus();
        $(self.weightField.getDom()).select();
      }, 0);
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.weightField = app.views.medications.MedicationUtils.createNumberField('n3', 90);

    this.validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._saveReferenceWeight();
      },
      onValidationError: function()
      {
        self.resultCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: self.view.getDictionary("field.value.is.required")
    });
  },

  _buildGui: function()
  {
    this.add(this.weightField);
    this.add(app.views.medications.MedicationUtils.crateLabel(
        'TextData',
        this.getView().getUnitsHolder().findKnownUnitByName(
            app.views.medications.TherapyEnums.knownUnitType.KG).getDisplayName()));
  },

  _setupValidation: function()
  {
    var self = this;
    this.validationForm.reset();
    this.validationForm.addFormField(new tm.jquery.FormField({
      component: self.weightField,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self.weightField.getValue();
        if (value == null || value <= 0)
        {
          return null;
        }
        return true;
      }
    }));
  },

  _saveReferenceWeight: function()
  {
    var self = this;

    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SAVE_REFERENCE_WEIGHT_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var referenceWeight = this.weightField.getValue();
    var saveUrl = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_REFERENCE_WEIGHT;
    var params = {
      patientId: self.view.getPatientId(),
      weight: referenceWeight
    };

    this.view.loadPostViewData(saveUrl, params, null,
        function()
        {
          var resultData = new app.views.common.AppResultData({success: true, value: referenceWeight});
          self.resultCallback(resultData);
          viewHubNotifier.actionEnded(hubAction);
        },
        function()
        {
          var resultData = new app.views.common.AppResultData({success: false});
          self.resultCallback(resultData);
          viewHubNotifier.actionFailed(hubAction);
        },
        true);
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    this._setupValidation();
    this.validationForm.submit()
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});

