package missu.epsilon.client.socket.packet.implemention.serverbound;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import missu.epsilon.client.socket.packet.Packet;

@Getter
public class ServerBoundUpdateIgnPacket implements Packet {
    private String name;

    public ServerBoundUpdateIgnPacket(String name) {
        this.name = name;
    }

    public ServerBoundUpdateIgnPacket() {
    }

    @Override
    public void readPacket(JsonObject jsonObject) {
        if (jsonObject.has("n")) {
            this.name = jsonObject.get("n").getAsString();
        }
    }

    @Override
    public JsonObject writePacket() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("n", name);
        return jsonObject;
    }

}