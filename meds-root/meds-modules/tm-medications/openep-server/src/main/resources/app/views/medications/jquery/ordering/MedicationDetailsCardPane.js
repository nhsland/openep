Class.define('app.views.medications.therapy.MedicationDetailsCardPane', 'app.views.common.containers.AppBodyContentContainer', {
  cls: 'medication-details-card',
  scrollable: 'both',
  /** configs */
  view: null,
  medicationData: null,
  selectedRoute: null,
  /** privates: components */

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (this.medicationData)
    {
      this._buildGui(this.medicationData, this.selectedRoute);
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {app.views.medications.common.dto.MedicationRoute} [selectedRoutes=null]
   */
  setMedicationData: function(medicationData, selectedRoutes)
  {
    this.removeAll();
    if (medicationData)
    {
      this._buildGui(medicationData, tm.jquery.Utils.isEmpty(selectedRoutes) ? null : selectedRoutes);
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {app.views.medications.common.dto.MedicationRoute} selectedRoutes
   * @private
   */
  _buildGui: function(medicationData, selectedRoutes)
  {
    var view = this.getView();
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 0));

    var iconContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      width: 48,
      height: 48,
      cls: this._getMedicationIcon(medicationData.getDoseForm())
    });

    var medicationInfoContainer = new tm.jquery.Container({
      cls: 'medication-info-container',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start"),
      scrollable: "vertical"
    });
    var contentContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create('felx-start', 'stretch'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto')
    });

    //medication
    if (medicationData.getMedication())
    {
      var medicationNameContainer = new tm.jquery.Container({
        html: medicationData.getMedication().getFormattedDisplayName()
      });
      contentContainer.add(medicationNameContainer);
    }
    var medicationDetailInfo = '';

    if (!medicationData.isVtm() && medicationData.getDoseForm())
    {
      //dose form
      medicationDetailInfo += '<span class="TextLabel">' + view.getDictionary("dose.form") + ' </span>';
      medicationDetailInfo += '<span class="TextDataBold">' +
          tm.jquery.Utils.escapeHtml(medicationData.getDoseForm().getName()) + ' </span>';
      medicationDetailInfo += '<br>';
    }
    if (!tm.jquery.Utils.isEmpty(medicationData.getMedicationPackaging()))
    {
      medicationDetailInfo += '<span class="TextLabel">' + view.getDictionary("medication.packaging") + ' </span>';
      medicationDetailInfo += '<span class="TextDataBold">' +
          tm.jquery.Utils.escapeHtml(medicationData.getMedicationPackaging()) + '</span>';
      medicationDetailInfo += '<br>';
    }
    if (medicationData.hasMedicationPrice())
    {
      var medicationPrice = medicationData.getMedicationPricingDetails();
      medicationDetailInfo +=
          '<span class="TextLabel">' + view.getDictionary('MedicationPropertyType.PRICE') + ' </span>';
      medicationDetailInfo += '<span class="TextDataBold">' +
          tm.jquery.Utils.escapeHtml(medicationPrice.getValue()) + '</span>';
      medicationDetailInfo += '<br>';
    }
    var medicationDataComponent = new tm.jquery.Component({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      html: medicationDetailInfo
    });

    medicationInfoContainer.add(medicationDataComponent);

    //medication ingredients
    if (!medicationData.isVtm() && medicationData.getMedicationIngredients().length > 0)
    {
      this._createMedicationIngredientsContainer(medicationData, medicationInfoContainer);
      if (medicationData.getPrescribingDose())
      {
        var prescribingDose = '<span class="TextLabel">' +
            view.getDictionary("prescribing.strength") + ' </span>' +
            '<span class="TextDataBold">' +
            (medicationData.getPrescribingDose().getDisplaySting()) +
            ' </span>';
        var prescribingDoseContainer = new tm.jquery.Container({
          html: prescribingDose
        });
        medicationInfoContainer.add(prescribingDoseContainer);
      }
    }
    if (medicationData.getAdministrationUnit())
    {
      var administrationUnit = '<span class="TextLabel">' +
          this.getView().getDictionary("administration.unit") + ' </span>' +
          '<span class="TextDataBold">' +
          (medicationData.getAdministrationUnit()) +
          ' </span>';
      medicationInfoContainer.add(
        new tm.jquery.Component({
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
          html: administrationUnit
        })
      );
    }
    if (medicationData.getMedicationDocuments().length > 0)
    {
      medicationInfoContainer.add(
          new app.views.medications.common.MedicationDocumentsContainer({
            view: view,
            documents: medicationData.getMedicationDocuments()
          }));
    }
    if (tm.jquery.Utils.isArray(selectedRoutes) && selectedRoutes.length === 1
        && !tm.jquery.Utils.isEmpty(selectedRoutes[0].getMaxDose()))
    {
      var configuredMaxDose = '<span class="TextLabel">' + view.getDictionary("configured.max") + ' ' + ' </span>';
      configuredMaxDose += '<span class="TextDataBold">' + selectedRoutes[0].getMaxDose().dose + ' ' +
          'mg' + '/' + selectedRoutes[0].getMaxDose().period.toLowerCase() + ' </span>';
      configuredMaxDose += '<br>';

      var medicationInfoMaxDose = new tm.jquery.Component({
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
        html: configuredMaxDose
      });

      medicationInfoContainer.add(medicationInfoMaxDose);
    }
    contentContainer.add(medicationInfoContainer);
    this.add(iconContainer);
    this.add(contentContainer);
  },

  /**
   * Creates and adds a list of  ingredients. If ingredient is main, it should be presented in bold.
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {tm.jquery.Container} medicationInfoContainer
   * @private
   */
  _createMedicationIngredientsContainer: function(medicationData, medicationInfoContainer)
  {
    var view = this.getView();
    var ingredientsStrengthContainer = new tm.jquery.Container({
      cls: 'ingredients-strength-container',
      layout: tm.jquery.HFlexboxLayout.create('felx-start', 'flex-start')
    });
    var ingredientsStrengthTitleContainer = new tm.jquery.Container({
      cls: 'TextLabel',
      html: view.getDictionary("strength")
    });
    var ingredientsStrengthContentContainer =  new tm.jquery.Container({
      cls: 'ingredients-strength-content-container',
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "flex-start")
    });

    ingredientsStrengthContainer.add(ingredientsStrengthTitleContainer);
    ingredientsStrengthContainer.add(ingredientsStrengthContentContainer);

    var medicationIngredientsStrength = "";
    for (var i = 0; i < medicationData.getMedicationIngredients().length; i++)
    {
      var ingredient = medicationData.getMedicationIngredients()[i];
      var strengthString = ingredient.getDisplayString();
      if (strengthString)
      {
        var ingredientCls = ingredient.isMain() ? 'TextDataBold' : 'TextData';
        medicationIngredientsStrength += '<span class=' + ingredientCls + '>' + strengthString + ' </span>';
        medicationIngredientsStrength += '<span class=' + ingredientCls + '>' +
            ' (' + tm.jquery.Utils.escapeHtml(ingredient.getIngredientName()) + ')' +
            ' </span><br>';
      }
    }
    if (medicationIngredientsStrength)
    {
      ingredientsStrengthContentContainer.setHtml(medicationIngredientsStrength);
      medicationInfoContainer.add(ingredientsStrengthContainer);
    }
  },

  _getMedicationIcon: function(doseForm)
  {
    if (doseForm && doseForm.getDoseFormType() === app.views.medications.TherapyEnums.doseFormType.TBL)
    {
      return "icon_pills";
    }
    return "icon_other_medication";
  }
});