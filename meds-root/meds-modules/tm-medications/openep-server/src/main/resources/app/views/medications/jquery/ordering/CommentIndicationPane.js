Class.define('app.views.medications.ordering.CommentIndicationPane', 'tm.jquery.Container', {
  cls: "comment-indication-pane",
  scrollable: "visible",
  /** configs */
  view: null,
  saveDateTimePaneEvent: null, //optional
  /** privates: components */
  commentField: null,
  indicationField: null,
  indicationCombo: null,

  orderingBehaviour: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    if (!this.orderingBehaviour)
    {
      throw new Error('orderingBehaviour is not defined.');
    }

    this._buildGui();
  },

  _buildGui: function()
  {
    var view = this.view;
    var self = this;

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));

    this.commentField = new tm.jquery.TextField({cls: "comment-field"});
    this.commentField.onKey(
        new tm.jquery.event.KeyStroke({key: "t", altKey: true, ctrlKey: true, shiftKey: false}),
        function()
        {
          if (self.saveDateTimePaneEvent)
          {
            self.saveDateTimePaneEvent();
          }
        });

    var commentComponent = new app.views.medications.common.VerticallyTitledComponent({
      titleText: view.getDictionary('commentary'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "50%"),
      contentComponent: this.commentField
    });

    this.indicationField = new tm.jquery.TextField({
      cls: "indication-field",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    this.indicationCombo = new tm.jquery.SelectBox({
      cls: "indication-combo",
      hidden: true,
      liveSearch: false,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      dropdownWidth: "stretch",
      placeholder: view.getDictionary("select"),
      allowSingleDeselect: true,
      defaultValueCompareToFunction: function(value1, value2)
      {
        return value1.id == value2.id;
      },
      defaultTextProvider: function(selectBox, index, option)
      {
        return option.getValue().name;
      }
    });
    this.indicationCombo.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      var selections = componentEvent.getEventData().selections;
      if (selections && selections.length > 0 && selections[0].id == "CUSTOM")
      {
        self.indicationField.show();
        self.indicationField.focus();
      }
      else
      {
        self.indicationField.hide();
      }
    });

    var indicationComponent = new app.views.medications.common.VerticallyTitledComponent({
      cls: "indication-container",
      titleText: view.getDictionary('indication'),
      scrollable: "visible",
      hidden: !this.getOrderingBehaviour().isIndicationAvailable(),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "50%"),
      contentComponent: new tm.jquery.Container({
        scrollable: "visible",
        layout: tm.jquery.HFlexboxLayout.create("flex-start", "center"),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
      })
    });
    indicationComponent.getContentComponent().add(this.indicationCombo);
    indicationComponent.getContentComponent().add(this.indicationField);

    this.add(commentComponent);
    this.add(indicationComponent);
  },

  /** public methods */
  setMedicationData: function(medicationData)
  {
    var self = this;
    if (medicationData.indications && medicationData.indications.length > 0)
    {
      this.indicationField.hide();
      this.indicationCombo.show();
      this.indicationCombo.removeAllOptions();
      medicationData.indications.forEach(function(indication)
      {
        self.indicationCombo.addOption(tm.jquery.SelectBox.createOption(indication));
      });
      this.indicationCombo.addOption(tm.jquery.SelectBox.createOption({
        id: "CUSTOM",
        name: "<span style='color:#aaa'>" + this.view.getDictionary("other.undef") + " ...</span>"
      }));
    }
    else
    {
      this.indicationCombo.hide();
      this.indicationField.show();
    }
  },

  getComment: function()
  {
    return this.commentField.getValue() ? this.commentField.getValue() : null;
  },

  setComment: function(comment)
  {
    this.commentField.setValue(comment);
  },

  getIndication: function()
  {
    if (!this.indicationCombo.isHidden())
    {
      var selectedIndication = this.indicationCombo.getSelections()[0];
      if (tm.jquery.Utils.isEmpty(selectedIndication))
      {
        return null;
      }
      else if (selectedIndication.id === "CUSTOM")
      {
        var customIndication = this.indicationField.getValue();
        if (customIndication)
        {
          return {id: null, name: customIndication};
        }
        else
        {
          return null;
        }
      }
      else
      {
        return selectedIndication;
      }
    }
    else
    {
      var indication = this.indicationField.getValue();
      if (indication)
      {
        return {id: null, name: indication};
      }
      return null;
    }
  },

  setIndication: function(indication)
  {
    if (this.indicationCombo.isHidden())
    {
      this.indicationField.setValue(indication ? indication.name : null);
    }
    else
    {
      if (indication == null || indication.name == null)
      {
        this.indicationCombo.setSelections([]);
      }
      else if (indication.id == null)
      {
        this.indicationCombo.setSelections([{id: "CUSTOM"}]);
        this.indicationField.setValue(indication.name);
      }
      else
      {
        this.indicationCombo.setSelections([indication]);
      }
    }
  },

  getIndicationValidations: function()
  {
    var self = this;
    var formFields = [];
    if (this.getOrderingBehaviour().isIndicationAvailable() && !this.indicationCombo.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: self.indicationCombo,
        required: true,
        componentValueImplementationFn: function()
        {
          var indicationComboValue = self.indicationCombo.getSelections()[0];
          if (tm.jquery.Utils.isEmpty(indicationComboValue))
          {
            return null;
          }
          return true;
        }
      }));
      formFields.push.apply(formFields, this.getIndicationFieldValidation());
    }
    return formFields;
  },

  /**
   * Validates indication filed input if available. Requires at least two characters without leading or trailing spaces.
   * @returns {Array<tm.jquery.FormField>}
   */
  getIndicationFieldValidation: function()
  {
    var formFields = [];
    if (this.getOrderingBehaviour().isIndicationAvailable() && !this.indicationField.isHidden())
    {
      formFields.push(new tm.jquery.FormField({
        component: this.indicationField,
        required: true,
        validation: {
          type: "local",
          validators: [app.views.medications.MedicationUtils.buildDefaultMinimumLengthStringValidator(this.getView())]
        }
      }));
    }
    return formFields;
  },

  /**
   * @returns {Array<tm.jquery.FormField>}
   */
  getCommentFieldValidation: function()
  {
    var formFields = [];
    formFields.push(new tm.jquery.FormField({
      component: this.commentField,
      required: true,
      validation: {
        type: "local",
        validators: [app.views.medications.MedicationUtils.buildDefaultMinimumLengthStringValidator(this.getView())]
      }
    }));
    return formFields;
  },

  clear: function()
  {
    this.indicationField.setValue(null);
    this.indicationCombo.setSelections([], true);
    this.commentField.setValue(null);
  },

  requestFocus: function()
  {
    this.commentField.getInputElement().focus();
  },

  /**
   * @return {app.views.medications.ordering.OrderingBehaviour}
   */
  getOrderingBehaviour: function()
  {
    return this.orderingBehaviour;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});

