package missu.epsilon.client.socket.processor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import missu.epsilon.client.socket.management.PacketManager;
import missu.epsilon.client.socket.packet.Packet;
import missu.epsilon.client.socket.utils.CryptUtil;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Protocol implements org.smartboot.socket.Protocol<Packet> {
    private final PacketManager packetManager = new PacketManager();
    private final Gson gson = new Gson();

    public byte[] encode(Packet packet) {
        String jsonData = gson.toJson(packetManager.writePacket(packet));
        return CryptUtil.RSA.encryptByPublicKey(jsonData.getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("CallToPrintStackTrace")
    @Override
    public Packet decode(ByteBuffer readBuffer, AioSession session) {
        int remaining = readBuffer.remaining();
        if (remaining < Integer.BYTES) {
            return null;
        }
        readBuffer.mark();
        int length = readBuffer.getInt();
        if (length > readBuffer.remaining()) {
            readBuffer.reset();
            return null;
        }
        byte[] encryptedData = new byte[length];
        readBuffer.get(encryptedData);

        byte[] decryptedData = CryptUtil.RSA.decryptByPrivateKey(encryptedData);
        String text = new String(decryptedData, StandardCharsets.UTF_8);

        try {
            JsonObject object = JsonParser.parseString(text).getAsJsonObject();
            return packetManager.readPacket(object);
        } catch (Exception e) {
            e.printStackTrace();
            session.close();
            return null;
        }
    }
}