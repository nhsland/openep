package com.marand.thinkmed.medications.dto.template;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class CustomTemplatesGroupDto extends DataTransferObject implements JsonSerializable
{
  private String group;
  private List<TherapyTemplateDto> customTemplates = new ArrayList<>();

  public CustomTemplatesGroupDto(
      final String group,
      final List<TherapyTemplateDto> customTemplates)
  {
    this.group = group;
    this.customTemplates = customTemplates;
  }

  public String getGroup()
  {
    return group;
  }

  public void setGroup(final String group)
  {
    this.group = group;
  }

  public List<TherapyTemplateDto> getCustomTemplates()
  {
    return customTemplates;
  }

  public void setCustomTemplates(final List<TherapyTemplateDto> customTemplates)
  {
    this.customTemplates = customTemplates;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("group", group)
        .append("customTemplates", customTemplates)
    ;
  }
}
