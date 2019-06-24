Class.define('app.views.medications.ordering.templates.TemplatesContainer', 'tm.jquery.Container', {
  cls: "templates-container",
  scrollable: "visible",
  /** @type /** @type app.views.common.AppView */
  view: null,
  /** @type function(Array<app.views.medications.ordering.TherapyOrder>) */
  addTemplateTherapyOrdersCallback: null,
  /** @type function(app.views.medications.common.therapy.TherapyContainer) */
  addTemplateTherapyOrderWithEditCallback: null,
  /** @type string of {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum.INPATIENT} */
  templateContext: app.views.medications.TherapyEnums.therapyTemplateContextEnum.INPATIENT,
  /** @type app.views.medications.common.patient.AbstractReferenceData */
  referenceData: null,
  /** @type app.views.medications.ordering.OrderingBehaviour */
  orderingBehaviour: null,
  /** privates*/
  /** @type Array<app.views.medications.ordering.dto.TherapyTemplates>|null */
  templates: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!this.referenceData)
    {
      throw Error('referenceData is undefined.');
    }
    if (!this.orderingBehaviour)
    {
      throw Error('orderingBehaviour is undefined.');
    }

    var self = this;
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self.reloadTemplates();
    });
  },

  /**
   * Reloads the templates from the API, attaches the default information source to each element, and displays the
   * templates.
   */
  reloadTemplates: function()
  {
    var self = this;
    this._loadTemplates()
        .then(
            /**
             * @param {app.views.medications.ordering.dto.TherapyTemplates} templates
             */
            function apply(templates)
            {
              self.templates = templates;
              self._displayTemplates(templates);
            });
  },

  /**
   * @return {app.views.medications.ordering.dto.TherapyTemplates}
   */
  getTemplates: function()
  {
    return this.templates;
  },

  /**
   * Override if required. The context further refines the displayed templates when
   * {@link app.views.medications.ordering.OrderingBehaviour#filterTemplatesByActivePatient} is enabled, otherwise
   * {@link #getTemplateMode} is used.
   * @return {string} {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum}
   */
  getTemplateContext: function()
  {
    return this.templateContext;
  },

  /**
   * Returns the calculated mode from the configured context. Defines the type of templates to be saved and loaded,
   * when loading all available templates.
   * @return {string} {@link app.views.medications.TherapyEnums.therapyTemplateModeEnum}
   */
  getTemplateMode: function()
  {
    return app.views.medications.TherapyEnums.mapTherapyTemplateContextToMode(this.getTemplateContext());
  },

  /**
   * @return {app.views.medications.common.patient.AbstractReferenceData}
   */
  getReferenceData: function()
  {
    return this.referenceData;
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /**
   *
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Constructs the top level {@link tm.jquery.Panel} to house individual template panels belonging to the same
   * template type (user, organizational, patient ...).
   * @param {string} title
   * @return {tm.jquery.Panel}
   * @private
   */
  _buildTopLevelGroupPanel: function(title)
  {
    var parentPanel = new tm.jquery.Panel({
      collapsed: true,
      showHeader: true,
      showFooter: false,
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var parentPanelHeader = parentPanel.getHeader();
    parentPanelHeader.setCls('template-group-panel-header');
    parentPanelHeader.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));

    var parentPanelTitleContainer = new tm.jquery.Container({
      cls: "TextDataBold text-unselectable ellipsis",
      cursor: "pointer",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      html: title
    });
    parentPanelHeader.add(parentPanelTitleContainer);
    parentPanel.bindToggleEvent([parentPanelTitleContainer]);
    var contentContainer = parentPanel.getContent();
    contentContainer.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    contentContainer.setScrollable('visible');

    return parentPanel;
  },

  _buildTemplateRow: function(template, deleteAvailable)
  {
    var self = this;
    var view = this.getView();

    return new app.views.medications.common.TemplateTherapyGroupPanel({
      groupTitle: template.name,
      view: view,
      contentData: template.templateElements,
      deleteAvailable: deleteAvailable === true,
      templatePreconditions: template.getPreconditions(),
      attachElementToolbar: function(elementContainer)
      {
        self._attachGroupPanelElementToolbar(elementContainer, template, deleteAvailable);
      },
      addToBasketEventCallback: function(content)
      {
        self._addTherapiesToBasket(content);
      },
      deleteTemplateEventCallback: function()
      {
        self._deleteTemplate(template);
      },
      collapsed: true
    });
  },

  _attachGroupPanelElementToolbar: function(elementContainer, template, removeAvailable)
  {
    var self = this;
    var toolbar = new app.views.medications.common.TemplateTherapyContainerToolbar({
      therapyContainer: elementContainer,
      removeFromTemplateAvailable: removeAvailable === true,
      addWithEditAvailable: !this.getOrderingBehaviour().isTemplateOnlyMode(),
      addToBasketEventCallback: function(therapyContainer)
      {
        self._addTherapiesToBasket([therapyContainer.getData()]);
      },
      addToBasketWithEditEventCallback: this.addTemplateTherapyOrderWithEditCallback.bind(this),
      removeFromTemplateEventCallback: function(therapyContainer)
      {
        self._removeElementFromTemplate(template, therapyContainer.getData()).then(
            function()
            {
              therapyContainer.getParent().remove(therapyContainer);
            });
      }
    });
    elementContainer.setToolbar(toolbar);
  },

  /**
   * Loads the templates based on the {@link #orderingBehaviour} - either all templates, or only those that apply to the
   * current active patient.
   * @private
   * @returns {tm.jquery.Promise}
   */
  _loadTemplates: function()
  {
    return this.getOrderingBehaviour().isFilterTemplatesByActivePatient() ?
        this.getView()
            .getRestApi()
            .loadTherapyTemplates(this.getTemplateContext(), this.getReferenceData(), true) :
        this.getView()
            .getRestApi()
            .loadAllTherapyTemplates(this.getTemplateMode(), true);
  },

  /**
   * Removes all components from this container, then traverses the templates and adds those top
   * level panels that contain templates.
   * @param {app.views.medications.ordering.dto.TherapyTemplates} templates
   * @private
   */
  _displayTemplates: function(templates)
  {
    var view = this.getView();
    this.removeAll();

    if (templates.getOrganizationTemplates().length > 0)
    {
      var orgTemplatesTitle = view.getDictionary('organizational.order.sets.long');
      if (view.getCareProviderName())
      {
        orgTemplatesTitle += ' (' + view.getCareProviderName() + ')';
      }
      this._attachTopLevelPanel(
          orgTemplatesTitle,
          templates.getOrganizationTemplates(),
          view.getTherapyAuthority().isManageOrganizationalTemplatesAllowed());
    }

    if (templates.getUserTemplates().length > 0)
    {
      this._attachTopLevelPanel(
          view.getDictionary('my.order.sets'),
          templates.getUserTemplates(),
          view.getTherapyAuthority().isManageUserTemplatesAllowed());
    }

    if (templates.getPatientTemplates().length > 0)
    {
      this._attachTopLevelPanel(
          view.getDictionary('patient.order.sets'),
          templates.getPatientTemplates(),
          view.getTherapyAuthority().isManagePatientTemplatesAllowed());
    }

    if (templates.getCustomTemplateGroups().length > 0)
    {
      templates
          .getCustomTemplateGroups()
          .forEach(
              function addGroups(customGroup)
              {
                if (customGroup.getCustomTemplates().length > 0)
                {
                  this._attachTopLevelPanel(
                      customGroup.getGroup(),
                      customGroup.getCustomTemplates(),
                      view.getTherapyAuthority().isManageAllTemplatesAllowed());
                }
              },
              this);
    }

    this._configureFirstPanelAutoExpand();
    this.repaint();
  },

  /**
   * Enables automatic expand of the first top level panel. Finds the first {@link tm.jquery.Panel} component in this
   * container and attaches a call to {@link tm.jquery.Panel#expand} once the panel is fully rendered.
   * @private
   */
  _configureFirstPanelAutoExpand: function()
  {
    var appFactory = this.getView().getAppFactory();
    var firstPanel = this.getComponents().find(function isPanel(component)
    {
      return component instanceof tm.jquery.Panel
    });

    if (firstPanel)
    {
      firstPanel.on(
          tm.jquery.ComponentEvent.EVENT_TYPE_RENDER,
          function onRender(component)
          {
            appFactory.createConditionTask(
                function()
                {
                  component.expand();
                },
                function()
                {
                  return component.isRendered(true);
                },
                500, 100
            );
          });
    }
  },

  /**
   * Constructs a new top level panel and adds it to the component list.
   * @param {string} title
   * @param {Array<app.views.medications.ordering.dto.TherapyTemplate>} templates
   * @param {boolean} [deleteAvailable=false] Is the removal of individual templates available / permitted?
   * @private
   */
  _attachTopLevelPanel: function(title, templates, deleteAvailable)
  {
    var topLevelPanel = this._buildTopLevelGroupPanel(title);

    templates
        .forEach(
            function add(template)
            {
              topLevelPanel
                  .getContent()
                  .add(this._buildTemplateRow(template, deleteAvailable === true));
            },
            this);

    this.add(topLevelPanel);
  },

  _deleteTemplate: function(template)
  {
    var self = this;
    var view = this.getView();
    var deleteUrl = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_DELETE_TEMPLATE;
    var params = {templateId: template.id};

    var message = view.getDictionary("template.delete.confirmation.message");
    message = tm.jquery.Utils.isEmpty(message) ? "" : message.replace("{0}", template.name);

    var confirmDialog = view.getAppFactory().createConfirmSystemDialog(message,
        function(confirmed)
        {
          if (confirmed === true)
          {
            view.loadPostViewData(deleteUrl, params, null,
                function()
                {
                  self.reloadTemplates();
                });
          }
        }
    );
    confirmDialog.setWidth(450);
    confirmDialog.setHeight(142);
    confirmDialog.show();
  },

  /**
   * @param {app.views.medications.ordering.dto.TherapyTemplate} template
   * @param {app.views.medications.ordering.dto.TherapyTemplateElement} templateElement
   * @returns {tm.jquery.Promise}
   * @private
   */
  _removeElementFromTemplate: function(template, templateElement)
  {
    var view = this.getView();
    template.templateElements.remove(templateElement);

    var deferred = tm.jquery.Deferred.create();
    view.getRestApi()
        .saveTemplate(template, this.getTemplateMode(), true)
        .then(
            function(newTemplateId)
            {
              template.setId(newTemplateId);
              deferred.resolve();
            },
            function()
            {
              deferred.reject();
            }
        );
    return deferred.promise();
  },

  /**
   * @param {Array<app.views.medications.ordering.dto.TherapyTemplateElement|
   * app.views.medications.common.therapy.AbstractTherapyContainerData>} templateElements
   * @private
   */
  _addTherapiesToBasket: function(templateElements)
  {
    var therapyOrders = [];
    for (var i = 0; i < templateElements.length; i++)
    {
      // has to be cloned or else references to child DTOs will be kept!
      var therapy = templateElements[i].getTherapy().clone(true);
      therapy.setCompleted(templateElements[i].isValid());

      var therapyOrder = new app.views.medications.ordering.TherapyOrder({
        therapy: therapy,
        validationIssues: templateElements[i].getValidationIssues(),
        recordAdministration: templateElements[i].isRecordAdministration()
      });

      therapyOrders.push(therapyOrder);
    }
    this.addTemplateTherapyOrdersCallback(therapyOrders);
  }
});

