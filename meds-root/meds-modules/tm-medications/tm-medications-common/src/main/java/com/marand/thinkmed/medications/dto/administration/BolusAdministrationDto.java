package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseDto;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class BolusAdministrationDto extends AdministrationDto implements DoseAdministration
{
  private TherapyDoseDto administeredDose;

  public BolusAdministrationDto()
  {
    super(AdministrationTypeEnum.BOLUS);
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
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("administeredDose", administeredDose);
  }
}
