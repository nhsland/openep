Class.define('app.views.medications.MedicationUtils', 'tm.jquery.Object', {

      /** statics */
      statics: {
        createTooltip: function(text, placement, view)
        {
          if (tm.jquery.Utils.isEmpty(view))
          {
            return new tm.jquery.Tooltip({
              type: "tooltip",
              placement: tm.jquery.Utils.isEmpty(placement) ? "bottom" : placement,
              trigger: "hover",
              title: text,
              html: true,
              animation: false,
              delay: {
                show: 1000,
                hide: 1000
              }
            });
          }
          else if (view instanceof app.views.common.AppView)
          {
            return view.getAppFactory().createDefaultHintTooltip(
                text, tm.jquery.Utils.isEmpty(placement) ? "bottom" : placement, "hover");
          }
        },
        createHintTooltip: function(view, message, placement)
        {
          return view.getAppFactory().createDefaultHintTooltip(message, placement);
        },

        pad: function(number, length)
        {
          var str = '' + number;
          while (str.length < length)
          {
            str = '0' + str;
          }
          return str;
        },

        /**
         * @param {number|null|undefined} value
         * @param {Object} format see Globalize number/format-properties.
         * @return {null|string}
         */
        doubleToString: function(value, format)
        {
          if (value == null)
          {
            return null;
          }

          var maximumFractionDigits = 3;
          if (value < 1)
          {
            var decimals = null;
            var valueString = value.toString();
            var delimitedNumber = valueString.split(".");
            if (delimitedNumber.length > 1)
            {
              decimals = delimitedNumber[1]
            }
            if (decimals)
            {
              var regexp = new RegExp('[1-9]');
              var indexOfFirstNonZero = regexp.exec(decimals).index;
              maximumFractionDigits = indexOfFirstNonZero + 3;
            }
          }
          format = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: maximumFractionDigits};

          return Globalize.formatNumber(value, format);
        },

        /**
         * @param {number} num
         * @return {boolean} true, if the number is an integer larger than 0, otherwise false.
         */
        isPositiveInteger: function(num)
        {
          return num >>> 0 === parseFloat(num.toString()) && num > 0;
        },

        /**
         * @param {string} value
         * @return {boolean} true, if the string represents a positive integer (that is larger than 0), otherwise false.
         */
        isStringPositiveInteger: function(value)
        {
          return !!value && tm.jquery.Utils.isNumeric(value) &&
              app.views.medications.MedicationUtils.isPositiveInteger(parseInt(value));
        },

        /**
         * @param {app.views.common.AppView} view
         * @returns {tm.jquery.Validator}
         */
        buildDefaultMinimumLengthStringValidator: function(view)
        {
          return new tm.jquery.Validator({
            errorMessage: view.getDictionary("field.value.is.invalid"),
            isValid: function(value)
            {
              return !value || value.trim().length >= 2;
            }
          })
        },

        /**
         * @param {app.views.medications.timeline.administration.dto.Administration} administration
         * @param {Boolean} numeratorOnly
         * @param {app.views.common.AppView} view
         * @returns {String}
         */
        buildAdministeredDoseDisplayString: function(administration, numeratorOnly, view)
        {
          var medicationUtils = app.views.medications.MedicationUtils;
          if (administration.getAdministeredDose())
          {
            //var dose = administration.administeredDose;
            //return dose.numerator ? app.views.medications.MedicationUtils.doubleToString(dose.numerator, 'n2') + ' ' + dose.numeratorUnit : '';
            var dose = administration.getAdministeredDose();
            var numeratorString = !tm.jquery.Utils.isEmpty(dose.getNumerator()) ?
                medicationUtils.getFormattedDecimalNumber(medicationUtils.doubleToString(dose.getNumerator(), 'n2')) + ' ' +
                medicationUtils.getFormattedUnit(dose.getNumeratorUnit(), view) :
                '';
            if (numeratorOnly)
            {
              return numeratorString;
            }
            var denominatorString = dose.getDenominator() ?
                medicationUtils.getFormattedDecimalNumber(medicationUtils.doubleToString(dose.getDenominator(), 'n2')) + ' ' +
                medicationUtils.getFormattedUnit(dose.getDenominatorUnit(), view) :
                '';
            return denominatorString ? (numeratorString + ' / ' + denominatorString) : numeratorString;
          }
          return null;
        },

        crateLabel: function(cls, text, padding, block)
        {
          return new tm.jquery.Label({
            cls: cls,
            text: text,
            style: block !== false ? 'display:block;' : '',
            padding: padding || padding === 0 ? padding : "5 0 0 0"
          });
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {app.views.medications.common.dto.Medication} selection
         * @param {boolean} enabled
         * @param {string|null} [cls=null]
         * @returns {tm.jquery.TypeaheadField}
         */
        createMedicationTypeaheadField: function(view, selection, enabled, cls)
        {
          return new tm.jquery.TypeaheadField({
            cls: cls,
            flex: tm.jquery.flexbox.item.Flex.create(1, 1, "auto"),
            scrollable: 'visible',
            placeholder: view.getDictionary("enter.three.chars.to.search.medication") + "...",
            displayProvider: function(medication)
            {
              return medication ? medication.getDisplayName() : null;
            },
            minLength: 3,
            mode: 'advanced',
            selection: selection,
            enabled: enabled,
            width: 'auto',
            items: 10000,
            matcher: function(item)
            {
              var cleanItem = item.replace('<strong>', '');
              cleanItem = cleanItem.replace('</strong>', '');
              var itemStrings = cleanItem.split(' ');
              var queryStrings = this.query.split(' ');
              for (var i = 0; i < queryStrings.length; i++)
              {
                var isAMatch = false;
                for (var j = 0; j < itemStrings.length; j++)
                {
                  if (itemStrings[j].indexOf('%') === itemStrings[j].length - 1)
                  {
                    if (itemStrings[j].toLowerCase() === queryStrings[i].toLowerCase())
                    {
                      isAMatch = true;
                      break;
                    }
                  }
                  else if (itemStrings[j].toLowerCase().indexOf(queryStrings[i].toLowerCase()) !== -1)
                  {
                    isAMatch = true;
                    break;
                  }
                }
                if (!isAMatch)
                {
                  return false
                }
              }
              return true;
            },
            highlighter: function(item, selectedItemData, query)
            {
              var queryStrings = this.query.split(' ');
              var highlightedItem = item;
              var replaceString = '';
              for (var i = 0; i < queryStrings.length; i++)
              {
                if (queryStrings[i])
                {
                  replaceString += queryStrings[i].replace(/[\-\[\]{}()*+?.,\\^$|#\s]/g, '\\$&');
                  if (i < queryStrings.length - 1)
                  {
                    replaceString += '|';
                  }
                }
              }

              if (query !== "")
              {
                highlightedItem = item.replace(new RegExp('(' + query + ')', 'ig'),
                    function($1, match)
                    {
                      return '<strong>' + match + '</strong>'
                    });
              }
              else
              {
                highlightedItem = item;
              }

              if (selectedItemData.active === false)
              {
                return "<span class='inactive'>" + highlightedItem + "</span>";
              }
              return highlightedItem;
            }
          });
        },

        createPerformerContainer: function(view, careProfessionals, presetCareProfessionalName)
        {
          var presetCareProfessional = this._getCareProfessionalAsPotentialPerformer(careProfessionals, presetCareProfessionalName);
          return new app.views.common.PerformerContainer({
            view: view,
            height: 35,
            dateEditable: false,
            withDetails: false,
            performDate: CurrentTime.get(),
            compositionEditor: presetCareProfessional,
            compositionDate: CurrentTime.get(),
            careProfessionals: careProfessionals,
            performer: presetCareProfessional,
            performerTitle: view.getDictionary("requested.by")
          });
        },

        createNumberField: function(formatting, width, cls)
        {
          if (formatting === "n0")
          {
            formatting = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 0};
          }
          else if (formatting === "n2")
          {
            formatting = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 2};
          }
          else if (formatting === "n3")
          {
            formatting = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: 3};
          }

          return new tm.jquery.NumberField({
            cls: cls,
            formatting: formatting,
            width: width,
            // @Override
            getDisplayValue: function(value)
            {
              return app.views.medications.MedicationUtils.safeFormatNumber(value, this.formatting);
            }
          });
        },

        /**
         * Formats the number via Globalize and the given format, unless the value is less than 1. In that case
         * it's rounded to the first non zero fraction and additional 3 digits, to make sure the precision isn't lost.
         * @param {Number|null} value
         * @param {String} formatting
         * @returns {String}
         */
        safeFormatNumber: function(value, formatting)
        {
          if (tm.jquery.Utils.isEmpty(value))
          {
            return "";
          }
          if (value < 1)
          {
            var splitNumber = value.toString().split(".");
            if (splitNumber.length === 2)
            {
              var decimals = splitNumber[1];
              var regexp = new RegExp('[1-9]');
              var indexOfFirstNonZero = regexp.exec(decimals).index;
              var maximumFractionDigits = indexOfFirstNonZero + 3;
              var format = {useGrouping: false, minimumFractionDigits: 0, maximumFractionDigits: maximumFractionDigits};
              return Globalize.formatNumber(value, format);
            }
          }
          return Globalize.formatNumber(value, formatting)
        },

        _getCareProfessionalAsPotentialPerformer: function(careProfessionals, careProfessionalName)
        {
          if (careProfessionalName)
          {
            for (var index = 0; index < careProfessionals.length; index++)
            {
              if (careProfessionals[index].name === careProfessionalName)
              {
                return careProfessionals[index];
              }
            }
          }
          return null;
        },

        getScrollbarWidth: function()
        {
          var outer = document.createElement("div");
          outer.style.visibility = "hidden";
          outer.style.width = "100px";
          outer.style.msOverflowStyle = "scrollbar";

          document.body.appendChild(outer);

          var widthNoScroll = outer.offsetWidth;
          outer.style.overflow = "scroll";

          var inner = document.createElement("div");
          inner.style.width = "100%";
          outer.appendChild(inner);

          var widthWithScroll = inner.offsetWidth;
          outer.parentNode.removeChild(outer);

          return widthNoScroll - widthWithScroll;
        },

        isScrollVisible: function(component)
        {
          if (component && component.getDom())
          {
            var scrollHeight = $(component.getDom())[0].scrollHeight;
            var height = $(component.getDom()).height();
            return scrollHeight > height + 1;
          }
          return false;
        },

        /**
         * Zero pads the given number to the specified amount of digits.
         * Function taken from http://stackoverflow.com/questions/10073699/pad-a-number-with-leading-zeros-in-javascript
         * @param {String|Number} number
         * @param {Number} digits
         * @returns {String}
         */
        padDigits: function(number, digits)
        {
          return new Array(Math.max(digits - String(number).length + 1, 0)).join(0) + number;
        },

        getIndexOfHourMinute: function(hourMinute, list)
        {
          for (var i = 0; i < list.length; i++)
          {
            if (list[i].hour === hourMinute.hour && list[i].minute === hourMinute.minute)
            {
              return i;
            }
          }
          return -1
        },

        /**
         * @param {Array<object>} timedDoseElements
         * @return {boolean} true if the elements represent a variable dose therapy which includes dose times.
         */
        isTherapyWithVariableDose: function(timedDoseElements)
        {
          return tm.jquery.Utils.isArray(timedDoseElements) && timedDoseElements.length > 0 &&
              timedDoseElements.every(
                  function hasDatelessDoseTime(element)
                  {
                    // timeDoseElements may or may not be properly cast from JSON, as a result of a missing jsClass
                    // representation one such case is when we load the data for the timeline or grid view.
                    return !!element.doseTime && !element.date && !tm.jquery.Utils.isString(element.timingDescription);
                  });
        },

        /**
         * @param {Array<object>} timedDoseElements
         * @return {boolean} true, if the elements contain dose date or timing description information, which means
         * there's a protocol that determines how the doses should be given.
         */
        isTherapyWithVariableDaysDose: function(timedDoseElements)
        {
          return tm.jquery.Utils.isArray(timedDoseElements) && timedDoseElements.length > 0 &&
              timedDoseElements.every(
                  function hasDoseDateOrTimingDescription(element)
                  {
                    // timeDoseElements may or may not be properly cast from JSON, as a result of a missing jsClass
                    // representation. One such case is when we load the data for the timeline or grid view.
                    return (!!element.doseTime && !!element.date) || tm.jquery.Utils.isString(element.timingDescription);
                  });
        },

        /**
         * @param {Array<object>} timedDoseElements
         * @return {boolean} true, if the elements represent a descriptive variable dose days representative of a discharge
         * protocol.
         */
        isTherapyWithDescriptiveVariableDaysDose: function(timedDoseElements)
        {
          return tm.jquery.Utils.isArray(timedDoseElements) && timedDoseElements.length > 0 &&
              timedDoseElements.every(
                  function hasOnlyTimingDescription(element)
                  {
                    return !element.doseTime && !element.date && tm.jquery.Utils.isString(element.timingDescription);
                  });
        },

        getUidWithoutVersion: function(uid)
        {
          return uid.substring(0, uid.indexOf("::"));
        },
        getNextLinkName: function(linkName) //A2 -> A3
        {
          var prefix = linkName.substring(0, 1);
          var number = linkName.substring(1, linkName.length);
          var nextNumber = Number(number) + 1;
          return prefix + nextNumber;
        },
        /**
         * @param {String} therapyLinkName
         * @param {Array<app.views.medications.common.dto.Therapy|OxygenTherapy>} therapies
         * @returns {boolean}
         */
        areOtherTherapiesLinkedToTherapy: function(therapyLinkName, therapies)
        {
          if (!therapyLinkName)
          {
            return false;
          }
          var nextLinkName = this.getNextLinkName(therapyLinkName);
          for (var i = 0; i < therapies.length; i++)
          {
            if (therapies[i].getLinkName() === nextLinkName)
            {
              return true;
            }
          }
          return false;
        },

        createPatientsReferenceWeightAndHeightHtml: function(view)
        {
          var html = "";
          var referenceData = new app.views.medications.common.patient.ViewBasedReferenceData({view: view});

          if (!!referenceData.getWeight())
          {
            var referenceWeightValueString =
                app.views.medications.MedicationUtils.doubleToString(referenceData.getWeight(), 'n3') +
                ' ' + view.getDictionary("kilogram.short");
            var bodySurfaceAreaString;

            if (!!referenceData.getHeight())
            {
              bodySurfaceAreaString =
                  app.views.medications.MedicationUtils.doubleToString(referenceData.getBodySurfaceArea(), 'n3') +
                  ' ' + view.getDictionary("square.metre.short");
            }

            /* the data attributes are used for automatic tests, since we don't use a component and can't set testAttribute */
            html = "<span style='text-transform: none; color: #63818D' data-type='weight'>" +
                view.getDictionary('reference.weight') + ": " +
                referenceWeightValueString + "</span>";

            if (bodySurfaceAreaString)
            {
              html += " - <span style='text-transform: none; color: #63818D' data-type='surface'>" +
                  view.getDictionary('body.surface') + ": " +
                  bodySurfaceAreaString + "</span>";
            }
          }

          return html;
        },

        getFirstOrNullTherapyChangeReason: function(map, actionEnum)
        {
          if (map.hasOwnProperty(actionEnum) && map[actionEnum].length > 0)
          {
            return new app.views.medications.common.dto.TherapyChangeReason({
              changeReason: map[actionEnum][0]
            });
          }
          return null;
        },

        calculateVariablePerDay: function(timedDoseElements)
        {
          if (timedDoseElements.isEmpty())
          {
            return null;
          }

          var dayDose = 0;
          timedDoseElements.forEach(function(item)
          {
            dayDose += item.doseElement.quantity;
          });
          return dayDose;
        },

        getFormattedDecimalNumber: function(value)
        {
          if (tm.jquery.Utils.isEmpty(value))
          {
            return value;
          }
          var delimiter = Globalize.cldr.main("numbers/symbols-numberSystem-latn/decimal");
          var restyledNumber = tm.jquery.Utils.escapeHtml(value.toString()); // numbers don't have regexp methods
          var splitRegex = new RegExp('[0-9]+(\\' + delimiter + '[0-9]+)', 'g');
          var floatNumbers = restyledNumber.match(splitRegex);
          if (!tm.jquery.Utils.isEmpty(floatNumbers))
          {
            for (var i = 0; i < floatNumbers.length; i++)
            {
              var splitNumber = floatNumbers[i].split(delimiter);
              if (splitNumber.length > 1)
              {
                restyledNumber = restyledNumber.replace(floatNumbers[i], splitNumber[0] + delimiter + '<span class="TextDataSmallerDecimal">' + splitNumber[1] + '</span>');
              }
            }
          }
          return restyledNumber;
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {Number|null} maxDosePercentage
         * @param {Boolean} [isForPatientDataContainer=false]
         * @returns {String}
         */
        createMaxDosePercentageInfoHtml: function(view, maxDosePercentage, isForPatientDataContainer)
        {
          if (!maxDosePercentage)
          {
            return '';
          }
          var maxDoseContainer = new tm.jquery.Container({
            layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
          });
          var maxDoseWarningCls = null;

          if (!isForPatientDataContainer)
          {
            maxDoseWarningCls =
                app.views.medications.warnings.WarningsHelpers.getImageClsForMaximumDosePercentage(maxDosePercentage);
          }
          else if (maxDosePercentage >= 100)
          {
            maxDoseWarningCls = 'cumulative-percentage-high-icon';
          }

          var maxDoseWarningImage = new tm.jquery.Image({
            cls: maxDoseWarningCls,
            margin: "0 5 0 0",
            width: 16,
            height: 16,
            flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px")
          });

          var html = !!isForPatientDataContainer ?
              '<span class="TextLabel">' + view.getDictionary("cumulative.dose.of.antipsychotic") + ': ' + '</span>' +
              '<span class="TextData">' + maxDosePercentage + '</span>' + '<span class="TextUnit">' + '%' + '</span>' :
              '<span class="From TextData">' +
              tm.jquery.Utils.formatMessage(
                  '{0}{1}',
                  maxDosePercentage,
                  view.getDictionary('of.maximum.recommended.dose')) +
              '</span>';

          if (!isForPatientDataContainer || isForPatientDataContainer && maxDosePercentage >= 100)
          {
            maxDoseContainer.add(maxDoseWarningImage);
          }

          maxDoseContainer.add(new tm.jquery.Label({
            html: html,
            flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
          }));

          maxDoseContainer.doRender();
          return maxDoseContainer.getRenderToElement().innerHTML;
        },

        /**
         * @param {String} iconCls
         * @param {String} description
         * @returns {String}
         */
        buildIconDescriptionRowHtml: function(iconCls, description)
        {
          var descriptionRow = new tm.jquery.Container({
            layout: tm.jquery.HFlexboxLayout.create("flex-start", "center")
          });

          var descriptionIcon = new tm.jquery.Image({
            cls: iconCls,
            width: 16,
            height: 16,
            flex: tm.jquery.flexbox.item.Flex.create(0, 0, "16px")
          });

          var descriptionLabel = new tm.jquery.Label({
            html: description,
            cls: "TextData",
            flex: tm.jquery.flexbox.item.Flex.create(0, 0, "auto")
          });

          descriptionRow.add(descriptionIcon);
          descriptionRow.add(descriptionLabel);

          descriptionRow.doRender();
          return descriptionRow.getRenderToElement().innerHTML;
        },

        getFormattedUnit: function(unit, view)
        {
          return !tm.jquery.Utils.isEmpty(unit) ?
              unit.replaceAll("ml", view.getDictionary("millilitre.short"))
                  .replaceAll('l/min', view.getDictionary("liter.per.minute.short")) :
              unit;
        },

        /**
         * @param {string} unit
         * @return {boolean}
         */
        isUnitSupportedForDosageCalculation: function(unit)
        {
          var acceptableUnits = ["g", "mg", "microgram", "µg", "nanogram", "i.e.", "mmol", "unit", "мг"];
          return acceptableUnits.indexOf(unit) > -1;
        },

        getDosageCalculationUnitOptions: function(view, doseUnit, patientHeight)
        {
          var enums = app.views.medications.TherapyEnums;
          var options = [];
          doseUnit = tm.jquery.Utils.isEmpty(doseUnit) ? "" : doseUnit;
          options.push({
            id: 0,
            displayUnit: doseUnit + "/" + view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.KG).getDisplayName()
            + "/" + view.getDictionary("dose.short"),
            doseUnit: doseUnit,
            patientUnit: enums.knownUnitType.KG
          });
          if (!tm.jquery.Utils.isEmpty(patientHeight))
          {
            options.push({
              id: 1,
              displayUnit: doseUnit + "/" + view.getUnitsHolder().findKnownUnitByName(enums.knownUnitType.M2).getDisplayName()
              + "/" + view.getDictionary("dose.short"),
              doseUnit: doseUnit,
              patientUnit: enums.knownUnitType.M2
            });
          }
          return options;
        },

        /**
         * Use for universal therapies only!
         * @param {app.views.medications.common.dto.Therapy} therapy
         * @returns {app.views.medications.common.dto.MedicationData}
         */
        getMedicationDataFromSimpleTherapy: function(therapy)
        {
          var medication = {
            id: null,
            name: therapy.medication.name,
            medicationType: app.views.medications.TherapyEnums.medicationTypeEnum.MEDICATION
          };
          var medicationIngredients = [
            new app.views.medications.common.dto.MedicationIngredient({
              strengthNumeratorUnit: therapy.quantityUnit,
              strengthDenominatorUnit: therapy.quantityDenominatorUnit
            })
          ];
          /**
           * Takes dose from therapy and sets numerator and denominator, since we only need the ratio between them.
           * Keep in mind that this may not be the same strength as defined during prescription. If the need to know
           * the exact prescribed strength ever arises, we will need to implement saving and reading this data.
           */
          var prescribingDose = new app.views.medications.common.dto.PrescribingDose({
            numerator: therapy.getDoseElement() ? therapy.getDoseElement().quantity : null,
            numeratorUnit: therapy.quantityUnit,
            denominator: therapy.getDoseElement() ? therapy.getDoseElement().quantityDenominator : null,
            denominatorUnit: therapy.quantityDenominatorUnit
          });
          return app.views.medications.common.dto.MedicationData.fromJson({
            doseForm: therapy.doseForm,
            medication: medication,
            medicationIngredients: medicationIngredients,
            prescribingDose: prescribingDose
          });
        },

        /**
         * Use for universal therapies only!
         * @param {app.views.common.AppView} view
         * @param {app.views.medications.common.dto.Therapy} therapy
         * @param {Number} [infusionIndex=null]
         * @returns {app.views.medications.common.dto.MedicationData}
         */
        getMedicationDataFromComplexTherapy: function(view, therapy, infusionIndex)
        {
          var mlUnit = view.getUnitsHolder().findKnownUnitByName(app.views.medications.TherapyEnums.knownUnitType.ML);

          var index = !tm.jquery.Utils.isEmpty(infusionIndex) ? infusionIndex : 0;
          var medication = new app.views.medications.common.dto.Medication({
            id: null,
            name: therapy.ingredientsList[index].medication.getName(),
            medicationType: therapy.ingredientsList[index].medication.getMedicationType() ||
                app.views.medications.TherapyEnums.medicationTypeEnum.MEDICATION
          });
          var medicationIngredient = new app.views.medications.common.dto.MedicationIngredient();
          var prescribingDose = new app.views.medications.common.dto.PrescribingDose();

          /**
           * For diluents, only mL as numerator unit must be set to the ingredient as well as the prescribing dose for
           * medication data.
           */
          if (medication.getMedicationType() === app.views.medications.TherapyEnums.medicationTypeEnum.DILUENT)
          {
            medicationIngredient.setStrengthNumeratorUnit(mlUnit.getDisplayName());
            prescribingDose.setNumeratorUnit(mlUnit.getDisplayName());
          }
          else
          {
            medicationIngredient.setStrengthNumeratorUnit(therapy.ingredientsList[index].quantityUnit);
            medicationIngredient.setStrengthDenominatorUnit(mlUnit.getDisplayName());

            /**
             * Takes dose from therapy and sets numerator and denominator, since we only need the ratio between them.
             * Keep in mind that this may not be the same strength as defined during prescription. If the need to know
             * the exact prescribed strength ever arises, we will need to implement saving and reading this data.
             */
            prescribingDose.setNumerator(therapy.ingredientsList[index].quantity || null);
            prescribingDose.setNumeratorUnit(therapy.ingredientsList[index].quantityUnit);
            prescribingDose.setDenominator(therapy.ingredientsList[index].quantityDenominator || null);
            prescribingDose.setDenominatorUnit(mlUnit.getDisplayName())
          }

          /**
           * In some instances (for example continuous infusions), we cannot reliably reconstruct the medication data from
           * the therapy. The resulting prescribing dose may be in an illegal state (missing numerator unit), so this is a
           * safety fall-back.
           */
          if (!prescribingDose.getNumeratorUnit())
          {
            prescribingDose.setNumeratorUnit(mlUnit.getDisplayName());
            prescribingDose.setDenominatorUnit(null);
          }

          return new app.views.medications.common.dto.MedicationData({
            doseForm: new app.views.medications.common.dto.DoseForm(therapy.ingredientsList[index].doseForm),
            medication: medication,
            medicationIngredients: [medicationIngredient],
            prescribingDose: prescribingDose
          });
        },

        /**
         * Attaches an extra save order button to the dialog, changes the confirm button text and configures the button handlers
         * to correctly call the extended version of the dialog content's processResultData as required.
         * {@see app.views.medications.ordering.outpatient.OutpatientOrderingContainer}
         *
         * @param {app.views.common.AppView|tm.views.medications.TherapyView} view A view, for support method access.
         * @param {tm.jquery.Dialog} dialog The dialog
         * @param {function} dialogResultCallback The dialog result function to be called by confirm buttons.
         */
        attachOutpatientOrderingDialogFooterButtons: function(view, dialog, dialogResultCallback)
        {
          var dialogFooter = dialog.getBody().getFooter();
          var confirmButton = dialogFooter.getConfirmButton();

          var saveButton = new tm.jquery.Button({
            text: view.getDictionary("save"),
            handler: createConfirmButtonHandler(true)
          });

          confirmButton.setText(view.getDictionary("do.order"));
          confirmButton.setHandler(view.isSwingApplication() ? createConfirmButtonHandler(false) : null);
          confirmButton.setHidden(!view.isSwingApplication()); // we can only save, not actually place orders, outside swing
          dialogFooter.rightButtons.unshift(saveButton);

          /** support functions **/

          function createConfirmButtonHandler(saveOnly)
          {
            return function()
            {
              if (tm.jquery.Utils.isFunction(dialog.getContent().processResultData))
              {
                enableAllRightButtons(false);

                dialog.getContent().processResultData(function(resultData)
                    {
                      if (resultData instanceof app.views.common.AppResultData)
                      {
                        if (resultData.isSuccess())
                        {
                          dialogResultCallback(resultData);
                          dialog.hide();
                        }
                        else
                        {
                          setTimeout(function()
                          {
                            enableAllRightButtons(true);
                          }, 500);
                        }
                      }
                    },
                    saveOnly);
              }
            };
          }

          function enableAllRightButtons(enable)
          {
            dialogFooter.rightButtons.forEach(function(button)
            {
              button.setEnabled(enable);
            });
          }
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {String} message
         * @param {number} [height = 300]
         * @param {number} [width = 160]
         * @returns {tm.jquery.Promise}
         */
        openConfirmationWithWarningDialog: function(view, message, width, height)
        {
          var appFactory = view.getAppFactory();
          var deferred = new tm.jquery.Deferred;
          var warningDialog = appFactory.createConfirmSystemDialog(
              message,
              resultCallback,
              width ? width : 300,
              height ? height : 160);

          warningDialog.show();

          function resultCallback(confirm)
          {
            deferred.resolve(confirm);
          }

          return deferred.promise();
        },

        /**
         * Based on the ES6 polyfill.
         * {@link https://developer.mozilla.org/en/docs/Web/JavaScript/Reference/Global_Objects/Array/find}
         * @param {Array<Object>} inArray
         * @param {function} predicate
         * @returns {*}
         */
        findInArray: function(inArray, predicate)
        {
          if (!inArray || !predicate)
          {
            return undefined;
          }

          if (typeof predicate !== 'function')
          {
            throw new TypeError('predicate must be a function');
          }

          // If thisArg was supplied, let T be thisArg; else let T be undefined.
          var thisArg = arguments[2];
          var k = 0;

          while (k < inArray.length)
          {
            var kValue = inArray[k];
            if (predicate.call(thisArg, kValue, k, inArray))
            {
              return kValue;
            }
            k++;
          }

          return undefined;
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {string} title
         * @param {Date|null} updatedDate
         * @returns a common format of the html title for entities which require the last update date to be shown as
         * part of the title, when available. Otherwise simply returns the title.
         */
        createUpdatableEntityTitle: function(view, title, updatedDate)
        {
          return !!title && tm.jquery.Utils.isDate(updatedDate) ?
              tm.jquery.Utils.formatMessage(
                  '{0}&nbsp({1}:&nbsp{2})',
                  tm.jquery.Utils.escapeHtml(title),
                  view.getDictionary('updated').toLowerCase(),
                  view.getDisplayableValue(updatedDate, "short.date")) :
              !!title ? tm.jquery.Utils.escapeHtml(title) : null;
        },

        /**
         * @param {app.views.common.AppView} view
         * @returns {String}
         */
        createConsecutiveDayLabel: function(view)
        {
          return view.isAntimicrobialDaysCountStartsWithOne() ?
              view.getDictionary('day.of.antimicrobials') :
              view.getDictionary('completed.days.of.antimicrobials');
        }
      }
    }
);
