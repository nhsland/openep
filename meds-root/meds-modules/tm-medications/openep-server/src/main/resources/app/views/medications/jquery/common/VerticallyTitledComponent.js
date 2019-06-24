Class.define('app.views.medications.common.VerticallyTitledComponent', 'tm.jquery.Container', {
  cls: 'vertically-titled-component',
  scrollable: 'visible',

  titleText: null,
  contentComponent: null,

  /** privates */
  _titleLabel: null,

  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGUI();
  },

  _buildGUI: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0));
    this._titleLabel = this._createTitleComponent();
    this.add(this._titleLabel);

    if (tm.jquery.Utils.isEmpty(this.contentComponent))
    {
      this.contentComponent = this._createContentContainer();
    }

    this.add(this.contentComponent);
  },

  getContentComponent: function ()
  {
    return this.contentComponent;
  },

  getTitleText: function ()
  {
    return tm.jquery.Utils.isEmpty(this.titleText) ? '' : this.titleText;
  },

  setTitleText: function(text)
  {
    this.titleText = text;
    this._titleLabel.setHtml(tm.jquery.Utils.escapeHtml(text));
  },

  _createTitleComponent: function ()
  {
    return new tm.jquery.Component({
      cls: 'TextLabel title-label ellipsis',
      html: this.getTitleText()
    });
  },

  _createContentContainer: function ()
  {
    return new tm.jquery.Container({
      scrollable: 'visible',
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
  }
});