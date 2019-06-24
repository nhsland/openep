Class.define('app.views.medications.pharmacists.dto.PharmacistReviewTherapy', 'app.views.medications.common.therapy.AbstractTherapyContainerData', {
  statics: {
    fromJson: function(jsonObject)
    {
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      var config = jQuery.extend(true, {}, jsonObject);
      if (!!config.therapy)
      {
        config.therapy = app.views.medications.common.TherapyJsonConverter.convert(config.therapy);
      }
      if (tm.jquery.Utils.isArray(config.changes))
      {
        config.changes = config.changes.map(app.views.medications.common.dto.TherapyChange.fromJson);
      }

      return new app.views.medications.pharmacists.dto.PharmacistReviewTherapy(config);
    }
  },
  therapy: null,
  changes: null,
  changeType: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this.changes = tm.jquery.Utils.isArray(this.changes)? this.changes : [];
  },

  /**
   * Cloning capability.
   * @returns {app.views.medications.pharmacists.dto.PharmacistReviewTherapy}
   */
  clone: function()
  {
    return new app.views.medications.pharmacists.dto.PharmacistReviewTherapy({
      therapy: this.getTherapy().clone(),
      changes: this.changes.slice(0),
      changeType: this.changeType
    })
  }
});