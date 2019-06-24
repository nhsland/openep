package com.marand.meds.rest.meds;

import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.eventbus.EventProducer;
import com.marand.thinkmed.medications.service.MedicationsService;
import com.marand.thinkmed.medications.service.impl.MedicationsServiceEvents.AdministrationChanged;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Boris Marn.
 */
@RestController
@RequestMapping("/medications")
public class TaskController
{
  private final MedicationsService service;

  @Autowired
  public TaskController(final MedicationsService service)
  {
    this.service = service;
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "deleteTask")
  @EventProducer(AdministrationChanged.class)
  public void deleteTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskId,
      @RequestParam(value = "groupUUId", required = false) final String groupUUId,
      @RequestParam(value = "therapyId", required = false) final String therapyId,
      @RequestParam(value = "comment", required = false) final String comment)
  {
    service.deleteTask(patientId, taskId, groupUUId, therapyId, comment);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "rescheduleAdministrationTasks")
  @EventProducer(AdministrationChanged.class)
  public void rescheduleTasks(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskId,
      @RequestParam("newTime") final String newTime,
      @RequestParam("therapyId") final String therapyId)
  {
    service.rescheduleTasks(patientId, taskId, JsonUtil.fromJson(newTime, DateTime.class), therapyId);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "rescheduleAdministrationTask")
  @EventProducer(AdministrationChanged.class)
  public void rescheduleTask(
      @RequestParam("patientId") final String patientId,
      @RequestParam("taskId") final String taskId,
      @RequestParam("newTime") final String newTime,
      @RequestParam("therapyId") final String therapyId)
  {
    service.rescheduleAdministrationTask(patientId, taskId, JsonUtil.fromJson(newTime, DateTime.class), therapyId);
  }

  @PostMapping(value = "rescheduleTherapyDoctorReviewTask")
  public void rescheduleTherapyDoctorReviewTask(
      @RequestParam("taskId") final String taskId,
      @RequestParam("newTime") final String newTime,
      @RequestParam("comment") final String comment)
  {
    service.rescheduleTherapyDoctorReviewTask(taskId, JsonUtil.fromJson(newTime, DateTime.class), comment);
  }

  @PostMapping(value = "rescheduleIvToOralTask")
  public void rescheduleIvToOralTask(
      @RequestParam("taskId") final String taskId,
      @RequestParam("newTime") final String newTime)
  {
    service.rescheduleIvToOralTask(taskId, JsonUtil.fromJson(newTime, DateTime.class));
  }
}
