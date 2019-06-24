(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.documentHeader')
      .filter('tmMedsNamedExternalNameFilter', function()
      {
        return function(namedExternalDto)
        {
          return angular.isDefined(namedExternalDto) ? namedExternalDto.name : null;
        }
      });
})();
