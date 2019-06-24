Class.define('app.views.medications.ordering.SearchContainer', 'tm.jquery.Container', {
  /** @type string */
  scrollable: 'visible',
  /** @type tm.jquery.common.AppView */
  view: null,
  /** @type function(app.views.medications.common.dto.MedicationData) */
  medicationSelectedEvent: null,
  /** @type Array<string>|null */
  additionalFilter : null,
  /** @type boolean */
  universalOrderFormAvailable: true,
  /** @type app.views.medications.common.MedicationSearchResultFormatter|null */
  searchResultFormatter: null,

  /** @type tm.jquery.Image */
  _universalOrderButton: null,
  /** @type app.views.medications.common.MedicationSearchField */
  _searchField: null,
  /** @type app.views.medications.common.testing.RenderCoordinator */
  _testRenderCoordinator: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this.displayProvider = new app.views.medications.common.therapy.TherapyContainerDisplayProvider({view: config.view});
    this._buildComponents();
    this._buildGui();

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'ordering-search-container',
      view: this.view,
      component: this,
      manualMode: true
    });

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self.view.getAppFactory().createConditionTask(
          function()
          {
            self._searchField.focus();
            self._testRenderCoordinator.insertCoordinator();
          },
          function(task)
          {
            if(!self.isRendered())
            {
              task.abort()
            }
            return self.isRendered(true);
          },
          1000, 50
      );
    });
  },

  abortUnlicensedMedicationSelection: function()
  {
    this._searchField.abortUnlicensedMedicationSelection();
  },

  clear: function()
  {
    this._searchField.clear();
    this._searchField.focus();
  },

  /**
   * Defined via the constructor config property.
   * @return {boolean}
   * @protected
   */
  isUniversalOrderFormAvailable: function()
  {
    return this.universalOrderFormAvailable === true;
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    var view = this.view;
    var appFactory = view.getAppFactory();

    this._searchField = new app.views.medications.common.MedicationSearchField({
      cls: "medication-search-field",
      placeholder: view.getDictionary("enter.three.chars.to.search.medication") + "...",
      view: view,
      width: this.isUniversalOrderFormAvailable() ? 650 : 679, // 20px on each side if no button is present
      dropdownAppendTo: view.getAppFactory().getDefaultRenderToElement(), /* due to the dialog use */
      dropdownHorizontalAlignment: "auto",
      dropdownVerticalAlignment: "auto",
      dropdownWidth: 642,
      dropdownMaxWidth: 642,
      dropdownHeight: "auto",
      dropdownMaxHeight: 400,
      additionalFilter: this.additionalFilter,
      searchResultFormatter: this.searchResultFormatter
    });

    this._searchField.on(tm.jquery.ComponentEvent.EVENT_TYPE_SELECT, function(component)
    {
      var medication = component.getSelectionMedication();
      if (medication)
      {
        self._readMedicationData(medication.getId());
      }
    });

    var popupMenu = appFactory.createPopupMenu();
    popupMenu.addTestAttribute('universal-order-popup-menu');

    popupMenu.addMenuItem(new tm.jquery.MenuItem({
      testAttribute: 'universal-order-menu-item',
      cls: "open-universal-ordering",
      text: view.getDictionary("universal.form"),
      handler: function()
      {
        self._openUniversalMedicationDataDialog()
      },
      iconCls: 'icon-add-universal'
    }));

    if (this.isUniversalOrderFormAvailable())
    {
      this._universalOrderButton = new tm.jquery.Image({
        testAttribute: 'universal-order-menu-button',
        cls: 'icon-show-universal-ordering',
        width: 46,
        height: 34,
        cursor: 'pointer'
      });
      this._universalOrderButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK,
          function(component, componentEvent, elementEvent)
          {
            if (component.isEnabled())
            {
              popupMenu.show(elementEvent);
            }
          });
    }
  },

  _buildGui: function()
  {
    var searchFieldContainer = new tm.jquery.Container({
      scrollable: 'visible',
      cls: "search-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0)
    });
    searchFieldContainer.add(this._searchField);
    if (!!this._universalOrderButton)
    {
      searchFieldContainer.add(this._universalOrderButton);
    }

    this.add(searchFieldContainer);
  },

  /**
   * @param {string} medicationId
   * @private
   */
  _readMedicationData: function(medicationId)
  {
    var self = this;
    this.view.getRestApi().loadMedicationData(medicationId).then(function onDataLoad(medicationData)
    {
      self.medicationSelectedEvent(medicationData);
    });
  },

  _openUniversalMedicationDataDialog: function()
  {
    var self = this;
    var view = this.view;
    var appFactory = view.getAppFactory();

    var universalMedicationDataContainer = new app.views.medications.ordering.UniversalMedicationDataContainer({
      view: view
    });
    var universalMedicationDataDialog = appFactory.createDataEntryDialog(
        view.getDictionary("universal.form"),
        null,
        universalMedicationDataContainer,
        function(resultData)
        {
          if (resultData)
          {
            self.medicationSelectedEvent(resultData.value);
          }
        },
        468,
        300
    );
    universalMedicationDataDialog.addTestAttribute('universal-order-dialog');
    if (view.getDoseForms().length === 0)
    {
      view.loadDoseForms();
    }
    universalMedicationDataDialog.show();
  }
});
