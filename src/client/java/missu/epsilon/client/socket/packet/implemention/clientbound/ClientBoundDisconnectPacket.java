package missu.epsilon.client.socket.packet.implemention.clientbound;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import missu.epsilon.client.socket.packet.Packet;

@Getter
public class ClientBoundDisconnectPacket implements Packet {
    private String reason;


    public ClientBoundDisconnectPacket() {
    }

    @Override
    public void readPacket(JsonObject jsonObject) {
        if (jsonObject.has("r")) {
            this.reason = jsonObject.get("r").getAsString();
        }
    }

    @Override
    public JsonObject writePacket() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("r", reason);
        return jsonObject;
    }

}