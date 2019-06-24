Class.define('app.views.medications.ordering.templates.ManageTemplatesDialogBuilder', 'tm.jquery.Object', {
  view: null,

  _templateContext: null,
  _medicationSearchFilter: null,
  _title: null,

  /**
   * Returns a new instance of the manage templates dialog builder. The dialog can be built either for the
   * inpatient templates management, or outpatient templates management.
   * @param {Object} config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Configure the dialog for inpatient templates management.
   * @return {app.views.medications.ordering.templates.ManageTemplatesDialogBuilder}
   */
  configureForInpatient: function()
  {
    this._templateContext = app.views.medications.TherapyEnums.therapyTemplateContextEnum.INPATIENT;
    this._medicationSearchFilter = app.views.medications.TherapyEnums.medicationFinderFilterEnum.INPATIENT_PRESCRIPTION;
    this._title = this.getView().getDictionary('manage.therapy.inpatient.templates');
    return this;
  },

  /**
   * Configure the dialog for outpatient templates.
   * @return {app.views.medications.ordering.templates.ManageTemplatesDialogBuilder}
   */
  configureForOutpatient: function()
  {
    this._templateContext = app.views.medications.TherapyEnums.therapyTemplateContextEnum.OUTPATIENT;
    this._medicationSearchFilter = app.views.medications.TherapyEnums.medicationFinderFilterEnum.OUTPATIENT_PRESCRIPTION;
    this._title = this.getView().getDictionary('manage.therapy.discharge.templates');
    return this;
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Returns a new instance of the therapy template management dialog, as configure by the builder.
   * @return {app.views.common.dialog.AppDialog}
   */
  create: function()
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var contentContainer = new app.views.medications.ordering.templates.ManageTemplatesDataEntryContainer({
      view: this.getView(),
      templateContext: this._templateContext,
      medicationSearchFilterMode: this._medicationSearchFilter
    });

    var dialog = appFactory.createDataEntryDialog(
        this._title,
        null,
        contentContainer,
        function(){ },
        $(window).width() - 50,
        $(window).height() - 150);

    // there's really no good way to override the footer from the factory methods this framework has
    dialog.getFooter().getRightButtons().remove(dialog.getFooter().getConfirmButton());
    dialog.getFooter().getCancelButton().setText(view.getDictionary('done.1'));
    dialog.setContainmentElement(this.getView().getDom());
    dialog.addTestAttribute('manage-templates-dialog');
    dialog.setFitSize(true);
    dialog.setHideOnEscape(false);

    return dialog;
  }
});