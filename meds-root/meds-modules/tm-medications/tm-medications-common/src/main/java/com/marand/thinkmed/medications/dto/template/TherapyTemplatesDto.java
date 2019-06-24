package com.marand.thinkmed.medications.dto.template;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.api.core.data.object.DataTransferObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class TherapyTemplatesDto extends DataTransferObject implements JsonSerializable
{
  private List<TherapyTemplateDto> userTemplates = new ArrayList<>();
  private List<TherapyTemplateDto> organizationTemplates = new ArrayList<>();
  private List<TherapyTemplateDto> patientTemplates = new ArrayList<>();
  private List<CustomTemplatesGroupDto> customTemplateGroups = new ArrayList<>();

  public List<TherapyTemplateDto> getUserTemplates()
  {
    return userTemplates;
  }

  public void setUserTemplates(final List<TherapyTemplateDto> userTemplates)
  {
    this.userTemplates = userTemplates;
  }

  public List<TherapyTemplateDto> getOrganizationTemplates()
  {
    return organizationTemplates;
  }

  public void setOrganizationTemplates(final List<TherapyTemplateDto> organizationTemplates)
  {
    this.organizationTemplates = organizationTemplates;
  }

  public List<TherapyTemplateDto> getPatientTemplates()
  {
    return patientTemplates;
  }

  public void setPatientTemplates(final List<TherapyTemplateDto> patientTemplates)
  {
    this.patientTemplates = patientTemplates;
  }

  public List<CustomTemplatesGroupDto> getCustomTemplateGroups()
  {
    return customTemplateGroups;
  }

  public void setCustomTemplateGroups(final List<CustomTemplatesGroupDto> customTemplateGroups)
  {
    this.customTemplateGroups = customTemplateGroups;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("userTemplates", userTemplates)
        .append("organizationTemplates", organizationTemplates)
        .append("patientTemplates", patientTemplates)
        .append("customTemplateGroups", customTemplateGroups)
    ;
  }
}
