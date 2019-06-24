Class.define('app.views.medications.common.ProtocolSummaryContainer', 'tm.jquery.Container', {
  cls: 'protocol-summary-container',
  scrollable: 'both',
  /** configs */
  view: null,
  timedDoseElements: null,
  unit: null,
  lineAcross: false,

  /**
   * A component to visualize various types of the variable days dosing. If {@link lineAcross} is set to true the
   * presented values will have a 'strikethrough formatting' effect, intended to represent old or discarded values,
   * as is the case when comparing old dosing to new dosing.
   * @param config
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);
    this._buildGui();
  },

  /**
   * Builds the contents based on the type of dosing information available. At some point this should be refactored and
   * broken into two different implementations as the logic for descriptive time is bound to the discharge protocol,
   * which can only be found on the reconciliation subview.
   * @private
   */
  _buildGui: function()
  {
    if (!app.views.medications.MedicationUtils.isTherapyWithDescriptiveVariableDaysDose(this.getTimedDoseElements()))
    {
      this._buildVariableDaysContent();
    }
    else
    {
      this._buildDescriptiveVariableDaysContent();
    }
  },

  /**
   * Builds the descriptive variable days dose display, intended to represent a discharge protocol.
   * @private
   */
  _buildDescriptiveVariableDaysContent: function()
  {
    this.setLayout(new tm.jquery.VFlexboxLayout.create('flex-start', 'stretch', 0));
    this.getTimedDoseElements().forEach(
        /**
         * @param {{doseElement: {quantity: number}, timingDescription: string}} element
         * @param {number} index
         */
        function addDescriptiveRow(element, index)
        {
          var row = new tm.jquery.Container({
            layout: new tm.jquery.HFlexboxLayout.create('flex-start', 'center')
          });

          row.add(
              this._buildCell(
                  element.doseElement.quantity,
                  'summary',
                  'data-index-' + index + '-0'));
          row.add(
              this._buildCell(
                  this.getUnit(),
                  'unit'));
          row.add(
              this._buildCell(
                  element.timingDescription,
                  'description',
                  'data-index-' + index + '-1'));

          this.add(row);
        },
        this)
  },

  /**
   * Builds the variable days dose display, intended to represent the inpatient based dosing protocol.
   * @private
   */
  _buildVariableDaysContent: function()
  {
    this.setLayout(new tm.jquery.HFlexboxLayout.create('flex-start', 'flex-start'));
    var self = this;

    this.timedDoseElements = this.getTimedDoseElements().sort(function(a, b)
    {
      // timeDoseElements may or may not be properly cast from JSON, as a result of a missing jsClass representation
      // one such case is when we load the data for the timeline or grid view, the elements are pure JSON
      var aDateTime = tm.jquery.Utils.isDate(a.date) ? new Date(a.date.getTime()) : new Date(a.date);
      aDateTime.setHours(a.doseTime.hour);
      aDateTime.setMinutes(a.doseTime.minute);

      var bDateTime = tm.jquery.Utils.isDate(b.date) ? new Date(b.date.getTime()) : new Date(b.date);
      bDateTime.setHours(b.doseTime.hour);
      bDateTime.setMinutes(b.doseTime.minute);
      return aDateTime < bDateTime ? -1 : 1;
    });

    var times = []; //in HourMinute
    for (var i = 0; i < this.getTimedDoseElements().length; i++)
    {
      if (app.views.medications.MedicationUtils.getIndexOfHourMinute(this.getTimedDoseElements()[i].doseTime, times) === -1)
      {
        times.push(this.getTimedDoseElements()[i].doseTime);
      }
    }

    times = times.sort(function(a, b)
    {
      var aTime = CurrentTime.get();
      aTime.setHours(a.hour);
      aTime.setMinutes(a.minute);

      var bTime = CurrentTime.get();
      bTime.setHours(b.hour);
      bTime.setMinutes(b.minute);
      return aTime < bTime ? -1 : 1;
    });

    var timesContainer = new tm.jquery.Container({
      cls: 'protocol-summary-metadata',
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'flex-start'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    times.forEach(function(time)
    {
      var fullTime = CurrentTime.get();
      fullTime.setHours(time.hour);
      fullTime.setMinutes(time.minute);
      timesContainer.add(
          self._buildCell(
              self.view.getDisplayableValue(fullTime, 'time.short'),
              'row-header',
              'time-index-' + times.indexOf(time) + '-0'));
    });
    this.add(timesContainer);

    var currentDate = null;
    var currentDayContainer = null;
    var rowIndex, columnIndex = 0;

    for (var j = 0; j < this.getTimedDoseElements().length; j++)
    {
      var element = this.getTimedDoseElements()[j];
      var elementDate = tm.jquery.Utils.isDate(element.date) ? new Date(element.date.getTime()) : new Date(element.date);
      if (currentDate === null ||
          currentDate.getTime() !== elementDate.getTime())
      {
        currentDate = elementDate;
        if (currentDayContainer)
        {
          this.add(currentDayContainer);
        }
        currentDayContainer = new tm.jquery.Container({
          layout: tm.jquery.VFlexboxLayout.create('flex-start', 'flex-start'),
          flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
        });
        currentDayContainer.add(
            this._buildCell(
                app.views.medications.MedicationTimingUtils.getDateWithoutYearDisplay(this.view, currentDate),
                'column-header'));
        rowIndex = 0; // since we're adding column by column, reset row index each new column
        columnIndex++;
      }
      currentDayContainer.add(
          this._buildCell(
              element.doseElement.quantity,
              'summary',
              'data-index-' + rowIndex + '-' + columnIndex));
      rowIndex++;
    }
    if (currentDayContainer)
    {
      this.add(currentDayContainer);
    }

    var dosesContainer = new tm.jquery.Container({
      cls: 'protocol-summary-metadata',
      layout: tm.jquery.VFlexboxLayout.create('flex-start', 'flex-start'),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto')
    });

    times.forEach(function()
    {
      dosesContainer.add(self._buildCell(self.getUnit(), 'unit'));
    });
    this.add(dosesContainer);
  },

  /**
   * @param {String} value
   * @param {Array<string>|String} cls
   * @param {String} [testAttribute=undefined]
   * @returns {tm.jquery.Container}
   * @private
   */
  _buildCell: function(value, cls, testAttribute)
  {
    cls = tm.jquery.Utils.isArray(cls) ? cls : [cls];
    if (this.isLineAcross())
    {
      cls.push('crossed');
    }
    cls.push('protocol-cell');
    cls.push('TextData');

    return new tm.jquery.Container({
      cls: cls.reverse().join(' '),
      flex: tm.jquery.flexbox.item.Flex.create(0, 0, 'auto'),
      html: value,
      testAttribute: testAttribute
    })
  },

  /**
   * @returns {Array}
   */
  getTimedDoseElements: function()
  {
    return this.timedDoseElements;
  },

  /**
   * @returns {boolean}
   */
  isLineAcross: function()
  {
    return this.lineAcross;
  },

  /**
   * @returns {String}
   */
  getUnit: function()
  {
    return this.unit;
  }
});