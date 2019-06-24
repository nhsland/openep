Class.define('app.views.medications.mentalHealth.TherapyGroupPanel', 'app.views.medications.common.TherapyGroupPanel', {

  addAllMedicationsToBasketFunction: null, // callback function for add all elements to basket
  addMedicationToBasketFunction: null,

  addAllEnabled: null, // enabled adding all elements to basket
  selectedCls: null, // element container css when added to basket
  addAllImage: null, // add all elements to basket image

  /* Override */
  buildElementContainer: function(elementData)
  {
    var therapyContainer = new app.views.medications.common.therapy.TherapyContainer({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      view: this.getView(),
      data: elementData,
      displayProvider: this.getDisplayProvider(),
      showIconTooltip: false,
      /* this is ugly but for some reason contentContainer's getParent() returns a different object ... */
      groupPanel: this,
      getGroupPanel: function()
      {
        return this.groupPanel;
      }
    });

    this.attachElementToolbar(therapyContainer);

    return therapyContainer;
  },

  attachElementToolbar: function(elementContainer)
  {
    var self = this;
    var toolbar = new app.views.medications.mentalHealth.TherapyContainerToolbars({
      therapyContainer: elementContainer,
      addToBasketEventCallback: function(elementContainer)
      {
        self.addMedicationToBasketFunction(elementContainer);
      }
    });
    elementContainer.setToolbar(toolbar);
    elementContainer.getToolbar().show();
  },

  /**
   * @param {app.views.medications.mentalHealth.MentalHealthTherapyContainerData
   * |app.views.medications.common.therapy.AbstractTherapyContainerData} elementData
   */
  markElementAddedToBasket: function(elementData)
  {
    if (!elementData)
    {
      return;
    }

    var index = this.getContentData().indexOf(elementData);
    var contentData = this.getContentData();
    if (index > -1 && index < contentData.length)
    {
      if (this.addAllEnabled === true)
      {
        this.addAllImage.show();
      }
    }

    var container = this._findTherapyContainerById(elementData.getId());
    if (!!container)
    {
      var oldCls = container.getCls();
      container.setCls(tm.jquery.Utils.isEmpty(oldCls) ? this.selectedCls : (oldCls + " " + this.selectedCls));
      container.applyCls(container.getCls());
      container.getToolbar().hide();
    }
  },

  /**
   * @param {string} elementId
   */
  markElementRemovedFromBasket: function(elementId)
  {
    if (!elementId)
    {
      return;
    }

    var container = this._findTherapyContainerById(elementId);
    if (!!container)
    {
      var oldCls = container.getCls();
      if (oldCls)
      {
        container.setCls(oldCls.replace(' ' + this.selectedCls, ''));
        container.applyCls(container.getCls());
      }
      container.getToolbar().show();
    }
  },

  /* Override */
  setContentData: function(content, expandAfter)
  {
    this.contentData = tm.jquery.Utils.isEmpty(content) ? [] : content;

    if (expandAfter === true)
    {
      this._processContentData(true);
    }
    else
    {
      this.collapsed = true;
    }
  },

  /* Override */
  _initHeaderContainer: function()
  {
    var self = this;
    var header = this.getHeader();

    header.setCls("TextDataBold");
    header.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));

    var headerValueContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      cursor: "pointer",
      cls: "TextDataBold",
      html: this.getGroupTitle()
    });

    if (this.addAllEnabled === true)
    {
      this.addAllImage = new tm.jquery.Image({
        cursor: "pointer",
        cls: "icon-extend-24",
        margin: "0px 5px 0px 0px",
        handler: function()
        {
          self._onAddAllMedicationsToBasket();
        }
      });
    }

    header.add(headerValueContainer);
    if (this.addAllEnabled === true)
    {
      header.add(this.addAllImage);
    }
    this.bindToggleEvent([headerValueContainer]);
  },

  _onAddAllMedicationsToBasket: function()
  {
    this.addAllMedicationsToBasketFunction(this);
    this.addAllImage.hide();
  },

  /**
   * @param {string} elementId
   * @return {app.views.medications.common.therapy.TherapyContainer}
   * @private
   */
  _findTherapyContainerById: function(elementId)
  {
    return this.getContent()
        .getComponents()
        .find(function byDataId(therapyContainer)
        {
          return therapyContainer.getData().getId() === elementId;
        });
  }
});