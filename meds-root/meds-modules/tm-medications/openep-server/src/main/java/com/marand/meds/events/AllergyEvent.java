package com.marand.meds.events;

import java.util.Map;

/**
 * @author Nejc Korasa
 */

public class AllergyEvent
{
  private String ehrUid;
  private String operationType;
  private Object[] oldResults;
  private Object[] newResults;

  public String getEhrUid()
  {
    return ehrUid;
  }

  public void setEhrUid(final String ehrUid)
  {
    this.ehrUid = ehrUid;
  }

  public String getOperationType()
  {
    return operationType;
  }

  public void setOperationType(final String operationType)
  {
    this.operationType = operationType;
  }

  public void setOldResults(final Object[] oldResults)
  {
    this.oldResults = oldResults;
  }

  public void setNewResults(final Object[] newResults)
  {
    this.newResults = newResults;
  }

  public String getOldCompositionUId()
  {
    if (oldResults != null && oldResults.length > 0)
    {
      //noinspection unchecked
      final Map<String, String> map = (Map<String, String>)oldResults[0];
      return map.get("#0");
    }
    return null;
  }

  public String getNewCompositionUId()
  {
    if (newResults != null && newResults.length > 0)
    {
      //noinspection unchecked
      final Map<String, String> map = (Map<String, String>)newResults[0];
      return map.get("#0");
    }
    return null;
  }
}