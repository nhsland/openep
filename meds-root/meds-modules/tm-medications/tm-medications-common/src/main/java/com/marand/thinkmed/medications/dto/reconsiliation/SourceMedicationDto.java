package com.marand.thinkmed.medications.dto.reconsiliation;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.ValidationIssueEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public abstract class SourceMedicationDto extends DataTransferObject implements JsonSerializable
{
  private TherapyDto therapy;
  private String sourceId;
  private List<ValidationIssueEnum> validationIssues = new ArrayList<>();

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  public String getSourceId()
  {
    return sourceId;
  }

  public void setSourceId(final String sourceId)
  {
    this.sourceId = sourceId;
  }

  public List<ValidationIssueEnum> getValidationIssues()
  {
    return validationIssues;
  }

  public void setValidationIssues(final List<ValidationIssueEnum> validationIssues)
  {
    this.validationIssues = validationIssues;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("therapy", therapy)
        .append("sourceId", sourceId)
    ;
  }
}
