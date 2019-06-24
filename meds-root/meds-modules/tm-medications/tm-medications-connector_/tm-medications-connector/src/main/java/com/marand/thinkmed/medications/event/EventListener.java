package com.marand.thinkmed.medications.event;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

/**
 * @author Bostjan Vester
 */
public abstract class EventListener
{
  private final List<EventHandler> eventHandlers = new ArrayList<>();

  protected final void handle(final @NonNull Event event)
  {
    for (final EventHandler eventHandler : eventHandlers)
    {
      event.handleWith(eventHandler);
    }
  }
}
