package com.marand.thinkmed.medications.model.impl;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;

/**
 * @author Nejc Korasa
 */
@Entity
@Table(indexes = @Index(name = "xfMedicationRoute", columnList = "medication_route_id"))
public class MentalHealthTemplateImpl extends AbstractCatalogEntity
{
  private MedicationRouteImpl medicationRoute;
  private List<MentalHealthTemplateMemberImpl> mentalHealthTemplateMemberList;

  @ManyToOne(targetEntity = MedicationRouteImpl.class, optional = true, fetch = FetchType.LAZY)
  public MedicationRouteImpl getMedicationRoute()
  {
    return medicationRoute;
  }

  @OneToMany(targetEntity = MentalHealthTemplateMemberImpl.class, mappedBy = "mentalHealthTemplate", fetch = FetchType.LAZY)
  public List<MentalHealthTemplateMemberImpl> getMentalHealthTemplateMemberList()
  {
    return mentalHealthTemplateMemberList;
  }

  public void setMentalHealthTemplateMemberList(final List<MentalHealthTemplateMemberImpl> mentalHealthTemplateMemberList)
  {
    this.mentalHealthTemplateMemberList = mentalHealthTemplateMemberList;
  }

  public void setMedicationRoute(final MedicationRouteImpl medicationRoute)
  {
    this.medicationRoute = medicationRoute;
  }
}
