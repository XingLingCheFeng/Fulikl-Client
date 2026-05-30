package missu.epsilon.client.socket.packet.implemention.serverbound;

import com.google.gson.JsonObject;
import lombok.Getter;
import missu.epsilon.client.socket.packet.Packet;

@Getter
public class ServerBoundMessagePacket implements Packet {
    private String message;

    public ServerBoundMessagePacket(String message) {
        this.message = message;
    }

    @Override
    public void readPacket(JsonObject jsonObject) {
        if (jsonObject.has("m")) {
            this.message = jsonObject.get("m").getAsString();
        }
    }

    @Override
    public JsonObject writePacket() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("m", message);
        return jsonObject;
    }


}