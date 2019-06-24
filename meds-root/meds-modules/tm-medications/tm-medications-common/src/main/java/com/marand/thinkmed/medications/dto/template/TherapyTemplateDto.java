package com.marand.thinkmed.medications.dto.template;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.maf.core.data.object.VersionalIdentityDto;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyTemplateDto extends VersionalIdentityDto implements JsonSerializable
{
  private String name;
  private TherapyTemplateTypeEnum type;
  private String userId;
  private String careProviderId;
  private String patientId;
  private String group;
  private List<TherapyTemplateElementDto> templateElements = new ArrayList<>();
  private List<TherapyTemplatePreconditionDto> preconditions = new ArrayList<>();

  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public TherapyTemplateTypeEnum getType()
  {
    return type;
  }

  public void setType(final TherapyTemplateTypeEnum type)
  {
    this.type = type;
  }

  public String getUserId()
  {
    return userId;
  }

  public void setUserId(final String userId)
  {
    this.userId = userId;
  }

  public String getCareProviderId()
  {
    return careProviderId;
  }

  public void setCareProviderId(final String careProviderId)
  {
    this.careProviderId = careProviderId;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public void setPatientId(final String patientId)
  {
    this.patientId = patientId;
  }

  public String getGroup()
  {
    return group;
  }

  public void setGroup(final String group)
  {
    this.group = group;
  }

  public List<TherapyTemplateElementDto> getTemplateElements()
  {
    return templateElements;
  }

  public void setTemplateElements(final List<TherapyTemplateElementDto> templateElements)
  {
    this.templateElements = templateElements;
  }

  public List<TherapyTemplatePreconditionDto> getPreconditions()
  {
    return preconditions;
  }

  public void setPreconditions(final List<TherapyTemplatePreconditionDto> preconditions)
  {
    this.preconditions = preconditions;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("name", name)
        .append("type", type)
        .append("userId", userId)
        .append("careProviderId", careProviderId)
        .append("patientId", patientId)
        .append("group", group)
        .append("templateElements", templateElements)
        .append("preconditions", preconditions);
  }
}
