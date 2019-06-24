package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class AdjustInfusionAdministrationDto extends AdministrationDto
    implements DoseAdministration, PlannedDoseAdministration
{
  private AdjustAdministrationSubtype adjustAdministrationSubtype;
  private TherapyDoseDto administeredDose;
  private TherapyDoseDto plannedDose;
  private boolean differentFromOrder;

  public AdjustInfusionAdministrationDto()
  {
    super(AdministrationTypeEnum.ADJUST_INFUSION);
  }

  protected AdjustInfusionAdministrationDto(final AdjustAdministrationSubtype adjustAdministrationSubtype)
  {
    super(AdministrationTypeEnum.ADJUST_INFUSION);
    this.adjustAdministrationSubtype = adjustAdministrationSubtype;
  }

  public AdjustAdministrationSubtype getAdjustAdministrationSubtype()
  {
    return adjustAdministrationSubtype;
  }

  @Override
  public TherapyDoseDto getAdministeredDose()
  {
    return administeredDose;
  }

  @Override
  public void setAdministeredDose(final TherapyDoseDto administeredDose)
  {
    this.administeredDose = administeredDose;
  }

  @Override
  public TherapyDoseDto getPlannedDose()
  {
    return plannedDose;
  }

  @Override
  public void setPlannedDose(final TherapyDoseDto plannedDose)
  {
    this.plannedDose = plannedDose;
  }

  @Override
  public boolean isDifferentFromOrder()
  {
    return differentFromOrder;
  }

  @Override
  public void setDifferentFromOrder(final boolean differentFromOrder)
  {
    this.differentFromOrder = differentFromOrder;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("adjustAdministrationSubtype", adjustAdministrationSubtype)
        .append("administeredDose", administeredDose)
        .append("plannedDose", plannedDose)
        .append("differentFromOrder", differentFromOrder);
  }
}
