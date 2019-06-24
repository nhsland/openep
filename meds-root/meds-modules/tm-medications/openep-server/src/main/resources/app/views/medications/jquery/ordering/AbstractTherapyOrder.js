/**
 * The purpose of this class is to provide an abstraction/interface of the data object that can be used with various
 * components that compose the {@link app.views.medications.ordering.MedicationsOrderingContainer}. As such this
 * jsClass only provides getters and setters to common properties that must be accessible. The concrete implementation
 * then has the ability to override the properties with correct field behind names, if they exist.
 *
 * This jsClass also extends the {@link @app.views.medications.common.therapy.AbstractTherapyContainerData} since instances
 * are intended to be used as data objects for the {@link app.views.medications.common.therapy.TherapyContainer}.
 *
 * When in doubt about which 'abstraction' to add a new method to, follow a simple rule: if the data will be accessed from
 * an instance of the TherapyContainer implementation, which should (internally) know nothing about AbstractTherapyOrder,
 * add it to the AbstractTherapyContainerData. If the method will be access from a client of the TherapyContainer, add it to
 * the AbstractTherapyOrder.
 */
Class.define('app.views.medications.ordering.AbstractTherapyOrder', 'app.views.medications.common.therapy.AbstractTherapyContainerData', {
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Added due to the requirements of editing orders based on order sets (templates). To be implemented in concrete
   * jsClasses. Returns a clone of the existing order for the purpose of the edit operation. The operation should be a deep
   * clone by default.
   * @param {boolean} startEndTimeAvailable
   * @return {app.views.medications.ordering.AbstractTherapyOrder}
   */
  cloneForEdit: function(startEndTimeAvailable)
  {
    throw new Error('Not implemented');
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} value
   */
  setTherapy: function(value)
  {
    this.therapy = value;
  },

  /**
   * @return {app.views.medications.common.dto.Therapy|null}
   */
  getLinkedTherapy: function()
  {
    return this.linkedTherapy;
  }
});