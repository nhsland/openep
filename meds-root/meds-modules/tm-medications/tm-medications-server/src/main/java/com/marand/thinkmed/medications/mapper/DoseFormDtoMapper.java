package com.marand.thinkmed.medications.mapper;

import com.marand.maf.core.data.mapper.AbstractSimpleCatalogIdentityMapper;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.model.impl.MedicationDoseFormImpl;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class DoseFormDtoMapper extends AbstractSimpleCatalogIdentityMapper<MedicationDoseFormImpl, DoseFormDto>
{
  @Override
  protected Class<DoseFormDto> getDestinationType()
  {
    return DoseFormDto.class;
  }

  @Override
  protected void mapSimpleCatalogIdentity(final MedicationDoseFormImpl from, final DoseFormDto to, final DateTime timestamp)
  {
    to.setDoseFormType(from.getDoseFormType());
    to.setMedicationOrderFormType(from.getMedicationOrderFormType());
  }
}
