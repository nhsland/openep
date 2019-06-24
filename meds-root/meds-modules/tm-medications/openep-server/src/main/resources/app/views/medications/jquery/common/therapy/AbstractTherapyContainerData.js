/**
 * The purpose of this class is to provide an abstraction/interface of the data object for the
 * {@link app.views.medications.common.therapy.TherapyContainer}. As such it only provides read access to the common
 * properties the presentation component requires and is intended to be extended by any DTO or domain model
 * jsClasses which is used as the data object for the container. While it might seem strange to have DTOs and models all
 * based on a UI abstraction, it's currently the only viable solution I could find.
 *
 * As such none of the field names are predefined so that we don't litter the objects that extend this jsClass
 * with unused properties. If any of the properties behind getters are named differently, you need to override
 * the getter in the parent jsClass.
 */
Class.define('app.views.medications.common.therapy.AbstractTherapyContainerData', 'tm.jquery.Object', {
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @return {app.views.medications.common.dto.Therapy|undefined|null}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @return {Date|undefined|null}
   */
  getOriginalTherapyStart: function()
  {
    return this.originalTherapyStart;
  },

  /**
   * @return {app.views.medications.common.dto.TherapyChange[]}
   */
  getChanges: function()
  {
    return tm.jquery.Utils.isArray(this.changes) ? this.changes : [];
  },

  /**
   * @return {string|null|undefined}
   */
  getChangeType: function()
  {
    return this.changeType;
  },

  /**
   * @return {app.views.medications.common.dto.TherapyChangeReason|null|undefined}
   */
  getTherapyChangeReason: function()
  {
    return this.changeReasonDto;
  },

  /**
   * @return {string|null|undefined} of type {@link app.views.medications.TherapyEnums.therapyStatusEnum}
   */
  getTherapyStatus: function()
  {
    return this.therapyStatus;
  },

  /**
   * @returns {Array<String>}
   */
  getValidationIssues: function()
  {
    return [];
  },

  /**
   * @returns {boolean}
   */
  isValid: function()
  {
    return this.getValidationIssues().length === 0
  },

  /**
   * @returns {Array}
   */
  getMedicationProperties: function()
  {
    return [];
  },

  /**
   * @returns {boolean}
   */
  hasMedicationProperties: function()
  {
    return this.getMedicationProperties().length > 0;
  },

  /**
   * @returns {boolean}
   */
  hasNonFormularyMedications: function()
  {
    return false;
  },

  /**
   * Returns a reason, required by certain therapy status transitions (such as stop or suspend)
   * @returns {String|null}
   */
  getStatusReason: function()
  {
    return this.statusReason;
  },

  /**
   * True when the container is presenting a future prescription (order), which should be automatically administered once
   * prescribed.
   * @returns {boolean}
   */
  isRecordAdministration: function()
  {
    return this.recordAdministration === true;
  }
});