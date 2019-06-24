package com.marand.thinkmed.medications.api.internal.dto;

import com.marand.maf.core.data.object.SimpleCatalogIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Bostjan Vester
 */
public class DoseFormDto extends SimpleCatalogIdentityDto implements JsonSerializable
{
  private DoseFormType doseFormType;
  private MedicationOrderFormType medicationOrderFormType;

  public DoseFormType getDoseFormType()
  {
    return doseFormType;
  }

  public void setDoseFormType(final DoseFormType doseFormType)
  {
    this.doseFormType = doseFormType;
  }

  public MedicationOrderFormType getMedicationOrderFormType()
  {
    return medicationOrderFormType;
  }

  public void setMedicationOrderFormType(final MedicationOrderFormType medicationOrderFormType)
  {
    this.medicationOrderFormType = medicationOrderFormType;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb.append("doseFormType", doseFormType);
    tsb.append("medicationOrderFormType", medicationOrderFormType);
  }
}
