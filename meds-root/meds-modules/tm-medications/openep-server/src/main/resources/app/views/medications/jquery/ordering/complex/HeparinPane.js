Class.define('app.views.medications.ordering.HeparinPane', 'tm.jquery.Container', {
  cls: "heparin-pane",

  /** configs */
  view: null,
  /** privates */
  /** privates: components */
  heparinButtonGroup: null,
  button0: null,
  button05: null,
  button1: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center", 5));
    this._buildComponents();
    this._buildGui();
  },

  /** private methods */
  _buildComponents: function()
  {
    this.button0 = new tm.jquery.RadioButton({
      cls: "heparin0-button", labelText: "0", data: null, labelAlign: "right", checked: true});
    this.button05 = new tm.jquery.RadioButton({
      cls: "heparin05-button", labelText: "0.5", data: "HEPARIN_05", labelAlign: "right"});
    this.button1 = new tm.jquery.RadioButton({
      cls: "heparin1-button", labelText: "1", data: "HEPARIN_1", labelAlign: "right"});
    this.heparinButtonGroup = new tm.jquery.RadioButtonGroup({});
    this.heparinButtonGroup.add(this.button0);
    this.heparinButtonGroup.add(this.button05);
    this.heparinButtonGroup.add(this.button1);
  },

  _buildGui: function()
  {
    var appFactory = this.view.getAppFactory();

    this.add(appFactory.createHRadioButtonGroupContainer(this.heparinButtonGroup));
    this.add(new tm.jquery.Label({text: "IE/mL", nowrap: true}));
  },

  /** public methods */
  getHeparinValue: function()
  {
    return this.heparinButtonGroup.getActiveRadioButton().data;
  },

  setHeparinValue: function(heparinValue)
  {
    var buttons = this.heparinButtonGroup.getRadioButtons();
    for (var i = 0; i < buttons.length; i++)
    {
      if (buttons[i].data === heparinValue)
      {
        this.heparinButtonGroup.setActiveRadioButton(buttons[i]);
        break;
      }
    }
    return this.heparinButtonGroup.getActiveRadioButton().data;
  },

  clear: function()
  {
    this.heparinButtonGroup.setActiveRadioButton(this.button0);
  }
});
