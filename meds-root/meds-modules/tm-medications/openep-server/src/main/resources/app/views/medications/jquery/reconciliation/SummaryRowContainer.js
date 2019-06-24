Class.define('app.views.medications.reconciliation.SummaryRowContainer', 'tm.jquery.Container', {
  cls: "summary-row",
  scrollable: "visible",

  _columns: null,

  columnCount: 3,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._columns = [];
    this._buildGui();
  },

  ///
  /// private methods
  ///
  _buildGui: function()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "stretch", 0));
    this.setFlex(tm.jquery.flexbox.item.Flex.create(0, 0, "auto"));
    var colWidth = Math.round((100 / this.getColumnCount()) * 100) / 100;
    var totalWidth = 100;

    for (var idx = 0; idx < this.getColumnCount(); idx++)
    {
      var column = new tm.jquery.Container({
        cls: "summary-col",
        scrollable: "visible",
        layout: tm.jquery.VFlexboxLayout.create("flex-start", "stretch", 0),
        flex: tm.jquery.flexbox.item.Flex.create(0, 0, (totalWidth < colWidth ? totalWidth : colWidth) + "%")
      });
      this._columns.push(column);
      this.add(column);
      totalWidth = totalWidth - colWidth;
    }
  },

  /**
   * @return {number}
   */
  getColumnCount: function()
  {
    return this.columnCount;
  },

  /**
   * @param number
   * @return {tm.jquery.Container}
   */
  getColumn: function(number)
  {
    return (number > this._columns.length - 1 || number < 0) ? null : this._columns[number];
  },

  /**
   * @return {tm.jquery.Container}
   */
  getAdmissionColumn: function()
  {
    return this.getColumn(0);
  },

  /**
   * @return {tm.jquery.Container}
   */
  getDischargeColumn: function()
  {
    return this.getColumn(1);
  },

  /**
   * @return {tm.jquery.Container}
   */
  getSummaryColumn: function()
  {
    return this.getColumn(2);
  }
});