Class.define('app.views.medications.pharmacists.dto.TherapyProblemDescription', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject){
      if (tm.jquery.Utils.isEmpty(jsonObject)) return null;

      return new app.views.medications.pharmacists.dto.TherapyProblemDescription({
        categories: jsonObject.categories,
        outcome: jsonObject.outcome,
        impact: jsonObject.impact,
        recommendation: jsonObject.recommendation
      });
    }
  },
  categories: null, /* array of NamedIdentity */
  outcome: null, /* NamedIdentity */
  impact: null, /* NamedIdentity */
  recommendation: null, /* string */

  /* getters and setters */
  getCategories: function()
  {
    return this.categories;
  },
  setCategories: function(value)
  {
    this.categories = value;
  },
  getOutcome: function()
  {
    return this.outcome;
  },
  setOutcome: function(value)
  {
    this.outcome = value;
  },
  getImpact: function()
  {
    return this.impact;
  },
  setImpact: function(value)
  {
    this.impact = value;
  },
  getRecommendation: function()
  {
    return this.recommendation;
  },
  setRecommendation: function(value)
  {
    this.recommendation = value;
  }
});