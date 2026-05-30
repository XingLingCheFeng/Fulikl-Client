package missu.epsilon.client.event.events.game;

import missu.epsilon.client.event.impl.Event;

public record EntityKilledEvent(String targetName) implements Event {}
