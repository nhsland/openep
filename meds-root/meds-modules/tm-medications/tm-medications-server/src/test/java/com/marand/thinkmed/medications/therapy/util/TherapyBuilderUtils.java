package com.marand.thinkmed.medications.therapy.util;

import java.util.ArrayList;
import java.util.List;

import com.marand.maf.core.data.object.HourMinuteDto;
import com.marand.maf.core.data.object.NamedIdDto;
import com.marand.thinkmed.medications.HeparinEnum;
import com.marand.thinkmed.medications.api.internal.dto.ComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DispenseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.DoseFormDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.FlowRateMode;
import com.marand.thinkmed.medications.api.internal.dto.IndicationDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicalDeviceEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.OxygenStartingDevice;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.PrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TitrationType;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.discharge.ControlledDrugSupplyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.DoseRangeDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.IllnessConditionType;
import com.marand.thinkmed.medications.api.internal.dto.eer.OutpatientPrescriptionDocumentType;
import com.marand.thinkmed.medications.api.internal.dto.eer.Payer;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import org.assertj.core.util.Lists;
import org.joda.time.DateTime;

/**
 * @author Mitja Lapajne
 */

public class TherapyBuilderUtils
{
  private TherapyBuilderUtils()
  {
  }

  public static ConstantSimpleTherapyDto createMinimalSimpleTherapyDto(final String uid)
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    fillTherapyDtoMinimal(therapy, uid);
    fillSimpleTherapyDtoMinimal(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.QUANTITY);
    therapy.setDoseElement(buildSimpleDoseElement(500.0, 12.0));

    return therapy;
  }

  public static ConstantSimpleTherapyDto createFullConstantSimpleTherapy(final String uid)
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillSimpleTherapyDtoFull(therapy);
    therapy.setDoseElement(buildSimpleDoseElement(500.0, 12.0));
    therapy.setDoseTimes(buildDoseTimes3x());
    therapy.setTitration(TitrationType.INR);
    therapy.setDoseType(TherapyDoseTypeEnum.QUANTITY);

    return therapy;
  }

  public static ConstantSimpleTherapyDto createFullConstantSimpleTherapyDoseRange(final String uid)
  {
    final ConstantSimpleTherapyDto therapy = new ConstantSimpleTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillSimpleTherapyDtoFull(therapy);
    final SimpleDoseElementDto doseElement = new SimpleDoseElementDto();
    final DoseRangeDto doseRange = new DoseRangeDto();
    doseRange.setMinNumerator(1.0);
    doseRange.setMaxNumerator(2.0);
    doseRange.setMinDenominator(10.0);
    doseRange.setMaxDenominator(20.0);
    doseElement.setDoseRange(doseRange);
    therapy.setDoseElement(doseElement);
    therapy.setDoseTimes(buildDoseTimes3x());
    therapy.setTitration(TitrationType.INR);
    therapy.setDoseType(TherapyDoseTypeEnum.QUANTITY);

    return therapy;
  }

  public static VariableSimpleTherapyDto createMinimalVariableSimpleTherapy(final String uid)
  {
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    fillTherapyDtoMinimal(therapy, uid);
    fillSimpleTherapyDtoMinimal(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.QUANTITY);

    therapy.setTimedDoseElements(Lists.newArrayList(
        buildTimedSimpleDoseElement(1.0, 10.0, 8, 0, new DateTime(2018, 10, 6, 0, 0, 0), null),
        buildTimedSimpleDoseElement(2.0, 20.0, 20, 0, new DateTime(2018, 10, 6, 0, 0, 0), null),
        buildTimedSimpleDoseElement(3.0, 30.0, 8, 0, new DateTime(2018, 10, 7, 0, 0, 0), null),
        buildTimedSimpleDoseElement(4.0, 40.0, 20, 0, new DateTime(2018, 10, 7, 0, 0, 0), null))
    );

    return therapy;
  }

  public static VariableSimpleTherapyDto createFullVariableSimpleTherapy(final String uid)
  {
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillSimpleTherapyDtoFull(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.QUANTITY);
    therapy.setTimedDoseElements(Lists.newArrayList(
        buildTimedSimpleDoseElement(1.0, 10.0, 8, 0, new DateTime(2018, 10, 6, 0, 0, 0), null),
        buildTimedSimpleDoseElement(2.0, 20.0, 20, 0, new DateTime(2018, 10, 6, 0, 0, 0), null),
        buildTimedSimpleDoseElement(3.0, 30.0, 8, 0, new DateTime(2018, 10, 7, 0, 0, 0), null),
        buildTimedSimpleDoseElement(4.0, 40.0, 20, 0, new DateTime(2018, 10, 7, 0, 0, 0), null))
    );

    return therapy;
  }

  public static VariableSimpleTherapyDto createFullVariableSimpleTherapyDischargeProtocol(final String uid)
  {
    final VariableSimpleTherapyDto therapy = new VariableSimpleTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillSimpleTherapyDtoFull(therapy);
    therapy.setQuantityDenominatorUnit(null);
    therapy.setDoseType(TherapyDoseTypeEnum.QUANTITY);
    therapy.setTimedDoseElements(Lists.newArrayList(
        buildTimedSimpleDoseElement(1.0, null, null, null, null, "first week"),
        buildTimedSimpleDoseElement(2.0, null, null, null, null, "second week"),
        buildTimedSimpleDoseElement(3.0, null, null, null, null, "third week"),
        buildTimedSimpleDoseElement(4.0, null, null, null, null, "forth week"))
    );

    return therapy;
  }

  public static ConstantComplexTherapyDto createMinimalConstantComplexTherapy(final String uid)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillTherapyDtoMinimal(therapy, uid);
    fillComplexTherapyDtoMinimal(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.QUANTITY);

    return therapy;
  }

  public static ConstantComplexTherapyDto createMinimalConstantComplexTherapyWithRate(final String uid)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillTherapyDtoMinimal(therapy, uid);
    fillComplexTherapyDtoMinimal(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.RATE_QUANTITY);
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setRate(20.0);
    doseElement.setRateUnit("ml/h");
    therapy.setDoseElement(doseElement);

    return therapy;
  }

  public static ConstantComplexTherapyDto createMinimalConstantComplexTherapyContinuousInfusion(final String uid)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillTherapyDtoMinimal(therapy, uid);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    ingredient.setMedication(MedicationsTestUtils.buildMedicationDto(200L, "Dopamin"));
    therapy.getIngredientsList().add(ingredient);
    therapy.setDoseType(TherapyDoseTypeEnum.RATE);
    therapy.setContinuousInfusion(true);
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setRate(20.0);
    doseElement.setRateUnit("ml/h");
    therapy.setDoseElement(doseElement);

    return therapy;
  }

  public static ConstantComplexTherapyDto createFullConstantComplexTherapy(final String uid)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillComplexTherapyDtoFull(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.VOLUME_SUM);

    return therapy;
  }

  public static ConstantComplexTherapyDto createFullConstantComplexTherapyUniversalForm(final String uid)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillComplexTherapyDtoFullUniversal(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.VOLUME_SUM);

    return therapy;
  }

  public static ConstantComplexTherapyDto createFullConstantComplexTherapyWithRate(final String uid)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillComplexTherapyDtoFull(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.RATE_VOLUME_SUM);
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setRate(20.0);
    doseElement.setRateUnit("ml/h");
    doseElement.setRateFormula(5.0);
    doseElement.setRateFormulaUnit("unit with space/kg/h");
    doseElement.setDuration(120);
    therapy.setDoseElement(doseElement);

    return therapy;
  }

  public static ConstantComplexTherapyDto createFullConstantComplexTherapyContinuousInfusion(final String uid)
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillComplexTherapyDtoFull(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.RATE);
    therapy.setContinuousInfusion(true);
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    doseElement.setRate(20.0);
    doseElement.setRateUnit("ml/h");
    doseElement.setRateFormula(5.0);
    doseElement.setRateFormulaUnit("mg/kg/h");
    doseElement.setDuration(120);
    therapy.setDoseElement(doseElement);

    return therapy;
  }

  public static VariableComplexTherapyDto createFullVariableComplexTherapy(final String uid)
  {
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillComplexTherapyDtoFull(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.RATE_VOLUME_SUM);

    final TimedComplexDoseElementDto timedDoseElement1 = new TimedComplexDoseElementDto();
    final ComplexDoseElementDto doseElement1 = new ComplexDoseElementDto();
    doseElement1.setRate(20.0);
    doseElement1.setRateUnit("ml/h");
    doseElement1.setRateFormula(5.0);
    doseElement1.setRateFormulaUnit("mg/kg/h");
    doseElement1.setDuration(120);
    timedDoseElement1.setDoseElement(doseElement1);
    timedDoseElement1.setDoseTime(new HourMinuteDto(8, 0));
    therapy.getTimedDoseElements().add(timedDoseElement1);

    final TimedComplexDoseElementDto timedDoseElement2 = new TimedComplexDoseElementDto();
    final ComplexDoseElementDto doseElement2 = new ComplexDoseElementDto();
    doseElement2.setRate(30.0);
    doseElement2.setRateUnit("ml/h");
    doseElement2.setRateFormula(7.0);
    doseElement2.setRateFormulaUnit("mg/kg/h");
    doseElement2.setDuration(60);
    timedDoseElement2.setDoseElement(doseElement2);
    timedDoseElement2.setDoseTime(new HourMinuteDto(18, 0));
    therapy.getTimedDoseElements().add(timedDoseElement2);

    return therapy;
  }

  public static VariableComplexTherapyDto createFullVariableComplexTherapyContinuousInfusion(final String uid)
  {
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    fillTherapyDtoFull(therapy, uid);
    fillComplexTherapyDtoFull(therapy);
    therapy.setDoseType(TherapyDoseTypeEnum.RATE);
    therapy.setContinuousInfusion(true);

    final TimedComplexDoseElementDto timedDoseElement1 = new TimedComplexDoseElementDto();
    final ComplexDoseElementDto doseElement1 = new ComplexDoseElementDto();
    doseElement1.setRate(20.0);
    doseElement1.setRateUnit("ml/h");
    doseElement1.setRateFormula(5.0);
    doseElement1.setRateFormulaUnit("mg/kg/h");
    doseElement1.setDuration(120);
    timedDoseElement1.setDoseElement(doseElement1);
    timedDoseElement1.setDoseTime(new HourMinuteDto(8, 0));
    therapy.getTimedDoseElements().add(timedDoseElement1);

    final TimedComplexDoseElementDto timedDoseElement2 = new TimedComplexDoseElementDto();
    final ComplexDoseElementDto doseElement2 = new ComplexDoseElementDto();
    doseElement2.setRate(30.0);
    doseElement2.setRateUnit("ml/h");
    doseElement2.setRateFormula(7.0);
    doseElement2.setRateFormulaUnit("mg/kg/h");
    doseElement2.setDuration(60);
    timedDoseElement2.setDoseElement(doseElement2);
    timedDoseElement2.setDoseTime(new HourMinuteDto(18, 0));
    therapy.getTimedDoseElements().add(timedDoseElement2);

    return therapy;
  }

  public static OxygenTherapyDto createOxygenTherapy(final String uid)
  {
    final OxygenTherapyDto therapy = new OxygenTherapyDto();
    fillTherapyDtoMinimal(therapy, uid);
    therapy.setMedication(MedicationsTestUtils.buildMedicationDto(500L, "Oxygen"));
    therapy.setMinTargetSaturation(90.0);
    therapy.setMaxTargetSaturation(95.0);
    therapy.setDoseType(TherapyDoseTypeEnum.RATE);

    final OxygenStartingDevice startingDevice = new OxygenStartingDevice(MedicalDeviceEnum.VENTURI_MASK);
    startingDevice.setRouteType("20");
    therapy.setStartingDevice(startingDevice);
    therapy.setHumidification(true);
    therapy.setFlowRate(20.0);
    therapy.setFlowRateUnit("l/min");
    therapy.setFlowRateMode(FlowRateMode.HIGH_FLOW);

    return therapy;
  }

  private static void fillTherapyDtoMinimal(final TherapyDto therapy, final String uid)
  {
    therapy.setCompositionUid(uid);
    therapy.setEhrOrderName("Medication order");
    therapy.setCreatedTimestamp(new DateTime(2018, 10, 6, 9, 0, 0));
    therapy.setStart(new DateTime(2018, 10, 6, 12, 0, 0));
    therapy.setTherapyDescription("Aspirin - 500mg Oral - 2x");
    therapy.setFormattedTherapyDisplay("<html>Aspirin - 500mg Oral - 2x</html>");
  }

  private static void fillTherapyDtoFull(final TherapyDto therapy, final String uid)
  {
    therapy.setCompositionUid(uid);
    therapy.setEhrOrderName("Medication order");
    therapy.setCreatedTimestamp(new DateTime(2018, 10, 6, 9, 0, 0));
    therapy.setStart(new DateTime(2018, 10, 6, 12, 0, 0));
    therapy.setEnd(new DateTime(2018, 10, 20, 12, 0, 0));
    therapy.setTherapyDescription("Aspirin - 500mg Oral - 2x");
    therapy.setFormattedTherapyDisplay("<html>Aspirin - 500mg Oral - 2x</html>");
    therapy.setDosingFrequency(buildDosingFrequency3x());
    therapy.setReleaseDetails(new ReleaseDetailsDto(ReleaseType.MODIFIED_RELEASE, 12));
    therapy.setWhenNeeded(true);
    therapy.setComment("Comment");
    therapy.setClinicalIndication(new IndicationDto("10", "Indication"));
    therapy.setDaysOfWeek(Lists.newArrayList("MONDAY", "FRIDAY"));
    therapy.setDosingDaysFrequency(2);
    therapy.setStartCriterion("BY_DOCTOR_ORDERS");
    therapy.setApplicationPrecondition("BEFORE_MEAL");
    therapy.setMaxDosePercentage(90);
    therapy.setSelfAdministeringActionEnum(SelfAdministeringActionEnum.AUTOMATICALLY_CHARTED);
    therapy.setSelfAdministeringLastChange(new DateTime(2018, 10, 6, 5, 0, 0));
    therapy.setCriticalWarnings(Lists.newArrayList("warning1", "warning2"));
    therapy.setInformationSources(Lists.newArrayList(new NamedIdDto(4, "Source")));
    therapy.setRoutes(Lists.newArrayList(MedicationsTestUtils.buildRoute(20L, "Oral", MedicationRouteTypeEnum.ORAL)));
    therapy.setPastTherapyStart(therapy.getStart().minusDays(10));
    therapy.setMaxDailyFrequency(4);
    therapy.setDispenseDetails(buildControlledDrugDispenseDetails());
    therapy.setPrescriptionLocalDetails(buildPrescriptionLocalDetails());
  }

  private static void fillSimpleTherapyDtoMinimal(final SimpleTherapyDto therapy)
  {
    therapy.setMedication(MedicationsTestUtils.buildMedicationDto(10L, "Aspirin"));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
  }

  private static void fillSimpleTherapyDtoFull(final SimpleTherapyDto therapy)
  {
    therapy.setMedication(MedicationsTestUtils.buildMedicationDto(10L, "Aspirin"));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setTargetInr(5.0);
    therapy.setDoseForm(buildDoseForm(30L, "Tablet"));
  }

  private static void fillComplexTherapyDtoMinimal(final ComplexTherapyDto therapy)
  {
    final InfusionIngredientDto ingredient = MedicationsTestUtils.buildInfusionIngredientDto(
        MedicationsTestUtils.buildMedicationDto(200L, "Dopamin"),
        100.0,
        "mg",
        10.0,
        "ml");
    therapy.getIngredientsList().add(ingredient);
  }

  private static void fillComplexTherapyDtoFull(final ComplexTherapyDto therapy)
  {
    therapy.getIngredientsList().add(
        MedicationsTestUtils.buildInfusionIngredientDto(
            MedicationsTestUtils.buildMedicationDto(200L, "Dopamin"),
            100.0,
            "mg",
            10.0,
            "ml"));

    therapy.getIngredientsList().add(
        MedicationsTestUtils.buildInfusionIngredientDto(
            MedicationsTestUtils.buildMedicationDto(300L, "Glucose"),
            1.0,
            "mg",
            100.0,
            "ml"));

    therapy.setVolumeSum(110.0);
    therapy.setVolumeSumUnit("ml");
    therapy.setAdjustToFluidBalance(true);
    therapy.setBaselineInfusion(true);
    therapy.setAdditionalInstruction(HeparinEnum.HEPARIN_05.name());
  }

  /**
   * Fills therapy with medications without ID's. One in MEDICATION, another is DILUENT.
   *
   * @param therapy therapy to fill
   */
  private static void fillComplexTherapyDtoFullUniversal(final ComplexTherapyDto therapy)
  {
    therapy.getIngredientsList().add(
        MedicationsTestUtils.buildInfusionIngredientDto(
            MedicationsTestUtils.buildMedicationDto(null, "Dopamin"),
            100.0,
            "mg",
            10.0,
            "ml"));

    therapy.getIngredientsList().add(
        MedicationsTestUtils.buildInfusionIngredientDto(
            MedicationsTestUtils.buildMedicationDto(null, "Glucose"),
            1.0,
            "mg",
            100.0,
            "ml"));

    therapy.getIngredientsList().get(0).getMedication().setMedicationType(MedicationTypeEnum.MEDICATION);
    therapy.getIngredientsList().get(1).getMedication().setMedicationType(MedicationTypeEnum.DILUENT);

    therapy.setVolumeSum(110.0);
    therapy.setVolumeSumUnit("ml");
    therapy.setAdjustToFluidBalance(true);
    therapy.setBaselineInfusion(true);
    therapy.setAdditionalInstruction(HeparinEnum.HEPARIN_05.name());
  }

  private static TimedSimpleDoseElementDto buildTimedSimpleDoseElement(
      final Double quantity,
      final Double quantityDenominator,
      final Integer hour,
      final Integer minute,
      final DateTime date,
      final String timingDescription)
  {
    final TimedSimpleDoseElementDto dose = new TimedSimpleDoseElementDto();
    dose.setDoseElement(buildSimpleDoseElement(quantity, quantityDenominator));
    if (hour != null)
    {
      dose.setDoseTime(new HourMinuteDto(hour, minute));
    }
    dose.setDate(date);
    dose.setTimingDescription(timingDescription);
    return dose;
  }

  private static SimpleDoseElementDto buildSimpleDoseElement(final Double quantity, final Double quantityDenominator)
  {
    final SimpleDoseElementDto dose = new SimpleDoseElementDto();
    dose.setQuantity(quantity);
    dose.setQuantityDenominator(quantityDenominator);
    return dose;
  }

  private static List<HourMinuteDto> buildDoseTimes3x()
  {
    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(8, 0));
    doseTimes.add(new HourMinuteDto(14, 0));
    doseTimes.add(new HourMinuteDto(20, 0));
    return doseTimes;
  }

  private static DosingFrequencyDto buildDosingFrequency3x()
  {
    return new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3.0);
  }

  private static DispenseDetailsDto buildControlledDrugDispenseDetails()
  {
    final DispenseDetailsDto dispenseDetails = new DispenseDetailsDto();
    dispenseDetails.setDaysDuration(3);
    dispenseDetails.setQuantity(10);
    dispenseDetails.setUnit("packages");
    dispenseDetails.setDispenseSource(new NamedIdDto(15L, "Pharmacy"));
    dispenseDetails.setControlledDrugSupply(Lists.newArrayList(buildControlledDrugSupply()));
    return dispenseDetails;
  }

  private static ControlledDrugSupplyDto buildControlledDrugSupply()
  {
    final ControlledDrugSupplyDto supply = new ControlledDrugSupplyDto();
    supply.setMedication(new NamedIdDto(90L, "Medication1"));
    supply.setQuantity(10);
    supply.setUnit("tablet");
    return supply;
  }

  private static PrescriptionLocalDetailsDto buildPrescriptionLocalDetails()
  {
    final EERPrescriptionLocalDetailsDto localDetails = new EERPrescriptionLocalDetailsDto();
    localDetails.setPrescriptionSystem("EER");
    localDetails.setRemainingDispenses(2);
    localDetails.setMaxDoseExceeded(true);
    localDetails.setIllnessConditionType(IllnessConditionType.CHRONIC_CONDITION);
    localDetails.setMagistralPreparation(true);
    localDetails.setUrgent(true);
    localDetails.setPrescriptionRepetition(5);
    localDetails.setPayer(Payer.PERSON);
    localDetails.setPrescriptionDocumentType(OutpatientPrescriptionDocumentType.GREEN);
    localDetails.setDoNotSwitch(true);
    localDetails.setInstructionsToPharmacist("Instructions");
    return localDetails;
  }

  private static DoseFormDto buildDoseForm(final Long id, final String name)
  {
    final DoseFormDto doseForm = new DoseFormDto();
    doseForm.setId(id);
    doseForm.setName(name);
    return doseForm;
  }
}
