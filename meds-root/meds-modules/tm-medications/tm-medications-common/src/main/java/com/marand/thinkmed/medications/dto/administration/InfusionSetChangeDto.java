package com.marand.thinkmed.medications.dto.administration;

import com.marand.thinkmed.medications.api.internal.dto.InfusionSetChangeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Mitja Lapajne
 */

public class InfusionSetChangeDto extends AdministrationDto implements InfusionBagAdministration
{
  private InfusionSetChangeEnum infusionSetChangeEnum;

  private InfusionBagDto infusionBag;

  public InfusionSetChangeEnum getInfusionSetChangeEnum()
  {
    return infusionSetChangeEnum;
  }

  public void setInfusionSetChangeEnum(final InfusionSetChangeEnum infusionSetChangeEnum)
  {
    this.infusionSetChangeEnum = infusionSetChangeEnum;
  }

  @Override
  public InfusionBagDto getInfusionBag()
  {
    return infusionBag;
  }

  @Override
  public void setInfusionBag(final InfusionBagDto infusionBag)
  {
    this.infusionBag = infusionBag;
  }

  public InfusionSetChangeDto()
  {
    super(AdministrationTypeEnum.INFUSION_SET_CHANGE);
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    super.appendToString(tsb);
    tsb.append("infusionSetChangeEnum", infusionSetChangeEnum).append("infusionBag", infusionBag);
  }
}
