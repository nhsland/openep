package com.marand.thinkmed.fdb.dto;

/**
 * @author Vid Kumše
 */
public enum FdbConceptTypeEnum
{
  DRUG("Drug"),
  PRODUCT("Product"),
  SUBSTANCE("Substance"),
  PACK("Pack"),
  ORDERABLEMED("OrderableMed");

  private String name;

  FdbConceptTypeEnum(final String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }
}
