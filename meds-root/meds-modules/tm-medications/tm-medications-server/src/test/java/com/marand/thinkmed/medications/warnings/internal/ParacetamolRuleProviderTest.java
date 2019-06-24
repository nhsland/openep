package com.marand.thinkmed.medications.warnings.internal;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.marand.ispek.common.Dictionary;
import com.marand.maf.core.dictionary.MafDictionary;
import com.marand.thinkmed.api.externals.data.object.NamedExternalDto;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.dto.MedicationDataDto;
import com.marand.thinkmed.medications.dto.MedicationIngredientDto;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.rule.impl.ParacetamolRule;
import com.marand.thinkmed.medications.rule.result.ParacetamolRuleResult;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.therapy.util.TherapyBuilderUtils;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProviderImpl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.*;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;

@RunWith(SpringJUnit4ClassRunner.class)
public class ParacetamolRuleProviderTest
{
  @InjectMocks
  private ParacetamolRuleProvider paracetamolRuleProvider;

  @Mock
  private TherapyDisplayProvider therapyDisplayProvider;

  @Mock
  private ParacetamolRule paracetamolRule;

  @Mock
  private MedicationsValueHolderProviderImpl medicationsValueHolderProvider;

  @Before
  public void setup()
  {
    Dictionary.setDelegate(new MafDictionary("Dictionary", "", new Locale("en")));
  }

  @Test
  public void getWarningsForChildTest()
  {
    final List<TherapyDto> activeTherapies = Lists.newArrayList();
    final List<TherapyDto> prospectiveTherapies = Lists.newArrayList();

    final TherapyDto aspirin = TherapyBuilderUtils.createMinimalSimpleTherapyDto("aspirin");
    final TherapyDto paracetamol1 = TherapyBuilderUtils.createMinimalSimpleTherapyDto("paracetamol1");
    final TherapyDto dopamine = TherapyBuilderUtils.createMinimalConstantComplexTherapy("dopamine");
    final TherapyDto paracetamol2 = TherapyBuilderUtils.createMinimalSimpleTherapyDto("paracetamol2");

    activeTherapies.add(aspirin);
    activeTherapies.add(paracetamol1);
    prospectiveTherapies.add(dopamine);
    prospectiveTherapies.add(paracetamol2);

    final ParacetamolRuleResult paracetamolRuleResult = getParacetamolRuleResult(
        false,
        "underage overdose",
        230.0,
        140.0);

    final NamedExternalDto paracetamolVTM = new NamedExternalDto("1", "paracetamolVTM");
    final NamedExternalDto paracetamolVMP = new NamedExternalDto("2", "paracetamolVMP");
    final List<NamedExternalDto> medications = Lists.newArrayList();
    medications.add(paracetamolVTM);
    medications.add(paracetamolVMP);
    paracetamolRuleResult.setMedications(medications);

    Mockito
        .when(paracetamolRule.applyRule(anyObject(), anyObject(), anyObject()))
        .thenReturn(paracetamolRuleResult);

    Mockito
        .when(therapyDisplayProvider.decimalToString(anyDouble(), anyObject()))
        .thenReturn("230");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    final MedicationIngredientDto medicationIngredientDto = new MedicationIngredientDto();
    medicationIngredientDto.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    medicationDataDto.getMedicationIngredients().add(medicationIngredientDto);

    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(anyLong()))
        .thenReturn(medicationDataDto);

    final Locale locale = new Locale("en");
    final List<MedicationsWarningDto> result = paracetamolRuleProvider.getWarnings(
        "123",
        activeTherapies,
        prospectiveTherapies,
        new DateTime(2019, 3, 15, 13, 39),
        new DateTime(20110, 2, 3, 4, 3, 4),
        67.0,
        locale);

    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getDescription()).isEqualTo("230% of max Paracetamol daily limit.");
    assertThat(result.get(0).getType()).isEqualTo(WarningType.PARACETAMOL);
    assertThat(result.get(0).getSeverity()).isEqualTo(WarningSeverity.HIGH_OVERRIDE);
    assertThat(result.get(0).getMedications().get(0).getName()).isEqualTo("paracetamolVTM");
    assertThat(result.get(0).getMedications().get(0).getId()).isEqualTo("1");
    assertThat(result.get(0).getMedications().get(1).getName()).isEqualTo("paracetamolVMP");
    assertThat(result.get(0).getMedications().get(1).getId()).isEqualTo("2");
    assertThat(result.get(0).getMedications().size()).isEqualTo(2);
  }

  @Test
  public void getWarningsIsOkTest()
  {
    final List<TherapyDto> activeTherapies = Lists.newArrayList();
    final List<TherapyDto> prospectiveTherapies = Lists.newArrayList();

    final TherapyDto aspirin = TherapyBuilderUtils.createMinimalSimpleTherapyDto("aspirin");
    final TherapyDto paracetamol1 = TherapyBuilderUtils.createMinimalSimpleTherapyDto("paracetamol1");
    final TherapyDto dopamine = TherapyBuilderUtils.createMinimalConstantComplexTherapy("dopamine");
    final TherapyDto paracetamol2 = TherapyBuilderUtils.createMinimalSimpleTherapyDto("paracetamol2");

    activeTherapies.add(aspirin);
    activeTherapies.add(paracetamol1);
    prospectiveTherapies.add(dopamine);
    prospectiveTherapies.add(paracetamol2);

    final ParacetamolRuleResult paracetamolRuleResult = getParacetamolRuleResult(
        true,
        null,
        null,
        null);

    Mockito
        .when(paracetamolRule.applyRule(anyObject(), anyObject(), anyObject()))
        .thenReturn(paracetamolRuleResult);

    Mockito
        .when(therapyDisplayProvider.decimalToString(anyDouble(), anyObject()))
        .thenReturn("55");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    final MedicationIngredientDto medicationIngredientDto = new MedicationIngredientDto();
    medicationIngredientDto.setIngredientRule(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
    medicationDataDto.getMedicationIngredients().add(medicationIngredientDto);

    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(anyLong()))
        .thenReturn(medicationDataDto);

    final Locale locale = new Locale("en");
    final List<MedicationsWarningDto> result = paracetamolRuleProvider.getWarnings(
        "123",
        activeTherapies,
        prospectiveTherapies,
        new DateTime(2019, 3, 15, 13, 39),
        new DateTime(20110, 2, 3, 4, 3, 4),
        67.0,
        locale);

    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void getWarningsEmptyListsTest()
  {
    final List<TherapyDto> activeTherapies = Lists.newArrayList();
    final List<TherapyDto> prospectiveTherapies = Lists.newArrayList();

    final ParacetamolRuleResult paracetamolRuleResult = getParacetamolRuleResult(
        true,
        null,
        null,
        null);

    Mockito
        .when(paracetamolRule.applyRule(anyObject(), anyObject(), anyObject()))
        .thenReturn(paracetamolRuleResult);

    Mockito
        .when(therapyDisplayProvider.decimalToString(anyDouble(), anyObject()))
        .thenReturn("55");

    final Locale locale = new Locale("en");
    final List<MedicationsWarningDto> result = paracetamolRuleProvider.getWarnings(
        "123",
        activeTherapies,
        prospectiveTherapies,
        new DateTime(2019, 3, 15, 13, 39),
        new DateTime(20110, 2, 3, 4, 3, 4),
        67.0,
        locale);

    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void getWarningsWithNoActiveParacetamol()
  {
    final List<TherapyDto> activeTherapies = Lists.newArrayList();
    final List<TherapyDto> prospectiveTherapies = Lists.newArrayList();

    final TherapyDto aspirin = TherapyBuilderUtils.createMinimalSimpleTherapyDto("aspirin");
    final TherapyDto dopamine = TherapyBuilderUtils.createMinimalConstantComplexTherapy("dopamine");
    final TherapyDto paracetamol2 = TherapyBuilderUtils.createMinimalSimpleTherapyDto("paracetamol2");

    activeTherapies.add(aspirin);
    prospectiveTherapies.add(dopamine);
    prospectiveTherapies.add(paracetamol2);

    final ParacetamolRuleResult paracetamolRuleResult = getParacetamolRuleResult(
        false,
        "underage overdose",
        230.0,
        140.0);

    final NamedExternalDto paracetamolVTM = new NamedExternalDto("1", "paracetamolVTM");
    final NamedExternalDto paracetamolVMP = new NamedExternalDto("2", "paracetamolVMP");
    final List<NamedExternalDto> medications = Lists.newArrayList();
    medications.add(paracetamolVTM);
    medications.add(paracetamolVMP);
    paracetamolRuleResult.setMedications(medications);

    Mockito
        .when(paracetamolRule.applyRule(anyObject(), anyObject(), anyObject()))
        .thenReturn(paracetamolRuleResult);

    Mockito
        .when(therapyDisplayProvider.decimalToString(anyDouble(), anyObject()))
        .thenReturn("230");

    final MedicationDataDto medicationDataDto = new MedicationDataDto();
    final MedicationIngredientDto medicationIngredientDto = new MedicationIngredientDto();
    medicationDataDto.getMedicationIngredients().add(medicationIngredientDto);

    Mockito
        .when(medicationsValueHolderProvider.getMedicationData(anyLong()))
        .thenReturn(medicationDataDto);

    final Locale locale = new Locale("en");
    final List<MedicationsWarningDto> result = paracetamolRuleProvider.getWarnings(
        "123",
        activeTherapies,
        prospectiveTherapies,
        new DateTime(2019, 3, 15, 13, 39),
        new DateTime(20110, 2, 3, 4, 3, 4),
        67.0,
        locale);

    assertThat(result.size()).isEqualTo(0);
  }

  @Test
  public void extractOverdosePercentageUnderageTest()
  {
    final ParacetamolRuleResult paracetamolRuleResult = getParacetamolRuleResult(
        false,
        "underage",
        230.0,
        140.0);

    final Double result = paracetamolRuleProvider.extractOverdosePercentage(paracetamolRuleResult);

    assertThat(result).isEqualTo(230.0);
  }

  @Test
  public void extractOverdosePercentageAdultTest()
  {
    final ParacetamolRuleResult paracetamolRuleResult = getParacetamolRuleResult(
        false,
        "adult",
        120.0,
        250.0);

    final Double result = paracetamolRuleProvider.extractOverdosePercentage(paracetamolRuleResult);

    assertThat(result).isEqualTo(250);
  }

  private ParacetamolRuleResult getParacetamolRuleResult(
      final boolean isQuantityOk,
      final String rule,
      final Double adultRulePercentage,
      final Double underageRulePercentage)
  {
    final ParacetamolRuleResult paracetamolRuleResult = new ParacetamolRuleResult();
    paracetamolRuleResult.setQuantityOk(isQuantityOk);
    paracetamolRuleResult.setRule(rule);
    paracetamolRuleResult.setAdultRulePercentage(adultRulePercentage);
    paracetamolRuleResult.setUnderageRulePercentage(underageRulePercentage);
    paracetamolRuleResult.setBetweenDosesTimeOk(true);
    return paracetamolRuleResult;
  }
}
