Class.define('app.views.medications.timeline.administration.TherapyAdministrationDialogBuilder', 'tm.jquery.Object', {
  view: null,

  _therapyMedicationDataLoader: null,

  /**
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._therapyMedicationDataLoader = new app.views.medications.common.TherapyMedicationDataLoader({
      view: this.getView()
    })
  },

  /**
   * Returns a promise to preload the medication data and show the {@link TherapyAdministrationContainer} dialog.
   * The promise is resolved when the dialog's resultCallback is executed.
   *
   * @param {Object} timelineRowData
   * @param {app.views.medications.common.dto.Therapy|OxygenTherapy} therapy
   * @param {Array<Object>|null} administrations
   * @param {Object|null} administration
   * @param {Boolean} createNewTask
   * @param {String} containerTitle
   * @param {app.views.medications.TherapyEnums.therapyDoseTypeEnum} therapyDoseTypeEnum
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|therapyDoseTypeEnum} administrationType
   * @param {Boolean} editMode
   * @param {Boolean} stopFlow
   * @param {Number} scannedMedicationId
   * @param {String} barcode
   * @returns {tm.jquery.Promise}
   */
  showAdministrationDialog: function(timelineRowData, therapy, administrations, administration, createNewTask,
                                     containerTitle, therapyDoseTypeEnum, administrationType, editMode, stopFlow,
                                     scannedMedicationId, barcode)
  {
    var self = this;
    var dialogResultDeferred = new tm.jquery.Deferred();

    this._therapyMedicationDataLoader
        .load(therapy)
        .then(
            function onTherapyMedicationDataLoaderSuccess(medicationData)
            {
              self._buildAdministrationContainer(timelineRowData, therapy, medicationData, administrations, administration,
                  createNewTask, containerTitle, therapyDoseTypeEnum, administrationType, editMode, stopFlow,
                  dialogResultDeferred, scannedMedicationId, barcode);
            },
            function onTherapyMedicationDataLoaderFailure()
            {
              dialogResultDeferred.reject();
            });

    return dialogResultDeferred.promise();
  },

/**
   * Returns a promise to preload the medication data and show the {@link InfusionSetChangeAdministrationContainer} dialog.
   * The promise is resolved when the dialog's resultCallback is executed.
   *
   * @param {Object} timelineRowData
   * @param {app.views.medications.common.dto.Therapy|OxygenTherapy} therapy
   * @param {Array<Object>|null} administrations
   * @param {Object|null} administration
   * @param {Boolean} editMode
   * @returns {tm.jquery.Promise}
   */
  showInfusionSetChangeDialog: function(timelineRowData, therapy, administrations, administration, editMode)
  {
    var self = this;
    var dialogResultDeferred = new tm.jquery.Deferred();

    this._therapyMedicationDataLoader.load(therapy).then(
        function onDataLoad(medicationData)
        {
          self._buildInfusionSetChangeDialog(timelineRowData, therapy, medicationData, administrations, administration,
              editMode, dialogResultDeferred);
        },
        function onFailure()
        {
          dialogResultDeferred.reject();
        });

    return dialogResultDeferred.promise();
  },

  /**
   * @param {app.views.medications.timeline.administration.BaseTherapyAdministrationDataEntryContainer} administrationContainer
   * @returns {number} height of the administration dialog - which is more or less a static number, unless the size of
   * the screen is smaller than that in which case we utilise all available space.
   * @private
   */
  _calculateDialogHeight: function(administrationContainer)
  {
    var administration = administrationContainer.getAdministration();
    var administrationType = administrationContainer.getAdministrationType();
    var enums = app.views.medications.TherapyEnums;
    var desiredHeight = 720;

    // if the dialog doesn't have the 'not given' tab, we can get away with a smaller height even when there's a longer
    // warning present
    if (tm.jquery.Utils.isEmpty(administration) ||
        administrationType === enums.administrationTypeEnum.INFUSION_SET_CHANGE ||
        administrationType === enums.administrationTypeEnum.STOP)
    {
      desiredHeight -= 150;
    }
    
    var maxHeight = $(window).height();
    
    return desiredHeight >= maxHeight ? maxHeight - 20 : desiredHeight;
  },

  /**
   * @param {Object} timelineRowData
   * @param {app.views.medications.common.dto.Therapy|OxygenTherapy} therapy
   * @param {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData} medicationData
   * @param {Array<Object>|null} administrations
   * @param {Object|null} administration
   * @param {Boolean} createNewTask
   * @param {String} containerTitle
   * @param {app.views.medications.TherapyEnums.therapyDoseTypeEnum} therapyDoseTypeEnum
   * @param {app.views.medications.TherapyEnums.administrationTypeEnum|therapyDoseTypeEnum} administrationType
   * @param {Boolean} editMode
   * @param {Boolean} stopFlow
   * @param {Number} scannedMedicationId
   * @param {String} barcode
   * @param {tm.jquery.Deferred} dialogResultDeferred
   * @private
   */
  _buildAdministrationContainer: function(timelineRowData, therapy, medicationData, administrations, administration,
                                       createNewTask, containerTitle, therapyDoseTypeEnum, administrationType, editMode,
                                       stopFlow, dialogResultDeferred, scannedMedicationId, barcode)
  {
    var self = this;
    var lastPositiveInfusionRate = null;
    if ((therapy.isContinuousInfusion() || therapy.isOrderTypeOxygen()) && timelineRowData)
    {
      lastPositiveInfusionRate = timelineRowData.lastPositiveInfusionRate;
    }
    var config = this._buildBaseAdministrationConfig(timelineRowData, therapy, medicationData, administrations,
        administration, editMode);

    config.createNewTask = createNewTask;
    config.therapyDoseTypeEnum = therapyDoseTypeEnum;
    config.administrationType = administrationType;
    config.stopFlow = stopFlow === true;
    config.lastPositiveInfusionRate = lastPositiveInfusionRate;
    config.barcode = barcode;
    if (timelineRowData && timelineRowData.currentStartingDevice)
    {
      config.currentStartingDevice = timelineRowData.currentStartingDevice;
    }

    this._attachMedicationProductsOnConfig(therapy, administration, createNewTask, config, scannedMedicationId).then(
        function()
        {
          var therapyAdministrationContainer = new app.views.medications.timeline.administration.TherapyAdministrationDataEntryContainer(config);

          self.getView().setActionCallbackListener(therapyAdministrationContainer);
          self._buildAndShowAdministrationDialog(therapyAdministrationContainer,
              containerTitle,
              dialogResultDeferred);
        }
    );
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Object} config
   * @param {Boolean} createNewTask
   * @param {Number} scannedMedicationId
   * @returns {tm.jquery.Promise}
   * @private
   */
  _attachMedicationProductsOnConfig: function(therapy, administration, createNewTask, config, scannedMedicationId)
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    if (this._isMedicationSubstitutionPossible(therapy, createNewTask))
    {
      this._loadTherapyMedicationProducts(therapy).then(
          function(medicationProducts)
          {
            config.medicationProducts = medicationProducts;
            self._findPreselectedMedicationProduct(medicationProducts, administration, therapy, scannedMedicationId).then(
                function(productMedicationData)
                {
                  config.preselectedProductMedicationData = productMedicationData;
                  deferred.resolve();
                })
          });
    }
    else
    {
      deferred.resolve();
    }
    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} createNewTask
   * @return {Boolean}
   * @private
   */
  _isMedicationSubstitutionPossible: function(therapy, createNewTask)
  {
    return !createNewTask && this.getView().isSubstituteAdministrationMedicationEnabled() &&
        therapy.isOrderTypeSimple() && !therapy.getMainMedication().isMedicationUniversal();
  },

  /**
   * @param {Object} timelineRowData
   * @param {app.views.medications.common.dto.Therapy|OxygenTherapy} therapy
   * @param {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData} medicationData
   * @param {Array<Object>|null} administrations
   * @param {Object|null} administration
   * @param {Boolean} editMode
   * @param {tm.jquery.Deferred} dialogResultDeferred
   * @private
   */
  _buildInfusionSetChangeDialog: function(timelineRowData, therapy, medicationData, administrations, administration,
                                          editMode, dialogResultDeferred)
  {
    var enums = app.views.medications.TherapyEnums;

    var config = this._buildBaseAdministrationConfig(timelineRowData, therapy, medicationData, administrations,
        administration, editMode);
    config.administrationType = enums.administrationTypeEnum.INFUSION_SET_CHANGE;

    var infusionSetChangeContainer = new app.views.medications.timeline.administration.InfusionSetChangeDataEntryContainer(config);
    this.getView().setActionCallbackListener(infusionSetChangeContainer);

    this._buildAndShowAdministrationDialog(infusionSetChangeContainer,
        this.getView().getDictionary("infusion.set.change"),
        dialogResultDeferred);
  },

  /**
   * @param {Object} timelineRowData
   * @param {app.views.medications.common.dto.Therapy|OxygenTherapy} therapy
   * @param {Array<app.views.medications.common.dto.MedicationData>|app.views.medications.common.dto.MedicationData} medicationData
   * @param {Array<Object>|null} administrations
   * @param {Object|null} administration
   * @param {Boolean} editMode
   * @returns {Object}
   * @private
   */
  _buildBaseAdministrationConfig: function(timelineRowData, therapy, medicationData, administrations, administration,
                                           editMode)
  {
    var enums = app.views.medications.TherapyEnums;
    var latestTherapyVersion = timelineRowData && timelineRowData.therapy !== therapy ? timelineRowData.therapy : null;

    // Properties from timeline row - only for additional actions for continuous infusion.
    var infusionActive = true;
    if ((therapy.isContinuousInfusion() || therapy.isOrderTypeOxygen()) && timelineRowData)
    {
      infusionActive = timelineRowData.infusionActive;
    }

    var therapyReviewedUntil = null;
    if (timelineRowData && timelineRowData.reviewedUntil)
    {
      therapyReviewedUntil = new Date(timelineRowData.reviewedUntil);
    }

    return {
      view: this.getView(),
      startProcessOnEnter: false,
      therapy: therapy,
      administrations: administrations,
      administration: administration,
      patientId: this.patientId,
      administrationType: enums.administrationTypeEnum.INFUSION_SET_CHANGE,
      editMode: editMode,
      therapyReviewedUntil: therapyReviewedUntil,
      latestTherapyVersion: latestTherapyVersion,
      infusionActive: infusionActive,
      medicationData: medicationData,
      witnessingRequired: this._isWitnessingRequired(timelineRowData.witnessingRequired)
    };
  },

  /**
   * Adjusts specifics for therapy administration dialogs.
   * @param {app.views.medications.timeline.administration.BaseTherapyAdministrationDataEntryContainer|*} administrationContainer
   * @param {String} title
   * @param {tm.jquery.Deferred} dialogResultDeferred
   * @private
   */
  _buildAndShowAdministrationDialog: function(administrationContainer, title, dialogResultDeferred)
  {
    var height = this._calculateDialogHeight(administrationContainer);

    var appFactory = this.getView().getAppFactory();
    var dialog = appFactory.createDataEntryDialog(
        title,
        null,
        administrationContainer,
        function(resultData)
        {
          dialogResultDeferred.resolve(resultData);
        },
        475,
        height
    );
    var self = this;
    dialog.setContainmentElement(this.getView().getDom());
    dialog.header.setCls("therapy-admin-header");
    dialog.addTestAttribute('therapy-administration-dialog');
    dialog.getFooter().setCls("therapy-admin-footer");
    dialog.getFooter().rightContainer.layout.gap = 0;

    dialog.setLeftButtons([administrationContainer.getResetButton()]);
    dialog.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self.getView().hideLoaderMask();
    });
    administrationContainer.setDialog(dialog);
    dialog.show();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @returns {tm.jquery.Promise}
   * @private
   */
  _loadTherapyMedicationProducts: function(therapy)
  {
    var medicationProductsDeferred = tm.jquery.Deferred.create();
    var releaseDetails = therapy.getReleaseDetails();

    this.getView()
        .getRestApi()
        .loadMedicationProducts(
            therapy.getMedication().getId(),
            therapy.getRoutes(),
            !!releaseDetails ? releaseDetails.getType() : null,
            !!releaseDetails ? releaseDetails.getHours() : null,
            false)
        .then(
            function(data)
            {
              medicationProductsDeferred.resolve(data);
            },
            function()
            {
              medicationProductsDeferred.reject();
            });
    
    return medicationProductsDeferred.promise();
  },

  /**
   * @param {Array<app.views.medications.common.dto.Medication>} medicationProducts
   * @param {Object} administration
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Number} scannedMedicationId
   * @returns {tm.jquery.Promise}
   * @private
   */
  _findPreselectedMedicationProduct: function(medicationProducts, administration, therapy, scannedMedicationId)
  {
    var deferredLoad = tm.jquery.Deferred.create();
    var medicationIdToCompare = administration && !tm.jquery.Utils.isEmpty(administration.getSubstituteMedication()) ?
        administration.getSubstituteMedication().getId() :
        therapy.getMedicationId();

    var matchingProductId = null;
    if (scannedMedicationId)
    {
      matchingProductId = scannedMedicationId;
    }
    else
    {
      var matchingProduct = app.views.medications.MedicationUtils.findInArray(medicationProducts, isMatchById);

      matchingProductId = matchingProduct ? matchingProduct.id :
          (medicationProducts.length >= 1 ? medicationProducts[0].id : null);
    }

    if (matchingProductId)
    {
      this.getView().getRestApi().loadMedicationData(matchingProductId).then(
          function(data)
          {
            deferredLoad.resolve(data)
          },
          function()
          {
            deferredLoad.reject();
          });
    }
    else
    {
      deferredLoad.resolve(null);
    }

    return deferredLoad.promise();

    function isMatchById(product)
    {
      return product.id === medicationIdToCompare;
    }
  },

  /**
   * @param {Boolean} witnessingRequiredByTherapy
   * @returns {Boolean}
   * @private
   */
  _isWitnessingRequired: function(witnessingRequiredByTherapy)
  {
    var view = this.getView();

    if (view.isAdministrationWitnessingEnabled())
    {
      return view.getPatientData().isWitnessingRequired() ||
          view.getTherapyAuthority().isUserWitnessingRequired() ||
          witnessingRequiredByTherapy === true
    }
    else
    {
      return false;
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});