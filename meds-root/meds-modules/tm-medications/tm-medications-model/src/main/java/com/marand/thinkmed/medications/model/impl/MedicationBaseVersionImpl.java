package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.model.impl.core.AbstractTemporalEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
@Entity
@Table(indexes = {
    @Index(name = "xfMedBaseVerAdmUnit", columnList = "administration_unit_id"),
    @Index(name = "xfMedBaseVerSuppUnit", columnList = "supply_unit_id"),
    @Index(name = "xfMedBaseVerGeneric", columnList = "medication_generic_id"),
    @Index(name = "xfMedBaseVerDoseForm", columnList = "dose_form_id"),
    @Index(name = "xfMedBaseVerAtcClass", columnList = "atc_classification_id")
})
public class MedicationBaseVersionImpl extends AbstractTemporalEntity
{
  private MedicationBaseImpl medicationBase;

  private MedicationUnitImpl administrationUnit;
  private Double administrationUnitFactor;
  private MedicationUnitImpl supplyUnit;
  private Double supplyUnitFactor;

  private Boolean descriptiveDose;
  private AtcClassificationImpl atcClassification;
  private MedicationGenericImpl medicationGeneric;
  private MedicationDoseFormImpl doseForm;
  private TitrationType titration;
  private Double roundingFactor;

  @ManyToOne(targetEntity = MedicationBaseImpl.class, fetch = FetchType.LAZY, optional = false)
  public MedicationBaseImpl getMedicationBase()
  {
    return medicationBase;
  }

  public void setMedicationBase(final MedicationBaseImpl medicationBase)
  {
    this.medicationBase = medicationBase;
  }

  public Double getAdministrationUnitFactor()
  {
    return administrationUnitFactor;
  }

  public void setAdministrationUnitFactor(final Double administrationUnitFactor)
  {
    this.administrationUnitFactor = administrationUnitFactor;
  }

  @ManyToOne(targetEntity = MedicationUnitImpl.class, fetch = FetchType.LAZY)
  public MedicationUnitImpl getAdministrationUnit()
  {
    return administrationUnit;
  }

  public void setAdministrationUnit(final MedicationUnitImpl administrationUnit)
  {
    this.administrationUnit = administrationUnit;
  }

  @ManyToOne(targetEntity = MedicationUnitImpl.class, fetch = FetchType.LAZY)
  public MedicationUnitImpl getSupplyUnit()
  {
    return supplyUnit;
  }

  public void setSupplyUnit(final MedicationUnitImpl supplyUnit)
  {
    this.supplyUnit = supplyUnit;
  }

  public Double getSupplyUnitFactor()
  {
    return supplyUnitFactor;
  }

  public void setSupplyUnitFactor(final Double supplyUnitFactor)
  {
    this.supplyUnitFactor = supplyUnitFactor;
  }

  @ManyToOne(targetEntity = MedicationGenericImpl.class, fetch = FetchType.LAZY)
  public MedicationGenericImpl getMedicationGeneric()
  {
    return medicationGeneric;
  }

  public void setMedicationGeneric(final MedicationGenericImpl medicationGeneric)
  {
    this.medicationGeneric = medicationGeneric;
  }

  @ManyToOne(targetEntity = MedicationDoseFormImpl.class, fetch = FetchType.LAZY)
  public MedicationDoseFormImpl getDoseForm()
  {
    return doseForm;
  }

  @ManyToOne(targetEntity = MedicationVersionImpl.class, fetch = FetchType.LAZY)
  public void setDoseForm(final MedicationDoseFormImpl doseForm)
  {
    this.doseForm = doseForm;
  }

  @ManyToOne(targetEntity = AtcClassificationImpl.class, fetch = FetchType.LAZY)
  public AtcClassificationImpl getAtcClassification()
  {
    return atcClassification;
  }

  public void setAtcClassification(final AtcClassificationImpl atcClassification)
  {
    this.atcClassification = atcClassification;
  }

  @Enumerated(EnumType.STRING)
  public TitrationType getTitration()
  {
    return titration;
  }

  public void setTitration(final TitrationType titration)
  {
    this.titration = titration;
  }

  public Double getRoundingFactor()
  {
    return roundingFactor;
  }

  public void setRoundingFactor(final Double roundingFactor)
  {
    this.roundingFactor = roundingFactor;
  }

  public Boolean isDescriptiveDose()
  {
    return descriptiveDose;
  }

  public void setDescriptiveDose(final Boolean descriptiveDose)
  {
    this.descriptiveDose = descriptiveDose;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);

    tsb
        .append("medicationBase", medicationBase)
        .append("administrationUnit", administrationUnit)
        .append("administrationUnitFactor", administrationUnitFactor)
        .append("supplyUnit", supplyUnit)
        .append("supplyUnitFactor", supplyUnitFactor)
        .append("atcClassification", atcClassification)
        .append("medicationGeneric", medicationGeneric)
        .append("doseForm", doseForm)
        .append("titration", titration)
        .append("roundingFactor", roundingFactor)
        .append("descriptiveDose", descriptiveDose)
    ;
  }
}
