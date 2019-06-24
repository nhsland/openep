package com.marand.thinkmed.medications.api.external.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mitja Lapajne
 */
public class RoutesDto
{
  private String display;
  private List<RouteDto> items = new ArrayList<>();

  public String getDisplay()
  {
    return display;
  }

  public void setDisplay(final String display)
  {
    this.display = display;
  }

  public List<RouteDto> getItems()
  {
    return items;
  }

  public void setItems(final List<RouteDto> items)
  {
    this.items = items;
  }
}
