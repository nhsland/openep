package com.marand.thinkmed.medications.dto.administration;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */

public class InfusionBagTaskDto extends AdministrationTaskDto
{
  private InfusionBagDto infusionBag;

  public void setInfusionBag(final InfusionBagDto infusionBag)
  {
    this.infusionBag = infusionBag;
  }

  public InfusionBagDto getInfusionBag()
  {
    return infusionBag;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("infusionBag", infusionBag);
  }
}
