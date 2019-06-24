package com.marand.thinkmed.medications.sync.updaters;

import com.marand.maf.core.server.entity.updater.entity.EntityUpdaterImpl;
import com.marand.thinkmed.medications.model.impl.AtcClassificationImpl;
import com.marand.thinkmed.medications.model.impl.MedicationCustomGroupImpl;
import com.marand.thinkmed.medications.model.impl.MedicationDoseFormImpl;
import com.marand.thinkmed.medications.model.impl.MedicationGenericImpl;
import com.marand.thinkmed.medications.model.impl.MedicationImpl;
import com.marand.thinkmed.medications.model.impl.MedicationIndicationImpl;
import com.marand.thinkmed.medications.model.impl.MedicationIngredientImpl;
import com.marand.thinkmed.medications.model.impl.MedicationPropertyImpl;
import com.marand.thinkmed.medications.model.impl.MedicationRouteImpl;
import com.marand.thinkmed.medications.model.impl.MedicationUnitImpl;
import com.marand.thinkmed.medications.model.impl.MedicationUnitTypeImpl;
import com.marand.thinkmed.medications.valueholder.MedicationRoutesValueHolder;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolder;
import com.marand.thinkmed.meds.config.dto.AtcClassificationDto;
import com.marand.thinkmed.meds.config.dto.DoseFormDto;
import com.marand.thinkmed.meds.config.dto.MedicationGenericDto;
import com.marand.thinkmed.meds.config.dto.ValidatableDto;
import com.marand.thinkmed.meds.config.dto.customgroup.CustomGroupDto;
import com.marand.thinkmed.meds.config.dto.indications.IndicationDto;
import com.marand.thinkmed.meds.config.dto.ingredient.IngredientDto;
import com.marand.thinkmed.meds.config.dto.medication.full.FullMedicationDto;
import com.marand.thinkmed.meds.config.dto.property.PropertyDto;
import com.marand.thinkmed.meds.config.dto.route.RouteDto;
import com.marand.thinkmed.meds.config.dto.unit.UnitDto;
import com.marand.thinkmed.meds.config.dto.unit.UnitTypeDto;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */

@Component
public class SyncUpdater
{
  private static final Logger LOG = LoggerFactory.getLogger(SyncUpdater.class);

  // Non versional updaters - use shared entity updater to load entities based on uuid
  private final EntityUpdaterImpl<MedicationImpl, FullMedicationDto> medicationSharedUpdater;
  private final EntityUpdaterImpl<MedicationRouteImpl, RouteDto> routeSharedUpdater;
  private final EntityUpdaterImpl<MedicationDoseFormImpl, DoseFormDto> doseFormUpdater;
  private final EntityUpdaterImpl<MedicationCustomGroupImpl, CustomGroupDto> customGroupUpdater;
  private final EntityUpdaterImpl<MedicationGenericImpl, MedicationGenericDto> genericUpdater;
  private final EntityUpdaterImpl<MedicationPropertyImpl, PropertyDto> propertyUpdater;
  private final EntityUpdaterImpl<MedicationUnitImpl, UnitDto> unitUpdater;
  private final EntityUpdaterImpl<MedicationUnitTypeImpl, UnitTypeDto> unitTypeUpdater;
  private final EntityUpdaterImpl<MedicationIngredientImpl, IngredientDto> ingredientUpdater;
  private final EntityUpdaterImpl<MedicationIndicationImpl, IndicationDto> indicationUpdater;
  private final EntityUpdaterImpl<AtcClassificationImpl, AtcClassificationDto> atcUpdater;

  private final MedicationsValueHolder medicationsValueHolder;
  private final MedicationRoutesValueHolder routesValueHolder;

  @Autowired
  public SyncUpdater(
      final MedicationsValueHolder medicationsValueHolder,
      final EntityUpdaterImpl<MedicationImpl, FullMedicationDto> medicationSharedUpdater,
      final EntityUpdaterImpl<MedicationRouteImpl, RouteDto> routeSharedUpdater,
      final EntityUpdaterImpl<MedicationDoseFormImpl, DoseFormDto> doseFormUpdater,
      final EntityUpdaterImpl<MedicationCustomGroupImpl, CustomGroupDto> customGroupUpdater,
      final EntityUpdaterImpl<MedicationGenericImpl, MedicationGenericDto> genericUpdater,
      final EntityUpdaterImpl<MedicationPropertyImpl, PropertyDto> propertyUpdater,
      final EntityUpdaterImpl<MedicationUnitImpl, UnitDto> unitUpdater,
      final EntityUpdaterImpl<MedicationUnitTypeImpl, UnitTypeDto> unitTypeUpdater,
      final EntityUpdaterImpl<MedicationIngredientImpl, IngredientDto> ingredientUpdater,
      final EntityUpdaterImpl<MedicationIndicationImpl, IndicationDto> indicationUpdater,
      final EntityUpdaterImpl<AtcClassificationImpl, AtcClassificationDto> atcUpdater,
      final MedicationRoutesValueHolder routesValueHolder)
  {
    this.medicationsValueHolder = medicationsValueHolder;
    this.medicationSharedUpdater = medicationSharedUpdater;
    this.routeSharedUpdater = routeSharedUpdater;
    this.doseFormUpdater = doseFormUpdater;
    this.customGroupUpdater = customGroupUpdater;
    this.genericUpdater = genericUpdater;
    this.propertyUpdater = propertyUpdater;
    this.unitUpdater = unitUpdater;
    this.unitTypeUpdater = unitTypeUpdater;
    this.ingredientUpdater = ingredientUpdater;
    this.indicationUpdater = indicationUpdater;
    this.atcUpdater = atcUpdater;
    this.routesValueHolder = routesValueHolder;
  }

  public void syncMedication(final @NonNull FullMedicationDto medication)
  {
    final long id = medicationSharedUpdater.update(medication).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(medication, "Medication", id);
  }

  public void syncRoute(final @NonNull RouteDto route)
  {
    final long id = routeSharedUpdater.update(route).getId();
    routesValueHolder.incrementVersion();

    logSyncedObject(route, "Route", id);
  }

  public void syncDoseForm(final DoseFormDto doseFormDto)
  {
    final long id = doseFormUpdater.update(doseFormDto).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(doseFormDto, "Dose form", id);
  }

  public void syncCustomGroup(final CustomGroupDto customGroupDto)
  {
    final long id = customGroupUpdater.update(customGroupDto).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(customGroupDto, "Custom group", id);
  }

  public void syncGeneric(final MedicationGenericDto genericDto)
  {
    final long id = genericUpdater.update(genericDto).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(genericDto, "Generic", id);
  }

  public void syncProperty(final PropertyDto propertyDto)
  {
    final long id = propertyUpdater.update(propertyDto).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(propertyDto, "Medication property", id);
  }

  public void syncUnit(final UnitDto unitDto)
  {
    final long id = unitUpdater.update(unitDto).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(unitDto, "Unit", id);
  }

  public void syncUnitType(final UnitTypeDto unitTypeDto)
  {
    final long id = unitTypeUpdater.update(unitTypeDto).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(unitTypeDto, "Unit tyoe", id);
  }

  public void syncIndication(final IndicationDto indicationDto)
  {
    final long id = indicationUpdater.update(indicationDto).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(indicationDto, "Indication", id);
  }

  public void syncIngredient(final IngredientDto ingredientDto)
  {
    final long id = ingredientUpdater.update(ingredientDto).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(ingredientDto, "Ingredient", id);
  }

  public void syncAtc(final AtcClassificationDto atcDto)
  {
    final long id = atcUpdater.update(atcDto).getId();
    medicationsValueHolder.incrementVersion();

    logSyncedObject(atcDto, "atc", id);
  }

  private void logSyncedObject(final ValidatableDto object, final String objectName, final long id)
  {
    LOG.debug(String.format("%s synced - [id] %d", objectName, id));
  }
}
