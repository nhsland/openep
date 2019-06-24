Class.define('app.views.medications.mentalHealth.T2T3OrderingContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "medications-ordering-container",
  padding: 0,

  view: null,
  patientId: null,

  orderingContainer: null,
  basketContainer: null,

  validationForm: null,

  defaultWidth: null,
  defaultHeight: null,
  reportType: null,

  _basketTherapyDisplayProvider: null,
  _orderTherapyDisplayProvider: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._basketTherapyDisplayProvider = this._createBasketContainerDisplayProvider();
    this._orderTherapyDisplayProvider = this._createOrderingContainerDisplayProvider();

    this._buildGui();
    this._configureValidationForm();
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));

    this.orderingContainer = this.buildOrderingContainer();
    this.basketContainer = this.buildBasketContainer();

    var mainContainer = new tm.jquery.Container({
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    mainContainer.add(this.orderingContainer);
    var eastContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    eastContainer.add(this.basketContainer);
    mainContainer.add(eastContainer);
    this.add(mainContainer);
  },

  _configureValidationForm: function()
  {
    var self = this;
    var form = new tm.jquery.Form({
      view: self.getView(),
      requiredFieldValidatorErrorMessage: self.getView().getDictionary(
          "value.maximum.recommended.dose.between.interval.warning")
    });

    var maxDoseContainer = self.getBasketContainer().getMaxDoseContainer();

    form.addFormField(new tm.jquery.FormField({
      component: maxDoseContainer.getMaxDoseTextField(),
      required: true,
      label: maxDoseContainer.getMaxDoseLabel(),
      componentValueImplementationFn: function()
      {
        var value = maxDoseContainer.getResult();
        var isNumeric = tm.jquery.Utils.isNumeric(value);
        var isEmpty = tm.jquery.Utils.isEmpty(value) || 0 === value.length;

        if (isEmpty || (isNumeric && value >= 100 && value <= 200))
        {
          return true;
        }
        return null;
      }
    }));

    this.validationForm = form;
  },

  /**
   * @param {app.views.medications.mentalHealth.TherapyOrder} order
   * @private
   */
  _addToBasket: function(order)
  {
    this.getBasketContainer().addTherapy(order);
  },

  /**
   * @param {Array<app.views.medications.mentalHealth.TherapyOrder>} therapyOrders
   * @private
   */
  _handleTherapiesRemovedEvent: function(therapyOrders)
  {
    this.getOrderingContainer().handleBasketTherapiesRemoved(
        therapyOrders.filter(
            function isGroupPanelElement(orderData)
            {
              return !!orderData.getGroup();
            }));
  },

  _onEditMedicationRoute: function(therapy, routes, callback)
  {
    this.selectMedicationRoute(therapy, routes, callback);
  },

  /**
   * @return {app.views.medications.common.therapy.TherapyContainerDisplayProvider} instance used on the left side of
   * the dialog (group panel therapies).
   * @private
   */
  _createOrderingContainerDisplayProvider: function()
  {
    var self = this;
    return new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
      view: this.getView(),
      showTherapyExpiring: false,
      // minor hack - disable all since the instance of the data object inside each container does not derive from Therapy
      showChangeHistory: false,
      showChangeReason: false,
      showMaxDose: false,
      /**
       * @param {app.views.medications.mentalHealth.MentalHealthTherapyContainerData|
       * app.views.medications.common.therapy.AbstractTherapyContainerData} containerData
       * @return {{background: *, layers: Array}}
       */
      getBigIconContainerOptions: function(containerData)
      {
        return {
          background: self._createDefaultBigIconBackground(containerData, this),
          layers: [{
            hpos: "right",
            vpos: "bottom",
            cls: this.getStatusIcon(containerData)
          }]
        };
      }
    });
  },

  /**
   * @return {app.views.medications.common.therapy.TherapyContainerDisplayProvider} instance used on the right side
   * of the dialog, for the content of the order basket.
   * @private
   */
  _createBasketContainerDisplayProvider: function()
  {
    var self = this;
    return new app.views.medications.common.therapy.TherapyContainerDisplayProvider({
      view: this.getView(),
      showTherapyExpiring: false,
      // minor hack - disable all since the instance of the data object inside each container does not derive from Therapy
      showChangeHistory: false,
      showChangeReason: false,
      showMaxDose: false,
      getStatusIcon: function() // don't show different statuses in basket container
      {
        return null;
      },
      getStatusClass: function() // don't show different statuses in basket container
      {
        return "normal";
      },
      /**
       * @param {app.views.medications.mentalHealth.MentalHealthTherapyContainerData|
       * app.views.medications.common.therapy.AbstractTherapyContainerData} containerData
       * @return {{background: *, layers: Array}}
       */
      getBigIconContainerOptions: function(containerData)
      {
        return {
          background: self._createDefaultBigIconBackground(containerData, this),
          layers: []
        };
      }
    });
  },

  /**
   * @param {app.views.medications.mentalHealth.MentalHealthTherapyContainerData|
   * app.views.medications.common.therapy.AbstractTherapyContainerData} containerData
   * @param {app.views.medications.common.therapy.TherapyContainerDisplayProvider} displayProvider
   * @returns {*}
   * @private
   */
  _createDefaultBigIconBackground: function(containerData, displayProvider) // MentalHealthTemplateDto.java / MentalHealthMedicationDto.java
  {
    if (containerData.getGroup() === app.views.medications.TherapyEnums.mentalHealthGroupEnum.TEMPLATES)
    {
      return {cls: this._getMentalHealthTemplateIcon(containerData.getTherapy())};
    }

    var therapyDto = new app.views.medications.common.dto.Therapy();
    therapyDto.setRoutes([containerData.getTherapy().getMentalHealthMedication().getRoute()]);
    return {cls: displayProvider.getTherapyIcon(therapyDto)};
  },

  /**
   * @param {app.views.medications.mentalHealth.dto.MentalHealthTemplate} template
   * @return {string} class name
   * @private
   */
  _getMentalHealthTemplateIcon: function(template)
  {
    var route = template.getRoute();
    if (!tm.jquery.Utils.isEmpty(route) && route.code === 31) // Oral
    {
      return "mental-health-list-oral-icon";
    }
    else
    {
      return "mental-health-list-all-icon";
    }
  },

  /**
   *
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @private
   */
  _handleMedicationSelected: function(medicationData)
  {
    var self = this;
    if (!tm.jquery.Utils.isEmpty(medicationData))
    {
      var therapy = new app.views.medications.mentalHealth.dto.MentalHealthTherapy({
        mentalHealthMedicationDto: new app.views.medications.mentalHealth.dto.MentalHealthMedication({
          id: medicationData.getMedication().getId(),
          name: medicationData.getMedication().getName()
        }),
        genericName: medicationData.getMedication().getGenericName()
      });

      var routes = medicationData.getRoutes();
      if (routes.length > 1) // open route selection container
      {
        this.selectMedicationRoute(
            therapy,
            routes,
            function(result)
            {
              if (!tm.jquery.Utils.isEmpty(result) && !tm.jquery.Utils.isEmpty(result.id))
              {
                therapy.changeRoute(result, self.getView());
              }
              self._addToBasket(
                  new app.views.medications.mentalHealth.TherapyOrder({
                    group: app.views.medications.TherapyEnums.mentalHealthGroupEnum.NEW_MEDICATION,
                    therapy: therapy
                  }));
            });
      }
      else
      {
        therapy.changeRoute(routes[0], self.getView());
        self._addToBasket(
            new app.views.medications.mentalHealth.TherapyOrder({
              group: app.views.medications.TherapyEnums.mentalHealthGroupEnum.NEW_MEDICATION,
              therapy: therapy
            }));
      }
    }
  },

  /**
   * Attempts to add the given TherapyContainerData to the order basket. Items already present in the basket will be ignored.
   * @param {Array<app.views.medications.mentalHealth.MentalHealthTherapyContainerData|
   * app.views.medications.common.therapy.AbstractTherapyContainerData>} containerData
   * @return {Array<app.views.medications.mentalHealth.MentalHealthTherapyContainerData>} that were successfully added
   * to the basket.
   * @private
   */
  _addTherapyContainerDataToBasket: function(containerData)
  {
    var dataNotInBasket =
        containerData.filter(
            function(item)
            {
              return !this.getBasketContainer().containsOrderOf(item);
            },
            this);

    dataNotInBasket.forEach(
        function(item)
        {
          this._addToBasket(new app.views.medications.mentalHealth.TherapyOrder({
              sourceId: item.getId(),
              group: item.getGroup(),
              therapy: item.getTherapy().clone(true)
            }));
        },
        this);

    return dataNotInBasket;
  },

  getDefaultWidth: function()
  {
    return tm.jquery.Utils.isEmpty(this.defaultWidth) ? $(window).width() - 50 : this.defaultWidth;
  },

  getDefaultHeight: function()
  {
    return tm.jquery.Utils.isEmpty(this.defaultHeight) ? $(window).height() - 10 : this.defaultHeight;
  },

  buildOrderingContainer: function()
  {
    var view = this.getView();

    return new app.views.medications.mentalHealth.T2T3TherapySelectionColumn({
      view: view,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, app.views.medications.ordering.OrderingContainer.DEFAULT_WIDTH),
      displayProvider: this._orderTherapyDisplayProvider,
      addTherapyContainerDataToBasket: this._addTherapyContainerDataToBasket.bind(this),
      onMedicationSelected: this._handleMedicationSelected.bind(this)
    });
  },

  buildBasketContainer: function()
  {
    var self = this;
    var view = this.getView();

    var headerTitle = this.reportType === app.views.medications.TherapyEnums.mentalHealthDocumentType.T2
        ? view.getDictionary("t2.prescription")
        : view.getDictionary("t3.prescription");

    return new app.views.medications.mentalHealth.T2T3BasketContainer({
      view: view,
      headerTitle: headerTitle,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      displayProvider: this._basketTherapyDisplayProvider,
      therapiesRemovedEventCallback: function(removedElementsData)
      {
        self._handleTherapiesRemovedEvent(removedElementsData);
      },
      editMedicationRouteFunction: function(therapy, routes, callback)
      {
        self._onEditMedicationRoute(therapy, routes, callback);
      }
    });
  },

  /**
   * @param {Array<app.views.medications.mentalHealth.TherapyOrder|
   * app.views.medications.common.therapy.AbstractTherapyContainerData>} content
   * @param {function} successCallback
   * @param {function} failureCallback
   */
  saveOrder: function(content, successCallback, failureCallback)
  {
    var self = this;
    var view = this.getView();

    var mentalHealthDrugs = [];
    var mentalHealthReports = [];
    var maxDosePercentage = this.getBasketContainer().getMaxDoseContainer().getResult();
    var mentalHealthDocumentType = self.reportType;

    content
        .map(function asOrderDetails(orderData)
        {
          return orderData.getOrderDetails();
        })
        .forEach(function sortMedicationsAndTemplates(orderDetails)
        {
          if (orderDetails.medication)
          {
            mentalHealthDrugs.push(orderDetails.medication);
          }
          if (orderDetails.template)
          {
            mentalHealthReports.push(orderDetails.template);
          }
        });

    var mentalHealthDocument = new app.views.medications.mentalHealth.dto.MentalHealthDocument({
      patientId: view.getPatientId(),
      mentalHealthDocumentType: mentalHealthDocumentType,
      maxDosePercentage: maxDosePercentage.length === 0 ? null : maxDosePercentage,
      mentalHealthMedicationDtoList: mentalHealthDrugs,
      mentalHealthTemplateDtoList: mentalHealthReports
    });

    view.getRestApi()
        .saveMentalHealthDocument(mentalHealthDocument)
        .then(
            function onMentalHealthDocumentSaved()
            {
              view.refreshTherapies(true);
              successCallback();
            },
            function onMentalHealthDocumentSaveError()
            {
              failureCallback();
            });
  },

  processResultData: function(resultDataCallback)
  {
    var self = this;
    var form = this.getValidationForm();
    var failResultData = new app.views.common.AppResultData({success: false});
    var successResultData = new app.views.common.AppResultData({success: true});

    form.setOnValidationSuccess(function()
    {
      var content = self.getBasketContainer().getContent();
      if (content != null && content.length > 0)
      {
        self.saveOrder(content, function()
            {
              resultDataCallback(successResultData);
            },
            function()
            {
              resultDataCallback(failResultData);
            });
      }
      else
      {
        resultDataCallback(failResultData);
      }
    });

    form.setOnValidationError(function()
    {
      resultDataCallback(failResultData);
    });

    form.submit();
  },

  /**
   * @param {app.views.medications.mentalHealth.dto.MentalHealthTherapy} therapy
   * @param {Array<app.views.medications.common.dto.MedicationRoute>} routes
   * @param {function} resultCallback
   */
  selectMedicationRoute: function(therapy, routes, resultCallback)
  {
    var self = this;

    var routeCount = 0;
    var charLength = 0;
    var linesNumber = 1;

    routes.forEach(function(item)
    {
      charLength += item.name.length;
      routeCount++;

      if (routeCount > 5 || charLength > 50)
      {
        linesNumber++;
        charLength = 0;
        routeCount = 0;
      }

      item.lineNumber = linesNumber;
    });

    var title = self.getView().getDictionary("route.selection");
    var height = linesNumber * 50 + 40 + 30 + 20; // header height + medication name label height + margin (10 + 10)
    var width = 500;

    if (linesNumber === 1)
    {
      var buttonsLength = charLength * 9 + routeCount * 40 + 20;
      var tittleLength = title.length * 9 + 80;

      width = Math.max(buttonsLength, tittleLength);
    }

    var therapyDisplayLength = therapy.getNameDisplayLength();
    if (therapyDisplayLength > width)
    {
      width = therapyDisplayLength;
    }

    var view = this.getView();
    var dialog = view.getAppFactory().createDefaultDialog(
        title,
        null,
        new app.views.medications.mentalHealth.RouteSelectionContainer({
          view: view,
          routes: routes,
          linesNumber: linesNumber,
          therapy: therapy,
          formattedTherapyDisplay: therapy.getFormattedTherapyDisplay(),
          scrollable: 'vertical',
          resultCallback: function(data)
          {
            resultCallback(data);
            dialog.hide();
          }
        }),
        null,
        width, height
    );

    dialog.header.setCls("therapy-admin-header"); // new iOS look
    dialog.show();
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {tm.jquery.Form}
   */
  getValidationForm: function()
  {
    return this.validationForm;
  },

  /**
   * @return {app.views.medications.mentalHealth.T2T3BasketContainer}
   * @protected
   */
  getBasketContainer: function()
  {
    return this.basketContainer;
  },

  /**
   * @return {app.views.medications.mentalHealth.T2T3TherapySelectionColumn}
   * @protected
   */
  getOrderingContainer: function()
  {
    return this.orderingContainer;
  }
});
