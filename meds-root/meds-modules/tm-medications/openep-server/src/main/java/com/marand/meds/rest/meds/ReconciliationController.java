package com.marand.meds.rest.meds;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.maf.core.JsonUtil;
import com.marand.thinkmed.medications.MedsJsonDeserializer;
import com.marand.thinkmed.medications.dto.DispenseSourceDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionDto;
import com.marand.thinkmed.medications.dto.admission.MedicationOnAdmissionGroupDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeDto;
import com.marand.thinkmed.medications.dto.discharge.MedicationOnDischargeGroupDto;
import com.marand.thinkmed.medications.dto.reconsiliation.ReconciliationSummaryDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Boris Marn
 */
@RestController
@RequestMapping("/medications")
public class ReconciliationController
{
  private final MedicationsService medicationsService;

  @Autowired
  public ReconciliationController(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "saveMedicationsOnAdmission", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<String> saveMedicationsOnAdmission(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapies") final String therapiesJson,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam("language") final String language)
  {
    final MedicationOnAdmissionDto[] therapies =
        JsonUtil.fromJson(
            therapiesJson,
            MedicationOnAdmissionDto[].class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    return medicationsService.saveMedicationsOnAdmission(
        patientId,
        Arrays.asList(therapies),
        centralCaseId,
        careProviderId,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "startNewReconciliation", produces = MediaType.APPLICATION_JSON_VALUE)
  public void startNewReconciliation(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId)
  {
    medicationsService.startNewReconciliation(patientId, centralCaseId, careProviderId);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "reviewAdmission", produces = MediaType.APPLICATION_JSON_VALUE)
  public void reviewAdmission(@RequestParam("patientId") final String patientId)
  {
    medicationsService.reviewAdmission(patientId);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @PostMapping(value = "reviewDischarge", produces = MediaType.APPLICATION_JSON_VALUE)
  public void reviewDischarge(@RequestParam("patientId") final String patientId)
  {
    medicationsService.reviewDischarge(patientId);
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(value = "getMedicationsOnAdmission", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MedicationOnAdmissionDto> getMedicationsOnAdmission(
      @RequestParam("patientId") final String patientId,
      @RequestParam("validateTherapy") final boolean validateTherapy,
      @RequestParam("language") final String language)
  {
    return medicationsService.getMedicationsOnAdmission(patientId, validateTherapy, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @PostMapping(value = "saveMedicationsOnDischarge", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<String> saveMedicationsOnDischarge(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapies") final String therapiesJson,
      @RequestParam(value = "centralCaseId", required = false) final String centralCaseId,
      @RequestParam(value = "careProviderId", required = false) final String careProviderId,
      @RequestParam("language") final String language)
  {
    final MedicationOnDischargeDto[] therapies =
        JsonUtil.fromJson(
            therapiesJson,
            MedicationOnDischargeDto[].class,
            MedsJsonDeserializer.INSTANCE.getTypeAdapters());

    return medicationsService.saveMedicationsOnDischarge(
        patientId,
        Arrays.asList(therapies),
        centralCaseId,
        careProviderId,
        new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getMedicationsOnDischarge", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MedicationOnDischargeDto> getMedicationsOnDischarge(
      @RequestParam("patientId") final String patientId,
      @RequestParam("language") final String language)
  {
    return medicationsService.getMedicationsOnDischarge(patientId, new Locale(language));
  }

  /**
   * Return true if discharge list has already been created in the latest reconciliation.
   */
  @Auditable(AuditableType.FULL)
  @GetMapping(value = "dischargeCreated", produces = MediaType.APPLICATION_JSON_VALUE)
  public boolean isDischargeCreated(@RequestParam("patientId") final String patientId)
  {
    return medicationsService.isDischargeCreated(patientId);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getReconciliationGroups", produces = MediaType.APPLICATION_JSON_VALUE)
  public ReconciliationSummaryDto getReconciliationGroups(
      @RequestParam("patientId") final String patientId,
      @RequestParam("language") final String language)
  {
    return medicationsService.getReconciliationSummary(patientId, new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(value = "getTherapiesOnAdmissionGroups", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MedicationOnAdmissionGroupDto> getTherapiesOnAdmissionGroups(
      @RequestParam("patientId") final String patientId,
      @RequestParam("language") final String language)
  {
    return medicationsService.getTherapiesOnAdmissionGroups(patientId, new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(value = "getTherapiesOnDischargeGroups", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MedicationOnDischargeGroupDto> getTherapiesOnDischargeGroups(
      @RequestParam("patientId") final String patientId,
      @RequestParam(value = "patientHeight", required = false) final Double patientHeight,
      @RequestParam("language") final String language)
  {
    return medicationsService.getTherapiesOnDischargeGroups(patientId, patientHeight, new Locale(language));
  }

  @Auditable(AuditableType.WITHOUT_RESULT)
  @GetMapping(value = "getDispenseSources", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DispenseSourceDto> getDispenseSources()
  {
    return medicationsService.getDispenseSources();
  }
}
