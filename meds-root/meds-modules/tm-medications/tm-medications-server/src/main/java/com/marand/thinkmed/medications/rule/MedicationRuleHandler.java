package com.marand.thinkmed.medications.rule;

import java.util.Locale;
import lombok.NonNull;

import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import com.marand.thinkmed.medications.rule.result.RuleResult;
import org.joda.time.DateTime;

/**
 * @author Nejc Korasa
 */
public interface MedicationRuleHandler
{
  RuleResult applyMedicationRule(
      @NonNull RuleParameters ruleParameters,
      @NonNull DateTime actionTimestamp,
      @NonNull Locale locale);
}
