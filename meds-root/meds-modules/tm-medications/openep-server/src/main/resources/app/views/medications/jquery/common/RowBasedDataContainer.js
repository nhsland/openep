Class.define('app.views.medications.common.RowBasedDataContainer', 'tm.jquery.Container', {
  componentCls: 'tm-component tm-container row-based-data-container',
  scrollable: 'visible',

  initialRowCount: 1,
  minimumRowCount: 0,
  rowFactory: null,
  rowData: null,

  _rowsContainer: null,
  _addRowButton: null,
  _removeRowButton: null,

  /**
   * A new instance of an extended version of a {@link tm.jquery.Container} which encapsulates the functionality of
   * a simple row based data entry container. The container has a plus and minus sign button in the bottom of the displayed
   * rows, enabling the user to either add or remove additional rows. Each row is created with the use of the
   * {@link #rowFactory} method, so you can configure actual instances of row components to use. When adding a new row, if
   * a parent scrollable element exists and it has visible scrollbars, it will scroll to the position of the last added row
   * automatically.
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    if (!tm.jquery.Utils.isFunction(this.rowFactory))
    {
      throw Error('rowFactory function is not defined');
    }
    this.rowData = tm.jquery.Utils.isArray(this.rowData) ? this.rowData : [];
    this._buildGui();
    this._ensureMinimumNumberOfRowsExist();
  },

  /**
   * @return {function(data: object): tm.jquery.Component|*}
   */
  getRowFactory: function()
  {
    return this.rowFactory;
  },

  /**
   * @return {number}
   * @private
   */
  getRowCount: function()
  {
    return this._rowsContainer.getComponents().length;
  },

  /**
   * @return {tm.jquery.Component|*}
   */
  getRows: function()
  {
    return this._rowsContainer.getComponents();
  },

  /**
   * @return {number} of initial rows that should be created when no data is present.
   */
  getInitialRowCount: function()
  {
    return this.initialRowCount;
  },

  /**
   * @return {number} of minimum number of rows allowed. The component will prevent the removal of any rows
   * when the count is lower than the given number.
   */
  getMinimumRowCount: function()
  {
    return this.minimumRowCount;
  },

  _buildGui: function()
  {
    this._rowsContainer = new tm.jquery.Container({
      scrollable: 'visible',
      cls: 'rows-container',
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    if (this.rowData.length > 0)
    {
      this.rowData
          .forEach(
              function addExistingRow(data)
              {
                this._rowsContainer.add(this.getRowFactory()(data));
              },
              this);
    }

    this._addRowButton = new tm.jquery.Container({cls: 'add-icon add-row-button', width: 30, height: 30});
    this._addRowButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, this._onAddRowButtonClick.bind(this));
    this._removeRowButton = new tm.jquery.Container({cls: 'remove-icon remove-row-button', width: 30, height: 30});
    this._removeRowButton.on(tm.jquery.ComponentEvent.EVENT_TYPE_CLICK, this._onRemoveRowButtonClick.bind(this));

    var buttonRowContainer = new tm.jquery.Container({
      cls: 'rows-control-container',
      layout: tm.jquery.HFlexboxLayout.create('flex-start', 'center', 0),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });
    buttonRowContainer.add(this._addRowButton);
    buttonRowContainer.add(this._removeRowButton);

    this.add(this._rowsContainer);
    this.add(buttonRowContainer);
  },

  /**
   * Adds more empty rows if the existing count is lower than required {@link #initialRowCount}.
   * @private
   */
  _ensureMinimumNumberOfRowsExist: function()
  {
    var numRowsToCreate = this.getInitialRowCount() - this.getRowCount();
    while (numRowsToCreate > 0)
    {
      this._rowsContainer.add(this.getRowFactory()());
      numRowsToCreate--;
    }
  },

  /**
   * Click event handler for {@link #_addRowButton}. Adds a new data row created with {@link #getRowFactory}.
   * @private
   */
  _onAddRowButtonClick: function()
  {
    this._rowsContainer.add(this.getRowFactory()());
    this._rowsContainer.repaint();

    var $scrollParent = $(this.getDom()).scrollParent();
    $scrollParent.animate({
      scrollTop: $scrollParent.get(0).scrollHeight - $scrollParent.get(0).clientHeight
    }, 250);
  },

  /**
   * Click event handler for {@link #_removeRowButton}. Removes the last data row. Since we need the ability to remove all
   * values, the user can basically remove all existing rows.
   * @private
   */
  _onRemoveRowButtonClick: function()
  {
    var numRows = this.getRowCount();

    if (numRows > this.getMinimumRowCount())
    {
      this._rowsContainer.remove(this._rowsContainer.getComponents()[numRows - 1]);
    }
  }
});