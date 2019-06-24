package com.marand.thinkmed.medications.test;

import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.dto.MedicationActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.dto.RoundsIntervalDto;
import com.marand.thinkmed.medications.ehr.model.Composer;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.IsmTransition;
import com.marand.thinkmed.medications.ehr.model.MedicationManagement;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.ehr.model.composition.Context;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */
public class MedicationsTestUtils
{
  private MedicationsTestUtils()
  {
  }

  public static MedicationManagement buildMedicationAction(
      final MedicationActionEnum medicationAction,
      final DateTime when)
  {
    final MedicationManagement action = new MedicationManagement();
    action.setTime(DataValueUtils.getDateTime(when));

    final IsmTransition ismTransition = new IsmTransition();
    ismTransition.setCareflowStep(medicationAction.getCareflowStep());
    ismTransition.setCurrentState(medicationAction.getCurrentState());
    action.setIsmTransition(ismTransition);

    return action;
  }

  public static RoundsIntervalDto getTestRoundsIntervalDto()
  {
    final RoundsIntervalDto roundsDto = new RoundsIntervalDto();
    roundsDto.setStartHour(7);
    roundsDto.setStartMinute(0);
    roundsDto.setEndHour(17);
    roundsDto.setEndMinute(0);
    return roundsDto;
  }

  public static MedicationDto buildMedicationDto(final Long id, final String name)
  {
    final MedicationDto medication = new MedicationDto();
    medication.setId(id);
    medication.setName(name);
    return medication;
  }

  public static MedicationDto buildMedicationDto(final Long id, final String name, final MedicationTypeEnum medicationType)
  {
    final MedicationDto medication = new MedicationDto();
    medication.setId(id);
    medication.setName(name);
    medication.setMedicationType(medicationType);
    return medication;
  }

  public static MedicationRouteDto buildRoute(final Long id, final String name, final MedicationRouteTypeEnum type)
  {
    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(type);
    route.setId(id);
    route.setName(name);
    return route;
  }

  public static InfusionIngredientDto buildInfusionIngredientDto(
      final MedicationDto medication,
      final Double quantity,
      final String quantityUnit,
      final Double quantityDenominator,
      final String quantityDenominatorUnit)
  {
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    ingredient.setMedication(medication);
    ingredient.setQuantity(quantity);
    ingredient.setQuantityUnit(quantityUnit);
    ingredient.setQuantityDenominator(quantityDenominator);
    ingredient.setQuantityDenominatorUnit(quantityDenominatorUnit);
    return ingredient;
  }

  public static InpatientPrescription buildTestInpatientPrescription(
      final String uid,
      final DateTime composeTimestamp,
      final String composerName,
      final String medicationOrderName)
  {
    final InpatientPrescription composition = new InpatientPrescription();

    composition.setUid(uid);
    if (composeTimestamp != null)
    {
      final Context context = new Context();
      context.setStartTime(DataValueUtils.getDateTime(composeTimestamp));
      composition.setContext(context);
    }
    if (composerName != null)
    {
      final Composer composer = new Composer();
      composer.setName(composerName);
      composition.setComposer(composer);
    }
    if (medicationOrderName != null)
    {
      final MedicationOrder medicationOrder = new MedicationOrder();
      medicationOrder.setName(DataValueUtils.getText(medicationOrderName));
      composition.setMedicationOrder(medicationOrder);
    }
    return composition;
  }

}
