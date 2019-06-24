package com.marand.thinkmed.medications.preferences;

import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.prefs.BooleanPreference;
import com.marand.maf.core.prefs.MafPrefsStorageType;
import com.marand.maf.core.prefs.Preferences;
import com.marand.maf.core.prefs.StringPreference;
import com.marand.thinkmed.medications.dto.AdministrationPatientTaskLimitsDto;
import com.marand.thinkmed.medications.dto.AdministrationTimingDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;

import static com.marand.thinkmed.medications.dto.AdministrationTimingDto.AdministrationTimestampsDto;

/**
 * @author Mitja Lapajne
 */

public class MedicationPreferencesUtil
{
  // prefixes
  private static final String ROUNDS_START_PREFIX = "ROUNDS_START_";
  private static final String WEEKEND_REVIEW = "WEEKEND_REVIEW_";
  private static final String ADMINISTRATION_INTERVALS = "ADMINISTRATION_INTERVALS_";
  private static final String ADMINISTRATION_PATIENT_TASK_LIMIT_PREFIX = "ADMINISTRATION_PATIENT_TASK_LIMIT";
  private static final String CARE_PROVIDER_PREFIX = "CARE_PROVIDER_";

  // keys
  private static final String UNLICENSED_MEDICATION_WARNING_KEY = "UNLICENSED_MEDICATION_WARNING";

  // defaults
  private static final RoundsIntervalDto ROUNDS_INTERVAL_DEFAULT = getDefaultRoundsIntervalDto();
  private static final AdministrationTimingDto ADMINISTRATION_TIMING_DEFAULT = getDefaultAdministrationTimingDto();
  private static final AdministrationPatientTaskLimitsDto ADMINISTRATION_PATIENT_TASK_LIMITS_DEFAULT = getDefaultAdministrationPatientTaskLimitsDto();

  private MedicationPreferencesUtil() { }

  public static RoundsIntervalDto getRoundsInterval(final String careProviderId)
  {
    if (careProviderId != null)
    {
      final String key = ROUNDS_START_PREFIX + CARE_PROVIDER_PREFIX + careProviderId;
      return getRoundsIntervalPreference(key, ROUNDS_INTERVAL_DEFAULT).getValue();
    }
    return ROUNDS_INTERVAL_DEFAULT;
  }

  public static boolean isReviewOnWeekendEnabled(final String careProviderId)
  {
    if (careProviderId != null)
    {
      final String key = WEEKEND_REVIEW + CARE_PROVIDER_PREFIX + careProviderId;
      return getWeekendReviewPreference(key, false).getValue();
    }
    return false;
  }

  public static AdministrationTimingDto getAdministrationTiming(final String careProviderId)
  {
    if (careProviderId != null)
    {
      final String key = ADMINISTRATION_INTERVALS + CARE_PROVIDER_PREFIX + careProviderId;
      return getTimingPreference(key, ADMINISTRATION_TIMING_DEFAULT).getValue();
    }
    return ADMINISTRATION_TIMING_DEFAULT;
  }

  public static String getUnlicensedMedicationWarning(final Locale locale)
  {
    final StringPreference strPref =
        new StringPreference(
            UNLICENSED_MEDICATION_WARNING_KEY,
            MafPrefsStorageType.SYSTEM,
            false,
            Dictionary.getEntry("unlicensed.medication.outpatient.warning", locale));
    return strPref.getValue();
  }

  public static AdministrationPatientTaskLimitsDto getAdministrationPatientTaskLimitsPreference()
  {
    final String key = ADMINISTRATION_PATIENT_TASK_LIMIT_PREFIX;
    final AdministrationPatientTaskLimitsPreference newPreference =
        new AdministrationPatientTaskLimitsPreference(
            key, MafPrefsStorageType.SYSTEM, false, ADMINISTRATION_PATIENT_TASK_LIMITS_DEFAULT);
    return Preferences.dynamicPreference(key, newPreference).getValue();
  }

  private static RoundsIntervalPreference getRoundsIntervalPreference(
      final String key,
      final RoundsIntervalDto defaultValue)
  {
    final RoundsIntervalPreference newPreference = new RoundsIntervalPreference(
        key,
        MafPrefsStorageType.SYSTEM,
        false,
        defaultValue);
    return Preferences.dynamicPreference(key, newPreference);
  }

  private static BooleanPreference getWeekendReviewPreference(final String key, final boolean defaultValue)
  {
    return Preferences.dynamicPreference(
        key,
        new BooleanPreference(
            key,
            MafPrefsStorageType.SYSTEM,
            false,
            defaultValue));
  }

  private static MedicationAdministrationTimingPreference getTimingPreference(
      final String key,
      final AdministrationTimingDto defaultValue)
  {
    final MedicationAdministrationTimingPreference newPreference = new MedicationAdministrationTimingPreference(
        key,
        MafPrefsStorageType.SYSTEM,
        false,
        defaultValue);
    return Preferences.dynamicPreference(key, newPreference);
  }

  public static RoundsIntervalDto getDefaultRoundsIntervalDto()
  {
    final RoundsIntervalDto roundsDto = new RoundsIntervalDto();
    roundsDto.setStartHour(7);
    roundsDto.setStartMinute(0);
    roundsDto.setEndHour(17);
    roundsDto.setEndMinute(0);
    return roundsDto;
  }

  private static AdministrationTimingDto getDefaultAdministrationTimingDto()
  {
    final AdministrationTimingDto timingDto = new AdministrationTimingDto();
    timingDto.getTimestampsList().add(getMorningTiming());
    timingDto.getTimestampsList().add(getNoonTiming());
    timingDto.getTimestampsList().add(get2XTiming());
    timingDto.getTimestampsList().add(get3XTiming());
    timingDto.getTimestampsList().add(get4XTiming());
    timingDto.getTimestampsList().add(get4HTiming());
    timingDto.getTimestampsList().add(get6HTiming());
    timingDto.getTimestampsList().add(get8HTiming());
    timingDto.getTimestampsList().add(get12HTiming());
    timingDto.getTimestampsList().add(getEveningTiming());
    return timingDto;
  }

  private static AdministrationPatientTaskLimitsDto getDefaultAdministrationPatientTaskLimitsDto()
  {
    final AdministrationPatientTaskLimitsDto limitsDto = new AdministrationPatientTaskLimitsDto();
    limitsDto.setDueTaskOffset(4 * 60);
    limitsDto.setFutureTaskOffset(6 * 60);
    limitsDto.setMaxNumberOfTasks(50);
    return limitsDto;
  }

  private static AdministrationTimestampsDto getMorningTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("MORNING");
    timestampsDto.getTimesList().add(new HourMinuteDto(8, 0));
    return timestampsDto;
  }

  private static AdministrationTimestampsDto getNoonTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("NOON");
    timestampsDto.getTimesList().add(new HourMinuteDto(12, 0));
    return timestampsDto;
  }

  private static AdministrationTimestampsDto get2XTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("2X");
    timestampsDto.getTimesList().add(new HourMinuteDto(8, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(20, 0));
    return timestampsDto;
  }

  private static AdministrationTimestampsDto get3XTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("3X");
    timestampsDto.getTimesList().add(new HourMinuteDto(8, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(13, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(20, 0));
    return timestampsDto;
  }

  private static AdministrationTimestampsDto get4XTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("4X");
    timestampsDto.getTimesList().add(new HourMinuteDto(8, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(13, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(17, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(21, 0));
    return timestampsDto;
  }

  private static AdministrationTimestampsDto get4HTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("4H");
    timestampsDto.getTimesList().add(new HourMinuteDto(0, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(4, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(8, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(12, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(16, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(20, 0));
    return timestampsDto;
  }

  private static AdministrationTimestampsDto get6HTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("6H");
    timestampsDto.getTimesList().add(new HourMinuteDto(0, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(6, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(12, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(18, 0));
    return timestampsDto;
  }

  private static AdministrationTimestampsDto get8HTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("8H");
    timestampsDto.getTimesList().add(new HourMinuteDto(0, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(8, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(16, 0));
    return timestampsDto;
  }

  private static AdministrationTimestampsDto get12HTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("12H");
    timestampsDto.getTimesList().add(new HourMinuteDto(9, 0));
    timestampsDto.getTimesList().add(new HourMinuteDto(21, 0));
    return timestampsDto;
  }

  private static AdministrationTimestampsDto getEveningTiming()
  {
    final AdministrationTimestampsDto timestampsDto = new AdministrationTimestampsDto();
    timestampsDto.setFrequency("EVENING");
    timestampsDto.getTimesList().add(new HourMinuteDto(21, 0));
    return timestampsDto;
  }
}