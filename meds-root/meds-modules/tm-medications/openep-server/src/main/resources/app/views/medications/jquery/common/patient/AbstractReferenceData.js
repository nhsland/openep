Class.define('app.views.medications.common.patient.AbstractReferenceData', 'tm.jquery.Object', {
  /**
   * The purpose of this class is to provide an abstraction/interface of the dosage and rate calculation reference data
   * used with various components that compose the {@link app.views.medications.ordering.MedicationsOrderingContainer}. As
   * such this jsClass only provides getters to common properties that must be accessible. The concrete implementation
   * then has the ability to override the properties with correct field behind names.
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Returns the reference weight, in kg, that should be used for dosing or rate calculations. Can be undefined.
   * @return {number|undefined|null}
   */
  getWeight: function()
  {
    return this.weight;
  },

  /**
   * Returns the reference height, in cm, that should be used for dosing or rate calculations. Can be undefined.
   * @return {number|undefined|null}
   */
  getHeight: function()
  {
    return this.height;
  },

  /**
   * Returns the calculated body surface area. Requires both the reference weight and height to be present, otherwise
   * returns undefined.
   * @return {number|undefined}
   */
  getBodySurfaceArea: function()
  {
    if (tm.jquery.Utils.isNumeric(this.getHeight()) && tm.jquery.Utils.isNumeric(this.getWeight()))
    {
      return Math.sqrt((this.getHeight() * this.getWeight()) / 3600.0);
    }
    return undefined;
  },

  /**
   * Returns the birth date which should be the basis for any calculations of age.
   * @return {Date|undefined|null}
   */
  getDateOfBirth: function()
  {
    return this.dateOfBirth;
  },

  /**
   * @returns {Number|null}
   */
  getAgeInYears: function()
  {
    if (tm.jquery.Utils.isDate(this.getDateOfBirth()))
    {
      return moment(CurrentTime.get()).diff(this.getDateOfBirth(), 'years');
    }
    return null;
  }
});