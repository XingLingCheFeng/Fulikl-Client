package missu.epsilon.client.socket;

import missu.epsilon.client.Client;
import missu.epsilon.client.socket.client.PacketHandler;
import missu.epsilon.client.socket.client.PacketTransport;
import missu.epsilon.client.socket.utils.CryptUtil;
import missu.epsilon.client.socket.utils.VerifyUtils;
import missu.epsilon.client.utils.client.ClientUtils;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

import static missu.epsilon.client.utils.Wrapper.mc;

@NativeObfuscation
public class VerifySocket {
    public PacketTransport transport;
    public PacketHandler packetHandler;

    @NativeObfuscation
    public void StartSocket() {
        transport = new PacketTransport("cn.pr80.icu", 7878, packetHandler = new PacketHandler() {
            @Override
            public void onMessage(String sender, String message) {
                String username = new String(Base64.getDecoder().decode(sender), StandardCharsets.UTF_8);
                String msg = new String(Base64.getDecoder().decode(message), StandardCharsets.UTF_8);
                if (username.equals("Server")) {
                    if (msg.endsWith(Client.username)) return;
                    ClientUtils.displayChat(msg);
                } else {
                    ClientUtils.displayChat(username + ": " + msg);
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onDisconnected(String message) {
                try {
                    Files.writeString(
                            Paths.get("disconnection_log.txt"),
                            message,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.TRUNCATE_EXISTING
                    );
                    Runtime.getRuntime().exec("cmd /c start disconnection_log.txt");
                } catch (IOException ignored) {
                }
                System.exit(0);
            }

            @Override
            public void onConnected() {
                ClientUtils.displayChat("Connected to IRC Server");
            }

            @Override
            public String getInGameUsername() {
                if (mc.player != null) {
                    return mc.player.getName().getString();
                } else {
                    return Client.username;
                }
            }
        });

        String token = System.getProperty("verify.token");

        byte[] encryptedBytes = Base64.getDecoder().decode(token);

        byte[] decryptedCompressedData = CryptUtil.RSA.decryptByPrivateKey(encryptedBytes);

        String originalData = CryptUtil.Deflate.decompress(decryptedCompressedData);

        String[] split = originalData.split(":");

        String password = split[1];

        transport.connect(Client.username, password, VerifyUtils.getUniqueMachineID(), Client.CLIENT_NAME);
    }
}
