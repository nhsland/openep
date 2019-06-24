Class.define('app.views.medications.ordering.timeline.PreviewTimelineAdministrationTaskContentFactory', 'app.views.medications.common.timeline.AbstractTimelineAdministrationTaskContentFactory', {
  view: null,

  /** constructor */
  Constructor: function(config)
  {
    this.callSuper(config);
  },

  getView: function()
  {
    return this.view;
  },

  /**
   * Returns a simplified representation of an administration task, ignoring most of the administration statuses
   * and adding a time representation of the administration due time underneath the task.
   * @override
   * @param therapy
   * @param administration
   */
  createContentContainer: function(therapy, administration)
  {
    var administrationTimestamp = administration.getAdministrationTimestamp();
    var orderState = this._getOrderStateString(administration);
    var taskStateDisplay = '<div class="task ' + this._getAdministrationClass(administration) + '">' +
        '<div class="orderState">' + orderState + '</div><div class="timeLabel TextData">' +
        this.getView().getDisplayableValue(administrationTimestamp, "short.time") + '</div></div>';

    var taskStateContainer = new tm.jquery.Container({
      scrollable: 'visible',
      cls: "task-state-container",
      html: taskStateDisplay
    });
    taskStateContainer.doRender();

    return taskStateContainer;
  },

  _getAdministrationClass: function (administration)
  {
    var administrationClass = tm.jquery.Utils.isEmpty(administration) ? '' : administration.getAdministrationStatus();

    if (!tm.jquery.Utils.isEmpty(administration) && !tm.jquery.Utils.isEmpty(administration.getInfusionSetChangeEnum()))
    {
      administrationClass += " " + administration.getInfusionSetChangeEnum();
    }

    return administrationClass;
  },

  _getOrderStateString: function (administration)
  {
    var enums = app.views.medications.TherapyEnums;
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.INFUSION_SET_CHANGE)
    {
      if (administration.getInfusionSetChangeEnum() === enums.infusionSetChangeEnum.INFUSION_SYSTEM_CHANGE)
      {
        return "";
      }
      if (administration.getInfusionSetChangeEnum() === enums.infusionSetChangeEnum.INFUSION_SYRINGE_CHANGE)
      {
        return "";
      }
      return "";
    }
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.START)
    {
      return administration.isDifferentFromOrder() ? '&#916;' : '';         //delta
    }
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.STOP)
    {
      return 'X';
    }
    if (administration && administration.getAdministrationType() === enums.administrationTypeEnum.ADJUST_INFUSION)
    {
      if (administration.isDifferentFromOrder())
      {
        return 'E\'';
      }
      return 'E';
    }
    return '';
  }
});