package com.marand.thinkmed.medications.rule.impl;

import java.util.Collections;
import java.util.Locale;

import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.maf.core.server.util.DefinedLocaleHolder;
import com.marand.thinkmed.medications.api.internal.dto.ConstantComplexTherapyDto;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.rule.MedicationParacetamolRuleType;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapiesParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapyParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleParameters;
import com.marand.thinkmed.medications.rule.result.ParacetamolRuleResult;
import com.marand.thinkmed.medications.units.converter.UnitsConverter;
import com.marand.thinkmed.medications.units.provider.UnitsProvider;
import com.marand.thinkmed.request.user.RequestUser;
import com.marand.thinkmed.request.user.UserDto;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Nejc Korasa
 */

@SuppressWarnings("ALL")
@RunWith(SpringJUnit4ClassRunner.class)
public class ParacetamolRuleTest
{
  @InjectMocks
  private ParacetamolRule paracetamolRule = new ParacetamolRule();

  @Mock
  private UnitsConverter unitsConverter;

  @Mock
  private UnitsProvider unitsProvider;

  @BeforeClass
  public static void initRequest()
  {
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));
    DefinedLocaleHolder.INSTANCE.setLocale(new Locale("en"));

    RequestUser.init(auth -> new UserDto("Test", null,"Test", Collections.emptyList()));
  }

  @Test(expected = NullPointerException.class)
  public void testApplyRuleForTherapyNoNullParameters() throws Exception
  {
    final ParacetamolRuleForTherapyParameters parameters = new ParacetamolRuleForTherapyParameters();
    parameters.setMedicationParacetamolRuleType(MedicationParacetamolRuleType.FOR_THERAPY);
    parameters.setMedicationDataDtoList(Collections.singletonList(new MedicationDataDto()));

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.applyRule(
        parameters,
        new DateTime(),
        new Locale("si Sl"));
  }

  @Test(expected = NullPointerException.class)
  public void testApplyRuleForTherapyNoNullParameters3() throws Exception
  {
    final ParacetamolRuleForTherapyParameters parameters = new ParacetamolRuleForTherapyParameters();
    parameters.setMedicationParacetamolRuleType(MedicationParacetamolRuleType.FOR_THERAPY);
    parameters.setMedicationDataDtoList(Collections.emptyList());

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.applyRule(
        parameters,
        new DateTime(),
        new Locale("si Sl"));
  }

  @Test(expected = NullPointerException.class)
  public void testApplyRuleForTherapiesNoNullParameters() throws Exception
  {
    final ParacetamolRuleForTherapiesParameters parameters = new ParacetamolRuleForTherapiesParameters();
    parameters.setMedicationParacetamolRuleType(MedicationParacetamolRuleType.FOR_THERAPIES);
    parameters.setTherapies(Collections.emptyList());

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.applyRule(
        parameters,
        new DateTime(),
        new Locale("si Sl"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testApplyRuleWithUnknownRuleType() throws Exception
  {
    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.applyRule(
        new ParacetamolRuleParameters(),
        new DateTime(),
        new Locale("si Sl"));
  }

  @Test
  public void testApplyRuleWithNoPatientWeight() throws Exception
  {
    final Locale locale = new Locale("en");
    final ParacetamolRuleForTherapyParameters parameters = new ParacetamolRuleForTherapyParameters();
    parameters.setMedicationParacetamolRuleType(MedicationParacetamolRuleType.FOR_THERAPY);
    parameters.setMedicationDataDtoList(Collections.singletonList(new MedicationDataDto()));
    parameters.setTherapyDto(new ConstantComplexTherapyDto());

    final String errorMessage = "Patient weight is not known, could not apply paracetamol rule.";

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.applyRule(parameters, new DateTime(), locale);

    assertTrue(!paracetamolRuleResult.isQuantityOk());
    assertEquals(errorMessage, paracetamolRuleResult.getErrorMessage());
  }

  @Test
  public void testCalculateParacetamolRuleResultForAdultPatient()
  {
    final long patientAgeInYears = 20L;
    final double patientWeight = 20.0;
    final double patientHeight = 150.0;
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);
    final Locale locale = new Locale("en_GB");

    final double dailyDosage = 2000.0;

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.calculateRuleResult(
        dailyDosage,
        patientWeight,
        patientAgeInYears,
        Collections.emptyList(),
        locale);

    assertNotNull(paracetamolRuleResult);
    assertTrue(paracetamolRuleResult.isQuantityOk());
  }

  @Test
  public void testCalculateParacetamolRuleResultUnderagedPatient()
  {
    final long patientAgeInYears = 2L;
    final double patientWeight = 20.0;
    final double patientHeight = 150.0;
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);
    final Locale locale = new Locale("en_GB");

    final double dailyDosage = 2000.0;

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.calculateRuleResult(
        dailyDosage,
        patientWeight,
        patientAgeInYears,
        Collections.emptyList(),
        locale);

    // 60mg/kg/day --> 60*20 = 1200mg
    assertNotNull(paracetamolRuleResult);
    assertTrue(paracetamolRuleResult.getRule().startsWith("60mg/"));
    assertEquals(167, paracetamolRuleResult.getUnderageRulePercentage().intValue());
    assertTrue(!paracetamolRuleResult.isQuantityOk());
  }

  @Test
  public void testCalculateParacetamolRuleResultUnderagedPatient2()
  {
    final long patientAgeInYears = 2L;
    final double patientWeight = 10.0;
    final double patientHeight = 150.0;
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);
    final Locale locale = new Locale("en_GB");

    final double dailyDosage = 2000.0;

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.calculateRuleResult(
        dailyDosage,
        patientWeight,
        patientAgeInYears,
        Collections.emptyList(),
        locale);

    // 60mg/kg/day --> 60*10 = 600mg
    assertNotNull(paracetamolRuleResult);
    assertTrue(paracetamolRuleResult.getRule().startsWith("60mg/"));
    // Note that percentage is calculated using Math.ceil() function, therefore result(2000/600 * 100) is not 333.
    assertEquals(334, paracetamolRuleResult.getUnderageRulePercentage().intValue());
    assertTrue(!paracetamolRuleResult.isQuantityOk());
  }

  @Test
  public void testCalculateParacetamolRuleResult100Percente()
  {
    final long patientAgeInYears = 20L;
    final double patientWeight = 60.0;
    final double patientHeight = 150.0;
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);
    final Locale locale = new Locale("en_GB");

    final double dailyDosage = 4000.0;

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.calculateRuleResult(
        dailyDosage,
        patientWeight,
        patientAgeInYears,
        Collections.emptyList(),
        locale);

    assertNotNull(paracetamolRuleResult);
    assertEquals(100, paracetamolRuleResult.getAdultRulePercentage().intValue());
    assertTrue(paracetamolRuleResult.isQuantityOk());
  }

  @Test
  public void testCalculateParacetamolRuleResultNear100Percente()
  {
    final long patientAgeInYears = 20L;
    final double patientWeight = 60.0;
    final double patientHeight = 150.0;
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);
    final Locale locale = new Locale("en_GB");

    final double dailyDosage = 4001.0;

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.calculateRuleResult(
        dailyDosage,
        patientWeight,
        patientAgeInYears,
        Collections.emptyList(),
        locale);

    assertNotNull(paracetamolRuleResult);
    assertEquals(101, paracetamolRuleResult.getAdultRulePercentage().intValue());
    assertTrue(!paracetamolRuleResult.isQuantityOk());
  }

  @Test
  public void testCalculateParacetamolRuleResultUnderagedPatient3()
  {
    final long patientAgeInYears = 2L;
    final double patientWeight = 10.0;
    final double patientHeight = 150.0;
    final DateTime actionTimeStamp = new DateTime(2016, 1, 20, 12, 0, 0);
    final Locale locale = new Locale("en_GB");

    final double dailyDosage = 600.0;

    final ParacetamolRuleResult paracetamolRuleResult = paracetamolRule.calculateRuleResult(
        dailyDosage,
        patientWeight,
        patientAgeInYears,
        Collections.emptyList(),
        locale);

    // 60mg/kg/day --> 60*10 = 600mg
    assertNotNull(paracetamolRuleResult);
    assertTrue(paracetamolRuleResult.isQuantityOk());
  }
}