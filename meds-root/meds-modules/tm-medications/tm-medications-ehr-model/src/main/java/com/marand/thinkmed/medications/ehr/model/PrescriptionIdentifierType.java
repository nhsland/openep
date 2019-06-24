package com.marand.thinkmed.medications.ehr.model;

/**
 * @author Nejc Korasa
 */

public enum PrescriptionIdentifierType
{
  RECONCILIATION("Reconciliation");

  private final String ehrName;

  PrescriptionIdentifierType(final String ehrName)
  {
    this.ehrName = ehrName;
  }

  public String getEhrName()
  {
    return ehrName;
  }

  public Identifier buildIdentifier(final String id)
  {
    final Identifier identifier = new Identifier();
    identifier.setType(ehrName);
    identifier.setId(id);

    return identifier;
  }
}
