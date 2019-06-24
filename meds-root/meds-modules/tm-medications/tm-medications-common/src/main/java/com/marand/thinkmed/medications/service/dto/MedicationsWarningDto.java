package com.marand.thinkmed.medications.service.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import lombok.NonNull;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */

@SuppressWarnings({"AssignmentOrReturnOfFieldWithMutableType", "ComparableImplementedButEqualsNotOverridden"})
public class MedicationsWarningDto extends DataTransferObject implements Comparable<MedicationsWarningDto>
{
  private String description;
  private WarningSeverity severity;
  private String externalType;
  private WarningType type;
  private String externalSeverity;
  private String monographHtml;
  private List<NamedExternalDto> medications = new ArrayList<>();

  public MedicationsWarningDto() { }

  public MedicationsWarningDto(
      final String description,
      final WarningSeverity severity,
      final WarningType type,
      final List<NamedExternalDto> medications)
  {
    this.description = description;
    this.severity = severity;
    this.type = type;
    this.medications = medications;
  }

  public MedicationsWarningDto(
      final String description,
      final WarningSeverity severity,
      final WarningType type)
  {
    this.description = description;
    this.severity = severity;
    this.type = type;
  }

  public String getExternalType()
  {
    return externalType;
  }

  public void setExternalType(final String externalType)
  {
    this.externalType = externalType;
  }

  public WarningSeverity getSeverity()
  {
    return severity;
  }

  public WarningType getType()

  {
    return type;
  }

  public void setType(final WarningType type)
  {
    this.type = type;
  }

  public void setSeverity(final WarningSeverity severity)
  {
    this.severity = severity;
  }

  public String getExternalSeverity()
  {
    return externalSeverity;
  }

  public void setExternalSeverity(final String externalSeverity)
  {
    this.externalSeverity = externalSeverity;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(final String description)
  {
    this.description = description;
  }

  public List<NamedExternalDto> getMedications()
  {
    return medications;
  }

  public void setMedications(final List<NamedExternalDto> medications)
  {
    this.medications = medications;
  }

  public String getMonographHtml()
  {
    return monographHtml;
  }

  public void setMonographHtml(final String monographHtml)
  {
    this.monographHtml = monographHtml;
  }

  @Override
  public int compareTo(final @NonNull MedicationsWarningDto o)
  {
    return Comparator
        .comparing(MedicationsWarningDto::getSeverity, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(MedicationsWarningDto::getType, Comparator.nullsLast(Comparator.naturalOrder()))
        .compare(this, o);
  }

  @Override
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (!(o instanceof MedicationsWarningDto))
    {
      return false;
    }
    //noinspection QuestionableName
    final MedicationsWarningDto that = (MedicationsWarningDto)o;
    //noinspection OverlyComplexBooleanExpression
    return Objects.equals(description, that.getDescription()) &&
        severity == that.getSeverity() &&
        type == that.getType() &&
        Objects.equals(medications, that.getMedications());
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(description, severity, type, medications);
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("description", description)
        .append("severity", severity)
        .append("type", type)
        .append("externalSeverity", externalSeverity)
        .append("medications", medications)
    ;
  }
}
