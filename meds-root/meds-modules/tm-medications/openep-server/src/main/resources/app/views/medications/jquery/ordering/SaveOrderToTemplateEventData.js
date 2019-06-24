/** Represents the payload data of 'add to template' events issued by individual order forms. */
Class.define('app.views.medications.ordering.SaveOrderToTemplateEventData', 'app.views.medications.ordering.ConfirmOrderEventData', {
  Constructor: function(config)
  {
    this.callSuper(config);
  }
});