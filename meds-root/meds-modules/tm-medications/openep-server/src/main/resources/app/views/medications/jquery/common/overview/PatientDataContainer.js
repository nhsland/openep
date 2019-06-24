/**
 * This component represents the patient banner found on top of the screen, underneath the main (navigational) toolbar.
 * The patient data consists of the reference weight, body area and allergies and should be shown on all the TherapyView's
 * sub views. Additionally, the component can also show the cumulative anti-psychotic dose value obtained from the
 * TherapyView, which is reloaded independently (when enabled by the server configuration), and optionally the additional
 * warnings and discharge list status notifications, whose content is static and intended to be shown only on some of the
 * sub views. Their visibility can therefore be controlled directly, while all other content's visibility also depends on
 * the presence of the related data and needs to be rebuilt when the data changes.
 *
 * Do not call any of the public methods, except {@link app.views.medications.common.overview.PatientDataContainer#clear},
 * before the component is fully rendered. See {@link app.views.medications.common.overview.PatientDataContainer#_buildGui}
 * for more information.
 */
Class.define('app.views.medications.common.overview.PatientDataContainer', 'tm.jquery.Container', {
  /** @type string */
  cls: 'patient-data-container',
  /** @type string */
  testAttribute: 'patient-data-container',
  /** @type tm.views.medications.TherapyView|app.views.common.AppView */
  view: null,

  /** @type tm.jquery.Container */
  _patientNameContainer: null,
  /** @type tm.jquery.Container */
  _patientWeightContainer: null,
  /** @type tm.jquery.Container */
  _patientSurfaceAreaContainer: null,
  /** @type tm.jquery.Container */
  _cumulativeMaxDoseContainer: null,
  /** @type tm.jquery.Container */
  _patientAllergiesContainer: null,
  /** @type tm.jquery.Container */
  _timelineWarningsNotificationContainer: null,
  /** @type tm.jquery.Container */
  _dischargeListNotificationContainer: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * Should be called after each change of the {@link tm.views.medications.TherapyView#getPatientData} and will also
   * update the presented patient reference data obtained from {@link tm.views.medications.TherapyView#getActiveViewData}.
   */
  refreshPatientData: function()
  {
    this._refreshPatientName();
    this.refreshViewReferenceData();
    this._refreshPatientAllergies();
  },

  /**
   * Should be called after each change of the {@link tm.views.medications.TherapyView#getCumulativeMaxDosePercentage}.
   */
  refreshCumulativeMaxDose: function()
  {
    if (!this._cumulativeMaxDoseContainer)
    {
      return;
    }

    var cumulativeMaxDosePercentageInfo = app.views.medications.MedicationUtils.createMaxDosePercentageInfoHtml(
        this.view,
        this.view.getCumulativeMaxDosePercentage(),
        true);
    if (!tm.jquery.Utils.isEmpty(cumulativeMaxDosePercentageInfo))
    {
      this._cumulativeMaxDoseContainer.setHtml(cumulativeMaxDosePercentageInfo);
      this._showCumulativeMaxDoseContainer();
    }
    else
    {
      this._hideAndClearCumulativeMaxDoseContainer();
    }

  },

  /**
   * Should be called after each change of the {@link tm.views.medications.TherapyView#getActiveViewData}.
   */
  refreshViewReferenceData: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var utils = app.views.medications.MedicationUtils;
    var referenceData = new app.views.medications.common.patient.ViewBasedReferenceData({view: this.view});

    if (!!referenceData.getWeight())
    {
      this._patientWeightContainer.setHtml(tm.jquery.Utils.formatMessage(
          '<span class="TextLabel">{0}:&nbsp;</span><span class="TextData">{1}</span>' +
          '<span class="TextUnit">&nbsp;{2}&nbsp;({3})</span>',
          this.view.getDictionary('reference.weight'),
          utils.getFormattedDecimalNumber(utils.doubleToString(referenceData.getWeight())),
          this.view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.KG).getDisplayName(),
          this.view.getDisplayableValue(referenceData.getWeightDate(), "short.date")));
      this._patientWeightContainer.show();
    }
    else
    {
      this._hideAndClearPatientWeightContainer();
    }

    if (!!referenceData.getHeight() && !!referenceData.getWeight())
    {
      var patientBodySurfaceArea = referenceData.getBodySurfaceArea();
      this._patientSurfaceAreaContainer.setHtml(
          tm.jquery.Utils.formatMessage(
              '<span class="TextLabel">{0}:&nbsp;</span><span class="TextData">{1}</span>' +
              '<span class="TextUnit">&nbsp;{2}</span>',
              this.view.getDictionary('body.surface'),
              utils.getFormattedDecimalNumber(utils.doubleToString(patientBodySurfaceArea, 'n3')),
              this.view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.M2).getDisplayName())
      );
      this._patientSurfaceAreaContainer.show();
    }
    else
    {
      this._hideAndClearPatientSurfaceAreaContainer();
    }
  },

  /**
   * @param {Boolean} visible
   */
  applyTimelineAdditionalWarningsVisibility: function(visible)
  {
    visible === true ? this._showTimelineWarningsNotification() : this._hideTimelineWarningsNotification();
  },

  /**
   * If requested by param showIfPresent, retrieves the patients discharge list status from the server and if the list has
   * been created, displays a notification about it. Otherwise hides the notification.
   * @param {Boolean} [showIfPresent=false]
   */
  applyDischargeListNotificationVisibility: function(showIfPresent)
  {
    if (!this._dischargeListNotificationContainer)
    {
      return;
    }

    if (showIfPresent === true)
    {
      var self = this;
      var patientId = this.view.getPatientId();

      this._loadDischargeListStatus().then(
          function(dischargeListCreated)
          {
            // skip applying changes if the patient changes in between ajax calls or the container is removed
            if (patientId !== self.view.getPatientId() || !self._dischargeListNotificationContainer)
            {
              return;
            }

            dischargeListCreated === true ?
                self._showDischargeListNotification() :
                self._hideDischargeListNotification();
          })
    }
    else
    {
      this._hideDischargeListNotification();
    }
  },

  /**
   * Hides all the content containers.
   */
  clear: function()
  {
    this._hideAndClearPatientNameContainer();
    this._hideAndClearPatientWeightContainer();
    this._hideAndClearPatientSurfaceAreaContainer();
    this._hideAndClearPatientAllergiesContainer();
    this._hideAndClearCumulativeMaxDoseContainer();
    this._hideTimelineWarningsNotification();
    this._hideDischargeListNotification();
  },

  /**
   * Creates child containers for each group of data shown by the component. Child containers are initially without any
   * content and hidden, as they become visible once the appropriate refresh method is called if the related data is
   * available, which needs to be done manually after each data (re)load. Since the additional warnings and discharge list
   * notification content is static, their visibility is controlled directly by calling the related 'apply visibility'
   * methods. Such a design was used to avoid issues related to resolved ajax calls attempting to change the child
   * container's DOM while the component was still being rendered.
   * The {@link #_dischargeListNotificationContainer} should be positioned in the right corner of the component, so all
   * the other child containers are wrapped in an additional container.
   *
   * @private
   */
  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start', 0));

    var patientDataContentContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start', 0)
    });
    this._patientNameContainer = new tm.jquery.Container({
      cls: 'patient-data-attribute-container',
      testAttribute: 'patient-name-container',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      hidden: true
    });
    patientDataContentContainer.add(this._patientNameContainer);

    this._patientWeightContainer = new tm.jquery.Container({
      cls: 'patient-data-attribute-container',
      testAttribute: 'weight-container',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      hidden: true
    });
    patientDataContentContainer.add(this._patientWeightContainer);

    this._patientSurfaceAreaContainer = new tm.jquery.Container({
      cls: 'patient-data-attribute-container',
      testAttribute: 'body-area-container',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      hidden: true
    });
    patientDataContentContainer.add(this._patientSurfaceAreaContainer);

    if (this.view.getCumulativeAntipsychoticDoseEnabled())
    {
      this._cumulativeMaxDoseContainer = new tm.jquery.Container({
        cls: 'patient-data-attribute-container',
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
        hidden: true
      });
      patientDataContentContainer.add(this._cumulativeMaxDoseContainer);
    }

    this._timelineWarningsNotificationContainer = this._createAdditionalWarningsContainer();
    patientDataContentContainer.add(this._timelineWarningsNotificationContainer);

    this._patientAllergiesContainer = this._createAllergiesContainer();
    patientDataContentContainer.add(this._patientAllergiesContainer);

    this._dischargeListNotificationContainer = new tm.jquery.Container({
      cls: 'discharge-notification',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      html: '<span class="TextLabel">' + this.view.getDictionary('discharge.prescription') + ': ' + '</span>' +
          '<span class="TextData">' + this.view.getDictionary('done') + '</span>',
      hidden: true
    });
    this.add(patientDataContentContainer);
    this.add(this._dischargeListNotificationContainer);
  },

  /**
   * Creates a container containing the static notification about the presence of additional warnings about the current
   * prescriptions. Initially hidden, should be displayed via {@link #applyTimelineAdditionalWarningsVisibility} when
   * necessary.
   * @returns {tm.jquery.Container}
   * @private
   */
  _createAdditionalWarningsContainer: function()
  {
    var timelineWarningsContainer = new tm.jquery.Container({
      cls: 'patient-data-attribute-container',
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start'),
      hidden: true
    });
    var patientAdditionalWarningsIcon = new tm.jquery.Container({
      cls: 'high-alert-icon',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
    var patientAdditionalWarnings = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
    patientAdditionalWarnings.setHtml(
        '<span class="TextLabel">' + this.view.getDictionary('warning') + ': ' + '</span>' +
        '<span class="TextData">' + this.view.getDictionary('therapy.risk.warning') + '</span>');
    timelineWarningsContainer.add(patientAdditionalWarningsIcon);
    timelineWarningsContainer.add(patientAdditionalWarnings);

    return timelineWarningsContainer;
  },

  /**
   * Creates a container for allergies. Keep in mind the flex-grow in relation to other containers, since we anticipate the
   * possibility that this container will include a larger amount of data. Initially hidden as the content and visibility is
   * controlled by {@link #_refreshPatientAllergies}.
   * @returns {tm.jquery.Container}
   * @private
   */
  _createAllergiesContainer: function()
  {
    return new tm.jquery.Container({
      cls: 'patient-data-attribute-container',
      testAttribute: 'allergies-container',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      hidden: true
    });
  },

  /**
   * @private
   */
  _refreshPatientName: function()
  {
    if (!!this.view.getPatientData().getPatientName())
    {
      this._patientNameContainer.setHtml(
          '<span class="TextLabel">' +
          this.view.getDictionary('name') + ':&nbsp;' +
          '</span><span class="TextData">' +
          tm.jquery.Utils.escapeHtml(this.view.getPatientData().getPatientName()) +
          '</span>');
      this._patientNameContainer.show();
    }
    else
    {
      this._hideAndClearPatientNameContainer();
    }
  },

  /**
   * Recreates the content of the {@link _patientAllergiesContainer} and toggles the visibility of the content based on
   * the presence of patient allergies information.
   * @private
   */
  _refreshPatientAllergies: function()
  {
    var patientData = this.view.getPatientData();
    if (!!patientData && patientData.hasAllergiesInformation())
    {
      this._patientAllergiesContainer.setHtml(this._createAllergiesHtmlDisplay(patientData));
      this._patientAllergiesContainer.show();
    }
    else
    {
      this._hideAndClearPatientAllergiesContainer();
    }
  },

  /**
   * @param {app.views.medications.common.dto.PatientDataForMedications} patientData
   * @return {string|null} HTML safe representation of either therapy status, or the names of known patient's allergies,
   * if any are present.
   * @private
   */
  _createAllergiesHtmlDisplay: function(patientData)
  {
    return '<span class="TextLabel">' +
        this.view.getDictionary('allergies') +
        ':&nbsp;</span>' +
        '<span class="TextData">' +
        (!!patientData.getAllergies() ?
            tm.jquery.Utils.escapeHtml(this._mapToCsvNames(patientData.getAllergies())) :
            this.view.getDictionary('AllergiesStatus.' + patientData.getAllergiesStatus())) +
        '</span>';
  },

  /**
   * @param {Array<{name: string}>} entities
   * @return {string|undefined} a comma separated list of all entity names
   * @private
   */
  _mapToCsvNames: function(entities)
  {
    return entities
        .map(function toName(item)
        {
          return item.name;
        })
        .join(', ');
  },

  /**
   * @returns {tm.jquery.Promise}
   * @private
   */
  _loadDischargeListStatus: function()
  {
    var deferred = tm.jquery.Deferred.create();
    this.view.getRestApi().isDischargeCreated(true).then(
        /**
         * @param {Boolean} dischargeCreated
         */
        function(dischargeCreated)
        {
          deferred.resolve(dischargeCreated);
        },
        function()
        {
          deferred.reject();
        });
    return deferred.promise();
  },

  /** Ensures the correct method is called based on the render state and removes the container's content. */
  _hideAndClearPatientNameContainer: function()
  {
    this._patientNameContainer.isRendered() ?
        this._patientNameContainer.hide() :
        this._patientNameContainer.setHidden(true);
    this._patientNameContainer.setHtml(null);
  },

  /** Ensures the correct method is called based on the render state and removes the container's content. */
  _hideAndClearPatientWeightContainer: function()
  {
    this._patientWeightContainer.isRendered() ?
        this._patientWeightContainer.hide() :
        this._patientWeightContainer.setHidden(true);
    this._patientWeightContainer.setHtml(null);
  },

  /** Ensures the correct method is called based on the render state and removes the container's content. */
  _hideAndClearPatientSurfaceAreaContainer: function()
  {
    this._patientSurfaceAreaContainer.isRendered() ?
        this._patientSurfaceAreaContainer.hide() :
        this._patientSurfaceAreaContainer.setHidden(true);
    this._patientSurfaceAreaContainer.setHtml(null);
  },

  /** Ensures the correct method is called based on the render state and removes the container's content. */
  _hideAndClearCumulativeMaxDoseContainer: function()
  {
    if (!this._cumulativeMaxDoseContainer)
    {
      return;
    }
    this._cumulativeMaxDoseContainer.isRendered() ?
        this._cumulativeMaxDoseContainer.hide() :
        this._cumulativeMaxDoseContainer.setHidden(true);
    this._cumulativeMaxDoseContainer.setHtml(null);
  },

  /** Ensures the correct method is called based on the render state. */
  _showCumulativeMaxDoseContainer: function()
  {
    if (!this._cumulativeMaxDoseContainer)
    {
      return;
    }
    this._cumulativeMaxDoseContainer.isRendered() ?
        this._cumulativeMaxDoseContainer.show() :
        this._cumulativeMaxDoseContainer.setHidden(false);
  },

  /** Ensures the correct method is called based on the render state and removes the container's content. */
  _hideAndClearPatientAllergiesContainer: function()
  {
    this._patientAllergiesContainer.isRendered() ?
        this._patientAllergiesContainer.hide() :
        this._patientAllergiesContainer.setHidden(true);
    this._patientAllergiesContainer.setHtml(null);
  },

  /** Ensures the correct method is called based on the render state. */
  _hideTimelineWarningsNotification: function()
  {
    this._timelineWarningsNotificationContainer.isRendered() ?
        this._timelineWarningsNotificationContainer.hide() :
        this._timelineWarningsNotificationContainer.setHidden(true);
  },

  /** Ensures the correct method is called based on the render state. */
  _showTimelineWarningsNotification: function()
  {
    this._timelineWarningsNotificationContainer.isRendered() ?
        this._timelineWarningsNotificationContainer.show() :
        this._timelineWarningsNotificationContainer.setHidden(false);
  },

  /** Ensures the correct method is called based on the render state. */
  _hideDischargeListNotification: function()
  {
    this._dischargeListNotificationContainer.isRendered() ?
        this._dischargeListNotificationContainer.hide() :
        this._dischargeListNotificationContainer.setHidden(true);
  },

  /** Ensures the correct method is called based on the render state. */
  _showDischargeListNotification: function()
  {
    this._dischargeListNotificationContainer.isRendered() ?
        this._dischargeListNotificationContainer.show() :
        this._dischargeListNotificationContainer.setHidden(false);
  }
});