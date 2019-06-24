Class.define('app.views.medications.common.therapy.BaseTherapyDetailsContentContainer', 'app.views.common.containers.AppBodyContentContainer', {
  cls: "therapy-details-content",
  view: null,

  medicationData: null,
  displayProvider: null,
  therapy: null,

  /**privates**/
  _contentContainer: null,
  _testRenderCoordinator: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    if (!tm.jquery.Utils.isArray(this.medicationData))
    {
      this.medicationData = [];
    }
    this._buildGui();

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'therapy-details-content-coordinator',
      view: this.getView(),
      component: this
    });
  },

  _buildGui: function()
  {

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));

    var mainContainer = new tm.jquery.Container({
      cls: "main-details-container",
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var headerContainer = new tm.jquery.Container({
      cls: "header-container",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var therapyIconContainer = new tm.jquery.Container({
      cls: this.getDisplayProvider().getTherapyIcon(this.getTherapy()) + " " + "icon-container",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    headerContainer.add(therapyIconContainer);

    var therapyContainer = new tm.jquery.Container({
      cls: "therapy-details-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    therapyContainer.add(this._buildTherapyDescriptionContainer());
    this._buildTherapyWarningsContainer(therapyContainer);
    headerContainer.add(therapyContainer);

    mainContainer.add(headerContainer);

    this._contentContainer = new tm.jquery.Container({
      cls: "content-container",
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    mainContainer.add(this._contentContainer);
    this.add(mainContainer);
  },

  //Override in subclass - optional
  _buildTherapyWarningsContainer: function()
  {

  },

  _buildTherapyDescriptionContainer: function()
  {
    var view = this.getView();
    var therapy = this.getTherapy();
    var utils = app.views.medications.MedicationUtils;
    var medicationDataList = this.getMedicationData();

    var therapyDescriptionContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0)
    });

    if (therapy.isOrderTypeComplex())
    {
      var ingredientList = therapy.getIngredientsList();
      for (var i = 0; i < ingredientList.length; i++)
      {
        var ingredient = ingredientList[i];
        var medication = ingredient.medication;
        var medicationData = this._getMedicationDataById(medication.getId());

        therapyDescriptionContainer.add(
            this._buildTherapyDescriptionRow(
                medication.getFormattedDisplayName(),
                this._buildQuantityFromIngredient(ingredient),
                "medication-ingredient",
                true));
        if (!!medicationData)
        {
          this._addPossibleMedicationDocumentsLinks(medicationData, therapyDescriptionContainer);
          therapyDescriptionContainer.add(this._buildTherapyConflictIcons(medicationData));
        }
      }
      var volumeSumDisplay = therapy.getVolumeSumDisplay();

      if (!tm.jquery.Utils.isEmpty(volumeSumDisplay))
      {
        var volumeSumLabel = "<span class='TextLabel'>" + view.getDictionary("volume.total") + "</span>";
        var volumeSum = utils.getFormattedDecimalNumber(volumeSumDisplay);
        therapyDescriptionContainer.add(
            this._buildTherapyDescriptionRow(
                volumeSumLabel,
                volumeSum,
                "volume-sum",
                true));
      }
    }
    else
    {
      therapyDescriptionContainer.add(
          this._buildTherapyDescriptionRow(
              therapy.getMedication().getFormattedDisplayName(),
              null,
              "medication-ingredient",
              true));

      if (!!medicationDataList[0])
      {
        this._addPossibleMedicationDocumentsLinks(medicationDataList[0], therapyDescriptionContainer);
        therapyDescriptionContainer.add(this._buildTherapyConflictIcons(medicationDataList[0]));
      }
    }

    return therapyDescriptionContainer;
  },

  /**
   *
   * @param ingredient
   * @returns {String|null}
   * @private
   */
  _buildQuantityFromIngredient: function(ingredient)
  {
    var utils = app.views.medications.MedicationUtils;

    if (ingredient.quantity)
    {
      var ingredientQuantity = utils.getFormattedDecimalNumber(utils.doubleToString(ingredient.quantity, 'n2')) + ' ' +
          utils.getFormattedUnit(ingredient.quantityUnit, this.getView());
      if (ingredient.quantityDenominator)
      {
        ingredientQuantity += " / " +
            utils.getFormattedDecimalNumber(utils.doubleToString(ingredient.quantityDenominator, 'n2')) + ' ' +
            utils.getFormattedUnit(ingredient.quantityDenominatorUnit, this.getView());
      }
      return ingredientQuantity;
    }
    return null;
  },

  /**
   * @param {String} therapyDescription
   * @param {String|undefined|null} [therapyDetails=undefined]
   * @param {String|undefined|null} [testAttributeName=undefined]
   * @param {Boolean} [skipEscape=false]
   * @private
   */
  _buildTherapyDescriptionRow: function(therapyDescription, therapyDetails, testAttributeName, skipEscape)
  {
    var therapyDescriptionRow = new tm.jquery.Container({
      cls: "therapy-description-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });

    var therapyDescriptionColumn = new tm.jquery.Container({
      cls: "therapy-description-column",
      html: skipEscape ? therapyDescription : tm.jquery.Utils.escapeHtml(therapyDescription),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    therapyDescriptionRow.add(therapyDescriptionColumn);

    if (therapyDetails)
    {
      var therapyDetailsColumn = new tm.jquery.Container({
        cls: "TextData",
        html: skipEscape ? therapyDetails : tm.jquery.Utils.escapeHtml(therapyDetails),
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
      });
      therapyDescriptionRow.add(therapyDetailsColumn);
    }

    if (testAttributeName)
    {
      therapyDescriptionRow.addTestAttribute(testAttributeName);
    }
    return therapyDescriptionRow;
  },

  /**
   * @param {String} labelValue
   * @param {String|null} descriptionValue
   * @param {String|null} [testAttributeName=null]
   * @param {Boolean} [skipEscape=false]
   * @returns {tm.jquery.Container}
   * @private
   */
  _buildLabelDataRowContainer: function(labelValue, descriptionValue, testAttributeName, skipEscape)
  {
    var contentContainerRow = new tm.jquery.Container({
      cls: "content-container-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
    });

    if (testAttributeName)
    {
      contentContainerRow.addTestAttribute(testAttributeName);
    }

    var rowLabel = new tm.jquery.Container({
      cls: "TextLabel row-label",
      html: skipEscape ? labelValue : tm.jquery.Utils.escapeHtml(labelValue),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
    });
    contentContainerRow.add(rowLabel);

    var rowDescription = new tm.jquery.Container({
      cls: "TextData",
      html: skipEscape ? descriptionValue : tm.jquery.Utils.escapeHtml(descriptionValue),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    contentContainerRow.add(rowDescription);

    return contentContainerRow;
  },

  /**
   * @param {String} labelValue
   * @param {String|null} [testAttributeName=null]
   * @returns {tm.jquery.Container} representing a row, where the value of the label is a checkbox sign.
   * @private
   */
  _buildCheckboxLabelDataRowContainer: function(labelValue, testAttributeName)
  {
    return this._buildLabelDataRowContainer(
        labelValue,
        this._buildCheckBoxIconHtml(),
        testAttributeName,
        true);
  },

  /**
   * @return {string} representing an html checkbox.
   * @private
   */
  _buildCheckBoxIconHtml: function()
  {
    return "<span class='checkbox-on-icon'></span>";
  },

  /**
   * @param {String} medicationId
   * @returns {Object|*}
   * @private
   */
  _getMedicationDataById: function(medicationId)
  {
    var medicationDataList = this.getMedicationData();
    for (var i = 0; i < medicationDataList.length; i++)
    {
      if (medicationDataList[i].getMedication().getId() === medicationId)
      {
        return medicationDataList[i];
      }
    }
    return null;
  },

  /**
   * If the provided medication has associated documents, constructs a new instance of the document links container and
   * adds them to the parent container. Otherwise does nothing.
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @param {tm.jquery.Container} parentContainer
   * @private
   */
  _addPossibleMedicationDocumentsLinks: function(medicationData, parentContainer)
  {
    if (medicationData.getMedicationDocuments().length > 0)
    {
      parentContainer.add(
          new app.views.medications.common.MedicationDocumentsContainer({
            view: this.getView(),
            documents: medicationData.getMedicationDocuments()
          }));
    }
  },

  /**
   * @param {app.views.medications.common.dto.MedicationData} medicationData
   * @private
   */
  _buildTherapyConflictIcons: function(medicationData)
  {
    var therapy = this.getTherapy();
    var enums = app.views.medications.TherapyEnums;
    var warningsIconRow = new tm.jquery.Container({
      cls: "warnings-icon-row",
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0)
    });

    var maxDoseImageCls =
        app.views.medications.warnings.WarningsHelpers.getImageClsForMaximumDosePercentage(therapy.getMaxDosePercentage());

    if (!!maxDoseImageCls)
    {
      warningsIconRow.add(this._createWarningIcon(maxDoseImageCls));
    }

    if (medicationData.isControlledDrug())
    {
      warningsIconRow.add(this._createWarningIcon(enums.medicationPropertyType.CONTROLLED_DRUG.toLowerCase() + "_icon"));
    }
    if (medicationData.isCriticalDrug())
    {
      warningsIconRow.add(this._createWarningIcon(enums.medicationPropertyType.CRITICAL_DRUG.toLowerCase() + "_icon"));
    }
    if (this.getView().isFormularyFilterEnabled() && !medicationData.isFormulary())
    {
      warningsIconRow.add(this._createWarningIcon("non_formulary_icon"));
    }
    if (medicationData.isBlackTriangleMedication())
    {
      warningsIconRow.add(this._createWarningIcon(
          enums.medicationPropertyType.BLACK_TRIANGLE_MEDICATION.toLowerCase() + "_icon"));
    }
    if (medicationData.isUnlicensedMedication())
    {
      warningsIconRow.add(this._createWarningIcon(
          enums.medicationPropertyType.UNLICENSED_MEDICATION.toLowerCase() + "_icon"));
    }
    if (medicationData.isHighAlertMedication())
    {
      warningsIconRow.add(this._createWarningIcon(
          enums.medicationPropertyType.HIGH_ALERT_MEDICATION.toLowerCase() + "_icon"));
    }
    if (medicationData.isClinicalTrialMedication())
    {
      warningsIconRow.add(this._createWarningIcon(
          enums.medicationPropertyType.CLINICAL_TRIAL_MEDICATION.toLowerCase() + "_icon"));
    }
    if (medicationData.isExpensiveDrug())
    {
      warningsIconRow.add(this._createWarningIcon(enums.medicationPropertyType.EXPENSIVE_DRUG.toLowerCase() + "_icon"));
    }
    return warningsIconRow;
  },

  /**
   * @param {string} cls
   * @private
   */
  _createWarningIcon: function(cls)
  {
    return new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      cls: "row-icon " + cls,
      width: 16,
      height: 16
    });
  },

  /**
   * Getters & Setters
   */

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationData|null}
   */
  getMedicationData: function()
  {
    return this.medicationData;
  },

  /**
   * @returns {app.views.medications.common.therapy.TherapyContainerDisplayProvider}
   */
  getDisplayProvider: function()
  {
    return this.displayProvider;
  }
});