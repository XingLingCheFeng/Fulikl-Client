package missu.epsilon.client.socket.packet;

import com.google.gson.JsonObject;

public interface Packet {
    void readPacket(JsonObject jsonObject);
    JsonObject writePacket();
}