(function()
{
  'use strict';
  angular.module('tm.angular.medications.documentation.common.ehr')
      .constant('tm.angular.medications.documentation.common.ehr.Constants', {
        VERSION_MARKER: '::'
      })
      .service('tm.angular.medications.documentation.common.ehr.CompositionUidUtils', [
        'tm.angular.medications.documentation.common.ehr.Constants', function(constants)
        {
          var service = this;
          service.getVersionlessCompositionUid = getVersionlessCompositionUid;
          service.getCompositionUidVersion = getCompositionUidVersion;
          service.incrementCompositionUid = incrementCompositionUid;
          service.isCompositionUidSameOrNewer = isCompositionUidSameOrNewer;

          /**
           * Returns the composition UID without the version information.
           * @param {string} compositionUid
           * @returns {string} A new string containing the composition UID.
           * @private
           */
          function getVersionlessCompositionUid(compositionUid)
          {
            var versionMarkerIndex = _getCompositionUidVersionMarkerIndex(compositionUid);
            return versionMarkerIndex > -1 ? compositionUid.substring(0, versionMarkerIndex) : compositionUid;
          }

          /**
           * @param {string} compositionUid
           * @returns {number} Returns the composition's version, or 0 if none is defined.
           * @private
           */
          function getCompositionUidVersion(compositionUid)
          {
            var versionMarkerIndex = _getCompositionUidVersionMarkerIndex(compositionUid);
            if (versionMarkerIndex > -1)
            {
              var currentVersion = parseInt(
                  compositionUid.substring(versionMarkerIndex + _getCompositionUidVersionMarker().length),
                  10
              );

              return angular.isNumber(currentVersion) ? currentVersion : 0;
            }

            return 0;
          }

          /**
           * Increments the EER composition UUID by one version.
           * @param {String} compositionUid
           * @returns {String}
           * @private
           */
          function incrementCompositionUid(compositionUid)
          {
            if (!angular.isDefined(compositionUid))
            {
              return '';
            }

            var versionMarkerIndex = _getCompositionUidVersionMarkerIndex(compositionUid);
            if (versionMarkerIndex > -1)
            {
              versionMarkerIndex += _getCompositionUidVersionMarker().length;
              var newNumber = parseInt(compositionUid.substring(versionMarkerIndex), 10);
              newNumber++;
              return compositionUid.substring(0, versionMarkerIndex) + newNumber;
            }

            return compositionUid;
          }

          /**
           * Compares the first and second composition UID and if the second one is the same or a newer version of the first
           * one returns true.
           * @param {string} compositionUid1 Composition UID to compare to.
           * @param {string} compositionUid2 Composition UID to compare with.
           * @returns {boolean} True if the second composition uid is newer or same as first.
           * @private
           */
          function isCompositionUidSameOrNewer(compositionUid1, compositionUid2)
          {
            if (compositionUid1 === compositionUid2)
            {
              return true;
            }

            var compositionUid1Base = getVersionlessCompositionUid(compositionUid1);
            var compositionUid2Base = getVersionlessCompositionUid(compositionUid2);

            if (compositionUid1Base === compositionUid2Base)
            {
              return getCompositionUidVersion(compositionUid1) <= getCompositionUidVersion(compositionUid2);
            }

            return false;
          }

          /**
           * @param {string} compositionUid
           * @returns {number} 0 or more if successful, otherwise -1
           * @private
           */
          function _getCompositionUidVersionMarkerIndex(compositionUid)
          {
            if (!angular.isDefined(compositionUid))
            {
              return -1;
            }

            return compositionUid.lastIndexOf(_getCompositionUidVersionMarker());
          }

          /**
           * @returns {string} EHR composition UID version marker.
           * @private
           */
          function _getCompositionUidVersionMarker()
          {
            return constants.VERSION_MARKER;
          }
        }]
      );
})();

