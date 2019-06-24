package com.marand.thinkmed.medications.administration;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.marand.thinkmed.medications.connector.data.object.PatientDisplayWithLocationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationPatientTaskDto;
import com.marand.thinkmed.medications.dto.administration.AdministrationTaskDto;
import com.marand.thinkmed.process.dto.NewTaskRequestDto;
import com.marand.thinkmed.process.dto.TaskDto;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public interface AdministrationTaskConverter
{
  AdministrationDto buildAdministrationFromTask(AdministrationTaskDto task, DateTime when);

  AdministrationDto buildAdministrationFromTask(TaskDto task, DateTime when);

  AdministrationDto convertNewTaskRequestDtoToAdministrationDto(NewTaskRequestDto taskRequest, DateTime when);

  AdministrationTaskDto convertTaskToAdministrationTask(TaskDto task);

  List<AdministrationPatientTaskDto> convertTasksToAdministrationPatientTasks(
      List<TaskDto> tasks,
      Map<String, PatientDisplayWithLocationDto> patientWithLocationMap,
      Locale locale,
      DateTime when);

  boolean isAdministrationTask(TaskDto task);
}
