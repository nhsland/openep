package com.marand.thinkmed.medications.connector.data.object.ehr;

/**
 * @author Vid Kumse
 */
public class IdNameCatalogDto extends IdNameDto
{
  private String code;

  public IdNameCatalogDto(final String id, final String name, final String code)
  {
    super(id, name);
    this.code = code;
  }

  public String getCode()
  {
    return code;
  }

  public void setCode(final String code)
  {
    this.code = code;
  }
}
