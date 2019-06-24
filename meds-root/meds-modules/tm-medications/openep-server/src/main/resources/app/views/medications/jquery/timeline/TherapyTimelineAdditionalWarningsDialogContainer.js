Class.define('app.views.medications.timeline.TherapyTimelineAdditionalWarningsDialogContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "therapy-timeline-container additional-warnings-container",
  layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 0),

  view: null,
  additionalWarnings: null,

  _form: null,
  _warningRowDataList: null,
  _therapyDisplayProvider: null,

  /**
   * Returns an instance of a popup dialog container that is displayed to the user in case the backend determines that there
   * are potential risks with active therapies (due to allergies or with mental health drugs). Such a scenario might
   * happen in cases when the allergy or mental health information is entered after a therapy was already prescribed. The
   * problematic therapies are displayed to the user as rows, each containing a list of related warnings. The user must
   * either abort the problematic therapy or enter an override reason for the presented warning.
   * @constructor
   */
  Constructor: function()
  {
    this.callSuper();

    var view = this.getView();

    this._warningRowDataList = [];
    this._therapyDisplayProvider = new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
      view: view
    });

    this._form = new tm.jquery.Form({
      view: view,
      showTooltips: false,
      requiredFieldValidatorErrorMessage: view.getDictionary("field.value.is.required")
    });

    this._buildGUI();
  },

  /**
   * Submits the form that validates that the user entered an override reason for all therapies that he hasn't aborted. If
   * validation succeeds, a list of aborted therapy IDs is created, alongside a list of override descriptions for each active
   * therapy. Keep in mind a single therapy may have multiple warnings, and the user can only enter one reason. The entered
   * reason is merged with each individual warning description into a collection of override descriptions, belonging to
   * a specific therapy ID, which is then sent to the backend server.
   * @param resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    var self = this;
    var form = this._form;
    var view = this.getView();

    form.setOnValidationSuccess(function onWarningsFormValidationSuccess()
    {
      var overrideWarnings = self._warningRowDataList
          .filter(function isNotAbortedTherapy(warningRowData)
          {
            return !warningRowData.isAborted();
          })
          .map(function createTherapyWarningsOverrides(warningRowData)
          {
            var overrideDescriptions =
                warningRowData
                    .getWarnings()
                    .map(function createTherapyWarningOverrideDescription(warning)
                    {
                      return app.views.medications.warnings.WarningsHelpers.createOverriddenWarningString(
                          view,
                          warningRowData.getReason(),
                          warning.description
                      )
                    });
            return {therapyId: warningRowData.getTherapy().getTherapyId(), warnings: overrideDescriptions};
          });

      var abortTherapyIds = self._warningRowDataList
          .filter(function isAbortedTherapy(warningRowData)
          {
            return warningRowData.isAborted();
          })
          .map(function abortedWarningRowDataToTherapyId(warningRowData)
          {
            return warningRowData.getTherapy().getTherapyId();
          });

      view.getRestApi()
          .handleAdditionalWarningsAction(abortTherapyIds, overrideWarnings, self.getAdditionalWarnings().getTaskIds())
          .then(
              function onHandleWarningsActionSuccess()
              {
                resultDataCallback(new app.views.common.AppResultData({success: true}));
              },
              function onHandleWarningsActionFailure()
              {
                resultDataCallback(new app.views.common.AppResultData({success: false}))
              });
    });

    form.setOnValidationError(function onWarningsFormValidationFailure()
    {
      resultDataCallback(new app.views.common.AppResultData({success: false}));
    });
    form.submit();
  },

  _buildGUI: function()
  {
    var headerContainer = this._createHeaderContainer();
    var warningsContainer = this._createWarningsContainer();

    this.add(headerContainer);
    this.add(warningsContainer);
  },

  _createHeaderContainer: function()
  {
    var view = this.getView();

    var additionalWarningsHeader = new tm.jquery.Container({
      cls: "additional-warnings-header",
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 10)
    });

    var warningImage = new tm.jquery.Image({cls: "additional_warnings", width: 48, height: 48});

    var headerDescription = '';

    headerDescription += '<p class="TextDataBold">';
    headerDescription += view.getDictionary("therapy.risk.warning.dialog.title");
    headerDescription += '</p>';
    headerDescription += '<p></p>';
    headerDescription += '<p class="TextData">';
    headerDescription += view.getDictionary("therapy.risk.warning.dialog.description");
    headerDescription += '</p>';

    var descriptionContainer = new tm.jquery.Container({flex: 1, html: headerDescription});

    additionalWarningsHeader.add(warningImage);
    additionalWarningsHeader.add(descriptionContainer);

    return additionalWarningsHeader;
  },

  _createWarningsContainer: function()
  {
    var self = this;
    var additionalWarnings = self.getAdditionalWarnings();

    var container = new tm.jquery.Container({
      cls: "warnings-container",
      scrollable: "vertical",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("start", "stretch", 20)
    });

    additionalWarnings.getWarnings()
        .filter(function(additionalWarning)
        {
          return additionalWarning.warnings.length;
        })
        .forEach(function(additionalWarning)
        {
          var therapy = additionalWarning.therapy;
          var warnings = additionalWarning.warnings;
          var warningRowData = new app.views.medications.timeline.AdditionalWarningRowData();

          warningRowData.setTherapy(therapy);
          warnings.forEach(function(warning)
          {
            warningRowData.addWarning(warning.warning);
          });
          self._warningRowDataList.add(warningRowData);

          var warningContainer = self._createWarningContainer(warningRowData, function()
          {
            self._performAbortAction(warningRowData);
          });
          warningRowData.setContainer(warningContainer);
          container.add(warningContainer);
        });
    return container;
  },

  _createWarningContainer: function(warningRowData, abortCallback)
  {
    var view = this.getView();
    var self = this;
    var appFactory = view.getAppFactory();

    var rowWarnings = warningRowData.getWarnings();
    var rowTherapy = warningRowData.getTherapy();

    var therapyIcon = new tm.jquery.Container({
      width: 48, height: 48, cls: "therapy-icon", html: appFactory.createLayersContainerHtml({
            background: {cls: this._therapyDisplayProvider.getTherapyIcon(rowTherapy)},
            layers: [
              {hpos: "right", vpos: "bottom", cls: "status-icon"}
            ]
          }
      )
    });

    var html = '';
    html += '<p class="TextData">';
    html += rowTherapy.formattedTherapyDisplay;
    html += '</p>';

    var detailTherapyContainer = new tm.jquery.Container({html: html});

    var detailWarningContainers = rowWarnings.map(function(warning)
    {

      var detailTherapyContainerWarningIcon = new tm.jquery.Image({
        cls: "high_alert_medication_icon",
        width: 24,
        height: 24
      });

      var detailTherapyWarningInfoContainer = new tm.jquery.Container({
        flex: 1, html: tm.jquery.Utils.escapeHtml(warning.description)
      });

      var detailWarningContainer = new tm.jquery.Container({
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 10)
      });

      detailWarningContainer.add(detailTherapyContainerWarningIcon);
      detailWarningContainer.add(detailTherapyWarningInfoContainer);

      return detailWarningContainer;
    });

    var detailContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 10)
    });

    detailContainer.add(detailTherapyContainer);
    detailWarningContainers.forEach(function(detailWarningContainer)
    {
      var detailCommentContainer = new tm.jquery.Container({
        cls: "comment-container", layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 10)
      });
      var commentLabel = new app.views.medications.MedicationUtils.crateLabel(
          "TextDataLight uppercase", view.getDictionary("reason") + ":", 0);
      var commentTextField = new tm.jquery.TextField({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        cls: "field-flat"
      });

      self._form.addFormField(new tm.jquery.FormField({
        component: commentTextField,
        required: true,
        validation: {
          type: "local"
        },
        componentValueImplementationFn: function()
        {
          return warningRowData.isAborted() ? true : commentTextField.getValue();
        }
      }));
      commentTextField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        warningRowData.setReason(commentTextField.getValue().trim());
      });

      detailCommentContainer.add(commentLabel);
      detailCommentContainer.add(commentTextField);

      detailContainer.add(detailWarningContainer);
      detailContainer.add(detailCommentContainer);
    });

    var deleteIcon = new tm.jquery.Image({
      cls: "icon-delete", cursor: "pointer", width: 24, height: 24
    });

    var container = new tm.jquery.Container({layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 10), html: html});

    container.add(therapyIcon);
    container.add(detailContainer);
    container.add(deleteIcon);

    deleteIcon.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (warningRowData.isAborted() === false)
      {
        abortCallback();
      }
    });

    return container;
  },

  _performAbortAction: function(warningRowData)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var rowContainer = warningRowData.getContainer();
    var rowTherapy = warningRowData.getTherapy();

    var warningImage = new tm.jquery.Image({cls: "action_warning", width: 48, height: 48});

    var html = '';
    html += '<p class="TextDataBold">';
    html += view.getDictionary("therapy.stopping");
    html += '</p>';
    html += '<p class="TextData">';
    html += view.getDictionary("stop.therapy.confirm.msg");
    html += '</p>';

    var descriptionContainer = new tm.jquery.Container({flex: 1, html: html});

    var container = new tm.jquery.Container({
      cls: "abort-action-container",
      layout: tm.jquery.HFlexboxLayout.create("start", "stretch", 10)
    });
    container.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      var message = $(component.getRenderToElement()).html();
      var confirmSystemDialog = appFactory.createConfirmSystemDialog(message, function(confirmed)
      {
        if (confirmed === true)
        {
          var $rowContainerDom = $(rowContainer.getDom());
          if (rowTherapy.getStart() > CurrentTime.get())
          {
            $rowContainerDom.find(".status-icon").addClass("icon_cancelled");
          }
          else
          {
            $rowContainerDom.find(".status-icon").addClass("icon_aborted");
          }

          $rowContainerDom.find(".tm-textfield").removeClass("form-field-validationError");
          rowContainer.setEnabled(false, true);
          rowContainer.addCls("disabled");

          tm.jquery.ComponentUtils.setElementOpacity($rowContainerDom, 0.5);

          warningRowData.setAborted(true);
        }
      });
      confirmSystemDialog.setWidth(320);
      confirmSystemDialog.setHeight(180);
      confirmSystemDialog.show();
    });
    container.add(warningImage);
    container.add(descriptionContainer);

    container.doRender();
  },

  getAdditionalWarnings: function()
  {
    return this.additionalWarnings;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }

});

Class.define('app.views.medications.timeline.AdditionalWarningRowData', 'tm.jquery.Object', {
  _therapy: null,
  _container: null,
  _aborted: false,
  /** @type {string|null} */
  _reason: null,

  Constructor: function(config)
  {
    this._warnings = [];
    this.callSuper(config)
  },

  addWarning: function(warning)
  {
    this._warnings.push(warning);
  },
  getWarnings: function()
  {
    return this._warnings;
  },
  setTherapy: function(therapy)
  {
    this._therapy = therapy;
  },
  getTherapy: function()
  {
    return this._therapy;
  },
  setContainer: function(container)
  {
    this._container = container;
  },
  getContainer: function()
  {
    return this._container;
  },
  setAborted: function(aborted)
  {
    this._aborted = aborted;
  },
  isAborted: function()
  {
    return this._aborted === true;
  },
  /**
   * @param {string|null} reason
   */
  setReason: function(reason)
  {
    this._reason = reason;
  },
  /**
   * @return {string|null}
   */
  getReason: function()
  {
    return this._reason;
  },

  toJson: function()
  {
    return this.convert(this.callSuper(), {
      warning: this._warnings,
      therapy: this._therapy,
      container: this._container,
      aborted: this._aborted,
      reason: this._reason
    });
  }
});