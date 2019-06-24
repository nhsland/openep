Class.define('app.views.medications.timeline.administration.AdministrationWarnings', 'tm.jquery.Object', {
  jumpWarning: null,
  administrationInFutureWarning: null,
  maxAdministrationsWarning: null,
  infusionInactiveWarning: null,
  therapyNotReviewedWarning: null,
  /* constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {String | null}
   */
  getJumpWarning: function()
  {
    return this.jumpWarning;
  },

  /**
   * @param {String} jumpWarning
   */
  setJumpWarning: function(jumpWarning)
  {
    this.jumpWarning = jumpWarning;
  },

  /**
   * @returns {String | null}
   */
  getAdministrationInFutureWarning: function()
  {
    return this.administrationInFutureWarning;
  },

  /**
   * @param {String} administrationInFutureWarning
   */
  setAdministrationInFutureWarning: function(administrationInFutureWarning)
  {
    this.administrationInFutureWarning = administrationInFutureWarning;
  },

  /**
   * @returns {String | null}
   */
  getMaxAdministrationsWarning: function()
  {
    return this.maxAdministrationsWarning;
  },

  /**
   * @param {String} maxAdministrationsWarning
   */
  setMaxAdministrationsWarning: function(maxAdministrationsWarning)
  {
    this.maxAdministrationsWarning = maxAdministrationsWarning;
  },

  /**
   * @returns {String | null}
   */
  getInfusionInactiveWarning: function()
  {
    return this.infusionInactiveWarning;
  },

  /**
   * @param {String} infusionInactiveWarning
   */
  setInfusionInactiveWarning: function(infusionInactiveWarning)
  {
    this.infusionInactiveWarning = infusionInactiveWarning;
  },

  /**
   * @returns {String | null}
   */
  getTherapyNotReviewedWarning: function()
  {
    return this.therapyNotReviewedWarning;
  },

  /**
   * @param {String} therapyNotReviewedWarning
   */
  setTherapyNotReviewedWarning: function(therapyNotReviewedWarning)
  {
    this.therapyNotReviewedWarning = therapyNotReviewedWarning;
  },

  /**
   * @returns {boolean}
   */
  hasRestrictiveWarnings: function()
  {
    return Boolean(this.getAdministrationInFutureWarning() ||
        this.getInfusionInactiveWarning() ||
        this.getJumpWarning() ||
        this.getMaxAdministrationsWarning() ||
        this.getTherapyNotReviewedWarning())
  }

});