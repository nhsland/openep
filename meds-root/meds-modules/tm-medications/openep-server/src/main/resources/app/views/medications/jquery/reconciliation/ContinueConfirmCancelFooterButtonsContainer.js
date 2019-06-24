Class.define('app.views.medications.reconciliation.ContinueConfirmCancelFooterButtonsContainer', 'app.views.common.containers.AppFooterButtonsContainer', {
  cls: 'reconciliation-dialog-buttons',
  confirmText: null,
  cancelText: null,
  continueText: null,
  backText: null,

  _confirmButton: null,
  _cancelButton: null,
  _continueButton: null,
  _nextButton: null,

  Constructor: function(config)
  {
    this.callSuper(config);

    this._confirmButton = this._createButton(this.confirmText, true, false);
    this._cancelButton = this._createButton(this.cancelText, true, false, 'link');
    this._continueButton = this._createButton(this.continueText, true, false);

    this.setRightButtons([this._continueButton, this._confirmButton, this._cancelButton]);
    this.rightContainer.setLayout(tm.jquery.HFlexboxLayout.create('flex-end', 'center', 0));
  },

  /**
   * @param {String} text
   * @param {Boolean} [enabled=true]
   * @param {Boolean} [hidden=false]
   * @param {String} [type='default']
   * @return {tm.jquery.Button}
   * @private
   */
  _createButton: function(text, enabled, hidden, type)
  {
    return new tm.jquery.Button({
      margin: '0 5 0 5',
      alignSelf: 'center',
      type: tm.jquery.Utils.isEmpty(type) ? 'default' : type,
      text: text,
      enabled: enabled !== false,
      hidden: hidden === true
    });
  },

  /**
   * @return {tm.jquery.Button}
   */
  getConfirmButton: function()
  {
    return this._confirmButton;
  },

  /**
   * @return {tm.jquery.Button}
   */
  getCancelButton: function()
  {
    return this._cancelButton;
  },

  /**
   * @return {tm.jquery.Button}
   */
  getContinueButton: function()
  {
    return this._continueButton;
  }
});