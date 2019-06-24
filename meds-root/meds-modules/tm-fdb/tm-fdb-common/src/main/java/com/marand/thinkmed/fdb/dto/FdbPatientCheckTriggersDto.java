package com.marand.thinkmed.fdb.dto;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "BooleanMethodNameMustStartWithQuestion"})
public class FdbPatientCheckTriggersDto implements JsonSerializable
{
  private FdbNameValue Gender;
  private Long Age;

  public FdbNameValue getGender()
  {
    return Gender;
  }

  public void setGender(final FdbNameValue gender)
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
}
