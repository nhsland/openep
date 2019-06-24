package com.marand.meds.rest.meds;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Lists;
import com.marand.auditing.auditing.Auditable;
import com.marand.auditing.auditing.AuditableType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.Opt;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.html.Parameter;
import com.marand.thinkmed.html.components.tree.TreeNodeData;
import com.marand.thinkmed.medications.api.internal.dto.MedicationFinderFilterEnum;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.dto.FormularyMedicationDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.dto.barcode.BarcodeTaskSearchDto;
import com.marand.thinkmed.medications.dto.unit.UnitsHolderDto;
import com.marand.thinkmed.medications.service.MedicationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Boris Marn.
 */

@RestController
@RequestMapping("/medications")
public class MedicationController
{
  private final MedicationsService medicationsService;

  @Autowired
  public MedicationController(final MedicationsService medicationsService)
  {
    this.medicationsService = medicationsService;
  }

  @GetMapping(value = "findmedications")
  public List<TreeNodeData> findMedications(
      @RequestParam("searchQuery") final String searchQuery,
      @RequestParam(value = "additionalFilters", required = false) final String additionalFilters,
      @RequestParam("language") final String language)
  {
    final List<String> filterStrings =
        additionalFilters != null
        ? Arrays.asList(JsonUtil.fromJson(additionalFilters, String[].class))
        : Collections.emptyList();

    final EnumSet<MedicationFinderFilterEnum> filterEnumSet = EnumSet.noneOf(MedicationFinderFilterEnum.class);
    filterStrings.forEach(filterString -> filterEnumSet.add(MedicationFinderFilterEnum.valueOf(filterString)));

    return medicationsService.findMedications(searchQuery, filterEnumSet, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "medicationdata")
  public MedicationDataDto getMedicationData(@RequestParam("medicationId") final long medicationId)
  {
    return medicationsService.getMedicationData(medicationId);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "medicationDataForMultipleIds")
  public Collection<MedicationDataDto> getMedicationDataForMultipleIds(
      @RequestParam("medicationIds") final String medicationIds)
  {
    final Long[] medicationIdsArray = JsonUtil.fromJson(medicationIds, Long[].class);
    final Set<Long> medicationIdsList = new HashSet<>(Arrays.asList(medicationIdsArray));
    return medicationsService.getMedicationDataMap(medicationIdsList).values();
  }

  @GetMapping(value = "getMedicationRoutes")
  public List<MedicationRouteDto> getMedicationRoutes(
      @RequestParam("medicationId") final long medicationId)
  {
    return medicationsService.getMedicationRoutes(medicationId);
  }

  @GetMapping(value = "getMedicationDocument", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> getMedicationDocument(
      @RequestParam("reference") final String reference)
  {
    final byte[] document = medicationsService.getMedicationDocument(reference);
    return ResponseEntity.ok(document);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "findSimilarMedications", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<TreeNodeData> findSimilarMedications(
      @RequestParam("medicationId") final Long medicationId,
      @RequestParam("language") final String language)
  {
    return medicationsService.findSimilarMedications(medicationId, new Locale(language));
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "findMedicationProducts", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MedicationDto> findMedicationProducts(
      @RequestParam("medicationId") final Long medicationId,
      @RequestParam("routeIds") final Parameter.Longs routeIds,// TODO change to List<Long> when Igor fixes Igy Framework bug
      @RequestParam(value = "releaseType", required = false) final String releaseType,
      @RequestParam(value = "releaseHours", required = false) final Integer releaseHours)
  {
    //noinspection Convert2MethodRef
    return medicationsService.findMedicationProducts(
        medicationId,
        Opt.resolve(() -> routeIds.getValue()).map(Lists::newArrayList).orElseGet(Lists::newArrayList),
        Opt.of(releaseType).map(type -> new ReleaseDetailsDto(ReleaseType.valueOf(type), releaseHours)).orElse(null));
  }

  @ResponseBody
  @GetMapping(value = "getRoutes", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<MedicationRouteDto> getRoutes()
  {
    return medicationsService.getRoutes();
  }

  @GetMapping(value = "getDoseForms", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<DoseFormDto> getDoseForms()
  {
    return medicationsService.getDoseForms();
  }

  @GetMapping(value = "unitsHolder", produces = MediaType.APPLICATION_JSON_VALUE)
  public UnitsHolderDto getUnitsHolder()
  {
    return medicationsService.getUnitsHolder();
  }

  @GetMapping(value = "getCareProfessionals", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<NamedExternalDto> getCareProfessionals()
  {
    return medicationsService.getCareProfessionals();
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getMedicationIdForBarcode", produces = MediaType.APPLICATION_JSON_VALUE)
  public Long getMedicationIdForBarcode(
      @RequestParam("barcode") final String barcode)
  {
    return medicationsService.getMedicationIdForBarcode(barcode);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getAdministrationTaskForBarcode", produces = MediaType.APPLICATION_JSON_VALUE)
  public BarcodeTaskSearchDto getAdministrationTaskForBarcode(
      @RequestParam("patientId") final String patientId,
      @RequestParam("medicationBarcode") final String medicationBarcode)
  {
    return medicationsService.getAdministrationTaskForBarcode(patientId, medicationBarcode);
  }

  @GetMapping(value = "getMedicationExternalId", produces = MediaType.TEXT_PLAIN_VALUE)
  public String getMedicationExternalId(
      @RequestParam("externalSystem") final String externalSystem,
      @RequestParam("medicationId") final Long medicationId)
  {
    return medicationsService.getMedicationExternalId(externalSystem, medicationId);
  }

  @Auditable(AuditableType.FULL)
  @GetMapping(value = "getOriginalTherapyId", produces = MediaType.APPLICATION_JSON_VALUE)
  public String getOriginalTherapyId(
      @RequestParam("patientId") final String patientId,
      @RequestParam("therapyId") final String therapyId)
  {
    return medicationsService.getOriginalTherapyId(patientId, therapyId);
  }

  @GetMapping(value = "getVmpMedications", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FormularyMedicationDto> getVmpMedications(@RequestParam("vtmId") final String vtmId)
  {
    return medicationsService.getVmpMedications(vtmId);
  }

  @GetMapping(value = "getUnlicensedMedicationWarning", produces = MediaType.APPLICATION_JSON_VALUE)
  public String getUnlicensedMedicationWarning(
      @RequestParam("language") final String language)
  {
    return medicationsService.getUnlicensedMedicationWarning(new Locale(language));
  }

  @GetMapping(value = "getCurrentUserCareProviders", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<NamedExternalDto> getCurrentUserCareProviders()
  {
    return medicationsService.getCurrentUserCareProviders();
  }
}
