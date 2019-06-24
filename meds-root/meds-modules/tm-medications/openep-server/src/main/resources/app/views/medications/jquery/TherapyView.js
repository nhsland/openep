Class.define('tm.views.medications.TherapyView', 'app.views.common.AppView', {
  cls: "v-therapy-view",

  mode: "FLOW", //FLOW, ORDERING, EDIT, ORDERING_PAST, EDIT_PAST (TherapyPaneModeEnum.java)

  /** properties */
  pharmacistReviewReferBackPreset: false,
  mentalHealthReportEnabled: false,
  medicationsSupplyPresent: false,
  autoAdministrationCharting: false,
  cumulativeAntipsychoticDoseEnabled: false,
  infusionBagEnabled: false,
  medicationsShowHeparinPane: false,
  outpatientPrescriptionType: null,
  doctorReviewEnabled: false,
  doseRangeEnabled: false,
  formularyFilterEnabled: false,
  substituteAdministrationMedicationEnabled: false,
  administrationWitnessingEnabled: false,
  administrationWitnessingMocked: false,
  administrationWitnessingIvRequired: false,
  antimicrobialDaysCountStartsWithOne: false,
  referenceWeightRequired: true,
  presetPastAdministrationTimeToNow: false,
  /** @type number */
  pharmacistReviewDisplayDays: NaN,
  /** @type boolean */
  suspendReasonMandatory: false,
  /** @type boolean */
  stopReasonMandatory: false,

  /** update data */
  dayCount: null,
  groupField: null,
  patientId: null,
  userPerson: null,
  context: null,
  surgeryReportEnabled: false,
  therapySortTypeEnum: null,
  nonFormularyMedicationSearchAllowed: null,

  presetMedicationId: null,
  presetDate: null,
  therapyToEdit: null,

  /** privates */
  therapyGrid: null,
  header: null,
  actionsHeader: null,
  timelineContainer: null,
  therapyEnums: null,
  medications: null,
  careProfessionals: null,
  doseForms: null,
  routes: null,
  problemDescriptionNamedIdentitiesMap: null,
  displayTypeButtonGroup: null,
  flowGridTypeButton: null,
  timelineTypeButton: null,
  pharmacistTypeButton: null,
  reconciliationSummaryTypeButton: null,
  pharmacistReviewContainer: null,
  summaryContainer: null,
  documentationContainer: null,
  actionCallbackListener: null,

  _activeViewData: null,
  _patientData: null,
  _patientDataContainer: null,
  _presentDataConditionalTask: null,
  _therapyAuthority: null,
  _unitsHolder: null,
  _informationSourceHolder: null,
  _changeReasonTypeHolder: null,
  _testRenderCoordinator: null,
  _restApi: null,
  _restErrorLogger: null,
  _cumulativeMaxDosePercentage: null,

  /** statics */
  statics: {
    VIEW_ACTION_CANCEL_PRESCRIPTION: 'cancelPrescription',
    VIEW_ACTION_OUTPATIENT_PRESCRIPTION: 'outpatientPrescription',
    VIEW_ACTION_DELETE_OUTPATIENT_PRESCRIPTION: 'deleteOutpatientPrescription',
    VIEW_ACTION_UPDATE_OUTPATIENT_PRESCRIPTION: 'updateOutpatientPrescription',
    VIEW_ACTION_GET_EXTERNAL_OUTPATIENT_PRESCRIPTIONS: 'getExternalOutpatientPrescription',
    VIEW_ACTION_AUTHORIZE_OUTPATIENT_PRESCRIPTION: 'authorizeOutpatientPrescription',
    VIEW_ACTION_AUTHENTICATE_ADMINISTRATION_WITNESS: 'authenticateAdministrationWitness',
    VIEW_ACTION_CREATE_AND_OPEN_FILE: 'createAndOpenFile',

    SERVLET_PATH_GET_TIME_OFFSET: '/getTimeOffset',
    SERVLET_PATH_MEDICATION_ORDER_CARD_INFO_DATA: '/patientmedicationordercardinfodata',
    SERVLET_PATH_GET_BASELINE_THERAPIES: '/getPatientBaselineInfusionIntervals',
    SERVLET_PATH_GET_THERAPY_FORMATTED_DISPLAY: '/getTherapyFormattedDisplay',
    SERVLET_PATH_SAVE_REFERENCE_WEIGHT: '/saveReferenceWeight',
    SERVLET_PATH_FIND_SUPPLY_CANDIDATES: '/getMedicationSupplyCandidates',
    SERVLET_PATH_GET_UNITS: "/getMedicationBasicUnits",
    SERVLET_PATH_GET_THERAPIES_FOR_WARNINGS: "/getTherapiesForWarnings",
    SERVLET_PATH_DELETE_TEMPLATE: "/deleteTherapyTemplate",
    SERVLET_PATH_GET_THERAPY_PDF_REPORT: "/getTherapyPdfReport",
    SERVLET_PATH_OPEN_MEDICATION_DOCUMENT: "/openMedicationDocument",
    SERVLET_PATH_GET_THERAPIES_FOR_PREVIOUS_HOSPITALIZATION: "/getLastTherapiesForPreviousHospitalization",
    SERVLET_PATH_GET_THERAPY_TIMELINE: '/getTherapyTimeline',
    SERVLET_PATH_GET_PHARMACIST_TIMELINE: '/getPharmacistTimeline',
    SERVLET_PATH_GET_CARE_PROFESSIONALS: '/getCareProfessionals',
    SERVLET_PATH_TAG_THERAPY_FOR_PRESCRIPTION: '/tagTherapyForPrescription',
    SERVLET_PATH_UNTAG_THERAPY_FOR_PRESCRIPTION: '/untagTherapyForPrescription',
    SERVLET_PATH_SAVE_PHARMACIST_REVIEW: '/savePharmacistReview',
    SERVLET_PATH_GET_PROBLEM_DESC_NAMED_IDENTITIES: "/getProblemDescriptionNamedIdentities",
    SERVLET_PATH_AUTHORIZE_PHARMACIST_REVIEWS: '/authorizePharmacistReviews',
    SERVLET_PATH_GET_PHARMACIST_REVIEWS: '/getPharmacistReviews',
    SERVLET_PATH_REVIEW_PHARMACIST_REVIEW: '/reviewPharmacistReview',
    SERVLET_PATH_GET_THERAPY_PHARMACIST_REVIEWS: '/getPharmacistReviewsForTherapy',
    SERVLET_PATH_DELETE_PHARMACIST_REVIEW: '/deletePharmacistReview',
    SERVLET_PATH_CALCULATE_PARACETAMOL_ADMINISTRATION_RULE: '/calculateParacetamolAdministrationRule',
    SERVLET_PATH_CALCULATE_INGREDIENT_RULE_FOR_THERAPIES: '/calculateIngredientRuleForTherapies',
    SERVLET_PATH_FIND_PREVIOUS_TASK_FOR_THERAPY: '/findPreviousTaskForTherapy',
    SERVLET_PATH_ASSERT_PASSWORD_FOR_USERNAME: '/assertPasswordForUsername',
    SERVLET_PATH_GET_MEDICATIONS_ON_DISCHARGE: '/getMedicationsOnDischarge',
    SERVLET_PATH_SEND_THERAPY_RESUPPLY_REQUEST: '/sendNurseResupplyRequest',
    SERVLET_PATH_ORDER_THERAPY_PERFUSION_SYRINGE: '/orderPerfusionSyringePreparation',
    SERVLET_PATH_DISMISS_THERAPY_PERFUSION_SYRINGE: '/deletePerfusionSyringeRequest',
    SERVLET_PATH_CONFIRM_SUPPLY_REMINDER_TASK: '/confirmSupplyReminderTask',
    SERVLET_PATH_EDIT_SUPPLY_REMINDER_TASK: '/editSupplyReminderTask',
    SERVLET_PATH_CONFIRM_SUPPLY_REVIEW_TASK: '/confirmSupplyReviewTask',
    SERVLET_PATH_DISMISS_NURSE_SUPPLY_TASK: '/dismissNurseSupplyTask',
    SERVLET_PATH_DISMISS_PHARMACIST_SUPPLY_TASK: '/dismissPharmacistSupplyTask',
    SERVLET_PATH_GET_PHARMACIST_SUPPLY_SIMPLE_TASK: '/getPharmacistSupplySimpleTask',
    SERVLET_PATH_GET_SUPPLY_DATA_FOR_PHARMACIST_REVIEW: '/getSupplyDataForPharmacistReview',
    SERVLET_PATH_SET_DOCTOR_CONFIRMATION_RESULT: '/setDoctorConfirmationResult',
    SERVLET_PATH_UPDATE_SELF_ADMINISTERING_STATUS: '/updateTherapySelfAdministeringStatus',
    SERVLET_PATH_GET_PERFUSION_SYRINGE_TASK: '/getPerfusionSyringeTaskSimpleDto',
    SERVLET_PATH_EDIT_PERFUSION_SYRINGE_TASK: '/editPerfusionSyringeTask',
    SERVLET_PATH_GET_MEDICATION_EXTERNAL_ID: '/getMedicationExternalId',
    SERVLET_PATH_GET_LINK_THERAPY_CANDIDATES: '/getLinkTherapyCandidates',
    SERVLET_PATH_GET_FINISHED_PERFUSION_SYRINGE_REQUESTS_EXIST: '/finishedPerfusionSyringeRequestsExistInLastHours',
    SERVLET_PATH_GET_DATA_FOR_TITRATION: '/getDataForTitration',
    SERVLET_PATH_GET_THERAPY_SURGERY_REPORT: '/createTherapySurgeryReport',

    EVENT_TYPE_MEDICATION_BARCODE_SCANNED: new tm.jquery.event.EventType({
      name: 'medicationBarcodeScanned', delegateName: null
    })
  },

  /** constructor */
  Constructor: function(config)
  {
    config = tm.jquery.Utils.applyIf({
      layout: new tm.jquery.VFlexboxLayout.create("flex-start", "stretch")
    }, config);
    this.callSuper(config);

    this.registerEventTypes('tm.views.medications.TherapyView', [
      {eventType: tm.views.medications.TherapyView.EVENT_TYPE_MEDICATION_BARCODE_SCANNED}
    ]);

    this._restApi = new app.views.medications.RestApi({view: this});
    this._restErrorLogger = new app.views.medications.common.RestErrorLogger({view: this});
    this._informationSourceHolder = new app.views.medications.common.therapy.InformationSourceHolder({view: this});
    this._changeReasonTypeHolder = new app.views.medications.common.therapy.ChangeReasonTypeHolder({view: this});
    this.pharmacistReviewReferBackPreset = this.getProperty("pharmacistReviewReferBackPreset");
    this.mentalHealthReportEnabled = this.getProperty("mentalHealthReportEnabled");
    this.autoAdministrationCharting = this.getProperty("autoAdministrationCharting");
    this.medicationsSupplyPresent = this.getProperty("medicationsSupplyPresent");
    this.cumulativeAntipsychoticDoseEnabled = this.getProperty("cumulativeAntipsychoticDoseEnabled");
    this.infusionBagEnabled = this.getProperty("infusionBagEnabled");
    this.medicationsShowHeparinPane = this.getProperty("medicationsShowHeparinPane");
    this.outpatientPrescriptionType = this.getProperty("outpatientPrescriptionType");
    this.doctorReviewEnabled = this.getProperty("doctorReviewEnabled");
    this.doseRangeEnabled = this.getProperty("doseRangeEnabled");
    this.formularyFilterEnabled = this.getProperty("formularyFilterEnabled");
    this.substituteAdministrationMedicationEnabled = this.getProperty("substituteAdministrationMedicationEnabled");
    this.administrationWitnessingEnabled = this.getProperty("administrationWitnessingEnabled");
    this.administrationWitnessingMocked = this.getProperty("administrationWitnessingMocked");
    this.administrationWitnessingIvRequired = this.getProperty("administrationWitnessingIvRequired");
    this.antimicrobialDaysCountStartsWithOne = this.getProperty("antimicrobialDaysCountStartsWithOne");
    this.referenceWeightRequired = this.getProperty("referenceWeightRequired");
    this.presetPastAdministrationTimeToNow = this.getProperty("presetPastAdministrationTimeToNow");
    this.doseCalculationsEnabled = this.getProperty("doseCalculationsEnabled");
    this.therapyReportEnabled = this.getProperty("therapyReportEnabled");
    this.surgeryReportEnabled = this.getProperty("surgeryReportEnabled");
    this.gridViewEnabled = this.getProperty("gridViewEnabled");
    this.timelineViewEnabled = this.getProperty("timelineViewEnabled");
    this.pharmacistReviewAllowed = this.getProperty("pharmacistReviewAllowed");
    this.showPharmacistReviewStatus = this.getProperty("showPharmacistReviewStatus");
    this.medicationReconciliationEnabled = this.getProperty("medicationReconciliationEnabled");
    this.medicationConsentT2T3Allowed = this.getProperty("medicationConsentT2T3Allowed");
    this.medicationDocumentViewEnabled = this.getProperty("medicationDocumentViewEnabled");
    this.addMedicationToPreparationTasklistAllowed = this.getProperty("addMedicationToPreparationTasklistAllowed");
    this.nonFormularyMedicationSearchAllowed = this.getProperty("nonFormularyMedicationSearchAllowed");
    this.pharmacistReviewDisplayDays = this.getProperty("pharmacistReviewDisplayDays");
    this.singleDayTherapiesOverviewEnabled = this.getProperty("singleDayTherapiesOverviewEnabled");
    this.suspendReasonMandatory = this.getProperty("suspendReasonMandatory");
    this.stopReasonMandatory = this.getProperty("stopReasonMandatory");

    this.optimizeForPerformance = this.isSwingApplication();

    this._testRenderCoordinator = new app.views.medications.common.testing.RenderCoordinator({
      attributeName: 'therapy-view-coordinator',
      view: this,
      component: this
    });

    CurrentTime.setOffset(this.getProperty("timeOffset"));

    tm.views.medications.TherapyView.THERAPY_PREPARE_VIEW_HUB = this.createUserAction("therapyPrepareViewHub");
    tm.views.medications.TherapyView.THERAPY_FLOW_LOAD_HUB = this.createUserAction("therapyFlowLoadHub");
    tm.views.medications.TherapyView.THERAPY_FLOW_NAVIGATE_HUB = this.createUserAction("therapyFlowNavigateHub");
    tm.views.medications.TherapyView.THERAPY_SAVE_REFERENCE_WEIGHT_HUB = this.createUserAction("therapySaveReferenceWeightHub");
    tm.views.medications.TherapyView.THERAPY_SAVE_HUB = this.createUserAction("therapySaveHub");
    tm.views.medications.TherapyView.THERAPY_TIMELINE_LOAD_HUB = this.createUserAction("therapyTimelineLoadHub");
    tm.views.medications.TherapyView.THERAPY_ABORT_HUB = this.createUserAction("therapyAbortHub");
    tm.views.medications.TherapyView.THERAPY_REVIEW_HUB = this.createUserAction("therapyReviewHub");
    tm.views.medications.TherapyView.THERAPY_SUSPEND_HUB = this.createUserAction("therapySuspendHub");
    tm.views.medications.TherapyView.THERAPY_SUSPEND_ALL_HUB = this.createUserAction("therapySuspendAllHub");
    tm.views.medications.TherapyView.THERAPY_REISSUE_HUB = this.createUserAction("therapyReissueHub");
    tm.views.medications.TherapyView.THERAPY_CONFIRM_ADMINISTRATION_HUB = this.createUserAction("therapyConfirmAdministrationHub");
    tm.views.medications.TherapyView.THERAPY_MOVE_ADMINISTRATION_HUB = this.createUserAction("therapyMoveAdministrationHub");
    tm.views.medications.TherapyView.THERAPY_DELETE_ADMINISTRATION_HUB = this.createUserAction("therapyDeleteAdministrationHub");
    tm.views.medications.TherapyView.THERAPY_DELETE_ADMINISTRATION_TASK_HUB = this.createUserAction("therapyDeleteAdministrationTaskHub");
    tm.views.medications.TherapyView.THERAPY_GET_LINK_THERAPY_CANDIDATES_HUB = this.createUserAction("therapyGetLinkTherapyCandidatesHub");
    tm.views.medications.TherapyView.THERAPY_HANDLE_ADDITIONAL_WARNINGS_ACTION = this.createUserAction("therapyHandleAdditionalWarningsActionHub");

    this.careProfessionals = [];
    this.doseForms = [];
    this.routes = [];

    var viewInitData = this.getViewInitData();

    if (!tm.jquery.Utils.isEmpty(viewInitData) && !tm.jquery.Utils.isEmpty(viewInitData.paneMode))
    {
      this.mode = viewInitData.paneMode;
    }

    this.userPerson = {
      id: this.getProperty("userPersonId"),
      name: this.getProperty("userPersonName")
    };

    this._therapyAuthority = new app.views.medications.TherapyAuthority({view: this});
    this.context = viewInitData && viewInitData.contextData ? JSON.parse(viewInitData.contextData) : null;

    this._loadCareProfessionals();
    this._loadUnitsHolder();
    if (this.mode === 'FLOW')
    {
      this._loadRoutes();
      this._buildFlowGui();
    }

    this.getLocalLogger().info("viewInitData.patientId:", viewInitData ? viewInitData.patientId : "");
    this.getLocalLogger().info("this.getProperty(patientId):", this.getProperty("patientId"));

    var patientId = viewInitData && viewInitData.patientId ? viewInitData.patientId : this.getProperty("patientId", true);
    if (patientId)
    {
      var updateDataCommand = {
        "update": {"data": {"patientId": patientId}}
      };
      this.onViewCommand(updateDataCommand);
    }
  },

  /**
   * Override the broken default render to element in {@link #_appFactory}, which is pointing to an intermediate
   * {@link app.views.common.AppExternalCallView} instead of our view, causing our styling to fail.
   * {@see app.views.common.AppFactory#createDefaultView} for more information.
   * @override
   */
  afterInitialize: function()
  {
    this.callSuper();

    var self = this;
    this.getAppFactory().getDefaultRenderToElement = function()
    {
      return self.dom;
    };
  },

  /**
   * @override to prevent swing app from overriding our dictionary - it should match our version.
   */
  getDictionaryMap: function()
  {
    return this.getViewResources()
        .dictionary
        .values
        .reduce(
            function toMap(map, obj)
            {
              map[obj.key] = obj.value;
              return map;
            },
            {});
  },

  onViewCommand: function(command)
  {
    if (command.hasOwnProperty('update'))
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus();
      tm.jquery.ComponentUtils.hideAllTooltips();
      tm.jquery.ComponentUtils.hideAllDialogs();
      this.updateDataCommand = command;
      this.updateData(command);
    }
    else if (command.hasOwnProperty('clear'))
    {
      this._abortPresentDataTask();

      tm.jquery.ComponentUtils.hideAllDropDownMenus();
      tm.jquery.ComponentUtils.hideAllTooltips();
      tm.jquery.ComponentUtils.hideAllDialogs();
      if (this.therapyGrid)
      {
        this.therapyGrid.clear();
      }
      else if (this.timelineContainer)
      {
        this.timelineContainer.clear();
      }
      else if (this.pharmacistReviewContainer)
      {
        this.pharmacistReviewContainer.clear();
      }
      else if (this.summaryContainer)
      {
        this.summaryContainer.clearData();
      }
      else if (this.documentationContainer)
      {
        this.documentationContainer.clearData();
      }
      this.getPatientDataContainer().clear();
    }
    else if (command.hasOwnProperty('refresh'))
    {
      tm.jquery.ComponentUtils.hideAllDropDownMenus();
      tm.jquery.ComponentUtils.hideAllTooltips();
      tm.jquery.ComponentUtils.hideAllDialogs();
      if (!!this.updateDataCommand)
      {
        this.updateData(this.updateDataCommand);
      }
      else
      {
        console.warn('Refresh command received before a valid update command was issued. Command ignored.');
      }
    }
    else if (command.hasOwnProperty("actionCallback"))
    {
      this.onViewActionCallback(command.actionCallback);
    }
    else
    {
      console.warn('Unknown command received. Call ignored.');
    }
  },

  onViewActionCallback: function(actionCallback)
  {
    if (!tm.jquery.Utils.isEmpty(this.actionCallbackListener))
    {
      this.actionCallbackListener.onActionCallback(actionCallback);
    }
    else if (!tm.jquery.Utils.isEmpty(this.documentationContainer)
        && this.documentationContainer.getPrescribedViewActions().contains(actionCallback.action))
    {
      this.documentationContainer.onViewActionCallback(actionCallback);
    }
  },

  updateData: function(config)
  {
    tm.jquery.ComponentUtils.hideAllDialogs();
    tm.jquery.ComponentUtils.hideAllDropDownMenus(this);
    tm.jquery.ComponentUtils.hideAllTooltips(this);

    this.patientId = config.update.data.patientId; //data - TherapyHtmlPortletValue.java
    this.presetCentralCaseId = config.update.data.presetCentralCaseId; //data - TherapyHtmlPortletValue.java

    //ORDERING PAST, EDIT PAST (DRP)
    this._patientData =
        app.views.medications.common.dto.PatientDataForMedications.fromJson(config.update.data.patientData);
    this.presetMedicationId = config.update.data.presetMedicationId || null;
    this.therapyToEdit = config.update.data.therapyToEdit ?
        app.views.medications.common.TherapyJsonConverter.convert(config.update.data.therapyToEdit) :
        config.update.data.therapyToEdit;
    this.presetDate = config.update.data.presetDate ? new Date(config.update.data.presetDate) : null;

    this._refreshTimeOffsetFromServer();

    if (this.mode === 'FLOW')
    {
      if (tm.jquery.Utils.isEmpty(this.getPatientData()))
      {
        this._showToolbarGlassLayer();
      }

      var viewHubNotifier = this.getHubNotifier();
      var hubAction = tm.views.medications.TherapyView.THERAPY_PREPARE_VIEW_HUB;
      viewHubNotifier.actionStarted(hubAction);

      tm.jquery.ComponentUtils.hideAllDialogs();

      if (config.update.data.subview)
      {
        this._setSubview(config.update.data.subview);
        this._saveContext();

        // Since we throw away the DOM when calling {@link tm.views.medications.TherapyView#_setSubview}, we need
        // to ensure the UI is repainted before we start reapplying values.
        this.content.repaint();
        this.header.repaint();
      }

      this._loadTherapyViewPatientData();
    }
    else if (['ORDERING', 'EDIT', 'ORDERING_PAST', 'EDIT_PAST'].indexOf(this.mode) > -1)
    {
      if (this.mode === 'ORDERING' || this.mode === 'ORDERING_PAST')
      {
        this._buildOrderingGui(this.mode === 'ORDERING_PAST', this.mode !== 'ORDERING_PAST');
      }
      else
      {
        this._buildEditGui(this.mode === 'EDIT_PAST');
      }
    }
  },

  _presentData: function()
  {
    this.actionsHeader.showRecentHospitalizationButton(
        this.isRecentHospitalization() && this.getTherapyAuthority().isManageInpatientPrescriptionsAllowed());

    this.actionsHeader.setCustomGroups(this.getActiveViewData().getCustomGroups());
    var discharged = this._isDischarged();
    this.actionsHeader.setIsDischarged(discharged);
    this.actionsHeader.resetSubview();
    var subview = this.actionsHeader.getSubview();
    var displayDischargeListNotification = this._isPatientBannerDischargeListNotificationRequired(subview);
    this.getPatientDataContainer().refreshPatientData();
    this.getPatientDataContainer().applyDischargeListNotificationVisibility(displayDischargeListNotification);
    this.refreshPatientsCumulativeAntipsychoticPercentage();

    if (subview === "GRID")
    {
      var searchDate = this._getDefaultSearchDate(this.dayCount);
      this.therapyGrid.paintGrid(this.dayCount, searchDate, this.groupField, this.therapySortTypeEnum);
      this._addGridEvents();
    }
    else if (subview === "TIMELINE")
    {
      this.timelineContainer.setPatientId(this.patientId, discharged, this.therapySortTypeEnum);
    }
    else if (subview === "PHARMACIST")
    {
      this.pharmacistReviewContainer.refreshData();
    }
    else if (subview === "RECONCILIATION")
    {
      this.summaryContainer.refreshData();
    }
    else if (subview === "DOCUMENTATION")
    {
      this.documentationContainer.refreshData();
    }
  },

  /**
   * @return {boolean}
   * @private
   */
  _isDischarged: function()
  {
    var centralCaseData = this.getCentralCaseData();
    if (!tm.jquery.Utils.isEmpty(centralCaseData))
    {
      if (centralCaseData.outpatient === false)
      {
        if (centralCaseData.centralCaseEffective.endMillis)
        {
          var centralCaseEnd = new Date(centralCaseData.centralCaseEffective.endMillis);
          if (centralCaseEnd < CurrentTime.get())
          {
            return true;
          }
        }
      }
    }

    return false;
  },

  _loadTherapyViewPatientData: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    this.getPatientDataContainer().clear();

    this.getRestApi()
        .loadTherapyViewPatientData(true)
        .then(
            function(data)
            {
              if (data && data.isForPatient(self.getPatientId()))
              {
                self._activeViewData = data;
                self._patientData = data.getPatientData();
                self._hideToolbarGlassLayer();
                self._abortPresentDataTask();
                self._presentDataConditionalTask = appFactory.createConditionTask(
                    function()
                    {
                      self._presentDataConditionalTask = null;
                      self._presentData();
                    },
                    function()
                    {
                      return self.isRendered(true) && !tm.jquery.Utils.isEmpty(self.getUnitsHolder()) &&
                          !!self.getInformationSourceHolder().isLoaded() && !!self.getChangeReasonTypeHolder().getMap();
                    },
                    50, 300
                );

                var viewHubNotifier = self.getHubNotifier();
                var hubAction = tm.views.medications.TherapyView.THERAPY_PREPARE_VIEW_HUB;
                viewHubNotifier.actionEnded(hubAction);
              }
            });
  },

  _refreshTimeOffsetFromServer: function()
  {
    var url = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_TIME_OFFSET;
    this.loadViewData(url, null, null, function(timeOffset)
    {
      CurrentTime.setOffset(timeOffset);
    });
  },

  _loadCareProfessionals: function()
  {
    var self = this;
    var careProfessionalsUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_CARE_PROFESSIONALS;
    this.loadViewData(careProfessionalsUrl, null, null, function(data)
    {
      self.careProfessionals.length = 0;
      $.merge(self.careProfessionals, data);
    });
  },

  _loadRoutes: function()
  {
    var self = this;
    this.getRestApi().loadRoutes(true).then(function(routes)
    {
      self.routes.length = 0;
      $.merge(self.routes, routes);
    });
  },

  _loadUnitsHolder: function()
  {
    var self = this;
    this.getRestApi().loadUnitsHolder().then(function onSuccess(units)
    {
      self._unitsHolder = units;
    });
  },

  _loadProblemDescriptionNamedIdentitiesMap: function()
  {
    var self = this;

    var getDataUrl =
        this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_GET_PROBLEM_DESC_NAMED_IDENTITIES;

    var params = {
      language: this.getViewLanguage()
    };

    this.loadViewData(getDataUrl, params, null, function(data)
    {
      if (!tm.jquery.Utils.isEmpty(data))
      {
        self.problemDescriptionNamedIdentitiesMap = data;
      }
      else
      {
        self.problemDescriptionNamedIdentitiesMap = {};
      }
    });
  },

  getMedicationSupplyCandidates: function(medicationId, routeId, callback)
  {
    var url = this.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_FIND_SUPPLY_CANDIDATES;

    var params = {
      medicationId: medicationId,
      routeId: routeId
    };

    this.loadViewData(url, params, null, function(data)
    {
      if (!tm.jquery.Utils.isEmpty(data))
      {
        callback(data);
      }
      else
      {
        callback([]);
      }
    });
  },

  _buildFlowGui: function()
  {
    var self = this;
    this.content = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch"),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    this.header = new tm.jquery.Container({
      cls: 'header',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "stretch"),
      scrollable: 'visible'
    });
    var headerContainer = new app.views.common.Toolbar({
      cls: 'app-views-toolbar portlet-header',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
    });
    headerContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      if (tm.jquery.Utils.isEmpty(self.getPatientData()))
      {
        self._showToolbarGlassLayer();
      }
    });

    this._patientDataContainer = new app.views.medications.common.overview.PatientDataContainer({
      view: this
    });

    var viewButtons = [];

    if (this.getTherapyAuthority().isGridViewEnabled())
    {
      this.flowGridTypeButton = new tm.jquery.Button({
        cls: 'flow-grid-icon btn-flat',
        type: 'GRID',
        tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("therapy.overview"), "bottom")
      });
      this.flowGridTypeButton.addTestAttribute('view-selection-grid-button');
      viewButtons.add(this.flowGridTypeButton);
    }
    if (this.getTherapyAuthority().isTimelineViewEnabled())
    {
      this.timelineTypeButton = new tm.jquery.Button({
        cls: 'timeline-icon btn-flat',
        type: 'TIMELINE',
        tooltip: this.getAppFactory().createDefaultHintTooltip(
            this.getDictionary("medication.administration.record"), "bottom")
      });
      this.timelineTypeButton.addTestAttribute('view-selection-timeline-button');
      viewButtons.add(this.timelineTypeButton);
    }
    if (this.getTherapyAuthority().isPharmacistReviewViewEnabled())
    {
      this.pharmacistTypeButton = new tm.jquery.Button({
        cls: 'pharmacist-review btn-flat',
        type: 'PHARMACIST',
        tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("pharmacists.review"), "bottom")
      });
      this.pharmacistTypeButton.addTestAttribute('view-selection-pharmacist-button');
      viewButtons.add(this.pharmacistTypeButton);
    }
    if (this.getTherapyAuthority().isMedicationSummaryViewEnabled())
    {
      this.reconciliationSummaryTypeButton = new tm.jquery.Button({
        cls: 'reconciliation-summary-icon btn-flat',
        type: 'RECONCILIATION',
        tooltip: this.getAppFactory().createDefaultHintTooltip(
            this.getDictionary("medication.reconciliation.summary"),
            "bottom")
      });
      this.reconciliationSummaryTypeButton.addTestAttribute('view-selection-reconciliation-button');
      viewButtons.add(this.reconciliationSummaryTypeButton);
    }
    if (this.getTherapyAuthority().isMedicationDocumentViewEnabled())
    {
      this.therapyDocumentationTypeButton = new tm.jquery.Button({
        cls: 'therapy-documentation-icon btn-flat',
        type: 'DOCUMENTATION',
        tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("list.of.documents"), "bottom")
      });
      this.therapyDocumentationTypeButton.addTestAttribute('view-selection-documentation-button');
      viewButtons.add(this.therapyDocumentationTypeButton);
    }

    this.displayTypeButtonGroup = new tm.jquery.ButtonGroup({
      cls: "btn-group-flat header",
      width: 246,
      orientation: "horizontal",
      type: "radio",
      buttons: viewButtons
    });

    this.displayTypeButtonGroup.on(tm.jquery.ComponentEvent.EVENT_TYPE_CHANGE, function(component, componentEvent)
    {
      component.setEnabled(false); // prevent the user from switching too fast while we set up the new view
      tm.jquery.ComponentUtils.hideAllDropDownMenus();
      tm.jquery.ComponentUtils.hideAllTooltips();
      tm.jquery.ComponentUtils.hideAllDialogs();
      var selectedViewType = componentEvent.getEventData().newSelectedButton.type;
      var displayDischargeListNotification = self._isPatientBannerDischargeListNotificationRequired(selectedViewType);
      self.getPatientDataContainer().applyDischargeListNotificationVisibility(displayDischargeListNotification);
      self.applyPatientDataContainerWarningsVisibility(false);
      if (selectedViewType === 'GRID')
      {
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setSubview("GRID");
        self._buildFlowGridGui();
        var searchDate = self._getDefaultSearchDate(self.dayCount);
        self.therapyGrid.paintGrid(self.dayCount, searchDate, self.groupField, self.therapySortTypeEnum);
        self._addGridEvents();
        self.content.repaint();
        self.header.repaint();
      }
      else if (selectedViewType === 'TIMELINE')
      {
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setSubview("TIMELINE");
        self._buildTimelineGui();
        self.timelineContainer.reloadTimelines(true, self.therapySortTypeEnum);
        self.content.repaint();
        self.header.repaint();
      }
      else if (selectedViewType === 'PHARMACIST')
      {
        /* switch to the Pharmacist's review mode */
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setSubview("PHARMACIST");
        self._buildPharmacistReviewGui();
        self.content.repaint();
        self.header.repaint();
        self.getAppFactory().createConditionTask(
            function()
            {
              self.pharmacistReviewContainer.refreshData()
            },
            function(task)
            {
              if (!self.pharmacistReviewContainer)
              {
                // abort the taks, the pharmacistReviewContainer was removed - which happens when you switch views
                task.abort();
                return;
              }
              return self.isRendered() && self.pharmacistReviewContainer.isRendered();
            },
            50, 10);
      }
      else if (selectedViewType === 'RECONCILIATION')
      {
        /* switch to the Medication Reconciliation Summary mode */
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setSubview("RECONCILIATION");
        self._buildReconciliationSummaryGui();
        self.content.repaint();
        self.header.repaint();
        self.getAppFactory().createConditionTask(
            function()
            {
              self.summaryContainer.refreshData()
            },
            function(task)
            {
              if (!self.summaryContainer)
              {
                task.abort(); // abort, the sub view has changed
              }
              // the sub-view updates the DOM of the actionsHeader after the data loads, so ensure it's fully rendered first
              return self.isRendered() && self.header.isRendered(true) && self.summaryContainer.isRendered(true);
            },
            70, 20);
      }
      else if (selectedViewType === 'DOCUMENTATION')
      {
        /* switch to the Documentation view mode */
        tm.jquery.ComponentUtils.hideAllDialogs();
        self.actionsHeader.setSubview("DOCUMENTATION");
        self._buildTherapyDocumentationGui();
        self.content.repaint();
        self.header.repaint();
        self.documentationContainer.refreshData();
      }
      self._saveContext();
      component.setEnabled(true);
    });

    headerContainer.add(this.displayTypeButtonGroup);
    headerContainer.add(this.header);

    this.add(headerContainer);
    this.add(this._patientDataContainer);
    this.add(this.content);

    this.actionsHeader = new app.views.medications.common.overview.header.OverviewHeader({
      view: this,
      therapySortTypeEnum: this.therapySortTypeEnum,
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      timelineFilterChangedFunction: function(routes, customGroups, refreshTimelines)
      {
        if (!tm.jquery.Utils.isEmpty(self.timelineContainer))
        {
          self.timelineContainer.setTimelineFilter(routes, customGroups, refreshTimelines);
        }
        else if (!tm.jquery.Utils.isEmpty(self.pharmacistReviewContainer))
        {
          self.pharmacistReviewContainer.setRoutesAndCustomGroupsFilter(routes, customGroups, refreshTimelines);
        }
      },
      timelineDateSelectorChangedFunction: function(newDate)
      {
        self.timelineContainer.setShownTherapies("customDateTherapies");
        self.timelineContainer.setTimelineDate(newDate);
      },
      medicationIdentifierScannedFunction: function(barcodeTaskSearch, barcode)
      {
        self._handleBarcodeScanned(barcodeTaskSearch, barcode);
      }
    });
    this.header.add(this.actionsHeader);
    this.actionsHeader.showRecentHospitalizationButton(
        this.isRecentHospitalization() && this.getTherapyAuthority().isManageInpatientPrescriptionsAllowed());

    var subview = !tm.jquery.Utils.isEmpty(this.getViewInitData()) ? this.getViewInitData().subview : null;
    subview = this.getContext() && this.getContext().subview ? this.getContext().subview : subview;
    this._setSubview(subview);
  },

  _setSubview: function(subview) //GRID, TIMELINE, PHARMACIST
  {
    if (subview === 'GRID' && this.getTherapyAuthority().isGridViewEnabled())
    {
      this.actionsHeader.setSubview(subview);
      this.displayTypeButtonGroup.setSelection([this.flowGridTypeButton], true);
      this.flowGridTypeButton.focus();
      this._buildFlowGridGui();
    }
    else if (subview === 'PHARMACIST' && this.getTherapyAuthority().isPharmacistReviewViewEnabled())
    {
      this.actionsHeader.setSubview(subview);
      this.displayTypeButtonGroup.setSelection([this.pharmacistTypeButton], true);
      this.pharmacistTypeButton.focus();
      this._buildPharmacistReviewGui();
    }
    else if (subview === 'RECONCILIATION' && this.getTherapyAuthority().isMedicationSummaryViewEnabled())
    {
      this.actionsHeader.setSubview(subview);
      this.displayTypeButtonGroup.setSelection([this.reconciliationSummaryTypeButton], true);
      this.reconciliationSummaryTypeButton.focus();
      this._buildReconciliationSummaryGui();
    }
    else if (subview === 'DOCUMENTATION' && this.getTherapyAuthority().isMedicationDocumentViewEnabled())
    {
      this.actionsHeader.setSubview(subview);
      this.displayTypeButtonGroup.setSelection([this.therapyDocumentationTypeButton], true);
      this.therapyDocumentationTypeButton.focus();
      this._buildTherapyDocumentationGui();
    }
    else if (subview === 'TIMELINE' && this.getTherapyAuthority().isTimelineViewEnabled())
    {
      // default to TIMELINE
      this.actionsHeader.setSubview('TIMELINE');
      this.displayTypeButtonGroup.setSelection([this.timelineTypeButton], true);
      this.timelineTypeButton.focus();
      this._buildTimelineGui()
    }
    else
    {
      var possibleDefault = this._getFirstEnabledSubview();
      if (possibleDefault)
      {
        this._setSubview(possibleDefault);
      }
    }
  },

  /**
   * Finds the first possible view type (if any), based on available view types and the predefined view type priority.
   * @returns {String|null}
   * @private
   */
  _getFirstEnabledSubview: function()
  {
    var subviewByPriority = ['TIMELINE', 'GRID', 'DOCUMENTATION', 'PHARMACIST', 'RECONCILIATION'];
    var buttons = this.displayTypeButtonGroup.getButtons();

    for (var idx = 0; idx < subviewByPriority.length; idx++)
    {
      var subviewButton =
          app.views.medications.MedicationUtils.findInArray(
              buttons,
              function isButtonVisibleAndSubviewNameMatches(button)
              {
                return !button.isHidden() && button.type === this.currentValue;
              },
              {currentValue: subviewByPriority[idx]}); // pass the value via context to prevent accessing mutable value

      if (subviewButton)
      {
        return subviewButton.type;
      }
    }

    return null;
  },

  _buildFlowGridGui: function()
  {
    this.timelineContainer = null;
    this.pharmacistReviewContainer = null;
    this.summaryContainer = null;
    this.documentationContainer = null;
    this.content.removeAll(true);
    this.therapyGrid = new app.views.medications.grid.GridView({
      view: this,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    this.content.add(this.therapyGrid);
    this._addGridHeaderEvents();

    if (this.context)
    {
      this.dayCount = this.actionsHeader.setNumberOfDaysMode(this.context.numberOfDaysMode);
      if (!this.dayCount)
      {
        this.dayCount = 3;
      }
    }
    else
    {
      this.dayCount = 3;
    }

    this._setGroupModeEnum();
    this._setTherapySortTypeEnum();
  },

  _buildTimelineGui: function()
  {
    var self = this;
    this.therapyGrid = null;
    this.pharmacistReviewContainer = null;
    this.summaryContainer = null;
    this.documentationContainer = null;
    this.content.removeAll(true);

    this.actionsHeader.addGroupingButtonGroupAction(function(groupField)
    {
      self.groupField = groupField;
      self.timelineContainer.setGrouping(groupField);
      self._saveContext();
    });

    this.actionsHeader.addSortButtonGroupAction(function(therapySortTypeEnum, showTherapies)
    {
      if (therapySortTypeEnum === self.therapySortTypeEnum)
      {
        self.timelineContainer.setShownTherapies(showTherapies.value);
        self.timelineContainer.reloadTimelines(false, self.therapySortTypeEnum);
        self._saveContext();
      }
      else
      {
        self.therapySortTypeEnum = therapySortTypeEnum;
        self.timelineContainer.reloadTimelines(false, therapySortTypeEnum);
        self._saveContext();
      }
    });

    this._setGroupModeEnum();
    this._setTherapySortTypeEnum();

    this.timelineContainer = new app.views.medications.timeline.TherapyTimelineContainer({
      view: this,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      patientId: this.patientId,
      groupField: this.groupField,
      therapyTimelineRowsLoadedFunction: function(therapyTimelineRows)
      {
        therapyTimelineRows = tm.jquery.Utils.isEmpty(therapyTimelineRows) ? [] : therapyTimelineRows;

        var groups = [];
        var routes = [];

        therapyTimelineRows.forEach(function(row)
        {
          if (!tm.jquery.Utils.isEmpty(row.customGroup) && !groups.contains(row.customGroup))
          {
            groups.push(row.customGroup);
          }

          if (!tm.jquery.Utils.isEmpty(row.therapy))
          {
            row.therapy.getRoutes().forEach(function(route)
            {
              if (!routes.contains(route.name))
              {
                routes.push(route.name);
              }
            });
          }

          if (row.currentStartingDevice)
          {
            row.currentStartingDevice = new app.views.medications.common.dto.OxygenStartingDevice(row.currentStartingDevice);
          }
        });

        self.actionsHeader.setupTimelineFilter(routes, groups);
        self.actionsHeader.requestBarcodeFieldFocus();
      }
    });
    this.content.add(this.timelineContainer);
  },
  _buildPharmacistReviewGui: function()
  {
    var self = this;

    this.therapyGrid = null;
    this.timelineContainer = null;
    this.summaryContainer = null;
    this.documentationContainer = null;
    this.content.removeAll(true);

    this._setGroupModeEnum();
    this._setTherapySortTypeEnum();

    if (tm.jquery.Utils.isEmpty(this.getProblemDescriptionNamedIdentitiesMap()))
    {
      this._loadProblemDescriptionNamedIdentitiesMap();
    }

    this.pharmacistReviewContainer = new app.views.medications.pharmacists.ReviewView({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this,
      therapyDataLoadedCallback: function(data)
      {
        data = tm.jquery.Utils.isEmpty(data) ? {} : data;
        data.therapyRows = tm.jquery.Utils.isEmpty(data.therapyRows) ? [] : data.therapyRows;

        var groups = [];
        var routes = [];

        data.therapyRows.forEach(function(row)
        {
          if (!tm.jquery.Utils.isEmpty(row.customGroup) && !groups.contains(row.customGroup)) groups.push(row.customGroup);
          if (!tm.jquery.Utils.isEmpty(row.route) && !routes.contains(row.route)) routes.push(row.route);
        });

        self.actionsHeader.setupTimelineFilter(routes, groups);
      }
    });

    this.actionsHeader.addGroupingButtonGroupAction(function(groupField)
    {
      self.groupField = groupField;
      self.pharmacistReviewContainer.refreshTherapies();
      self._saveContext();
    });

    this.actionsHeader.addSortButtonGroupAction(function(therapySortTypeEnum, showTherapies)
    {
      if (therapySortTypeEnum === self.therapySortTypeEnum)
      {
        self.pharmacistReviewContainer.setShownTherapies(showTherapies.value);
        self.pharmacistReviewContainer.refreshTherapies();
        self._saveContext();
      }
      else
      {
        self.therapySortTypeEnum = therapySortTypeEnum;
        self.pharmacistReviewContainer.refreshTherapies();
        self._saveContext();
      }
    });

    this.content.add(this.pharmacistReviewContainer);
  },

  _buildReconciliationSummaryGui: function()
  {
    this.therapyGrid = null;
    this.timelineContainer = null;
    this.pharmacistReviewContainer = null;
    this.documentationContainer = null;
    this.content.removeAll(true);

    this.summaryContainer = new app.views.medications.reconciliation.SummaryView({
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      view: this
    });
    this.actionsHeader.attachEventsToSubView(this.summaryContainer);

    this.content.add(this.summaryContainer);
  },

  _buildTherapyDocumentationGui: function()
  {
    if (tm.jquery.Utils.isEmpty(this.documentationContainer))
    {
      this.therapyGrid = null;
      this.timelineContainer = null;
      this.pharmacistReviewContainer = null;
      this.summaryContainer = null;
      this.content.removeAll(true);

      this.documentationContainer = new app.views.medications.documentation.TherapyDocumentationView({
        flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
        view: this
      });

      this.content.add(this.documentationContainer);
    }
  },

  _setTherapySortTypeEnum: function()
  {
    if (this.context)
    {
      this.therapySortTypeEnum = this.actionsHeader.setTherapySortType(this.context.therapySortTypeEnum);
      if (tm.jquery.Utils.isEmpty(this.therapySortTypeEnum))
      {
        this.therapySortTypeEnum = this.actionsHeader.setTherapySortType(app.views.medications.TherapyEnums.therapySortTypeEnum.DESCRIPTION_ASC);
      }
    }
    else
    {
      this.therapySortTypeEnum = this.actionsHeader.setTherapySortType(app.views.medications.TherapyEnums.therapySortTypeEnum.DESCRIPTION_ASC);
    }
  },

  _setGroupModeEnum: function()
  {
    if (this.context)
    {
      this.groupField = this.actionsHeader.setGroupMode(this.context.groupByMode);
    }
  },

  _buildOrderingGui: function(allowPastOrdering, assertBaselineInfusion)
  {
    var orderingContainer = new app.views.medications.ordering.MedicationsOrderingContainer({
      view: this,
      patientId: this.patientId,
      presetMedicationId: this.presetMedicationId ? this.presetMedicationId : null,
      isPastMode: allowPastOrdering,
      assertBaselineInfusion: assertBaselineInfusion,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    this.add(orderingContainer, {region: 'center'});
    var footer = this._buildDialogFooter(orderingContainer);
    this.add(footer, {region: 'south', height: 40});

    this.repaint();
  },

  _buildEditGui: function(isPastEditMode)
  {
    //this.therapyToEdit must be set
    var therapyToEdit = this.getTherapyToEdit();
    var self = this;
    var appFactory = this.getAppFactory();
    var ingredientsMedicationLoader = new app.views.medications.common.TherapyMedicationDataLoader({view: this});
    var editContainer;

    ingredientsMedicationLoader.load(therapyToEdit).then(function showDialog(medicationData)
    {
      if (ingredientsMedicationLoader.isMedicationNoLongerAvailable(therapyToEdit, medicationData))
      {
        var message = self.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            self.getDictionary('stop.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 500, 160).show();
      }
      else if (therapyToEdit.isOrderTypeComplex())
      {
        editContainer = new app.views.medications.ordering.ComplexTherapyEditContainer({
          view: self,
          startProcessOnEnter: true,
          therapy: therapyToEdit,
          isPastMode: isPastEditMode,
          medicationData: medicationData,
          flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
        });
        addAndRepaint.call(self, editContainer);
      }
      else
      {
        editContainer = new app.views.medications.ordering.SimpleTherapyEditContainer({
          view: self,
          startProcessOnEnter: true,
          therapy: therapyToEdit,
          isPastMode: isPastEditMode,
          medicationData: medicationData
        });
        addAndRepaint.call(this, editContainer);
      }
    });

    function addAndRepaint(editContainer)
    {
      this.add(editContainer, {region: 'center'});
      var footer = this._buildDialogFooter(editContainer);
      this.add(footer, {region: 'south', height: 40});
      this.repaint();
    }
  },

  _buildDialogFooter: function(dialogContainer)
  {
    var self = this;
    var footer = new tm.jquery.Container({
      height: 50,
      style: "background-color: #f7f7f7; border-top: 1px solid #ebebeb; padding-right: 20px;",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 10)
    });

    footer.saveButton = new tm.jquery.Button({
      text: this.getDictionary("confirm"),
      tooltip: this.getAppFactory().createDefaultHintTooltip(this.getDictionary("save.therapy"), "left"),
      handler: function()
      {
        footer.saveButton.setEnabled(false);
        dialogContainer.processResultData(function(data)
        {
          if (data.success)
          {
            self.sendAction("closeTherapyDialog", {reason: "save"});
          }
          else
          {
            footer.saveButton.setEnabled(true);
          }
        });
      }
    });
    footer.add(footer.saveButton);

    footer.cancelLink = new tm.jquery.Button({
      text: this.getDictionary("cancel"), type: "link",
      handler: function()
      {
        self.sendAction("closeTherapyDialog", {reason: "cancel"});
      }
    });
    footer.add(footer.cancelLink);
    return footer;
  },

  _addGridHeaderEvents: function()
  {
    var self = this;
    this.actionsHeader.addPreviousButtonAction(function()
    {
      tm.jquery.ComponentUtils.hideAllDialogs();
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self);
      tm.jquery.ComponentUtils.hideAllTooltips(self);
      self.therapyGrid.changeSearchDate(false);
    });
    this.actionsHeader.addNextButtonAction(function()
    {
      tm.jquery.ComponentUtils.hideAllDialogs();
      tm.jquery.ComponentUtils.hideAllDropDownMenus(self);
      tm.jquery.ComponentUtils.hideAllTooltips(self);
      self.therapyGrid.changeSearchDate(true);
    });
    this.actionsHeader.addDayCountButtonGroupAction(function(dayCount)
    {
      self.dayCount = dayCount;
      var searchDate = self._getDefaultSearchDate(dayCount);
      self.therapyGrid.repaintGrid(dayCount, searchDate, self.therapySortTypeEnum);
      self._addGridEvents();
      self._saveContext();
    });
    this.actionsHeader.addGroupingButtonGroupAction(function(groupField)
    {
      self.groupField = groupField;
      self.therapyGrid.setGrouping(groupField);
      self._saveContext();
    });

    this.actionsHeader.addSortButtonGroupAction(function(therapySortTypeEnum)
    {
      self.therapySortTypeEnum = therapySortTypeEnum;
      var searchDate = self._getDefaultSearchDate(self.dayCount);
      self.therapyGrid.repaintGrid(self.dayCount, searchDate, therapySortTypeEnum);
      self._addGridEvents();
      self._saveContext();
    });
  },

  _addGridEvents: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    this.therapyGrid.grid.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function(component)
    {
      appFactory.createConditionTask(
          function()
          {
            self.therapyGrid.reloadGridData();
          },
          function(task)
          {
            if (!self.therapyGrid || !component.isRendered())
            {
              // abort the task if the grid was destroyed (happens when fast switching back and forth)
              task.abort();
              return;
            }
            return !tm.jquery.Utils.isEmpty(component.getPlugin()) && !tm.jquery.Utils.isEmpty(self.getPatientData());
          },
          20, 1000
      );
    });
  },

  _saveContext: function()
  {
    if (!this.context)
    {
      this.context = {};
    }
    this.context.subview =
        this.displayTypeButtonGroup.getSelection().length > 0 ?
            this.displayTypeButtonGroup.getSelection()[0].type :
            null;
    var headerContext = this.actionsHeader.getFilterContext();
    this.context.groupByMode = headerContext.groupByMode;
    this.context.therapySortTypeEnum = this.therapySortTypeEnum;
    if (this.context.subview === "GRID")
    {
      this.context.numberOfDaysMode = headerContext.numberOfDaysMode;
    }

    this.sendAction("SAVE_CONTEXT", {contextData: JSON.stringify(this.context)});
  },

  _getDefaultSearchDate: function(dayCount)
  {
    var searchDate = CurrentTime.get();
    searchDate.setDate(searchDate.getDate() - dayCount + 1);
    return searchDate;
  },

  /**
   * Reloads current patients cumulative antipsychotic percentage and refreshes
   * the notification in {@link #_patientDataContainer}
   */
  refreshPatientsCumulativeAntipsychoticPercentage: function()
  {
    var self = this;
    if (this.getCumulativeAntipsychoticDoseEnabled())
    {
      this.getRestApi().loadPatientsCumulativeAntipsychoticPercentage().then(function(percentage)
      {
        self.setCumulativeMaxDosePercentage(percentage);
        self.getPatientDataContainer().refreshCumulativeMaxDose();
      });
    }
  },

  /**
   * Refreshes the therapies, depending on which subview is available.
   * @param {Boolean} [repaintTimeline=false]
   * @private
   */
  refreshTherapies: function(repaintTimeline)
  {
    if (this.therapyGrid)
    {
      this.therapyGrid.reloadGridData();
    }
    else if (this.timelineContainer)
    {
      this.timelineContainer.reloadTimelines(repaintTimeline === true, this.therapySortTypeEnum);
    }
    else if (this.pharmacistReviewContainer)
    {
      this.pharmacistReviewContainer.refreshTherapies();
    }
    this._saveContext();
    this.refreshPatientsCumulativeAntipsychoticPercentage();
  },

  /**
   * @return {tm.jquery.Promise} resolved once the dialog closes and the entered reference weight is saved and refreshed.
   */
  openReferenceWeightDialog: function()
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var appFactory = this.getAppFactory();

    var dialog = appFactory.createDataEntryDialog(
        this.getDictionary("reference.weight"),
        null,
        new app.views.medications.common.overview.ReferenceWeightDataEntryContainer({
          cls: 'therapy-reference-weight',
          view: self,
          startProcessOnEnter: true,
          weight: this.getPatientData().getWeightInKg()
        }),
        function(resultData)
        {
          if (resultData)
          {
            self._onReferenceWeightChange(resultData.value);
            deferred.resolve();
          }
        },
        180,
        130
    );
    dialog.addTestAttribute('reference-weight-dialog');
    dialog.setHideOnEscape(false);
    dialog.show();

    return deferred.promise();
  },

  openMedicationOrderingDialog: function(presetTherapies)
  {
    var self = this;
    if (!this.isReferenceWeightInputRequired())
    {
      this._openMedicationOrderingDialog(presetTherapies);
    }
    else
    {
      this.openReferenceWeightDialog()
          .then(function()
          {
            self._openMedicationOrderingDialog(presetTherapies);
          });
    }
  },

  openOutpatientOrderingDialog: function()
  {
    var self = this;
    if (!this.isReferenceWeightInputRequired())
    {
      this._openOutpatientOrderingDialog();
    }
    else
    {
      this.openReferenceWeightDialog()
          .then(function()
          {
            self._openOutpatientOrderingDialog();
          });
    }
  },

  openT2T3OrderingDialog: function(mentalHealthReportType)
  {
    var centralCaseData = this.getCentralCaseData();

    if (tm.jquery.Utils.isEmpty(centralCaseData) || tm.jquery.Utils.isEmpty(centralCaseData.outpatient))
    {
      var errorSystemDialog = this.getAppFactory().createErrorSystemDialog(this.getDictionary("patient.not.hospitalised") + ".", 400, 122);
      errorSystemDialog.show();
      return;
    }

    this._openT2T3OrderingDialog(mentalHealthReportType);
  },

  _openMedicationOrderingDialog: function(presetTherapies)
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var prescribeByTemplatesOnlyMode = !this.getTherapyAuthority().isManageInpatientPrescriptionsAllowed() &&
        this.getTherapyAuthority().isPrescribeByTemplatesAllowed();
    var orderingDialog = appFactory.createDataEntryDialog(
        self.getDictionary("prescribe.medications"),
        null,
        new app.views.medications.ordering.MedicationsOrderingContainer({
          view: self,
          patientId: self.patientId,
          presetTherapies: presetTherapies,
          prescribeByTemplatesOnlyMode: prescribeByTemplatesOnlyMode
        }),
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(true);
          }
        },
        this._getOrderingDialogWidth(),
        this._getOrderingDialogHeight()
    );
    orderingDialog.setContainmentElement(this.getDom());
    orderingDialog.addTestAttribute('medication-ordering-dialog');
    orderingDialog.setFitSize(true);
    orderingDialog.setHideOnEscape(false);
    var confirmButton = orderingDialog.getBody().footer.confirmButton;
    confirmButton.setText(this.getDictionary("prescribe"));
    orderingDialog.show();
  },

  _openOutpatientOrderingDialog: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var templatesOnlyMode = !this.getTherapyAuthority().isManageOutpatientPrescriptionsAllowed() &&
        this.getTherapyAuthority().isPrescribeByTemplatesAllowed();

    var content = new app.views.medications.ordering.outpatient.OutpatientOrderingContainer({
      view: self,
      patientId: self.patientId,
      prescribeByTemplatesOnlyMode: templatesOnlyMode
    });

    this.actionCallbackListener = content;

    var dialog = appFactory.createDataEntryDialog(
        self.getDictionary("outpatient.prescription"),
        null,
        content,
        dialogResultCallback,
        content.getDefaultWidth(),
        content.getDefaultHeight()
    );
    dialog.setContainmentElement(this.getDom());
    dialog.setHideOnEscape(false);

    app.views.medications.MedicationUtils.attachOutpatientOrderingDialogFooterButtons(this, dialog, dialogResultCallback);

    dialog.show();

    function dialogResultCallback(resultData)
    {
      if (resultData && resultData.success && !tm.jquery.Utils.isEmpty(self.documentationContainer))
      {
        self.documentationContainer.refreshData();
      }
      self.actionCallbackListener = null;
    }
  },

  /**
   * @param {string} mentalHealthReportType of {@link app.views.medications.TherapyEnums.mentalHealthDocumentType}
   * @private
   */
  _openT2T3OrderingDialog: function(mentalHealthReportType)
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var content = new app.views.medications.mentalHealth.T2T3OrderingContainer({
      view: self,
      patientId: self.patientId,
      reportType: mentalHealthReportType
    });

    this.actionCallbackListener = content;
    var dialog = appFactory.createDataEntryDialog(
        mentalHealthReportType,
        null,
        content,
        function()
        {
          self.actionCallbackListener = null;
        },
        content.getDefaultWidth(),
        content.getDefaultHeight()
    );
    dialog.setContainmentElement(this.getDom());
    dialog.setHideOnEscape(false);

    var confirmButton = dialog.getBody().getFooter().getConfirmButton();
    confirmButton.setText(this.getDictionary("save"));

    dialog.show();
  },

  _getOrderingDialogWidth: function()
  {
    return $(window).width() - 50;
  },

  _getOrderingDialogHeight: function()
  {
    return $(window).height() - 10;
  },

  /**
   * @param {Number} referenceWeight
   * @private
   */
  _onReferenceWeightChange: function(referenceWeight)
  {
    var subview = this.actionsHeader.getSubview();
    var displayDischargeListNotification = this._isPatientBannerDischargeListNotificationRequired(subview);
    this.getActiveViewData().setReferenceWeight(referenceWeight);
    this.refreshTherapies(false);
    this.getPatientDataContainer().refreshViewReferenceData();
    this.applyPatientDataContainerWarningsVisibility(this._isPatientBannerWarningRequired());
    this.getPatientDataContainer().applyDischargeListNotificationVisibility(displayDischargeListNotification);
  },

  /**
   * @param {String} subview
   * @returns {boolean}
   * @private
   */
  _isPatientBannerDischargeListNotificationRequired: function(subview)
  {
    return ['GRID', 'TIMELINE', 'PHARMACIST'].indexOf(subview) > -1;
  },

  /**
   * @returns {boolean}
   * @private
   */
  _isPatientBannerWarningRequired: function()
  {
    return !!this.getTimelineContainer() &&
        this.getTimelineContainer().getAdditionalWarnings() &&
        this.getTimelineContainer().getAdditionalWarnings().hasWarnings()
  },

  /**
   * @param {app.views.medications.common.dto.BarcodeTaskSearch} barcodeTaskSearch
   * @param {String} barcode
   * @private
   */
  _handleBarcodeScanned: function(barcodeTaskSearch, barcode)
  {
    if (barcodeTaskSearch.isTaskFound())
    {
      this.fireEvent(new tm.jquery.ComponentEvent({
        eventType: tm.views.medications.TherapyView.EVENT_TYPE_MEDICATION_BARCODE_SCANNED,
        eventData: {
          barcodeTaskSearch: barcodeTaskSearch,
          barcode: barcode
        }
      }), null);
    }
    else
    {
      this.getAppFactory().createWarningSystemDialog(
          this.getDictionary(barcodeTaskSearch.getFailedMessageKey()), 500, 160).show();
    }
  },

  /**
   * Sanitizing parameters for get and delete operations before executing.
   * @Override
   * @param url
   * @param method (post | get)
   * @param dataType (text | json)
   * @param params
   * @param objectKey
   * @param successFn (function)
   * @param failureFn (function)
   * @param displayType (app.views.common.AppNotifierDisplayType)
   * @param options (json)
   */
  sendRequest: function(url, method, dataType, params, objectKey, successFn, failureFn, displayType, options)
  {
    return this.callSuper(url,
        method,
        dataType,
        app.views.medications.RestApi.sanitizedQueryParams(params),
        objectKey,
        successFn,
        failureFn,
        displayType,
        options);
  },

  orderPreviousHospitalizationTherapies: function()
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var patientId = this.patientId;
    var previousTherapiesUrl = this.getViewModuleUrl() +
        tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPIES_FOR_PREVIOUS_HOSPITALIZATION;
    var params = {
      patientId: patientId,
      patientHeight: this.getPatientHeightInCm()
    };
    this.loadViewData(previousTherapiesUrl, params, null, function(previousTherapies)
    {
      if (patientId === self.patientId)
      {
        if (previousTherapies && previousTherapies.length !== 0)
        {
          for (var i = 0; i < previousTherapies.length; i++)
          {
            previousTherapies[i] = app.views.medications.common.TherapyJsonConverter.convert(previousTherapies[i]);
          }
          self.openMedicationOrderingDialog(previousTherapies);
        }
        else
        {
          appFactory.createWarningSystemDialog(self.getDictionary("no.previous.hospitalization.therapies"), 320, 138).show();
        }
      }
    });
  },

  showEditTherapyDialog: function(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction)
  {
    var editAllowed = true;
    if (!copyTherapy)
    {
      editAllowed = app.views.medications.MedicationTimingUtils.checkEditAllowed(therapy, this);
    }
    if (editAllowed)
    {
      if (therapy.isOrderTypeOxygen())
      {
        this._showEditOxygenTherapyDialog(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction);
      }
      else if (!therapy.isOrderTypeComplex())
      {
        this._showEditSimpleTherapyDialog(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction);
      }
      else
      {
        this._showEditComplexTherapyDialog(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction);
      }
    }
  },

  _getEditTherapyDialogTitle: function(therapyAlreadyStarted, copyTherapy)
  {
    if (copyTherapy)
    {
      return this.getDictionary("copy.therapy");
    }
    if (therapyAlreadyStarted)
    {
      return this.getDictionary("edit.therapy");
    }
    return this.getDictionary("change.prescription");
  },

  _showEditSimpleTherapyDialog: function(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var ingredientsMedicationLoader = new app.views.medications.common.TherapyMedicationDataLoader({view: this});

    ingredientsMedicationLoader.load(therapy).then(function showDialog(medicationData)
    {
      if (ingredientsMedicationLoader.isMedicationNoLongerAvailable(therapy, medicationData))
      {
        var message = self.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            self.getDictionary('stop.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 500, 160).show();
      }
      else
      {
        var editContainer = new app.views.medications.ordering.SimpleTherapyEditContainer({
          view: self,
          startProcessOnEnter: true,
          therapy: therapy,
          copyTherapy: copyTherapy,
          therapyModifiedInThePast: therapyModifiedInThePast,
          medicationData: medicationData,
          saveTherapyFunction: saveTherapyFunction ? function(result, prescriber)
          {
            saveTherapyFunction(result, editContainer, prescriber)
          } : null
        });
        var dialog = appFactory.createDataEntryDialog(
            self._getEditTherapyDialogTitle(therapy.isStarted(), copyTherapy),
            null,
            editContainer,
            function(resultData)
            {
              if (resultData)
              {
                self.refreshTherapies(false);
              }
            },
            725,
            $(window).height() - 10 < 655 ? $(window).height() - 10 : 655
        );
        dialog.addTestAttribute('simple-therapy-edit-dialog');
        dialog.setContainmentElement(self.getDom());
        dialog.setHideOnEscape(false);
        dialog.show();
      }
    });
  },

  _showEditComplexTherapyDialog: function(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction)
  {
    var self = this;
    var appFactory = this.getAppFactory();
    var ingredientsMedicationLoader = new app.views.medications.common.TherapyMedicationDataLoader({view: this});

    ingredientsMedicationLoader.load(therapy).then(function showDialog(medicationData)
    {
      if (ingredientsMedicationLoader.isMedicationNoLongerAvailable(therapy, medicationData))
      {
        var message = self.getDictionary('prescribed.medication.no.longer.available') + " <br>" +
            self.getDictionary('stop.therapy.order.alternative.medication');
        appFactory.createWarningSystemDialog(message, 500, 160).show();
      }
      else
      {
        var editContainer = new app.views.medications.ordering.ComplexTherapyEditContainer({
          view: self,
          startProcessOnEnter: true,
          therapy: therapy,
          copyTherapy: copyTherapy,
          medicationData: medicationData,
          therapyModifiedInThePast: therapyModifiedInThePast,
          saveTherapyFunction: saveTherapyFunction ?
              function(result, prescriber)
              {
                saveTherapyFunction(result, editContainer, prescriber)
              } : null
        });
        var dialog = appFactory.createDataEntryDialog(
            self._getEditTherapyDialogTitle(therapy.isStarted(), copyTherapy),
            null,
            editContainer,
            function(resultData)
            {
              if (resultData)
              {
                self.refreshTherapies(false);
              }
            },
            715,
            $(window).height() - 10 < 700 ? $(window).height() - 10 : 700
        );
        dialog.addTestAttribute('complex-therapy-edit-dialog');
        dialog.setContainmentElement(self.getDom());
        dialog.setHideOnEscape(false);
        dialog.show();
      }
    });
  },

  /**
   * @param {app.views.medication.common.dto.Therapy|app.views.medication.common.dto.OxygenTherapy} therapy
   * @param {Boolean} copyTherapy
   * @param {Boolean} therapyModifiedInThePast
   * @param {function|null} saveTherapyFunction
   * @private
   */
  _showEditOxygenTherapyDialog: function(therapy, copyTherapy, therapyModifiedInThePast, saveTherapyFunction)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var dataEntryContainer = new app.views.medications.ordering.oxygen.OxygenTherapyEditDataEntryContainer({
      view: self,
      startProcessOnEnter: true,
      therapy: therapy,
      copyMode: copyTherapy,
      saveTherapyFunction: saveTherapyFunction ? function(result, prescriber)
      {
        saveTherapyFunction(result, dataEntryContainer, prescriber)
      } : null
    });

    var dialog = appFactory.createDataEntryDialog(
        self._getEditTherapyDialogTitle(therapy.isStarted(), copyTherapy),
        null,
        dataEntryContainer,
        function(resultData)
        {
          if (resultData)
          {
            self.refreshTherapies(false);
          }
        },
        715,
        $(window).height() - 10 < 680 ? $(window).height() - 10 : 680
    );
    dialog.setContainmentElement(this.getDom());
    dialog.setHideOnEscape(false);
    dialog.show();
  },

  _abortPresentDataTask: function()
  {
    if (this._presentDataConditionalTask)
    {
      this._presentDataConditionalTask.abort();
      this._presentDataConditionalTask = null;
    }
  },

  /**
   * Will preventing the user from interacting with the toolbar content. It's used to prevent changing the view
   * or trying to open the ordering dialog until the patient data is loaded, since it's vital for other ajax
   * queries to the server.
   *
   * Using the jQuery plugin directly since the {@link app.views.common.AppView#showLoaderMask} imposes a msg.
   * @private
   */
  _showToolbarGlassLayer: function()
  {
    $(this.getDom()).find(".app-views-toolbar.portlet-header").loadmask();
  },

  /**
   * Hides the toolbar's glass layer, if present, effectively enabling interaction with the main toolbar.
   * Should be executed once the core patient data is loaded.
   * @private
   */
  _hideToolbarGlassLayer: function()
  {
    $(this.getDom()).find(".app-views-toolbar.portlet-header").unloadmask();
  },

  /**
   * @returns {boolean}
   */
  isReferenceWeightInputRequired: function()
  {
    return this.isReferenceWeightRequired() && tm.jquery.Utils.isEmpty(this.getReferenceWeight())
  },

  loadDoseForms: function()
  {
    var self = this;
    this.getRestApi().loadDoseForms(true).then(function(doseForms)
    {
      self.doseForms.length = 0;
      $.merge(self.doseForms, doseForms);
    });
  },

  getNoTherapiesField: function(hidden)
  {
    var self = this;
    var noTherapiesContainer = new tm.jquery.Container({
      cls: 'no-data-container',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "flex-start", 10),
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      hidden: hidden
    });
    noTherapiesContainer.addTestAttribute('no-therapies-container');
    var infoAlert = new tm.jquery.Alert({
      type: "info",
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      style: "background-color:white; border-color:white; font-size: 18px; color: #dcdcdc",
      content: self.getDictionary("no.therapies.found"),
      closeButton: false
    });
    infoAlert.setAlignSelf("center");
    noTherapiesContainer.add(infoAlert);
    this.noTherapiesField = noTherapiesContainer;
    return noTherapiesContainer;
  },

  /**
   * @param {Boolean} visible
   */
  applyPatientDataContainerWarningsVisibility: function(visible)
  {
    this.getPatientDataContainer().applyTimelineAdditionalWarningsVisibility(visible)
  },

  onShowRelatedPharmacistReviews: function(therapyData, refreshCallback)
  {
    var self = this;
    var appFactory = this.getAppFactory();

    var loadReviewsUrl = self.getViewModuleUrl() +
        tm.views.medications.TherapyView.SERVLET_PATH_GET_THERAPY_PHARMACIST_REVIEWS;

    self.showLoaderMask();

    var params = {
      patientId: self.patientId,
      therapyCompositionUid: therapyData.therapy.compositionUid,
      language: self.getViewLanguage()
    };

    self.loadViewData(loadReviewsUrl, params, null, function(data)
    {
      data = tm.jquery.Utils.isEmpty(data) ? [] : data;

      var reviews = data.map(function(item)
      {
        return app.views.medications.pharmacists.dto.PharmacistMedicationReview.fromJson(item);
      });

      var contentContainer = new app.views.medications.common.therapy.PharmacistTherapyReviewDataEntryContainer({
        view: self,
        reviews: reviews,
        refreshCallbackFunction: refreshCallback
      });

      var closeFooterButtonsContainer = appFactory.createCloseFooterButtonsContainer();

      var dialogContentAndFooterButtonsContainer
          = appFactory.createContentAndFooterButtonsContainer(contentContainer, closeFooterButtonsContainer);

      var dialog = appFactory.createDefaultDialog(
          self.getDictionary("pharmacists.reviews"),
          null,
          dialogContentAndFooterButtonsContainer,
          null,
          jQuery(window).width() - 10 < 700 ? jQuery(window).width() - 10 : 700,
          jQuery(window).height() - 10 < 650 ? jQuery(window).height() - 10 : 650
      );
      contentContainer.setDialog(dialog);

      var closeButton = closeFooterButtonsContainer.getCloseButton();
      closeButton.setHandler(function()
      {
        dialog.hide();
      });
      dialog.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function(component, componentEvent, elementEvent)
      {
        elementEvent.stopPropagation();
      });

      dialog.setModal(false);
      dialog.setHideOnEscape(true);
      self.hideLoaderMask();
      dialog.show();
    });
  },

  getPatientId: function()
  {
    return this.patientId;
  },
  getPresetCentralCaseId: function()
  {
    return this.presetCentralCaseId;
  },
  getTimelineContainer: function()
  {
    return this.timelineContainer;
  },

  getCareProfessionals: function()
  {
    return this.careProfessionals;
  },
  getDoseForms: function()
  {
    return this.doseForms;
  },
  getRoutes: function()
  {
    return this.routes;
  },

  getUnitsHolder: function()
  {
    return this._unitsHolder;
  },

  getCentralCaseData: function()
  {
    return !!this.getPatientData() ? this.getPatientData().getCentralCaseDto() : null;
  },

  getCurrentUserAsCareProfessional: function()
  {
    return this.userPerson;
  },
  getCareProvider: function()
  {
    var centralCaseData = this.getCentralCaseData();
    return !tm.jquery.Utils.isEmpty(centralCaseData) ? centralCaseData.careProvider : null;
  },
  getCareProviderId: function()
  {
    var careProvider = this.getCareProvider();
    return careProvider ? careProvider.id : null;
  },
  getCareProviderName: function()
  {
    var careProvider = this.getCareProvider();
    return careProvider ? careProvider.name : null;
  },

  getContext: function()
  {
    return this.context;
  },
  setContext: function(context)
  {
    this.context = context;
  },
  getOptimizeForPerformance: function()
  {
    return this.optimizeForPerformance;
  },
  isMentalHealthReportEnabled: function()
  {
    return this.mentalHealthReportEnabled === true;
  },
  isAutoAdministrationChartingEnabled: function()
  {
    return this.autoAdministrationCharting === true;
  },
  getViewMode: function()
  {
    return this.mode;
  },
  getPresetDate: function()
  {
    return this.presetDate;
  },
  setPresetDate: function(presetDate)
  {
    this.presetDate = presetDate;
  },
  getProblemDescriptionNamedIdentitiesMap: function()
  {
    return this.problemDescriptionNamedIdentitiesMap;
  },
  getPharmacistReviewReferBackPreset: function()
  {
    return this.pharmacistReviewReferBackPreset;
  },
  getMedicationsSupplyPresent: function()
  {
    return this.medicationsSupplyPresent;
  },

  /**
   * @return {app.views.medications.common.dto.PatientDataForMedications|null}
   */
  getPatientData: function()
  {
    return this._patientData;
  },

  /**
   * @return {Number|null}
   */
  getPatientHeightInCm: function()
  {
    return !!this.getPatientData() ? this.getPatientData().getHeightInCm() : null;
  },

  /**
   * @return {String}
   */
  getPatientNextLinkName: function()
  {
    return this.getActiveViewData().getNextLinkName();
  },
  /**
   * @return {String}
   */
  getPatientLastLinkNamePrefix: function()
  {
    return this.getActiveViewData().getLastLinkName();
  },
  /**
   * @param {String|null} patientLastLinkName
   */
  setPatientLastLinkNamePrefix: function(patientLastLinkName)
  {
    this.getActiveViewData().setLastLinkName(patientLastLinkName);
  },

  /**
   * @return {Number|null}
   */
  getReferenceWeight: function()
  {
    return this.getActiveViewData().getReferenceWeight();
  },

  /**
   * @return {Date|null}
   */
  getReferenceWeightDate: function()
  {
    return this.getActiveViewData().getReferenceWeightDate();
  },

  /**
   * @returns {Boolean} true if the administration witnessing functionality is enabled, otherwise false. There are other
   * conditions that determine whether witness is required or not, but this property overrides them if set to false.
   */
  isAdministrationWitnessingEnabled: function()
  {
    return this.administrationWitnessingEnabled === true;
  },

  /**
   * @returns {Boolean} true if the administration witnessing is required for therapies with IV route of administration.
   */
  isAdministrationWitnessingIvRequired: function()
  {
    return this.administrationWitnessingIvRequired === true;
  },

  /**
   * @return {boolean} true, if the the witness verification should be mocked by the client, otherwise false.
   */
  isAdministrationWitnessingMocked: function()
  {
    return this.administrationWitnessingMocked === true;
  },

  /**
   * @returns {boolean}
   */
  isAntimicrobialDaysCountStartsWithOne: function()
  {
    return this.antimicrobialDaysCountStartsWithOne === true;
  },

  /**
   * @returns {boolean}
   */
  isReferenceWeightRequired: function()
  {
    return this.referenceWeightRequired === true;
  },

  /**
   * @return {boolean}
   */
  getMedicationsShowHeparinPane: function()
  {
    return this.medicationsShowHeparinPane === true;
  },

  /**
   * @return {boolean}
   */
  getCumulativeAntipsychoticDoseEnabled: function()
  {
    return this.cumulativeAntipsychoticDoseEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isInfusionBagEnabled: function()
  {
    return this.infusionBagEnabled === true;
  },

  /**
   * @returns {boolean}
   */
  isFormularyFilterEnabled: function()
  {
    return this.formularyFilterEnabled === true;
  },
  /**
   * @returns {boolean}
   */
  isNonFormularyMedicationSearchAllowed: function()
  {
    return this.nonFormularyMedicationSearchAllowed === true;
  },
  getOutpatientPrescriptionType: function()
  {
    return this.outpatientPrescriptionType;
  },

  setActionCallbackListener: function(content)
  {
    this.actionCallbackListener = content;
  },

  /**
   * @returns {Boolean}
   */
  isDoctorReviewEnabled: function()
  {
    return this.doctorReviewEnabled === true;
  },

  /**
   * @returns {Boolean}
   */
  isDoseRangeEnabled: function()
  {
    return this.doseRangeEnabled === true;
  },

  /**
   * Can the user substitute medication when administering? Environment based setting - the actual availability of the action
   * may be based on additional conditions, even when the setting is turned on, and should never be available if turned off.
   * @returns {Boolean}
   */
  isSubstituteAdministrationMedicationEnabled: function()
  {
    return this.substituteAdministrationMedicationEnabled === true;
  },

  /**
   * @returns {app.views.medications.RestApi}
   */
  getRestApi: function()
  {
    return this._restApi;
  },

  /**
   *
   * @returns {tm.views.medications.TherapyAuthority}
   */
  getTherapyAuthority: function()
  {
    return this._therapyAuthority;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy}
   */
  getTherapyToEdit: function()
  {
    return this.therapyToEdit;
  },

  /**
   * @return {app.views.medications.common.therapy.InformationSourceHolder}
   */
  getInformationSourceHolder: function()
  {
    return this._informationSourceHolder;
  },

  /**
   * @return {app.views.medications.common.therapy.ChangeReasonTypeHolder}
   */
  getChangeReasonTypeHolder: function()
  {
    return this._changeReasonTypeHolder;
  },

  /**
   * @protected
   * @return {app.views.medications.common.overview.PatientDataContainer}
   */
  getPatientDataContainer: function()
  {
    return this._patientDataContainer;
  },

  /**
   * @returns {Number|null}
   */
  getCumulativeMaxDosePercentage: function()
  {
    return this._cumulativeMaxDosePercentage;
  },

  /**
   * @param {Number|null} value
   */
  setCumulativeMaxDosePercentage: function(value)
  {
    this._cumulativeMaxDosePercentage = value;
  },

  /**
   * @protected
   * @return {app.views.medications.common.dto.TherapyViewPatient}
   */
  getActiveViewData: function()
  {
    return this._activeViewData;
  },

  /**
   * @protected
   * @return {Boolean}
   */
  isRecentHospitalization: function()
  {
    return !!this.getActiveViewData() ? this.getActiveViewData().isRecentHospitalization() : false;
  },

  /**
   * @return {{timestampsList: []}}
   */
  getAdministrationTiming: function()
  {
    return this.getActiveViewData().getAdministrationTiming();
  },

  /**
   * @return {{startHour: Number, startMinute: Number, endHour: Number, endMinute: Number}}
   */
  getRoundsInterval: function()
  {
    return this.getActiveViewData().getRoundsInterval();
  },

  /**
   * Should administration time for administration in the past be preset to now?
   * @returns {boolean}
   */
  isPresetPastAdministrationTimeToNow: function()
  {
    return this.presetPastAdministrationTimeToNow === true;
  },

  /**
   * Are therapy dose and infusion rate calculations enabled?
   * @returns {boolean}
   */
  isDoseCalculationsEnabled: function()
  {
    return this.doseCalculationsEnabled === true;
  },

  /**
   * @return {number} the configured number of days for which to display the pharmacist reviews.
   */
  getPharmacistReviewDisplayDays: function()
  {
    return this.pharmacistReviewDisplayDays;
  },

  /**
   * Is therapies review for single day enabled?
   * @returns {boolean}
   */
  isSingleDayTherapiesOverviewEnabled: function()
  {
    return this.singleDayTherapiesOverviewEnabled === true;
  },

  /**
   * Should the user be prompted to enter the reason for suspending the therapy?
   * @returns {boolean}
   */
  isSuspendReasonMandatory: function()
  {
    return this.suspendReasonMandatory === true;
  },

  /**
   * Should the user be prompted to enter the reason for stopping the therapy?
   * @returns {boolean}
   */
  isStopReasonMandatory: function()
  {
    return this.stopReasonMandatory === true;
  },

  /**
   * @Override
   */
  destroy: function()
  {
    this._abortPresentDataTask();
    this.callSuper();
  },

  onViewInit: function()
  {
    this.callSuper();

    if (this.isDevelopmentMode() && !this.patientId)
    {
      // update data command //
      var updateDataCommand = {
        //Steluca Zubčić
        // 397965328
        //"update":{"data":{"patientId":398227601, viewType: "GRID"}}};
        //"update":{"data":{"patientId":397965335, viewType: "GRID"}}};
        //"update":{"data":{"patientId":397965335, "viewType": "PHARMACIST"}}};
        //"update":{"data":{"patientId":408510900, "viewType": "DOCUMENTATION"}}};
        // "update": {"data": {"patientId": 113804972, "viewType": "TIMELINE"}}
        "update": {"data": {"patientId": 463520801, "viewType": "TIMELINE"}}
      };

      var self = this;
      setTimeout(function()
      {
        self.onViewCommand(updateDataCommand);
      }, 100);
    }
  }
});
