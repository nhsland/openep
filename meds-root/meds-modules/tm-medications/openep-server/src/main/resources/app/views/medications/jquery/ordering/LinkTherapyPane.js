Class.define('app.views.medications.ordering.LinkTherapyPane', 'app.views.common.containers.AppDataEntryContainer', {

  /** configs */
  view: null,
  orderedTherapies: null,
  /** components **/
  list: null,
  /** privates*/
  resultCallback: null,

  /** constructor */
  Constructor: function(config)
  {
    var self = this;
    this.callSuper(config);
    this.setLayout(tm.jquery.VFlexboxLayout.create("flex-start", "stretch"));
    this._buildComponents();
    this._buildGui();

    this.on(tm.jquery.ComponentEvent.EVENT_TYPE_RENDER, function()
    {
      self._setListData(self.orderedTherapies);
    });
  },

  /** private methods */
  _buildComponents: function()
  {
    var self = this;
    this.list = new tm.jquery.List({
      cls: 'link-candidates-list',
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
      autoLoad: false,
      dataSource: [],
      itemTpl: function(index, item)
      {
        return self._buildRow(index, item.therapy);
      },
      selectable: true
    });
  },
  _buildGui: function()
  {
    this.add(this.list);
  },

  _buildRow: function(index, therapy)
  {
    var container = new tm.jquery.Container({
      cls: 'link-candidates-row',
      padding: '5 5 5 10',
      layout: tm.jquery.HFlexboxLayout.create("flex-start", "start")
    });

    container.add(new tm.jquery.Container({
      margin: '2 0 0 0',
      height: 20,
      width: 20,
      cls: therapy.completed == false ? 'invalid-therapy-icon' : ""
    }));

    var therapyContainer = new tm.jquery.Container({
      flex: tm.jquery.flexbox.item.Flex.create(1, 0, "auto"),
      html: therapy.formattedTherapyDisplay,
      cls: 'TherapyDescription'
    });

    if (therapy.completed === false)
    {
      container.setCls('invalid-therapy');
    }
    container.add(therapyContainer);
    return container;
  },

  /**
   * @param {Array<app.views.medications.common.dto.Therapy>} therapies
   * @private
   */
  _setListData: function(therapies)
  {
    for (var i = 0; i < therapies.length; i++)
    {
      var therapy = therapies[i];
      var otherTherapiesLinkedToTherapy =
          app.views.medications.MedicationUtils.areOtherTherapiesLinkedToTherapy(therapy.getLinkName(), therapies);
      if (therapy.isContinuousInfusion() && !tm.jquery.Utils.isEmpty(therapy.getEnd()) && !otherTherapiesLinkedToTherapy)
      {
        var rowData = {therapy: therapies[i]};
        this.list.addRowData(rowData, i);
      }
    }
    if (this.list.getListData().length === 1)
    {
      this.list.setSelections([this.list.getListData()[0]]);
    }
  },

  /** public methods */
  processResultData: function(resultDataCallback)
  {
    this.resultCallback = resultDataCallback;
    var selectedTherapy = this.list.getSelections().isEmpty() ? null : this.list.getSelections().get(0).therapy;

    if (tm.jquery.Utils.isEmpty(selectedTherapy))
    {
      var message = this.view.getDictionary('you.have.no.therapies.selected');
      this.view.getAppFactory().createWarningSystemDialog(message, 320, 160).show();
      this.resultCallback(new app.views.common.AppResultData({success: false}));
    }
    else
    {
      this.resultCallback(new app.views.common.AppResultData({success: true, selectedTherapy: selectedTherapy}));
    }
  }
});
