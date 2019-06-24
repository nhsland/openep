Class.define('app.views.medications.pharmacists.ConfirmAllTherapiesPlaceholderContainer', 'tm.jquery.Container', {
  cls: 'placeholder-container',

  view: null,

  confirmAllTherapiesCallback: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("center", "center", 0));

    var self = this;
    var view = this.getView();

    var confirmAllTherapiesButton = new tm.jquery.Button({
      text: view.getDictionary("pharmacists.review.confirm.all"),
      type: "link",
      enabled: !tm.jquery.Utils.isEmpty(this.getView().getCurrentUserAsCareProfessional())
    });

    confirmAllTherapiesButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function()
    {
      if (!tm.jquery.Utils.isEmpty(self.confirmAllTherapiesCallback))
      {
        self.confirmAllTherapiesCallback();
      }
    });

    this.add(confirmAllTherapiesButton);

    this.add(new tm.jquery.Component({
      cls: "TextDataLight",
      html: view.getDictionary("or")
    }));
    this.add(new tm.jquery.Component({
      cls: "TextDataLight",
      html: view.getDictionary("select.therapy.from.list")
    }));
  },

  getReviewData: function()
  {
    return { createTimestamp: CurrentTime.get() };
  },

  getView: function()
  {
    return this.view;
  }
});