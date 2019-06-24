Class.define('app.views.medications.pharmacists.ReviewContainer', 'tm.jquery.Container', {
  statics: {
    EVENT_TYPE_MODE_SWITCH: new tm.jquery.event.EventType({name: 'reviewContainerModeSwitch', delegateName: null}),
    EVENT_TYPE_THERAPY_REMOVED: new tm.jquery.event.EventType({name: 'reviewContainerTherapyRemoved', delegateName: null})
  },
  cls: 'pharmacist-review-container',

  /** @type app.views.medications.pharmacists.dto.PharmacistMedicationReview */
  reviewData: null,
  /** @type boolean */
  editable: true,
  /** @type boolean */
  startInEditMode: false,
  /** @type boolean */
  showSupply: false,
  /** @type app.views.common.AppView */
  view: null,
  /** @type function(app.views.medications.pharmacists.ReviewContainer) */
  deleteReviewEventCallback: null,

  _headerContainer: null,
  _cardContainer: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this.registerEventTypes('app.views.medications.pharmacists.ReviewContainer', [
      { eventType: app.views.medications.pharmacists.ReviewContainer.EVENT_TYPE_MODE_SWITCH }
    ]);
    this.registerEventTypes('app.views.medications.pharmacists.ReviewContainer', [
      { eventType: app.views.medications.pharmacists.ReviewContainer.EVENT_TYPE_THERAPY_REMOVED }
    ]);

    this.reviewData = this.getConfigValue('reviewData', new app.views.medications.pharmacists.dto.PharmacistMedicationReview());

    this._buildGui();
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    var self = this;

    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch'));

    var headerContainer = this.buildHeader(this);
    this.add(headerContainer);
    this._headerContainer = headerContainer;

    if (this.getReviewData().getNoProblem() !== true)
    {

      var cardContainer = new tm.jquery.SimpleCardContainer({
        prerendering: true,
        optimized: true,
        activeIndex: this.startInEditMode === true ? 1 : 0
      });
      cardContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, function ()
      {
        if (!self.isInEditMode()) self.switchMode(true);
      });

      cardContainer.add(this.buildViewContentCard(this));
      cardContainer.add(this.buildEditContentCard(this));

      this.add(cardContainer);

      this._cardContainer = cardContainer;
    }
    else
    {
      this.add(new tm.jquery.Component({
        cls: "TextData no-problem-container",
        html: this.view.getDictionary("everything.is.ok")
      }));
    }
  },

  buildHeader: function (reviewContainer)
  {
    var self = this;

    return new app.views.medications.pharmacists.ReviewContainerHeader({
      reviewContainer: reviewContainer,
      editable: this.editable,
      editMode: this.editable && this.startInEditMode,
      showReminders: true,
      view: this.view,
      confirmReviewEventCallback: function ()
      {
        self._cardContainer.getActiveItem().getContent().validateData(self.view).then(
            function()
            {
              self.onConfirmReview(self._cardContainer.getActiveItem().getContent().getEditorData());
            });
      },
      deleteReviewEventCallback: function ()
      {
        self.onDeleteReview();
      },
      editReviewEventCallback: function ()
      {
        self.onEditReview();
      },
      cancelEditEventCallback: function ()
      {
        self.onCancelEdit();
      }
    });
  },


  buildViewContentCard: function (reviewContainer)
  {
    return new app.views.medications.pharmacists.ReviewContainerViewContentCard({
      initialGuiBuild: this.startInEditMode !== true,
      reviewContainer: reviewContainer,
      showSupply: this.showSupply
    });
  },

  buildEditContentCard: function (reviewContainer)
  {
    return new app.views.medications.pharmacists.ReviewContainerEditContentCard({
      initialGuiBuild: this.startInEditMode === true,
      reviewContainer: reviewContainer,
      showSupply: this.showSupply,
      therapyRemovedCallback: function (id)
      {
        reviewContainer.onTherapyRemoved(id);
      }
    });
  },

  switchMode: function (edit)
  {
    if (this.editable)
    {
      this._cardContainer.setActiveIndex(edit === true ? 1 : 0);
      this.getHeader().setEditMode(edit);
      this.getHeader().refresh();
      this._cardContainer.getActiveItem().getContent().refresh();

      this.fireEvent(new tm.jquery.ComponentEvent({
        eventType: app.views.medications.pharmacists.ReviewContainer.EVENT_TYPE_MODE_SWITCH,
        eventData: {
          switchedToEdit: edit
        }
      }), null);
    }
  },

  /**
   * Returns true while the review is in edit mode.
   * @return {boolean}
   */
  isInEditMode: function ()
  {
    return !tm.jquery.Utils.isEmpty(this._cardContainer) && this._cardContainer.getActiveIndex() === 1;
  },

  /**
   * @return {app.views.common.AppView}
   */
  getView: function ()
  {
    return this.view;
  },

  getReviewData: function ()
  {
    return this.reviewData;
  },

  /* returns a list of active therapies - therapies present on this review. They can differ based on the
   * fact if the review is in edit mode or view mode. */
  getActiveTherapies: function ()
  {
    var data = this.isInEditMode() && !tm.jquery.Utils.isEmpty(this._cardContainer.getActiveItem())
        ? this._cardContainer.getActiveItem().getContent().getEditorData() :
        this.getReviewData();

    return data.getRelatedTherapies();
  },

  setReviewData: function (value)
  {
    this.reviewData = value;
  },

  setEditable: function (editable)
  {
    if (!editable)
    {
      if (!this.getCls().contains("read-only")) this.setCls(this.getCls() + " read-only");
    }
    else
    {
      if (this.getCls().contains("read-only")) this.setCls(this.getCls().replace(" read-only", ""));
    }

    this.editable = editable;
  },

  getHeader: function ()
  {
    return this._headerContainer;
  },

  onConfirmReview: function (reviewData)
  {
    var self = this;

    if (tm.jquery.Utils.isEmpty(reviewData.getComposer())) return; // block confirmation if not a care professional

    if (reviewData.getNoProblem() !== true && reviewData.getRelatedTherapies().length <= 0)
    {
      var warningSystemDialog = this.view.getAppFactory().createWarningSystemDialog(
          this.view.getDictionary("saving.a.review.without.a.therapy.not.possible"));
      warningSystemDialog.setHeight(150);
      warningSystemDialog.setWidth(450);
      warningSystemDialog.setModal(false);
      warningSystemDialog.show();
      return;
    }

    this.view.showLoaderMask();
    var url = this.view.getViewModuleUrl() + tm.views.medications.TherapyView.SERVLET_PATH_SAVE_PHARMACIST_REVIEW;

    var params = {
      patientId: this.view.getPatientId(),
      pharmacistReview: JSON.stringify(reviewData),
      authorize: false,
      language: this.view.getViewLanguage()
    };

    this.view.loadPostViewData(url, params, null, function (compositionUid)
    {
      self.view.hideLoaderMask();
      reviewData.setCompositionUid(compositionUid);
      self.setReviewData(reviewData);
      self.startInEditMode = false; // clear the flag, we don't need it any more - otherwise cancel edit will cause a delete
      self.switchMode(false);
    });
  },

  onCancelEdit: function ()
  {
    if (this.isInEditMode())
    {
      if (this.startInEditMode === true)
      {
        this.onDeleteReview();
      }
      else
      {
        this.switchMode(false);
      }
    }
  },

  onDeleteReview: function ()
  {
    if (!tm.jquery.Utils.isEmpty(this.deleteReviewEventCallback))
    {
      this.deleteReviewEventCallback(this);
    }
  },

  onEditReview: function ()
  {
    if (this.editable) this.switchMode(true);
  },

  onTherapyRemoved: function (therapyId)
  {
    this.fireEvent(new tm.jquery.ComponentEvent({
      eventType: app.views.medications.pharmacists.ReviewContainer.EVENT_TYPE_THERAPY_REMOVED,
      eventData: {
        therapyId: therapyId
      }
    }), null);
  },

  // makes sure the therapy is added to the related therapies only if it's not present already
  addTherapy: function (therapyData)
  {
    if (!this.isInEditMode()) return;

    this._cardContainer.getActiveItem().getContent().addTherapy(therapyData);
  },

  authorize: function ()
  {
    this.getReviewData().markAuthorized();
    this.getHeader().refresh();
  },

  refresh: function ()
  {
    this._cardContainer.getActiveItem().getContent().refresh();
  },

  highlight: function ()
  {
    if (this.isRendered() && this.getHeader().isRendered())
    {
      $(this.getHeader().getDom()).effect("highlight", { color: "#FFFFCC" }, 1000);
    }
    else
    {
      var appFactory = this.view.getAppFactory();
      var self = this;

      appFactory.createConditionTask(
          function ()
          {
            self.highlight();
          },
          function ()
          {
            return self.isRendered() && self.getHeader().isRendered();
          },
          50, 10);
    }
  }
});
