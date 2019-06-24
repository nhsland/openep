package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.TherapyTemplatePreconditionEnum;
import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = @Index(name = "xfTherapyTempPrecTherapyTemp", columnList = "therapy_template_id"))
public class TherapyTemplatePreconditionImpl extends AbstractPermanentEntity
{
  private TherapyTemplateImpl therapyTemplate;
  private TherapyTemplatePreconditionEnum precondition;
  private Double minValue;
  private Double maxValue;
  private String exactValue;

  @ManyToOne(targetEntity = TherapyTemplateImpl.class, fetch = FetchType.LAZY, optional = false)
  public TherapyTemplateImpl getTherapyTemplate()
  {
    return therapyTemplate;
  }

  public void setTherapyTemplate(final TherapyTemplateImpl therapyTemplate)
  {
    this.therapyTemplate = therapyTemplate;
  }

  @Enumerated(EnumType.STRING)
  public TherapyTemplatePreconditionEnum getPrecondition()
  {
    return precondition;
  }

  public void setPrecondition(final TherapyTemplatePreconditionEnum precondition)
  {
    this.precondition = precondition;
  }

  public Double getMinValue()
  {
    return minValue;
  }

  public void setMinValue(final Double minValue)
  {
    this.minValue = minValue;
  }

  public Double getMaxValue()
  {
    return maxValue;
  }

  public void setMaxValue(final Double maxValue)
  {
    this.maxValue = maxValue;
  }

  public String getExactValue()
  {
    return exactValue;
  }

  public void setExactValue(final String exactValue)
  {
    this.exactValue = exactValue;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb.append("therapyTemplate", therapyTemplate)
        .append("condition", precondition)
        .append("minValue", precondition)
        .append("maxValue", precondition)
        .append("exactValue", precondition);
  }
}
