package com.marand.thinkmed.medications.warnings.internal;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.marand.ispek.common.Dictionary;
import com.marand.thinkmed.medications.api.internal.dto.TherapyDto;
import com.marand.thinkmed.medications.business.impl.TherapyDisplayProvider;
import com.marand.thinkmed.medications.rule.MedicationParacetamolRuleType;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.rule.impl.ParacetamolRule;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapiesParameters;
import com.marand.thinkmed.medications.rule.result.ParacetamolRuleResult;
import com.marand.thinkmed.medications.service.WarningSeverity;
import com.marand.thinkmed.medications.service.WarningType;
import com.marand.thinkmed.medications.service.dto.MedicationsWarningDto;
import com.marand.thinkmed.medications.valueholder.MedicationsValueHolderProviderImpl;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ParacetamolRuleProvider implements InternalWarningsProvider
{
  private ParacetamolRule paracetamolRule;
  private TherapyDisplayProvider therapyDisplayProvider;
  private MedicationsValueHolderProviderImpl medicationsValueHolderProvider;

  @Autowired
  public void setMedicationsValueHolderProvider(final MedicationsValueHolderProviderImpl medicationsValueHolderProvider)
  {
    this.medicationsValueHolderProvider = medicationsValueHolderProvider;
  }

  @Autowired
  public void setTherapyDisplayProvider(final TherapyDisplayProvider therapyDisplayProvider)
  {
    this.therapyDisplayProvider = therapyDisplayProvider;
  }

  @Autowired
  public void setParacetamolRule(final ParacetamolRule paracetamolRule)
  {
    this.paracetamolRule = paracetamolRule;
  }

  @Override
  public List<MedicationsWarningDto> getWarnings(
      final @NonNull String patientId,
      final @NonNull List<TherapyDto> activeTherapies,
      final @NonNull List<TherapyDto> prospectiveTherapies,
      final @NonNull DateTime when,
      final DateTime dateOfBirth,
      final Double patientWeightInKg,
      final @NonNull Locale locale)
  {
    final boolean prospectiveTherapiesWithParacetamol = prospectiveTherapies.stream()
        .anyMatch(this::containsParacetamol);

    if (!prospectiveTherapiesWithParacetamol)
    {
      return Collections.emptyList();
    }

    final ParacetamolRuleForTherapiesParameters parameters = new ParacetamolRuleForTherapiesParameters();
    parameters.setMedicationRuleEnum(MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);

    if (dateOfBirth == null)
    {
      parameters.setPatientAgeInYears(null);
    }
    else
    {
      parameters.setPatientAgeInYears((long)Years.yearsBetween(
          new DateTime(dateOfBirth),
          new DateTime(when)).getYears());
    }

    parameters.setPatientWeight(patientWeightInKg);
    parameters.setMedicationParacetamolRuleType(MedicationParacetamolRuleType.FOR_THERAPIES);
    parameters.setPatientId(patientId);
    final List<TherapyDto> therapies = Lists.newArrayList();
    therapies.addAll(prospectiveTherapies);
    therapies.addAll(activeTherapies);
    parameters.setTherapies(therapies);

    final ParacetamolRuleResult result = paracetamolRule.applyRule(
        parameters,
        when,
        locale);

    if (result.isQuantityOk())
    {
      return Collections.emptyList();
    }

    final MedicationsWarningDto medicationsWarning = new MedicationsWarningDto();
    medicationsWarning.setExternalType(WarningType.PARACETAMOL.name());
    medicationsWarning.setType(WarningType.PARACETAMOL);

    if (patientWeightInKg != null)
    {
      medicationsWarning.setSeverity(WarningSeverity.HIGH_OVERRIDE);
      medicationsWarning.setMedications(result.getMedications());
      final Double overdosePercentage = extractOverdosePercentage(result);
      final String warningDescription = therapyDisplayProvider.decimalToString(overdosePercentage, locale)
          + Dictionary.getEntry("paracetamol.max.daily.limit.percentage", locale);
      medicationsWarning.setDescription(warningDescription);
    }
    else
    {
      medicationsWarning.setSeverity(WarningSeverity.HIGH);
      medicationsWarning.setDescription(result.getErrorMessage());
    }

    return Lists.newArrayList(medicationsWarning);
  }

  private boolean containsParacetamol(final TherapyDto therapy)
  {
    return therapy.getMedicationIds().stream()
        .anyMatch(this::containsParacetamol);
  }

  private boolean containsParacetamol(final Long medicationId)
  {
    if (medicationId == null)
    {
      return false;
    }

    return medicationsValueHolderProvider.getMedicationData(medicationId).getMedicationIngredients().stream()
        .anyMatch(mi -> mi.getIngredientRule() == MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE);
  }

  Double extractOverdosePercentage(final ParacetamolRuleResult paracetamolRuleResult)
  {
    if (paracetamolRuleResult.getAdultRulePercentage() != null && paracetamolRuleResult.getUnderageRulePercentage() != null)
    {
      return Math.max(paracetamolRuleResult.getAdultRulePercentage(), paracetamolRuleResult.getUnderageRulePercentage());
    }
    if (paracetamolRuleResult.getAdultRulePercentage() != null)
    {
      return paracetamolRuleResult.getAdultRulePercentage();
    }
    return paracetamolRuleResult.getUnderageRulePercentage();
  }
}
