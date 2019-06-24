package com.marand.thinkmed.medications.business.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.marand.ispek.ehr.common.EhrLinkType;
import com.marand.maf.core.JsonUtil;
import com.marand.maf.core.time.Intervals;
import com.marand.openehr.util.DataValueUtils;
import com.marand.thinkmed.medications.MedsJsonDeserializer;
import com.marand.thinkmed.medications.TherapyStatusEnum;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.InfusionIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationRouteDto;
import com.marand.thinkmed.medications.api.internal.dto.MedicationTypeEnum;
import com.marand.thinkmed.medications.api.internal.dto.VariableComplexTherapyDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.ComplexDoseElementDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.TimedComplexDoseElementDto;
import com.marand.thinkmed.medications.dto.InfusionRateCalculationDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthMedicationDto;
import com.marand.thinkmed.medications.dto.mentalHealth.MentalHealthTherapyDto;
import com.marand.thinkmed.medications.dto.pharmacist.review.PharmacistReviewDto;
import com.marand.thinkmed.medications.ehr.model.InpatientPrescription;
import com.marand.thinkmed.medications.ehr.model.ReferenceWeight;
import com.marand.thinkmed.medications.ehr.utils.LinksEhrUtils;
import com.marand.thinkmed.medications.units.converter.UnitsConverter;
import com.marand.thinkmed.medications.units.converter.impl.UnitsConverterImpl;
import com.marand.thinkmed.medications.units.provider.TestUnitsProviderImpl;
import com.marand.thinkmed.medications.units.provider.UnitsProvider;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProviderImpl;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

/**
 * @author Mitja Lapajne
 */

@SuppressWarnings("TooBroadScope")
@RunWith(SpringJUnit4ClassRunner.class)
public class DefaultMedicationsBoTest
{
  @InjectMocks
  private final DefaultMedicationsBo medicationsBo = new DefaultMedicationsBo();

  @Mock
  private MedicationsValueHolderProviderImpl medicationsValueHolderProvider;

  @Spy
  private final UnitsProvider unitsProvider = new TestUnitsProviderImpl();

  private UnitsConverter unitsConverter;

  @BeforeClass
  public static void initRequest()
  {
    RequestUser.init(auth -> new UserDto("Test", null, "Test", Collections.emptyList()));
  }

  @Before
  public void setUp()
  {
    final UnitsConverterImpl uc = new UnitsConverterImpl();
    uc.setUnitsProvider(unitsProvider);

    unitsConverter = spy(uc);
    medicationsBo.setUnitsConverter(unitsConverter);
  }

  @Test
  public void testAreInpatientPrescriptionsLinkedByUpdate()
  {
    final InpatientPrescription composition1 = new InpatientPrescription();
    composition1.setUid("uid1");
    final InpatientPrescription composition2 = new InpatientPrescription();
    composition2.setUid("uid2");

    composition2.getLinks().add(LinksEhrUtils.createLink(
        "uid1",
        "update",
        EhrLinkType.UPDATE));

    assertTrue(medicationsBo.areInpatientPrescriptionsLinkedByUpdate(composition2, composition1));
    assertTrue(medicationsBo.areInpatientPrescriptionsLinkedByUpdate(composition1, composition2));
  }

  @Test
  public void testFilterMentalHealthDrugsList()
  {
    final List<MentalHealthTherapyDto> mentalHealthTherapyDtos = new ArrayList<>();

    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(1L, 1, TherapyStatusEnum.NORMAL));
    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(2L, 1, TherapyStatusEnum.NORMAL));
    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(1L, 1, TherapyStatusEnum.NORMAL));
    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(2L, 1, TherapyStatusEnum.NORMAL));

    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(2L, 2, TherapyStatusEnum.NORMAL));
    mentalHealthTherapyDtos.add(createMentalHealthTherapyDto(2L, 2, TherapyStatusEnum.ABORTED));

    final Set<MentalHealthTherapyDto> mentalHealthMedicationDtoSet = medicationsBo.filterMentalHealthTherapyList(
        mentalHealthTherapyDtos);
    assertEquals(4, mentalHealthMedicationDtoSet.size());
  }

  private MentalHealthTherapyDto createMentalHealthTherapyDto(
      final long routeId,
      final long medicationId,
      final TherapyStatusEnum therapyStatusEnum)
  {
    final MentalHealthTherapyDto mentalHealthTherapyDto = new MentalHealthTherapyDto();
    mentalHealthTherapyDto.setTherapyStatusEnum(therapyStatusEnum);

    final MedicationRouteDto route = new MedicationRouteDto();
    route.setId(routeId);
    route.setName("route name");

    final MentalHealthMedicationDto mentalHealthMedicationDto = new MentalHealthMedicationDto(
        medicationId,
        "medication name",
        "generic name",
        route);

    mentalHealthTherapyDto.setMentalHealthMedicationDto(mentalHealthMedicationDto);
    return mentalHealthTherapyDto;
  }

  @Test
  public void testIsTherapyActive()
  {
    //1.10.2013 is TUESDAY
    final List<String> daysOfWeek = new ArrayList<>();
    daysOfWeek.add("TUESDAY");
    daysOfWeek.add("WEDNESDAY");

    //test start day on active day of week
    assertTrue(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 1, 11, 0, 0))
    );
    //test some day on active day of week
    assertTrue(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 2, 11, 0, 0))
    );
    //test some day on inactive day of week
    assertFalse(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 3, 11, 0, 0))
    );
    //test therapy finished
    assertFalse(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            new Interval(new DateTime(2013, 5, 1, 10, 0, 0), new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 2, 11, 0, 0))
    );
    //test therapy not started yet
    assertFalse(
        medicationsBo.isTherapyActive(
            daysOfWeek,
            null,
            Intervals.infiniteFrom(new DateTime(2013, 10, 2, 10, 0, 0)),
            new DateTime(2013, 10, 1, 11, 0, 0))
    );
    //test every second day on start day
    assertTrue(
        medicationsBo.isTherapyActive(
            null,
            2,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 1, 11, 0, 0))
    );
    //test every second day on inactive day
    assertFalse(
        medicationsBo.isTherapyActive(
            null,
            2,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 2, 11, 0, 0))
    );
    //test every second day on active day
    assertTrue(
        medicationsBo.isTherapyActive(
            null,
            2,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 3, 11, 0, 0))
    );
    //test every fifth day on active day
    assertTrue(
        medicationsBo.isTherapyActive(
            null,
            5,
            Intervals.infiniteFrom(new DateTime(2013, 10, 1, 10, 0, 0)),
            new DateTime(2013, 10, 6, 11, 0, 0))
    );
    //test every fifth day on inactive day
    assertFalse(
        medicationsBo.isTherapyActive(
            null,
            5,
            Intervals.infiniteFrom(new DateTime(2013, 1, 10, 10, 0, 0)),
            new DateTime(2013, 1, 5, 11, 0, 0))
    );
  }

  @Test
  public void testBuildReferenceWeightComposition()
  {
    final DateTime testTimestamp = new DateTime(2013, 11, 28, 16, 0, 0);

    final ReferenceWeight referenceWeight = medicationsBo.buildReferenceWeightComposition(10.0, testTimestamp);
    assertEquals(new Double(10.0), new Double(referenceWeight.getReferenceBodyWeight().getWeight().getMagnitude()));
    assertEquals(testTimestamp, DataValueUtils.getDateTime(referenceWeight.getReferenceBodyWeight().getTime()));
  }

  @Test
  public void testCalculateInfusionFormulaFromRate1()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(10.0);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setQuantityDenominator(5.0);
    final Double formula = medicationsBo.calculateInfusionFormulaFromRate(10.0, calculationDto, 10.0, "mg/kg/h", 10.0);
    assertEquals(new Double(2.0), formula);
  }

  @Test
  public void testCalculateInfusionRateFromFormula1()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(10.0);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setQuantityDenominator(5.0);
    final Double rate = medicationsBo.calculateInfusionRateFromFormula(2.0, "mg/kg/h", calculationDto, 10.0, 70.0);
    assertEquals(new Double(10.0), rate);
  }

  @Test
  public void testCalculateInfusionFormulaFromRate2()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(0.5);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setQuantityDenominator(2.0);
    final Double formula = medicationsBo.calculateInfusionFormulaFromRate(
        21.0,
        calculationDto,
        10.0,
        "microgram/kg/min",
        5.0);
    final double formulaWithNormalPrecision = Math.round(formula * 1000.0) / 1000.0;
    assertEquals(new Double(17.5), new Double(formulaWithNormalPrecision));
  }

  @Test
  public void testCalculateInfusionRateFromFormula2()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(0.5);
    calculationDto.setQuantityUnit("mg");
    calculationDto.setQuantityDenominator(2.0);
    final Double rate = medicationsBo.calculateInfusionRateFromFormula(17.5, "microgram/kg/min", calculationDto, 5.0, 70.0);
    final double rateWithNormalPrecision = Math.round(rate * 1000.0) / 1000.0;
    assertEquals(new Double(21.0), new Double(rateWithNormalPrecision));
  }

  @Test
  public void testCalculateInfusionFormulaFromRate3()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(50.0);
    calculationDto.setQuantityUnit("nanogram");
    calculationDto.setQuantityDenominator(1.0);
    final Double formula = medicationsBo.calculateInfusionFormulaFromRate(
        100.0,
        calculationDto,
        10.0,
        "microgram/kg/d",
        10.0);
    final double formulaWithNormalPrecision = Math.round(formula * 1000.0) / 1000.0;
    assertEquals(new Double(12.0), new Double(formulaWithNormalPrecision));
  }

  @Test
  public void testCalculateInfusionRateFromFormula3()
  {
    final InfusionRateCalculationDto calculationDto = new InfusionRateCalculationDto();
    calculationDto.setQuantity(50.0);
    calculationDto.setQuantityUnit("nanogram");
    calculationDto.setQuantityDenominator(1.0);
    final Double rate = medicationsBo.calculateInfusionRateFromFormula(12.0, "microgram/kg/d", calculationDto, 10.0, 70.0);
    final double rateWithNormalPrecision = Math.round(rate * 1000.0) / 1000.0;
    assertEquals(new Double(100.0), new Double(rateWithNormalPrecision));
  }

  @Test
  public void testGetInfusionRateCalculationData1()
  {
    //single infusion ingredient (MEDICATION), normal infusion
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setContinuousInfusion(false);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionRateCalculationDto data = medicationsBo.getInfusionRateCalculationData(therapy);
    assertEquals(new Double(10.0), data.getQuantity());
    assertEquals("mg", data.getQuantityUnit());
    assertEquals(new Double(5.0), data.getQuantityDenominator());
  }

  @Test
  public void testGetInfusionRateCalculationData2()
  {
    //single infusion ingredient (MEDICATION), continuous infusion

    final Map<Long, MedicationDataDto> medicationsMap = new HashMap<>();
    final MedicationDataDto medicationDto1 = new MedicationDataDto();
    final MedicationDto medication1 = new MedicationDto();
    medication1.setId(1L);
    medication1.setName("Lekadol 20x500mg");
    medicationDto1.setMedication(medication1);
    medicationsMap.put(1L, medicationDto1);

    final MedicationDataDto medicationDto2 = new MedicationDataDto();
    final MedicationDto medication2 = new MedicationDto();
    medication2.setId(2L);
    medication2.setName("Primotren 20x500mg");
    medicationDto2.setMedication(medication2);
    medicationsMap.put(2L, medicationDto2);

    Mockito.when(medicationsValueHolderProvider.getMedicationData(1L)).thenReturn(medicationsMap.get(1L));

    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setStart(new DateTime(2013, 12, 10, 12, 0, 0));
    therapy.setContinuousInfusion(true);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);
    medication.setId(1L);

    final InfusionRateCalculationDto data = medicationsBo.getInfusionRateCalculationData(therapy); // TODO nejc definingIngredient
    //assertEquals(new Double(10.0), data.getQuantity());
    //assertEquals("mg", data.getQuantityUnit());
    //assertEquals(new Double(5.0), data.getQuantityDenominator());
  }

  @Test
  public void testGetInfusionRateCalculationData3()
  {
    //multiple infusion ingredients (only one MEDICATION)
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setVolumeSum(55.0);

    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient1);
    ingredient1.setQuantityUnit("mg");
    ingredient1.setQuantity(10.0);
    ingredient1.setQuantityDenominator(5.0);
    final MedicationDto medication1 = new MedicationDto();
    ingredient1.setMedication(medication1);
    medication1.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient2);
    ingredient2.setQuantityDenominator(50.0);
    final MedicationDto solution = new MedicationDto();
    ingredient2.setMedication(solution);
    solution.setMedicationType(MedicationTypeEnum.DILUENT);

    final InfusionRateCalculationDto data = medicationsBo.getInfusionRateCalculationData(therapy);
    assertEquals(new Double(10.0), data.getQuantity());
    assertEquals("mg", data.getQuantityUnit());
    assertEquals(new Double(55.0), data.getQuantityDenominator());
  }

  @Test
  public void testGetInfusionRateCalculationData4()
  {
    //multiple infusion ingredients (multiple MEDICATION-s)
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setVolumeSum(55.0);

    final InfusionIngredientDto ingredient1 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient1);
    ingredient1.setQuantityUnit("mg");
    ingredient1.setQuantity(10.0);
    ingredient1.setQuantityDenominator(5.0);
    final MedicationDto medication1 = new MedicationDto();
    ingredient1.setMedication(medication1);
    medication1.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionIngredientDto ingredient2 = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient2);
    ingredient2.setQuantityUnit("mg");
    ingredient2.setQuantity(2.0);
    ingredient2.setQuantityDenominator(1.0);
    final MedicationDto solution = new MedicationDto();
    ingredient2.setMedication(solution);
    solution.setMedicationType(MedicationTypeEnum.MEDICATION);

    final InfusionRateCalculationDto data = medicationsBo.getInfusionRateCalculationData(therapy);
    assertNull(data);
  }

  @Test
  public void testFillInfusionFormulaFromRate1()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setContinuousInfusion(false);
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    therapy.setDoseElement(doseElement);
    doseElement.setRateFormulaUnit("mg/kg/h");
    doseElement.setRate(10.0);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionFormulaFromRate(therapy, 10.0, 70.0);
    assertEquals(new Double(2.0), therapy.getDoseElement().getRateFormula());
  }

  @Test
  public void testFillInfusionRateFromFormula1()
  {
    final ConstantComplexTherapyDto therapy = new ConstantComplexTherapyDto();
    therapy.setContinuousInfusion(false);
    final ComplexDoseElementDto doseElement = new ComplexDoseElementDto();
    therapy.setDoseElement(doseElement);
    doseElement.setRateFormulaUnit("mg/kg/h");
    doseElement.setRateFormula(2.0);
    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionRateFromFormula(therapy, 10.0, 70.0);
    assertEquals(new Double(10.0), therapy.getDoseElement().getRate());
  }

  @Test
  public void testFillInfusionFormulaFromRate2()
  {
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    therapy.setContinuousInfusion(false);

    final TimedComplexDoseElementDto timedComplexDoseElement1 = new TimedComplexDoseElementDto();
    therapy.getTimedDoseElements().add(timedComplexDoseElement1);
    final ComplexDoseElementDto doseElement1 = new ComplexDoseElementDto();
    timedComplexDoseElement1.setDoseElement(doseElement1);
    doseElement1.setRateFormulaUnit("mg/kg/h");
    doseElement1.setRate(10.0);

    final TimedComplexDoseElementDto timedComplexDoseElement2 = new TimedComplexDoseElementDto();
    therapy.getTimedDoseElements().add(timedComplexDoseElement2);
    final ComplexDoseElementDto doseElement2 = new ComplexDoseElementDto();
    timedComplexDoseElement2.setDoseElement(doseElement2);
    doseElement2.setRateFormulaUnit("mg/kg/h");
    doseElement2.setRate(20.0);

    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionFormulaFromRate(therapy, 10.0, 70.0);
    assertEquals(new Double(2.0), therapy.getTimedDoseElements().get(0).getDoseElement().getRateFormula());
    assertEquals(new Double(4.0), therapy.getTimedDoseElements().get(1).getDoseElement().getRateFormula());
  }

  @Test
  public void testFillInfusionRateFromFormula2()
  {
    final VariableComplexTherapyDto therapy = new VariableComplexTherapyDto();
    therapy.setContinuousInfusion(false);

    final TimedComplexDoseElementDto timedComplexDoseElement1 = new TimedComplexDoseElementDto();
    therapy.getTimedDoseElements().add(timedComplexDoseElement1);
    final ComplexDoseElementDto doseElement1 = new ComplexDoseElementDto();
    timedComplexDoseElement1.setDoseElement(doseElement1);
    doseElement1.setRateFormulaUnit("mg/kg/h");
    doseElement1.setRateFormula(2.0);

    final TimedComplexDoseElementDto timedComplexDoseElement2 = new TimedComplexDoseElementDto();
    therapy.getTimedDoseElements().add(timedComplexDoseElement2);
    final ComplexDoseElementDto doseElement2 = new ComplexDoseElementDto();
    timedComplexDoseElement2.setDoseElement(doseElement2);
    doseElement2.setRateFormulaUnit("mg/kg/h");
    doseElement2.setRateFormula(4.0);

    final InfusionIngredientDto ingredient = new InfusionIngredientDto();
    therapy.getIngredientsList().add(ingredient);
    ingredient.setQuantityUnit("mg");
    ingredient.setQuantity(10.0);
    ingredient.setQuantityDenominator(5.0);
    final MedicationDto medication = new MedicationDto();
    ingredient.setMedication(medication);
    medication.setMedicationType(MedicationTypeEnum.MEDICATION);

    medicationsBo.fillInfusionRateFromFormula(therapy, 10.0, 70.0);
    assertEquals(new Double(10.0), therapy.getTimedDoseElements().get(0).getDoseElement().getRate());
    assertEquals(new Double(20.0), therapy.getTimedDoseElements().get(1).getDoseElement().getRate());
  }

  @Test
  public void pharmacyReviewJsonDeserializer()
  {
    final String json =
        "[\n" +
            "  {\n" +
            "    \"configs\": [],\n" +
            "    \"createTimestamp\": \"2015-04-14T14:58:14.346Z\",\n" +
            "    \"compositionId\": null,\n" +
            "    \"composer\": {\n" +
            "      \"id\": \"2323321\",\n" +
            "      \"name\": \"Today Guy\"\n" +
            "    },\n" +
            "    \"referBackToPescriber\": null,\n" +
            "    \"relatedTherapies\": [\n" +
            "      {\n" +
            "        \"therapy\": {\n" +
            "          \"doseElement\": {\n" +
            "            \"quantity\": 4,\n" +
            "            \"doseDescription\": null,\n" +
            "            \"quantityDenominator\": 0.4,\n" +
            "            \"serialVersionUID\": 0\n" +
            "          },\n" +
            "          \"medication\": {\n" +
            "            \"id\": 1717028,\n" +
            "            \"name\": \"ASPIRIN 10 MG SVEČKA ZA OTROKE\",\n" +
            "            \"shortName\": \"ASPIRIN 10 mg SVEČKA ZA OTROKE \",\n" +
            "            \"genericName\": \"acetilsalicilna kislina\",\n" +
            "            \"medicationType\": \"MEDICATION\",\n" +
            "            \"displayName\": \"acetilsalicilna kislina (ASPIRIN 10 MG SVEČKA ZA OTROKE)\",\n" +
            "            \"serialVersionUID\": 0\n" +
            "          },\n" +
            "          \"quantityUnit\": \"mg\",\n" +
            "          \"doseForm\": {\n" +
            "            \"doseFormType\": \"SUPPOSITORY\",\n" +
            "            \"medicationOrderFormType\": \"SIMPLE\",\n" +
            "            \"serialVersionUID\": 0,\n" +
            "            \"name\": \"Svečka\",\n" +
            "            \"code\": \"289\",\n" +
            "            \"description\": null,\n" +
            "            \"deleted\": false,\n" +
            "            \"auditInfo\": null,\n" +
            "            \"version\": 0,\n" +
            "            \"id\": 289\n" +
            "          },\n" +
            "          \"quantityDenominatorUnit\": \"svečka\",\n" +
            "          \"quantityDisplay\": \"4 mg\",\n" +
            "          \"compositionUid\": \"07e5280c-3309-48ce-a8b6-04ad41a53da5::prod.pediatrics.marand.si::3\",\n" +
            "          \"ehrOrderName\": \"Medication instruction\",\n" +
            "          \"medicationOrderFormType\": \"SIMPLE\",\n" +
            "          \"variable\": false,\n" +
            "          \"therapyDescription\": \"acetilsalicilna kislina (ASPIRIN 10 MG SVEČKA ZA OTROKE) - 4 mg - 3X na dan - rect\",\n" +
            "          \"route\": {\n" +
            "            \"type\": null,\n" +
            "            \"unlicensedRoute\": false,\n" +
            "            \"serialVersionUID\": 0,\n" +
            "            \"code\": \"35\",\n" +
            "            \"name\": \"rect\",\n" +
            "            \"id\": 0\n" +
            "          },\n" +
            "          \"dosingFrequency\": {\n" +
            "            \"type\": \"DAILY_COUNT\",\n" +
            "            \"value\": 3\n" +
            "          },\n" +
            "          \"dosingDaysFrequency\": null,\n" +
            "          \"daysOfWeek\": null,\n" +
            "          \"start\": {\n" +
            "            \"data\": \"2015-03-11T13:48:00.000+01:00\",\n" +
            "            \"default\": \"11.3.2015\",\n" +
            "            \"short.date\": \"11.3.2015\",\n" +
            "            \"short.time\": \"13:48\",\n" +
            "            \"short.date.time\": \"11.3.2015 13:48\"\n" +
            "          },\n" +
            "          \"end\": null,\n" +
            "          \"whenNeeded\": false,\n" +
            "          \"comment\": null,\n" +
            "          \"clinicalIndication\": null,\n" +
            "          \"prescriberName\": null,\n" +
            "          \"composerName\": \"Tadej Avčin\",\n" +
            "          \"startCriterion\": null,\n" +
            "          \"applicationPrecondition\": null,\n" +
            "          \"frequencyDisplay\": \"3X na dan\",\n" +
            "          \"daysFrequencyDisplay\": null,\n" +
            "          \"whenNeededDisplay\": null,\n" +
            "          \"startCriterionDisplay\": null,\n" +
            "          \"daysOfWeekDisplay\": null,\n" +
            "          \"applicationPreconditionDisplay\": null,\n" +
            "          \"formattedTherapyDisplay\": \"<span class='GenericName TextDataBold'>acetilsalicilna kislina </span><span class='MedicationName TextData'>(ASPIRIN 10 MG SVEČKA ZA OTROKE) </span><br><span class='DoseLabel TextLabel'>ODMEREK </span><span class='Quantity TextData'>4 mg </span><span class='Delimiter TextData'><span> &ndash; </span> </span><span class='Frequency TextData'>3X na dan </span><span class='Delimiter TextData'><span> &ndash; </span> </span><span class='Route TextData'>rect </span><span class='TherapyInterval'><br><span class='FromLabel TextLabel'>Od </span><span class='From TextData'>11.3.2015 13:48 </span></span>\",\n" +
            "          \"pastDaysOfTherapy\": null,\n" +
            "          \"linkName\": null,\n" +
            "          \"maxDailyFrequency\": null,\n" +
            "          \"createdTimestamp\": \"2015-03-11T13:48:41.174+01:00\",\n" +
            "          \"tags\": [],\n" +
            "          \"criticalWarnings\": [],\n" +
            "          \"serialVersionUID\": 0\n" +
            "        },\n" +
            "        \"therapyStatus\": \"NORMAL\",\n" +
            "        \"doctorReviewNeeded\": true,\n" +
            "        \"therapyEndsBeforeNextRounds\": false,\n" +
            "        \"modifiedFromLastReview\": false,\n" +
            "        \"modified\": false,\n" +
            "        \"active\": true,\n" +
            "        \"consecutiveDay\": 26,\n" +
            "        \"showConsecutiveDay\": false\n" +
            "      },\n" +
            "      {\n" +
            "        \"therapy\": {\n" +
            "          \"timedDoseElements\": [\n" +
            "            {\n" +
            "              \"doseElement\": {\n" +
            "                \"quantity\": 15,\n" +
            "                \"doseDescription\": null,\n" +
            "                \"quantityDenominator\": null,\n" +
            "                \"serialVersionUID\": 0\n" +
            "              },\n" +
            "              \"doseTime\": {\n" +
            "                \"hour\": 8,\n" +
            "                \"minute\": 0\n" +
            "              },\n" +
            "              \"date\": null,\n" +
            "              \"timeDisplay\": \"08:00\",\n" +
            "              \"quantityDisplay\": \"15 mg\",\n" +
            "              \"serialVersionUID\": 0\n" +
            "            },\n" +
            "            {\n" +
            "              \"doseElement\": {\n" +
            "                \"quantity\": 20,\n" +
            "                \"doseDescription\": null,\n" +
            "                \"quantityDenominator\": null,\n" +
            "                \"serialVersionUID\": 0\n" +
            "              },\n" +
            "              \"doseTime\": {\n" +
            "                \"hour\": 13,\n" +
            "                \"minute\": 0\n" +
            "              },\n" +
            "              \"date\": null,\n" +
            "              \"timeDisplay\": \"13:00\",\n" +
            "              \"quantityDisplay\": \"20 mg\",\n" +
            "              \"serialVersionUID\": 0\n" +
            "            },\n" +
            "            {\n" +
            "              \"doseElement\": {\n" +
            "                \"quantity\": 15,\n" +
            "                \"doseDescription\": null,\n" +
            "                \"quantityDenominator\": null,\n" +
            "                \"serialVersionUID\": 0\n" +
            "              },\n" +
            "              \"doseTime\": {\n" +
            "                \"hour\": 17,\n" +
            "                \"minute\": 0\n" +
            "              },\n" +
            "              \"date\": null,\n" +
            "              \"timeDisplay\": \"17:00\",\n" +
            "              \"quantityDisplay\": \"15 mg\",\n" +
            "              \"serialVersionUID\": 0\n" +
            "            },\n" +
            "            {\n" +
            "              \"doseElement\": {\n" +
            "                \"quantity\": 20,\n" +
            "                \"doseDescription\": null,\n" +
            "                \"quantityDenominator\": null,\n" +
            "                \"serialVersionUID\": 0\n" +
            "              },\n" +
            "              \"doseTime\": {\n" +
            "                \"hour\": 21,\n" +
            "                \"minute\": 0\n" +
            "              },\n" +
            "              \"date\": null,\n" +
            "              \"timeDisplay\": \"21:00\",\n" +
            "              \"quantityDisplay\": \"20 mg\",\n" +
            "              \"serialVersionUID\": 0\n" +
            "            }\n" +
            "          ],\n" +
            "          \"medication\": {\n" +
            "            \"id\": 1061700,\n" +
            "            \"name\": \"SINECOD 50 mg film.obl.tbl. \",\n" +
            "            \"shortName\": \"Sinecod tbl.\",\n" +
            "            \"genericName\": \"butamirat\",\n" +
            "            \"medicationType\": \"MEDICATION\",\n" +
            "            \"displayName\": \"butamirat (SINECOD 50 mg film.obl.tbl. )\",\n" +
            "            \"serialVersionUID\": 0\n" +
            "          },\n" +
            "          \"quantityUnit\": \"mg\",\n" +
            "          \"doseForm\": {\n" +
            "            \"doseFormType\": null,\n" +
            "            \"medicationOrderFormType\": \"SIMPLE\",\n" +
            "            \"serialVersionUID\": 0,\n" +
            "            \"name\": \"Tableta s podaljšanim sproščanjem\",\n" +
            "            \"code\": \"296\",\n" +
            "            \"description\": null,\n" +
            "            \"deleted\": false,\n" +
            "            \"auditInfo\": null,\n" +
            "            \"version\": 0,\n" +
            "            \"id\": 296\n" +
            "          },\n" +
            "          \"quantityDenominatorUnit\": null,\n" +
            "          \"quantityDisplay\": \"15-20-15-20 mg\",\n" +
            "          \"compositionUid\": \"8da7c530-03c8-44ec-b022-8539c43f0b71::prod.pediatrics.marand.si::3\",\n" +
            "          \"ehrOrderName\": \"Medication instruction\",\n" +
            "          \"medicationOrderFormType\": \"SIMPLE\",\n" +
            "          \"variable\": true,\n" +
            "          \"therapyDescription\": \"butamirat (SINECOD 50 mg film.obl.tbl. ) - 15-20-15-20 mg - 4X na dan - po\",\n" +
            "          \"route\": {\n" +
            "            \"type\": null,\n" +
            "            \"unlicensedRoute\": false,\n" +
            "            \"serialVersionUID\": 0,\n" +
            "            \"code\": \"34\",\n" +
            "            \"name\": \"po\",\n" +
            "            \"id\": 0\n" +
            "          },\n" +
            "          \"dosingFrequency\": {\n" +
            "            \"type\": \"DAILY_COUNT\",\n" +
            "            \"value\": 4\n" +
            "          },\n" +
            "          \"dosingDaysFrequency\": null,\n" +
            "          \"daysOfWeek\": null,\n" +
            "          \"start\": {\n" +
            "            \"data\": \"2015-03-11T14:17:00.000+01:00\",\n" +
            "            \"default\": \"11.3.2015\",\n" +
            "            \"short.date\": \"11.3.2015\",\n" +
            "            \"short.time\": \"14:17\",\n" +
            "            \"short.date.time\": \"11.3.2015 14:17\"\n" +
            "          },\n" +
            "          \"end\": null,\n" +
            "          \"whenNeeded\": false,\n" +
            "          \"comment\": null,\n" +
            "          \"clinicalIndication\": null,\n" +
            "          \"prescriberName\": null,\n" +
            "          \"composerName\": \"Tadej Avčin\",\n" +
            "          \"startCriterion\": null,\n" +
            "          \"applicationPrecondition\": null,\n" +
            "          \"frequencyDisplay\": \"4X na dan\",\n" +
            "          \"daysFrequencyDisplay\": null,\n" +
            "          \"whenNeededDisplay\": null,\n" +
            "          \"startCriterionDisplay\": null,\n" +
            "          \"daysOfWeekDisplay\": null,\n" +
            "          \"applicationPreconditionDisplay\": null,\n" +
            "          \"formattedTherapyDisplay\": \"<span class='GenericName TextDataBold'>butamirat </span><span class='MedicationName TextData'>(SINECOD 50 mg film.obl.tbl. ) </span><br><span class='DoseLabel TextLabel'>ODMEREK </span><span class='Quantity TextData'>15-20-15-20 mg </span><span class='Delimiter TextData'><span> &ndash; </span> </span><span class='Frequency TextData'>4X na dan </span><span class='Delimiter TextData'><span> &ndash; </span> </span><span class='Route TextData'>po </span><span class='TherapyInterval'><br><span class='FromLabel TextLabel'>Od </span><span class='From TextData'>11.3.2015 14:17 </span></span>\",\n" +
            "          \"pastDaysOfTherapy\": null,\n" +
            "          \"linkName\": null,\n" +
            "          \"maxDailyFrequency\": null,\n" +
            "          \"createdTimestamp\": \"2015-03-11T14:17:33.693+01:00\",\n" +
            "          \"tags\": [],\n" +
            "          \"criticalWarnings\": [],\n" +
            "          \"serialVersionUID\": 0\n" +
            "        },\n" +
            "        \"therapyStatus\": \"NORMAL\",\n" +
            "        \"doctorReviewNeeded\": true,\n" +
            "        \"therapyEndsBeforeNextRounds\": false,\n" +
            "        \"modifiedFromLastReview\": false,\n" +
            "        \"modified\": false,\n" +
            "        \"active\": true,\n" +
            "        \"consecutiveDay\": 26,\n" +
            "        \"showConsecutiveDay\": false\n" +
            "      }\n" +
            "    ],\n" +
            "    \"drugRelatedProblem\": {\n" +
            "      \"c\": {\n" +
            "        \"categories\": [\n" +
            "          {\n" +
            "            \"id\": 123,\n" +
            "            \"name\": \"Adherence issue\"\n" +
            "          }\n" +
            "        ],\n" +
            "        \"outcome\": {\n" +
            "          \"id\": 123,\n" +
            "          \"name\": \"Cost saving only\"\n" +
            "        },\n" +
            "        \"impact\": {\n" +
            "          \"id\": 123,\n" +
            "          \"name\": \"Potentially severe\"\n" +
            "        },\n" +
            "        \"recommendation\": \"Withold Warfarin for next 2 days as INR >6. Repeat INR and reassess dose based on tomorrow's result.\"\n" +
            "      },\n" +
            "      \"configs\": [],\n" +
            "      \"categories\": [\n" +
            "        {\n" +
            "          \"id\": 123,\n" +
            "          \"name\": \"Adherence issue\"\n" +
            "        }\n" +
            "      ],\n" +
            "      \"outcome\": {\n" +
            "        \"id\": 123,\n" +
            "        \"name\": \"Cost saving only\"\n" +
            "      },\n" +
            "      \"impact\": {\n" +
            "        \"id\": 123,\n" +
            "        \"name\": \"Potentially severe\"\n" +
            "      },\n" +
            "      \"recommendation\": \"Withold Warfarin for next 2 days as INR >6. Repeat INR and reassess dose based on tomorrow's result.\"\n" +
            "    },\n" +
            "    \"pharmacokineticIssue\": null,\n" +
            "    \"patientRelatedProblem\": null\n" +
            "  }\n" +
            "]\n";
    JsonUtil.fromJson(json, PharmacistReviewDto[].class, MedsJsonDeserializer.INSTANCE.getTypeAdapters());
  }
}
