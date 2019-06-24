/**
 * In charge of transforming the list of active prescriptions into grouped data for our sub views. Can either return the
 * given array of prescriptions as an array of sorted groups (the actual groups are based on the requested grouping mode)
 * and their members, by calling{@link app.views.medications.common.overview.TherapyRowDataGrouper#group}, or provide group
 * membership information for a specific prescription (used by our grid plugin) by calling
 * {@link app.views.medications.common.overview.TherapyRowDataGrouper#createHtmlGroupKeys}.
 */
Class.define('app.views.medications.common.overview.TherapyRowDataGrouper', 'tm.jquery.Object', {
  /** @type app.views.common.AppView */
  view: null,

  /** @type {Object.<string, function(Array<app.views.medications.common.overview.TherapyRowGroupData)>} */
  _groupByStrategyMap: null,
  /** @type Object.<string, string> */
  _prescriptionGroupTitles: null,
  /** @type Array<string> */
  _prescriptionGroupSortOrder: [
    app.views.medications.TherapyEnums.prescriptionGroupEnum.ANTIMICROBIALS,
    app.views.medications.TherapyEnums.prescriptionGroupEnum.ANTICOAGULANTS,
    app.views.medications.TherapyEnums.prescriptionGroupEnum.INSULINS,
    app.views.medications.TherapyEnums.prescriptionGroupEnum.FLUIDS,
    app.views.medications.TherapyEnums.prescriptionGroupEnum.BLOOD_PRODUCTS,
    app.views.medications.TherapyEnums.prescriptionGroupEnum.MEDICINAL_GASES,
    app.views.medications.TherapyEnums.prescriptionGroupEnum.STAT_DOSES,
    app.views.medications.TherapyEnums.prescriptionGroupEnum.REGULAR,
    app.views.medications.TherapyEnums.prescriptionGroupEnum.PRN
  ],

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);

    this._groupByStrategyMap = {};
    this._groupByStrategyMap['atcGroup'] = this._groupByAtc;
    this._groupByStrategyMap['routes'] = this._groupByRoute;
    this._groupByStrategyMap['customGroup'] = this._groupByCustomGroup;
    this._groupByStrategyMap['prescriptionGroup'] = this._groupByPrescriptionGroup;

    this._prescriptionGroupTitles = this._createPrescriptionGroupTitleMap();
  },

  /**
   * Transforms the given active prescription data into sorted groups, based on the desired 'group by' type.
   * @param {string} type
   * @param {Array<app.views.medications.timeline.TherapyRow|app.views.medications.grid.dto.TherapyFlowRow>} rows
   * @return {Array<app.views.medications.common.overview.TherapyRowGroupData>}
   */
  group: function(type, rows)
  {
    if (!this._groupByStrategyMap.hasOwnProperty(type))
    {
      throw new Error('Unsupported grouping mode.');
    }
    return this._groupByStrategyMap[type].call(this, rows);
  },

  /**
   * Returns the group data instance from the given group array, or null if it's not found.
   * @param {Array<app.views.medications.common.overview.TherapyRowGroupData>} groups
   * @param {string} key
   * @return {app.views.medications.common.overview.TherapyRowGroupData|null}
   */
  findGroupByKey: function(groups, key)
  {
    for (var i = 0; i < groups.length; i++)
    {
      if (groups[i].getKey() === key)
      {
        return groups[i]
      }
    }
    return null;
  },

  /**
   * Needed by our grid plugin, as it implements it's own grouping and sorting mechanism.
   * @param {app.views.medications.grid.dto.TherapyFlowRow} row
   * @return {{routes: string, customGroup: string, prescriptionGroup: string, atcGroup: string}}
   */
  createHtmlGroupKeys: function(row)
  {
    return {
      atcGroup: this._createGroupDataFromRowAtcValues(row).getKeyDisplayValue(),
      routes: this._createGroupDataFromRowRouteNames(row.getRoutes()).getKeyDisplayValue(),
      customGroup: this._createGroupDataFromRowCustomGroupValue(row).getKeyDisplayValue(),
      prescriptionGroup: this._createGroupDataFromRowPrescriptionGroupValue(row).getKeyDisplayValue()
    }
  },

  /**
   * Creates a translation map of {@link app.views.medications.TherapyEnums.prescriptionGroupEnum} to dictionary
   * values.
   * @return {Object.<string, string>}
   * @private
   */
  _createPrescriptionGroupTitleMap: function()
  {
    var nameMap = {};

    for (var groupEnum in app.views.medications.TherapyEnums.prescriptionGroupEnum)
    {
      if (!app.views.medications.TherapyEnums.prescriptionGroupEnum.hasOwnProperty(groupEnum))
      {
        continue;
      }
      nameMap[groupEnum] = this.view.getDictionary('PrescriptionGroupEnum.' + groupEnum);
    }

    return nameMap;
  },

  /**
   * @param {app.views.medications.timeline.TherapyRow|app.views.medications.grid.dto.TherapyFlowRow} row
   * @return {app.views.medications.common.overview.TherapyRowGroupData}
   */
  _createGroupDataFromRowAtcValues: function(row)
  {
    var atc = row.getAtcGroupName() ?
        row.getAtcGroupName() + ' (' + row.getAtcGroupCode() + ')' :
        this.view.getDictionary("without.atc");
    return new app.views.medications.common.overview.TherapyRowGroupData({key: atc});
  },

  /**
   * The purpose of this method is to support the row data format of the grid view, which differs from the grouping
   * logic perspective in how the route names are provided. If only one route is present on therapy, it's name is used for
   * grouping. Therapies with multiple routes are grouped together, as are therapies with no routes (medicinal gas therapies)
   *
   * @param {Array<string>} routeNames
   * @return {app.views.medications.common.overview.TherapyRowGroupData}
   */
  _createGroupDataFromRowRouteNames: function(routeNames)
  {
    if (!tm.jquery.Utils.isArray(routeNames))
    {
      throw new Error('routeNames not an array');
    }
    if (routeNames.length === 0)
    {
      return new app.views.medications.common.overview.TherapyRowGroupData({key: this._getOtherRoutesGroupKey()});
    }

    var route = routeNames.length === 1 ? routeNames[0] : this._getMultipleRoutesGroupKey();
    return new app.views.medications.common.overview.TherapyRowGroupData({key: route});
  },

  /**
   * @param {app.views.medications.timeline.TherapyRow|app.views.medications.grid.dto.TherapyFlowRow} row
   * @return {app.views.medications.common.overview.TherapyRowCustomGroupData}
   */
  _createGroupDataFromRowCustomGroupValue: function(row)
  {
    var groupKey = row.getCustomGroup() || this.view.getDictionary("other.undef");
    var sortOrder = row.getCustomGroupSortOrder() ?
        app.views.medications.MedicationUtils.padDigits(row.getCustomGroupSortOrder(), 6) :
        999999;

    return new app.views.medications.common.overview.TherapyRowCustomGroupData({
      key: groupKey,
      sortOrder: sortOrder
    });
  },

  /**
   * @param {app.views.medications.timeline.TherapyRow|app.views.medications.grid.dto.TherapyFlowRow} row
   * @return {app.views.medications.common.overview.TherapyRowCustomGroupData}
   */
  _createGroupDataFromRowPrescriptionGroupValue: function(row)
  {
    return new app.views.medications.common.overview.TherapyRowGroupData({
      sortOrder: this._prescriptionGroupSortOrder.indexOf(row.getPrescriptionGroup()),
      key: this._prescriptionGroupTitles[row.getPrescriptionGroup()]
    });
  },

  /**
   * @return {string}
   * @private
   */
  _getMultipleRoutesGroupKey: function()
  {
    return this.view.getDictionary("multiple.routes");
  },

  /**
   * @returns {string}
   * @private
   */
  _getOtherRoutesGroupKey: function()
  {
    return this.view.getDictionary('other.undef')
  },

  /**
   * @param {{therapy: app.views.medications.common.dto.Therapy}} therapyRow
   * @return {app.views.medications.common.overview.TherapyRowGroupData}
   * @private
   */
  _createGroupDataFromRowRouteValue: function(therapyRow)
  {
    var therapyDto = therapyRow.therapy;
    var routes = therapyDto.getRoutes();
    return this._createGroupDataFromRowRouteNames(
        routes.map(
            function toName(route)
            {
              return route.name
            }));
  },

  /**
   * @param {app.views.medications.common.overview.TherapyRowGroupData} group1
   * @param {app.views.medications.common.overview.TherapyRowGroupData} group2
   * @return {number}
   * @private
   */
  _compareBySortOrder: function(group1, group2)
  {
    var key1 = group1.getSortOrderValue();
    var key2 = group2.getSortOrderValue();
    if (key1 < key2)
    {
      return -1;
    }
    if (key1 > key2)
    {
      return 1;
    }
    return 0;
  },

  /**
   * Group the given data, using the provided method to construct {app.views.medications.common.overview.TherapyRowGroupData}
   * for each row. Returns an array of unique groups.
   * @param {Array<app.views.medications.timeline.TherapyRow|app.views.medications.grid.dto.TherapyFlowRow>} rows
   * @param {function} createDataFn Should return an instance of {app.views.medications.common.overview.TherapyRowGroupData}.
   * @return {Array}
   * @private
   */
  _groupByData: function(rows, createDataFn)
  {
    var groups = [];
    for (var rowIndex = 0; rowIndex < rows.length; rowIndex++)
    {
      var currentTherapyRow = rows[rowIndex];
      var currentRowGroupData = createDataFn.call(this, currentTherapyRow);

      this._findOrAddByKey(groups, currentRowGroupData)
          .addTherapyRowData(currentTherapyRow);
    }

    return groups;
  },

  /**
   * @param {Array<app.views.medications.timeline.TherapyRow|app.views.medications.grid.dto.TherapyFlowRow>} rows
   * @return {Array<app.views.medications.common.overview.TherapyRowGroupData>}
   * @private
   */
  _groupByRoute: function(rows)
  {
    return this
        ._groupByData(rows, this._createGroupDataFromRowRouteValue)
        .sort(this._compareBySortOrder);
  },

  /**
   * @param {Array<app.views.medications.timeline.TherapyRow|app.views.medications.grid.dto.TherapyFlowRow>} rows
   * @return {Array<app.views.medications.common.overview.TherapyRowGroupData>}
   * @private
   */
  _groupByAtc: function(rows)
  {
    return this
        ._groupByData(rows, this._createGroupDataFromRowAtcValues)
        .sort(this._compareBySortOrder);
  },

  /**
   * @param {Array<app.views.medications.timeline.TherapyRow|app.views.medications.grid.dto.TherapyFlowRow>} rows
   * @return {Array<app.views.medications.common.overview.TherapyRowCustomGroupData>}
   * @private
   */
  _groupByCustomGroup: function(rows)
  {
    return this
        ._groupByData(rows, this._createGroupDataFromRowCustomGroupValue)
        .sort(this._compareBySortOrder);
  },

  /**
   * @param {Array<app.views.medications.timeline.TherapyRow|app.views.medications.grid.dto.TherapyFlowRow>} rows
   * @return {Array<app.views.medications.common.overview.TherapyRowCustomGroupData>}
   * @private
   */
  _groupByPrescriptionGroup: function(rows)
  {
    return this
        ._groupByData(rows, this._createGroupDataFromRowPrescriptionGroupValue)
        .sort(this._compareBySortOrder);
  },

  /**
   * Finds the group by it's key in the array and returns the existing instance in the array or pushes the group
   * into the array and returns it.
   *
   * @param {Array<app.views.medications.common.overview.TherapyRowGroupData>} groups
   * @param {app.views.medications.common.overview.TherapyRowGroupData} group
   * @return {app.views.medications.common.overview.TherapyRowGroupData}
   * @private
   */
  _findOrAddByKey: function(groups, group)
  {
    return this.findGroupByKey(groups, group.getKey()) || groups[groups.push(group) - 1];
  }
});
