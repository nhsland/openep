Class.define('app.views.medications.RestApi', 'tm.jquery.Object', {
  statics: {
    SERVLET_PATH_LOAD_THERAPY_VIEW_PATIENT_DATA: '/getTherapyViewPatientData',
    SERVLET_PATH_MEDICATION_DATA: '/medicationdata',
    SERVLET_PATH_FIND_MEDICATIONS: '/findmedications',
    SERVLET_PATH_FIND_MEDICATION_PRODUCTS: '/findMedicationProducts',
    SERVLET_PATH_FIND_SIMILAR_MEDICATIONS: '/findSimilarMedications',
    SERVLET_PATH_SAVE_MEDICATIONS_ORDER: '/saveMedicationsOrder',
    SERVLET_PATH_MODIFY_THERAPY: '/modifyTherapy',
    SERVLET_PATH_GET_THERAPY_CHANGE_TYPE: '/getTherapyChangeTypes',
    SERVLET_PATH_CREATE_ADMINISTRATION_TASK: '/createAdministrationTask',
    SERVLET_PATH_CONFIRM_THERAPY_ADMINISTRATION: '/confirmTherapyAdministration',
    SERVLET_PATH_GET_MEDICATION_DATA_FOR_MULTIPLE_IDS: '/medicationDataForMultipleIds',
    SERVLET_PATH_RESCHEDULE_ADMINISTRATION_TASKS: '/rescheduleAdministrationTasks',
    SERVLET_PATH_RESCHEDULE_ADMINISTRATION_TASK: '/rescheduleAdministrationTask',
    SERVLET_PATH_RESCHEDULE_DOCTOR_REVIEW_TASK: '/rescheduleTherapyDoctorReviewTask',
    SERVLET_PATH_RESCHEDULE_IV_TO_ORAL_TASK: '/rescheduleIvToOralTask',
    SERVLET_PATH_GET_THERAPY_AUDIT_TRAIL: '/getTherapyAuditTrail',
    SERVLET_PATH_SET_ADMINISTRATION_TITRATION_DOSE: '/setAdministrationTitratedDose',
    SERVLET_PATH_GET_THERAPY: '/getTherapy',
    SERVLET_PATH_GET_UNLICENSED_MEDICATION_WARNING: '/getUnlicensedMedicationWarning',
    SERVLET_PATH_SET_ADMINISTRATION_DOCTORS_COMMENT: '/setAdministrationDoctorsComment',
    SERVLET_PATH_GET_THERAPIES_FOR_WARNINGS: "/getTherapiesForWarnings",
    SERVLET_PATH_FIND_WARNINGS: "/findMedicationWarnings",
    SERVLET_PATH_APPLY_MEDICATION_RULE: '/applyMedicationRule',
    SERVLET_PATH_GET_REMAINING_INFUSION_BAG_QUANTITY: '/getRemainingInfusionBagQuantity',
    SERVLET_PATH_GET_UNITS_HOLDER: '/unitsHolder',
    SERVLET_PATH_GET_THERAPY_TEMPLATES: '/getTherapyTemplates',
    SERVLET_PATH_GET_ALL_THERAPY_TEMPLATES: '/getAllTherapyTemplates',
    SERVLET_PATH_GET_MEDICATION_ID_FOR_BARCODE: '/getMedicationIdForBarcode',
    SERVLET_PATH_GET_ADMINISTRATION_TASK_FOR_BARCODE: '/getAdministrationTaskForBarcode',
    SERVLET_PATH_GET_ORIGINAL_THERAPY_ID: '/getOriginalTherapyId',
    SERVLET_PATH_GET_DOSE_FORMS: '/getDoseForms',
    SERVLET_PATH_LOAD_FILL_PHARMACISTS_THERAPY_EDIT: '/fillPharmacistReviewTherapyOnEdit',
    SERVLET_PATH_GET_RECONCILIATION_GROUPS: '/getReconciliationGroups',
    SERVLET_PATH_GET_ROUTES: '/getRoutes',
    SERVLET_PATH_START_NEW_RECONCILIATION: '/startNewReconciliation',
    SERVLET_PATH_GET_MEDICATIONS_ON_ADMISSION: '/getMedicationsOnAdmission',
    SERVLET_PATH_GET_MEDICATIONS_ON_DISCHARGE: '/getMedicationsOnDischarge',
    SERVLET_PATH_SAVE_MEDICATIONS_ON_ADMISSION: '/saveMedicationsOnAdmission',
    SERVLET_PATH_SAVE_MEDICATIONS_ON_DISCHARGE: '/saveMedicationsOnDischarge',
    SERVLET_PATH_REVIEW_MEDICATIONS_ON_ADMISSION: '/reviewAdmission',
    SERVLET_PATH_GET_THERAPY_GROUPS_ON_ADMISSION: '/getTherapiesOnAdmissionGroups',
    SERVLET_PATH_GET_THERAPY_GROUPS_ON_DISCHARGE: '/getTherapiesOnDischargeGroups',
    SERVLET_PATH_FILL_DISPLAY_VALUES: '/fillTherapyDisplayValues',
    SERVLET_PATH_SAVE_TEMPLATE: '/saveTherapyTemplate',
    SERVLET_PATH_GET_THERAPY_TEMPLATE_GROUPS: '/getTherapyTemplateGroups',
    SERVLET_PATH_GET_ACTIVE_THERAPIES: '/getActiveTherapies',
    SERVLET_PATH_LOAD_THERAPY_FLOW_DATA: '/therapyflowdata',
    SERVLET_PATH_SUSPEND_ALL_THERAPIES: '/suspendAllTherapies',
    SERVLET_PATH_STOP_ALL_THERAPIES: '/stopAll',
    SERVLET_PATH_SUSPEND_ALL_THERAPIES_TEMPORARY_LEAVE: '/suspendAllTherapiesOnTemporaryLeave',
    SERVLET_PATH_GET_INFORMATION_SOURCES: '/getInformationSources',
    SERVLET_PATH_GET_VMP_MEDICATIONS: '/getVmpMedications',
    SERVLET_PATH_GET_DISPENSE_SOURCES: '/getDispenseSources',
    SERVLET_PATH_GET_NUMERIC_VALUE: '/getNumericValue',
    SERVLET_PATH_HAS_THERAPY_CHANGED: '/hasTherapyChanged',
    SERVLET_PATH_GET_ACTIVE_AND_PAST_THERAPIES_REPORT: '/getActiveAndPastTherapiesReport',
    GET_PAST_THERAPIES_REPORT: '/getPastTherapiesReport',
    GET_TEMPLATE_REPORT: '/getTemplateReport',
    SERVLET_PATH_FIND_CURRENT_THERAPIES_WARNINGS: '/findCurrentTherapiesWarnings',
    SERVLET_PATH_GET_MEDICATION_DOCUMENT: '/getMedicationDocument',
    SERVLET_PATH_SAVE_OUTPATIENT_PRESCRIPTION: '/saveOutpatientPrescription',
    SERVLET_PATH_GET_THERAPY_ADMINISTRATION_TIMES: '/calculateTherapyAdministrationTimes',
    SERVLET_PATH_GET_THERAPY_SURGERY_REPORT: '/createTherapySurgeryReport',
    SERVLET_PATH_GET_CURRENT_HOSPITALIZATION_MENTAL_HEALTH_DRUGS: '/getCurrentHospitalizationMentalHealthTherapies',
    SERVLET_PATH_GET_MEDICATION_ROUTES: '/getMedicationRoutes',
    SERVLET_PATH_SAVE_MENTAL_HEALTH_DOCUMENT: '/saveMentalHealthDocument',
    SERVLET_PATH_GET_MENTAL_HEALTH_TEMPLATES: '/getMentalHealthTemplates',
    SERVLET_PATH_GET_DISCHARGE_CREATED: '/dischargeCreated',
    SERVLET_PATH_GET_PATIENTS_CUMULATIVE_ANTIPSYCHOTIC_PERCENTAGE: '/getPatientsCumulativeAntipsychoticPercentage',
    SERVLET_PATH_HANDLE_ADDITIONAL_WARNINGS_ACTION: '/handleAdditionalWarningsAction',
    SERVLET_PATH_SUSPEND_THERAPY: "/suspendTherapy",
    SERVLET_PATH_RELOAD_SINGLE_THERAPY_AFTER_ACTION: '/reloadSingleTherapyAfterAction',
    SERVLET_PATH_ABORT_THERAPY: "/abortTherapy",
    SERVLET_PATH_REVIEW_THERAPY: "/reviewTherapy",
    SERVLET_PATH_REISSUE_THERAPY: "/reissueTherapy",
    SERVLET_PATH_REVIEW_MEDICATIONS_ON_DISCHARGE: '/reviewDischarge',
    SERVLET_PATH_CANCEL_ADMINISTRATION: '/cancelAdministrationTask',
    SERVLET_PATH_UNCANCEL_ADMINISTRATION: '/uncancelAdministrationTask',
    SERVLET_PATH_DELETE_ADMINISTRATION: '/deleteAdministration',
    SERVLET_PATH_DELETE_TASK: '/deleteTask',
    SERVLET_PATH_CALCULATE_NEXT_THERAPY_ADMINISTRATION_TIME: '/calculateNextTherapyAdministrationTime',

    /**
     * Removes null and undefined properties on the passed object and returns the reference to it.
     * @param {*} params
     * @return {*} params
     */
    sanitizedQueryParams: function(params)
    {
      if (tm.jquery.Utils.isObject(params) && !tm.jquery.Utils.isFunction(params))
      {
        for (var prop in params)
        {
          if (params.hasOwnProperty(prop) && tm.jquery.Utils.isEmpty(params[prop], true))
          {
            delete params[prop];
          }
        }
      }
      return params;
    }
  },

  /** @type app.views.common.AppView */
  view: null,
  _loadMedicationWarningsRequests: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._loadMedicationWarningsRequests = {};
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadTherapyViewPatientData: function(preventMask)
  {
    var view = this.getView();
    var self = this;
    var getTherapyViewPatientDataUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_LOAD_THERAPY_VIEW_PATIENT_DATA;
    var patientId = this.getView().getPatientId();
    var params = {
      patientId: patientId,
      centralCaseId: this.getView().getPresetCentralCaseId()
    };
    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(
        getTherapyViewPatientDataUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(app.views.medications.common.dto.TherapyViewPatient.fromJson(data, patientId));
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param medicationId
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedicationData: function(medicationId, preventMask)
  {
    var view = this.getView();
    var self = this;
    var medicationDataUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_MEDICATION_DATA;
    var params = {medicationId: medicationId};
    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(medicationDataUrl, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          var medicationData = !tm.jquery.Utils.isEmpty(data) ?
              app.views.medications.common.dto.MedicationData.fromJson(data) : null;
          deferred.resolve(medicationData);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {Array<String>} ids
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedicationDataForMultipleIds: function(ids, preventMask)
  {
    var view = this.getView();
    var self = this;
    var deferred = new tm.jquery.Deferred;
    var restUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_MEDICATION_DATA_FOR_MULTIPLE_IDS;
    var params = {medicationIds: JSON.stringify(ids)};

    this._showLoaderMask(preventMask);

    view.loadViewData(restUrl, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);

          if (!tm.jquery.Utils.isArray(data)) data = [data];
          var medicationData = data.map(function(item)
          {
            return app.views.medications.common.dto.MedicationData.fromJson(item);
          });
          deferred.resolve(medicationData);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {String} searchQuery
   * @param {Array<String>|null} [additionalFilters=null]
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedications: function(searchQuery, additionalFilters, preventMask)
  {
    var view = this.getView();
    var self = this;

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_FIND_MEDICATIONS;

    var params = {
      searchQuery: searchQuery,
      additionalFilters: tm.jquery.Utils.isArray(additionalFilters) ?
          JSON.stringify(additionalFilters) :
          undefined
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(url, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {String} medicationId
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadSimilarMedications: function(medicationId, preventMask) //Similar medications have same generic and route
  {
    var view = this.getView();
    var self = this;

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_FIND_SIMILAR_MEDICATIONS;

    var params = {
      medicationId: medicationId
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(url, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {string} medicationId
   * @param {Array<number>} [routes=null]
   * @param {String|undefined|null} [releaseType=undefined] of {@link app.views.medications.TherapyEnums.releaseType}
   * @param {Number|undefined|null} [releaseHours=undefined]
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedicationProducts: function(medicationId, routes, releaseType, releaseHours, preventMask)
  {
    var view = this.getView();
    var self = this;

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_FIND_MEDICATION_PRODUCTS;

    var routeIds = routes ?
        routes.map(function(route)
        {
          return route.id;
        }) :
        null;

    var params = {
      medicationId: medicationId,
      routeIds: routeIds,
      releaseType: releaseType,
      releaseHours: releaseHours
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(url, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          data = data.map(function convert(item)
          {
            return new app.views.medications.common.dto.Medication(item);
          });
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {Array<app.views.medications.ordering.dto.SaveMedicationOrder>} medicationOrders
   * @param {Object} prescriber
   * @param {Date|null} saveDateTime
   * @param {String|null} lastLinkName
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  saveMedicationsOrder: function(medicationOrders, prescriber, saveDateTime, lastLinkName, preventMask)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SAVE_HUB;

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_SAVE_MEDICATIONS_ORDER;

    var centralCaseData = view.getCentralCaseData();

    var params = {
      patientId: view.getPatientId(),
      medicationOrders: JSON.stringify(medicationOrders),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      hospitalizationStart: view.getPatientData() && view.getPatientData().getHospitalizationStart() ?
          view.getPatientData().getHospitalizationStart().getTime() :
          null,
      careProviderId: view.getCareProviderId(),
      prescriber: JSON.stringify(prescriber),
      lastLinkName: lastLinkName,
      saveDateTime: saveDateTime ? JSON.stringify(saveDateTime) : null,
      language: view.getViewLanguage()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    viewHubNotifier.actionStarted(hubAction);
    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  loadTherapyChangeReasonTypeMap: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_CHANGE_TYPE;

    var params = {
      language: view.getViewLanguage()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(url, params, null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          if (data)
          {
            // append suspend on admission reasons to suspend reasons so they can be used under one key in the suspend dialog
            // then simply check if it belongs to one but not the other group
            var orderEnums = app.views.medications.TherapyEnums.actionReasonTypeEnum;
            if (data.hasOwnProperty(orderEnums.SUSPEND_ADMISSION) &&
                tm.jquery.Utils.isArray(data[orderEnums.SUSPEND_ADMISSION]))
            {
              if (!data.hasOwnProperty(orderEnums.SUSPEND))
              {
                data[orderEnums.SUSPEND] = [];
              }
              data[orderEnums.SUSPEND] = data[orderEnums.SUSPEND].concat(data[orderEnums.SUSPEND_ADMISSION]);
            }
          }
          else
          {
            data = {};
          }
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {app.views.medications.common.dto.TherapyChangeReason} changeReason
   * @param {Object} prescriber
   * @param {Date} saveDateTime
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  modifyTherapy: function(therapy, changeReason, prescriber, saveDateTime, preventMask)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SAVE_HUB;
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_MODIFY_THERAPY;

    var params = {
      patientId: view.getPatientId(),
      therapy: JSON.stringify(therapy),
      changeReason: changeReason ? JSON.stringify(changeReason) : null,
      centralCaseId: view.getCentralCaseData() ? view.getCentralCaseData().centralCaseId : null,
      careProviderId: view.getCareProviderId(),
      prescriber: JSON.stringify(prescriber),
      saveDateTime: saveDateTime ? JSON.stringify(saveDateTime) : null
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    viewHubNotifier.actionStarted(hubAction);
    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Boolean} requestSupply
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  createAdministrationTask: function(therapy, administration, requestSupply, preventMask)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_CONFIRM_ADMINISTRATION_HUB;
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_CREATE_ADMINISTRATION_TASK;

    var params = {
      therapyCompositionUid: therapy.getCompositionUid(),
      ehrOrderName: therapy.getEhrOrderName(),
      patientId: view.getPatientId(),
      administration: JSON.stringify(administration),
      requestSupply: requestSupply === true
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    viewHubNotifier.actionStarted(hubAction);
    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Object} administration
   * @param {Boolean} editMode
   * @param {Boolean} requestSupply
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  confirmAdministrationTask: function(therapy, administration, editMode, requestSupply, preventMask)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_CONFIRM_ADMINISTRATION_HUB;
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_CONFIRM_THERAPY_ADMINISTRATION;

    var centralCaseData = view.getCentralCaseData();

    var params = {
      therapyCompositionUid: therapy.getCompositionUid(),
      ehrOrderName: therapy.getEhrOrderName(),
      patientId: view.getPatientId(),
      editMode: editMode,
      administration: JSON.stringify(administration),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: view.getCareProviderId(),
      requestSupply: requestSupply === true
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    viewHubNotifier.actionStarted(hubAction);
    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {string} taskId
   * @param {Date} selectedTimestamp
   * @param {string} therapyId
   * @param {Boolean} moveSingle
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  rescheduleAdministrationTasks: function(taskId, selectedTimestamp, therapyId, moveSingle, preventMask)
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var view = this.getView();
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_MOVE_ADMINISTRATION_HUB;
    viewHubNotifier.actionStarted(hubAction);
    var params = {
      patientId: view.getPatientId(),
      taskId: taskId,
      newTime: JSON.stringify(selectedTimestamp),
      therapyId: therapyId
    };

    var url = this.view.getViewModuleUrl() + (moveSingle === true ?
        app.views.medications.RestApi.SERVLET_PATH_RESCHEDULE_ADMINISTRATION_TASK :
        app.views.medications.RestApi.SERVLET_PATH_RESCHEDULE_ADMINISTRATION_TASKS);

    this._showLoaderMask(preventMask);

    this.view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve()
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject()
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {string} taskId
   * @param {Date} selectedTimestamp
   * @param {String} comment
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  rescheduleTherapyDoctorReviewTask: function(taskId, selectedTimestamp, comment, preventMask)
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_MOVE_ADMINISTRATION_HUB;
    viewHubNotifier.actionStarted(hubAction);
    var params = {
      taskId: taskId,
      newTime: JSON.stringify(selectedTimestamp),
      comment: comment
    };

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_RESCHEDULE_DOCTOR_REVIEW_TASK;

    this._showLoaderMask(preventMask);

    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve()
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject()
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {string} taskId
   * @param {Date} selectedTimestamp
   * @param {Boolean} preventMask
   * @returns {tm.jquery.Promise}
   */
  rescheduleTherapySwitchIvToOralTask: function(taskId, selectedTimestamp, preventMask)
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_MOVE_ADMINISTRATION_HUB;
    viewHubNotifier.actionStarted(hubAction);
    var params = {
      taskId: taskId,
      newTime: JSON.stringify(selectedTimestamp)
    };

    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_RESCHEDULE_IV_TO_ORAL_TASK;

    this._showLoaderMask(preventMask);

    view.loadPostViewData(url, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve()
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject()
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadAuditTrailData: function(therapy, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var auditTrailUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_AUDIT_TRAIL;
    var params = {
      patientId: view.getPatientId(),
      compositionId: therapy.getCompositionUid(),
      ehrOrderName: therapy.getEhrOrderName(),
      patientHeight: view.getPatientHeightInCm(),
      language: view.getViewLanguage()
    };

    this._showLoaderMask(preventMask);
    view.loadViewData(auditTrailUrl, params, null, function(auditTrailData)
        {
          self._hideLoaderMask(preventMask);
          var therapyAuditTrail = app.views.medications.common.dto.TherapyAuditTrail.fromJson(auditTrailData);
          deferred.resolve(therapyAuditTrail);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {string} latestTherapyId
   * @param {app.views.medications.timeline.administration.dto.Administration} administration
   * @param {boolean} markGiven
   * @param {date|null} until
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  setAdministrationTitrationDose: function(latestTherapyId, administration, markGiven, until, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var setTitratedDoseUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_SET_ADMINISTRATION_TITRATION_DOSE;

    var params = {
      patientId: view.getPatientId(),
      latestTherapyId: latestTherapyId,
      taskId: administration.getTaskId(),
      administration: JSON.stringify(administration),
      confirmAdministration: markGiven,
      until: until ? JSON.stringify(until) : null,
      centralCaseId: view.getCentralCaseData() && view.getCentralCaseData().centralCaseId ?
          view.getCentralCaseData().centralCaseId : null,
      careProviderId: view.getCareProviderId()
    };

    this._showLoaderMask(preventMask);
    view.loadPostViewData(setTitratedDoseUrl, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {String} id
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadTherapy: function(id, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var getTherapyUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY;

    var params = {
      patientId: view.getPatientId(),
      therapyId: id
    };

    this._showLoaderMask(preventMask);
    view.loadViewData(getTherapyUrl, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          var therapy = !tm.jquery.Utils.isEmpty(data) ?
              app.views.medications.common.TherapyJsonConverter.convert(data) : null;
          deferred.resolve(therapy);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * Returns the ordering templates, filtered by the current patient, care provider and the passed reference data.
   * @param {String} context {@link app.views.medications.TherapyEnums.therapyTemplateContextEnum}
   * @param {app.views.medications.common.patient.AbstractReferenceData} referenceData
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadTherapyTemplates: function(context, referenceData, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getTemplatesUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_TEMPLATES;

    var params = {
      patientId: view.getPatientId(),
      templateContext: context,
      careProviderId: view.getCareProviderId(),
      referenceWeight: referenceData.getWeight(),
      patientHeight: referenceData.getHeight(),
      birthDate: !!referenceData.getDateOfBirth() ? JSON.stringify(referenceData.getDateOfBirth()) : null
    };

    this._showLoaderMask(preventMask);

    view.loadViewData(getTemplatesUrl, params, null,
        function onSuccess(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(app.views.medications.ordering.dto.TherapyTemplates.fromJson(data));
        },
        function onFailure()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * Loads all the therapy templates.
   * @param {String} templateMode {@link app.views.medications.TherapyEnums.therapyTemplateModeEnum}
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadAllTherapyTemplates: function(templateMode, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getAllTemplatesUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_ALL_THERAPY_TEMPLATES;

    var params = {
      templateMode: templateMode,
      careProviderId: view.getCareProviderId()
    };

    this._showLoaderMask(preventMask);

    view.loadViewData(getAllTemplatesUrl, params, null,
        function onSuccess(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(app.views.medications.ordering.dto.TherapyTemplates.fromJson(data));
        },
        function onFailure()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @returns {tm.jquery.Promise}
   * @param {boolean} [preventMask=false]
   */
  loadUnlicensedMedicationWarning: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var getTherapyUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_UNLICENSED_MEDICATION_WARNING;

    var params = {language: view.getViewLanguage()};

    this._showLoaderMask(preventMask);
    view.loadViewData(getTherapyUrl, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {string} taskId
   * @param {string} doctorsComment
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  setAdministrationDoctorsComment: function(taskId, doctorsComment, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var setAdministrationDoctorsCommentUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_SET_ADMINISTRATION_DOCTORS_COMMENT;

    var params = {
      taskId: taskId,
      doctorsComment: doctorsComment
    };
    this._showLoaderMask(preventMask);
    view.sendPostRequest(setAdministrationDoctorsCommentUrl, params, function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   *
   * @param {Date} selectedTimestamp
   * @param {string} therapyId
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadInfusionBagQuantity: function(selectedTimestamp, therapyId, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var params = {
      patientId: view.getPatientId(),
      when: JSON.stringify(selectedTimestamp),
      therapyId: therapyId
    };
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_REMAINING_INFUSION_BAG_QUANTITY;

    this._showLoaderMask(preventMask);
    view.loadViewData(url, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();

  },

  /**
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadTherapiesForWarnings: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var servletMethodUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPIES_FOR_WARNINGS;

    var params = {
      patientId: view.getPatientId()
    };

    this._showLoaderMask(preventMask);
    view.loadViewData(servletMethodUrl, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          var resultData = tm.jquery.Utils.isArray(data) ?
              data.map(function toObject(jsonConfig)
              {
                return new app.views.medications.warnings.dto.MedicationForWarningsSearch(jsonConfig);
              }) : [];
          deferred.resolve(resultData);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {Array<app.views.medications.common.dto.Therapy>} patientMedsForWarnings
   * @param {{taskName: String}} options
   * @param {boolean} includeActiveTherapies
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedicationWarnings: function(patientMedsForWarnings, options, includeActiveTherapies, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var servletMethodUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_FIND_WARNINGS;

    var patientData = view.getPatientData();
    var referenceData = new app.views.medications.common.patient.ViewBasedReferenceData({view: view});

    var params = {
      patientId: view.getPatientId(),
      dateOfBirth: !!referenceData.getDateOfBirth() ? JSON.stringify(referenceData.getDateOfBirth()) : null,
      patientWeightInKg: referenceData.getWeight(),
      bsaInM2: referenceData.getBodySurfaceArea(),
      gender: JSON.stringify(patientData.getGender()),
      patientDiseases: JSON.stringify(patientData.getDiseases()),
      patientAllergies: JSON.stringify(patientData.getAllergies() || []),
      therapies: JSON.stringify(patientMedsForWarnings),
      includeActiveTherapies: includeActiveTherapies
    };

    if (this._loadMedicationWarningsRequests[options.taskName])
    {
      this._loadMedicationWarningsRequests[options.taskName].abort();
      delete this._loadMedicationWarningsRequests[options.taskName];
    }

    this._showLoaderMask(preventMask);
    this._loadMedicationWarningsRequests[options.taskName] =
        view.loadPostViewData(servletMethodUrl, params, null, function(data)
            {
              self._hideLoaderMask(preventMask);
              deferred.resolve(new app.views.medications.warnings.dto.MedicationsWarnings(
                  {
                    warnings: data.map(app.views.medications.warnings.dto.MedicationsWarning.fromJson)
                  }
              ));
            },
            onFailedOrAbortedHandler,
            null,
            {
              abortHandler: function()
              {
                onFailedOrAbortedHandler();
              }
            });

    return deferred.promise();

    function onFailedOrAbortedHandler()
    {
      self._hideLoaderMask(preventMask);
      deferred.reject();
    }
  },

  /**
   * @param {String} medicationRuleEnum {@see app.views.medications.TherapyEnums.medicationRuleEnum}
   * @param {Object} ruleParameters
   * @param {app.views.medications.common.patient.AbstractReferenceData} referenceData
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  getMedicationRule: function(medicationRuleEnum, ruleParameters, referenceData, preventMask)
  {
    var view = this.getView();
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var servletMethodUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_APPLY_MEDICATION_RULE;

    ruleParameters.medicationRuleEnum = medicationRuleEnum;
    ruleParameters.patientWeight = referenceData.getWeight();
    ruleParameters.patientAgeInYears = referenceData.getAgeInYears();

    var params = {ruleParameters: JSON.stringify(ruleParameters)};

    this._showLoaderMask(preventMask);
    view.loadPostViewData(servletMethodUrl, params, null,
        function(resultJson)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(app.views.medications.warnings.dto.ParacetamolRuleResult.fromJson(resultJson));
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * Loads units holder and returns new instance of {@link UnitsHolder}
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadUnitsHolder: function(preventMask)
  {
    var view = this.getView();
    var self = this;
    var unitsHolderUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_UNITS_HOLDER;
    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(unitsHolderUrl, null, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(app.views.medications.common.dto.UnitsHolder.fromJson(data));
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {String} barcode
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  getMedicationIdForBarcode: function(barcode, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getMedicationIdForBarcodeUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_MEDICATION_ID_FOR_BARCODE;

    var params = {
      barcode: barcode
    };
    this._showLoaderMask(preventMask);
    view.loadViewData(getMedicationIdForBarcodeUrl, params, null, function(medicationId)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(medicationId);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {String} medicationBarcode
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  getAdministrationTaskForBarcode: function(medicationBarcode, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getAdministrationTaskForBarcodeUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_ADMINISTRATION_TASK_FOR_BARCODE;

    var params = {
      patientId: view.getPatientId(),
      medicationBarcode: medicationBarcode
    };
    this._showLoaderMask(preventMask);
    view.loadViewData(getAdministrationTaskForBarcodeUrl, params, null, function(barcodeTaskSearch)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(new app.views.medications.common.dto.BarcodeTaskSearch(barcodeTaskSearch));
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {String} therapyId
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  getOriginalTherapyId: function(therapyId, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getOriginalTherapyIdUrl = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_ORIGINAL_THERAPY_ID;

    var params = {
      patientId: view.getPatientId(),
      therapyId: therapyId
    };
    this._showLoaderMask(preventMask);
    view.loadViewData(getOriginalTherapyIdUrl, params, null,
        function(originalTherapyId)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(originalTherapyId);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} originalTherapy
   * @param {app.views.medications.common.dto.Therapy} changedTherapy
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadFillPharmacistsTherapyEdit: function(originalTherapy, changedTherapy, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var fillPharmacistReviewTherapyOnEdit = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_LOAD_FILL_PHARMACISTS_THERAPY_EDIT;

    var params = {
      originalTherapy: JSON.stringify(originalTherapy),
      changedTherapy: JSON.stringify(changedTherapy)
    };

    this._showLoaderMask(preventMask);

    view.loadPostViewData(fillPharmacistReviewTherapyOnEdit, params, null,
        function(therapyWithDisplayValues)
        {
          self._hideLoaderMask(preventMask);
          therapyWithDisplayValues.therapy.completed = originalTherapy.completed;
          therapyWithDisplayValues.therapy =
              app.views.medications.common.TherapyJsonConverter.convert(therapyWithDisplayValues.therapy);
          if (tm.jquery.Utils.isArray(therapyWithDisplayValues.changes))
          {
            therapyWithDisplayValues.changes =
                therapyWithDisplayValues.changes.map(app.views.medications.common.dto.TherapyChange.fromJson);
          }

          deferred.resolve(therapyWithDisplayValues);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  getReconciliationGroups: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getReconciliationGroupsUrl = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_RECONCILIATION_GROUPS;

    var params = {
      patientId: view.getPatientId(),
      language: view.getViewLanguage()
    };

    this._showLoaderMask(preventMask);

    view.loadViewData(getReconciliationGroupsUrl, params, null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(app.views.medications.common.dto.ReconciliationSummary.fromJson(data));
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadDoseForms: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getDoseFormsUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_DOSE_FORMS;

    this._showLoaderMask(preventMask);
    view.loadViewData(getDoseFormsUrl, null, null,
        function(data)
        {
          var doseForms = data.map(function(doseForm)
          {
            return new app.views.medications.common.dto.DoseForm(doseForm)
          });
          self._hideLoaderMask(preventMask);
          deferred.resolve(doseForms);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadRoutes: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getRoutesUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_ROUTES;

    this._showLoaderMask(preventMask);
    view.loadViewData(getRoutesUrl, null, null,
        function(data)
        {
          var mappedRoutes = data.map(function(route)
          {
            return new app.views.medications.common.dto.MedicationRoute(route);
          });
          self._hideLoaderMask(preventMask);
          deferred.resolve(mappedRoutes);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  startNewReconciliation: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var startNewReconciliationUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_START_NEW_RECONCILIATION;

    var centralCaseData = view.getCentralCaseData();

    var params = {
      patientId: view.getPatientId(),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: view.getCareProviderId()
    };

    this._showLoaderMask(preventMask);
    view.sendPostRequest(startNewReconciliationUrl, params, function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {Boolean} validate
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedicationsOnAdmission: function(validate, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getMedicationOnAdmissionUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_MEDICATIONS_ON_ADMISSION;

    var params = {
      validateTherapy: validate === true,
      patientId: view.getPatientId(),
      language: view.getViewLanguage()
    };

    this._showLoaderMask(preventMask);
    view.loadViewData(
        getMedicationOnAdmissionUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          var items =
              tm.jquery.Utils.isArray(data) ?
                  data.map(app.views.medications.reconciliation.dto.MedicationOnAdmission.fromJson) :
                  [];

          deferred.resolve(items);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedicationsOnDischarge: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getMedicationOnDischargeUrl = view.getViewModuleUrl()
        + app.views.medications.RestApi.SERVLET_PATH_GET_MEDICATIONS_ON_DISCHARGE;

    var params = {
      patientId: view.getPatientId(),
      language: view.getViewLanguage()
    };

    this._showLoaderMask(preventMask);
    view.loadViewData(
        getMedicationOnDischargeUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);

          var items =
              tm.jquery.Utils.isArray(data) ?
                  data.map(app.views.medications.reconciliation.dto.MedicationOnDischarge.fromJson) :
                  [];

          deferred.resolve(items);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {Array<app.views.medications.reconciliation.dto.MedicationOnAdmission>} therapies
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  saveMedicationOnAdmission: function(therapies, preventMask)
  {
    var self = this;
    var view = this.getView();
    var saveMedicationOnAdmissionUrl = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_SAVE_MEDICATIONS_ON_ADMISSION;
    var centralCaseData = view.getCentralCaseData();

    var params = {
      patientId: view.getPatientId(),
      therapies: JSON.stringify(therapies),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: view.getCareProviderId(),
      language: view.getViewLanguage()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadPostViewData(
        saveMedicationOnAdmissionUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {Array<app.views.medications.reconciliation.dto.MedicationOnDischarge>} therapies
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  saveMedicationOnDischarge: function(therapies, preventMask)
  {
    var self = this;
    var view = this.getView();
    var saveMedicationOnDischargeUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_SAVE_MEDICATIONS_ON_DISCHARGE;
    var centralCaseData = view.getCentralCaseData();

    var params = {
      patientId: view.getPatientId(),
      therapies: JSON.stringify(therapies),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: view.getCareProviderId(),
      language: view.getViewLanguage()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadPostViewData(
        saveMedicationOnDischargeUrl,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  reviewMedicationOnAdmission: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var reviewMedicationOnAdmissionUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_REVIEW_MEDICATIONS_ON_ADMISSION;

    var params = {
      patientId: view.getPatientId()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadPostViewData(
        reviewMedicationOnAdmissionUrl,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadTherapyGroupsOnAdmission: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var getAdmissionGroupsUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_GROUPS_ON_ADMISSION;

    var params = {
      patientId: view.getPatientId(),
      language: view.getViewLanguage()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadViewData(
        getAdmissionGroupsUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          if (tm.jquery.Utils.isArray(data))
          {
            data = data.map(app.views.medications.reconciliation.dto.MedicationOnAdmissionGroup.fromJson);
          }
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );

    return deferred.promise();
  },

  /**
   * @param [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadTherapyGroupsOnDischarge: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var getDischargeGroupsUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_GROUPS_ON_DISCHARGE;

    var params = {
      patientId: view.getPatientId(),
      language: view.getViewLanguage()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadViewData(
        getDischargeGroupsUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          if (tm.jquery.Utils.isArray(data))
          {
            data = data.map(app.views.medications.reconciliation.dto.MedicationOnDischargeGroup.fromJson);
          }
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * Returns a new instance of the passed Therapy which has the html display value filled by the server. Persists
   * the {@link app.views.medications.common.dto.Therapy#completed} state since it's UI bound.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  fillTherapyDisplayValues: function(therapy, preventMask)
  {
    var self = this;
    var view = this.getView();
    var fillTherapyDisplayValueUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_FILL_DISPLAY_VALUES;

    var params = {
      therapy: JSON.stringify(therapy)
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadPostViewData(
        fillTherapyDisplayValueUrl,
        params,
        null,
        function(therapyJson)
        {
          therapyJson.completed = therapy.isCompleted();
          deferred.resolve(app.views.medications.common.TherapyJsonConverter.convert(therapyJson));
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {app.views.medications.ordering.dto.TherapyTemplate} template
   * @param {string} templateMode of {@link app.views.medications.TherapyEnums.therapyTemplateModeEnum}
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  saveTemplate: function(template, templateMode, preventMask)
  {
    var view = this.getView();
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var saveUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_SAVE_TEMPLATE;
    var params = {
      template: JSON.stringify(template),
      templateMode: templateMode
    };

    view.loadPostViewData(saveUrl, params, null,
        function(newTemplateId)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(newTemplateId);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * Returns a list of available template custom groups.
   * @param {app.views.medications.TherapyEnums.therapyTemplateModeEnum} templateMode
   * @param [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadTherapyTemplateGroups: function(templateMode, preventMask)
  {
    var view = this.getView();
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var getTherapyTemplateGroupsUrl = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_TEMPLATE_GROUPS;

    var params = {
      templateMode: templateMode
    };

    view.loadViewData(
        getTherapyTemplateGroupsUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * Returns a list of active inpatient therapies for the current patient.
   * @param [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadActiveTherapies: function(preventMask)
  {
    var view = this.getView();
    var self = this;
    var deferred = tm.jquery.Deferred.create();
    var getActiveTherapiesUrl = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_ACTIVE_THERAPIES;

    var params = {
      patientId: view.getPatientId(),
      patientData: JSON.stringify(view.getPatientData())
    };

    view.loadViewData(
        getActiveTherapiesUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          data = tm.jquery.Utils.isArray(data) ? data : [];
          deferred.resolve(data.map(app.views.medications.timeline.TherapyRow.fromJson));
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {Object} searchParams
   * @param {String} therapySortTypeEnum {@Link app.views.medications.TherapyEnums.therapySortTypeEnum}
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadTherapyFlowGridData: function(searchParams, therapySortTypeEnum, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var findTherapyFlowDataUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_LOAD_THERAPY_FLOW_DATA;

    this._showLoaderMask(preventMask);
    var params = {
      patientId: view.getPatientId(),
      centralCaseId: view.getCentralCaseData() && view.getCentralCaseData().centralCaseId ?
          view.getCentralCaseData().centralCaseId : null,
      patientHeight: view.getPatientHeightInCm(),
      startDate: searchParams.searchDate.getTime(),
      dayCount: searchParams.dayCount,
      todayIndex: searchParams.todayIndex !== null ? searchParams.todayIndex : -1,
      roundsInterval: JSON.stringify(view.getRoundsInterval()),
      therapySortTypeEnum: therapySortTypeEnum,
      careProviderId: view.getCareProviderId()
    };
    view.loadViewData(
        findTherapyFlowDataUrl,
        params,
        null,
        function(therapyFlowData)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(therapyFlowData);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {String|undefined} suspendReason
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  suspendAllTherapies: function(suspendReason, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SUSPEND_ALL_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var suspendAllTherapiesUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_SUSPEND_ALL_THERAPIES;
    var params = {patientId: view.getPatientId(), suspendReason: suspendReason};

    this._showLoaderMask(preventMask);

    view.loadPostViewData(
        suspendAllTherapiesUrl,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {String} [stopReason=null]
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  stopAllTherapies: function(stopReason, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var stopAllTherapiesUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_STOP_ALL_THERAPIES;
    var params = {patientId: view.getPatientId(), stopReason: stopReason};

    this._showLoaderMask(preventMask);

    view.loadPostViewData(
        stopAllTherapiesUrl,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  suspendAllTherapiesOnTemporaryLeave: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var suspendAllTherapiesOnTemporaryLeaveUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_SUSPEND_ALL_THERAPIES_TEMPORARY_LEAVE;
    var params = {patientId: view.getPatientId()};

    this._showLoaderMask(preventMask);

    view.loadPostViewData(
        suspendAllTherapiesOnTemporaryLeaveUrl,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {boolean} [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadInformationSources: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var getInformationSourcesUrl = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_INFORMATION_SOURCES;

    this._showLoaderMask(preventMask);

    view.loadViewData(
        getInformationSourcesUrl,
        null,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          var sources = tm.jquery.Utils.isArray(data) ?
              data.map(function toInstance(jsonObject)
              {
                return new app.views.medications.common.dto.InformationSource(jsonObject);
              }) :
              [];
          deferred.resolve(sources);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {String} therapyUuid
   * @param {Boolean} [preventMask=false]
   * @returns {*}
   */
  loadNumericValue: function(therapyUuid, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var getNumericValueUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_NUMERIC_VALUE;
    var params = {value: therapyUuid};

    this._showLoaderMask(preventMask);

    view.loadViewData(
        getNumericValueUrl,
        params,
        null,
        function(numericRepresentation)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(numericRepresentation);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {string} vtmId
   * @param {boolean} [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadVmpMedications: function(vtmId, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var getVmpMedicationsUrl = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_VMP_MEDICATIONS;
    var params = {
      vtmId: vtmId
    };

    this._showLoaderMask(preventMask);

    view.loadViewData(
        getVmpMedicationsUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(
              tm.jquery.Utils.isArray(data) ?
                  data.map(function toFormularyMedication(jsonData)
                  {
                    return new app.views.medications.common.dto.FormularyMedication(jsonData);
                  }) :
                  []);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {boolean} [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadDispenseSources: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var getDispenseSourcesUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_DISPENSE_SOURCES;

    this._showLoaderMask(preventMask);

    view.loadViewData(
        getDispenseSourcesUrl,
        null,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(
              tm.jquery.Utils.isArray(data) ?
                  data.map(function toDispenseSource(jsonData)
                  {
                    return new app.views.medications.common.dto.DispenseSource(jsonData);
                  }) :
                  []);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} originalTherapy
   * @param {app.views.medications.common.dto.Therapy} modifiedTherapy
   * @param {'REQUIRES_CHANGE_REASON'|'REQUIRES_NEW_THERAPY'} group
   * @param {boolean} [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadHasTherapyChanged: function(originalTherapy, modifiedTherapy, group, preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var hasTherapyChangedUrl = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_HAS_THERAPY_CHANGED;
    var params = {
      changeGroup: group,
      therapy: JSON.stringify(originalTherapy),
      changedTherapy: JSON.stringify(modifiedTherapy)
    };

    this._showLoaderMask(preventMask);

    view.loadPostViewData(
        hasTherapyChangedUrl,
        params,
        null,
        function(result)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(result);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {boolean} [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadCurrentTherapiesWarnings: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();
    var findCurrentTherapiesWarningsUrl = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_FIND_CURRENT_THERAPIES_WARNINGS;
    var patientData = view.getPatientData();
    var referenceData = new app.views.medications.common.patient.ViewBasedReferenceData({view: view});

    var params = {
      patientId: view.getPatientId(),
      dateOfBirth: !!referenceData.getDateOfBirth() ? JSON.stringify(referenceData.getDateOfBirth()) : null,
      patientWeightInKg: referenceData.getWeight(),
      patientDiseases: JSON.stringify(patientData.getDiseases()),
      patientAllergies: JSON.stringify(patientData.getAllergies() || []),
      bsaInM2: referenceData.getBodySurfaceArea(),
      gender: JSON.stringify(patientData.getGender())
    };
    this._showLoaderMask(preventMask);

    view.loadPostViewData(
        findCurrentTherapiesWarningsUrl,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(
              new app.views.medications.warnings.dto.MedicationsWarnings(
                  {
                    warnings: data.map(app.views.medications.warnings.dto.MedicationsWarning.fromJson)
                  }
              ));
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.PrescriptionPackage} prescriptionPackage
   * @param {boolean} [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  saveOutpatientPrescription: function(prescriptionPackage, preventMask)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_SAVE_OUTPATIENT_PRESCRIPTION;

    var params = {
      patientId: view.getPatientId(),
      prescriptionPackage: JSON.stringify(prescriptionPackage)
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadPostViewData(
        url,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadTherapyAdministrationTimes: function(therapy, preventMask)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_ADMINISTRATION_TIMES;

    var params = {
      therapy: JSON.stringify(therapy)
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadPostViewData(
        url,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(
              tm.jquery.Utils.isArray(data) ?
                  data.map(app.views.medications.timeline.administration.dto.Administration.fromJson) :
                  []);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadCurrentHospitalizationMentalHealthTherapies: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_CURRENT_HOSPITALIZATION_MENTAL_HEALTH_DRUGS;

    var centralCaseData = view.getCentralCaseData();
    var centralCaseEffectiveData = centralCaseData.centralCaseEffective;
    var lastHospitalizationStart =
        centralCaseData.outpatient === false && !!centralCaseEffectiveData && !!centralCaseEffectiveData.startMillis ?
            new Date(centralCaseEffectiveData.startMillis) :
            CurrentTime.get();

    var params = {
      patientId: view.getPatientId(),
      hospitalizationStart: JSON.stringify(lastHospitalizationStart)
    };
    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadViewData(
        url,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(
              tm.jquery.Utils.isArray(data) ?
                  data.map(function deserializeMentalHealthTherapy(json)
                  {
                    return app.views.medications.mentalHealth.dto.MentalHealthTherapy.fromJson(json, view);
                  }) :
                  []);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {Number} medicationId
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMedicationRoutes: function(medicationId, preventMask)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_MEDICATION_ROUTES;

    var params = {
      medicationId: medicationId
    };
    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadViewData(
        url,
        params,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(
              tm.jquery.Utils.isArray(data) ?
                  data.map(function deserializeMedicationRoute(routeJson)
                  {
                    return new app.views.medications.common.dto.MedicationRoute(routeJson)
                  }) :
                  []);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  loadMentalHealthTemplates: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_MENTAL_HEALTH_TEMPLATES;

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadViewData(
        url,
        null,
        null,
        function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(
              tm.jquery.Utils.isArray(data) ?
                  data.map(function deserializeMentalHealthTemplate(json)
                  {
                    return app.views.medications.mentalHealth.dto.MentalHealthTemplate.fromJson(json, view);
                  }) :
                  []);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        }
    );
    return deferred.promise();
  },

  /**
   * @param {app.views.medications.mentalHealth.dto.MentalHealthDocument} mentalHealthDocument
   * @param {Boolean} [preventMask=false]
   */
  saveMentalHealthDocument: function(mentalHealthDocument, preventMask)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_SAVE_MENTAL_HEALTH_DOCUMENT;

    var params = {
      mentalHealthDocument: JSON.stringify(mentalHealthDocument),
      careProvider: JSON.stringify(view.getCareProvider()),
      language: view.getViewLanguage()
    };
    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadPostViewData(
        url,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  isDischargeCreated: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_DISCHARGE_CREATED;
    var deferred = tm.jquery.Deferred.create();

    var params = {patientId: view.getPatientId()};
    this._showLoaderMask(preventMask);

    view.loadViewData(
        url,
        params,
        null,
        function onLoadSuccess(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function onLoadFailure()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @return {tm.jquery.Promise}
   */
  downloadActiveMedicationAdministrationRecord: function()
  {
    var view = this.getView();
    return this._downloadPdf(
        'mar_active.pdf',
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_ACTIVE_AND_PAST_THERAPIES_REPORT,
        {patientId: view.getPatientId()});

  },

  /**
   * @param {Date} startDate
   * @param {Date} endDate
   * @returns {tm.jquery.Promise}
   */
  downloadMedicationAdministrationHistory: function(startDate, endDate)
  {
    // setting hours on start and end date based on API requirements - should include the full end date
    startDate.setHours(0, 0, 0);
    endDate.setHours(24, 0, 0);

    var view = this.getView();
    return this._downloadPdf(
        'mar_report.pdf',
        view.getViewModuleUrl() + app.views.medications.RestApi.GET_PAST_THERAPIES_REPORT,
        {patientId: view.getPatientId(), startDate: JSON.stringify(startDate), endDate: JSON.stringify(endDate)});
  },

  /**
   * @param {number} numberOfPages
   * @returns {tm.jquery.Promise}
   */
  downloadActiveMedicationAdministrationRecordReportTemplate: function(numberOfPages)
  {
    var view = this.getView();
    return this._downloadPdf(
        'mar_template.pdf',
        view.getViewModuleUrl() + app.views.medications.RestApi.GET_TEMPLATE_REPORT,
        {patientId: view.getPatientId(), numberOfPages: numberOfPages});
  },

  /**
   * @param {string} documentReference
   * @return {tm.jquery.Promise}
   */
  downloadMedicationDocument: function(documentReference)
  {
    return this._downloadPdf(
        'medication_document.pdf',
        this.getView().getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_MEDICATION_DOCUMENT,
        {reference: documentReference});
  },

  /**
   * Also known as the surgery report.
   * @return {tm.jquery.Promise}
   */
  downloadOneDayMedicationAdministrationRecord: function()
  {
    var view = this.getView();
    return this._downloadPdf(
        'mar_one_day.pdf',
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_GET_THERAPY_SURGERY_REPORT,
        {
          patientId: view.getPatientId()
        });
  },


  /**
   * @returns {tm.jquery.Promise}
   * @param {boolean} [preventMask=false]
   */
  loadPatientsCumulativeAntipsychoticPercentage: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var deferred = tm.jquery.Deferred.create();

    var url = view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_GET_PATIENTS_CUMULATIVE_ANTIPSYCHOTIC_PERCENTAGE;
    var params = {patientId: view.getPatientId()};

    this._showLoaderMask(preventMask);

    view.loadViewData(url, params, null, function(data)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(data);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });
    return deferred.promise();
  },

  /**
   * @param {Array<string>} abortedTherapyIds
   * @param {Array<{therapyId: therapyId, warnings: Array<string>}>} overrideWarnings
   * @param {Array<string>} taskIds
   * @param {boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  handleAdditionalWarningsAction: function(abortedTherapyIds, overrideWarnings, taskIds, preventMask)
  {
    var self = this;
    var view = this.getView();
    var viewHubNotifier = view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_HANDLE_ADDITIONAL_WARNINGS_ACTION;
    var url = view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_HANDLE_ADDITIONAL_WARNINGS_ACTION;

    var params = {
      additionalWarningsActionDto: JSON.stringify({
        patientId: view.getPatientId(),
        abortTherapyIds: abortedTherapyIds,
        overrideWarnings: overrideWarnings,
        completeTaskIds: taskIds
      })
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    viewHubNotifier.actionStarted(hubAction);
    view.loadPostViewData(
        url,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {String} [suspendReason=undefined]
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  suspendTherapy: function(therapy, suspendReason, preventMask)
  {
    var self = this;
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_SUSPEND_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var suspendTherapyUrl = this.view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_SUSPEND_THERAPY;

    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: app.views.medications.MedicationUtils.getUidWithoutVersion(therapy.getCompositionUid()),
      ehrOrderName: therapy.getEhrOrderName(),
      suspendReason: suspendReason
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    this.view.loadPostViewData(
        suspendTherapyUrl,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  reloadSingleTherapyAfterAction: function(therapy, preventMask)
  {
    var self = this;
    var view = this.getView();

    var reloadSingleTherapyAfterActionUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_RELOAD_SINGLE_THERAPY_AFTER_ACTION;

    var params = {
      patientId: view.getPatientId(),
      careProviderId: view.getCareProviderId(),
      compositionUid: app.views.medications.MedicationUtils.getUidWithoutVersion(therapy.getCompositionUid()),
      ehrOrderName: therapy.getEhrOrderName(),
      roundsInterval: JSON.stringify(view.getRoundsInterval())
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);

    view.loadViewData(
        reloadSingleTherapyAfterActionUrl,
        params,
        null,
        function(data)
        {
          var therapyReloadAfterAction = app.views.medications.grid.dto.TherapyReloadAfterAction.fromJson(data);
          self._hideLoaderMask(preventMask);
          deferred.resolve(therapyReloadAfterAction);

        },
        function()
        {
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {String} [stopReason=null]
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  abortTherapy: function(therapy, stopReason, preventMask)
  {
    var self = this;
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_ABORT_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var abortTherapyUrl =
        this.view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_ABORT_THERAPY;
    var uidWithoutVersion = app.views.medications.MedicationUtils.getUidWithoutVersion(therapy.getCompositionUid());
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: therapy.getEhrOrderName(),
      stopReason: stopReason
    };
    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    this.view.loadPostViewData(abortTherapyUrl, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  reviewTherapy: function(therapy, preventMask)
  {
    var self = this;
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_REVIEW_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var reviewTherapyUrl = this.view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_REVIEW_THERAPY;
    var uidWithoutVersion = app.views.medications.MedicationUtils.getUidWithoutVersion(therapy.getCompositionUid());
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: therapy.getEhrOrderName()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    this.view.loadPostViewData(reviewTherapyUrl, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  reissueTherapy: function(therapy, preventMask)
  {
    var self = this;
    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_REISSUE_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var reissueTherapyUrl =
        this.view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_REISSUE_THERAPY;

    var ehrCompositionId = therapy.getCompositionUid();
    var uidWithoutVersion = app.views.medications.MedicationUtils.getUidWithoutVersion(ehrCompositionId);
    var params = {
      patientId: this.view.getPatientId(),
      compositionUid: uidWithoutVersion,
      ehrOrderName: therapy.getEhrOrderName()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    this.view.loadPostViewData(reissueTherapyUrl, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  reviewMedicationOnDischarge: function(preventMask)
  {
    var self = this;
    var view = this.getView();
    var reviewMedicationOnDischargeUrl =
        view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_REVIEW_MEDICATIONS_ON_DISCHARGE;

    var params = {
      patientId: view.getPatientId()
    };

    var deferred = tm.jquery.Deferred.create();

    this._showLoaderMask(preventMask);
    view.loadPostViewData(
        reviewMedicationOnDischargeUrl,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * Canceling an administration marks it as completed, not administered with a reason
   * {@link app.views.medications.TherapyEnums.notAdministeredReasonEnum.CANCELLED}. Should only be used for unconfirmed
   * administrations. This action can be reverted via {@link #app.views.medications.RestApi.uncancelAdministration}.
   * @param {String} comment
   * @param {app.views.medications.timeline.administration.dto.Administration} administration
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  cancelAdministration: function(comment, administration, preventMask)
  {
    if (administration.isAdministrationConfirmed())
    {
      throw new Error('Cannot cancel a confirmed administration.');
    }
    var self = this;
    var cancelAdministrationUrl =
        this.view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_CANCEL_ADMINISTRATION;

    var params = {
      patientId: this.view.getPatientId(),
      administration: JSON.stringify(administration),
      comment: comment
    };

    var deferred = tm.jquery.Deferred.create();
    this._showLoaderMask(preventMask);

    this.view.loadPostViewData(
        cancelAdministrationUrl,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * Uncanceling an administration changes it's state to unconfirmed and removes the not administered reason. Should only
   * be used for canceled administrations.
   * {@see #app.views.medications.timeline.administration.dto.Administration.isAdministrationCancelled}
   * @param {app.views.medications.timeline.administration.dto.Administration} administration
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  uncancelAdministration: function(administration, preventMask)
  {
    if (!administration.isAdministrationCancelled())
    {
      throw new Error('Cannot uncancel an administration that is not canceled.');
    }
    var self = this;
    var uncancelAdministrationUrl =
        this.view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_UNCANCEL_ADMINISTRATION;

    var params = {
      patientId: this.view.getPatientId(),
      administration: JSON.stringify(administration)
    };

    var deferred = tm.jquery.Deferred.create();
    this._showLoaderMask(preventMask);

    this.view.loadPostViewData(
        uncancelAdministrationUrl,
        params,
        null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        });

    return deferred.promise();
  },

  /**
   * Deleting an administration reverts the state of the administration to it's previous state:
   *  - a planned confirmed administration reverts to planned unconfirmed administration
   *  - an unplanned administration is removed completely
   * Should only be used for confirmed administrations.
   * {@see #app.views.medications.timeline.administration.dto.Administration.isAdministrationConfirmed}
   * @param {String} comment
   * @param {app.views.medications.timeline.administration.dto.Administration} administration
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  deleteAdministration: function(comment, administration, therapy, preventMask)
  {
    if (!administration.isAdministrationConfirmed())
    {
      throw new Error('Cannot delete an unconfirmed administration.');
    }
    var self = this;

    var deleteAdministrationUrl =
        this.view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_DELETE_ADMINISTRATION;

    var viewHubNotifier = this.view.getHubNotifier();
    var hubAction = tm.views.medications.TherapyView.THERAPY_DELETE_ADMINISTRATION_HUB;
    viewHubNotifier.actionStarted(hubAction);

    var params = {
      patientId: this.view.getPatientId(),
      administration: JSON.stringify(administration),
      therapyId: therapy.getTherapyId(),
      therapyDoseType: therapy.getDoseType(),
      comment: comment
    };

    var deferred = tm.jquery.Deferred.create();
    this._showLoaderMask(preventMask);

    this.view.loadPostViewData(deleteAdministrationUrl, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionEnded(hubAction);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          viewHubNotifier.actionFailed(hubAction);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {String} taskId
   * @param {Boolean} [preventMask=false]
   * @returns {tm.jquery.Promise}
   */
  deleteTask: function(taskId, preventMask)
  {
    var self = this;

    var deleteUrl = this.view.getViewModuleUrl() + app.views.medications.RestApi.SERVLET_PATH_DELETE_TASK;

    var params = {
      patientId: this.view.getPatientId(),
      taskId: taskId
    };

    var deferred = tm.jquery.Deferred.create();
    this._showLoaderMask(preventMask);

    this.view.loadPostViewData(deleteUrl, params, null,
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve();
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {boolean} [newPrescription=false]
   * @param {boolean} [preventMask=false]
   * @return {tm.jquery.Promise}
   */
  loadNextTherapyAdministrationTime: function(therapy, newPrescription, preventMask)
  {
    var url = this.view.getViewModuleUrl() +
        app.views.medications.RestApi.SERVLET_PATH_CALCULATE_NEXT_THERAPY_ADMINISTRATION_TIME;
    var params = {
      patientId: this.view.getPatientId(),
      therapy: JSON.stringify(therapy),
      newPrescription: newPrescription
    };

    var deferred = tm.jquery.Deferred.create();
    var self = this;

    this._showLoaderMask(preventMask);
    this.view.loadPostViewData(
        url,
        params,
        null,
        function(nextTime)
        {
          self._hideLoaderMask(preventMask);
          deferred.resolve(nextTime ? new Date(nextTime) : null);
        },
        function()
        {
          self._hideLoaderMask(preventMask);
          deferred.reject();
        },
        true);

    return deferred.promise();
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * Helper with conditional execution for less code.
   * @param {Boolean} prevent
   * @private
   */
  _showLoaderMask: function(prevent)
  {
    if (!prevent)
    {
      this.getView().showLoaderMask();
    }
  },

  /**
   * Helper with conditional execution for less code.
   * @param {Boolean} prevent
   * @private
   */
  _hideLoaderMask: function(prevent)
  {
    if (!prevent)
    {
      this.getView().hideLoaderMask();
    }
  },

  /**
   * Triggers the download of the given PDF file. When running inside the Swing application, the recieved bytes are
   * forwarded as a Base64 encoded string to the client, which will then create the actual file and open it. When
   * running inside a conventional browser, the bytes are Base64 encoded into a link, which gets clicked by JS, which
   * in turn triggers the actual download of the file by the browser.
   * @param {string} filename
   * @param {string} url
   * @param {object|null|undefined} [params=undefined]
   * @returns {tm.jquery.Promise}
   * @private
   */
  _downloadPdf: function(filename, url, params)
  {
    var view = this.getView();

    if (!view.isSwingApplication())
    {
      var uri =
          tm.jquery.URI.create(url, params ? app.views.medications.RestApi.sanitizedQueryParams(params) : null).toUrl();
      return view.getAppFactory()
          .download({
                data: uri,
                filename: filename,
                mimeType: 'application/pdf'
              },
              app.views.common.AppNotifierDisplayType.HTML);
    }

    var deferred = tm.jquery.Deferred.create();
    this._loadArrayBuffer(url, params)
        .then(
            function onPdfBytesReceived(bytes)
            {
              view.sendAction(
                  tm.views.medications.TherapyView.VIEW_ACTION_CREATE_AND_OPEN_FILE,
                  {
                    filename: filename,
                    content: tm.jquery.Utils.convertByteArrayToBase64(bytes)
                  });
              deferred.resolve();
            },
            function onPdfBytesDownloadFailed()
            {
              view.getAppNotifier().warning(
                  view.getDictionary('no.data.for.report'),
                  app.views.common.AppNotifierDisplayType.HTML,
                  320,
                  150);
              deferred.reject();
            });
    return deferred.promise();
  },

  /**
   * Loads a byte array from the given URL. If no data is returned, the promise will be rejected.
   * @param {string} url
   * @param {object|null|undefined} params
   * @return {tm.jquery.Promise}
   * @private
   */
  _loadArrayBuffer: function(url, params)
  {
    var deferred = tm.jquery.Deferred.create();
    this.getView().sendRequest(
        url,
        'get',
        'arraybuffer',
        app.views.medications.RestApi.sanitizedQueryParams(params),
        null,
        function(bytes)
        {
          if (!bytes || bytes.length === 0)
          {
            deferred.reject();
          }
          deferred.resolve(bytes);
        },
        function()
        {
          deferred.reject();
        },
        app.views.common.AppNotifierDisplayType.NONE);

    return deferred.promise();
  }
});