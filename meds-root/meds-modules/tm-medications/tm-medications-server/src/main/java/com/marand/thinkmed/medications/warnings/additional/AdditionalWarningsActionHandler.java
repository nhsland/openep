package com.marand.thinkmed.medications.warnings.additional;

import com.marand.maf.core.Pair;
import com.marand.thinkmed.medications.dao.openehr.MedicationsOpenEhrDao;
import com.marand.thinkmed.medications.dto.warning.AdditionalWarningsActionDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.therapy.util.TherapyIdUtils;
import com.marand.thinkmed.process.service.ProcessService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class AdditionalWarningsActionHandler
{
  private MedicationsOpenEhrDao medicationsOpenEhrDao;
  private ProcessService processService;
  private MedicationsService medicationsService;

  @Autowired
  public void setMedicationsOpenEhrDao(final MedicationsOpenEhrDao medicationsOpenEhrDao)
  {
    this.medicationsOpenEhrDao = medicationsOpenEhrDao;
  }

  @Autowired
  public void setProcessService(final ProcessService processService)
  {
    this.processService = processService;
  }

  @Autowired
  public void setMedicationsService(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  public ProcessService getProcessService()
  {
    return processService;
  }

  public void handleAdditionalWarningsAction(final @NonNull AdditionalWarningsActionDto additionalWarningsActionDto)
  {
    for (final String id : additionalWarningsActionDto.getAbortTherapyIds())
    {
      final Pair<String, String> therapyIdPair = TherapyIdUtils.parseTherapyId(id);
      medicationsService.abortTherapy(
          additionalWarningsActionDto.getPatientId(),
          therapyIdPair.getFirst(),
          therapyIdPair.getSecond(),
          null);
    }

    additionalWarningsActionDto.getOverrideWarnings()
        .forEach(w -> medicationsOpenEhrDao.appendWarningsToTherapy(
            additionalWarningsActionDto.getPatientId(),
            w.getTherapyId(),
            w.getWarnings()));

    additionalWarningsActionDto.getCompleteTaskIds().forEach(id -> processService.completeTasks(id));

  }
}
