Class.define('app.views.medications.common.dto.Range', 'tm.jquery.Object', {
  statics: {
    /**
     * @param {Number|null} min
     * @param {Number|null} max
     * @returns {app.views.medications.common.dto.Range|null}
     */
    createStrict: function(min, max){

      if (!min || !max) return null;

      return new app.views.medications.common.dto.Range({
        min: min,
        max: max
      });
    }
  },
  
  min: null,
  max: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {null|Number}
   */
  getMin: function()
  {
    return this.min;
  },

  /**
   * @returns {null|Number}
   */
  getMax: function()
  {
    return this.max;
  },

  /**
   * Override, as per http://jsclass.jcoglan.com/equality.html recommendation, based on jsClass's Range.
   * @returns {string}
   */
  hash: function()
  {
    var hash = (this.getMin() ? this.getMin() : '') + '..';
    hash += this.getMax() ? this.getMax() : '.';
    return hash;
  },

  /**
   * Override
   * @param {*} other
   * @returns {boolean}
   */
  equals: function(other)
  {
    return JS.isType(other, app.views.medications.common.dto.Range) &&
        other.getMin() === this.getMin() && other.getMax() === this.getMax();
  },

  /**
   * @returns {string}
   */
  toString: function()
  {
    return this.getMin() + ' - ' + this.getMax();
  }
});