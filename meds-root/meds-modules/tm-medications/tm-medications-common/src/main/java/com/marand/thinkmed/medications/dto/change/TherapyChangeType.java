package com.marand.thinkmed.medications.dto.change;

import java.util.EnumSet;

/**
 * @author Igor Horvat
 * @author Mitja Lapajne
 */

public enum TherapyChangeType
{
  MEDICATION(StringTherapyChangeDto.class),
  ROUTE(StringsTherapyChangeDto.class),
  VARIABLE_DOSE(VariableDoseTherapyChangeDto.class),
  VARIABLE_DOSE_TO_DOSE(VariableDoseToDoseTherapyChangeDto.class),
  DOSE_TO_VARIABLE_DOSE(DoseToVariableDoseTherapyChangeDto.class),
  VARIABLE_RATE(VariableRateTherapyChangeDto.class),
  VARIABLE_RATE_TO_RATE(VariableRateToRateTherapyChangeDto.class),
  RATE_TO_VARIABLE_RATE(RateToVariableRateTherapyChangeDto.class),
  DOSE(StringTherapyChangeDto.class),
  VOLUME_SUM(StringTherapyChangeDto.class),
  DOSE_INTERVAL(StringTherapyChangeDto.class),
  DOSE_TIMES(StringsTherapyChangeDto.class),
  RATE(StringTherapyChangeDto.class),
  INFUSION_DURATION(StringTherapyChangeDto.class),
  ADDITIONAL_CONDITIONS(StringTherapyChangeDto.class),
  ADDITIONAL_INSTRUCTION(StringTherapyChangeDto.class),
  WHEN_NEEDED(StringTherapyChangeDto.class),
  MAX_DOSES(StringTherapyChangeDto.class),
  DOCTOR_ORDERS(StringTherapyChangeDto.class),
  COMMENT(StringTherapyChangeDto.class),
  INDICATION(StringTherapyChangeDto.class),
  RELEASE_DETAILS(StringTherapyChangeDto.class),
  START(StringTherapyChangeDto.class),
  END(StringTherapyChangeDto.class),

  //Oxygen
  DEVICE(StringTherapyChangeDto.class),
  SATURATION(StringTherapyChangeDto.class);

  private final Class<? extends TherapyChangeDto<?, ?>> dtoClass;

  TherapyChangeType(final Class<? extends TherapyChangeDto<?, ?>> dtoClass)
  {
    this.dtoClass = dtoClass;
  }

  public Class<? extends TherapyChangeDto<?, ?>> getDtoClass()
  {
    return dtoClass;
  }

  public enum TherapyChangeGroup
  {
    REQUIRES_NEW_THERAPY(EnumSet.of(
        MEDICATION,
        ROUTE,
        VARIABLE_DOSE,
        VARIABLE_DOSE_TO_DOSE,
        DOSE_TO_VARIABLE_DOSE,
        VARIABLE_RATE,
        VARIABLE_RATE_TO_RATE,
        RATE_TO_VARIABLE_RATE,
        DOSE,
        VOLUME_SUM,
        DOSE_INTERVAL,
        DOSE_TIMES,
        RATE,
        INFUSION_DURATION,
        ADDITIONAL_CONDITIONS,
        ADDITIONAL_INSTRUCTION,
        WHEN_NEEDED,
        MAX_DOSES,
        RELEASE_DETAILS,
        DOCTOR_ORDERS,
        END,
        DEVICE,
        SATURATION)),

    REQUIRES_CHANGE_REASON(EnumSet.of(
        MEDICATION,
        ROUTE,
        VARIABLE_DOSE,
        VARIABLE_DOSE_TO_DOSE,
        DOSE_TO_VARIABLE_DOSE,
        VARIABLE_RATE,
        VARIABLE_RATE_TO_RATE,
        RATE_TO_VARIABLE_RATE,
        DOSE,
        VOLUME_SUM,
        DOSE_INTERVAL,
        DOSE_TIMES,
        RATE,
        INFUSION_DURATION,
        ADDITIONAL_CONDITIONS,
        ADDITIONAL_INSTRUCTION,
        WHEN_NEEDED,
        RELEASE_DETAILS,
        MAX_DOSES,
        DOCTOR_ORDERS,
        DEVICE,
        SATURATION));

    private final EnumSet<TherapyChangeType> changes;

    TherapyChangeGroup(final EnumSet<TherapyChangeType> changes)
    {
      this.changes = changes;
    }

    public EnumSet<TherapyChangeType> getChanges()
    {
      return changes;
    }
  }
}
