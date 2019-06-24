Class.define('app.views.medications.common.MedicationSearchField', 'tm.jquery.TreeTypeaheadField', {
  /** @type string */
  cls: "medication-search-field",
  /** @type app.views.common.AppView */
  view: null,
  /** @type Array<string>|null */
  additionalFilter: null,
  /** @type app.views.medications.common.dto.Medication|null */
  limitSimilarMedication: null,
  /** @type boolean */
  forceToggleButton: false,
  /** @type app.views.medications.common.MedicationSearchResultFormatter (optional) */
  searchResultFormatter: null,

  /* The following are tm.jquery.TreeTypeaheadField property overrides */
  /** @type number */
  minLength: 3,
  /** @type boolean */
  treeWideMode: true,
  /** @type boolean */
  treeShowIcons: false,
  /** @type string */
  mode: 'advanced',
  /** @type boolean */
  clearable: false,

  /** @type tm.jquery.ToggleButton|null */
  _showNonFormularyMedicationButton: null,
  /** @type string */
  _inputBorderCls: 'input-append',
  /** @type boolean */
  _destroyed: false,

  Constructor: function()
  {
    this.callSuper();

    if (!this.searchResultFormatter)
    {
      this.searchResultFormatter = new app.views.medications.common.MedicationSearchResultFormatter();
    }

    // Make sure the toggle button is always created, so we can switch modes dynamically.
    // It's either this way or altering the config before calling callSuper.
    if (!this._toggleButton)
    {
      this._toggleButton = new tm.jquery.ToggleButton({
        cls: 'toggle-button',
        html: '<span class="caret"></span>',
        hidden: !this.isLimitSimilarMedication() && !this.isForceToggleButton()
      });
      this.setAppend(this._toggleButton);
    }
    else
    {
      this._toggleButton.setHidden(!this.isLimitSimilarMedication() && !this.isForceToggleButton());
    }

    // support setting medication as the selection directly.
    this.selection = this.selection && this.selection instanceof app.views.medications.common.dto.Medication ?
        this._convertMedicationToTreeNode(this.selection) :
        this.selection;

    this.footer = this._buildFooter();
    this.dataLoader = this._buildDataLoader();
    this.renderTitleCallback = this.onRenderTitle.bind(this);
  },

  /**
   * @param {app.views.medications.common.dto.Medication|null} medication
   */
  setLimitBySimilar: function(medication)
  {
    this.limitSimilarMedication = medication;
    this.dataLoader = this._buildDataLoader();
    this._applyToggleButtonVisibility(!!medication || this.isForceToggleButton());
    this.setEnabled(this.isEnabled()); // prevent free input
  },

  /**
   * Override to support setting Medication as the selection directly. Be advised that
   * getSelection will return tm.jquery.tree.Node where the data is a simple json due to the
   * FancyTreePlugin implementation.
   * @param selection (app.views.medications.common.dto.Medication|tm.jquery.tree.Node)
   * @param preventEvent (optional)
   * @param preventValueChange (optional)
   */
  setSelection: function(selection, preventEvent, preventValueChange)
  {
    if (selection instanceof app.views.medications.common.dto.Medication)
    {
      selection = this._convertMedicationToTreeNode(selection);
    }

    this.callSuper(selection, preventEvent, preventValueChange);
  },

  /**
   * Clears the component and resets the formulary filter state
   */
  clear: function()
  {
    this.setSelection(null, true);
    if (!!this._showNonFormularyMedicationButton)
    {
      this._showNonFormularyMedicationButton.setPressed(false, true);
      this._showNonFormularyMedicationButton.setText(this.view.getDictionary("show.non.formulary"));
      this._clearActiveSearchQuery();
    }
  },

  /**
   * Helper method to get the selected medication from the selected node, if any.
   * @returns {app.views.medications.common.dto.Medication|null}
   */
  getSelectionMedication: function()
  {
    var selection = this.getSelection();
    if (selection)
    {
      // don't use instanceof, due to json cloning from TreeTypeAhead breaking instanceof, and
      // using the broken object to create a new object seems to break the new object properties
      if (selection.getData() && !JS.isType(selection.getData(), app.views.medications.common.dto.Medication))
      {
        return new app.views.medications.common.dto.Medication(selection.getData());
      }
      return selection.getData();
    }
    return null;
  },

  /**
   * @return {boolean} true, if the search results should be filtered to only show formulary medications, otherwise
   * false. Returns true only if the formulary filter is enabled by system wide settings and the user either
   * has no permissions to turn it off or it's active based on the state of {@link #_showNonFormularyMedicationButton}.
   */
  isLimitByFormulary: function()
  {
    return (this.view.isFormularyFilterEnabled() &&
        (!this.view.getTherapyAuthority().isNonFormularyMedicationSearchAllowed()
            || (this._showNonFormularyMedicationButton && !this._showNonFormularyMedicationButton.isPressed())));

  },

  /**
   * @return {boolean} true, if the visibility of the toggle button is forced by component configuration, regardless
   * of using the {@link #setLimitBySimilar} mode.
   */
  isForceToggleButton: function()
  {
    return this.forceToggleButton === true;
  },

  /**
   * Sets a new data loading method. Normally the default implementation, which retrieves the results from the server,
   * should suffice. Keep in mind that calling {@link #setLimitBySimilar} will reset the data loader to an internal
   * implementation.
   * @param {function(component: app.views.medications.common.MedicationSearchField, requestParams:{searchQuery: string},
   * processCallback: function)} dataLoader
   */
  setDataLoader: function(dataLoader)
  {
    this.limitSimilarMedication = null;
    this.dataLoader = dataLoader;
  },

  abortUnlicensedMedicationSelection: function()
  {
    var self = this;

    this.view.getRestApi().loadUnlicensedMedicationWarning().then(function(warning)
    {
      self._warnAndAbortUnlicensedMedicationSelection(warning);
    })
  },

  /**
   * Override, remove the added input-append class from the root div if the toggle button is not visible to prevent
   * sharp borders instead of round ones.
   * @returns {Element}
   */
  createDom: function()
  {
    var div = this.callSuper();

    if (this._toggleButton.isHidden())
    {
      $(div).removeClass(this._inputBorderCls);
    }

    return div;
  },

  /**
   * Override the logic and keep the toggle button enabled if the similar medication limit is set. Also applies
   * the disabled status to the input component to prevent free text searches in such a mode, regardless if
   * the component is set enabled.
   * @param enabled
   */
  applyEnabled: function(enabled)
  {
    this.callSuper(enabled && !this.isLimitSimilarMedication());

    if (this.getAppend() instanceof tm.jquery.Button)
    {
      this.getAppend().setEnabled(this.isLimitSimilarMedication() || enabled);
    }
  },

  /**
   * @protected
   * @returns {boolean}
   */
  isLimitSimilarMedication: function()
  {
    return !tm.jquery.Utils.isEmpty(this.limitSimilarMedication);
  },

  /**
   * Creates the HTML for an individual search result node, with the purpose to allow additional information to be added to
   * the title via the {@link searchResultFormatter}. Code taken from FancyTree's {@link #nodeRenderTitle}. Keep in mind the
   * node data is a plain json since we don't convert to jsClasses when we load from the API to increase performance in case
   * of larger results. So we can't use getters or instanceof checks.
   * @param {{component: tm.jquery.Tree}} component
   * @param {{node: {key: string, title: string, data: object} , options: {object}}} eventData
   * @return {string} HTML for the single node.
   * @protected
   */
  onRenderTitle: function(component, eventData)
  {
    var node = eventData.node;
    var aria = eventData.options.aria;

    // The following removes the ability for the user to select the 'Loading...' node, which causes a chain of errors due to
    // the original component not preventing such a selection. Optimally, this should be fixed by the Think!Clinical team in
    // the original component. @see https://jira.marand.si/browse/TMEDS-2072
    if (node && node.key === '_statusNode')
    {
      node.unselectable = true;
    }

    var tooltip = node.tooltip ? ' title="' + tm.jquery.Utils.escapeHtml(node.tooltip) + '"' : '';
    var id = aria ? ' id="ftal_' + node.key + '"' : '';
    var role = aria ? ' role="treeitem"' : '';
    var tabindex = ' tabindex="0"';
    // skipping the title formatting for status nodes (e.g. 'loading...')
    var title = !node.statusNodeType ? this.searchResultFormatter.createTitle(node) : node.title;

    return '<span ' + role + ' class="fancytree-title"' + id + tooltip + tabindex + '>' + title + '</span>';
  },

  /**
   * @see app.views.medications.common.MedicationSearchField#_doFieldKeyUpEventHandler
   * @override
   */
  destroy: function()
  {
    this.callSuper();
    this._destroyed = true;
  },

  _buildFooter: function()
  {
    var view = this.view;
    var self = this;
    if (view.isFormularyFilterEnabled() && view.getTherapyAuthority().isNonFormularyMedicationSearchAllowed())
    {
      this._showNonFormularyMedicationButton = new tm.jquery.ToggleButton({
        cls: "toggle-formulary-button",
        type: 'link',
        text: view.getDictionary("show.non.formulary"),
        pressed: false,
        handler: function(component, componentEvent, elementEvent)
        {
          elementEvent.stopPropagation();
          self._refreshDropdownContent();
          component.setText(view.getDictionary(component.isPressed() ? "hide.non.formulary" : "show.non.formulary"));
        }
      });

      var dropdownFooterContainer = new tm.jquery.Container({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
      });
      dropdownFooterContainer.add(this._showNonFormularyMedicationButton);

      return dropdownFooterContainer;
    }

    return null;
  },

  /**
   * @return {function}
   * @private
   */
  _buildDataLoader: function()
  {
    var self = this;
    var view = this.view;

    return function(component, requestParams, processCallback)
    {
      if (!self.isLimitSimilarMedication())
      {
        view.getRestApi().loadMedications(
            requestParams.searchQuery, this._buildAdditionalRequestParams(), true).then(
            function onResult(data)
            {
              processCallback(data, requestParams);
            });
      }
      else
      {
        view.getRestApi().loadSimilarMedications(
            self.limitSimilarMedication.getId(),
            true).then(
            function onResult(data)
            {
              processCallback(data, requestParams);
            });
      }
    }
  },

  /**
   * @returns {Array<String>}
   * @protected
   */
  _buildAdditionalRequestParams: function()
  {
    var filterEnums = app.views.medications.TherapyEnums.medicationFinderFilterEnum;
    var filterList = [];

    if (!tm.jquery.Utils.isEmpty(this.additionalFilter))
    {
      filterList.push(this.additionalFilter);
    }

    if (this.isLimitByFormulary())
    {
      filterList.push(filterEnums.FORMULARY);
    }

    return filterList;
  },

  /**
   * The contents of this method were copied directly from {@link tm.jquery.TreeTypeaheadField}, with the sole purpose of
   * avoiding calls to private methods of the base jsClass (which works in the world of JS, but is bad design).
   * @private
   */
  _doTreeRefresh: function()
  {
    if (tm.jquery.Utils.isEmpty(this._activeSearchQuery) || this._activeSearchQuery !== this.getValue())
    {
      if (tm.jquery.Utils.isEmpty(this._$treeContainerDialog) === false)
      {
        this._tree.restore();
      }
    }
  },

  /**
   * A hopefully temporary hack, until the Think!Clinical team updates the original component. It was added due to the
   * fact that the original method in {@link tm.jquery.TreeTypeaheadField} is called inside a timeout, by the Tree's
   * 'EVENT_TYPE_TREE_LOAD_COMPLETE' event handler. That fact alone opens up the possibility that the '_$dialogPlaceholder'
   * property is null when the method is actually executed. One such possible case is if the 'destroy' method is called in
   * between. It was reproducible by using the emulated 'fast 3g' network speed and closing the order dialog after the
   * component showed the 'loading' mask in it's dropdown menu.
   * @see https://jira.marand.si/browse/TMEDS-2072
   * @private
   */
  _doDialogResize: function()
  {
    if (!this._$dialogPlaceholder)
    {
      return;
    }
    this.callSuper();
  },

  /**
   * Another, hopefully temporary hack, until the Think!Clinical team updates the original component. It was added due to the
   * numerous (cascading) errors being thrown in scenarios where the user would close the parent (order) dialog while the
   * component attempted to display (updated) search results.  It was reproducible by using the emulated 'fast 3g' network
   * speed and closing the order dialog after the component showed the 'loading' mask in it's dropdown menu. Initially
   * we attempted to extend the _doDialogOpen (attempts to access null _$dialogPlaceholder inside setTimeout) and the
   * _isDialogOpen ("cannot call methods on dialog prior to initialization; attempted to call method 'isOpen'") with
   * little success. Preventing any further key event reactions after the component is destroyed seems to solve them all.
   * @see https://jira.marand.si/browse/TMEDS-2072
   * @private
   */
  _doFieldKeyUpEventHandler: function()
  {
    if (this._destroyed)
    {
      return;
    }
    this.callSuper();
  },

  /**
   *
   * @param {app.views.medications.common.dto.Medication} medication
   * @private
   */
  _convertMedicationToTreeNode: function(medication)
  {
    return medication ? new tm.jquery.tree.Node({
      key: medication.getId() ? medication.getId() : ('UNCODED' + medication.hash()),
      title: medication.getDisplayName(),
      data: medication
    }) : medication;
  },

  /**
   * Forces a redraw of the dropdown content. Don't call this method if you aren't sure the component won't be removed from
   * the screen in the near future as it's an async operation inside {@link tm.jquery.TreeTypeaheadField}, resulting in
   * an timeout execute of {@link tm.jquery.TreeTypeaheadField#_doDialogResize}, which assumes the dialog placeholder HTML
   * element is still present in the DOM!
   * @private
   */
  _refreshDropdownContent: function()
  {
    this._clearActiveSearchQuery();
    this._doTreeRefresh();
  },

  /**
   * Clears the currently used search query value, which in turn acts as a soft reset for the contents of the tree plugin,
   * resulting in a content redraw before displaying the next results.
   * @private
   */
  _clearActiveSearchQuery: function()
  {
    this._activeSearchQuery = null;
  },

  /**
   * @param {boolean} value
   */
  _applyToggleButtonVisibility: function(value)
  {
    if (this.isRendered())
    {
      value ? this._toggleButton.show() : this._toggleButton.hide();
    }
    else
    {
      this._toggleButton.setHidden(!value);
    }

    if (this.dom)
    {
      var $dom = $(this.dom);
      if (value)
      {
        if (!$dom.hasClass(this._inputBorderCls))
        {
          $dom.addClass(this._inputBorderCls);
        }
      }
      else
      {
        $dom.removeClass(this._inputBorderCls)
      }
    }
  },

  /**
   * @param {String} warning
   * @private
   */
  _warnAndAbortUnlicensedMedicationSelection: function(warning)
  {
    var self = this;
    var dialog = this.view.getAppFactory().createWarningSystemDialog(warning, 600, 280);

    dialog.on(tm.jquery.ComponentEvent.EVENT_TYPE_DIALOG_HIDE, function()
    {
      self.setValue(null, true);
      self.focus();
    });

    dialog.show();
  }
});
