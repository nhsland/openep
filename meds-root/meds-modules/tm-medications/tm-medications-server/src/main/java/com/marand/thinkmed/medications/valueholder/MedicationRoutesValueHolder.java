package com.marand.thinkmed.medications.valueholder;

import java.util.Map;

import com.marand.maf.core.valueholder.ValueHolder;
import com.marand.thinkmed.medications.dao.MedicationsDao;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Mitja Lapajne
 */
@Component
public class MedicationRoutesValueHolder extends ValueHolder<Map<Long, MedicationRouteDto>>
{
  private MedicationsDao medicationsDao;

  @Autowired
  public void setMedicationsDao(final MedicationsDao medicationsDao)
  {
    this.medicationsDao = medicationsDao;
  }

  @Override
  protected String getKey()
  {
    return "MEDICATIONS"; // uses same key as MedicationsValueHolder for easier use
  }

  @Override
  protected Map<Long, MedicationRouteDto> loadValue()
  {
    return medicationsDao.loadRoutesMap();
  }
}
