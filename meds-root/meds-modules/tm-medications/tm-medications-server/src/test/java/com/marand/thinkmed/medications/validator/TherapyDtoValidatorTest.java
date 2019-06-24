package com.marand.thinkmed.medications.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.ConstantSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyDto;
import com.marand.thinkmed.medications.api.internal.dto.DosingFrequencyTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.IndicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.OxygenTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDoseTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.VariableSimpleTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.SimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedSimpleDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.property.MedicationPropertyType;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.property.MedicationPropertyDto;
import com.marand.thinkmed.medications.therapy.util.TherapyBuilderUtils;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class TherapyDtoValidatorTest
{

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(TherapyDtoValidatorTest.class);

  @InjectMocks
  private final TherapyDtoValidator therapyDtoValidator = new TherapyDtoValidator();

  @Mock
  private MedicationsValueHolderProvider medicationsValueHolderProvider;

  public void resetMocks()
  {
    Mockito.reset(medicationsValueHolderProvider);
  }

  @Test
  /*
    Input is empty ConstantSimpleTherapyDto.
    Method isValid must return false.
    Response of method getErrorsDescription must contains 'Medication is missing' string.
   */
  public void test_01()
  {
    final ConstantSimpleTherapyDto therapyDto = new ConstantSimpleTherapyDto();
    final boolean valid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(valid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertThat(errorsDescription).contains(TherapyDtoValidatorEnum.MEDICATION_IS_MISSING.getText());
  }

  @Test
   /*
    Input is full ConstantSimpleTherapyDto.
    Property titration is set to null.
    Method isValid must return true.
    Response of method getErrorsDescription must be empty.
   */
  public void test_02()
  {
    final ConstantSimpleTherapyDto therapyDtoFull = TherapyBuilderUtils.createFullConstantSimpleTherapy("therapy full");
    therapyDtoFull.setTitration(null);
    final boolean valid = therapyDtoValidator.isValid(therapyDtoFull);
    assertTrue(valid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDtoFull);
    assertTrue(errorsDescription.isEmpty());
  }

  @Test
   /*
    Input is full ConstantSimpleTherapyDto.
    Property titration is INR.
    Method isValid must return false.
    Response of method getErrorsDescription must contains 'Dose element and titration exists' string.
   */
  public void test_03()
  {
    final ConstantSimpleTherapyDto therapyDtoFull = TherapyBuilderUtils.createFullConstantSimpleTherapy("therapy full");
    final boolean valid = therapyDtoValidator.isValid(therapyDtoFull);
    assertFalse(valid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDtoFull);
    assertTrue(errorsDescription.contains(TherapyDtoValidatorEnum.DOSE_ELEMENT_AND_TITRATION_EXISTS.getText()));
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription (Aspirin) with dose, route and dosing interval.
    Methods isValid must return true.
   */
  public void test_04()
  {
    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createMinimalSimpleTherapyDto("simple prescription");

    final SimpleDoseElementDto simpleDoseElement = new SimpleDoseElementDto();
    simpleDoseElement.setQuantity(500.0d);
    therapyDto.setDoseElement(simpleDoseElement);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final DosingFrequencyDto dosingFrequencyDto = new DosingFrequencyDto(DosingFrequencyTypeEnum.MORNING);
    therapyDto.setDosingFrequency(dosingFrequencyDto);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription (Aspirin) !without! dose, route and dosing interval.
    Methods isValid must return false.
    Response of method getErrorsDescription must contain
      - 'Dose element is missing'
      - 'Route is missing'
      - 'Dosing interval is missing' strings.
   */
  public void test_05()
  {
    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createMinimalSimpleTherapyDto("simple prescription");
    therapyDto.getDoseElement().setQuantity(null);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(3, errorsDescription.size());
    assertTrue(errorsDescription.containsAll(Arrays.asList(
        TherapyDtoValidatorEnum.DOSE_ELEMENT_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.ROUTE_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.DOSING_INTERVAL_IS_MISSING.getText())));
  }

  @Test
  /*
    Input is VariableSimpleTherapyDto prescription with dose/time pairs and route.
    Methods isValid must return true.
   */
  public void test_06()
  {
    final VariableSimpleTherapyDto therapyDto = TherapyBuilderUtils.createMinimalVariableSimpleTherapy(
        "simple variable prescription");

    final TimedSimpleDoseElementDto timedSimpleDoseElementDto = new TimedSimpleDoseElementDto();
    final SimpleDoseElementDto simpleDoseElementDto = new SimpleDoseElementDto();
    simpleDoseElementDto.setQuantity(100.0d);
    timedSimpleDoseElementDto.setDoseElement(simpleDoseElementDto);
    final List<TimedSimpleDoseElementDto> timedSimpleDoseElementDtoList = new ArrayList<>();
    timedSimpleDoseElementDtoList.add(timedSimpleDoseElementDto);
    therapyDto.setTimedDoseElements(timedSimpleDoseElementDtoList);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is VariableSimpleTherapyDto prescription !without! dose/time pairs and route.
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - 'Dose element is missing'
      - 'Route is missing'
   */
  public void test_07()
  {
    final VariableSimpleTherapyDto therapyDto = TherapyBuilderUtils.createMinimalVariableSimpleTherapy(
        "simple variable prescription");
    therapyDto.getTimedDoseElements().get(0).setDoseElement(null);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(2, errorsDescription.size());
    assertTrue(errorsDescription.containsAll(Arrays.asList(
        TherapyDtoValidatorEnum.DOSE_ELEMENT_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.ROUTE_IS_MISSING.getText())));
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Warfarin' with dose, route, dosing interval, target INR and indication.
    -> The temporary deal is 'if therapy has INR, then it must have indication'.
    Methods isValid must return true.
   */
  public void test_08()
  {
    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createMinimalSimpleTherapyDto("simple prescription");

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    therapyDto.setTargetInr(100.0d);

    final IndicationDto indicationDto = new IndicationDto("1337", "Cool indication");
    therapyDto.setClinicalIndication(indicationDto);

    final DosingFrequencyDto dosingFrequencyDto = new DosingFrequencyDto(DosingFrequencyTypeEnum.MORNING);
    therapyDto.setDosingFrequency(dosingFrequencyDto);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    therapyDtoValidator.print(therapyDto, errorsDescription);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Warfarin' !without! dose, route, dosing interval and indication (with target INR).
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - 'Dose element is missing'
      - 'Route is missing'
      - 'Dosing interval is missing'
      - 'Indication is missing' strings.
   */
  public void test_09()
  {
    resetMocks();

    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createMinimalSimpleTherapyDto("simple prescription");
    therapyDto.setDoseElement(null);
    therapyDto.setTargetInr(100.0d);

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.getProperties().add(new MedicationPropertyDto(1L, MedicationPropertyType.HIGH_ALERT_MEDICATION, "High alert medication"));
    medicationDataDto.setFormulary(true);

    Mockito
            .when(medicationsValueHolderProvider.getMedicationData(therapyDto.getMainMedicationId()))
            .thenReturn(medicationDataDto);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(4, errorsDescription.size());
    assertTrue(errorsDescription.containsAll(Arrays.asList(
        TherapyDtoValidatorEnum.DOSE_ELEMENT_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.ROUTE_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.DOSING_INTERVAL_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.INDICATION_IS_MISSING.getText())));
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription or/with bolus with dose numerator, route and dosing interval.
    Methods isValid must return true.
   */
  public void test_10()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapy(
        "complex constant prescription");

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final DosingFrequencyDto dosingFrequencyDto = new DosingFrequencyDto(DosingFrequencyTypeEnum.MORNING);
    therapyDto.setDosingFrequency(dosingFrequencyDto);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription or/with bolus !without! dose numerator, route and dosing interval.
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - 'Dose element is missing'
      - 'Route is missing'
      - 'Dosing interval is missing'
   */
  public void test_11()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapy(
        "complex constant prescription");

    therapyDto.getIngredientsList().get(0).setQuantity(null);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(3, errorsDescription.size());
    assertTrue(errorsDescription.containsAll(Arrays.asList(
        TherapyDtoValidatorEnum.DOSE_ELEMENT_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.ROUTE_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.DOSING_INTERVAL_IS_MISSING.getText())));
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription with continuous infusion with route and rate.
    Methods isValid must return true.
   */
  public void test_12()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapyContinuousInfusion(
        "complex constant prescription");

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription with continuous infusion !without! route and rate.
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - 'Route is missing'
      - 'Dosing interval is missing'
   */
  public void test_13()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapyContinuousInfusion(
        "complex constant prescription");

    therapyDto.getDoseElement().setRate(null);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(2, errorsDescription.size());
    assertTrue(errorsDescription.containsAll(Arrays.asList(
        TherapyDtoValidatorEnum.ROUTE_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.DOSE_ELEMENT_IS_MISSING.getText())));
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription with rate infusion with route, rate, dose numerator and dosing interval.
    Methods isValid must return true.
   */
  public void test_14()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapyWithRate(
        "complex constant prescription");

    final DosingFrequencyDto dosingFrequencyDto = new DosingFrequencyDto(DosingFrequencyTypeEnum.MORNING);
    therapyDto.setDosingFrequency(dosingFrequencyDto);

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(20.0d);
    complexDoseElementDto.setDuration(60);
    therapyDto.setDoseElement(complexDoseElementDto);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription !without! rate infusion with route, rate, dose numerator and dosing interval.
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - 'Dose element is missing'
      - 'Route is missing'
      - 'Dosing interval is missing'
   */
  public void test_15()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapyWithRate(
        "complex constant prescription");

    /*
      LOG.debug(therapyDto.getIngredientsList().get(0).toString());
      LOG.debug(therapyDto.getDoseElement().toString());
      This exists, but because complexDoseElementDto has only rate (no duration), we get 'Dose element is missing' error
     */

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(3, errorsDescription.size());
    assertTrue(errorsDescription.containsAll(Arrays.asList(
        TherapyDtoValidatorEnum.ROUTE_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.DOSE_ELEMENT_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.DOSING_INTERVAL_IS_MISSING.getText())));
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription FB and adjust to fluid balance with route.
    Methods isValid must return true.
   */
  public void test_16()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapyContinuousInfusion(
        "complex constant prescription");

    therapyDto.setAdjustToFluidBalance(true);
    therapyDto.setDoseType(TherapyDoseTypeEnum.RATE);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription FB and adjust to fluid balance !without! route .
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - 'Route is missing'
   */
  public void test_17()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapyContinuousInfusion(
        "complex constant prescription");

    therapyDto.setAdjustToFluidBalance(true);
    therapyDto.setDoseType(TherapyDoseTypeEnum.RATE);

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(null);
    therapyDto.setDoseElement(complexDoseElementDto);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(1, errorsDescription.size());
    assertTrue(errorsDescription.contains(TherapyDtoValidatorEnum.ROUTE_IS_MISSING.getText()));
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription FB and !without! adjust to fluid balance with route and rate.
    Methods isValid must return true.
   */
  public void test_18()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapyContinuousInfusion(
        "complex constant prescription");

    therapyDto.setAdjustToFluidBalance(false);
    therapyDto.setDoseType(TherapyDoseTypeEnum.RATE);

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(20.0d);
    therapyDto.setDoseElement(complexDoseElementDto);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantComplexTherapyDto prescription FB and !without! adjust to fluid balance !without! route and rate.
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - 'Route is missing'
      - 'Dose element is missing'
   */
  public void test_19()
  {
    final ConstantComplexTherapyDto therapyDto = TherapyBuilderUtils.createMinimalConstantComplexTherapyContinuousInfusion(
        "complex constant prescription");

    therapyDto.setAdjustToFluidBalance(false);
    therapyDto.setDoseType(TherapyDoseTypeEnum.RATE);

    final ComplexDoseElementDto complexDoseElementDto = new ComplexDoseElementDto();
    complexDoseElementDto.setRate(null);
    therapyDto.setDoseElement(complexDoseElementDto);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(2, errorsDescription.size());
    assertTrue(errorsDescription.containsAll(Arrays.asList(
        TherapyDtoValidatorEnum.ROUTE_IS_MISSING.getText(),
        TherapyDtoValidatorEnum.DOSE_ELEMENT_IS_MISSING.getText())));
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Aspirin' with PRN (dosing interval is not mendatory, bud indication is).
    Methods isValid must return true.
   */
  public void test_20()
  {
    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createMinimalSimpleTherapyDto("simple prescription");

    therapyDto.setWhenNeeded(true); //if this is set to false, we get 'Dosing interval is missing' error
    therapyDto.setDosingFrequency(null);
    therapyDto.setClinicalIndication(new IndicationDto("1337", "Cool indication"));

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Aspirin' with PRN (dosing interval is not mendatory, bud indication is).
    Here indication is empty.
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - Indication is missing
   */
  public void test_21()
  {
    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createMinimalSimpleTherapyDto("simple prescription");

    therapyDto.setWhenNeeded(true); //if this is set to false, we get 'Dosing interval is missing' error
    therapyDto.setDosingFrequency(null);
    therapyDto.setClinicalIndication(null);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.ORAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(1, errorsDescription.size());
    assertTrue(errorsDescription.contains(TherapyDtoValidatorEnum.INDICATION_IS_MISSING.getText()));
  }

  @Test
  /*
    Input is VariableSimpleTherapyDto prescription 'Aspirin' with ANTIBIOTIC flag. Indication is mendatory.
    Methods isValid must return true.
   */
  public void test_22()
  {
    final VariableSimpleTherapyDto therapyDto = TherapyBuilderUtils.createFullVariableSimpleTherapy(
        "createFullVariableSimpleTherapy");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.getProperties().add(new MedicationPropertyDto(1L, MedicationPropertyType.ANTIBIOTIC, "Antibiotic"));
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(therapyDto.getMainMedicationId()))
        .thenReturn(medicationDataDto);

    therapyDto.setClinicalIndication(new IndicationDto("1337", "Cool indication"));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is VariableSimpleTherapyDto prescription 'Aspirin' with ANTIBIOTIC flag. Indication is mendatory.
    Here indication is empty.
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - Indication is missing
   */
  public void test_23()
  {
    final VariableSimpleTherapyDto therapyDto = TherapyBuilderUtils.createFullVariableSimpleTherapy(
        "createFullVariableSimpleTherapy");

    therapyDto.setClinicalIndication(null);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(1, errorsDescription.size());
    assertTrue(errorsDescription.contains(TherapyDtoValidatorEnum.INDICATION_IS_MISSING.getText()));
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Aspirin' with formulary flag set to false
    Methods isValid must return true.
   */
  public void test_24()
  {
    resetMocks();

    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createFullConstantSimpleTherapy(
        "createFullVariableSimpleTherapy");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setFormulary(false);
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(therapyDto.getMainMedicationId()))
        .thenReturn(medicationDataDto);

    therapyDto.setTitration(null);
    therapyDto.setComment("Cool comment");

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Aspirin' with formulary flag set to false
    Here comment is 1 length long.
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - Comment is missing
   */
  public void test_25()
  {
    resetMocks();

    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createFullConstantSimpleTherapy(
        "createFullVariableSimpleTherapy");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    medicationDataDto.setFormulary(false);
    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(therapyDto.getMainMedicationId()))
        .thenReturn(medicationDataDto);

    therapyDto.setTitration(null);
    therapyDto.setComment("C");

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(1, errorsDescription.size());
    assertTrue(errorsDescription.contains(TherapyDtoValidatorEnum.COMMENT_IS_MISSING.getText()));
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Aspirin' with universal form prescription
    Methods isValid must return true.
   */
  public void test_26()
  {
    resetMocks();

    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createFullConstantSimpleTherapy(
        "createFullVariableSimpleTherapy");

    for (final MedicationDto medicationDto : therapyDto.getMedications())
    {
      medicationDto.setId(null);
    }

    therapyDto.setTitration(null);
    therapyDto.setComment("Cool comment");

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Aspirin' with universal form prescription
    Here comment is 1 length long.
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - Comment is missing
   */
  public void test_27()
  {
    resetMocks();

    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createFullConstantSimpleTherapy(
        "createFullVariableSimpleTherapy");

    for (final MedicationDto medicationDto : therapyDto.getMedications())
    {
      medicationDto.setId(null);
    }

    therapyDto.setTitration(null);
    therapyDto.setComment("C");

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(1, errorsDescription.size());
    assertTrue(errorsDescription.contains(TherapyDtoValidatorEnum.COMMENT_IS_MISSING.getText()));
  }

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Aspirin' with reminder days and comment
    Methods isValid must return true.
   */
public void test_28()
{
  final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createFullConstantSimpleTherapy(
      "createFullVariableSimpleTherapy");

  therapyDto.setTitration(null);
  therapyDto.setReviewReminderComment("Cool review reminder comment");
  therapyDto.setReviewReminderDays(13);

  final boolean isValid = therapyDtoValidator.isValid(therapyDto);
  assertTrue(isValid);
}

  @Test
  /*
    Input is ConstantSimpleTherapyDto prescription 'Aspirin' with reminder comment and without reminder days
    Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - Review reminder days is missing
   */
  public void test_29()
  {
    final ConstantSimpleTherapyDto therapyDto = TherapyBuilderUtils.createFullConstantSimpleTherapy(
        "createFullVariableSimpleTherapy");

    therapyDto.setTitration(null);
    therapyDto.setReviewReminderComment("Cool review reminder comment");
    therapyDto.setReviewReminderDays(null);

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(1, errorsDescription.size());
    assertTrue(errorsDescription.contains(TherapyDtoValidatorEnum.REVIEW_REMINDER_DAYS_IS_MISSING.getText()));
  }

  @Test
  /*
    Input is OxygenTherapyDto prescription with max and min target saturation
    Methods isValid must return true.
   */
  public void test_30()
  {
    final OxygenTherapyDto therapyDto = TherapyBuilderUtils.createOxygenTherapy("createOxygenTherapy");

    therapyDto.setMaxTargetSaturation(100.0);
    therapyDto.setMinTargetSaturation(50.0);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.INHAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertTrue(isValid);
  }

  @Test
  /*
    Input is OxygenTherapyDto prescription with max but !without! min target saturation
     Methods isValid must return false.
    Response of method getErrorsDescription must contains
      - Target saturation is missing
   */
  public void test_31()
  {
    final OxygenTherapyDto therapyDto = TherapyBuilderUtils.createOxygenTherapy("createOxygenTherapy");

    therapyDto.setMaxTargetSaturation(100.0);
    therapyDto.setMinTargetSaturation(null);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setType(MedicationRouteTypeEnum.INHAL);
    therapyDto.setRoutes(Lists.newArrayList(route));

    final boolean isValid = therapyDtoValidator.isValid(therapyDto);
    assertFalse(isValid);

    final List<String> errorsDescription = therapyDtoValidator.getErrorsDescription(therapyDto);
    assertEquals(1, errorsDescription.size());
    assertTrue(errorsDescription.contains(TherapyDtoValidatorEnum.TARGET_SATURATION_IS_MISSING.getText()));
  }
}
