Class.define('app.views.medications.ordering.TherapyOrder', 'app.views.medications.ordering.AbstractTherapyOrder', {
  /** @type app.views.medications.common.dto.Therapy */
  therapy: null,
  /** @type app.views.medications.common.dto.TherapyChangeReason|null */
  changeReason: null,
  /** @type app.views.medications.common.dto.Therapy|null */
  linkedTherapy: null,
  /** @type Array<String> */
  validationIssues: null,
  /** @type boolean */
  recordAdministration: false,

  /**
   * Creates a new instance of the 'add to basket' event data, used by all three order forms in conjunction
   * with the {@link app.views.medications.ordering.OrderingContainer}. Solves the issue of numerous optional
   * parameters and no ability for function overloading. Extends (or rather implements) the
   * {@link app.views.medications.ordering.AbstractTherapyOrder}.
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.validationIssues = tm.jquery.Utils.isArray(this.validationIssues) ? this.validationIssues : [];
  },

  /**
   * @override
   * @param {boolean} [startEndTimeAvailable=true]
   * @return {app.views.medications.ordering.TherapyOrder}
   */
  cloneForEdit: function(startEndTimeAvailable)
  {
    return new app.views.medications.ordering.TherapyOrder({
      therapy: this.therapy.clone(true),
      changeReason: this.changeReason ? this.changeReason.clone() : null,
      linkedTherapy: this.linkedTherapy ? this.linkedTherapy.clone(true) : null,
      validationIssues: this.validationIssues.slice(),
      recordAdministration: this.recordAdministration === true
    });
  },

  /**
   * @return {app.views.medications.common.dto.Therapy|null}
   */
  getLinkedTherapy: function()
  {
    return this.linkedTherapy;
  },

  /**
   * @override due to a change of the field behind the getter.
   * @return {app.views.medications.common.dto.TherapyChangeReason|null}
   */
  getTherapyChangeReason: function()
  {
    return this.changeReason;
  },

  /**
   * @param {app.views.medications.common.dto.TherapyChangeReason|null} changeReason
   * @return {app.views.medications.ordering.TherapyOrder}
   */
  setTherapyChangeReason: function(changeReason)
  {
    this.changeReason = changeReason;
    return this;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy|null} therapy
   * @return {app.views.medications.ordering.TherapyOrder}
   */
  setLinkedTherapy: function(therapy)
  {
    this.linkedTherapy = therapy;
    return this;
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} value
   * @return {app.views.medications.ordering.TherapyOrder}
   */
  setTherapy: function(value)
  {
    this.therapy = value;
    return this;
  },

  /**
   * @param {boolean} value
   * @return {app.views.medications.ordering.TherapyOrder}
   */
  setRecordAdministration: function(value)
  {
    this.recordAdministration = value;
    return this;
  },

  /**
   * @override
   * @return {Array<String>}
   */
  getValidationIssues: function()
  {
    return this.validationIssues;
  }
});
