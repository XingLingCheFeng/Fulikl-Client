package missu.epsilon.client.socket.packet.implemention.clientbound;

import com.google.gson.JsonObject;
import missu.epsilon.client.socket.packet.Packet;

public class ClientBoundConnectedPacket implements Packet {

    public ClientBoundConnectedPacket() {
    }

    @Override
    public void readPacket(JsonObject jsonObject) {
        // 空包，没有字段
    }

    @Override
    public JsonObject writePacket() {
        return new JsonObject(); // 返回空JSON对象
    }

}