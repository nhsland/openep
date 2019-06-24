package com.marand.thinkmed.medications.rule.result;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Nejc Korasa
 */

public interface RuleResult extends JsonSerializable
{
  String getRule();

  void setRule(final String rule);

  String getErrorMessage();

  void setErrorMessage(final String errorMessage);
}
