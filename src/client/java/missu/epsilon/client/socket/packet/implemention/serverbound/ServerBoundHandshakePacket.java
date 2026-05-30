package missu.epsilon.client.socket.packet.implemention.serverbound;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import missu.epsilon.client.socket.packet.Packet;

@Getter
public class ServerBoundHandshakePacket implements Packet {
    private String username;
    private String token;
    private String client;
    private String password;
    private String version;

    public ServerBoundHandshakePacket(String username, String password, String token, String client, String version) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.client = client;
        this.version = version;
    }

    public ServerBoundHandshakePacket() {
    }

    @Override
    public void readPacket(JsonObject jsonObject) {
        if (jsonObject.has("u")) {
            this.username = jsonObject.get("u").getAsString();
        }
        if (jsonObject.has("p")) {
            this.password = jsonObject.get("p").getAsString();
        }
        if (jsonObject.has("t")) {
            this.token = jsonObject.get("t").getAsString();
        }
        if (jsonObject.has("z")) {
            this.client = jsonObject.get("z").getAsString();
        }
        if (jsonObject.has("v")) {
            this.version = jsonObject.get("v").getAsString();
        }
    }

    @Override
    public JsonObject writePacket() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("u", username);
        jsonObject.addProperty("p", password);
        jsonObject.addProperty("t", token);
        jsonObject.addProperty("z", client);
        jsonObject.addProperty("v", version);
        return jsonObject;
    }

}