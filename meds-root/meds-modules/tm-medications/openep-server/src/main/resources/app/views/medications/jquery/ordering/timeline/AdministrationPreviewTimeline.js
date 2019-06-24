Class.define('app.views.medications.ordering.timeline.AdministrationPreviewTimeline', 'tm.jquery.Container', {
  cls: "administration-preview-timeline",

  data: null,
  therapy: null,

  autoDraw: true,
  options: null,
  view: null,

  intervalStart: null,
  intervalEnd: null,

  _timeline: null,
  _timelineContainer: null,
  _visData: null,

  Constructor: function(config)
  {
    this.callSuper(config);

    var tomorrow = CurrentTime.get();
    tomorrow.setDate(tomorrow.getDate() + 1);

    this.data = this.getConfigValue('data', []);
    this.intervalStart = this.getConfigValue('intervalStart', CurrentTime.get());
    this.intervalEnd = this.getConfigValue('intervalEnd', tomorrow);
    this._visData = new vis.DataSet([], {queue: true});

    this.options = {
      width: "100%",
      locale: this.getView().getViewLanguage(),
      locales: {},
      showMajorLabels: true,
      showMinorLabels: true,
      moveable: false,
      zoomable: false,
      selectable: false, /* prevent selectable styles being added to content since we don't need them */
      stack: false,
      timeAxis: { scale: 'hour', step: 2 },
      orientation: {axis: 'top'},
      type: 'point',
      align: 'center'
    };
    // create locale (text strings should be replaced with localized strings) for timeline
    this.options.locales[this.options.locale] = {
      current: this.getView().getDictionary("visjs.timeline.current"),
      time: this.getView().getDictionary("visjs.timeline.time")
    };

    this._buildGui();
  },

  /**
   * @return {boolean} true, if the timeline should be initialized and drawn as soon as the component is rendered, otherwise
   * false if the initialization should be delayed until the {@link #setData} or {@link #refreshData} is called. Intended
   * to reduce the number of painting the browser has to perform if the data is expected to shortly available.
   */
  isAutoDraw: function()
  {
    return this.autoDraw === true;
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  },

  /**
   * @return {Array<Object>} an array of administration tasks displayed in this timeline.
   */
  getData: function()
  {
    return tm.jquery.Utils.isEmpty(this.data) ? [] : this.data;
  },

  /**
   * @returns {app.views.medications.common.dto.Therapy} the therapy for which this preview is based on.
   */
  getTherapy: function()
  {
    return this.therapy;
  },

  /**
   * Updates the contents of the timeline based on the given data (therapy and administration tasks).
   * @param {app.views.medications.common.dto.Therapy} therapy
   * @param {Array<Object>} data
   */
  setData: function (therapy, data)
  {
    this.data = tm.jquery.Utils.isEmpty(data) ? [] : data;
    this.therapy = therapy;
    this._applyTimelineContent();
  },

  /**
   * @param {Date} date representing
   */
  setStartTime: function(date)
  {
    this.intervalStart = new Date(date.valueOf());
    this.intervalEnd = new Date(date.valueOf());

    this.intervalStart.setHours(date.getHours() - 4);
    this.intervalEnd.setDate(date.getDate() + 1);
  },

  /**
   * Clears the contents of the timeline.
   */
  clear: function ()
  {
    this.data.removeAll();
    this.therapy = null;
    this._applyTimelineContent();
  },

  /**
   * @Override
   */
  destroy: function ()
  {
    if (this._timeline) {
      this._timeline.destroy();
      this._timeline = null;
    }
    this.callSuper();
  },

  /**
   * @param {Date} start
   * @param therapy
   */
  refreshData: function (start, therapy)
  {
    if (!tm.jquery.Utils.isDate(start))
    {
      this.clear();
      return;
    }

    var self = this;
    this.getView()
        .getRestApi()
        .loadTherapyAdministrationTimes(therapy, true)
        .then(
            function onSuccess(data)
            {
              self.setStartTime(start);
              self.setData(therapy, data);
            });
  },

  _buildGui: function ()
  {
    var self = this;

    this._timelineContainer = new tm.jquery.Container({
      //cls: "therapy-timeline",
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      scrollable: "visible"
    });
    this.add(this._timelineContainer);

    if (this.isAutoDraw())
    {
      this._timelineContainer.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function ()
      {
        self._applyTimelineContent();
      });
    }
  },

  _buildTimelineElements: function()
  {
    var administrationTaskContentFactory =
        new app.views.medications.ordering.timeline.PreviewTimelineAdministrationTaskContentFactory({view: this.getView()});

    var elementCreator = new app.views.medications.common.timeline.TimelineContentBuilder()
        .setView(this.getView())
        .setAdministrationTaskContentFactory(administrationTaskContentFactory)
        .setTherapy(this.getTherapy())
        .setAdministrations(this.getData())
        .setIntervalStart(this.intervalStart)
        .setIntervalEnd(this.intervalEnd)
        .setGroupId(this._buildDefaultDataGroup().id);

    var timelineElements = elementCreator.build();
    this._removeBrokenDurationElements(timelineElements);

    this._visData.clear();
    this._visData.add(timelineElements);
    this._visData.flush();
  },

  /**
   * Updates the contents of the internal Vis.js data set linked to the timeline by recreating the Vis.js specific data
   * structures for the tasks found in {@link #getData}. Reconfigures the start and end interval of the timeline if exists.
   * If the timeline plugin hasn't been initialized, triggers the creation of the plugin, in which case the data and
   * the display interval is used in the construction.
   * @private
   */
  _applyTimelineContent: function ()
  {
    this._buildTimelineElements();

    if (this._timeline)
    {
      this._timeline.setOptions(this._buildTimelineOptions());
    }
    else if (this._timelineContainer.isRendered())
    {
      this._drawTimeline(this._timelineContainer);
    }
  },

  /**
   * Checks all range elements which are generated based on start and stop tasks and removes those
   * who's start property is not set as they will produce exceptions inside the Vis.js library.
   * The cause for such generated items is when the duration of the infusion is such that
   * the individual infusion durations overlap - currently we don't know how to match the appropriate
   * start and end tasks from the data we receive.
   * @param {Array<Object>} timelineElements
   * @private
   */
  _removeBrokenDurationElements: function(timelineElements)
  {
    if (!timelineElements) return;

    var idx = timelineElements.length;
    while (idx--)
    {
      if (timelineElements[idx].type === 'range' && !timelineElements[idx].start)
      {
        timelineElements.remove(timelineElements[idx]);
      }
    }
  },

  /**
   * @return {Object} representing the Vis.JS Timeline configuration object. Takes {@link #options} and attaches the
   * interval start and end properties, determined by the given {@link #therapy}. Ideally we'd want to load the plugin
   * once all the data is available, to reduce the number of redraws, but in the case of the ordering container there
   * is no initial data, unless editing a template based therapy directly.
   * @private
   */
  _buildTimelineOptions: function()
  {
    var options = jQuery.extend(true, {}, this.options);
    options.min = this.intervalStart;
    options.max = this.intervalEnd;
    options.start = options.min;
    options.end =  options.max;
    return options;
  },

  /**
   * @return {{id: number, content: string}} the default and only group in which all content is shown.
   * @private
   */
  _buildDefaultDataGroup: function()
  {
    return {
      id: 1,
      content: ''
    }
  },

  /**
   * Constructs the Vis.js timeline, causing it create it's DOM into the passed element. Does nothing it timeline already
   * exists.
   * @param {tm.jquery.Component} paintToComponent
   * @private
   */
  _drawTimeline: function (paintToComponent)
  {
    if (!this._timeline)
    {
      this._timeline = new vis.Timeline(
          paintToComponent.getDom(),
          this._visData,
          [this._buildDefaultDataGroup()],
          this._buildTimelineOptions());
    }
    else
    {
      console.warn('Attempted to construct the Vis.JS Timeline more than one time.');
    }
  }
});
