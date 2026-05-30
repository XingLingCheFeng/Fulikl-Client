package missu.epsilon.client.socket.management;

import com.google.gson.JsonObject;
import missu.epsilon.client.socket.packet.Packet;
import missu.epsilon.client.socket.packet.implemention.clientbound.*;
import missu.epsilon.client.socket.packet.implemention.serverbound.ServerBoundHandshakePacket;
import missu.epsilon.client.socket.packet.implemention.serverbound.ServerBoundMessagePacket;
import missu.epsilon.client.socket.packet.implemention.serverbound.ServerBoundUpdateIgnPacket;

import java.util.HashMap;
import java.util.Map;

public class PacketManager {
    private final Map<Integer, Class<? extends Packet>> idToPacketMap = new HashMap<>();
    private final Map<Class<? extends Packet>, Integer> packetToIdMap = new HashMap<>();
    private int id;

    public PacketManager() {
        // client bound
        register(
                ClientBoundDisconnectPacket.class,
                ClientBoundConnectedPacket.class,
                ClientBoundUpdateUserListPacket.class,
                ClientBoundUpdateUserClientListPacket.class,
                ClientBoundMessagePacket.class
        );

        // server bound
        register(
                ServerBoundHandshakePacket.class,
                ServerBoundUpdateIgnPacket.class,
                ServerBoundMessagePacket.class
        );
    }

    @SafeVarargs
    private void register(Class<? extends Packet>... classes) {
        for (Class<? extends Packet> clazz : classes) {
            idToPacketMap.put(id, clazz);
            packetToIdMap.put(clazz, id);
            id++;
        }
    }

    public Packet readPacket(JsonObject object) {
        if (object.has("id") && object.has("cxt")) {
            int id = object.get("id").getAsInt();
            Packet packet = create(id);
            packet.readPacket(object.get("cxt").getAsJsonObject());
            return packet;
        } else {
            throw new RuntimeException("Unknown packet");
        }
    }

    public JsonObject writePacket(Packet packet) {
        JsonObject jsonObject = new JsonObject();
        JsonObject packetJson = packet.writePacket();
        jsonObject.addProperty("id", packetToIdMap.get(packet.getClass()));
        jsonObject.add("cxt", packetJson);
        return jsonObject;
    }

    public Packet create(int id) {
        if (!idToPacketMap.containsKey(id)) {
            throw new IllegalArgumentException("Unknown packet: " + id);
        }
        Class<? extends Packet> clazz = idToPacketMap.get(id);
        return create(clazz);
    }

    public Packet create(Class<? extends Packet> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create packet: " + clazz.getName(), e);
        }
    }
}