package com.marand.thinkmed.medications.dose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.api.internal.dto.dose.PrescribingDoseDto;
import com.marand.thinkmed.medications.units.converter.UnitsConverter;
import com.marand.thinkmed.medications.units.converter.impl.UnitsConverterImpl;
import com.marand.thinkmed.medications.units.provider.TestUnitsProviderImpl;
import com.marand.thinkmed.medications.units.provider.UnitsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("NumericCastThatLosesPrecision")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {DoseUtilsTest.DoseUtilsConfTest.class})
public class DoseUtilsTest
{
  @Configuration
  @ComponentScan(basePackageClasses = UnitsConverterImpl.class)
  public static class DoseUtilsConfTest
  {
    @Autowired
    private UnitsConverter unitsConverter;

    @Bean
    DoseUtils doseUtils()
    {
      return new DoseUtils(unitsConverter);
    }

    @Bean
    UnitsProvider unitsProvider()
    {
      return new TestUnitsProviderImpl();
    }
  }

  @Autowired
  private DoseUtils doseUtils;

  @Test
  public void buildPrescribingDoseWithAdministrationUnit()
  {
    final MedicationDataDto medication = new MedicationDataDto();
    medication.setAdministrationUnit("spoon");
    final PrescribingDoseDto doseDto = doseUtils.buildPrescribingDose(medication);
    assertNull(doseDto);
  }

  @Test(expected = IllegalStateException.class)
  public void buildPrescribingDoseNoIngredientsNoAdministrationUnit()
  {
    final MedicationDataDto medication = new MedicationDataDto();
    doseUtils.buildPrescribingDose(medication);
  }

  @Test
  public void buildPrescribingDoseOneMainIngredient()
  {
    final MedicationDataDto medication = new MedicationDataDto();

    final MedicationIngredientDto ingredient1 = new MedicationIngredientDto();
    ingredient1.setMain(true);
    ingredient1.setStrengthNumerator(10.0);
    ingredient1.setStrengthNumeratorUnit("mg");
    medication.getMedicationIngredients().add(ingredient1);

    final PrescribingDoseDto doseDto = doseUtils.buildPrescribingDose(medication);
    assertNull(doseDto.getDenominatorUnit());
    assertEquals((int)doseDto.getNumerator(), ingredient1.getStrengthNumerator().intValue());
    assertEquals(doseDto.getNumeratorUnit(), ingredient1.getStrengthNumeratorUnit());
  }

  @Test
  public void buildPrescribingDoseOneIngredientNoMain()
  {
    final MedicationDataDto medication = new MedicationDataDto();

    final MedicationIngredientDto ingredient1 = new MedicationIngredientDto();
    ingredient1.setStrengthNumerator(10.0);
    ingredient1.setStrengthNumeratorUnit("mg");
    medication.getMedicationIngredients().add(ingredient1);

    final PrescribingDoseDto doseDto = doseUtils.buildPrescribingDose(medication);
    assertNull(doseDto.getDenominatorUnit());
    assertEquals((int)doseDto.getNumerator(), ingredient1.getStrengthNumerator().intValue());
    assertEquals(doseDto.getNumeratorUnit(), ingredient1.getStrengthNumeratorUnit());
  }

  @Test
  public void buildPrescribingDoseMultipleIngredientsNoMain()
  {
    final MedicationDataDto medication = new MedicationDataDto();

    final MedicationIngredientDto ingredient1 = new MedicationIngredientDto();
    ingredient1.setStrengthNumerator(1000.0);
    ingredient1.setStrengthNumeratorUnit("mg");
    ingredient1.setStrengthDenominator(1.0);
    ingredient1.setStrengthDenominatorUnit("ml");
    medication.getMedicationIngredients().add(ingredient1);

    final MedicationIngredientDto ingredient2 = new MedicationIngredientDto();
    ingredient2.setStrengthNumerator(1.0);
    ingredient2.setStrengthNumeratorUnit("g");
    ingredient2.setStrengthDenominator(1.0);
    ingredient2.setStrengthDenominatorUnit("ml");
    medication.getMedicationIngredients().add(ingredient2);

    final MedicationIngredientDto ingredient3 = new MedicationIngredientDto();
    ingredient3.setStrengthNumerator(1.0);
    ingredient3.setStrengthNumeratorUnit("g");
    ingredient3.setStrengthDenominator(1.0);
    ingredient3.setStrengthDenominatorUnit("ml");
    medication.getMedicationIngredients().add(ingredient3);

    final PrescribingDoseDto doseDto = doseUtils.buildPrescribingDose(medication);

    assertEquals(3000, (int)doseDto.getNumerator());
    assertEquals(doseDto.getNumeratorUnit(), ingredient1.getStrengthNumeratorUnit());

    assertEquals(doseDto.getDenominatorUnit(), ingredient2.getStrengthDenominatorUnit());
    assertEquals(doseDto.getDenominator(), ingredient2.getStrengthDenominator());
  }

  @Test
  public void buildPrescribingDoseMultipleIngredientsOneMain()
  {
    final MedicationDataDto medication = new MedicationDataDto();

    final MedicationIngredientDto ingredient1 = new MedicationIngredientDto();
    ingredient1.setStrengthNumerator(1000.0);
    ingredient1.setStrengthNumeratorUnit("mg");
    ingredient1.setStrengthDenominator(1.0);
    ingredient1.setStrengthDenominatorUnit("ml");
    medication.getMedicationIngredients().add(ingredient1);

    final MedicationIngredientDto ingredient2 = new MedicationIngredientDto();
    ingredient2.setStrengthNumerator(1.0);
    ingredient2.setStrengthNumeratorUnit("g");
    ingredient2.setStrengthDenominator(1.0);
    ingredient2.setStrengthDenominatorUnit("ml");
    medication.getMedicationIngredients().add(ingredient2);

    final MedicationIngredientDto ingredient3 = new MedicationIngredientDto();
    ingredient3.setStrengthNumerator(1.0);
    ingredient3.setStrengthNumeratorUnit("g");
    ingredient3.setStrengthDenominator(1.0);
    ingredient3.setStrengthDenominatorUnit("ml");
    ingredient3.setMain(true);
    medication.getMedicationIngredients().add(ingredient3);

    final PrescribingDoseDto doseDto = doseUtils.buildPrescribingDose(medication);

    assertEquals(1, (int)doseDto.getNumerator());
    assertEquals(doseDto.getNumeratorUnit(), ingredient3.getStrengthNumeratorUnit());

    assertEquals(doseDto.getDenominatorUnit(), ingredient3.getStrengthDenominatorUnit());
    assertEquals(doseDto.getDenominator(), ingredient3.getStrengthDenominator());
  }

  @Test
  public void buildPrescribingDoseMultipleIngredientsTwoMain()
  {
    final MedicationDataDto medication = new MedicationDataDto();

    final MedicationIngredientDto ingredient1 = new MedicationIngredientDto();
    ingredient1.setStrengthNumerator(1000.0);
    ingredient1.setStrengthNumeratorUnit("mg");
    ingredient1.setStrengthDenominator(1.0);
    ingredient1.setStrengthDenominatorUnit("ml");
    ingredient1.setMain(true);
    medication.getMedicationIngredients().add(ingredient1);

    final MedicationIngredientDto ingredient2 = new MedicationIngredientDto();
    ingredient2.setStrengthNumerator(1.0);
    ingredient2.setStrengthNumeratorUnit("g");
    ingredient2.setStrengthDenominator(1.0);
    ingredient2.setStrengthDenominatorUnit("ml");
    medication.getMedicationIngredients().add(ingredient2);

    final MedicationIngredientDto ingredient3 = new MedicationIngredientDto();
    ingredient3.setStrengthNumerator(1.0);
    ingredient3.setStrengthNumeratorUnit("g");
    ingredient3.setStrengthDenominator(1.0);
    ingredient3.setStrengthDenominatorUnit("ml");
    ingredient3.setMain(true);
    medication.getMedicationIngredients().add(ingredient3);

    final PrescribingDoseDto doseDto = doseUtils.buildPrescribingDose(medication);

    assertEquals(2000, (int)doseDto.getNumerator());
    assertEquals(doseDto.getNumeratorUnit(), ingredient1.getStrengthNumeratorUnit());

    assertEquals(doseDto.getDenominatorUnit(), ingredient1.getStrengthDenominatorUnit());
    assertEquals(doseDto.getDenominator(), ingredient1.getStrengthDenominator());
  }

  @Test
  @SuppressWarnings("TooBroadScope")
  public void buildDoseForIngredients()
  {
    final List<MedicationIngredientDto> ingredients = new ArrayList<>();

    final MedicationIngredientDto ingredient1 = new MedicationIngredientDto();
    ingredient1.setStrengthNumerator(1000.0);
    ingredient1.setStrengthNumeratorUnit("mg");
    ingredient1.setStrengthDenominator(1.0);
    ingredient1.setStrengthDenominatorUnit("ml");
    ingredients.add(ingredient1);

    final MedicationIngredientDto ingredient2 = new MedicationIngredientDto();
    ingredient2.setStrengthNumerator(1.0);
    ingredient2.setStrengthNumeratorUnit("g");
    ingredient2.setStrengthDenominator(1.0);
    ingredient2.setStrengthDenominatorUnit("ml");
    ingredients.add(ingredient2);

    final MedicationIngredientDto ingredient3 = new MedicationIngredientDto();
    ingredient3.setStrengthNumerator(1.0);
    ingredient3.setStrengthNumeratorUnit("g");
    ingredient3.setStrengthDenominator(1.0);
    ingredient3.setStrengthDenominatorUnit("ml");
    ingredients.add(ingredient3);

    final PrescribingDoseDto doseDto = doseUtils.buildDoseFromIngredients(ingredients);

    assertEquals(3000, (int)doseDto.getNumerator());
    assertEquals(doseDto.getNumeratorUnit(), ingredient1.getStrengthNumeratorUnit());

    assertEquals(doseDto.getDenominatorUnit(), ingredient1.getStrengthDenominatorUnit());
    assertEquals(doseDto.getDenominator(), ingredient1.getStrengthDenominator());
  }

  @Test
  @SuppressWarnings("TooBroadScope")
  public void buildDoseForIngredientsEmptyList()
  {
    final PrescribingDoseDto doseDto = doseUtils.buildDoseFromIngredients(Collections.emptyList());
    assertNull(doseDto);
  }
}