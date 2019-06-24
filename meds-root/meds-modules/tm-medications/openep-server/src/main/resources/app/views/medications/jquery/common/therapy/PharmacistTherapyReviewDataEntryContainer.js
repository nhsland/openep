Class.define('app.views.medications.common.therapy.PharmacistTherapyReviewDataEntryContainer', 'app.views.common.containers.AppDataEntryContainer', {
  cls: "pharmacist-review-entry-container",

  scrollable: "vertical",
  view: null,
  reviews: null,
  dialog: null,
  therapyActions: null,
  refreshCallbackFunction: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this.reviews = this.getConfigValue("reviews", []);
    this.therapyActions = new app.views.medications.common.therapy.TherapyActions({view: config.view});

    this._buildGUI();
  },

  _buildGUI: function ()
  {
    var self = this;

    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    this.getReviews().forEach(function (reviewData)
    {
      var reviewContainer = new app.views.medications.pharmacists.ReviewContainer({
        view: self.getView(),
        reviewData: reviewData,
        editable: false,
        startInEditMode: false,
        showReminders: false,
        showSupply: self.getView().getMedicationsSupplyPresent(),
        buildHeader: function (reviewContainer)
        {
          return new app.views.medications.common.therapy.PharmacistTherapyReviewDataEntryContainer.ReviewHeader({
            reviewContainer: reviewContainer
          });
        },
        buildViewContentCard: function (reviewContainer)
        {
          return new app.views.medications.pharmacists.ReviewContainerViewContentCard({
            initialGuiBuild: true,
            reviewContainer: reviewContainer,
            showSupply: self.getView().getMedicationsSupplyPresent(),
            /**
             * @param {tm.views.medications.TherapyView} view
             * @param {app.views.medications.pharmacists.dto.PharmacistReviewTherapy|
             * app.views.medications.common.therapy.AbstractTherapyContainerData} data
             * @return {*}
             */
            buildTherapyContainer: function (view, data)
            {
              if (reviewData.isMostRecentReview() && view.getTherapyAuthority().isManageInpatientPrescriptionsAllowed())
              {
                var therapyContainer = app.views.medications.pharmacists.TherapyContainer.forFlowViewReviewPane({
                  flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
                  view: view,
                  data: data,
                  changeType: data.getChangeType()
                }, true);
                therapyContainer.getToolbar().setAcceptChangeEventCallback(function (container)
                {
                  self.onConfirmChanges(reviewContainer, container);
                });
                therapyContainer.getToolbar().setDenyChangeEventCallback(function (container)
                {
                  self.onDenyChanges(reviewContainer, container);
                });
                therapyContainer.getToolbar().setEditTherapyEventCallback(function (container)
                {
                  self.onEditTherapy(reviewContainer, container);
                });
                if (data.getChangeType() === "SUSPEND")
                {
                  therapyContainer.getToolbar().setReissueTherapyEventCallback(function (container)
                  {
                    self.onReissueTherapy(reviewContainer, container);
                  });
                  therapyContainer.getToolbar().setAbortSuspendedTherapyEventCallback(function (container)
                  {
                    self.onAbortTherapy(reviewContainer, container);
                  });
                }
                else if (data.getChangeType() === "ABORT")
                {
                  therapyContainer.getToolbar().setCopyTherapyEventCallback(function (container)
                  {
                    self.onCopyTherapy(reviewContainer, container);
                  });
                }
                return therapyContainer;
              }
              else
              {
                return app.views.medications.pharmacists.TherapyContainer.forFlowViewReviewPane(
                    {
                      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
                      view: view,
                      data: data
                    },
                    false);
              }
            }
          });
        }
      });
      self.add(reviewContainer);
    });
  },

  _therapyEditDialogResultCallback: function (result, editorContainer, therapyContainer)
  {
    this._loadTherapyDisplayData(result, function (displayValue)
    {
      var dialogResultData = new app.views.common.AppResultData({success: true});
      editorContainer._resultCallback(dialogResultData);

      // remove empty properties, so that they don't get overwritten - like compositionUid, which is our PK
      for (var propName in displayValue)
      {
        if (tm.jquery.Utils.isEmpty(displayValue[propName]))
        {
          delete displayValue[propName];
        }
      }

      /* extend over the original, keeping whatever is missing */
      therapyContainer.getData().therapy = jQuery().extend(true, therapyContainer.getData().therapy, displayValue);
      therapyContainer.getData().changeType = app.views.medications.TherapyEnums.pharmacistTherapyChangeType.EDIT;
      therapyContainer.refresh();
    });
  },

  _confirmOrDenyChanges: function (reviewContainer, therapyContainer, accepted)
  {
    var self = this;
    var url = self.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_REVIEW_PHARMACIST_REVIEW;

    var reviewAction = accepted ? app.views.medications.TherapyEnums.reviewPharmacistReviewAction.ACCEPTED :
        app.views.medications.TherapyEnums.reviewPharmacistReviewAction.DENIED;

    var pharmacistReviewUid = reviewContainer.getReviewData().getCompositionUid();
    var otherReviewIds = self.getAllReviewIdsButThis(pharmacistReviewUid);

    var params = {
      patientId: self.getView().getPatientId(),
      pharmacistReviewUid: pharmacistReviewUid,
      reviewAction: reviewAction,
      reviewIdsToDeny: otherReviewIds != null ? JSON.stringify(otherReviewIds) : null,
      language: self.getView().getViewLanguage()
    };

    this.getView().sendPostRequest(url, params, function ()
    {
      self.getView().hideLoaderMask();
      reviewContainer.getReviewData().markReviewed();
      reviewContainer.getHeader().refresh();
      accepted === true ? therapyContainer.getToolbar().markAccepted() : therapyContainer.getToolbar().markDenied();
      self.refreshCallbackFunction();
      self.dialog.hide();
    }, function ()
    {
      self.getView().hideLoaderMask();
      therapyContainer.getToolbar().clearMark();
    }, app.views.common.AppNotifierDisplayType.HTML);
  },

  containsUnreviewedReviews: function ()
  {
    var reviewContainers = this.getComponents();
    for (var idx = 0; idx < reviewContainers.length; idx++)
    {
      if (reviewContainers[idx] instanceof app.views.medications.pharmacists.ReviewContainer
          && !reviewContainers[idx].getReviewData().isReviewed())
      {
        return true;
      }
    }
    return false;
  },

  getAllReviewIdsButThis: function (reviewId)
  {
    var reviewContainers = this.getComponents();
    if (reviewContainers.size() === 1)
    {
      return null
    }
    var ids = [];
    for (var idx = 0; idx < reviewContainers.length; idx++)
    {
      if (reviewContainers[idx] instanceof app.views.medications.pharmacists.ReviewContainer
          && reviewContainers[idx].getReviewData().getCompositionUid() !== reviewId)
      {
        ids.push(reviewContainers[idx].getReviewData().getCompositionUid());
      }
    }
    return ids;
  },

  onEditTherapy: function (reviewContainer, therapyContainer)
  {
    var self = this;
    var view = this.getView();
    var therapyDayDto = therapyContainer.getData();
    var therapy = therapyDayDto.therapy;

    view.showEditTherapyDialog(therapy, false, therapyDayDto.modified, function (result, editContainer, prescriber)
    {
      self._saveEditedTherapy(therapy, result, editContainer, prescriber, therapyContainer, reviewContainer)
    });
  },

  _saveEditedTherapy: function (originalTherapy, changedTherapy, editContainer, prescriber, therapyContainer, reviewContainer)
  {
    var self = this;
    var view = this.getView();
    var centralCaseData = view.getCentralCaseData(); //TherapyCentralCaseData
    var url = this.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_REVIEW_PHARMACIST_REVIEW;

    changedTherapy.compositionUid = originalTherapy.compositionUid;
    changedTherapy.ehrOrderName = originalTherapy.ehrOrderName;

    var pharmacistReviewUid = reviewContainer.getReviewData().getCompositionUid();
    var otherReviewIds = self.getAllReviewIdsButThis(pharmacistReviewUid);

    var params = {
      patientId: view.getPatientId(),
      pharmacistReviewUid: pharmacistReviewUid,
      reviewAction: app.views.medications.TherapyEnums.reviewPharmacistReviewAction.MODIFIED,
      modifiedTherapy: JSON.stringify(changedTherapy),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: this.view.getCareProviderId(),
      prescriber: JSON.stringify(prescriber),
      reviewIdsToDeny: otherReviewIds != null ? JSON.stringify(otherReviewIds) : null,
      saveDateTime: null
    };

    view.sendPostRequest(
        url,
        params,
        function ()
        {
          view.hideLoaderMask();
          reviewContainer.getReviewData().markReviewed();
          therapyContainer.getToolbar().markEdited();
          self.refreshCallbackFunction();
          self.dialog.hide();
        },
        function ()
        {
          view.hideLoaderMask();
          therapyContainer.getToolbar().clearMark();
        },
        app.views.common.AppNotifierDisplayType.HTML);
  },

  onReissueTherapy: function (reviewContainer, therapyContainer)
  {
    var self = this;
    var therapyDayDto = therapyContainer.getData();
    var therapy = therapyDayDto.therapy;
    self._reissueTherapy(therapy, therapyContainer, reviewContainer);
  },

  _reissueTherapy: function (therapy, therapyContainer, reviewContainer)
  {
    var self = this;
    var view = this.getView();
    var centralCaseData = view.getCentralCaseData(); //TherapyCentralCaseData
    var url = this.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_REVIEW_PHARMACIST_REVIEW;

    var pharmacistReviewUid = reviewContainer.getReviewData().getCompositionUid();
    var reviewIds = self.getAllReviewIdsButThis(pharmacistReviewUid);

    var params = {
      patientId: view.getPatientId(),
      pharmacistReviewUid: pharmacistReviewUid,
      reviewAction: app.views.medications.TherapyEnums.reviewPharmacistReviewAction.REISSUED,
      modifiedTherapy: JSON.stringify(therapy),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: this.view.getCareProviderId(),
      reviewIdsToDeny: reviewIds != null ? JSON.stringify(reviewIds) : null,
      saveDateTime: null
    };

    this.getView().sendPostRequest(url, params, function ()
    {
      self.getView().hideLoaderMask();
      reviewContainer.getReviewData().markReviewed();
      reviewContainer.getHeader().refresh();
      therapyContainer.getToolbar().markReissued();
      self.refreshCallbackFunction();
      self.dialog.hide();
    }, function ()
    {
      self.getView().hideLoaderMask();
      therapyContainer.getToolbar().clearMark();
    }, app.views.common.AppNotifierDisplayType.HTML);
  },

  onCopyTherapy: function (reviewContainer, therapyContainer)
  {
    var self = this;
    var view = this.getView();
    var therapyDayDto = therapyContainer.getData();
    var therapy = therapyDayDto.therapy;

    view.showEditTherapyDialog(therapy, true, therapyDayDto.modified, function (result, editContainer, prescriber)
    {
      self._copyTherapy(result, editContainer, prescriber, therapyContainer, reviewContainer);
    });
  },

  _copyTherapy: function (newTherapy, editContainer, prescriber, therapyContainer, reviewContainer)
  {
    var self = this;
    var view = this.getView();
    var centralCaseData = view.getCentralCaseData();
    var url = this.getView().getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_REVIEW_PHARMACIST_REVIEW;

    var pharmacistReviewUid = reviewContainer.getReviewData().getCompositionUid();
    var reviewIds = self.getAllReviewIdsButThis(pharmacistReviewUid);

    var params = {
      patientId: view.getPatientId(),
      pharmacistReviewUid: pharmacistReviewUid,
      reviewAction: app.views.medications.TherapyEnums.reviewPharmacistReviewAction.COPIED,
      modifiedTherapy: JSON.stringify(newTherapy),
      centralCaseId: centralCaseData ? centralCaseData.centralCaseId : null,
      careProviderId: this.view.getCareProviderId(),
      prescriber: JSON.stringify(prescriber),
      reviewIdsToDeny: reviewIds != null ? JSON.stringify(reviewIds) : null,
      saveDateTime: null
    };

    view.sendPostRequest(
        url,
        params,
        function ()
        {
          view.hideLoaderMask();
          reviewContainer.getReviewData().markReviewed();
          therapyContainer.getToolbar().markCopied();
          self.refreshCallbackFunction();
          self.dialog.hide();
        },
        function ()
        {
          view.hideLoaderMask();
          therapyContainer.getToolbar().clearMark();
        },
        app.views.common.AppNotifierDisplayType.HTML);

  },

  onAbortTherapy: function (reviewContainer, therapyContainer)
  {
    var self = this;
    var therapyDayDto = therapyContainer.getData();
    var therapy = therapyDayDto.therapy;
    self._abortTherapy(therapy, therapyContainer, reviewContainer);
  },

  _abortTherapy: function (therapy, therapyContainer, reviewContainer)
  {
    var self = this;
    var view = this.getView();
    var url = view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_REVIEW_PHARMACIST_REVIEW;

    var pharmacistReviewUid = reviewContainer.getReviewData().getCompositionUid();
    var reviewIds = self.getAllReviewIdsButThis(pharmacistReviewUid);

    var params = {
      patientId: self.getView().getPatientId(),
      pharmacistReviewUid: pharmacistReviewUid,
      reviewAction: app.views.medications.TherapyEnums.reviewPharmacistReviewAction.ABORTED,
      modifiedTherapy: JSON.stringify(therapy),
      reviewIdsToDeny: reviewIds != null ? JSON.stringify(reviewIds) : null,
      language: self.getView().getViewLanguage()
    };

    view.sendPostRequest(url, params, function ()
    {
      self.getView().hideLoaderMask();
      reviewContainer.getReviewData().markReviewed();
      reviewContainer.getHeader().refresh();
      therapyContainer.getToolbar().markAborted();

      self.refreshCallbackFunction();
      self.dialog.hide();
    }, function ()
    {
      self.getView().hideLoaderMask();
      therapyContainer.getToolbar().clearMark();
    }, app.views.common.AppNotifierDisplayType.HTML);
  },

  onConfirmChanges: function (reviewContainer, therapyContainer)
  {
    this._confirmOrDenyChanges(reviewContainer, therapyContainer, true);
  },

  onDenyChanges: function (reviewContainer, therapyContainer)
  {
    this._confirmOrDenyChanges(reviewContainer, therapyContainer, false);
  },

  getReviews: function ()
  {
    return this.reviews;
  },

  getView: function ()
  {
    return this.view;
  },

  setDialog: function (value)
  {
    this.dialog = value;
  }
});
Class.define('app.views.medications.common.therapy.PharmacistTherapyReviewDataEntryContainer.ReviewHeader', 'tm.jquery.Container', {
  cls: 'header-container',

  reviewContainer: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui(false);
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    var view = this.getReviewContainer().getView();
    var appFactory = view.getAppFactory();
    var data = this.getReviewContainer().getReviewData();
    var statusEnum = app.views.medications.TherapyEnums.pharmacistReviewStatusEnum;
    var statusCls = data.getPharmacistReviewStatus() === statusEnum.REVIEWED ? 'status-icon done' : 'status-icon inprogress';

    var layerContainerOptions = {
      background: {
        cls: 'document-icon'
      },
      layers: []
    };
    layerContainerOptions.layers.push({
      hpos: 'right', vpos: 'bottom', cls: statusCls,
      title: data.getPharmacistReviewStatus() === statusEnum.REVIEWED ?
          view.getDictionary('Status.DONE') : view.getDictionary('Status.IN_PROGRESS')
    });

    var statusIcon = new tm.jquery.Image({
      html: appFactory.createLayersContainerHtml(layerContainerOptions),
      width: 48,
      height: 48
    });

    var titleContainer = new tm.jquery.Container({
      cls: 'title-container',
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0),
      html: '<div class="PortletHeading1">' + view.getDictionary('pharmacists.review') + '</div>' +
      '<div class="TextData">' + view.getDisplayableValue(new Date(data.getCreateTimestamp()), 'short.date.time')
      + (tm.jquery.Utils.isEmpty(data.getComposer()) ? '' : ', '.concat(data.getComposer().name)) + '</div>',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, 'auto'),
      alignSelf: 'stretch'
    });

    this.setLayout(tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0));
    this.add(statusIcon);
    this.add(titleContainer);

  },

  getReviewContainer: function()
  {
    return this.reviewContainer;
  },

  refresh: function()
  {
    if (this.isRendered())
    {
      this.removeAll();
      this._buildGui();
      this.repaint();
    }
  }
});