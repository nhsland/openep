/**
 * The meds-therapy-icon controller implementation. The controller provides the view with information about which
 * class names to use when presenting individual (corner) status icons, similarly to how we implemented the icons
 * in the TherapyContainer of the prescribing module, along side with the general background icon displaying the
 * route of application for the prescribed therapy.
 */
(function()
{
  'use strict';
  angular.module('app.views.medications.angularjs.common.therapy')
      .controller('app.views.medications.angularjs.common.therapy.MedsTherapyIconController',
          ['app.views.medications.angularjs.common.therapy.TherapyStatusEnum',
            'app.views.medications.angularjs.common.therapy.SelfAdministeringActionEnum',
            'app.views.medications.angularjs.common.therapy.PharmacistTherapyChangeType',
            'app.views.medications.angularjs.common.therapy.TherapyChangeReasonEnum', MedsTherapyIconController]);

  /**
   * @param {app.views.medications.angularjs.common.therapy.TherapyStatusEnum} therapyStatusEnum
   * @param {app.views.medications.angularjs.common.therapy.SelfAdministeringActionEnum} selfAdministeringActionEnum
   * @param {app.views.medications.angularjs.common.therapy.PharmacistTherapyChangeType} pharmacistTherapyChangeType
   * @param {app.views.medications.angularjs.common.therapy.TherapyChangeReasonEnum} therapyChangeReasonEnum
   * @constructor
   */
  function MedsTherapyIconController(therapyStatusEnum, selfAdministeringActionEnum, pharmacistTherapyChangeType,
                                     therapyChangeReasonEnum)
  {
    var vm = this;

    vm.getBigIconClass = getBigIconClass;
    vm.isChanged = isChanged;
    vm.getSelfAdministeringIconClass = getSelfAdministeringIconClass;
    vm.getTherapyLinkName = getTherapyLinkName;
    vm.getConsecutiveDay = getConsecutiveDay;
    vm.getStatusIconClass = getStatusIconClass;

    /**
     * Returns the class name for the big therapy icon, based on it's route of administration. Code based on
     * {@link app.views.medications.common.therapy.TherapyContainerDisplayProvider#getTherapyIcon}.
     * @return {string|undefined}
     */
    function getBigIconClass()
    {
      if (!_isTherapyDefined())
      {
        return undefined;
      }

      var route = vm._dto.therapy.route; // backwards compatibility
      // take first route form routes if route not present, which should be te case on new therapies
      // keep in mind that the routes can be empty, as is the case in oxygen
      if ((!route) && (vm._dto.therapy.routes instanceof Array) && vm._dto.therapy.routes.length >= 1)
      {
        route = vm._dto.therapy.routes[0];
      }

      var routeType = !!route ? route.type : undefined;

      if (routeType === 'IV')
      {
        if (vm._dto.therapy.baselineInfusion)
        {
          return 'icon_baseline_infusion';
        }
        if (vm._dto.therapy.continuousInfusion)
        {
          return 'icon_continuous_infusion';
        }
        if (vm._dto.therapy.speedDisplay)
        {
          if (vm._dto.therapy.speedDisplay === 'BOLUS')
          {
            return 'icon_bolus';
          }
          return 'icon_infusion';
        }
        return 'icon_injection'
      }
      if (routeType === 'IM')
      {
        return 'icon_injection'
      }
      if (routeType === 'INHAL')
      {
        return 'icon_inhalation'
      }
      if (vm._dto.therapy.doseForm && vm._dto.therapy.doseForm.doseFormType === 'TBL')
      {
        return 'icon_pills';
      }
      return 'icon_other_medication';
    }

    /**
     * Returns the class name for the therapy's small status icon.
     * @return {string|undefined}
     */
    function getStatusIconClass()
    {
      if (!_isTherapyDefined())
      {
        return undefined;
      }

      var status = this._dto.changeType ? this._dto.changeType : this._dto.therapyStatus;

      if (status === therapyStatusEnum.ABORTED)
      {
        return 'icon_aborted';
      }
      if (status === therapyStatusEnum.CANCELLED)
      {
        return 'icon_cancelled';
      }
      if (status === therapyStatusEnum.LATE)
      {
        return 'icon_late';
      }
      if (status === therapyStatusEnum.VERY_LATE)
      {
        return 'icon_very_late';
      }
      if (status === therapyStatusEnum.SUSPENDED)
      {
        return vm._dto.therapyChangeReasonEnum === therapyChangeReasonEnum.TEMPORARY_LEAVE ?
            'icon_suspended_temporary_leave' :
            'icon_suspended';
      }
      if (status === pharmacistTherapyChangeType.ABORT)
      {
        return 'icon_aborted';
      }
      if (status === pharmacistTherapyChangeType.SUSPEND)
      {
        return 'icon_suspended';
      }

      return undefined;
    }

    /**
     * @return {boolean} true, if the therapy was modified after being prescribed, otherwise false.
     */
    function isChanged()
    {
      return _isTherapyDefined() ?
          !!vm._dto.modifiedFromLastReview || !!vm._dto.therapy.completed : // no valid status on our DTO
          false;
    }

    /**
     * @return {number|undefined} of consecutive days, if present.
     */
    function getConsecutiveDay()
    {
      if (!_isTherapyDefined())
      {
        return;
      }

      return !!vm._dto.showConsecutiveDay && angular.isNumber(vm._dto.consecutiveDay) ? vm._dto.consecutiveDay : undefined;
    }

    /**
     * Returns the link name in case when the therapy is linked. Otherwise nothing.
     * @return {string|undefined}
     */
    function getTherapyLinkName()
    {
      if (!_isTherapyDefined())
      {
        return;
      }

      return !!vm._dto.therapy.linkName && vm._dto.therapy.linkName.length <= 3 ? vm._dto.therapy.linkName : undefined;
    }

    /**
     * Returns the class name for the icon representing the self administering action, if the therapy is active and was
     * prescribed as a self administered therapy.
     * Based on {@link app.views.medications.common.therapy.TherapyContainerDisplayProvider#getSelfAdminStatusIcon}.
     * @return {string|undefined}
     */
    function getSelfAdministeringIconClass()
    {
      if (!_isTherapyDefined())
      {
        return;
      }

      var therapyStatus = this._dto.changeType ? this._dto.changeType : this._dto.therapyStatus;
      var isTherapyActive = !therapyStatus ||
          [therapyStatusEnum.ABORTED, therapyStatusEnum.CANCELLED, therapyStatusEnum.SUSPENDED].indexOf(therapyStatus) > -1;
      var selfAdminActionEnum = vm._dto.therapy.selfAdministeringActionEnum;

      if (isTherapyActive)
      {
        if (selfAdminActionEnum === selfAdministeringActionEnum.AUTOMATICALLY_CHARTED)
        {
          return 'icon_self_admin_automatic';
        }
        else if (selfAdminActionEnum === selfAdministeringActionEnum.CHARTED_BY_NURSE)
        {
          return 'icon_self_admin_nurse';
        }
      }
      return undefined;
    }

    /* Private methods */

    /**
     * @return {boolean} true, if the therapy object on our input dto is set, otherwise false.
     * @private
     */
    function _isTherapyDefined()
    {
      return angular.isObject(vm._dto) && angular.isObject(vm._dto.therapy);
    }
  }
})();
