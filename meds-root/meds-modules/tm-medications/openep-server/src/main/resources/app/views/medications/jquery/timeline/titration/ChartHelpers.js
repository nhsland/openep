Class.define('app.views.medications.timeline.titration.ChartHelpers', 'tm.jquery.Object', {
  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @param {Element} renderToElement
   * @param {Number} chartHeight
   * @param {Number} tickInterval - tick interval in hours
   * @param {Boolean} [showTimeLabels=false] set to true to display X axis time labels at the bottom of the chart
   * @param {Boolean} [showDateLabels=false] set to true to display X axis day labels at the bottom of the chart
   * @returns {tm.jquery.Promise}
   */
  createChart: function(renderToElement, chartHeight, tickInterval, showTimeLabels, showDateLabels)
  {
    showTimeLabels = showTimeLabels || false;
    showDateLabels = showDateLabels || false;

    var chartTheme = this.getTheme();
    var deferred = tm.jquery.Deferred.create();
    var view = this.getView();

    tm.jquery.ExternalsUtils.createHighchart({
          chart: {
            renderTo: renderToElement,
            backgroundColor: chartTheme.background,
            animation: false,
            marginLeft: 0,
            marginRight: 0,
            spacingTop: 0,
            spacingLeft: 0,
            spacingRight: 20, // adjustment for possible scrollbars, limits the right position for labels that overflow
            spacingBottom: 0,
            height: chartHeight,
            width: 543,
            events: {
              load: function(event)
              {
                deferred.resolve(event.currentTarget);
              }
            }
          },
          title: {
            text: null
          },
          xAxis: [
            {
              type: 'datetime',
              dateTimeLabelFormats: {
                hour: '%H'
              },
              gridLineWidth: 1,
              tickInterval: tickInterval * 3600 * 1000,
              lineColor: chartTheme.xAxisGridLineColor,
              tickLength: 0,
              labels: {
                enabled: showTimeLabels,
                y: 12,
                style: {
                  color: chartTheme.xAxisLabelsColor,
                  fontSize: '10px'
                },
                formatter: this.getXAxisLabelFormatter()
              }
            },
            {
              linkedTo: 0,
              opposite: true,
              type: 'datetime',
              gridLineWidth: 3,
              tickInterval: 24 * 3600 * 1000,
              lineColor: chartTheme.xAxisGridLineColor,
              tickLength: 0,
              labels: {
                enabled: showDateLabels,
                formatter: function()
                {
                  return app.views.medications.MedicationTimingUtils.getFriendlyDateDisplayableValue(this.value, view);
                },
                style: {
                  color: chartTheme.xAxisLabelsColor,
                  fontSize: '14px' // setting it to less, for some reason, only works on 'yesterday|today|tomorrow' labels
                }
              }
            }],
          yAxis: {
            min: 0,
            offset: 24,
            tickAmount: 5,
            showLastLabel: true,
            type: 'linear',
            title: {
              text: null
            },
            gridLineWidth: 0,
            labels: {
              enabled: true,
              allowDecimals: false,
              align: 'left',
              x: 3,
              y: -2,
              style: {
                color: chartTheme.yAxisLabelsColor,
                fontSize: '10px'
              }
            }
          },
          legend: {
            enabled: false
          },
          tooltip: {
            enabled: true,
            shadow: true,
            backgroundColor: chartTheme.tooltipBackgroundColor,
            followPointer: false,
            shared: false,
            borderColor: chartTheme.tooltipBackgroundColor,
            borderRadius: 2,
            borderWidth: 0,
            formatter: this.getYAxisTooltipFormatter(),
            hideDelay: 20,
            snap: 0,
            style: {
              width: 450
            }
          },
          plotOptions: {
            series: {
              states: {
                hover: {
                  enabled: false
                }
              }
            }
          }
        }
    );

    return deferred.promise();
  },

  /**
   * @returns {{background: string, xAxisGridLineColor: string, yAxisGridLineColor: string, xAxisLabelsColor: string, xAxisStackLabelsColor: string, yAxisLabelsColor: string, yAxisStackLabelsColor: string, columnBorderColor: string, zeroLineColor: string, currentTimeLineColor: string, cumulativeLineColor: string, maxInputLineColor: string, maxInputLabelColor: string, maxOutputLineColor: string, maxOutputLabelColor: string, balanceBoxLabelColor: string, balanceBoxFillColor: string, balanceBoxStrokeColor: string}}
   */
  getTheme: function()
  {
    return {
      background: '#ffffff',
      xAxisGridLineColor: '#dcdcdc',
      yAxisGridLineColor: '#dcdcdc',
      xAxisLabelsColor: "#61646D",
      xAxisStackLabelsColor: "#000",
      yAxisLabelsColor: "#61646D",
      yAxisStackLabelsColor: '#576c80',
      columnBorderColor: "#fff",
      zeroLineColor: '#b1b1b1',
      currentTimeLineColor: "#be1e2d",
      cumulativeLineColor: '#61646D',
      maxInputLineColor: '#fcd9dd',
      maxInputLabelColor: '#be1e2d',
      maxOutputLineColor: '#fcd9dd',
      maxOutputLabelColor: '#be1e2d',
      balanceBoxLabelColor: '#61646D',
      balanceBoxFillColor: '#eeeeee',
      balanceBoxStrokeColor: '#888',
      tooltipBackgroundColor: '#F7F7F7'
    };
  },

  /**
   * @returns {function}
   */
  getXAxisLabelFormatter: function()
  {
    return function()
    {
      var hour = Highcharts.dateFormat('%H', this.value);
      return hour == 0 ? '24' : +hour;
    }
  },

  getXAxisDayLabelFormat: function()
  {
    var view = this.getView();
    return function() {
      app.views.medications.MedicationTimingUtils.getFriendlyDateDisplayableValue(this.value, view)
    }
  },

  /**
   * @returns {function}
   */
  getYAxisDataLabelFormatter: function()
  {
    return function()
    {
      var format = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2};
      return app.views.medications.MedicationUtils.safeFormatNumber(this.y, format);
    }
  },

  /**
   * @returns {function}
   */
  getYAxisTooltipFormatter: function()
  {
    var self = this;
    return function()
    {
      var view = self.getView();
      var element = this.point.element;
      if (element)
      {
        var quantityLabel;
        var quantity = element.getQuantity();
        var quantityUnit = this.point.quantityUnit;

        if (element.isBolusAdministration())
        {
          quantityLabel = view.getDictionary("dose");
          quantity = element.getBolusQuantity();
          quantityUnit = element.getBolusUnit();
        }
        else if (this.point.hasMeasurementResults)
        {
          quantityLabel = view.getDictionary("measurement");
        }
        else if (this.point.hasAdministrations && !this.point.isContinuousInfusion)
        {
          quantityLabel = view.getDictionary("dose");
        }
        else if (this.point.hasAdministrations && this.point.isContinuousInfusion)
        {
          quantityLabel = view.getDictionary("rate");
        }
        var format = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2};

        var tooltipText = app.views.medications.MedicationUtils.safeFormatNumber(quantity, format);
        if (quantityUnit)
        {
          tooltipText += " " + quantityUnit;
        }
        var tooltipContent = '<span style="color: #848998; text-transform: uppercase;">' + quantityLabel +
            " " + '</span>' + '<span style=“color: #646464”>' + tooltipText + '</span>';

        if (element.hasComment())
        {
          tooltipContent += '<br>' + '<span style="color: #848998; text-transform: uppercase;">' +
              view.getDictionary("commentary") + " " + '</span>' + '<span style="color: #646464">' + element.getComment() +
              '</span>';
        }
        return tooltipContent;
      }
      return false;
    }
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});