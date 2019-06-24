package com.marand.thinkmed.medications.automatic.confirm;

import java.util.Collections;

import com.marand.thinkehr.session.EhrSessioned;
import com.marand.thinkmed.medications.MedicationJobPerformerEnum;
import com.marand.thinkmed.medications.administration.AdministrationHandler;
import com.marand.thinkmed.medications.charting.AutomaticChartingType;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.process.dto.TaskDto;
import com.marand.thinkmed.request.user.UserDto;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nejc Korasa
 */

@Component
public class AdministrationAutoTaskConfirmerHandler
{
  private AdministrationHandler administrationHandler;

  @Autowired
  public void setAdministrationHandler(final AdministrationHandler administrationHandler)
  {
    this.administrationHandler = administrationHandler;
  }

  @EhrSessioned
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void autoConfirmAdministrationTask(
      final @NonNull AutomaticChartingType autoChartingType,
      final @NonNull String patientId,
      final @NonNull InpatientPrescription prescription,
      final @NonNull TaskDto administrationTask,
      final @NonNull DateTime currentTime)
  {
    final UserDto autoChartingUser = new UserDto(
        MedicationJobPerformerEnum.AUTOMATIC_CHARTING_PERFORMER.getId(),
        MedicationJobPerformerEnum.AUTOMATIC_CHARTING_PERFORMER.getCode(),
        MedicationJobPerformerEnum.AUTOMATIC_CHARTING_PERFORMER.getCode(),
        Collections.emptyList());

    administrationHandler.confirmAdministrationTask(
        patientId,
        prescription,
        administrationTask,
        autoChartingType,
        autoChartingUser,
        currentTime);
  }
}
