Class.define('app.views.medications.ordering.RoutesPane', 'tm.jquery.Container', {
  /** @type string */
  cls: "routes-container",
  /** @type string */
  overflow: 'visible',
  /** @type app.views.common.AppView */
  view: null,
  /** @type function(Array<app.views.medications.common.dto.MedicationRoute>)|null */
  changeEvent: null,
  /** @type boolean */
  selectionRequired: true,
  /** @type boolean */
  discretionaryRoutesDisabled: true,
  /** @type number */
  maxRouteButtons: 5,
  /** @type number */
  maxRouteCharLength: 37,

  /** @type tm.jquery.ButtonGroup */
  _routesButtonGroup: null,
  /** @type tm.jquery.SelectBox */
  _routesSelectBox: null,
  /** @type Array<app.views.medications.common.dto.MedicationRoute> */
  _previousSelectBoxSelections: null,
  /** @type boolean */
  _largeNumberOfRoutes: false,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._previousSelectBoxSelections = [];
    this.setLayout(new tm.jquery.HFlexboxLayout({gap: 5}));
    this._buildComponents();
    this._buildGui();
  },

  /**
   * Applies the given routes to the UI components and displays them, either with a SelectBox, or by using a
   * RadioButtonGroup, if we determine we have enough horizontal space available.
   * If a default route is passed it will be preselected. If the default route is not explicitly defined, and there's only
   * one available route, that route should be preselected.
   * The call will trigger {@link #changeEvent} if a default selection is made, unless preventEvent is set to true.
   * @param {Array<app.views.medications.common.MedicationRoute>} routes
   * @param {app.views.medications.common.MedicationRoute} defaultRoute
   * @param {Boolean} [preventEvent=false]
   */
  setRoutes: function(routes, defaultRoute, preventEvent) //routes -> medicationRouteDto.java
  {
    if (!defaultRoute && routes.length === 1)
    {
      defaultRoute = routes[0];
    }

    var preselectedRouteIds = this.selectionRequired && defaultRoute ? [defaultRoute.getId()] : [];

    routes.sort(
        function(route1, route2)
        {
          return route1.getName().toLowerCase().localeCompare(route2.getName().toLowerCase());
        });

    this._largeNumberOfRoutes = this._isSelectBoxDisplayModeRequired(routes);
    this._applyAppropriateRouteSelectionDisplayMode();

    if (this._largeNumberOfRoutes)
    {
      this._createRoutesSelectBoxOptions(routes, preselectedRouteIds);
    }
    else
    {
      this._createRoutesButtons(routes, preselectedRouteIds);
    }

    if (!preventEvent && this.changeEvent)
    {
      var selectedRoutes = this.getSelectedRoutes();
      if (selectedRoutes.length > 0)
      {
        this.changeEvent(selectedRoutes)
      }
    }
  },

  /**
   * Returns the selected routes, if any.
   * @return {Array<app.views.medications.common.dto.MedicationRoute>}
   */
  getSelectedRoutes: function()
  {
    if (this._largeNumberOfRoutes)
    {
      return this._routesSelectBox.getSelections();
    }

    // for some reason sometimes the button is pressed, but the validation fails.. returning the first 1 if there's only 1
    var buttons = this._routesButtonGroup.getButtons();
    if (this.selectionRequired && buttons.length === 1)
    {
      return [buttons[0].data];
    }

    return this._routesButtonGroup
        .getSelection()
        .map(function(button)
        {
          return button.data;
        });
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationRoute>|null} routes
   */
  setSelectedRoute: function(routes)
  {
    if (tm.jquery.Utils.isArray(routes) && routes.length > 0)
    {
      var selectedRoutes = [];
      routes.forEach(function(route)
      {
        var routeOption = this._findRouteOptionById(route.getId());
        if (routeOption)
        {
          selectedRoutes.push(this._largeNumberOfRoutes ? routeOption.value : routeOption);
        }
      }, this);

      this._largeNumberOfRoutes ?
          this._routesSelectBox.setSelections(selectedRoutes) :
          this._routesButtonGroup.setSelection(selectedRoutes);
    }
    else
    {
      this.clear();
    }
  },

  /**
   * Returns the array of validations required for this component.
   * @return {Array<tm.jquery.FormField>}
   */
  getRoutesPaneValidations: function()
  {
    var self = this;
    var formFields = [];

    formFields.push(
        new tm.jquery.FormField({
          component: this,
          required: this.selectionRequired === true,
          componentValueImplementationFn: function()
          {
            var selectedRoutes = self.getSelectedRoutes();
            return selectedRoutes.length > 0 ? selectedRoutes : null;
          }
        }));

    return formFields;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  requestFocus: function()
  {
    var buttons = this._routesButtonGroup.getButtons();
    if (buttons.length > 0)
    {
      buttons[0].focus();
    }
  },

  clear: function()
  {
    this._routesButtonGroup.clearSelection(true);
    this._routesSelectBox.deselectAll(true);
    this._previousSelectBoxSelections = [];
  },

  _buildComponents: function()
  {
    var self = this;

    this._routesButtonGroup = new tm.jquery.ButtonGroup({
      testAttribute: 'routes-button-group',
      orientation: "horizontal",
      type: "checkbox"
    });

    this._routesSelectBox = new tm.jquery.SelectBox({
      testAttribute: 'routes-selectbox',
      width: 220,
      liveSearch: true,
      placeholder: this.getView().getDictionary('select.route'),
      dropdownWidth: "stretch",
      dropdownHeight: 7,
      dropdownAlignment: "left",

      options: [],
      selections: [],

      allowSingleDeselect: true,
      multiple: !this.discretionaryRoutesDisabled, /* allow to support discretionaryRoutes */
      defaultValueCompareToFunction: function(value1, value2)
      {
        return (tm.jquery.Utils.isEmpty(value1) ? null : value1.id)
            === (tm.jquery.Utils.isEmpty(value2) ? null : value2.id);
      }

    });

    if (this.changeEvent)
    {
      this._routesButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
      {
        var selectedRoutes = self.getSelectedRoutes();
        self.changeEvent(selectedRoutes)
      });
    }

    this._routesSelectBox.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component)
    {
      if (self.discretionaryRoutesDisabled !== true)
      {
        var selections = component.getSelections();
        var selectionLength = selections.length;

        if (selectionLength === 1)
        {
          self._previousSelectBoxSelections = [selections[0]];
          if (!selections[0].isDiscretionary())
          {
            component.hidePluginDropdown();
          }
        }
        else if (selectionLength >= 1)
        {
          var oldSelectedRoutes = self._previousSelectBoxSelections;
          var oldSelectionLength = oldSelectedRoutes.length;

          if (oldSelectionLength > 0 && oldSelectionLength < selectionLength)
          {
            var newSelectedRoute = null;
            selections.forEach(function(item)
            {
              if (!self._previousSelectBoxSelections.contains(item))
              {
                newSelectedRoute = item;
              }
            });

            var oldContainsDiscretionary = oldSelectedRoutes.some(function(route)
            {
              return route.isDiscretionary();
            });

            if (!newSelectedRoute.isDiscretionary() || (newSelectedRoute.isDiscretionary() && !oldContainsDiscretionary))
            {
              component.deselectAll(true);
              component.setSelections([newSelectedRoute], false);
              self._previousSelectBoxSelections = [newSelectedRoute];
            }
            else
            {
              self._previousSelectBoxSelections = selections;
            }
            if (!newSelectedRoute.isDiscretionary())
            {
              component.hidePluginDropdown();
            }
          }
          else
          {
            self._previousSelectBoxSelections = selections;
          }
        }
      }
      if (self.changeEvent)
      {
        self.changeEvent(self.getSelectedRoutes());
      }
    });
  },

  _buildGui: function()
  {
    this.add(this._routesButtonGroup);
    this.add(this._routesSelectBox);
  },

  /**
   * Takes all the available routes of application and determines if we should use the SelectBox component to display them.
   * This happens either because the total number of routes is larger than the defined {@link #maxRouteButtons}, or because
   * the cumulative number of characters of the route names surpasses the {@link #maxRouteCharLength}.
   * The latter is used as a simplified mechanism of determining the available horizontal space to correctly display the
   * buttons in the RadioButtonGroup component.
   * @param {Array<app.views.medications.common.MedicationRoute>} routes
   * @return {boolean}
   * @private
   */
  _isSelectBoxDisplayModeRequired: function(routes)
  {
    var maxRoutes = this.maxRouteButtons;
    var maxRouteChars = this.maxRouteCharLength;

    var allRoutesStr = "";
    for (var i = 0; i < routes.length; i++)
    {
      allRoutesStr += routes[i].getName();
    }

    return routes.length >= maxRoutes || allRoutesStr.length > maxRouteChars;
  },

  /**
   * When {@link #_largeNumberOfRoutes} is true, hides the {@link #_routesButtonGroup} and displays the
   * {@link #_routesSelectBox}, otherwise defaults back to displaying the {@link #_routesButtonGroup}, which is easier
   * to use.
   * @private
   */
  _applyAppropriateRouteSelectionDisplayMode: function()
  {
    if (this._largeNumberOfRoutes)
    {
      this._routesButtonGroup.isRendered() ? this._routesButtonGroup.hide() : this._routesButtonGroup.setHidden(true);
      this._routesSelectBox.isRendered() ? this._routesSelectBox.show() : this._routesSelectBox.setHidden(false);
    }
    else
    {
      this._routesSelectBox.isRendered() ? this._routesSelectBox.hide() : this._routesSelectBox.setHidden(true);
      this._routesButtonGroup.isRendered() ? this._routesButtonGroup.show() : this._routesButtonGroup.setHidden(false);
    }
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationRoute>} routes
   * @param {Array} [preselectedRouteIds=[]]
   * @private
   */
  _createRoutesSelectBoxOptions: function(routes, preselectedRouteIds)
  {
    preselectedRouteIds = tm.jquery.Utils.isArray(preselectedRouteIds) ? preselectedRouteIds : [];

    var self = this;
    var options = [];
    var preselected = [];

    this._routesSelectBox.removeAllOptions();

    routes.forEach(function(route)
    {
      var preselect = preselectedRouteIds.contains(route.getId());

      var cls = "route-option";
      if (route.isUnlicensedRoute())
      {
        cls += " route-option-unlicensed";
      }
      if (route.isDiscretionary() && self.discretionaryRoutesDisabled !== true)
      {
        cls += " route-option-discretionary";
      }
      if (preselect)
      {
        preselected.push(route);
      }
      options.push(tm.jquery.SelectBox.createOption(route, route.getName(), cls, null, preselect));
    });
    this._routesSelectBox.addOptions(options);
    this._routesSelectBox.setSelections(preselected, true);
    /** Need to manually set {@link #_previousSelectBoxSelections}, since we block the change event, otherwise
     *  the logic fails on the first change by user.  */
    this._previousSelectBoxSelections = this._routesSelectBox.getSelections();
  },

  /**
   * @param {Array<app.views.medications.common.dto.MedicationRoute>} routes
   * @param {Array} preselectedRouteIds
   * @private
   */
  _createRoutesButtons: function(routes, preselectedRouteIds)
  {
    var self = this;
    var view = this.getView();
    var buttons = [];

    routes.forEach(function(route)
    {
      var preselect = preselectedRouteIds.contains(route.getId());
      var tooltipTexts = [];
      var cls = "route-button";
      if (route.isUnlicensedRoute())
      {
        cls += " route-button-unlicensed";
        tooltipTexts.push(view.getDictionary('unlicensed.route'));
      }
      if (route.isDiscretionary() && self.discretionaryRoutesDisabled !== true)
      {
        cls += " route-button-discretionary";
        tooltipTexts.push(view.getDictionary('discretionary.route'));
      }
      var routeButton = new tm.jquery.Button({
        cls: cls,
        data: route,
        text: route.getName(),
        pressed: preselect,
        handler: function(component)
        {
          self._handleRoutesButtonPressed(component)
        }
      });

      if (tooltipTexts.length > 0)
      {
        routeButton.setTooltip(app.views.medications.MedicationUtils.createTooltip(tooltipTexts.join(", "), null, view));
      }
      buttons.push(routeButton);
    });

    this._routesButtonGroup.setButtons(buttons);
  },

  /**
   * @param {tm.jquery.Button} component
   * @private
   */
  _handleRoutesButtonPressed: function(component)
  {
    var self = this;
    var discretionary = component.data.discretionary === true && self.discretionaryRoutesDisabled !== true;
    if (component.pressed === true && !discretionary)
    {
      this._routesButtonGroup.setSelection([component], true);
    }
    else if (component.pressed === true && discretionary)
    {
      this._routesButtonGroup.getSelection().forEach(function(item)
      {
        if (item.data.discretionary !== true)
        {
          self._routesButtonGroup.setSelection([component], true);
        }
      });
    }
  },

  /**
   * @param {Number} routeId
   * @returns {tm.jquery.selectbox.Option|tm.jquery.Button|undefined}
   * @private
   */
  _findRouteOptionById: function(routeId)
  {
    return this._largeNumberOfRoutes === true ?
        this._routesSelectBox
            .getOptions()
            .find(function(option)
            {
              return option.value.id === routeId;
            }) :
        this._routesButtonGroup
            .getButtons()
            .find(function(button)
            {
              return button.data.id === routeId;
            });
  }
});

