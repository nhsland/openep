package com.marand.thinkmed.medications.rule.impl;

import java.util.Locale;
import java.util.Map;

import com.marand.thinkmed.medications.rule.MedicationRule;
import com.marand.thinkmed.medications.rule.MedicationRuleHandler;
import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Nejc Korasa
 */
@Component
public class MedicationRuleHandlerImpl implements MedicationRuleHandler
{
  private Map<String, MedicationRule> medicationRules;

  @Autowired
  public void setMedicationRules(final Map<String, MedicationRule> medicationRules)
  {
    this.medicationRules = medicationRules;
  }

  @Override
  public RuleResult applyMedicationRule(
      final @NonNull RuleParameters ruleParameters,
      final @NonNull DateTime actionTimestamp,
      final @NonNull Locale locale)
  {
    final MedicationRule medicationRule = medicationRules.get(ruleParameters.getMedicationRuleEnum().name());

    if (medicationRule != null)
    {
      return medicationRule.applyRule(ruleParameters, actionTimestamp, locale);
    }

    return null;
  }
}
