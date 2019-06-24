Class.define('app.views.medications.ordering.UniversalStrengthContainer', 'tm.jquery.Container', {
  cls: 'universal-strength-container',
  view: null,
  _strengthNumeratorField: null,
  _strengthNumeratorUnitField: null,
  _strengthDenominatorField: null,
  _strengthDenominatorUnitField: null,
  _fractionLine: null,

  _doseForm: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * @returns {app.views.medications.common.dto.PrescribingDose}
   */
  getPrescribingDose: function()
  {
    return new app.views.medications.common.dto.PrescribingDose({
      numerator: this._strengthNumeratorField.getValue(),
      numeratorUnit: this._strengthNumeratorUnitField.getSelection(),
      denominator: !this._strengthDenominatorField.isHidden() ?
          this._strengthDenominatorField.getValue() :
          null,
      denominatorUnit: !this._strengthDenominatorUnitField.isHidden() ?
          this._strengthDenominatorUnitField.getSelection() :
          null
    })
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getStrengthValidators: function()
  {
    var self = this;

    var formFields = [];

    formFields.push(new tm.jquery.FormField({
      component: this._strengthNumeratorField,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self._strengthNumeratorField.getValue();
        if (!value || value <= 0)
        {
          return null;
        }
        return true;
      }
    }));

    formFields.push(new tm.jquery.FormField({
      component: this._strengthNumeratorUnitField,
      required: true,
      componentValueImplementationFn: function()
      {
        var value = self._strengthNumeratorUnitField.getSelection();
        if (!value || value.length <= 0)
        {
          return null;
        }
        return true;
      }
    }));

    if (!this._strengthDenominatorField.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: this._strengthDenominatorField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self._strengthDenominatorField.getValue();
          if ((!value || value <= 0) &&
              self._strengthDenominatorUnitField.getSelection() &&
              self._strengthDenominatorUnitField.getSelection().length > 0)
          {
            return null;
          }
          return true;
        }
      }));

      formFields.push(new tm.jquery.FormField({
        component: this._strengthDenominatorUnitField,
        required: true,
        componentValueImplementationFn: function()
        {
          var value = self._strengthDenominatorUnitField.getSelection();
          if ((!value || value <= 0) && !!self._strengthDenominatorField.getValue())
          {
            return null;
          }
          return true;
        }
      }));
    }

    return formFields;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Disable {@link #_strengthDenominatorUnitField} and set {@link #app.views.medications.TherapyEnums.knownUnitType.ML}
   * as unit
   */
  adjustForComplexOrderForm: function()
  {
    this._strengthDenominatorUnitField.setSelection(
        this.getView()
            .getUnitsHolder()
            .findKnownUnitByName(app.views.medications.TherapyEnums.knownUnitType.ML).getDisplayName());
    this._strengthDenominatorUnitField.setEnabled(false);
  },

  /**
   * Clear selection and enable {@link #_strengthDenominatorUnitField}
   */
  adjustForSimpleOrderForm: function()
  {
    this._strengthDenominatorUnitField.setSelection();
    this._strengthDenominatorUnitField.setEnabled(true);
  },

  _buildGui: function()
  {
    var view = this.getView();

    this._strengthNumeratorField = app.views.medications.MedicationUtils.createNumberField('n2', 68);
    this._strengthDenominatorField = app.views.medications.MedicationUtils.createNumberField('n2', 68);

    this._strengthNumeratorUnitField = new tm.jquery.TypeaheadField({
      cls: "numerator-unit-field",
      margin: '0 0 0 8',
      minLength: 1,
      mode: 'advanced',
      width: 135,
      items: 10000
    });

    this._strengthDenominatorUnitField = new tm.jquery.TypeaheadField({
      cls: "denominator-unit-field",
      minLength: 1,
      mode: 'advanced',
      margin: "0 1 0 8",
      width: 135,
      items: 10000
    });

    var allUnits = view.getUnitsHolder().getAllUnits();

    this._strengthNumeratorUnitField.setSource(allUnits);
    this._strengthDenominatorUnitField.setSource(allUnits);

    this._fractionLine = new tm.jquery.Container({cls: 'TextData fraction-line', html: '/'});

    this.add(this._strengthNumeratorField);
    this.add(this._strengthNumeratorUnitField);
    this.add(this._fractionLine);
    this.add(this._strengthDenominatorField);
    this.add(this._strengthDenominatorUnitField);

  }
});