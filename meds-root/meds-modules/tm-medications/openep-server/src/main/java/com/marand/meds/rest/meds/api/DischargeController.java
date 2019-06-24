package com.marand.meds.rest.meds.api;

import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.thinkmed.medications.api.external.dto.DischargeListDto;
import com.marand.thinkmed.medications.api.external.dto.DischargeSummaryDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Mitja Lapajne
 */

@RestController
@Api(tags = "Discharge", produces = APPLICATION_JSON_VALUE)
@RequestMapping(value = "api/discharge")
public class DischargeController
{
  private MedicationsService service;

  @Autowired
  public void setService(final MedicationsService service)
  {
    this.service = service;
  }

  @Auditable(AuditableType.WITHOUT_PARAMETERS)
  @ApiOperation(value = "Get discharge list", notes="Returns latest discharge list for the given patient identifier.")
  @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_VALUE)
  public DischargeListDto list(@RequestParam("patientId") final String patientId)
  {
    return service.getDischargeList(patientId);
  }

  @Auditable(AuditableType.WITHOUT_PARAMETERS)
  @ApiOperation(value = "Get discharge summary", notes="Returns latest discharge summary for the given patient identifier.")
  @GetMapping(value = "summary", produces = MediaType.APPLICATION_JSON_VALUE)
  public DischargeSummaryDto summary(@RequestParam("patientId") final String patientId)
  {
    return service.getDischargeSummary(patientId);
  }
}
