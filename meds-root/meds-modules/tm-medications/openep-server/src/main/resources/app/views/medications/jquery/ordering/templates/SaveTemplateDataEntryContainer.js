Class.define('app.views.medications.ordering.SaveTemplateDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  statics: {
    DIALOG_WIDTH: 370,
    DIALOG_HEIGHT: 370
  },
  /** @type string|null */
  cls: 'save-template-dialog',
  /** @type app.views.common.AppView */
  view: null,
  /** @type Array<app.views.medications.ordering.dto.TherapyTemplateElement> */
  elements: null,
  /** @type app.views.medications.ordering.dto.TherapyTemplates */
  templates: null,
  /** @type boolean and requires a single {@link app.views.medications.ordering.SaveTemplateDataEntryContainer#elements} */
  addToExistingTemplateSupported: true,
  /** @type string of {@link app.views.medications.TherapyEnums.therapyTemplateModeEnum} */
  templateMode: app.views.medications.TherapyEnums.therapyTemplateModeEnum.INPATIENT,
  /** @type Array<string>|null containing a list of possible custom groups for which templates can be saved */
  customGroups: null,

  /** @type function(app.views.common.AppResultData) */
  _resultCallback: null,
  /** @type tm.jquery.Form */
  _validationForm: null,
  /** @type tm.jquery.SelectBox */
  _templateTypeSelectBox: null,
  /** @type tm.jquery.RadioButtonGroup */
  _modeButtonGroup: null,
  /** @type tm.jquery.RadioButton */
  _newTemplateRadioButton: null,
  /** @type tm.jquery.RadioButton */
  _existingTemplateRadioButton: null,
  /** @type tm.jquery.TextField */
  _newTemplateNameField: null,
  /** @type tm.jquery.TypeaheadField */
  _overrideTemplateTypeahead: null,
  /** @type tm.jquery.CheckBox|null */
  _incompleteTherapyCheckBox: null,
  /** @type app.views.medications.ordering.templates.TherapyTemplatePreconditionContainer */
  _templatePreconditionContainer: null,
  /** @type app.views.medications.common.VerticallyTitledComponent|null */
  _customGroupSelectComponent: null,
  /** @type tm.jquery.Dialog */
  _dialog: null,
  /** @type object */
  _templateTypeSourceMap: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.elements = tm.jquery.Utils.isArray(this.elements) ? this.elements : [];

    if (!!this.addToExistingTemplateSupported && this.elements.length > 1)
    {
      throw new Error('addToExistingTemplateSupported can only be set true when using one element');
    }

    this._buildTemplateSourceMap();
    this._buildComponents();
    this._buildGui();

    var self = this;

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      setTimeout(function()
      {
        self._newTemplateNameField.focus();
      }, 0);
    });
  },

  /**
   * Set the dialog that houses this data entry container. Required to dynamically resize the dialog based on
   * additional input fields that might need to be displayed.
   * @param {tm.jquery.Dialog} dialog
   */
  setDialog: function(dialog)
  {
    this._dialog = dialog;
  },

  /**
   * @param {function(app.views.common.AppResultData)} resultDataCallback
   */
  processResultData: function(resultDataCallback)
  {
    this._resultCallback = resultDataCallback;
    this._setupValidation();
    this._validationForm.submit();
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;

    this._templateTypeSelectBox = new tm.jquery.SelectBox({
      cls: 'template-type-selection',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    if (this.view.getTherapyAuthority().isManageUserTemplatesAllowed())
    {
      this._templateTypeSelectBox.addOption(
          tm.jquery.SelectBox.createOption(
              enums.templateTypeEnum.USER,
              this.view.getDictionary('my.order.set'),
              null,
              null,
              true));
    }

    if (this.view.getTherapyAuthority().isManageOrganizationalTemplatesAllowed() &&
        !tm.jquery.Utils.isEmpty(this.view.getCareProviderId()))
    {
      this._templateTypeSelectBox.addOption(
          tm.jquery.SelectBox.createOption(
              enums.templateTypeEnum.ORGANIZATIONAL,
              this.view.getDictionary('organizational.order.set'),
              null,
              null,
              false));
    }

    if (this.view.getTherapyAuthority().isManagePatientTemplatesAllowed())
    {
      this._templateTypeSelectBox.addOption(
          tm.jquery.SelectBox.createOption(
              enums.templateTypeEnum.PATIENT,
              this.view.getDictionary('patient.order.set'),
              null,
              null,
              false));
    }

    if (this._isCustomGroupsPresent())
    {
      this._templateTypeSelectBox.addOption(
          tm.jquery.SelectBox.createOption(
              enums.templateTypeEnum.CUSTOM_GROUP,
              this.view.getDictionary('custom.group.order.set'),
              null,
              null,
              false));

      this._customGroupSelectComponent = new app.views.medications.common.VerticallyTitledComponent({
        cls: 'custom-group-selection',
        scrollable: 'visible',
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
        titleText: this.view.getDictionary('custom.group.order.set'),
        hidden: true,
        contentComponent: new tm.jquery.SelectBox({
          options: this._createCustomGroupSelectBoxOptions(),
          selections: [],
          multiple: false,
          flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
        })
      });
      this._customGroupSelectComponent.getContentComponent()
          .on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, this._onCustomGroupSelectComponentSelectionChange.bind(this));
    }

    var firstTemplateTypeEnum = this._templateTypeSelectBox.getOptions()[0].getValue();
    this._templateTypeSelectBox.setSelections([firstTemplateTypeEnum]);
    this._templateTypeSelectBox.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        this._onTemplateTypeSelectBoxSelectionChange.bind(this));

    this._newTemplateRadioButton = new tm.jquery.RadioButton({cls: 'new-template-button', checked: true});
    this._existingTemplateRadioButton = new tm.jquery.RadioButton({cls: 'existing-template-button'});
    this._modeButtonGroup = new tm.jquery.RadioButtonGroup({
      groupName: 'modeGroup',
      radioButtons: [this._newTemplateRadioButton, this._existingTemplateRadioButton],
      onChange: this._onModeButtonGroupChange.bind(this)
    });

    this._newTemplateNameField = new tm.jquery.TextField({
      cls: 'template-name-field',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, '100%')
    });

    this._newTemplateNameField.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        this._onNewTemplateNameFieldSelectionChange.bind(this));

    this._overrideTemplateTypeahead = new tm.jquery.TypeaheadField({
      cls: 'templates-combo',
      displayProvider: function(template)
      {
        return template.name;
      },
      mode: 'advanced',
      minLength: 1,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      items: 10000
    });

    this._overrideTemplateTypeahead.on(
        tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        this._onTemplatesSelectBoxSelectionChange.bind(this));
    this._overrideTemplateTypeahead.setSource(this._templateTypeSourceMap[firstTemplateTypeEnum]());

    if (this.addToExistingTemplateSupported)
    {
      this._incompleteTherapyCheckBox = new tm.jquery.CheckBox({
        cls: 'incomplete-therapy-checkbox',
        labelText: this.view.getDictionary('mark.therapy.incomplete')
      });
      if (this.elements[0].isIncomplete())
      {
        this._incompleteTherapyCheckBox.setChecked(true);
        this._incompleteTherapyCheckBox.setEnabled(false);
      }
    }

    this._validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        self._saveTemplate();
      },
      onValidationError: function()
      {
        self._resultCallback(new app.views.common.AppResultData({success: false}));
      },
      requiredFieldValidatorErrorMessage: this.view.getDictionary('field.value.is.required')
    });
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0));

    this.add(
        new app.views.medications.common.VerticallyTitledComponent({
          scrollable: 'visible',
          flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
          titleText: this.view.getDictionary('type'),
          contentComponent: this._templateTypeSelectBox
        }));

    if (!!this._customGroupSelectComponent)
    {
      this.add(this._customGroupSelectComponent);
    }

    var newTemplateContainer = new app.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      titleText: this.view.getDictionary('new.order.set')
    });
    newTemplateContainer.getContentComponent().setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));
    newTemplateContainer.getContentComponent().add(this._newTemplateRadioButton);
    newTemplateContainer.getContentComponent().add(this._newTemplateNameField);
    this.add(newTemplateContainer);

    var existingTemplateContainer = new app.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      titleText: this.addToExistingTemplateSupported ?
          this.view.getDictionary('add.to.existing.order.set') :
          this.view.getDictionary('override.existing.order.set')
    });
    existingTemplateContainer.getContentComponent().setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));
    existingTemplateContainer.getContentComponent().add(this._existingTemplateRadioButton);
    existingTemplateContainer.getContentComponent().add(this._overrideTemplateTypeahead);
    this.add(existingTemplateContainer);

    if (this.addToExistingTemplateSupported)
    {
      this.add(this._incompleteTherapyCheckBox);
    }

    this._templatePreconditionContainer = new app.views.medications.ordering.templates.TherapyTemplatePreconditionContainer({
      view: this.view
    });
    this._templatePreconditionContainer.on(
        app.views.medications.ordering.templates.TherapyTemplatePreconditionContainer.EVENT_TYPE_PRECONDITION_TYPE_SELECTION_CHANGE,
        this._onTemplatePreconditionContainerTypeChange.bind(this));

    var preconditionContainer = new app.views.medications.common.VerticallyTitledComponent({
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      titleText: this.view.getDictionary('defined.criteria'),
      contentComponent: this._templatePreconditionContainer,
      alignSelf: 'stretch'
    });
    this.add(preconditionContainer)
  },

  _setupValidation: function()
  {
    var self = this;
    this._validationForm.reset();
    if (this._modeButtonGroup.getActiveRadioButton() === self._newTemplateRadioButton)
    {
      this._validationForm.addFormField(new tm.jquery.FormField({
        component: self._newTemplateNameField,
        required: true,
        componentValueImplementationFn: function()
        {
          return self._newTemplateNameField.getValue();
        }
      }));
    }
    else
    {
      this._validationForm.addFormField(new tm.jquery.FormField({
        component: self._overrideTemplateTypeahead,
        required: true,
        componentValueImplementationFn: function()
        {
          return self._overrideTemplateTypeahead.getSelection();
        }
      }));
    }
    this._templatePreconditionContainer.getFormFieldValidators().forEach(function(validator)
    {
      self._validationForm.addFormField(validator);
    })
  },

  /**
   * True, if the custom groups are present, otherwise false.
   * @return {boolean}
   * @private
   */
  _isCustomGroupsPresent: function()
  {
    return tm.jquery.Utils.isArray(this.customGroups) && this.customGroups.length > 0;
  },

  /**
   * @returns {Array<tm.jquery.SelectBox.Option>}
   * @private
   */
  _createCustomGroupSelectBoxOptions: function()
  {
    return this.customGroups.map(function mapToOption(group)
    {
      return tm.jquery.SelectBox.createOption(group, group);
    });
  },

  /**
   * Change event handler for {@link #_templateTypeSelectBox}. Sets the appropriate source template list for {@link _overrideTemplateTypeahead},
   * based on the selected type of template. If the previous or current selection is of type custom groups, also
   * handles showing or hiding the group type selection component, along with resizing the dialog.
   * @param {tm.jquery.SelectBox} component
   * @param {Object} componentEvent
   * @private
   */
  _onTemplateTypeSelectBoxSelectionChange: function(component, componentEvent)
  {
    var enums = app.views.medications.TherapyEnums;
    var type = this._getSelectedTemplateType();

    this._overrideTemplateTypeahead.setSource(this._templateTypeSourceMap[type]());
    this._overrideTemplateTypeahead.setSelection(null);

    // skip visibility change if the previous selection or current selection has nothing to do with the custom groups opt.
    if (!!this._customGroupSelectComponent &&
        componentEvent.getEventData().changes.selections.contains(enums.templateTypeEnum.CUSTOM_GROUP))
    {
      this._applyCustomGroupSelectionVisibility(type === enums.templateTypeEnum.CUSTOM_GROUP);
    }
  },

  /**
   * Event handler for the change event of {@link #_overrideTemplateTypeahead}. If a selection has been made, the mode
   * button is switched to the existing template selection and the preconditions for this template are displayed. Since
   * the height of the dialog has to be adjusted based on the presence of preconditions, the method checks if there are
   * any existing preconditions being displayed, then checks if the newly selected template has any preconditions saved
   * and if the number of preconditions doesn't match, adjusts the size accordingly (either make the dialog smaller, or
   * larger). If the number of preconditions matches, we keep the size.
   * At this point deselection isn't supported so we don't react to it.
   * @private
   */
  _onTemplatesSelectBoxSelectionChange: function()
  {
    if (!!this._overrideTemplateTypeahead.getSelection())
    {
      this._modeButtonGroup.setActiveRadioButton(this._existingTemplateRadioButton);

      var selectedTemplate = this._overrideTemplateTypeahead.getSelection();
      var prevPreconditionCount = !!this._templatePreconditionContainer.getPrecondition() ? 1 : 0;
      var newPreconditionCount = selectedTemplate.getPreconditions().length;

      if (prevPreconditionCount !== newPreconditionCount)
      {
        this._applyDialogSizeByTemplatePreconditionsCount(newPreconditionCount);
      }

      this._templatePreconditionContainer.setPrecondition(
          selectedTemplate.hasPreconditions() ?
              selectedTemplate.getPreconditions()[0] :
              undefined);
      this._templatePreconditionContainer.setPreconditionInputAvailability(!this.addToExistingTemplateSupported);
    }
  },

  /**
   * Change event handler for {@link #_modeButtonGroup}. Configures the related components based on the active radio
   * button.
   * @private
   */
  _onModeButtonGroupChange: function()
  {
    var self = this;
    setTimeout(function()
    {
      if (self._modeButtonGroup.getActiveRadioButton() !== self._newTemplateRadioButton)
      {
        self._newTemplateNameField.setValue(null);
        if (self.addToExistingTemplateSupported)
        {
          self._templatePreconditionContainer.setPreconditionInputAvailability(false);
        }
      }
      if (self._modeButtonGroup.getActiveRadioButton() !== self._existingTemplateRadioButton)
      {
        self._templatePreconditionContainer.setPreconditionInputAvailability(true);

        self._overrideTemplateTypeahead.setSelection(null);
      }
    }, 100);
  },

  /**
   * Value change event handler for {@link #_newTemplateNameField}. Sets the active radio button selection of
   * {@link #_modeButtonGroup} to the corresponding value, once the user enters the name of a new template under which
   * he wants to save.
   * @private
   */
  _onNewTemplateNameFieldSelectionChange: function()
  {
    if (!!this._newTemplateNameField.getValue())
    {
      this._modeButtonGroup.setActiveRadioButton(this._newTemplateRadioButton);
    }
  },

  /**
   * Selection change event handler for the content component of {@link #_customGroupSelectComponent}. Sets the
   * correct source of existing templates for {@link #_overrideTemplateTypeahead}. The source object map takes care of the
   * actual filtering based on the active selection.
   * @private
   */
  _onCustomGroupSelectComponentSelectionChange: function()
  {
    this._overrideTemplateTypeahead.setSource(
        this._templateTypeSourceMap[app.views.medications.TherapyEnums.templateTypeEnum.CUSTOM_GROUP]());
    this._overrideTemplateTypeahead.setSelection(null);
    this._modeButtonGroup.setActiveRadioButton(this._newTemplateRadioButton);
  },

  /**
   * Selection change event handler for {@link #_templatePreconditionContainer}. Triggers dialog resizing based
   * on the selection.
   * @param component
   * @param componentEvent
   * @private
   */
  _onTemplatePreconditionContainerTypeChange: function(component, componentEvent)
  {
    this._applyDialogSizeByTemplatePreconditionsCount(componentEvent.getEventData().selections.length);
  },

  /**
   * Resize the dialog based on the presence of template preconditions (which in turn means we're displaying additional
   * input fields). At this point only one precondition is taken into account when adjusting the size.
   * @param {number} count
   * @private
   */
  _applyDialogSizeByTemplatePreconditionsCount: function(count)
  {
    var componentSize = 45;
    this._dialog.setHeight(
        count > 0 ?
            this._dialog.getHeight() + componentSize :
            this._dialog.getHeight() - componentSize);
  },

  /**
   * Show or hide the custom group selection component.
   * @param {boolean} visible
   * @private
   */
  _applyCustomGroupSelectionVisibility: function(visible)
  {
    var componentSize = 56;
    this.isRendered() ?
        (visible ? this._customGroupSelectComponent.show() : this._customGroupSelectComponent.hide()) :
        this._customGroupSelectComponent.setHidden(!visible);

    this._dialog.setHeight(visible ? this._dialog.getHeight() + componentSize : this._dialog.getHeight() - componentSize);
  },

  _getSelectedTemplateType: function()
  {
    return this._templateTypeSelectBox.getSelections()[0];
  },

  _saveTemplate: function()
  {
    var self = this;
    var template = this._buildTemplate();

    if (template.id === 0 && this._doseTemplateWithNameAlreadyExist(template.name))
    {
      this.view
          .getAppNotifier()
          .warning(
              this.view.getDictionary('template.with.name.already.exists'),
              app.views.common.AppNotifierDisplayType.HTML,
              360,
              122);
      failureHandler();
    }
    else
    {
      this.view
          .getRestApi()
          .saveTemplate(template, this.templateMode, true)
          .then(
              function()
              {
                self._resultCallback(new app.views.common.AppResultData({success: true}));
              },
              failureHandler);
    }
    function failureHandler()
    {
      self._resultCallback(new app.views.common.AppResultData({success: false}));
    }
  },

  _doseTemplateWithNameAlreadyExist: function(templateName)
  {
    var templates = this._overrideTemplateTypeahead.getSource();
    for (var i = 0; i < templates.length; i++)
    {
      if (templates[i].getName() === templateName)
      {
        return true;
      }
    }
    return false;
  },

  /**
   * @return {app.views.medications.ordering.dto.TherapyTemplate}
   * @private
   */
  _buildTemplate: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var createNewTemplate = this._modeButtonGroup.getActiveRadioButton() === this._newTemplateRadioButton;
    var selectedTemplate = this._overrideTemplateTypeahead.getSelection();
    var templateElements = [];

    if (this.addToExistingTemplateSupported && !createNewTemplate && selectedTemplate)
    {
      templateElements = selectedTemplate.templateElements.slice();
    }

    this.elements.forEach(
        function addToTemplateElements(element)
        {
          if (this._incompleteTherapyCheckBox)
          {
            element.setTemplateStatus(
                this._incompleteTherapyCheckBox.isChecked() ?
                    app.views.medications.TherapyEnums.therapyTemplateStatus.INCOMPLETE :
                    app.views.medications.TherapyEnums.therapyTemplateStatus.COMPLETE);
          }
          templateElements.push(element);
        },
        this);

    var templateType = this._getSelectedTemplateType();
    var isOrganizationalTemplate = templateType === enums.templateTypeEnum.ORGANIZATIONAL;
    var isPatientTemplate = templateType === enums.templateTypeEnum.PATIENT;
    var precondition = this._templatePreconditionContainer.getPrecondition();
    var customGroup = !!this._customGroupSelectComponent &&
    this._customGroupSelectComponent.getContentComponent().getSelections().length > 0 ?
        this._customGroupSelectComponent.getContentComponent().getSelections()[0] :
        null;

    return new app.views.medications.ordering.dto.TherapyTemplate({
      id: createNewTemplate ? 0 : this._overrideTemplateTypeahead.getSelection().id,
      name: createNewTemplate ? this._newTemplateNameField.getValue() : this._overrideTemplateTypeahead.getSelection().name,
      type: templateType,
      userId: this.view.getCurrentUserAsCareProfessional().id,
      careProviderId: isOrganizationalTemplate ? this.view.getCareProviderId() : null,
      patientId: isPatientTemplate ? this.view.getPatientId() : null,
      templateElements: templateElements,
      preconditions: precondition ? [precondition] : [],
      group: customGroup
    });
  },

  /**
   * Constructs the {@link #_templateTypeSourceMap} object map whose properties (keys) are of type
   * {@link app.views.medications.TherapyEnums.templateTypeEnum} and their values are functions which return the proper
   * source of templates from the {@link #templates} object. Having this map allows us to not to use switch / if statements.
   * @private
   */
  _buildTemplateSourceMap: function()
  {
    var self = this;
    var enums = app.views.medications.TherapyEnums;
    this._templateTypeSourceMap = {};

    this._templateTypeSourceMap[enums.templateTypeEnum.ORGANIZATIONAL] = function()
    {
      return self.templates.getOrganizationTemplates();
    };
    this._templateTypeSourceMap[enums.templateTypeEnum.USER] = function()
    {
      return self.templates.getUserTemplates();
    };

    this._templateTypeSourceMap[enums.templateTypeEnum.PATIENT] = function()
    {
      return self.templates.getPatientTemplates();
    };
    this._templateTypeSourceMap[enums.templateTypeEnum.CUSTOM_GROUP] = function()
    {
      var customGroup = !!self._customGroupSelectComponent ?
          self.templates
              .getCustomTemplateGroups()
              .find(function byGroupName(customTemplateGroup)
              {
                return customTemplateGroup.getGroup() ===
                    self._customGroupSelectComponent.getContentComponent().getSelections()[0];
              }) :
          undefined;

      return !!customGroup ? customGroup.getCustomTemplates() : [];
    };
  }
});
