package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "BooleanMethodNameMustStartWithQuestion"})
public class FdbPatientDto implements JsonSerializable
{
  private Long Gender;
  private Long Age;
  private Boolean ConditionListComplete;

  public Long getGender()
  {
    return Gender;
  }

  public void setGender(final Long gender)
  {
    Gender = gender;
  }

  public Long getAge()
  {
    return Age;
  }

  public void setAge(final Long age)
  {
    Age = age;
  }

  public Boolean getConditionListComplete()
  {
    return ConditionListComplete;
  }

  public void setConditionListComplete(final Boolean conditionListComplete)
  {
    ConditionListComplete = conditionListComplete;
  }
}
