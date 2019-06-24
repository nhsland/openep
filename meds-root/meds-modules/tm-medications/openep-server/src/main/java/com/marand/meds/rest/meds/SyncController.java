package com.marand.meds.rest.meds;

import com.marand.thinkmed.medications.sync.SyncServiceImpl;
import com.marand.thinkmed.meds.config.dto.AtcClassificationDto;
import com.marand.thinkmed.meds.config.dto.DoseFormDto;
import com.marand.thinkmed.meds.config.dto.MedicationGenericDto;
import com.marand.thinkmed.meds.config.dto.customgroup.CustomGroupDto;
import com.marand.thinkmed.meds.config.dto.indications.IndicationDto;
import com.marand.thinkmed.meds.config.dto.ingredient.IngredientDto;
import com.marand.thinkmed.meds.config.dto.medication.full.FullMedicationDto;
import com.marand.thinkmed.meds.config.dto.property.PropertyDto;
import com.marand.thinkmed.meds.config.dto.route.RouteDto;
import com.marand.thinkmed.meds.config.dto.unit.UnitDto;
import com.marand.thinkmed.meds.config.dto.unit.UnitTypeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Nejc Korasa
 */

@RestController
@RequestMapping("/sync")
public class SyncController
{
  private final SyncServiceImpl syncService;

  @Autowired
  public SyncController(final SyncServiceImpl syncService)
  {
    this.syncService = syncService;
  }

  @PostMapping(value = "/medication", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncMedication(@RequestBody final FullMedicationDto medication)
  {
    syncService.syncMedication(checkNotNull(medication, "medication"));
  }

  @PostMapping(value = "/route", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncRoute(@RequestBody final RouteDto routeDto)
  {
    syncService.syncRoute(checkNotNull(routeDto, "routeDto"));
  }

  @PostMapping(value = "/doseform", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncDoseFrom(@RequestBody final DoseFormDto doseFormDto)
  {
    syncService.syncDoseForm(checkNotNull(doseFormDto, "doseFormDto"));
  }

  @PostMapping(value = "/customgroup", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncCustomGroup(@RequestBody final CustomGroupDto customGroupDto)
  {
    syncService.syncCustomGroup(checkNotNull(customGroupDto, "customGroupDto"));
  }

  @PostMapping(value = "/generic", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncGeneric(@RequestBody final MedicationGenericDto generic)
  {
    syncService.syncGeneric(checkNotNull(generic, "generic"));
  }

  @PostMapping(value = "/property", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncProperty(@RequestBody final PropertyDto propertyDto)
  {
    syncService.syncProperty(checkNotNull(propertyDto, "propertyDto"));
  }

  @PostMapping(value = "/unit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncUnit(@RequestBody final UnitDto unitDto)
  {
    syncService.syncUnit(checkNotNull(unitDto, "unitDto"));
  }

  @PostMapping(value = "/unittype", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncUnitType(@RequestBody final UnitTypeDto unitTypeDto)
  {
    syncService.syncUnitType(checkNotNull(unitTypeDto, "unitTypeDto"));
  }

  @PostMapping(value = "/indication", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncIndication(@RequestBody final IndicationDto indicationDto)
  {
    syncService.syncIndication(checkNotNull(indicationDto, "indicationDto"));
  }

  @PostMapping(value = "/ingredient", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncIngredient(@RequestBody final IngredientDto ingredientDto)
  {
    syncService.syncIngredient(checkNotNull(ingredientDto, "ingredientDto"));
  }

  @PostMapping(value = "/atc", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public void syncAtc(@RequestBody final AtcClassificationDto atcDto)
  {
    syncService.syncAtc(checkNotNull(atcDto, "atcDto"));
  }

  @GetMapping(value = "/ping")
  public String ping()
  {
    return "pong";
  }
}
