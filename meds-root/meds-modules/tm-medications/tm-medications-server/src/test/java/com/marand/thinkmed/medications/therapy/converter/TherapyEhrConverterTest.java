package com.marand.thinkmed.medications.therapy.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.marand.maf.core.JsonUtil;
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
import com.marand.thinkmed.medications.api.internal.dto.IndicationDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.PrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.ReleaseType;
import com.marand.thinkmed.medications.api.internal.dto.SelfAdministeringActionEnum;
import com.marand.thinkmed.medications.api.internal.dto.SimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.discharge.ControlledDrugSupplyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.EERPrescriptionLocalDetailsDto;
import com.marand.thinkmed.medications.api.internal.dto.eer.IllnessConditionType;
import com.marand.thinkmed.medications.api.internal.dto.eer.OutpatientPrescriptionDocumentType;
import com.marand.thinkmed.medications.api.internal.dto.eer.Payer;
import com.marand.thinkmed.medications.ehr.model.MedicationOrder;
import com.marand.thinkmed.medications.test.MedicationsTestUtils;
import com.marand.thinkmed.medications.therapy.converter.fromehr.ConstantComplexTherapyFromEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.fromehr.ConstantSimpleTherapyFromEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.fromehr.OxygenTherapyFromEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.fromehr.TherapyFromEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.fromehr.TherapyFromEhrUtils;
import com.marand.thinkmed.medications.therapy.converter.fromehr.VariableComplexTherapyFromEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.fromehr.VariableSimpleTherapyFromEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.toehr.ConstantComplexTherapyToEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.toehr.ConstantSimpleTherapyToEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.toehr.OxygenTherapyToEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.toehr.TherapyToEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.toehr.TherapyToEhrUtils;
import com.marand.thinkmed.medications.therapy.converter.toehr.VariableComplexTherapyToEhrConverter;
import com.marand.thinkmed.medications.therapy.converter.toehr.VariableSimpleTherapyToEhrConverter;
import com.marand.thinkmed.medications.therapy.util.TherapyBuilderUtils;
import com.marand.thinkmed.medications.valueholder.MedicationRoutesValueHolder;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.assertj.core.util.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Mitja Lapajne
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyEhrConverterTest
{
  @InjectMocks
  private final TherapyConverter therapyConverter = new TherapyConverter();

  @InjectMocks
  private final TherapyFromEhrUtils therapyFromEhrUtils = new TherapyFromEhrUtils();

  @Mock
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  @Mock
  private MedicationRoutesValueHolder medicationRoutesValueHolder;

  @Before
  public void setup()
  {
    final TherapyConverterSelector therapyConverterSelector = new TherapyConverterSelector();
    therapyConverterSelector.setToEhrConverters(getTherapyToEhrConverters());
    therapyConverterSelector.setFromEhrConverters(getTherapyFromEhrConverters());
    therapyConverter.setTherapyConverterSelector(therapyConverterSelector);

    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(10L);
    medication1.setName("Aspirin");
    Mockito.when(medicationsValueHolderProvider.getMedication(10L)).thenReturn(medication1);

    final MedicationDto medication2 = new MedicationDto();
    medication2.setId(200L);
    medication2.setName("Dopamin");
    Mockito.when(medicationsValueHolderProvider.getMedication(200L)).thenReturn(medication2);

    final MedicationDto medication3 = new MedicationDto();
    medication3.setId(300L);
    medication3.setName("Glucose");
    Mockito.when(medicationsValueHolderProvider.getMedication(300L)).thenReturn(medication3);

    final MedicationDto medication4 = new MedicationDto();
    medication4.setId(500L);
    medication4.setName("Oxygen");
    Mockito.when(medicationsValueHolderProvider.getMedication(500L)).thenReturn(medication4);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(20L);
    route.setName("Oral");
    route.setType(MedicationRouteTypeEnum.ORAL);
    final Map<Long, MedicationRouteDto> routesMap = new HashMap<>();
    routesMap.put(20L, route);
    Mockito.when(medicationRoutesValueHolder.getValue()).thenReturn(routesMap);
  }

  @SuppressWarnings("TooBroadScope")
  private List<TherapyToEhrConverter<?>> getTherapyToEhrConverters()
  {
    final TherapyToEhrUtils therapyToEhrUtils = new TherapyToEhrUtils();

    final List<TherapyToEhrConverter<?>> toEhrConverters = new ArrayList<>();

    final ConstantSimpleTherapyToEhrConverter constantSimpleTherapyToEhrConverter = new ConstantSimpleTherapyToEhrConverter();
    constantSimpleTherapyToEhrConverter.setTherapyToEhrUtils(therapyToEhrUtils);
    toEhrConverters.add(constantSimpleTherapyToEhrConverter);

    final VariableSimpleTherapyToEhrConverter variableSimpleTherapyToEhrConverter = new VariableSimpleTherapyToEhrConverter();
    variableSimpleTherapyToEhrConverter.setTherapyToEhrUtils(therapyToEhrUtils);
    toEhrConverters.add(variableSimpleTherapyToEhrConverter);

    final ConstantComplexTherapyToEhrConverter constantComplexTherapyToEhrConverter = new ConstantComplexTherapyToEhrConverter();
    constantComplexTherapyToEhrConverter.setTherapyToEhrUtils(therapyToEhrUtils);
    toEhrConverters.add(constantComplexTherapyToEhrConverter);

    final VariableComplexTherapyToEhrConverter variableComplexTherapyToEhrConverter = new VariableComplexTherapyToEhrConverter();
    variableComplexTherapyToEhrConverter.setTherapyToEhrUtils(therapyToEhrUtils);
    toEhrConverters.add(variableComplexTherapyToEhrConverter);

    final OxygenTherapyToEhrConverter oxygenTherapyToEhrConverter = new OxygenTherapyToEhrConverter();
    oxygenTherapyToEhrConverter.setTherapyToEhrUtils(therapyToEhrUtils);
    toEhrConverters.add(oxygenTherapyToEhrConverter);
    return toEhrConverters;
  }

  @SuppressWarnings("TooBroadScope")
  private List<TherapyFromEhrConverter<?>> getTherapyFromEhrConverters()
  {
    final List<TherapyFromEhrConverter<?>> fromEhrConverters = new ArrayList<>();

    final ConstantSimpleTherapyFromEhrConverter constantSimpleTherapyFromEhrConverter = new ConstantSimpleTherapyFromEhrConverter();
    constantSimpleTherapyFromEhrConverter.setTherapyFromEhrUtils(therapyFromEhrUtils);
    constantSimpleTherapyFromEhrConverter.setMedicationRoutesValueHolder(medicationRoutesValueHolder);
    fromEhrConverters.add(constantSimpleTherapyFromEhrConverter);

    final VariableSimpleTherapyFromEhrConverter variableSimpleTherapyFromEhrConverter = new VariableSimpleTherapyFromEhrConverter();
    variableSimpleTherapyFromEhrConverter.setTherapyFromEhrUtils(therapyFromEhrUtils);
    variableSimpleTherapyFromEhrConverter.setMedicationRoutesValueHolder(medicationRoutesValueHolder);
    fromEhrConverters.add(variableSimpleTherapyFromEhrConverter);

    final ConstantComplexTherapyFromEhrConverter constantComplexTherapyFromEhrConverter = new ConstantComplexTherapyFromEhrConverter();
    constantComplexTherapyFromEhrConverter.setTherapyFromEhrUtils(therapyFromEhrUtils);
    constantComplexTherapyFromEhrConverter.setMedicationRoutesValueHolder(medicationRoutesValueHolder);
    fromEhrConverters.add(constantComplexTherapyFromEhrConverter);

    final VariableComplexTherapyFromEhrConverter variableComplexTherapyFromEhrConverter = new VariableComplexTherapyFromEhrConverter();
    variableComplexTherapyFromEhrConverter.setTherapyFromEhrUtils(therapyFromEhrUtils);
    variableComplexTherapyFromEhrConverter.setMedicationRoutesValueHolder(medicationRoutesValueHolder);
    fromEhrConverters.add(variableComplexTherapyFromEhrConverter);

    final OxygenTherapyFromEhrConverter oxygenTherapyFromEhrConverter = new OxygenTherapyFromEhrConverter();
    oxygenTherapyFromEhrConverter.setTherapyFromEhrUtils(therapyFromEhrUtils);
    oxygenTherapyFromEhrConverter.setMedicationRoutesValueHolder(medicationRoutesValueHolder);
    fromEhrConverters.add(oxygenTherapyFromEhrConverter);
    return fromEhrConverters;
  }

  @Test
  public void testMinimalConstantSimpleTherapy()
  {
    final ConstantSimpleTherapyDto therapy = TherapyBuilderUtils.createMinimalSimpleTherapyDto("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullConstantSimpleTherapy()
  {
    final ConstantSimpleTherapyDto therapy = TherapyBuilderUtils.createFullConstantSimpleTherapy("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullConstantSimpleTherapyDoseRange()
  {
    final ConstantSimpleTherapyDto therapy = TherapyBuilderUtils.createFullConstantSimpleTherapyDoseRange("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testMinimalVariableSimpleTherapy()
  {
    final VariableSimpleTherapyDto therapy = TherapyBuilderUtils.createMinimalVariableSimpleTherapy("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullVariableSimpleTherapy()
  {
    final VariableSimpleTherapyDto therapy = TherapyBuilderUtils.createFullVariableSimpleTherapy("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullVariableSimpleTherapyDischargeProtocol()
  {
    final VariableSimpleTherapyDto therapy = TherapyBuilderUtils.createFullVariableSimpleTherapyDischargeProtocol("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testMinimalConstantComplexTherapy()
  {
    final ConstantComplexTherapyDto therapy = TherapyBuilderUtils.createMinimalConstantComplexTherapy("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testMinimalConstantComplexTherapyWithRate()
  {
    final ConstantComplexTherapyDto therapy = TherapyBuilderUtils.createMinimalConstantComplexTherapyWithRate("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testMinimalConstantComplexTherapyContinuousInfusion()
  {
    final ConstantComplexTherapyDto therapy = TherapyBuilderUtils.createMinimalConstantComplexTherapyContinuousInfusion("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullConstantComplexTherapy()
  {
    final ConstantComplexTherapyDto therapy = TherapyBuilderUtils.createFullConstantComplexTherapy("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullConstantComplexTherapyUniversalForm()
  {
    final ConstantComplexTherapyDto therapy = TherapyBuilderUtils.createFullConstantComplexTherapyUniversalForm("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullConstantComplexTherapyWithRate()
  {
    final ConstantComplexTherapyDto therapy = TherapyBuilderUtils.createFullConstantComplexTherapyWithRate("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullConstantComplexTherapyContinuousInfusion()
  {
    final ConstantComplexTherapyDto therapy = TherapyBuilderUtils.createFullConstantComplexTherapyContinuousInfusion("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullVariableComplexTherapy()
  {
    final VariableComplexTherapyDto therapy = TherapyBuilderUtils.createFullVariableComplexTherapy("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testFullVariableComplexTherapyContinuousInfusion()
  {
    final VariableComplexTherapyDto therapy = TherapyBuilderUtils.createFullVariableComplexTherapyContinuousInfusion("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  @Test
  public void testOxygenTherapy()
  {
    final OxygenTherapyDto therapy = TherapyBuilderUtils.createOxygenTherapy("uid");

    final MedicationOrder medicationOrder = therapyConverter.convertToMedicationOrder(therapy);
    final TherapyDto convertedTherapy = therapyConverter.convertToTherapyDto(
        medicationOrder,
        "uid",
        new DateTime(2018, 10, 6, 9, 0, 0));
    assertEquals(JsonUtil.toJson(therapy), JsonUtil.toJson(convertedTherapy));
  }

  private void fillTherapyDtoMinimal(final TherapyDto therapy)
  {
    therapy.setCompositionUid("uid");
    therapy.setEhrOrderName("Medication order");
    therapy.setCreatedTimestamp(new DateTime(2018, 10, 6, 9, 0, 0));
    therapy.setStart(new DateTime(2018, 10, 6, 12, 0, 0));
    therapy.setTherapyDescription("Aspirin - 500mg Oral - 2x");
    therapy.setFormattedTherapyDisplay("<html>Aspirin - 500mg Oral - 2x</html>");
  }

  private void fillTherapyDtoFull(final TherapyDto therapy)
  {
    therapy.setCompositionUid("uid");
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

  private void fillSimpleTherapyDtoMinimal(final SimpleTherapyDto therapy)
  {
    therapy.setMedication(MedicationsTestUtils.buildMedicationDto(10L, "Aspirin"));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
  }

  private void fillSimpleTherapyDtoFull(final SimpleTherapyDto therapy)
  {
    therapy.setMedication(MedicationsTestUtils.buildMedicationDto(10L, "Aspirin"));
    therapy.setQuantityUnit("mg");
    therapy.setQuantityDenominatorUnit("ml");
    therapy.setTargetInr(5.0);
    therapy.setDoseForm(buildDoseForm(30L, "Tablet"));
  }

  private void fillComplexTherapyDtoMinimal(final ComplexTherapyDto therapy)
  {
    final InfusionIngredientDto ingredient = MedicationsTestUtils.buildInfusionIngredientDto(
        MedicationsTestUtils.buildMedicationDto(200L, "Dopamin"),
        100.0,
        "mg",
        10.0,
        "ml");
    therapy.getIngredientsList().add(ingredient);
  }

  private void fillComplexTherapyDtoFull(final ComplexTherapyDto therapy)
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

  private TimedSimpleDoseElementDto buildTimedSimpleDoseElement(
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

  private SimpleDoseElementDto buildSimpleDoseElement(final Double quantity, final Double quantityDenominator)
  {
    final SimpleDoseElementDto dose = new SimpleDoseElementDto();
    dose.setQuantity(quantity);
    dose.setQuantityDenominator(quantityDenominator);
    return dose;
  }

  private List<HourMinuteDto> buildDoseTimes3x()
  {
    final List<HourMinuteDto> doseTimes = new ArrayList<>();
    doseTimes.add(new HourMinuteDto(8, 0));
    doseTimes.add(new HourMinuteDto(14, 0));
    doseTimes.add(new HourMinuteDto(20, 0));
    return doseTimes;
  }

  private DosingFrequencyDto buildDosingFrequency3x()
  {
    return new DosingFrequencyDto(DosingFrequencyTypeEnum.DAILY_COUNT, 3.0);
  }

  private DispenseDetailsDto buildControlledDrugDispenseDetails()
  {
    final DispenseDetailsDto dispenseDetails = new DispenseDetailsDto();
    dispenseDetails.setDaysDuration(3);
    dispenseDetails.setQuantity(10);
    dispenseDetails.setUnit("packages");
    dispenseDetails.setDispenseSource(new NamedIdDto(15L, "Pharmacy"));
    dispenseDetails.setControlledDrugSupply(Lists.newArrayList(buildControlledDrugSupply()));
    return dispenseDetails;
  }

  private ControlledDrugSupplyDto buildControlledDrugSupply()
  {
    final ControlledDrugSupplyDto supply = new ControlledDrugSupplyDto();
    supply.setMedication(new NamedIdDto(90L, "Medication1"));
    supply.setQuantity(10);
    supply.setUnit("tablet");
    return supply;
  }

  private PrescriptionLocalDetailsDto buildPrescriptionLocalDetails()
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

  private DoseFormDto buildDoseForm(final Long id, final String name)
  {
    final DoseFormDto doseForm = new DoseFormDto();
    doseForm.setId(id);
    doseForm.setName(name);
    return doseForm;
  }
}