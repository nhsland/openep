Class.define('app.views.medications.common.overview.header.DataEntryTooltipFactory', 'tm.jquery.Container', {
  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getView: function()
  {
    return this.view;
  },

  /**
   * @return {tm.jquery.Tooltip} with {@link app.views.medications.common.overview.DateIntervalDataEntryContainer} as the
   * contents, which sends the selected interval as a view action to request the display of external outpatient prescriptions
   * for the active patient. The returned results are displayed by the {@link tm.views.medication.TherapyView}.
   */
  buildViewExternalOutpatientPrescriptionsDataEntryTooltip: function()
  {
    var view = this.getView();
    return this._buildDateIntervalDataEntryTooltip(
        /**
         * @param {{start: Date, end: Date}} selectedInterval
         */
        function(selectedInterval)
        {
          view.sendAction(
              tm.views.medications.TherapyView.VIEW_ACTION_GET_EXTERNAL_OUTPATIENT_PRESCRIPTIONS,
              {
                patientId: view.getPatientId(),
                searchStart: JSON.stringify(selectedInterval.start),
                searchEnd: JSON.stringify(selectedInterval.end)
              });

        });
  },

  /**
   * @return {tm.jquery.Tooltip} with {@link app.views.medications.common.overview.DateIntervalDataEntryContainer} as the
   * contents, which uses the selected end date and triggers the browser download of the therapy history report as a PDF.
   */
  buildDownloadMedicationAdministrationHistoryReportDataEntryTooltip: function()
  {
    var view = this.getView();
    return this._buildDateIntervalDataEntryTooltip(
        /**
         * @param {{start: Date, end: Date}} selectedInterval
         */
        function(selectedInterval)
        {
          view.getRestApi().downloadMedicationAdministrationHistory(selectedInterval.start, selectedInterval.end);
        },
        31);
  },

  /**
   * @return {tm.jquery.Tooltip} with {@link app.views.medications.common.overview.NumberDataEntryContainer} as the
   * contents, which uses the inputted number of pages and triggers the browser download of template report as a PDF.
   */
  buildDownloadMedicationAdministrationReportTemplate: function()
  {
    var view = this.getView();
    return this._buildNumberOfPagesDataEntryTooltip(
        /**
         * @param {number} numberOfPages
         */
        function(numberOfPages)
        {
          view.getRestApi().downloadActiveMedicationAdministrationRecordReportTemplate(numberOfPages);
        });
  },

  /**
   * @param {function({start: Date, end: Date})} resultCallback called when the user confirms the selected interval
   * @param {number|undefined} [maximumDuration=undefined]
   * @return {tm.jquery.Tooltip}
   * @private
   */
  _buildDateIntervalDataEntryTooltip: function(resultCallback, maximumDuration)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var entryContainer = new app.views.medications.common.overview.DateIntervalDataEntryContainer({
      view: view,
      maximumDuration: maximumDuration
    });

    var popoverTooltip = appFactory.createDataEntryPopoverTooltip(
        view.getDictionary('select.date'),
        entryContainer,
        function(resultData)
        {
          if (resultData && resultData.isSuccess())
          {
            var value = resultData.value;
            if (!tm.jquery.Utils.isEmpty(value))
            {
              resultCallback({start: value.startDate, end: value.endDate});
            }
          }
        }
    );

    this._enableEntryContainerEscKey(entryContainer, popoverTooltip);
    this._applyDefaultPopoverTooltipOptions(
        popoverTooltip,
        entryContainer.getDefaultWidth(),
        entryContainer.getDefaultHeight());

    return popoverTooltip;
  },

  /**
   * @param {function(number)} resultCallback called when the user confirms the number of pages
   * @return {tm.jquery.Tooltip}
   * @private
   */
  _buildNumberOfPagesDataEntryTooltip: function(resultCallback)
  {
    var view = this.getView();
    var appFactory = view.getAppFactory();

    var entryContainer = new app.views.medications.common.overview.NumberDataEntryContainer({
      view: view
    });

    var popoverTooltip = appFactory.createDataEntryPopoverTooltip(
        view.getDictionary('pages'),
        entryContainer,
        function(resultData)
        {
          if (resultData && resultData.isSuccess())
          {
            resultCallback(resultData.value);
          }
        }
    );

    this._enableEntryContainerEscKey(entryContainer, popoverTooltip);
    this._applyDefaultPopoverTooltipOptions(
        popoverTooltip,
        entryContainer.getDefaultWidth(),
        entryContainer.getDefaultHeight());

    return popoverTooltip;
  },

  /**
   * @param {app.views.common.containers.AppDataEntryContainer|tm.jquery.AbstractComponent} entryContainer
   * @param {tm.jquery.Tooltip} tooltip
   * @private
   */
  _enableEntryContainerEscKey: function(entryContainer, tooltip)
  {
    entryContainer.onKey(
        new tm.jquery.event.KeyStroke({key: 'esc', altKey: false, ctrlKey: false, shiftKey: false}),
        function()
        {
          tooltip.cancel();
        }
    );
  },

  /**
   * @param {tm.jquery.Tooltip} tooltip
   * @param {number} width
   * @param {number} height
   * @private
   */
  _applyDefaultPopoverTooltipOptions: function(tooltip, width, height)
  {
    tooltip.setTrigger('manual');
    tooltip.setWidth(width);
    tooltip.setHeight(height);
    tooltip.setHideOnDocumentClick(false); // issues with the date picker
  }
});