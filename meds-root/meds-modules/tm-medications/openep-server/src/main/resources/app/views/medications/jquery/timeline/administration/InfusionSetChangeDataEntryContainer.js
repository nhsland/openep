Class.define('app.views.medications.timeline.administration.InfusionSetChangeDataEntryContainer', 'app.views.medications.timeline.administration.BaseTherapyAdministrationDataEntryContainer', {
  /** configs */

  /** components */
  _infusionSystemChangeButton: null,
  _infusionSyringeChangeButton: null,

  /**
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
    this._presentMedicationData();
  },

  /**
   * @private
   */
  _buildGui: function()
  {
    this.add(this.buildTherapyDescriptionAndInfoContainer());

    if ((tm.jquery.Utils.isEmpty(this.getAdministration())) || !this.getAdministration().isInfusionBagChangeTask())
    {
      this.add(this._buildInfusionSetChangeContainer());
    }

    this.add(this.getAdministrationTimeContainer());
    if (!!this.getWitnessContainer())
    {
      this._addConfiguredWitnessContainer()
    }
    if (this.isInfusionBagEnabled())
    {
      this.add(this.getBagContainer());
    }
    if (this.isRequestSupplyEnabled())
    {
      this.add(this.getRequestSupplyContainer());
    }
    this.add(this.getCommentContainer());
    this.add(this.getWarningContainer());
  },

  /**
   * Checks if witness container is available, and if any requirements for witnessing are met. Sets witness as mandatory
   * accordingly.
   * @private
   */
  _addConfiguredWitnessContainer: function()
  {
    this.add(this.getWitnessContainer());
    this.getWitnessContainer().setHidden(false);
    this.getWitnessContainer().setMandatory(this.isWitnessingRequired() ||
        this.getView().isAdministrationWitnessingIvRequired());
  },

  /**
   * @private
   */
  _buildInfusionSetChangeContainer: function()
  {
    var self = this;
    var view = this.getView();
    var enums = app.views.medications.TherapyEnums;
    this._infusionSystemChangeButton = new tm.jquery.Button({
      cls: "btn-bubble",
      data: enums.infusionSetChangeEnum.INFUSION_SYSTEM_CHANGE,
      text: view.getDictionary("InfusionSetChangeEnum." + enums.infusionSetChangeEnum.INFUSION_SYSTEM_CHANGE),
      pressed: true
    });
    this._infusionSyringeChangeButton = new tm.jquery.Button({
      cls: "btn-bubble",
      data: enums.infusionSetChangeEnum.INFUSION_SYRINGE_CHANGE,
      text: view.getDictionary("InfusionSetChangeEnum." + enums.infusionSetChangeEnum.INFUSION_SYRINGE_CHANGE)
    });

    var infusionSetChangeContainer = new tm.jquery.Container({
      cls: "infusion-set-change-container with-top-border",
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center'),
      height: 48
    });
    this.infusionSetChangeButtonGroup = new tm.jquery.ButtonGroup({
      cls: "btn-group-bubble",
      buttons: [this._infusionSystemChangeButton, this._infusionSyringeChangeButton],
      orientation: 'horizontal',
      type: 'radio'
    });
    this.infusionSetChangeButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function()
    {
      self._handleBagContainerVisibility();
    });
    infusionSetChangeContainer.add(this.infusionSetChangeButtonGroup);

    return infusionSetChangeContainer;
  },

  /**
   * @private
   */
  _presentMedicationData: function()
  {
    var enums = app.views.medications.TherapyEnums;

    if (this.getAdministration())
    {
      if (this.getTherapy().isContinuousInfusion() &&
          this.administrationType === enums.administrationTypeEnum.INFUSION_SET_CHANGE)
      {
        if (this.isInfusionBagEnabled() && this.getAdministration().isInfusionBagChangeTask())
        {
          this.getBagContainer().isRendered() ? this.getBagContainer().show() : this.getBagContainer().setHidden(false);
          this.setBagFieldValue(this.getAdministration().getInfusionBag().quantity);
        }
      }
      if (this.getAdministration().getInfusionSetChangeEnum() && !this.getAdministration().isInfusionBagChangeTask())
      {
        var buttons = this.infusionSetChangeButtonGroup.getButtons();
        for (var i = 0; i < buttons.length; i++)
        {
          if (buttons[i].data === this.getAdministration().getInfusionSetChangeEnum())
          {
            this.infusionSetChangeButtonGroup.setSelection([buttons[i]]);
            break;
          }
        }
        this._handleBagContainerVisibility();
      }
    }
    this.applyMedicationInfoButtonTooltip(this.getMedicationData());
  },

  /**
   * @private
   */
  _confirmTherapyAdministration: function()
  {
    var self = this;
    var view = this.getView();

    if (tm.jquery.Utils.isEmpty(this.administration))
    {
      this.administration = new app.views.medications.timeline.administration.dto.Administration();
      this.administration.setAdditionalAdministration(true);
      this.administration.setAdministrationType(this.administrationType);
    }
    this.administration.setAdministeredDose(new app.views.medications.common.dto.TherapyDose());
    this.administration.setPlannedDose(new app.views.medications.common.dto.TherapyDose());
    this.administration.setAdministrationResult(app.views.medications.TherapyEnums.administrationResultEnum.GIVEN);
    if (!!this.getWitnessContainer())
    {
      this.administration.setWitness(this.getWitnessContainer().getAuthenticatedWitness());
    }
    this.administration.setComment(this.getAdministrationComment());
    this.administration.setInfusionSetChangeEnum(this.infusionSetChangeButtonGroup.getSelection()[0].data);
    this.administration.setAdministrationTime(this._getSelectedTimestamp());

    if (this.isInfusionBagEnabled() && !this.getBagContainer().isHidden())
    {
      if (this.getBagFieldValue())
      {
        this.administration.setInfusionBag({quantity: this.getBagFieldValue(), unit: "mL"});
      }
    }

    view.getRestApi().confirmAdministrationTask(
        this.getTherapy(),
        this.getAdministration(),
        this.isEditMode(),
        this.isSupplyRequested(),
        true).then(
          function onSuccess()
          {
            self.resultCallback(new app.views.common.AppResultData({success: true}));
          },
          function onFailure()
          {
            self.resultCallback(new app.views.common.AppResultData({success: false}));
          }
    );
  },

  _handleBagContainerVisibility: function()
  {
    if (this.isInfusionBagEnabled() && this.getTherapy().isContinuousInfusion())
    {
      if (this._infusionSyringeChangeButton.isPressed())
      {
        this.getBagContainer().isRendered() ? this.getBagContainer().show() : this.getBagContainer().setHidden(false);
      }
      else
      {
        this.getBagContainer().isRendered() ? this.getBagContainer().hide() : this.getBagContainer().setHidden(true);
      }
    }
  },

  /**
   * @Override
   */
  onValidationSuccess: function()
  {
    this._confirmTherapyAdministration();
  }
});