package com.marand.thinkmed.medications.dto.template;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.dto.ValidationIssueEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyTemplateElementDto extends DataTransferObject implements JsonSerializable
{
  private long id;
  private TherapyDto therapy;
  private TherapyTemplateStatus templateStatus;
  private boolean recordAdministration;
  private List<ValidationIssueEnum> validationIssues = new ArrayList<>();

  public long getId()
  {
    return id;
  }

  public void setId(final long id)
  {
    this.id = id;
  }

  public TherapyDto getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final TherapyDto therapy)
  {
    this.therapy = therapy;
  }

  public TherapyTemplateStatus getStatus()
  {
    return templateStatus;
  }

  public void setStatus(final TherapyTemplateStatus templateStatus)
  {
    this.templateStatus = templateStatus;
  }

  public boolean doRecordAdministration()
  {
    return recordAdministration;
  }

  public void setRecordAdministration(final boolean recordAdministration)
  {
    this.recordAdministration = recordAdministration;
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
        .append("templateStatus", templateStatus)
        .append("recordAdministration", recordAdministration)
        .append("validationIssues", validationIssues);
  }
}
