/**
 * Represents a collection of pharmacist reviews grouped by the date of creation, for a custom number of past days from
 * the current date (included). The purpose of this class is to reorganize the retrieved backend data into a model that
 * supports the functionality of {@link app.views.medications.pharmacists.ReviewView}.
 */
Class.define('app.views.medications.pharmacists.PharmacistMedicationReviewsByDate', 'tm.jquery.Object', {
  /** @type number */
  numberOfDays: NaN,
  /** @type Array<app.views.medications.pharmacists.dto.PharmacistMedicationReview> */
  reviews: null,

  /** @type tm.jquery.HashMap */
  _reviewsByDateMap: null,
  /** @type Date|null */
  _lastConfirmedReviewDate: null,

  /**
   * @param {number} config.numberOfDays
   * @param {Array<app.views.medications.pharmacists.dto.PharmacistMedicationReview>} config.reviews
   * @constructor
   */
  Constructor: function(config)
  {
    this.callSuper(config);

    this.reviews = tm.jquery.Utils.isArray(this.reviews) ? this.reviews : [];
    this._reviewsByDateMap = new tm.jquery.HashMap();

    if (typeof this.numberOfDays !== "number" || this.numberOfDays < 1)
    {
      throw new Error('numberOfDays is not a valid number or is less than 1');
    }

    this._mapReviewData();
  },

  /**
   * Returns the latest date of the collection, which was the start of the current day at the time of construction. Might
   * actually be older if the data is stale or retrieved close to midnight.
   * @return {Date}
   */
  getLatestDate: function()
  {
    return new Date(this._reviewsByDateMap.keys()[0]);
  },

  /**
   * Returns the list of reviews for the first available date, which should be the current day, if the data isn't stale
   * or retrieved close to midnight.
   * @return {Array<app.views.medications.pharmacists.dto.PharmacistMedicationReview>}
   */
  getReviewsForLatestDate: function()
  {
    return this._reviewsByDateMap.get(this._reviewsByDateMap.keys()[0]);
  },

  /**
   * Used to retrieve the reviews for a given date. Throws an error if the passed date is outside of the given data bounds
   * used at construction time. Recommended to be used in conjunction with {@link getAvailablePastDates}.
   * @param {Date} date representing the start of requested day
   * @return {Array<app.views.medications.pharmacists.dto.PharmacistMedicationReview>}
   */
  getReviewsForDate: function(date)
  {
    var dateMillis = date.getTime();

    if (!this._reviewsByDateMap.containsKey(dateMillis))
    {
      throw new Error(tm.jquery.Utils.formatMessage('Invalid review date ({0}).', date.toLocaleDateString()));
    }

    return this._reviewsByDateMap.get(dateMillis);
  },

  /**
   * Returns a list of past days for this collection - that is without the latest date, which is considered as today.
   * @return {Array<Date>}
   */
  getAvailablePastDates: function()
  {
    return this._reviewsByDateMap
        .keys()
        .filter(function skipToday(date, index)
        {
          return index > 0; // filter by index to prevent issues with stale data used over midnight
        })
        .map(function toDate(timestamp)
        {
          return new Date(timestamp);
        });
  },

  /**
   * Returns true if the last confirmed report date (non-draft) is older than the time of the last prescription change,
   * and the user has the required authority. If no prescription changes were made, or the last authorized review is newer
   * than the last change, or the last prescription change is older than the available review data, a new review is not
   * required. In the case of the last, it's due to the fact that we can't reliably determine that the change is still
   * relevant.
   * @param {Date|null} lastPrescriptionChangeDate
   * @param {app.views.medications.TherapyAuthority} therapyAuthority
   */
  isNewReviewRequired: function(lastPrescriptionChangeDate, therapyAuthority)
  {
    lastPrescriptionChangeDate = !!lastPrescriptionChangeDate && !this._isBeforeAvailableDate(lastPrescriptionChangeDate) ?
        lastPrescriptionChangeDate :
        null;

    return therapyAuthority.isManagePatientPharmacistReviewAllowed() &&
        (!!lastPrescriptionChangeDate && (lastPrescriptionChangeDate > this._lastConfirmedReviewDate));
  },

  /**
   * @param {Date} date
   * @return {boolean}
   * @private
   */
  _isBeforeAvailableDate: function(date)
  {
    var oldestStartOfDayMillis = this._reviewsByDateMap.keys()[this.numberOfDays - 1];
    return moment(date).isBefore(oldestStartOfDayMillis);
  },

  /**
   * Creates the internal map with the {@link numberOfDays} keys. Each map key is the value of {@link Date#getTime} at the
   * start of a day, for the past {@link numberOfDays}, starting from the current date. The reviews given at construction
   * are then placed into the appropriate array (key's value) based on the date of creation. During the sort process we also
   * create a cache of the most recent review's creation date, needed to determine the result of {@link isNewReviewRequired}.
   * @private
   */
  _mapReviewData: function()
  {
    var self = this;

    Array(this.numberOfDays)
        .fill(CurrentTime.get())
        .map(function toSubtractedDayMoment(value, index)
        {
          return moment(value).subtract(index, 'days').startOf('day');
        })
        .forEach(function createReviewsByDayMapKey(date)
        {
          self._reviewsByDateMap.put(date.valueOf(), []);
        });

    this.reviews.forEach(
        /** @param {app.views.medications.pharmacists.dto.PharmacistMedicationReview} review */
        function(review)
        {
          var startOfCreationDateMillis = moment(review.getCreateTimestamp()).startOf('day').valueOf();

          if (self._reviewsByDateMap.containsKey(startOfCreationDateMillis))
          {
            self._reviewsByDateMap.get(startOfCreationDateMillis).push(review);
          }

          if (!review.isDraft() && review.getCreateTimestamp() > self._lastConfirmedReviewDate)
          {
            self._lastConfirmedReviewDate = new Date(review.getCreateTimestamp().getTime());
          }
        });
  }
});
