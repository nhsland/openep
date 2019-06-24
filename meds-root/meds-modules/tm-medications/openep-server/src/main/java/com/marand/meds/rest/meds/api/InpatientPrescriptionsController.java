package com.marand.meds.rest.meds.api;

import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.thinkmed.medications.service.MedicationsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Mitja Lapajne
 */

@RestController
@Api(tags = "Inpatient Prescriptions", produces = APPLICATION_JSON_VALUE)
@RequestMapping(value = "api/inpatient-prescriptions")
public class InpatientPrescriptionsController
{
  private MedicationsService service;

  @Autowired
  public void setService(final MedicationsService service)
  {
    this.service = service;
  }

  @Auditable(AuditableType.FULL)
  @ApiOperation(value = "Suspend all on temporary leave", notes = "Suspends all inpatient prescriptions when patient goes on temporary leave")
  @PostMapping(value = "actions/temporary-leave", produces = MediaType.APPLICATION_JSON_VALUE)
  public void list(@RequestParam("patientId") final String patientId)
  {
    service.suspendAllTherapiesOnTemporaryLeave(patientId);
  }

  @Auditable(AuditableType.FULL)
  @ApiOperation(value = "Reissue on return from temporary leave", notes = "Reissues inpatient prescriptions that were suspended when patient went on temporary leave")
  @PostMapping(value = "actions/return-from-temporary-leave", produces = MediaType.APPLICATION_JSON_VALUE)
  public void summary(@RequestParam("patientId") final String patientId)
  {
    service.reissueAllTherapiesOnReturnFromTemporaryLeave(patientId);
  }
}
