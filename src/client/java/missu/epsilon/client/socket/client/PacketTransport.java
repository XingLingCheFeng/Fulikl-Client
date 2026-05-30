package missu.epsilon.client.socket.client;

import lombok.Setter;
import missu.epsilon.client.Client;
import missu.epsilon.client.socket.packet.Packet;
import missu.epsilon.client.socket.packet.implemention.clientbound.*;
import missu.epsilon.client.socket.packet.implemention.serverbound.ServerBoundHandshakePacket;
import missu.epsilon.client.socket.packet.implemention.serverbound.ServerBoundMessagePacket;
import missu.epsilon.client.socket.packet.implemention.serverbound.ServerBoundUpdateIgnPacket;
import missu.epsilon.client.socket.processor.Protocol;
import missu.epsilon.client.utils.client.security.KillSwitch;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@NativeObfuscation
public class PacketTransport {

    private final Protocol protocol = new Protocol();
    private AioSession session;
    @Setter private PacketHandler handler;
    private final String host;
    private final int port;
    private final Map<String, String> userToIgnMap = new ConcurrentHashMap<>();
    private final Map<String, String> userToClientMap = new ConcurrentHashMap<>();
    private final Map<String, String> ignToUserMap = new ConcurrentHashMap<>();
    public final AtomicBoolean packetReceived = new AtomicBoolean(false);
    public final AtomicBoolean isReconnecting = new AtomicBoolean(false);
    public final ScheduledExecutorService timeoutScheduler = Executors.newScheduledThreadPool(1);

    public PacketTransport(String host, int port, PacketHandler handler) {
        this.host = host;
        this.port = port;
        this.handler = handler;

        initSession();
    }

    @SuppressWarnings("resource")
    @NativeObfuscation
    private void initSession() {
        try {
            MessageProcessor<Packet> processor = (session, msg) -> {
                if (msg instanceof ClientBoundDisconnectPacket) {
                    handler.onDisconnected(((ClientBoundDisconnectPacket) msg).getReason());
                    timeoutScheduler.shutdown();
                }
                if (msg instanceof ClientBoundConnectedPacket) {
                    handler.onConnected();
                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                    Runnable task = this::sendInGameUsername;
                    scheduler.scheduleAtFixedRate(task, 60, 5, TimeUnit.SECONDS);

                    timeoutScheduler.scheduleAtFixedRate(() -> packetReceived.set(false), 5, 5, TimeUnit.SECONDS);
                }
                if (msg instanceof ClientBoundUpdateUserListPacket) {
                    packetReceived.set(true);
                    isReconnecting.set(false);
                    userToIgnMap.clear();
                    Map<String, String> decryptedUserMap = new ConcurrentHashMap<>();
                    ((ClientBoundUpdateUserListPacket) msg).getUserMap().forEach((user, ign) -> {
                        try {
                            String decodedUser = new String(Base64.getDecoder().decode(user), StandardCharsets.UTF_8);
                            String decodedIgn = new String(Base64.getDecoder().decode(ign), StandardCharsets.UTF_8);
                            decryptedUserMap.put(decodedUser, decodedIgn);
                        } catch (IllegalArgumentException e) {
                            decryptedUserMap.put(user, ign);
                        }
                    });
                    userToIgnMap.putAll(decryptedUserMap);
                    ignToUserMap.clear();
                    userToIgnMap.forEach((user, ign) -> ignToUserMap.put(ign, user));
                }

                if (msg instanceof ClientBoundUpdateUserClientListPacket) {
                    userToClientMap.clear();
                    Map<String, String> decryptedUserMap = new ConcurrentHashMap<>();
                    ((ClientBoundUpdateUserClientListPacket) msg).getUserMap().forEach((user, client) -> {
                        try {
                            String decodedUser = new String(Base64.getDecoder().decode(user), StandardCharsets.UTF_8);
                            String decodedClient = new String(Base64.getDecoder().decode(client), StandardCharsets.UTF_8);
                            decryptedUserMap.put(decodedUser, decodedClient);
                        } catch (IllegalArgumentException e) {
                            decryptedUserMap.put(user, client);
                        }
                    });
                    userToClientMap.putAll(decryptedUserMap);
                }

                if (msg instanceof ClientBoundMessagePacket) {
                    handler.onMessage(((ClientBoundMessagePacket) msg).getSender(), ((ClientBoundMessagePacket) msg).getMessage());
                }
            };
            AioQuickClient client = new AioQuickClient(host, port, protocol, processor);
            session = client.start();
        } catch (IOException e) {
            KillSwitch.suicide();
        }
    }


    @SuppressWarnings("resource")
    public void sendPacket(Packet packet) {
        try {
            byte[] data = protocol.encode(packet);
            session.writeBuffer().writeInt(data.length);
            session.writeBuffer().write(data);
            session.writeBuffer().flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUser(String name) {
        return ignToUserMap.containsKey(name);
    }

    public String getName(String ign) {
        return ignToUserMap.get(ign);
    }

    public String getClient(String name) {
        return userToClientMap.get(name);
    }

    public void sendChat(String message) {
        sendPacket(new ServerBoundMessagePacket(message));
    }

    public void sendInGameUsername(String username) {
        sendPacket(new ServerBoundUpdateIgnPacket(username));
    }

    public void sendInGameUsername() {
        sendInGameUsername(handler.getInGameUsername());
    }

    public void connect(String username, String password, String token, String client) {
        sendPacket(new ServerBoundHandshakePacket(username, password, token, client, Client.CLIENT_VERSION));
    }
}
