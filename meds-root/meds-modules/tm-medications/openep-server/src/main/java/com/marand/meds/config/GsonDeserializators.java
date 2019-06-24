package com.marand.meds.config;

import com.google.gson.JsonDeserializer;
import com.marand.maf.core.JsonUtil.TypeAdapterPair;
import com.marand.maf.core.time.Intervals;
import com.marand.thinkmed.medications.dto.change.TherapyChangeDto;
import com.marand.thinkmed.medications.dto.change.TherapyChangeType;
import com.marand.thinkmed.medications.rule.MedicationParacetamolRuleType;
import com.marand.thinkmed.medications.rule.MedicationRuleEnum;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForAdministrationParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapiesParameters;
import com.marand.thinkmed.medications.rule.parameters.ParacetamolRuleForTherapyParameters;
import com.marand.thinkmed.medications.rule.parameters.RuleParameters;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * @author Boris Marn
 */

public class GsonDeserializators
{
  public static final TypeAdapterPair INTERVAL_DESERIALIZER = new TypeAdapterPair(
      Interval.class, (JsonDeserializer<Interval>)(json, typeOfT, context) ->
  {
    final Long startMillis = context.deserialize(json.getAsJsonObject().get("startMillis"), Long.class);
    final Long endMillis = context.deserialize(json.getAsJsonObject().get("endMillis"), Long.class);
    if (startMillis == null && endMillis == null)
    {
      return Intervals.INFINITE;
    }
    if (startMillis == null)
    {
      return Intervals.infiniteTo(new DateTime(endMillis));
    }
    if (endMillis == null)
    {
      return Intervals.infiniteFrom(new DateTime(startMillis));
    }
    return new Interval(startMillis, endMillis);
  });

  public static final TypeAdapterPair THERAPY_CHANGE_DESERIALIZER = new TypeAdapterPair(
      TherapyChangeDto.class, (JsonDeserializer<TherapyChangeDto<?, ?>>)(json, typeOfT, context) ->
  {
    final TherapyChangeType type =
        TherapyChangeType.valueOf(context.deserialize(json.getAsJsonObject().get("type"), String.class));
    return context.deserialize(json, type.getDtoClass());
  });

  public static final TypeAdapterPair RULE_PARAMETERS_DESERIALIZER = new TypeAdapterPair(
      RuleParameters.class, (JsonDeserializer<RuleParameters>)(json, typeOfT, context) ->
  {
    final String medicationRuleEnum
        = context.deserialize(json.getAsJsonObject().get("medicationRuleEnum"), String.class);

    if (MedicationRuleEnum.valueOf(medicationRuleEnum) == MedicationRuleEnum.PARACETAMOL_MAX_DAILY_DOSE)
    {
      final String medicationParacetamolRuleType
          = context.deserialize(json.getAsJsonObject().get("medicationParacetamolRuleType"), String.class);

      if (MedicationParacetamolRuleType.valueOf(medicationParacetamolRuleType) == MedicationParacetamolRuleType.FOR_THERAPY)
      {
        return context.deserialize(json, ParacetamolRuleForTherapyParameters.class);
      }
      else if (MedicationParacetamolRuleType.valueOf(medicationParacetamolRuleType) == MedicationParacetamolRuleType.FOR_THERAPIES)
      {
        return context.deserialize(json, ParacetamolRuleForTherapiesParameters.class);
      }
      else if (MedicationParacetamolRuleType.valueOf(medicationParacetamolRuleType) == MedicationParacetamolRuleType.FOR_ADMINISTRATION)
      {
        return context.deserialize(json, ParacetamolRuleForAdministrationParameters.class);
      }
      else
      {
        return null;
      }
    }
    return null;
  });

}
