Class.define('app.views.medications.warnings.WarningsContainerRow', 'tm.jquery.Container', {
  cls: 'warnings-container-row',
  /** configs */
  view: null,
  warning: null,
  override: null,

  overrideAvailable: true,
  overrideAllowed: true,

  /** privates: components */
  _commentField: null,

  statics: {
    EVENT_TYPE_NEW_WARNING_OVERRIDE: new tm.jquery.event.EventType({
      name: 'warningsContainerRowNewWarningOverride', delegateName: null
    })
  },

  Constructor: function(config)
  {
    this.callSuper(config);

    this.registerEventTypes('app.views.medications.warnings.WarningsContainerRow', [
      {eventType: app.views.medications.warnings.WarningsContainerRow.EVENT_TYPE_NEW_WARNING_OVERRIDE}
    ]);

    this._buildGui();
  },

  /**
   * @returns {String|null}
   */
  getOverrideReason: function()
  {
    return !!this._commentField && this.isOverrideAllowed() ? this._commentField.getValue() : null;
  },

  /**
   * @returns {app.views.medications.TherapyEnums.warningSeverityEnum|null}
   */
  getWarningSeverity: function()
  {
    return this.getWarning().getSeverity();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /*
   * @returns {app.views.medications.warnings.dto.MedicationsWarning}
   */
  getWarning: function()
  {
    return this.warning;
  },

  /**
   * @returns {app.views.medications.ordering.warnings.WarningOverride|null}
   */
  getOverride: function()
  {
    return this.override;
  },

  setOverride: function(value)
  {
    this.override = value;
  },

  /**
   * @return {boolean} true, if the user has permissions to override a high severity warning. If not, the user will be
   * presented with a warning if he tries to enter an override reason and the component will always return a blank reason.
   */
  isOverrideAllowed: function()
  {
    return this.overrideAllowed === true;
  },

  /**
   * @return {boolean} true, if the component supports the ability to enter an override reason for warnings that require
   * an override reason. Works in conjunction with {@link #isOverrideAllowed} which further determines if the user
   * can actually enter the reason if available.
   */
  isOverrideAvailable: function()
  {
    return this.overrideAvailable === true;
  },

  /**
   * Returns the override reason form field validator when the input is present, otherwise null.
   * @returns {tm.jquery.form.FormField|null}
   */
  getOverrideReasonValidation: function()
  {
    if (!!this._commentField)
    {
      return new tm.jquery.form.FormField({
        name: this.getId() + '-medication-field',
        component: this._commentField,
        validation: new tm.jquery.form.FormFieldsValidation({
          type: "locale",
          validators: [
            app.views.medications.MedicationUtils.buildDefaultMinimumLengthStringValidator(this.view),
            new tm.jquery.RequiredValidator({errorMessage: this.view.getDictionary("field.value.is.required")})
          ],
          markers: {
            error: [
              new tm.jquery.form.FormFieldValidationMarker()
            ]
          }
        })
      })
    }
    return null;
  },

  _buildGui: function()
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;

    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start', 0));

    var warningRowContainer = new tm.jquery.Container({
      cls: 'warning-row-container',
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    var warningContainer = new tm.jquery.Container({
      cls: 'TextData',
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'flex-start'),
      html: this.getWarning().getFormattedWarningDescription(),
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    var leftIconsContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start')
    });
    var rightIconsContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create('flex-end', 'flex-end'),
      margin: '0 0 0 10'
    });

    var severityIconContainer = app.views.medications.warnings.WarningsHelpers.createTypeAndSeverityIconContainer(
        view,
        this.getWarning().getType(),
        this.getWarning().getSeverity()
    );
    var monographContainer = app.views.medications.warnings.WarningsHelpers.createMonographContainer(view, this.getWarning());
    if (severityIconContainer)
    {
      warningContainer.addCls('warning-row-with-icon');
      leftIconsContainer.add(severityIconContainer);
    }
    else
    {
      warningContainer.addCls('warning-row-no-icon');
    }
    warningRowContainer.add(warningContainer);

    if (this.isOverrideAvailable() && this.getWarning().getSeverity() === enums.warningSeverityEnum.HIGH_OVERRIDE)
    {
      var commentContainer = new tm.jquery.Container({
        cls: 'comment-container',
        layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start'),
        margin: '0 0 0 5'
      });
      var commentLabel = new app.views.medications.MedicationUtils.crateLabel(
          'comment-label TextDataBold',
          view.getDictionary('override.reason')
      );

      this._commentField = new tm.jquery.TextField({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
        cls: 'field-flat',
        value: this.getOverride() ? this.getOverride().getOverrideReason() : null
      });

      this._commentField.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
      {
        self._handleCommentFieldValueChange(component.getValue());
      });

      if (!this.isOverrideAllowed())
      {
        this._commentField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_GAINED, function()
        {
          view.getAppNotifier().warning(
              view.getDictionary('medication.warning.override.not.permitted'),
              app.views.common.AppNotifierDisplayType.HTML,
              240,
              180);
        });
        this._commentField.on(tm.jquery.ComponentEvent.EVENT_TYPE_FOCUS_LOST, function(component)
        {
          component.setValue(null);
        });
      }

      commentContainer.add(commentLabel);
      commentContainer.add(this._commentField);
      warningRowContainer.add(commentContainer);
    }

    if (monographContainer)
    {
      rightIconsContainer.add(monographContainer);
    }

    this.add(leftIconsContainer);
    this.add(warningRowContainer);
    this.add(rightIconsContainer);
  },


  _fireNewWarningOverrideEvent: function()
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.warnings.WarningsContainerRow.EVENT_TYPE_NEW_WARNING_OVERRIDE,
      eventData: {
        override: this.getOverride()
      }
    }), null);
  },

  /**
   * Handle override reason change. If no override is set for this row, fire an event letting the
   * parent component know about it (required to correctly reset the values once the list is reloaded).
   *
   * @param {String|null} newValue
   * @private
   */
  _handleCommentFieldValueChange: function(newValue)
  {
    if (!this.getOverride())
    {
      this.setOverride(new app.views.medications.ordering.warnings.WarningOverride({
        warning: this.getWarning(),
        overrideReason: newValue
      }));
      this._fireNewWarningOverrideEvent();
    }
    else
    {
      this.getOverride().setOverrideReason(newValue);
    }
  }
});

