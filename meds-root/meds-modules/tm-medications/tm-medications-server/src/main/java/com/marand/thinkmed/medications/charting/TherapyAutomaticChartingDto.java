package com.marand.thinkmed.medications.charting;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public abstract class TherapyAutomaticChartingDto extends DataTransferObject
{
  private final AutomaticChartingType type;
  private final String compositionUid;
  private final String instructionName;
  private final String patientId;

  protected TherapyAutomaticChartingDto(
      final @NonNull AutomaticChartingType type,
      final @NonNull String compositionUid,
      final @NonNull String instructionName,
      final @NonNull String patientId)
  {
    this.type = type;
    this.compositionUid = compositionUid;
    this.instructionName = instructionName;
    this.patientId = patientId;
  }

  public AutomaticChartingType getType()
  {
    return type;
  }

  public String getCompositionUid()
  {
    return compositionUid;
  }

  public String getInstructionName()
  {
    return instructionName;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public abstract boolean isEnabled(final @NonNull DateTime when);

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("compositionUid", compositionUid)
        .append("instructionName", instructionName)
        .append("patientId", patientId)
    ;
  }
}
