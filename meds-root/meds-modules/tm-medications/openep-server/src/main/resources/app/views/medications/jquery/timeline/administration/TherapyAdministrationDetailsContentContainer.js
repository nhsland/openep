Class.define('app.views.medications.timeline.administration.TherapyAdministrationDetailsContentContainer', 'app.views.medications.common.therapy.BaseTherapyDetailsContentContainer', {
  scrollable: 'both',

  administration: null,
  reasonMap: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this.reasonMap = this.view.getChangeReasonTypeHolder().getMap();
    this._buildContentContainer();
  },

  /**
   * @returns {Object|null}
   */
  getAdministration: function()
  {
    return this.administration;
  },

  _buildContentContainer: function()
  {
    var enums = app.views.medications.TherapyEnums;

    this._addAdministrationResultRow();
    this._addAdministrationTimeRow();
    this._addInfusionSetVolumeRow();

    this._addInfusionSetChangeRow();
    this._addSubstituteMedicationRow();
    this._addWitnessRow();
    this._addCommentRow();
    this._addComposerRow();

    if (this.getAdministration().isAdministrationAdministered())
    {
      this._addAdministeredVolumeRow();
      this._addAdministeredQuantityRow();
      this._addAdministeredDoseRow();
      this._addStartingDeviceRow();
      this._addRouteRow();
    }

    this._addPlannedTimeRow();
    this._addPlannedDoseRow();
    this._addPlannedStartingDeviceRow();
    this._addDoctorsCommentRow();
  },

  _addPlannedTimeRow: function()
  {
    if (this.getAdministration().plannedTime)
    {
      var view = this.getView();
      this._contentContainer.add(new tm.jquery.Container({cls: 'administration-card-border'}));
      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('administration.planned.time'),
          view.getDisplayableValue(new Date(this.getAdministration().plannedTime), "short.date.time")));
    }
  },

  _addPlannedDoseRow: function()
  {
    if (this.getAdministration().getPlannedDose())
    {
      var view = this.getView();
      var utils = app.views.medications.MedicationUtils;

      var plannedDose = this.getAdministration().getPlannedDose();
      var isOxygen = this.getTherapy().isOrderTypeOxygen();

      var plannedNumeratorString =
          plannedDose.getNumerator() ? utils.getFormattedDecimalNumber(utils.doubleToString(plannedDose.getNumerator(), 'n2')) + ' ' +
          utils.getFormattedUnit(plannedDose.getNumeratorUnit(), view) : '';
      var plannedDenominatorString =
          plannedDose.getDenominator() ? utils.getFormattedDecimalNumber(utils.doubleToString(plannedDose.getDenominator(), 'n2')) +
          ' ' + utils.getFormattedUnit(plannedDose.getDenominatorUnit(), view) : '';
      var plannedDoseString =
          plannedDenominatorString ? (plannedNumeratorString + ' / ' + plannedDenominatorString) : plannedNumeratorString;

      this._contentContainer.add(this._buildLabelDataRowContainer(
          isOxygen ? view.getDictionary('planned.rate') : view.getDictionary('dose.planned'),
          plannedDoseString,
          'planned-rate',
          true));
    }
  },

  _addInfusionSetChangeRow: function()
  {
    if (this.getAdministration().getInfusionSetChangeEnum())
    {
      var view = this.getView();

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("InfusionSetChangeEnum." +
          this.getAdministration().getInfusionSetChangeEnum()), ""));
    }
  },

  _addInfusionSetVolumeRow: function()
  {
    if (this.getAdministration().getInfusionBag() && this.getAdministration().getInfusionBag().quantity !== null)
    {
      var infusionBagQuantity = this.getAdministration().getInfusionBag().quantity;
      var infusionBagUnit = this.getAdministration().getInfusionBag().unit;
      var infusionBagChange = infusionBagQuantity + " " + infusionBagUnit;

      var view = this.getView();
      this._contentContainer.add(
          this._buildLabelDataRowContainer(view.getDictionary("bag.syringe.volume"), infusionBagChange));
    }
  },

  _addAdministrationResultRow: function()
  {
    if (this.getAdministration().getAdministrationResult())
    {
      var reason;
      var administrationResultString = "";
      var enums = app.views.medications.TherapyEnums;
      var view = this.getView();

      if (this.getAdministration().getAdministrationResult() === enums.administrationResultEnum.GIVEN)
      {
        administrationResultString += view.getDictionary("given");
      }
      else if (this.getAdministration().getAdministrationResult() === enums.administrationResultEnum.DEFER)
      {
        reason = this._getNotAdministeredReasonFromMap(this.reasonMap.ADMINISTRATION_DEFER);
      }
      else if (this.getAdministration().getAdministrationResult() === enums.administrationResultEnum.SELF_ADMINISTERED)
      {
        administrationResultString += this.getDisplayProvider().getSelfAdminStatusDescription(
            this.getAdministration().getSelfAdministrationType());
      }
      else if (this.getAdministration().getAdministrationResult() === enums.administrationResultEnum.NOT_GIVEN)
      {
        if (this.getAdministration().isAdministrationCancelled())
        {
          administrationResultString += view.getDictionary('cancelled');
        }
        else
        {
          administrationResultString += view.getDictionary('not.given');
          reason = this._getNotAdministeredReasonFromMap(this.reasonMap.ADMINISTRATION_NOT_GIVEN);
        }
      }
      if (!tm.jquery.Utils.isEmpty(reason))
      {
        administrationResultString += ", " + reason.name;
      }
      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary("administration"),
          administrationResultString));
    }
  },

  _addSubstituteMedicationRow: function()
  {
    if (this.getAdministration().getSubstituteMedication())
    {
      var view = this.getView();
      var medicationDisplay = this.getDisplayProvider().getMedicationNameDisplay(
          this.getAdministration().getSubstituteMedication(), true);

      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("medication"),
              medicationDisplay,
              'substitute-medication',
              true));
    }
  },

  _addAdministrationTimeRow: function()
  {
    if (this.getAdministration().getAdministrationTime())
    {
      var view = this.getView();

      this._contentContainer.add(this._buildLabelDataRowContainer(
          this.getAdministration().isAdministrationAdministered() ?
              view.getDictionary("administration.time") :
              view.getDictionary("time.documented"),
          view.getDisplayableValue(this.getAdministration().getAdministrationTime(), "short.date.time")));
    }
  },

  _addAdministeredVolumeRow: function()
  {
    if (this.getAdministration().getAdministeredDose() &&
        this.getAdministration().getAdministeredDose().getTherapyDoseTypeEnum() === 'RATE_VOLUME_SUM')
    {
      var view = this.getView();
      var utils = app.views.medications.MedicationUtils;
      var volumeData = utils.getFormattedDecimalNumber(
          utils.doubleToString(this.getAdministration().getAdministeredDose().getSecondaryNumerator(), 'n2'))
          + ' ' + utils.getFormattedUnit(this.getAdministration().getAdministeredDose().getSecondaryNumeratorUnit(), view);

      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("volume.total"),
              volumeData,
              'volume-total',
              true));
    }
  },

  _addAdministeredQuantityRow: function()
  {
    if (this.getAdministration().getAdministeredDose() &&
        this.getAdministration().getAdministeredDose().getTherapyDoseTypeEnum() === 'RATE_QUANTITY')
    {
      var view = this.getView();
      var utils = app.views.medications.MedicationUtils;
      var quantityData = utils.getFormattedDecimalNumber(
              utils.doubleToString(this.getAdministration().getAdministeredDose().getSecondaryNumerator(), 'n2')) + ' ' +
          utils.getFormattedUnit(this.getAdministration().getAdministeredDose().getSecondaryNumeratorUnit(), view) + ' / '
          + utils.getFormattedDecimalNumber(
              utils.doubleToString(this.getAdministration().getAdministeredDose().getSecondaryDenominator(), 'n2'))
          + ' ' + utils.getFormattedUnit(this.getAdministration().getAdministeredDose().getSecondaryDenominatorUnit(), view);

      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              view.getDictionary("quantity"),
              quantityData,
              'quantity',
              true));

    }
  },

  _addAdministeredDoseRow: function()
  {
    if (this.getAdministration().getAdministeredDose())
    {
      var view = this.getView();
      var isOxygen = this.getTherapy().isOrderTypeOxygen();
      var doseString = app.views.medications.MedicationUtils.buildAdministeredDoseDisplayString(
          this.getAdministration(), false, view);

      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              isOxygen ? view.getDictionary('rate') : view.getDictionary('dose'),
              doseString,
              'administered-dose',
              true));
    }
  },
  _addPlannedStartingDeviceRow: function()
  {
    if (this.getAdministration().getPlannedStartingDevice())
    {
      var view = this.getView();
      var route = this.getAdministration().getPlannedStartingDevice().getRoute();
      var routeType = this.getAdministration().getPlannedStartingDevice().getRouteType();
      var plannedStartingDeviceString = view.getDictionary('MedicalDeviceEnum.' + route);

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('planned.device'),
          routeType ? plannedStartingDeviceString + " " + routeType : plannedStartingDeviceString));
    }
  },

  _addStartingDeviceRow: function()
  {
    if (this.getAdministration().getStartingDevice())
    {
      var view = this.getView();
      var route = this.getAdministration().getStartingDevice().getRoute();
      var routeType = this.getAdministration().getStartingDevice().getRouteType();
      var startingDeviceString = view.getDictionary('MedicalDeviceEnum.' + route);

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('device'),
          routeType ? startingDeviceString + " " + routeType : startingDeviceString));
    }
  },

  _addRouteRow: function()
  {
    if (this.getAdministration().getRoute() && this.getAdministration().getRoute().getName())
    {
      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              this.getView().getDictionary('route'),
              " " + this.getAdministration().getRoute().getName()));
    }
  },

  _addWitnessRow: function()
  {
    if (this.getAdministration().getWitness())
    {
      var view = this.getView();
      var witnessString = this.getAdministration().getWitness().name;

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('witness'), witnessString));
    }
  },

  /**
   * The cancellation reason is stored in the
   * {@link app.views.medications.timeline.administration.dto.Administration#comment} field. Since only one value can be
   * stored in that field we can presume that for cancelled administrations, this is always the cancellation reason.
   * @private
   */
  _addCommentRow: function()
  {
    if (this.getAdministration().getComment())
    {
      var view = this.getView();

      this._contentContainer.add(
          this._buildLabelDataRowContainer(
              this.getAdministration().isAdministrationCancelled() ?
                  view.getDictionary('cancellation.reason') :
                  view.getDictionary('commentary'),
          this.getAdministration().getComment()));
    }
  },

  _addComposerRow: function()
  {
    if (this.getAdministration().getComposerName())
    {
      var view = this.getView();

      this._contentContainer.add(this._buildLabelDataRowContainer(
          this.getAdministration().isAdministrationAdministered() ?
              view.getDictionary('administered.by') :
              view.getDictionary('documented.by'),
          this.getAdministration().getComposerName()));
    }
  },

  /**
   * @param {null|Object.<string, Array<{{id: string, name: string>>} map
   * @returns {null|Object.<{id: string, name: string}>}
   * @private
   */
  _getNotAdministeredReasonFromMap: function(map)
  {
    var reasonCode = !tm.jquery.Utils.isEmpty(this.getAdministration().getNotAdministeredReason()) ?
        this.getAdministration().getNotAdministeredReason().code :
        null;
    if (!tm.jquery.Utils.isEmpty(reasonCode))
    {
      var reason = this._getReasonFromMap(map, reasonCode);
      if (!tm.jquery.Utils.isEmpty(reason))
      {
        return reason;
      }
    }
    return null;
  },

  _getReasonFromMap: function(map, code)
  {
    if (tm.jquery.Utils.isEmpty(map)) return {};

    var reason = null;
    map.forEach(function(r)
    {
      if (r.code === code) reason = r
    });
    return reason;
  },

  _addDoctorsCommentRow: function()
  {
    if (this.getAdministration().getDoctorsComment())
    {
      var view = this.getView();

      this._contentContainer.add(this._buildLabelDataRowContainer(view.getDictionary('doctors.comment'),
          this.getAdministration().getDoctorsComment()));
    }
  }
});