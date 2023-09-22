package io.phasetwo.keycloak.events;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

/** */
public abstract class AbstractEventListenerProvider implements EventListenerProvider {

  @Override
  public abstract void onEvent(Event event);

  @Override
  public abstract void onEvent(AdminEvent adminEvent, boolean b);

  @Override
  public void close() {}
}