Class.define('app.views.medications.pharmacists.DailyReviewsContainerHeader', 'tm.jquery.Container', {
  cls: "header-container",

  /* public members */
  dailyContainer: null,

  /* event handler callbacks */
  doneButtonClickCallback: null,
  allTherapiesCheckboxChangeCallback: null,

  /* private members */
  _actionContainer: null,
  _doneButton: null,
  _statusIcon: null,

  /* constructor */
  Constructor: function (config)
  {
    this.callSuper(config);

    this._buildGui();
  },

  ///
  /// private methods
  ///
  _buildGui: function ()
  {
    this.setLayout(tm.jquery.HFlexboxLayout.create("flex-start", "center"));

    var self = this;
    var contentDate = this.getDailyContainer().getContentDate();
    var today = CurrentTime.get();
    var yesterday = CurrentTime.get();
    var view = this.getDailyContainer().getView();
    var dateString = view.getDisplayableValue(new Date(contentDate), "short.date");
    var isActive = this.getDailyContainer().isActive();
    var isCareProfessional = !tm.jquery.Utils.isEmpty(view.getCurrentUserAsCareProfessional());
    var reviewCount = this.getDailyContainer().getContent().length;

    yesterday.setDate(yesterday.getDate() - 1);
    yesterday.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    var statusIcon = new tm.jquery.Image({
      cls: "status-icon",
      width: 16,
      height: 16,
      hidden: !this.getDailyContainer().isActive()
    });

    if (today.getTime() === contentDate.getTime())
    {
      dateString = view.getDictionary("today") + ' ' + dateString;
    }
    else if (yesterday.getTime() === contentDate.getTime())
    {
      dateString = view.getDictionary("yesterday") + ' ' + dateString;
    }

    var title = new tm.jquery.Component({
      cls: "PortletHeading1",
      html: dateString,
      flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto")
    });

    var actionContainer = new tm.jquery.Container({
      padding: "0 5 0 5",
      layout: tm.jquery.HFlexboxLayout.create("flex-end", "center", 15)
    });

    if (this.getDailyContainer().getView().getTherapyAuthority().isManagePatientPharmacistReviewAllowed())
    {
      var doneButton = new tm.jquery.Button({
        enabled: isActive && reviewCount > 0 && isCareProfessional,
        hidden: !isActive,
        text: this.getDailyContainer().getView().getDictionary("i.am.done"),
        height: 23,
        alignSelf: "center",
        handler: function (component)
        {
          component.setEnabled(false);
          if (!tm.jquery.Utils.isEmpty(self.doneButtonClickCallback))
          {
            self.doneButtonClickCallback();
          }
        }
      });

      actionContainer.add(doneButton);
    }

    this.add(statusIcon);
    this.add(title);
    this.add(actionContainer);

    this._actionContainer = actionContainer;
    this._statusIcon = statusIcon;
    this._doneButton = doneButton;
  },

  ///
  // getters, setters, public
  //
  getDailyContainer: function ()
  {
    return this.dailyContainer;
  },

  getActionContainer: function ()
  {
    return this._actionContainer;
  },

  refresh: function ()
  {
    var dailyContainer = this.getDailyContainer();

    if (dailyContainer.isActive())
    {
      this._actionContainer.show();
    }
    else
    {
      this._actionContainer.hide();
    }
  },

  setDoneButtonEnabled: function (value)
  {
    var isCareProfessional = !tm.jquery.Utils.isEmpty(this.getDailyContainer().getView().getCurrentUserAsCareProfessional());

    if (!this.getDailyContainer().getView().getTherapyAuthority().isManagePatientPharmacistReviewAllowed() ||
        (value == true && !isCareProfessional))
    {
      return; // not permitted if not a care professional
    }


    this._doneButton.setEnabled(value);

    if (value === true)
    {
      this._statusIcon.setCls("status-icon inprogress");
    }
    else
    {
      this._statusIcon.setCls("status-icon done");
    }
  }
});
