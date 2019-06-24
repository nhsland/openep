Class.define('app.views.medications.timeline.administration.WitnessContainer', 'tm.jquery.Container', {
  cls: 'administration-witness-container',
  scrollable: 'visible',
  view: null,
  /** @type boolean */
  mandatory: false,

  _authenticatedWitness: null,
  _waitingForWitnessAuthenticationImg: null,
  _witnessAuthenticationSuccessfulImg: null,
  _witnessAuthenticationFailedImg: null,
  _witnessAuthenticationRequestButton: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   *
   * @returns {Object|null}
   */
  getAuthenticatedWitness: function()
  {
    return this._authenticatedWitness;
  },

  /**
   * Sets {@link mandatory} property and sets witness label accordingly
   * @param {Boolean} mandatory
   */
  setMandatory: function(mandatory)
  {
    this.mandatory = mandatory;
    this._witnessLabel.setHtml(this.getView().getDictionary(mandatory ? 'witness.required' : 'witness'))
  },

  /**
   *
   * @param {Object|null} actionData
   */
  setAuthenticatedWitness: function(actionData)
  {
    this._witnessAuthenticationRequestButton.setEnabled(true);
    if (actionData && actionData.witness)
    {
      this._authenticatedWitness = actionData.witness;
      this._witnessAuthenticationSuccessfulImg.show();
      this._witnessAuthenticationFailedImg.hide();
    }
    else
    {
      this._authenticatedWitness = null;
      this._witnessAuthenticationFailedImg.show();
      this._witnessAuthenticationSuccessfulImg.hide();
    }
    this._waitingForWitnessAuthenticationImg.hide();
  },

  /**
   * @returns {tm.jquery.FormField}
   */
  getFormValidations: function()
  {
    var self = this;
    return new tm.jquery.FormField({
      component: this,
      required: false,
      componentValueImplementationFn: function()
      {
        return self.getAuthenticatedWitness();
      },
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: this.getView().getDictionary("authenticate.witness"),
            isValid: function(value)
            {
              return !self.mandatory || !tm.jquery.Utils.isEmpty(value);
            }
          })
        ]
      }
    });
  },

  /**
   *
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 0));

    this._waitingForWitnessAuthenticationImg = new tm.jquery.Container({
      cls: 'loader witness-loader',
      hidden: true
    });

    this._witnessAuthenticationSuccessfulImg = new tm.jquery.Container({
      cls: "CheckLabel",
      width: 16,
      height: 16,
      hidden: true
    });

    this._witnessAuthenticationFailedImg = new tm.jquery.Container({
      cls: "CrossLabel",
      width: 16,
      height: 16,
      hidden: true
    });

    this._witnessAuthenticationRequestButton = new tm.jquery.Button({
      cls: 'btn-bubble',
      width: 200,
      text: "Witness authentication",
      alignSelf: "center",
      handler: this._requestWitnessConfirmation.bind(this)
    });

    this._witnessLabel = new tm.jquery.Container({
      cls: 'TextLabel witness-label',
      html: this.getView().getDictionary(this.mandatory ? 'witness.required' : 'witness'),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    this.add(this._witnessLabel);
    this.add(this._waitingForWitnessAuthenticationImg);
    this.add(this._witnessAuthenticationFailedImg);
    this.add(this._witnessAuthenticationSuccessfulImg);
    this.add(this._witnessAuthenticationRequestButton);
  },

  /**
   * Authenticates the witness and shows authentication result.
   * User beware: No real witness authentication system is implemented. Both solutions return mocked data with given delay.
   * @private
   */
  _requestWitnessConfirmation: function()
  {
    var self = this;
    this._authenticatedWitness = null;
    this._waitingForWitnessAuthenticationImg.show();
    this._witnessAuthenticationFailedImg.hide();
    this._witnessAuthenticationRequestButton.setEnabled(false);
    this._witnessAuthenticationSuccessfulImg.hide();
    if (!this.getView().isAdministrationWitnessingMocked())
    {
      this.getView().sendAction(tm.views.medications.TherapyView.VIEW_ACTION_AUTHENTICATE_ADMINISTRATION_WITNESS, {});
    }
    else
    {
      setTimeout(function()
      {
        self.getView().onViewCommand({
          actionCallback: {
            action: tm.views.medications.TherapyView.VIEW_ACTION_AUTHENTICATE_ADMINISTRATION_WITNESS,
            successful: true,
            actionData: {
              witness: {
                id: 399309352,
                name: "Nurse Andrews"
              }
            }
          }
        })
      }, 2500);
    }
  }
});
