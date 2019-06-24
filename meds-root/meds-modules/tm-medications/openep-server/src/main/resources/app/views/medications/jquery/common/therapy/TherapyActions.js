Class.define('app.views.medications.common.therapy.TherapyActions', 'tm.jquery.Object', {
  /** @type app.views.common.AppView */
  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * Marks selected therapy as reviewed. Should only be used if the server property
   * {@link tm.views.medications.TherapyView.doctorReviewEnabled} is set to true.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @returns {tm.jquery.Promise}
   */
  reviewTherapy: function(therapy)
  {
    return this.view.getRestApi().reviewTherapy(therapy);
  },

  /**
   * Stops selected therapy. If required by server property, will prompt the user to enter a reason for stopping the therapy.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @returns {tm.jquery.Promise}
   */
  abortTherapy: function(therapy)
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();

    this._requestStopReasonIfMandatory(therapy)
        .then(
            function(stopReason)
            {
              self.view.getRestApi()
                  .abortTherapy(therapy, stopReason)
                  .then(
                      function()
                      {
                        deferred.resolve();
                      },
                      function()
                      {
                        deferred.reject();
                      });
            },
            function()
            {
              deferred.reject();
            }
        );

    return deferred.promise();
  },

  /**
   * Suspends selected therapy. If required by server property, will prompt the user to enter a reason for suspension.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @returns {tm.jquery.Promise}
   */
  suspendTherapy: function(therapy)
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();

    this._requestSuspendReasonIfMandatory()
        .then(
            function(suspendReason)
            {
              self.view.getRestApi()
                  .suspendTherapy(therapy, suspendReason)
                  .then(
                      function()
                      {
                        deferred.resolve();
                      },
                      function()
                      {
                        deferred.reject();
                      });
            },
            function()
            {
              deferred.reject();
            });

    return deferred.promise();
  },

  /**
   * Reissues a suspended therapy.
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @returns {tm.jquery.Promise}
   */
  reissueTherapy: function(therapy)
  {
    return this.view.getRestApi().reissueTherapy(therapy);
  },

  /**
   * Suspends all currently active therapies. If required by server property, will prompt user to enter a reason for
   * suspension.
   * @returns {tm.jquery.Promise}
   */
  suspendAllTherapies: function()
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();

    this._requestSuspendReasonIfMandatory()
        .then(
            function(suspendReason)
            {
              self.view.getRestApi()
                  .suspendAllTherapies(suspendReason)
                  .then(
                      function()
                      {
                        deferred.resolve();
                      },
                      function()
                      {
                        deferred.reject();
                      });
            },
            function()
            {
              deferred.reject();
            });

    return deferred.promise();
  },

  /**
   * Suspends all currently active therapies. Reason for suspension is predetermined to be "temporary leave".
   * @returns {tm.jquery.Promise}
   */
  suspendAllTherapiesForTemporaryLeave: function()
  {
    return this.view.getRestApi().suspendAllTherapiesOnTemporaryLeave();
  },

  /**
   * Stops all currently active therapies. If required by server property, will prompt user to enter a reason for
   * stopping the therapies.
   * @returns {tm.jquery.Promise}
   */
  stopAllTherapies: function()
  {
    var self = this;
    var deferred = tm.jquery.Deferred.create();

    this._requestStopReasonIfMandatory()
        .then(
            function(stopReason)
            {
              self.view.getRestApi()
                  .stopAllTherapies(stopReason)
                  .then(
                      function()
                      {
                        deferred.resolve();
                      },
                      function()
                      {
                        deferred.reject();
                      });
            },
            function()
            {
              deferred.reject()
            });

    return deferred.promise();
  },

  /**
   * If therapy suspend reason is required by server property, opens therapy change reason entry dialog and resolves the
   * promise with the entered reason.
   * Otherwise resolves the promise with undefined.
   * @returns {tm.jquery.Promise}
   * @private
   */
  _requestSuspendReasonIfMandatory: function()
  {
    var deferred = tm.jquery.Deferred.create();

    if (this.view.isSuspendReasonMandatory())
    {
      this._openTherapyStatusChangeReasonDialog(app.views.medications.TherapyEnums.therapyStatusEnum.SUSPENDED)
          .then(
              function(suspendReason)
              {
                deferred.resolve(suspendReason);
              },
              function()
              {
                deferred.reject();
              }
          );
    }
    else
    {
      deferred.resolve();
    }

    return deferred.promise();
  },

  /**
   * If therapy stop reason is required by server property and change reason is not provided, opens therapy change reason
   * entry dialog and resolves the promise with the entered reason.Otherwise resolves the promise with undefined.
   * @param {app.views.medications.common.dto.Therapy} [therapy=null]
   * @returns {tm.jquery.Promise}
   * @private
   */
  _requestStopReasonIfMandatory: function(therapy)
  {
    var deferred = tm.jquery.Deferred.create();

    if (this.view.isStopReasonMandatory() || (!!therapy && therapy.isLinkedToAdmission()))
    {
      this._openTherapyStatusChangeReasonDialog(app.views.medications.TherapyEnums.therapyStatusEnum.ABORTED)
          .then(
              function(stopReason)
              {
                deferred.resolve(stopReason);
              },
              function()
              {
                deferred.reject();
              }
          );
    }
    else
    {
      deferred.resolve();
    }

    return deferred.promise();
  },

  /**
   * Opens therapy change reason dialog, configured according to selected therapy status. Resolves the promise with the
   * reason, entered by the user.
   * @param {String} selectedTherapyStatus of type {@link app.views.medications.TherapyEnums.therapyStatusEnum}
   * @returns {tm.jquery.Promise}
   * @private
   */
  _openTherapyStatusChangeReasonDialog: function(selectedTherapyStatus)
  {
    var deferred = tm.jquery.Deferred.create();

    app.views.medications.common.TherapyStatusChangeReasonDataEntryContainer.asDialog(
        this.view,
        selectedTherapyStatus,
        function(resultData)
        {
          if (resultData != null && resultData.isSuccess())
          {
            deferred.resolve(resultData.getValue().therapyChangeReason);
          }
          else
          {
            deferred.reject();
          }
        })
        .show();

    return deferred.promise();
  }
});
