package com.marand.thinkmed.medications.dto.unit;

import com.marand.maf.core.data.object.SimpleCatalogIdentityDto;
import com.marand.thinkmed.api.core.JsonSerializable;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Nejc Korasa
 */
public class MedicationUnitDto extends SimpleCatalogIdentityDto implements JsonSerializable
{
  private MedicationUnitTypeDto type;

  public MedicationUnitDto()
  {
  }

  public MedicationUnitDto(final MedicationUnitTypeDto type, final String name, final String code)
  {
    this.type = type;
    setName(name);
    setCode(code);
  }

  public void setType(final MedicationUnitTypeDto type)
  {
    this.type = type;
  }

  public MedicationUnitTypeDto getType()
  {
    return type;
  }

  @Override
  protected void appendToString(final ToStringBuilder tsb)
  {
    tsb
        .append("type", type)
    ;
  }
}
