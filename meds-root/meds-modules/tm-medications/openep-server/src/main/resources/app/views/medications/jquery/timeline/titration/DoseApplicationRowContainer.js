Class.define('app.views.medications.timeline.titration.DoseApplicationRowContainer', 'app.views.medications.timeline.titration.BaseApplicationRowContainer', {
  scrollable: 'visible',
  cls: "dose-application-row-container",

  futureAdministrationTimes: null,

  _useDoseInAllCheckBox: null,
  _doseTimesSelectBox: null,
  _dosePane: null,
  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @override
   * @returns {app.views.medications.common.VerticallyTitledComponent}
   */
  buildApplicationOptionsColumn: function()
  {
    var self = this;
    var view = this.getView();

    var applicationOptionsColumn = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("application"),
      scrollable: 'visible',
      contentComponent: new tm.jquery.Container({
        scrollable: 'visible',
        layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
      }),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    var doseReuseOptionWrapper = new tm.jquery.Container({
      scrollable: 'visible',
      hidden: this.isScheduleAdditional() || this.isApplyUnplanned() || this.isBolusAdministration(),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    var plannedTimeOptions = this.getFutureAdministrationTimes().map(function mapToSelectBoxOption(plannedTime)
    {
      return tm.jquery.SelectBox.createOption(plannedTime, view.getDisplayableValue(plannedTime, "short.date.time"))
    });

    var useDoseInAllCheckBox = new tm.jquery.CheckBox({
      cls: "use-dose-all-checkbox",
      enabled: plannedTimeOptions.length > 0,
      labelText: view.getDictionary("use.this.dose.in.all.applications.until.including"),
      labelCls: "TextData",
      checked: false,
      labelAlign: "right",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      nowrap: true
    });

    var doseTimesSelectBox = new tm.jquery.SelectBox({
      cls: "dose-time-select",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      dropdownWidth: "stretch",
      allowSingleDeselect: true,
      enabled: plannedTimeOptions.length > 0,
      options: plannedTimeOptions,
      defaultTextProvider: function(selectBox, index, option)
      {
        return view.getDisplayableValue(option.getValue(), "short.date.time");
      }
    });

    doseTimesSelectBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE,
        function alterUseDoseCheckBox(component, componentEvent)
        {
          var eventData = componentEvent.getEventData();
          var selections = eventData && eventData.selections;

          if (selections)
          {
            self._useDoseInAllCheckBox.setChecked(selections.length > 0);
          }
        });

    doseReuseOptionWrapper.add(useDoseInAllCheckBox);
    doseReuseOptionWrapper.add(doseTimesSelectBox);
    applicationOptionsColumn.getContentComponent().add(doseReuseOptionWrapper);

    this._doseTimesSelectBox = doseTimesSelectBox;
    this._useDoseInAllCheckBox = useDoseInAllCheckBox;

    var markAsGivenCheckBox = new tm.jquery.CheckBox({
      labelText: view.getDictionary("mark.this.application.as.already.given"),
      cls: "mark-given-checkbox",
      labelCls: "TextData",
      checked: this.isApplyUnplanned() || this.isBolusAdministration(),
      labelAlign: "right",
      enabled: !(this.isApplyUnplanned() || this.isBolusAdministration()),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      nowrap: true
    });
    markAsGivenCheckBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self.assertAdministrationChange();
    });

    applicationOptionsColumn.getContentComponent().add(markAsGivenCheckBox);

    this._markAsGivenCheckBox = markAsGivenCheckBox;

    return applicationOptionsColumn;
  },

  /**
   * @override
   * @returns {tm.jquery.Container}
   */
  buildApplicationDosingRow: function()
  {
    var view = this.getView();
    var therapy = this.getTherapyForTitration().getTherapy();
    var plannedDose = this.getAdministration() ? this.getAdministration().getPlannedDose() : null;

    var applicationDosingRow = new tm.jquery.Container({
      scrollable: 'visible',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var dosePaneColumn = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary("dose"),
      cls: 'dose-pane-container',
      scrollable: 'visible',
      contentComponent: new app.views.medications.ordering.dosing.DoseContainer({
        view: view,
        medicationData: this.getMedicationData(),
        orderingBehaviour: new app.views.medications.ordering.OrderingBehaviour({
          doseCalculationsAvailable: view.isDoseCalculationsEnabled()
        }),
        referenceData: new app.views.medications.common.patient.ViewBasedReferenceData({view: view}),
        verticalLayout: false,
        addDosageCalculationPane: true,
        addDosageCalcBtn: true,
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        denominatorAlwaysVolume: therapy.isOrderTypeComplex(),
        doseNumerator: plannedDose ? plannedDose.numerator : null,
        doseDenominator: plannedDose ? plannedDose.denominator : null
      }),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    this._dosePane = dosePaneColumn.getContentComponent();

    applicationDosingRow.add(dosePaneColumn);

    return applicationDosingRow;
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getFormFields: function()
  {
    var formFields = [];
    var self = this;
    formFields = formFields.concat(this._dosePane.getDosePaneValidations());

    formFields.push(new tm.jquery.FormField({
      component: this._doseTimesSelectBox,
      required: false,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function(value)
            {
              return self._useDoseInAllCheckBox.isChecked() ? !tm.jquery.Utils.isEmpty(value) : true;
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        var selections = component.getSelections();
        if (selections.length > 0)
        {
          return selections[0];
        }
        return null;
      }
    }));

    formFields.push(new tm.jquery.FormField({
      component: this._markAsGivenCheckBox,
      required: false,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function(value)
            {
              var hasRestrictiveWarnings = self.getAdministrationWarnings() ?
                  self.getAdministrationWarnings().hasRestrictiveWarnings() : false;
              return value === true && !hasRestrictiveWarnings || value === false;
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.isChecked();
      }
    }));

    formFields.push(new tm.jquery.FormField({
      component: this._timePicker,
      required: true,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function()
            {
              return !self.hasAdministrationTimeRelatedWarnings();
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate();
      },
      getComponentValidationMarkElement: function(component)
      {
        return component.getTimePicker().getField().getInputElement();
      }
    }));

    formFields.push(new tm.jquery.FormField({
      component: this._timePicker,
      required: true,
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: null,
            isValid: function()
            {
              return !self.hasAdministrationTimeRelatedWarnings();
            }
          })
        ]
      },
      componentValueImplementationFn: function(component)
      {
        return component.getDate();
      },
      getComponentValidationMarkElement: function(component)
      {
        return component.getDatePicker().getField().getInputElement();
      }
    }));

    return formFields;
  },

  /**
   * @returns {Date|null}
   */
  getSetDoseUntilDateTime: function()
  {
    return this._useDoseInAllCheckBox.isChecked() ? this._doseTimesSelectBox.getSelections()[0] : null;
  },

  /**
   * @returns {app.views.medications.common.dto.TherapyDose}
   */
  getTherapyDose: function()
  {
    var doseEnums = app.views.medications.TherapyEnums.therapyDoseTypeEnum;
    var doseValues = this._dosePane.getDoseWithUnits();
    return new app.views.medications.common.dto.TherapyDose({
      therapyDoseTypeEnum: doseEnums.QUANTITY,
      numerator: doseValues.quantity === null ? doseValues.quantityDenominator : doseValues.quantity,
      denominator: doseValues.quantity === null ? null : doseValues.quantityDenominator,
      numeratorUnit: doseValues.quantityUnit,
      denominatorUnit: doseValues.denominatorUnit
    });
  },

  /**
   * @returns {Array<Object>}
   */
  getFutureAdministrationTimes: function()
  {
    var futureTimes = [];
    var allAdministrations = this.getAllAdministrations();
    var index = allAdministrations.indexOf(this.administration);

    if (index > -1 && index < allAdministrations.length + 1) // makes sure we can skip this administration
    {
      var futureAdministrations = allAdministrations.slice(index + 1);

      futureAdministrations = futureAdministrations.filter(function filterByStatus(administration)
      {
        return !administration.isAdministrationConfirmed();
      });

      futureTimes = futureAdministrations.map(function extractPlannedTime(administration)
      {
        return new Date(administration.plannedTime);
      });
    }

    return futureTimes;
  }
});