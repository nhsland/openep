Class.define('app.views.medications.timeline.administration.dto.Administration', 'tm.jquery.Object', {
  statics: {
    fromJson: function(jsonObject)
    {
      var config = jQuery.extend(true, {}, jsonObject);
      config.administrationTime = tm.jquery.Utils.isEmpty(jsonObject.administrationTime) ?
          null :
          new Date(jsonObject.administrationTime);
      config.plannedTime = tm.jquery.Utils.isEmpty(jsonObject.plannedTime) ?
          null :
          new Date(jsonObject.plannedTime);

      config.route = tm.jquery.Utils.isEmpty(jsonObject.route) ?
          null :
          new app.views.medications.common.dto.MedicationRoute(jsonObject.route);

      config.substituteMedication = tm.jquery.Utils.isEmpty(jsonObject.substituteMedication) ?
          null :
          new app.views.medications.common.dto.Medication(jsonObject.substituteMedication);

      config.administeredDose = tm.jquery.Utils.isEmpty(jsonObject.administeredDose) ?
          null :
          new app.views.medications.common.dto.TherapyDose(jsonObject.administeredDose);

      config.plannedDose = tm.jquery.Utils.isEmpty(jsonObject.plannedDose) ?
          null :
          new app.views.medications.common.dto.TherapyDose(jsonObject.plannedDose);

      config.plannedStartingDevice = tm.jquery.Utils.isEmpty(jsonObject.plannedStartingDevice) ?
          null :
          new app.views.medications.common.dto.OxygenStartingDevice(jsonObject.plannedStartingDevice);

      config.startingDevice = tm.jquery.Utils.isEmpty(jsonObject.startingDevice) ?
          null :
          new app.views.medications.common.dto.OxygenStartingDevice(jsonObject.startingDevice);

      return new app.views.medications.timeline.administration.dto.Administration(config);
    },

    /**
     * Returns the sort index value by order of ascending administration time.
     * @param {app.views.medications.timeline.administration.dto.Administration} a
     * @param {app.views.medications.timeline.administration.dto.Administration} b
     * @returns {number}
     */
    compareByTimestamp: function(a, b)
    {
      var administrationTimestampA = a.getAdministrationTimestamp();
      var administrationTimestampB = b.getAdministrationTimestamp();

      if (administrationTimestampA < administrationTimestampB) return -1;
      if (administrationTimestampA > administrationTimestampB) return 1;
      return 0;
    }
  },

  administrationType: null,
  administrationStatus: null,
  administrationResult: null,
  notAdministeredReason: null,
  selfAdministrationType: null,
  administrationTime: null,
  plannedTime: null,
  additionalAdministration: null,
  administrationId: null,
  taskId: null,
  groupUUId: null,
  therapyId: null,
  composerName: null,
  witness: null,
  comment: null,
  doctorsComment: null,
  doctorConfirmation: null,
  route: null,
  startAdministrationSubtype: null,
  substituteMedication: null,
  administeredDose: null,
  plannedDose: null,
  differentFromOrder: null,
  duration: null,
  infusionBag: null,
  plannedStartingDevice: null,
  startingDevice: null,
  adjustAdministrationSubtype: null,
  infusionSetChangeEnum: null,

  Constructor: function(config)
  {
    this.callSuper(config);
  },

  /**
   * @returns {String} of type {@link #app.views.medications.TherapyEnums.administrationTypeEnum}
   */
  getAdministrationType: function()
  {
    return this.administrationType;
  },

  /**
   *
   * @param {String} administrationType of type {@link #app.views.medications.TherapyEnums.administrationTypeEnum}
   */
  setAdministrationType: function(administrationType)
  {
    this.administrationType = administrationType;
  },

  /**
   * @returns {String} of type {@link #app.views.medications.TherapyEnums.administrationStatusEnum}
   */
  getAdministrationStatus: function()
  {
    return this.administrationStatus;
  },

  /**
   * @param {String} administrationStatus of type {@link #app.views.medications.TherapyEnums.administrationStatusEnum}
   */
  setAdministrationStatus: function(administrationStatus)
  {
    this.administrationStatus = administrationStatus;
  },

  /**
   * @returns {String} of type {@link #app.views.medications.TherapyEnums.administrationResultEnum}
   */
  getAdministrationResult: function()
  {
    return this.administrationResult;
  },

  /**
   * @param {String} administrationResult of type {@link #app.views.medications.TherapyEnums.administrationResultEnum}
   */
  setAdministrationResult: function(administrationResult)
  {
    this.administrationResult = administrationResult;
  },

  /**
   * @returns {Object}
   */
  getNotAdministeredReason: function()
  {
    return this.notAdministeredReason;
  },

  /**
   * @param {Object} notAdministeredReason
   */
  setNotAdministeredReason: function(notAdministeredReason)
  {
    this.notAdministeredReason = notAdministeredReason;
  },

  /**
   * @returns {String} of type {@link #app.views.medications.TherapyEnums.selfAdministrationTypeEnum}
   */
  getSelfAdministrationType: function()
  {
    return this.selfAdministrationType;
  },

  /**
   * @param {String} selfAdministrationType of type {@link #app.views.medications.TherapyEnums.selfAdministrationTypeEnum}
   */
  setSelfAdministrationType: function(selfAdministrationType)
  {
    this.selfAdministrationType = selfAdministrationType;
  },

  /**
   * @returns {Date|null}
   */
  getAdministrationTime: function()
  {
    return this.administrationTime;
  },

  /**
   * @param {Date|null} administrationTime
   */
  setAdministrationTime: function(administrationTime)
  {
    this.administrationTime = administrationTime;
  },

  /**
   * @returns {Date|null}
   */
  getPlannedTime: function()
  {
    return this.plannedTime;
  },

  /**
   * @param {Date|null} plannedTime
   */
  setPlannedTime: function(plannedTime)
  {
    this.plannedTime = plannedTime;
  },

  /**
   * @returns {Boolean}
   */
  isAdditionalAdministration: function()
  {
    return this.additionalAdministration === true;
  },

  /**
   * @param {Boolean} additionalAdministration
   */
  setAdditionalAdministration: function(additionalAdministration)
  {
    this.additionalAdministration = additionalAdministration;
  },

  /**
   * @returns {String}
   */
  getAdministrationId: function()
  {
    return this.administrationId;
  },

  /**
   * @param {String} administrationId
   */
  setAdministrationId: function(administrationId)
  {
    this.administrationId = administrationId;
  },

  /**
   * @returns {String}
   */
  getTaskId: function()
  {
    return this.taskId;
  },

  /**
   * @param {String} taskId
   */
  setTaskId: function(taskId)
  {
    this.taskId = taskId;
  },

  /**
   * @returns {String}
   */
  getGroupUUId: function()
  {
    return this.groupUUId;
  },

  /**
   * @param {String} groupUUId
   */
  setGroupUUId: function(groupUUId)
  {
    this.groupUUId = groupUUId;
  },

  /**
   * @returns {String}
   */
  getTherapyId: function()
  {
    return this.therapyId;
  },

  /**
   * @param {String} therapyId
   */
  setTherapyId: function(therapyId)
  {
    this.therapyId = therapyId
  },

  /**
   * @returns {String}
   */
  getComposerName: function()
  {
    return this.composerName;
  },

  /**
   * @param {String} composerName
   */
  setComposerName: function(composerName)
  {
    this.composerName = composerName;
  },

  /**
   * @returns {Object}
   */
  getWitness: function()
  {
    return this.witness;
  },

  /**
   * @param {Object} witness
   */
  setWitness: function(witness)
  {
    this.witness = witness
  },

  /**
   * @returns {String}
   */
  getComment: function()
  {
    return this.comment;
  },

  /**
   * @param {String} comment
   */
  setComment: function(comment)
  {
    this.comment = comment;
  },

  /**
   * @returns {String}
   */
  getDoctorsComment: function()
  {
    return this.doctorsComment;
  },

  /**
   * @param {String} doctorsComment
   */
  setDoctorsComment: function(doctorsComment)
  {
    this.doctorsComment = doctorsComment;
  },

  /**
   * @returns {Boolean|null}
   */
  getDoctorConfirmation: function()
  {
    return this.doctorConfirmation;
  },

  /**
   * @param {Boolean} doctorConfirmation
   */
  setDoctorConfirmation: function(doctorConfirmation)
  {
    this.doctorConfirmation = doctorConfirmation;
  },

  /**
   * @returns {app.views.medications.common.dto.MedicationRoute}
   */
  getRoute: function()
  {
    return this.route;
  },

  /**
   * @param {app.views.medications.common.dto.MedicationRoute} route
   */
  setRoute: function(route)
  {
    this.route = route;
  },

  /**
   * @returns {String} of type {@link #app.views.medications.TherapyEnums.startAdministrationSubtype}
   */
  getStartAdministrationSubtype: function()
  {
    return this.startAdministrationSubtype;
  },

  /**
   * @param {String} startAdministrationSubtype of type {@link #app.views.medications.TherapyEnums.startAdministrationSubtype}
   */
  setStartAdministrationSubtype: function(startAdministrationSubtype)
  {
    this.startAdministrationSubtype = startAdministrationSubtype;
  },

  /**
   * @returns {app.views.medications.common.dto.Medication}
   */
  getSubstituteMedication: function()
  {
    return this.substituteMedication;
  },

  /**
   * @param {app.views.medications.common.dto.Medication} substituteMedication
   */
  setSubstituteMedication: function(substituteMedication)
  {
    this.substituteMedication = substituteMedication;
  },

  /**
   * @returns {app.views.medications.common.dto.TherapyDose}
   */
  getAdministeredDose: function()
  {
    return this.administeredDose;
  },

  /**
   * @param {app.views.medications.common.dto.TherapyDose} administeredDose
   */
  setAdministeredDose: function(administeredDose)
  {
    this.administeredDose = administeredDose;
  },

  /**
   * @returns {app.views.medications.common.dto.TherapyDose}
   */
  getPlannedDose: function()
  {
    return this.plannedDose;
  },

  /**
   * @param {app.views.medications.common.dto.TherapyDose} plannedDose
   */
  setPlannedDose: function(plannedDose)
  {
    this.plannedDose = plannedDose;
  },

  /**
   * @returns {Boolean}
   */
  isDifferentFromOrder: function()
  {
    return this.differentFromOrder === true;
  },

  /**
   * @param {Boolean} differentFromOrder
   */
  setDifferentFromOrder: function(differentFromOrder)
  {
    this.differentFromOrder = differentFromOrder;
  },

  /**
   * @returns {Number}
   */
  getDuration: function()
  {
    return this.duration;
  },

  /**
   * @param {Number} duration
   */
  setDuration: function(duration)
  {
    this.duration = duration;
  },

  /**
   * @returns {Object}
   */
  getInfusionBag: function()
  {
    return this.infusionBag;
  },

  /**
   * @param {{quantity: Number, unit: String}} infusionBag
   */
  setInfusionBag: function(infusionBag)
  {
    this.infusionBag = infusionBag;
  },

  /**
   * @returns {app.views.medications.common.dto.OxygenStartingDevice}
   */
  getPlannedStartingDevice: function()
  {
    return this.plannedStartingDevice;
  },

  /**
   * @param {app.views.medications.common.dto.OxygenStartingDevice} plannedStartingDevice
   */
  setPlannedStartingDevice: function(plannedStartingDevice)
  {
    this.plannedStartingDevice = plannedStartingDevice;
  },

  /**
   * @returns {app.views.medications.common.dto.OxygenStartingDevice}
   */
  getStartingDevice: function()
  {
    return this.startingDevice;
  },

  /**
   * @param {app.views.medications.common.dto.OxygenStartingDevice} startingDevice
   */
  setStartingDevice: function(startingDevice)
  {
    this.startingDevice = startingDevice;
  },

  /**
   * @returns {String} of type {@link #app.views.medications.TherapyEnums.adjustAdministrationSubtype}
   */
  getAdjustAdministrationSubtype: function()
  {
    return this.adjustAdministrationSubtype;
  },

  /**
   * @param {String} adjustAdministrationSubtype of type {@link #app.views.medications.TherapyEnums.adjustAdministrationSubtype}
   */
  setAdjustAdministrationSubtype: function(adjustAdministrationSubtype)
  {
    this.adjustAdministrationSubtype = adjustAdministrationSubtype;
  },

  /**
   * @returns {String} of type {@link #app.views.medications.TherapyEnums.infusionSetChangeEnum}
   */
  getInfusionSetChangeEnum: function()
  {
    return this.infusionSetChangeEnum;
  },

  /**
   * @param {String} infusionSetChangeEnum  of type {@link #app.views.medications.TherapyEnums.infusionSetChangeEnum}
   */
  setInfusionSetChangeEnum: function(infusionSetChangeEnum)
  {
    this.infusionSetChangeEnum = infusionSetChangeEnum;
  },

  /**
   * The administration is confirmed after the user has selected and confirmed an administration result, regardless of the
   * outcome (patient may or may not have received the medication).
   * @returns {boolean}
   */
  isAdministrationConfirmed: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var administrationStatus = this.getAdministrationStatus();
    return this.isAdministrationCompleted() || administrationStatus === enums.administrationStatusEnum.FAILED;
  },

  /**
   * @returns {boolean}
   */
  isAdministrationCompleted: function()
  {
    var enums = app.views.medications.TherapyEnums;
    var administrationStatus = this.getAdministrationStatus();
    return administrationStatus === enums.administrationStatusEnum.COMPLETED ||
        administrationStatus === enums.administrationStatusEnum.COMPLETED_LATE ||
        administrationStatus === enums.administrationStatusEnum.COMPLETED_EARLY;
  },

  /**
   * @returns {boolean}
   */
  isAdministrationAdministered: function()
  {
    var enums = app.views.medications.TherapyEnums;
    return (this.getAdministrationResult() === enums.administrationResultEnum.GIVEN ||
        this.getAdministrationResult() === enums.administrationResultEnum.SELF_ADMINISTERED);
  },

  /**
   * @returns {Date}
   */
  getAdministrationTimestamp: function()
  {
    return this.getAdministrationTime() ? this.getAdministrationTime() : this.getPlannedTime();
  },

  /**
   * @returns {boolean}
   */
  isWithZeroRate: function()
  {
    return !!this.getAdministeredDose() && this.getAdministeredDose().getNumerator() === 0;
  },

  /**
   * @param {Date} timestamp
   * @returns {boolean}
   */
  isAtSameTimeAs: function(timestamp)
  {
    return !!timestamp && timestamp.getTime() === this.getAdministrationTimeInMilliseconds();
  },

  /**
   * @param {Number|app.views.medications.timeline.administration.dto.Administration} compareTo
   * @returns {boolean}
   */
  isScheduledBefore: function(compareTo)
  {
    var timestamp = compareTo instanceof app.views.medications.timeline.administration.dto.Administration ?
        compareTo.getAdministrationTimeInMilliseconds() :
        compareTo;
    return this.getAdministrationTimeInMilliseconds() < timestamp;
  },

  /**
   * @param {Number|app.views.medications.timeline.administration.dto.Administration} compareTo
   * @returns {boolean}
   */
  isScheduledBeforeOrAtSameTime: function(compareTo)
  {
    var timestamp = compareTo instanceof app.views.medications.timeline.administration.dto.Administration ?
        compareTo.getAdministrationTimeInMilliseconds() :
        compareTo;
    return this.getAdministrationTimeInMilliseconds() <= timestamp;
  },

  /**
   * @param {Number|app.views.medications.timeline.administration.dto.Administration} compareTo
   * @returns {boolean}
   */
  isScheduledAfter: function(compareTo)
  {
    var timestamp = compareTo instanceof app.views.medications.timeline.administration.dto.Administration ?
        compareTo.getAdministrationTimeInMilliseconds() :
        compareTo;
    return this.getAdministrationTimeInMilliseconds() > timestamp;
  },

  /**
   * @param {Number|app.views.medications.timeline.administration.dto.Administration} compareTo
   * @returns {boolean}
   */
  isScheduledAfterOrAtSameTime: function(compareTo)
  {
    var timestamp = compareTo instanceof app.views.medications.timeline.administration.dto.Administration ?
        compareTo.getAdministrationTimeInMilliseconds() :
        compareTo;
    return this.getAdministrationTimeInMilliseconds() >= timestamp;
  },

  /**
   * Calculates if administration time is between two administrations or timestamps
   * @param {Number|app.views.medications.timeline.administration.dto.Administration} previous
   * @param {Number|app.views.medications.timeline.administration.dto.Administration} next
   * @param {Boolean} [strictBefore=false]
   * @param {Boolean} [strictAfter=false]
   * @returns {boolean}
   */
  isScheduledBetween: function(previous, next, strictBefore, strictAfter)
  {
    var isBefore = strictBefore === true ?
        this.isScheduledBefore(next) :
        this.isScheduledBeforeOrAtSameTime(next);
    var isAfter = strictAfter === true ?
        this.isScheduledAfter(previous) :
        this.isScheduledAfterOrAtSameTime(previous);

    return isBefore && isAfter;
  },

  /**
   * @returns {Number}
   */
  getAdministrationTimeInMilliseconds: function()
  {
    return this.getAdministrationTimestamp().getTime();
  },

  /**
   * @returns {Boolean}
   */
  isInfusionBagChangeTask: function()
  {
    return !tm.jquery.Utils.isEmpty(this.getTaskId()) && !tm.jquery.Utils.isEmpty(this.getInfusionBag())
  },

  /**
   * @returns {Null|String} of type {@link #app.views.medications.TherapyEnums.therapyDoseTypeEnum}
   */
  getDoseType: function()
  {
    if (this.getPlannedDose() && this.getPlannedDose().getTherapyDoseTypeEnum())
    {
      return this.getPlannedDose().getTherapyDoseTypeEnum();
    }
    else if (this.getAdministeredDose() && this.getAdministeredDose().getTherapyDoseTypeEnum())
    {
      return this.getAdministeredDose().getTherapyDoseTypeEnum();
    }
  },

  /**
   * A cancelled administration was planned, but was cancelled in advanced by a user, meaning the patient should not receive
   * the medication at the planned time.
   * @returns {boolean}
   */
  isAdministrationCancelled: function()
  {
    return !!this.getNotAdministeredReason() &&
        this.getNotAdministeredReason().code === app.views.medications.TherapyEnums.notAdministeredReasonEnum.CANCELLED;
  },

  /**
   * Planned administrations are planned in advanced (either by the prescription or additionally by a user).
   * As such, they have a planned task with a {@link #taskId}, as opposed to additionally recorded administrations, which are
   * recorded retrospectively and have no planned task.
   * Checking if the administration has a {@link #taskId} is a sure way to determine whether it was planned in advanced or
   * recorded additionally.
   * @returns {boolean}
   */
  isAdministrationPlanned: function()
  {
    return !!this.getTaskId();
  },

  /**
   * @param {Boolean} deep
   * @returns {app.views.medications.timeline.administration.dto.Administration}
   */
  clone: function(deep)
  {
    return deep !== false ?
        jQuery.extend(true, new app.views.medications.timeline.administration.dto.Administration(), this) :
        jQuery.extend(new app.views.medications.timeline.administration.dto.Administration(), this);
  }
});