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
@Table(name = "medication_formulary_org", indexes = {
    @Index(name = "xfMedFormOrgMedication", columnList = "medication_id")})
public class MedicationFormularyOrganization extends AbstractPermanentEntity
{
  private MedicationImpl medication;
  private String organizationCode;
  private Integer sortOrder;

  @ManyToOne(targetEntity = MedicationImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationImpl getMedication()
  {
    return medication;
  }

  public void setMedication(final MedicationImpl medication)
  {
    this.medication = medication;
  }

  public String getOrganizationCode()
  {
    return organizationCode;
  }

  public void setOrganizationCode(final String organizationCode)
  {
    this.organizationCode = organizationCode;
  }

  public Integer getSortOrder()
  {
    return sortOrder;
  }

  public void setSortOrder(final Integer sortOrder)
  {
    this.sortOrder = sortOrder;
  }
}
