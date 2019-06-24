package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractPermanentEntity;

/**
 * @author Nejc Korasa
 */
@Entity
@Table(indexes = {
    @Index(name = "xfMedication", columnList = "medication_id"),
    @Index(name = "xfMedicationTemplate", columnList = "mental_health_template_id")})
public class MentalHealthTemplateMemberImpl extends AbstractPermanentEntity
{
  private MedicationImpl medication;
  private MentalHealthTemplateImpl mentalHealthTemplate;

  @ManyToOne(targetEntity = MedicationImpl.class, optional = false, fetch = FetchType.LAZY)
  public MedicationImpl getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationImpl medication)
  {
    this.medication = medication;
  }

  @ManyToOne(targetEntity = MentalHealthTemplateImpl.class, optional = false, fetch = FetchType.LAZY)
  public MentalHealthTemplateImpl getMentalHealthTemplate()
  {
    return mentalHealthTemplate;
  }

  public void setMentalHealthTemplate(final MentalHealthTemplateImpl medicationGroupTemplate)
  {
    this.mentalHealthTemplate = medicationGroupTemplate;
  }
}
