package com.marand.thinkmed.medications.charting;

import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public class SelfAdminAutomaticChartingDto extends TherapyAutomaticChartingDto
{
  private final DateTime startTime;
  private final SelfAdministeringActionEnum selfAdministeringAction;

  public SelfAdminAutomaticChartingDto(
      final @NonNull String compositionUid,
      final @NonNull String instructionName,
      final @NonNull String patientId,
      final DateTime startTime,
      final SelfAdministeringActionEnum selfAdministeringAction)
  {
    super(AutomaticChartingType.SELF_ADMINISTER, compositionUid, instructionName, patientId);
    this.startTime = startTime;
    this.selfAdministeringAction = selfAdministeringAction;
  }

  @Override
  public boolean isEnabled(final @NonNull DateTime when)
  {
    return selfAdministeringAction == SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED && when.isAfter(startTime.minusMinutes(1));
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("startTime", startTime)
        .append("selfAdministeringAction", selfAdministeringAction)
    ;
  }
}
