package com.marand.thinkmed.medications.units.mapper;

import com.marand.maf.core.data.mapper.AbstractIdentityMapper;
import com.marand.thinkmed.medications.dto.unit.MedicationUnitTypeDto;
import com.marand.thinkmed.medications.model.impl.MedicationUnitTypeImpl;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class MedicationUnitTypeDtoMapper extends AbstractIdentityMapper<MedicationUnitTypeImpl, MedicationUnitTypeDto>
{
  /**
   * @implNote code is mapped to name and name is mapped to displayName!
   */
  @Override
  protected void mapIdentity(final MedicationUnitTypeImpl from, final MedicationUnitTypeDto to, final DateTime timestamp)
  {
    to.setFactor(from.getFactor());
    to.setName(from.getCode());
    to.setGroup(from.getUnitGroup());
    to.setDisplayName(from.getName());
  }

  @Override
  protected Class<MedicationUnitTypeDto> getDestinationType()
  {
    return MedicationUnitTypeDto.class;
  }
}
