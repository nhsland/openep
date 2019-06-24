Class.define('app.views.medications.MedicationTimingUtils', 'tm.jquery.Object', {

      /** statics */
      statics: {
        daysOfWeek: {
          "SUNDAY": 0,
          "MONDAY": 1,
          "TUESDAY": 2,
          "WEDNESDAY": 3,
          "THURSDAY": 4,
          "FRIDAY": 5,
          "SATURDAY": 6
        },
        titrationIntervalHours: {
          BLOOD_SUGAR: {interval: 48, tick: 4},
          MAP: {interval: 4, tick: 1},
          INR: {interval: 120, tick: 6},
          APTTR: {interval: 120, tick: 6}
        },
        /** private methods */
        _getFrequencyTypeAdministrationTimes: function(administrationTiming, frequencyType)
        {
          if (administrationTiming)
          {
            if (frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.DAILY_COUNT)
            {
              return this._getFrequencyTypeKeyAdministrationTimes(administrationTiming, "X");
            }
            else if (frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
            {
              return this._getFrequencyTypeKeyAdministrationTimes(administrationTiming, "H");
            }
          }
          return [];
        },

        _getFrequencyTypeKeyAdministrationTimes: function(administrationTiming, typeString)
        {
          var administrationTimesOfDay = [];
          for (var i = 0; i < administrationTiming.timestampsList.length; i++)
          {
            var isFrequencyOfType = this._isFrequencyOfType(administrationTiming.timestampsList[i].frequency, typeString);
            if (isFrequencyOfType)
            {
              var administrationTimesForFrequency = administrationTiming.timestampsList[i].timesList;
              this._pushAdministrationTimesToList(administrationTimesForFrequency, administrationTimesOfDay);
            }
          }
          this._sortAdministrationTimesOfDay(administrationTimesOfDay);
          return administrationTimesOfDay;
        },

        _isFrequencyOfType: function(frequency, type)
        {
          var frequencyType = frequency.slice(-1);
          return type ? type === frequencyType : true;
        },

        _pushAdministrationTimesToList: function(timesList, list)
        {
          for (var j = 0; j < timesList.length; j++)
          {
            list.push({
              hour: timesList[j].hour,
              minute: timesList[j].minute
            });
          }
        },

        _sortAdministrationTimesOfDay: function(list)
        {
          list.sort(function(a, b)
          {
            var minutesA = a.hour * 60 + a.minute;
            var minutesB = b.hour * 60 + b.minute;
            return minutesA - minutesB;
          });
        },

        _adjustToDaysOfWeek: function(date, daysOfWeek)
        {
          var daysToFirstSuitableDayOfWeek = this._getDaysToFirstSuitableDayOfWeek(date, daysOfWeek);
          date.setDate(date.getDate() + daysToFirstSuitableDayOfWeek);
          return date;
        },

        _getDaysToFirstSuitableDayOfWeek: function(startDate, daysOfWeek)
        {
          if (daysOfWeek == null || daysOfWeek.length === 0 || daysOfWeek.length === 7)
          {
            return 0;
          }
          var dayCounter = 0;
          var dayOfWeekIndex = startDate.getDay();
          for (var i = startDate.getDay(); i < startDate.getDay() + 7; i++)
          {
            for (var j = 0; j < daysOfWeek.length; j++)
            {
              var suitableDayOfWeekIndex = app.views.medications.MedicationTimingUtils._getDayOfWeekIndex(daysOfWeek[j]);
              if (dayOfWeekIndex === suitableDayOfWeekIndex)
              {
                return dayCounter;
              }
            }
            dayCounter++;
            dayOfWeekIndex = (dayOfWeekIndex + 1) % 7;
          }
          //should never happen
          return 0;
        },

        _getDayOfWeekIndex: function(day)
        {
          return this.daysOfWeek[day];
        },

        /** public methods */
        getFrequencyAdministrationTimesOfDay: function(administrationTiming, frequency)
        {
          if (administrationTiming)
          {
            for (var i = 0; i < administrationTiming.timestampsList.length; i++)
            {
              if (administrationTiming.timestampsList[i].frequency === frequency)
              {
                var administrationTimesOfDay = administrationTiming.timestampsList[i].timesList;
                var administrationHourMinutes = [];
                for (var j = 0; j < administrationTimesOfDay.length; j++)
                {
                  administrationHourMinutes.push({
                    hour: administrationTimesOfDay[j].hour,
                    minute: administrationTimesOfDay[j].minute
                  });
                }
                return administrationHourMinutes;
              }
            }
          }
          return [];
        },

        getFrequencyTimingPattern: function(administrationTiming, frequencyKey, frequencyType)
        {
          var enums = app.views.medications.TherapyEnums;
          var administrationTimes = app.views.medications.MedicationTimingUtils.getFrequencyAdministrationTimesOfDay(
              administrationTiming,
              frequencyKey);

          if (administrationTimes.length <= 0)
          {
            var allTimes = this._getFrequencyTypeAdministrationTimes(administrationTiming, frequencyType);
            var oncePerDayFrequencyKey = frequencyKey === "1X" || frequencyKey === "24H";

            if (frequencyType === enums.dosingFrequencyTypeEnum.ONCE_THEN_EX) // stat dose
            {
              var nextAsapAdministrationTime =
                  app.views.medications.MedicationTimingUtils.getNextAdministrationTimestampForAsap(frequencyType);
              administrationTimes.push({
                hour: nextAsapAdministrationTime.getHours(),
                minute: nextAsapAdministrationTime.getMinutes()
              });
            }
            else if (oncePerDayFrequencyKey && allTimes.length > 0)
            {
              var now = CurrentTime.get();
              var currentTimeInMinutes = now.getHours() * 60 + now.getMinutes();
              var nextAdministration = null;
              for (var i = 0; i < allTimes.length; i++)
              {
                var administrationTimeInMinutes =
                    allTimes[i].hour * 60 + allTimes[i].minute;
                if (administrationTimeInMinutes >= currentTimeInMinutes)
                {
                  nextAdministration = allTimes[i];
                  break;
                }
              }

              if (nextAdministration == null)
              {
                nextAdministration = allTimes[0];
              }
              administrationTimes.push(nextAdministration);
            }
            else if (frequencyKey != null) //nonstandard timing
            {
              var firstAdministration =
              {
                hour: 9,
                minute: 0
              };
              administrationTimes.push(firstAdministration);

              var betweenDoses = 24;
              var dailyCount = 1;
              if (frequencyType === enums.dosingFrequencyTypeEnum.DAILY_COUNT)
              {
                dailyCount = frequencyKey.substring(0, frequencyKey.length - 1);
                betweenDoses = 24 / dailyCount;
              }
              else if (frequencyType === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
              {
                betweenDoses = this._getHoursBetweenDosesFromFrequencyKey(frequencyKey);
                dailyCount = 24 / betweenDoses;
              }

              for (var j = 1; j < dailyCount; j++)
              {
                var nextHour = (firstAdministration.hour + j * betweenDoses) % 24;
                administrationTimes.push({
                  hour: Math.floor(nextHour),
                  minute: nextHour % 1 * 60
                });
              }
            }
          }
          return administrationTimes;
        },

        _getHoursBetweenDosesFromFrequencyKey: function(frequencyKey)
        {
          return Number(frequencyKey.substring(0, frequencyKey.length - 1));
        },

        getPatternForFrequencyBetweenHours: function(firstHourMinute, frequencyKey)
        {
          var hoursBetweenDoses = this._getHoursBetweenDosesFromFrequencyKey(frequencyKey);
          var pattern = [];

          var now = CurrentTime.get();
          var time = new Date(now.getFullYear(), now.getMonth(), now.getDate(), firstHourMinute.hour, firstHourMinute.minute, 0, 0);
          var endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), firstHourMinute.hour + 24, firstHourMinute.minute, 0, 0);
          while (time < endTime)
          {
            pattern.push(
                {
                  hour: time.getHours(),
                  minute: time.getMinutes()
                });
            time = new Date(time.getTime() + hoursBetweenDoses * 60 * 60 * 1000);
          }
          return pattern;
        },

        getNextTimeFromPattern: function(pattern, daysOfWeek)
        {
          var day = CurrentTime.get();
          var now = CurrentTime.get();
          var nextTime = null;
          var index = -1;

          if (pattern && pattern.length > 0)
          {
            pattern.sort(function(first, second)
            {
              var firstInMinutes = first.hour * 60 + first.minute;
              var secondInMinutes = second.hour * 60 + second.minute;
              if (firstInMinutes < secondInMinutes)
              {
                return -1;
              }
              if (firstInMinutes > secondInMinutes)
              {
                return 1;
              }
              return 0;
            });

            while (nextTime == null || nextTime < now)
            {
              if (index < pattern.length - 1)
              {
                index++;
              }
              else
              {
                index = 0;
                day = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1, day.getHours(), day.getMinutes(), 0, 0);
              }
              nextTime = new Date(day.getFullYear(), day.getMonth(), day.getDate(), pattern[index].hour, pattern[index].minute, 0, 0);
              nextTime = this._adjustToDaysOfWeek(nextTime, daysOfWeek);
            }
            return {
              hourMinute: pattern[index],
              time: nextTime
            }
          }
          return null;
        },

        hourMinuteToString: function(hour, minute)
        {
          return hour + ":" + app.views.medications.MedicationUtils.pad(minute, 2);
        },

        hourMinuteToDate: function(hourMinute)
        {
          if (hourMinute)
          {
            var now = CurrentTime.get();
            return new Date(now.getFullYear(), now.getMonth(), now.getDate(), hourMinute.hour, hourMinute.minute, 0, 0);
          }
          return null;
        },

        getDateWithoutYearDisplay: function(view, date)
        {
          var dateDisplay = view.getDisplayableValue(new Date(date), "short.date");
          dateDisplay = dateDisplay.substring(0, dateDisplay.length - 5);
          return dateDisplay;
        },

        getDayOfWeekDisplay: function(view, day, onlyFirstThreeLetters)
        {
          var dayOfWeekString = view.getDictionary("DayOfWeek." + day);
          return onlyFirstThreeLetters ? dayOfWeekString.substring(0, 3) : dayOfWeekString;
        },

        /**
         * Calculates next allowed administration time for "PRN" (when needed) therapies
         * @return {Date|null}
         */
        getNextAllowedAdministrationTimeForPRN: function(dosingFrequency, timestamp)
        {
          var enums = app.views.medications.TherapyEnums;
          if (dosingFrequency)
          {
            if (dosingFrequency.type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
            {
              return new Date(timestamp.getTime() + dosingFrequency.value * 60 * 60 * 1000)
            }
            if (dosingFrequency.type === enums.dosingFrequencyTypeEnum.DAILY_COUNT)
            {
              var hoursUntilNextDose = 24 / dosingFrequency.value;
              return new Date(timestamp.getTime() + hoursUntilNextDose * 60 * 60 * 1000)
            }
          }
          return null;
        },

        /**
         * @return {Date}
         */
        getNextAdministrationTimestampForVario: function(timedDoseElements)
        {
          if (!timedDoseElements || !app.views.medications.MedicationUtils.isTherapyWithVariableDose(timedDoseElements))
          {
            return null;
          }

          var now = CurrentTime.get();
          var day = CurrentTime.get();
          var index = 0;
          var doseTime = timedDoseElements[index].doseTime;
          var administrationTimestamp = new Date(day.getFullYear(), day.getMonth(), day.getDate(), doseTime.hour, doseTime.minute, 0, 0);
          while (administrationTimestamp < now)
          {
            if (index < timedDoseElements.length - 1)
            {
              index++;
            }
            else
            {
              index = 0;
              day = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1, day.getHours(), day.getMinutes(), 0, 0);
            }
            doseTime = timedDoseElements[index].doseTime;
            administrationTimestamp = new Date(day.getFullYear(), day.getMonth(), day.getDate(), doseTime.hour, doseTime.minute, 0, 0);
          }
          return administrationTimestamp;
        },

        /**
         * @return {Date}
         */
        getNextAdministrationTimestampForVarioWithRate: function(timedDoseElements)
        {
          if (!timedDoseElements)
          {
            return null;
          }
          var now = CurrentTime.get();
          var doseTime = timedDoseElements[0].doseTime;
          var administrationTimestamp =
              new Date(now.getFullYear(), now.getMonth(), now.getDate(), doseTime.hour, doseTime.minute, 0, 0);
          if (administrationTimestamp < now)
          {
            administrationTimestamp.setDate(administrationTimestamp.getDate() + 1);
          }
          return administrationTimestamp;
        },

        /**
         * Returns the 'as soon as possible' date for the next administration. Normally, this is a future date close to the
         * current date and time, which should allow for enough time to finish the order and still enable a quick edit in
         * case of mistakes. Stat doses, however, should never be prescribed in the future, as the edit operation of a
         * planned task in the future results in an additional administration task. In such cases the edit should be
         * prevented.
         * @param {string} frequencyType from {@link app.views.medications.TherapyEnums.dosingFrequencyTypeEnum}
         * @return {Date}
         */
        getNextAdministrationTimestampForAsap: function(frequencyType)
        {
          return frequencyType === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.ONCE_THEN_EX ?
              CurrentTime.get() :
              app.views.medications.MedicationTimingUtils.getTimestampRoundedUp(
                  (moment(CurrentTime.get()).add('minutes', 2)).toDate(),
                  5);
        },

        /**
         * @param {number} timestamp
         * @param {number} roundToMinutes
         * @return {Date}
         */
        getTimestampRoundedUp: function(timestamp, roundToMinutes)
        {
          var date = new Date(timestamp);
          while (date.getMinutes() % roundToMinutes != 0)
          {
            date.setMinutes(date.getMinutes() + 1);
          }
          return date;
        },

        getFrequencyKey: function(dosingFrequency)
        {
          var enums = app.views.medications.TherapyEnums;
          if (dosingFrequency)
          {
            if (dosingFrequency.type === enums.dosingFrequencyTypeEnum.BETWEEN_DOSES)
            {
              return dosingFrequency.value + 'H';
            }
            if (dosingFrequency.type === enums.dosingFrequencyTypeEnum.DAILY_COUNT)
            {
              return dosingFrequency.value + 'X';
            }
            return dosingFrequency.type;
          }
          return null;
        },
        getWorkdayInterval: function(roundsInterval, when)
        {
          if (!roundsInterval)
          {
            return null;
          }
          var todaysRoundsStart = new Date(when.getFullYear(), when.getMonth(), when.getDate(), roundsInterval.startHour, roundsInterval.startMinute, 0, 0);

          if (when > todaysRoundsStart)
          {
            //todays workday
            return {
              start: todaysRoundsStart,
              end: new Date(todaysRoundsStart.getTime() + 24 * 60 * 60 * 1000)
            };
          }
          else
          {
            //yesterdays workday
            return {
              start: new Date(todaysRoundsStart.getTime() - 24 * 60 * 60 * 1000),
              end: new Date(todaysRoundsStart)
            };
          }
        },
        checkEditAllowed: function(therapy, view)
        {
          var therapyHasAlreadyStarted = new Date(therapy.start) < CurrentTime.get();
          var isOnlyOnce = therapy.dosingFrequency
              && therapy.dosingFrequency.type === app.views.medications.TherapyEnums.dosingFrequencyTypeEnum.ONCE_THEN_EX;
          if (therapyHasAlreadyStarted && isOnlyOnce)
          {
            view.getAppFactory().createWarningSystemDialog(view.getDictionary('cannot.edit.past.once.only.therapy'), 520, 120).show();
            return false;
          }
          return true;
        },
        /**
         * Returns the interval length in hours for the specified titration type.
         * @param {String} type app.views.medications.TherapyEnums.therapyTitrationTypeEnum
         * @returns {Integer} Interval length in hours.
         */
        getTitrationIntervalHoursByType: function(type)
        {
          var intervalEnums = app.views.medications.MedicationTimingUtils.titrationIntervalHours;

          if (intervalEnums.hasOwnProperty(type))
          {
            return intervalEnums[type].interval;
          }
          return 24;
        },

        /**
         * Returns the interval length in hours for the specified titration type.
         * @param {String} type (app.views.medications.TherapyEnums.therapyTitrationTypeEnum)
         * @returns {Integer} Interval length in hours.
         */
        getTitrationTickIntervalByType: function(type)
        {
          var intervalEnums = app.views.medications.MedicationTimingUtils.titrationIntervalHours;

          if (intervalEnums.hasOwnProperty(type))
          {
            return intervalEnums[type].tick;
          }
          return 24;
        },

        /**
         * Returns the offset in minutes for the specified titration type.
         * @param {String} type (app.views.medications.TherapyEnums.therapyTitrationTypeEnum)
         * @returns {Integer} Titration offset in minutes.
         */
        getTitrationOffsetMinutesByType: function(type)
        {
          return app.views.medications.MedicationTimingUtils.getTitrationIntervalHoursByType(type) / 8 * 60;
        },

        /**
         * @param {Date} date
         * @returns {number}
         */
        getUtcTime: function(date)
        {
          return Date.UTC(date.getFullYear(), date.getMonth(), date.getDate(), date.getHours(), date.getMinutes());
        },

        /**
         * @param {Date} date
         * @param {app.views.common.AppView} view
         * @returns {string}
         */
        getFriendlyDateDisplayableValue: function(date, view)
        {
          var today = new Date();
          var yesterday = new Date();
          var tomorrow = new Date();

          yesterday.setDate(yesterday.getDate() - 1);
          tomorrow.setDate(tomorrow.getDate() + 1);
          yesterday.setHours(0, 0, 0, 0);
          today.setHours(0, 0, 0, 0);
          tomorrow.setHours(0, 0, 0, 0);

          var friendlyDate = new Date(date);
          friendlyDate.setHours(0, 0, 0, 0);

          if (today.getTime() === friendlyDate.getTime())
          {
            return view.getDictionary("today");
          }
          else if (yesterday.getTime() === friendlyDate.getTime())
          {
            return view.getDictionary("yesterday");

          }
          else if (tomorrow.getTime() === friendlyDate.getTime())
          {
            return view.getDictionary("tomorrow");
          }
          else
          {
            if (view.getViewLanguage() === "en")
            {
              return Globalize.formatDate(friendlyDate, {skeleton: "MMMd"});
            }
            else
            {
              return Globalize.formatDate(friendlyDate, {skeleton: "Md"});
            }
          }
        },

        /**
         * @param {Array<{date: Date, doseTime: { hour: Date, minute: Date}}>} timedDoseElements
         * @returns {{start: *, end: *}}
         */
        getVariableDaysTherapyInterval: function(timedDoseElements)
        {
          var start = null;
          var end = null;

          if (app.views.medications.MedicationUtils.isTherapyWithDescriptiveVariableDaysDose(timedDoseElements))
          {
            return {start: start, end: end};
          }

          for (var i = 0; i < timedDoseElements.length; i++)
          {
            var timedDoseElement = timedDoseElements[i];
            var date = new Date(timedDoseElement.date);
            var applicationTimestamp =
                new Date(
                    date.getFullYear(),
                    date.getMonth(),
                    date.getDate(),
                    timedDoseElement.doseTime.hour,
                    timedDoseElement.doseTime.minute);
            if (end == null || end < applicationTimestamp)
            {
              end = applicationTimestamp;
            }
            if (applicationTimestamp > CurrentTime.get() && (start == null || start > applicationTimestamp))
            {
              start = applicationTimestamp;
            }
          }
          return {start: start, end: end};
        },

        /**
         * @param {app.views.common.AppView} view
         * @param {Object} administration
         * @returns {string}
         */
        getFormattedAdministrationPlannedTime: function(view, administration)
        {
          return '<span class="PlannedAdministrationTime TextLabel MedicationLabel">' +
              view.getDictionary("planned.time") + '&nbsp;</span>' + '<span class="TextData">' +
              view.getDisplayableValue(new Date(administration.plannedTime), "short.date.time") + '</span>';
        }
      }
    }
);
