Class.define('app.views.medications.ordering.ApplicationPreconditionPane', 'app.views.common.containers.AppDataEntryContainer', {

  cls: 'therapy-preconditions-pane',
  /** configs */
  view: null,
  orderingBehaviour: null,
  applicationPrecondition: null,  //optional
  reminderDays: null,
  reminderComment: null,

  /** components **/
  conditionsButtonGroup: null,

  /** privates*/

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined.');
    }
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();
    if (this.applicationPrecondition)
    {
      this._setCondition(this.applicationPrecondition);
    }
    this._setReminder();
  },

  /** private methods */
  _buildComponents: function()
  {
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;

    var noSelectionButton = new tm.jquery.RadioButton({
      labelText: view.getDictionary('no.medication.start.criterion'),
      data: null,
      labelAlign: 'right',
      checked: true
    });

    var emptyStomachEnum = enums.medicationAdditionalInstructionEnum.EMPTY_STOMACH;
    var emptyStomachButton = new tm.jquery.RadioButton({
      labelText: view.getDictionary('MedicationAdditionalInstructionEnum.' + emptyStomachEnum),
      data: emptyStomachEnum,
      labelAlign: 'right'
    });

    var beforeMealEnum = enums.medicationAdditionalInstructionEnum.BEFORE_MEAL;
    var beforeMealButton = new tm.jquery.RadioButton({
      labelText: view.getDictionary('MedicationAdditionalInstructionEnum.' + beforeMealEnum),
      data: beforeMealEnum,
      labelAlign: 'right'
    });

    var withFoodEnum = enums.medicationAdditionalInstructionEnum.WITH_FOOD;
    var withFoodButton = new tm.jquery.RadioButton({
      labelText: view.getDictionary('MedicationAdditionalInstructionEnum.' + withFoodEnum),
      data: withFoodEnum,
      labelAlign: 'right'
    });

    var afterMealEnum = enums.medicationAdditionalInstructionEnum.AFTER_MEAL;
    var afterMealButton = new tm.jquery.RadioButton({
      labelText: view.getDictionary('MedicationAdditionalInstructionEnum.' + afterMealEnum),
      data: afterMealEnum,
      labelAlign: 'right'
    });

    var atBedtimeEnum = enums.medicationAdditionalInstructionEnum.AT_BEDTIME;
    var atBedtimeButton = new tm.jquery.RadioButton({
      labelText: view.getDictionary('MedicationAdditionalInstructionEnum.' + atBedtimeEnum),
      data: atBedtimeEnum,
      labelAlign: 'right'
    });

    var regardlessOfMealEnum = enums.medicationAdditionalInstructionEnum.REGARDLESS_OF_MEAL;
    var regardlessOfMealButton = new tm.jquery.RadioButton({
      labelText: view.getDictionary('MedicationAdditionalInstructionEnum.' + regardlessOfMealEnum),
      data: regardlessOfMealEnum,
      labelAlign: 'right'
    });
    this.conditionsButtonGroup = new tm.jquery.RadioButtonGroup({
      groupName: 'vertical-radio-button-group',
      cls: 'conditions-button-group'
    });
    this.conditionsButtonGroup.add(noSelectionButton);
    this.conditionsButtonGroup.add(emptyStomachButton);
    this.conditionsButtonGroup.add(beforeMealButton);
    this.conditionsButtonGroup.add(withFoodButton);
    this.conditionsButtonGroup.add(afterMealButton);
    this.conditionsButtonGroup.add(atBedtimeButton);
    this.conditionsButtonGroup.add(regardlessOfMealButton);
  },

  _buildGui: function()
  {
    var appFactory = this.getView().getAppFactory();
    this.add(appFactory.createVRadioButtonGroupContainer(this.conditionsButtonGroup, 0));
    if (this.getOrderingBehaviour().isReviewReminderAvailable())
    {
      this.add(this._buildReminderContainer());
    }
  },

  _setCondition: function(applicationPrecondition)
  {
    for (var i = 0; i < this.conditionsButtonGroup.getRadioButtons().length; i++)
    {
      var button = this.conditionsButtonGroup.getRadioButtons()[i];
      if (applicationPrecondition === button.data)
      {
        this.conditionsButtonGroup.setActiveRadioButton(button);
      }
    }
  },

  /**
   * @private
   */
  _setReminder: function()
  {
    if (this.getOrderingBehaviour().isReviewReminderAvailable())
    {
      if (this.reminderDays)
      {
        this._reminderDaysField.setValue(this.reminderDays);
        if (this.reminderComment)
        {
          this._reminderCommentField.setValue(this.reminderComment)
        }
      }
    }
  },

  /**
   * @returns {app.views.medications.common.VerticallyTitledComponent}
   * @private
   */
  _buildReminderContainer: function()
  {
    var view = this.getView();
    var reminderContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 0)
    });
    var reminderDaysContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0)
    });
    this._reminderDaysField = new tm.jquery.NumberField({
      cls: "review-reminder-field",
      placeholder: view.getDictionary("days"),
      width: 55
    });
    var reminderDaysLabel = new tm.jquery.Container({
      html: view.getDictionary("days").toLowerCase()
    });

    reminderDaysContainer.add(this._reminderDaysField);
    reminderDaysContainer.add(reminderDaysLabel);

    this._reminderCommentField = new tm.jquery.TextField({
      cls: "review-reminder-comment-field",
      width: 300,
      placeholder: view.getDictionary("commentary")
    });
    reminderContainer.add(reminderDaysContainer);
    reminderContainer.add(this._reminderCommentField);
    return new app.views.medications.common.VerticallyTitledComponent({
      cls: "vertically-titled-component reminder-container",
      titleText: view.getDictionary('reminder'),
      contentComponent: reminderContainer,
      scrollable: "visible",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
  },

  /**
   * @returns {{selection}}
   * @private
   */
  _getSelectedPreconditionAndReminder: function()
  {
    var selectedButton = this.conditionsButtonGroup.getActiveRadioButton();
    var preconditionAndReminder = {
      selection: selectedButton.data
    };
    if (this.getOrderingBehaviour().isReviewReminderAvailable())
    {
      preconditionAndReminder.reminderDays = this._reminderDaysField.getValue();
      preconditionAndReminder.reminderComment = this._reminderCommentField.getValue();
    }
    return preconditionAndReminder;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    var self = this;
    var view = this.getView();
    var validationForm = new tm.jquery.Form({
      onValidationSuccess: function()
      {
        resultDataCallback(new app.views.common.AppResultData({
          success: true,
          value: self._getSelectedPreconditionAndReminder()
        }));
      },
      onValidationError: function()
      {
        resultDataCallback(new app.views.common.AppResultData({
          success: false
        }));
      },
      requiredFieldValidatorErrorMessage: view.getDictionary("field.value.is.required")
    });
    if (this.getOrderingBehaviour().isReviewReminderAvailable())
    {
      validationForm.addFormField(new tm.jquery.FormField({
        component: self._reminderDaysField,
        required: true,
        componentValueImplementationFn: function()
        {
          if (self._reminderCommentField.getValue() && !self._reminderDaysField.getValue())
          {
            return null;
          }
          return true;
        }
      }));
    }
    validationForm.submit();
  }
});
