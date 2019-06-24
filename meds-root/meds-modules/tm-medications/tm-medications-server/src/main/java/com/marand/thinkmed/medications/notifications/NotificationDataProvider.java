package com.marand.thinkmed.medications.notifications;

import java.util.stream.Collectors;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */

@Component
public class NotificationDataProvider
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Transactional
  @EhrSessioned
  public String getMedicationName(final String patientId, final String compositionUid)
  {
    return medicationsOpenEhrDao.loadInpatientPrescription(patientId, TherapyIdUtils.getCompositionUidWithoutVersion(compositionUid))
        .getMedicationOrder()
        .getMedicationItem()
        .getValue();
  }

  public String getMedicationName(final TherapyDto therapyDto)
  {
    return therapyDto.getMedications().stream().map(MedicationDto::getDisplayName).collect(Collectors.joining(" + "));
  }

  public String getLocalisedDescription(final String key, final Object... params)
  {
    return Dictionary.getMessageWithLocale(key, DefinedLocaleHolder.INSTANCE.getCalculatedNotNullLocale(), params);
  }
}
