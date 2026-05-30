package missu.epsilon.client.socket.packet.implemention.clientbound;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import missu.epsilon.client.socket.packet.Packet;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ClientBoundUpdateUserClientListPacket implements Packet {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private Map<String, String> userMap;

    public ClientBoundUpdateUserClientListPacket() {
        this.userMap = new HashMap<>();
    }

    @Override
    public void readPacket(JsonObject jsonObject) {
        if (jsonObject.has("f")) {
            String mapJson = jsonObject.get("f").toString();
            this.userMap = GSON.fromJson(mapJson, MAP_TYPE);
        }
    }

    @Override
    public JsonObject writePacket() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("f", GSON.toJsonTree(userMap, MAP_TYPE));
        return jsonObject;
    }

}