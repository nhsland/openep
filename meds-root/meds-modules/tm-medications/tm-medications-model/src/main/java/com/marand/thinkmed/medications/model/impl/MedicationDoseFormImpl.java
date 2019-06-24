package com.marand.thinkmed.medications.model.impl;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;

import com.marand.thinkmed.medications.api.internal.dto.DoseFormType;
import com.marand.thinkmed.medications.api.internal.dto.MedicationOrderFormType;
import com.marand.thinkmed.medications.model.impl.core.AbstractCatalogEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */
@Entity
@Table(indexes = {
    @Index(name = "xpMedDoseFormCode", columnList = "code")
})
public class MedicationDoseFormImpl extends AbstractCatalogEntity
{
  private MedicationOrderFormType medicationOrderFormType;
  private DoseFormType doseFormType;

  @Enumerated(EnumType.STRING)
  public MedicationOrderFormType getMedicationOrderFormType()
  {
    return medicationOrderFormType;
  }

  public void setMedicationOrderFormType(final MedicationOrderFormType medicationOrderFormType)
  {
    this.medicationOrderFormType = medicationOrderFormType;
  }

  @Enumerated(EnumType.STRING)
  public DoseFormType getDoseFormType()
  {
    return doseFormType;
  }

  public void setDoseFormType(final DoseFormType doseFormType)
  {
    this.doseFormType = doseFormType;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb
        .append("medicationOrderFormType", medicationOrderFormType)
        .append("doseFormType", doseFormType)
    ;
  }
}
