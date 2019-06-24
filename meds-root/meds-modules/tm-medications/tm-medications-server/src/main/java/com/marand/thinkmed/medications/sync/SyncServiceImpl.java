package com.marand.thinkmed.medications.sync;

import javax.validation.Valid;

import com.marand.thinkmed.medications.sync.updaters.SyncUpdater;
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
import com.marand.thinkmed.meds.config.validation.validators.MedicationCustomValidator;
import com.marand.thinkmed.meds.config.validation.validators.ValidatorUtils;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * @author Nejc Korasa
 */

@Validated
@Component
public class SyncServiceImpl
{
  private final SyncUpdater syncUpdater;
  private final MedicationCustomValidator medicationCustomValidator;

  @Autowired
  public SyncServiceImpl(
      final SyncUpdater syncUpdater,
      final MedicationCustomValidator medicationCustomValidator)
  {
    this.syncUpdater = syncUpdater;
    this.medicationCustomValidator = medicationCustomValidator;
  }

  @Transactional
  public void syncMedication(final @NonNull FullMedicationDto medication)
  {
    if (medication.shouldValidate())
    {
      ValidatorUtils.validateAndThrow(medication);
      medicationCustomValidator.validateAndThrow(medication);
    }

    syncUpdater.syncMedication(medication);
  }

  @Transactional
  public void syncRoute(@Valid final @NonNull RouteDto route)
  {
    syncUpdater.syncRoute(route);
  }

  @Transactional
  public void syncDoseForm(@Valid final @NonNull DoseFormDto doseFormDto)
  {
    syncUpdater.syncDoseForm(doseFormDto);
  }

  @Transactional
  public void syncCustomGroup(@Valid final @NonNull CustomGroupDto customGroupDto)
  {
    syncUpdater.syncCustomGroup(customGroupDto);
  }

  @Transactional
  public void syncGeneric(@Valid final @NonNull MedicationGenericDto genericDto)
  {
    syncUpdater.syncGeneric(genericDto);
  }

  @Transactional
  public void syncProperty(@Valid final @NonNull PropertyDto propertyDto)
  {
    syncUpdater.syncProperty(propertyDto);
  }

  @Transactional
  public void syncUnit(@Valid final @NonNull UnitDto unitDto)
  {
    syncUpdater.syncUnit(unitDto);
  }

  @Transactional
  public void syncUnitType(@Valid final @NonNull UnitTypeDto unitTypeDto)
  {
    syncUpdater.syncUnitType(unitTypeDto);
  }

  @Transactional
  public void syncIndication(@Valid final @NonNull IndicationDto indicationDto)
  {
    syncUpdater.syncIndication(indicationDto);
  }

  @Transactional
  public void syncIngredient(@Valid final @NonNull IngredientDto ingredientDto)
  {
    syncUpdater.syncIngredient(ingredientDto);
  }

  @Transactional
  public void syncAtc(@Valid final @NonNull AtcClassificationDto atcDto)
  {
    syncUpdater.syncAtc(atcDto);
  }
}
