Class.define('app.views.medications.pharmacists.ColumnContainer', 'tm.jquery.Container', {
  /* public members */
  listContainer: null,
  columnTitle: null,

  /* private members */
  _titleComponent: null,

  /** constructor */
  Constructor: function (config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  _buildGui: function ()
  {
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0));

    var header = new tm.jquery.Container({
      cls: "column-title",
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto"),
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "center", 0)
    });
    var titleText = new tm.jquery.Component({
      cls: "PortletHeading1",
      html: tm.jquery.Utils.escapeHtml(this.getColumnTitle()),
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });
    header.add(titleText);

    var listContainer = new tm.jquery.Container({
      layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
      cls: "list-container",
      scrollable: "vertical",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    this.add(header);
    this.add(listContainer);

    this.listContainer = listContainer;
    this._titleComponent = titleText;
  },

  getListContainer: function ()
  {
    return this.listContainer;
  },

  getColumnTitle: function ()
  {
    return this.columnTitle;
  },

  setColumnTitle: function (value)
  {
    this.columnTitle = value;

    if (!tm.jquery.Utils.isEmpty(this._titleComponent))
    {
      this._titleComponent.setHtml(tm.jquery.Utils.escapeHtml(value));
    }
  }
});