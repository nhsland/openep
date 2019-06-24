package com.marand.thinkmed.elmdoc.data;

import com.marand.thinkmed.api.core.JsonSerializable;
import org.joda.time.LocalDate;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings("InstanceVariableNamingConvention")
public class PatientDo implements JsonSerializable
{
  private LocalDate BirthDate;
  private GenderEnum Gender;
  private Double Weight;
  private Double BodySurfaceArea;

  public LocalDate getBirthDate()
  {
    return BirthDate;
  }

  public void setBirthDate(final LocalDate birthDate)
  {
    BirthDate = birthDate;
  }

  public GenderEnum getGender()
  {
    return Gender;
  }

  public void setGender(final GenderEnum gender)
  {
    Gender = gender;
  }

  public Double getWeight()
  {
    return Weight;
  }

  public void setWeight(final Double weight)
  {
    Weight = weight;
  }

  public Double getBodySurfaceArea()
  {
    return BodySurfaceArea;
  }

  public void setBodySurfaceArea(final Double bodySurfaceArea)
  {
    BodySurfaceArea = bodySurfaceArea;
  }
}
