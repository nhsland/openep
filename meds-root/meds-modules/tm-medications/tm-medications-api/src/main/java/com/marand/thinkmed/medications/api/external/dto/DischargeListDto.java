package com.marand.thinkmed.medications.api.external.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mitja Lapajne
 */
public class DischargeListDto
{
  private List<DischargeListItemDto> items = new ArrayList<>();

  public List<DischargeListItemDto> getItems()
  {
    return items;
  }

  public void setItems(final List<DischargeListItemDto> items)
  {
    this.items = items;
  }
}
