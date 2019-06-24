package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
@Entity
@Table(indexes = @Index(name = "xfTherapyTempMemberTherapyTemp", columnList = "therapy_template_id"))
public class TherapyTemplateElementImpl extends AbstractPermanentEntity
{
  private TherapyTemplateImpl therapyTemplate;
  private String therapy;
  private Boolean completed;
  private Boolean recordAdministration;

  @ManyToOne(targetEntity = TherapyTemplateImpl.class, fetch = FetchType.LAZY, optional = false)
  public TherapyTemplateImpl getTherapyTemplate()
  {
    return therapyTemplate;
  }

  public void setTherapyTemplate(final TherapyTemplateImpl therapyTemplate)
  {
    this.therapyTemplate = therapyTemplate;
  }

  @Lob
  public String getTherapy()
  {
    return therapy;
  }

  public void setTherapy(final String therapy)
  {
    this.therapy = therapy;
  }

  @Column(nullable = false)
  public Boolean getCompleted()
  {
    return completed;
  }

  public void setCompleted(final Boolean completed)
  {
    this.completed = completed;
  }

  public Boolean getRecordAdministration()
  {
    return recordAdministration;
  }

  public void setRecordAdministration(final Boolean recordAdministration)
  {
    this.recordAdministration = recordAdministration;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("therapyTemplate", therapyTemplate)
        .append("therapy", therapy)
        .append("completed", completed)
        .append("recordAdministration", recordAdministration);
  }
}
