package com.marand.thinkmed.medications;

import com.google.common.base.Preconditions;

/**
 * @author Mitja Lapajne
 */
public enum EhrTerminologyEnum
{
  PK_NANDA("PK-Nanda");

  private final String ehrName;

  EhrTerminologyEnum(final String ehrName)
  {
    this.ehrName = Preconditions.checkNotNull(ehrName);
  }

  public String getEhrName()
  {
    return ehrName;
  }
}
