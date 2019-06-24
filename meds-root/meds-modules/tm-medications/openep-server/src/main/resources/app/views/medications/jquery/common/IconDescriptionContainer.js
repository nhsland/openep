/**
 * Represents a container that houses an 48px image and description text.
 */
Class.define('app.views.medications.common.IconDescriptionContainer', 'tm.jquery.Container',{
  /** @type app.views.common.AppView */
  view: null,
  /** @type String */
  iconImage: null,
  /** @type String */
  description: null,

  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function()
  {
    var icon = new tm.jquery.Image({
      style: "background-image: url("
          + this.view.getAppFactory().createResourceModuleImageIconPath('/' + this.iconImage) + ");",
      width: 48,
      height: 48
    });

    var descriptionContainer = new tm.jquery.Component({
      cls: "TextDataBold",
      html: this.description,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this.add(icon);
    this.add(descriptionContainer);
  }
});