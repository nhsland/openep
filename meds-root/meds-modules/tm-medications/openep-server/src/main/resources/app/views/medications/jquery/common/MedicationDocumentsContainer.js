Class.define('app.views.medications.common.MedicationDocumentsContainer', 'tm.jquery.Container', {
  view: null,
  documents: null,
  /**
   * @param {Object} config
   * @param {app.views.common.AppView} config.view
   * @param {Array<app.views.medications.common.dto.MedicationDocument>} config.documents
   * @constructor that returns a new instance of a vertically positioned buttons which link to medication documents. A
   * button may trigger either a PDF download from our API server, or open the document's URL in a new tab.
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.documents = tm.jquery.Utils.isArray(this.documents) ? this.documents : [];
    this._buildGui();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @protected
   * @return {Array<app.views.medications.common.dto.MedicationDocument>}
   */
  getDocuments: function()
  {
    return this.documents;
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "flex-start", 0));
    this.getDocuments()
        .filter(
            /**
             * @param {app.views.medications.common.dto.MedicationDocument} document
             */
            function isValid(document)
            {
              return !!document.getExternalSystem() && !!document.getDocumentReference();
            })
        .forEach(
            /**
             * @param {app.views.medications.common.dto.MedicationDocument} document
             */
            function(document) {
              this.add(this._createDocumentButton(document));
            },
            this);
  },

  _createDocumentButton: function(document)
  {
    var dictionaryName = this.getView().getDictionary(document.getExternalSystem(), true);
    var name = !!dictionaryName ? dictionaryName : document.getExternalSystem();

    return this._attachButtonClickHandler(
        new tm.jquery.Button({cls: 'button-align-left smpc-cls', text: name, type: 'link'}),
        document);
  },

  /**
   * @param {tm.jquery.Component|*} button
   * @param {app.views.medications.common.dto.MedicationDocument} document
   * @return {tm.jquery.Component}
   * @private
   */
  _attachButtonClickHandler: function(button, document)
  {
    var view = this.getView();

    button.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component)
    {
      if (document.isExternalLink())
      {
        var open = window.open(document.getDocumentReference(), '_blank');
        //there is possibility that mobile browser is set to block popups - in this case we have to warn user
        //http://stackoverflow.com/questions/9880316/javascript-window-open-in-safari
        if (!open)
        {
          view.getAppNotifier().warning(
              view.getDictionary('popups.are.blocked.cannot.open.print.preview'),
              app.views.common.AppNotifierDisplayType.HTML,
              450, 180);
        }
      }
      else {
        component.setEnabled(false);
        view
            .getRestApi()
            .downloadMedicationDocument(document.getDocumentReference())
            .always(function()
            {
              component.setEnabled(true);
            });
      }
    });

    return button;
  }
});