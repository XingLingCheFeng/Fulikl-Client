package missu.epsilon.client.event.events.player;

import missu.epsilon.client.event.impl.Event;

public record PlayerDirectionChangeEvent(float prevPitch, float prevYaw, float pitch, float yaw) implements Event {}
