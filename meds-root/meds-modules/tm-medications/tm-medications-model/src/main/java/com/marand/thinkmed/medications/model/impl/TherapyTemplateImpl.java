package com.marand.thinkmed.medications.model.impl;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.marand.thinkmed.medications.TherapyTemplateModeEnum;
import com.marand.thinkmed.medications.TherapyTemplateTypeEnum;
import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
@Entity
@Table(indexes = {
    @Index(name = "xpTherapyTemplateCareProvider", columnList = "care_provider_id"),
    @Index(name = "xfTherapyTemplatePatient", columnList = "patient_id")})
public class TherapyTemplateImpl extends AbstractPermanentEntity
{
  private String name;
  private TherapyTemplateTypeEnum type;
  private TherapyTemplateModeEnum templateMode;
  private String userId;
  private String careProviderId;
  private String patientId;
  private TherapyTemplateGroupImpl templateGroup;
  private Set<TherapyTemplateElementImpl> therapyTemplateElements = new HashSet<>();
  private Set<TherapyTemplatePreconditionImpl> preconditions = new HashSet<>();

  @Column(nullable = false)
  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  @Enumerated(EnumType.STRING)
  public TherapyTemplateTypeEnum getType()
  {
    return type;
  }

  public void setType(final TherapyTemplateTypeEnum type)
  {
    this.type = type;
  }

  @Enumerated(EnumType.STRING)
  public TherapyTemplateModeEnum getTemplateMode()
  {
    return templateMode;
  }

  public void setTemplateMode(final TherapyTemplateModeEnum templateMode)
  {
    this.templateMode = templateMode;
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

  @ManyToOne(targetEntity = TherapyTemplateGroupImpl.class, fetch = FetchType.LAZY)
  public TherapyTemplateGroupImpl getTemplateGroup()
  {
    return templateGroup;
  }

  public void setTemplateGroup(final TherapyTemplateGroupImpl templateGroup)
  {
    this.templateGroup = templateGroup;
  }

  @OneToMany(targetEntity = TherapyTemplateElementImpl.class, mappedBy = "therapyTemplate", fetch = FetchType.LAZY)
  public Set<TherapyTemplateElementImpl> getTherapyTemplateElements()
  {
    return therapyTemplateElements;
  }

  public void setTherapyTemplateElements(final Set<TherapyTemplateElementImpl> therapyTemplateElements)
  {
    this.therapyTemplateElements = therapyTemplateElements;
  }

  @OneToMany(targetEntity = TherapyTemplatePreconditionImpl.class, mappedBy = "therapyTemplate", fetch = FetchType.LAZY)
  public Set<TherapyTemplatePreconditionImpl> getPreconditions()
  {
    return preconditions;
  }

  public void setPreconditions(final Set<TherapyTemplatePreconditionImpl> preconditions)
  {
    this.preconditions = preconditions;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("name", name)
        .append("type", type)
        .append("templateMode", templateMode)
        .append("userId", userId)
        .append("careProviderId", careProviderId)
        .append("patientId", patientId)
        .append("templateGroup", templateGroup)
        .append("therapyTemplateElements", therapyTemplateElements)
        .append("preconditions", preconditions)
    ;
  }
}
