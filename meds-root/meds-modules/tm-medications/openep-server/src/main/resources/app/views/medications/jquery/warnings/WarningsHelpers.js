Class.define('app.views.medications.warnings.WarningsHelpers', 'tm.jquery.Object', {

      /** statics */
      statics: {
        /**
         * @param {app.views.common.AppView} view
         * @param {app.views.medications.warnings.dto.MedicationsWarning} warning
         * @returns {tm.jquery.Container|null}
         */
        createMonographContainer: function(view, warning)
        {
          if (warning.getMonographHtml())
          {
            return new tm.jquery.Container({
              width: 30,
              cls: 'icon-monograph',
              flex: tm.jquery.HFlexboxLayout.create("flex-start", "flex-end"),
              tooltip: view.getAppFactory().createDefaultPopoverTooltip(
                  view.getDictionary('monograph'),
                  null,
                  new tm.jquery.Container({
                    cls: 'monograph',
                    scrollable: "both",
                    html: warning.getMonographHtml()
                  }),
                  800, 600
              )
            });
          }
          return null;
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {String} warningType See {@link app.views.medications.TherapyEnums.warningType}
         * @param {String|undefined} [warningSeverity=undefined]
         * @returns {tm.jquery.Container|null}
         */
        createTypeAndSeverityIconContainer: function(view, warningType, warningSeverity)
        {
          var enums = app.views.medications.TherapyEnums;
          if (warningSeverity && (warningSeverity === enums.warningSeverityEnum.HIGH_OVERRIDE ||
                  warningSeverity === enums.warningSeverityEnum.HIGH))
          {
            return new tm.jquery.Container({
              width: 30,
              cls: 'severity icon-high',
              tooltip:
                  app.views.medications.warnings.WarningsHelpers.getTypeAndSeverityTooltip(view, warningType, warningSeverity)
            });
          }
          return null;
        },

        getTypeAndSeverityTooltip: function(view, warningType, warningSeverity)
        {
          var severityString = view.getDictionary("warning.severity." + warningSeverity);
          var typeString = view.getDictionary("warning.type." + warningType);
          var tooltipString = tm.jquery.Utils.isEmpty(warningSeverity) ? typeString : typeString + " - " + severityString;

          return app.views.medications.MedicationUtils.createTooltip(tooltipString, "right", view);
        },

        /**
         * @param {Number|null} percentage
         * @returns {String|null}
         */
        getImageClsForMaximumDosePercentage: function(percentage)
        {
          if (tm.jquery.Utils.isEmpty(percentage))
          {
            return null;
          }
          if (percentage < 50)
          {
            return 'max-dose-low-icon';
          }
          else if (percentage < 100)
          {
            return 'max-dose-significant-icon';
          }
          else
          {
            return 'max-dose-high-icon';
          }
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {string} overrideReason
         * @param {string} warningDescription
         * @returns {string}
         */
        createOverriddenWarningString: function(view, overrideReason, warningDescription)
        {
          return view.getDictionary("warning.overridden") + ". " + view.getDictionary("reason")+ ": "  +
              overrideReason + " " + view.getDictionary("warning") + ": " + warningDescription;
        }
      }
    }
);
