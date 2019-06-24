package com.marand.thinkmed.medications.connector.data.object;

import com.marand.thinkmed.api.core.JsonSerializable;
import com.marand.thinkmed.medications.connector.data.object.ehr.IdNameDto;

/**
 * @author Mitja Lapajne
 */

public class DiseaseDto extends IdNameDto implements JsonSerializable
{
  private String comment;

  public DiseaseDto(final String id, final String name)
  {
    super(id, name);
  }

  public DiseaseDto(final String name)
  {
    super(name);
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(final String comment)
  {
    this.comment = comment;
  }
}
