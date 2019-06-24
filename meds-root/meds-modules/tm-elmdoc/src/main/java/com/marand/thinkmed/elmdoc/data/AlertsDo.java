package com.marand.thinkmed.elmdoc.data;

import java.util.ArrayList;
import java.util.List;

import com.marand.thinkmed.api.core.JsonSerializable;

/**
 * @author Mitja Lapajne
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "unused"})
public class AlertsDo implements JsonSerializable
{
  private List<AlertDo> Items = new ArrayList<>();

  public AlertsDo(final List<AlertDo> items)
  {
    Items = items;
  }

  public List<AlertDo> getItems()
  {
    return Items;
  }
}
