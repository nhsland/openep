(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.data.models')
      .constant('tm.angular.medications.documentation.data.models.PrescriptionStatus', {
        PRESCRIBED: 'PRESCRIBED',
        PARTIALLY_USED: 'PARTIALLY_USED',
        CANCELLED: 'CANCELLED',
        USED: 'USED',
        IN_PREPARATION: 'IN_PREPARATION',
        IN_DISPENSE: 'IN_DISPENSE',
        PARTIALLY_USED_AND_CANCELLED: 'PARTIALLY_USED_AND_CANCELLED',
        REJECTED: 'REJECTED',
        WITHDRAWN: 'WITHDRAWN',
        PARTIALLY_USED_AND_REJECTED: 'PARTIALLY_USED_AND_REJECTED'
      });
})(); 