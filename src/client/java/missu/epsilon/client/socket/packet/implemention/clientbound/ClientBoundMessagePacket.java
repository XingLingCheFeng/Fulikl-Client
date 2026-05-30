// [file name]: ClientBoundMessagePacket.java
package missu.epsilon.client.socket.packet.implemention.clientbound;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import missu.epsilon.client.socket.packet.Packet;

@Getter
public class ClientBoundMessagePacket implements Packet {
    private String sender;
    private String message;

    public ClientBoundMessagePacket() {
    }

    @Override
    public void readPacket(JsonObject jsonObject) {
        if (jsonObject.has("s")) {
            this.sender = jsonObject.get("s").getAsString();
        }
        if (jsonObject.has("m")) {
            this.message = jsonObject.get("m").getAsString();
        }
    }

    @Override
    public JsonObject writePacket() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("s", sender);
        jsonObject.addProperty("m", message);
        return jsonObject;
    }

}