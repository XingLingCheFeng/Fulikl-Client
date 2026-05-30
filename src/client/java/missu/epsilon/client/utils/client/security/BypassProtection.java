package missu.epsilon.client.utils.client.security;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.Priorities;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.utils.Wrapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@NativeObfuscation
public class BypassProtection implements Wrapper {

    private final KeyPair keyPair;
    private final byte[] encFinger;
    private final byte[] encLocraw;
    private final byte[] encExpected;

    private int trustToken = 0;
    private byte[] encryptedToken = null;

    private boolean fingerprintLocked = false;
    private boolean locrawApplied = false;
    private boolean expectingLocraw = false;

    private final AtomicBoolean lock = new AtomicBoolean(false);

    private long verifyStartTime = 0;
    private int  retryCount = 0;
    private static final int  MAX_RETRIES = 3;
    private static final long RETRY_INTERVAL = 3000L;

    private boolean cachedTrust = false;
    private long cacheExpiry = 0;
    private static final long CACHE_TTL = 500L;

    private static final List<String> EXPECTED_FINGERPRINT = List.of(
            "white","white","dark_blue","black","dark_green",
            "white","white","dark_green","black","dark_red",
            "dark_aqua","blue","dark_green","black","black",
            "dark_aqua","blue","dark_green","black","black",
            "dark_aqua","blue","dark_green","black","black"
    );

    public BypassProtection() {
        KeyPair kp = null;
        byte[] encF = null;
        byte[] encL = null;
        byte[] encE = null;
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048, new SecureRandom());
            kp = gen.generateKeyPair();

            SecureRandom rng = new SecureRandom();
            int f = rng.nextInt();
            int l = rng.nextInt();
            encF = encrypt(kp, f);
            encL = encrypt(kp, l);
            encE = encrypt(kp, f ^ l);
        } catch (Exception ignored) {
        }
        keyPair = kp;
        encFinger = encF;
        encLocraw = encL;
        encExpected = encE;
        Client.getInstance().getEventManager().subscribe(this);
    }

    @EventTarget
    public void onTick(TickEvent e) {
        if (mc.getNetworkHandler() == null) {
            onDisconnected();
            return;
        }

        if (lock.get() && mc.player != null){
            lock.set(false);
        }

        if (verifyStartTime != 0 && !isTrustedEnvironment()) {
            long elapsed = System.currentTimeMillis() - verifyStartTime;
            if (elapsed > RETRY_INTERVAL * (retryCount + 1) && retryCount < MAX_RETRIES) {
                retryCount++;
                if (locrawApplied) {
                    trustToken ^= decryptToken(encLocraw);
                    locrawApplied = false;
                    invalidateCache();
                }
                expectingLocraw = true;
                mc.getNetworkHandler().sendCommand("locraw");
            }
        }

    }

    @EventTarget
    public void onWorld(WorldEvent e) {
        if (lock.get()) return;

        lock.set(true);

        if (locrawApplied) {
            trustToken ^= decryptToken(encLocraw);
            locrawApplied = false;
            invalidateCache();
        }

        expectingLocraw = true;
        retryCount = 0;
        verifyStartTime = System.currentTimeMillis();

        MinecraftClient.getInstance().send(() -> {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendCommand("locraw");
            }
        });

    }

    @EventTarget(Priorities.VERY_HIGH)
    public void onPacket(PacketEvent event) {
        if (!(event.getPacket() instanceof GameMessageS2CPacket pkt)) return;
        Text content = pkt.content();

        if (!fingerprintLocked) {
            if (deserialize(content).equals(EXPECTED_FINGERPRINT)) {
                trustToken ^= decryptToken(encFinger);
                fingerprintLocked = true;
                invalidateCache();
            }
        }

        if (expectingLocraw && content.getString().startsWith("{\"server\":")) {
            expectingLocraw = false;
            locrawApplied = true;
            trustToken ^= decryptToken(encLocraw);
            invalidateCache();
            if (trustToken == decryptToken(encExpected)) {
                try {
                    Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    c.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
                    encryptedToken = c.doFinal(ByteBuffer.allocate(4).putInt(trustToken).array());
                } catch (Exception ignored) {}
                trustToken = 0;
                verifyStartTime = 0;
                retryCount = 0;
                invalidateCache();
            }
            event.setCancelled(true);
        }
    }

    @NativeObfuscation.Inline
    public boolean isTrustedEnvironment() {
        if (DummyClass.nTrustFactor() == 0x4E3A1F7C) return true;

        long now = System.currentTimeMillis();
        if (now < cacheExpiry) return cachedTrust;

        boolean result = encryptedToken != null
                && keyPair != null
                && encExpected != null
                && decryptToken(encryptedToken) == decryptToken(encExpected);

        cachedTrust = result;
        cacheExpiry = now + CACHE_TTL;
        return result;
    }

    private void onDisconnected() {
        lock.set(false);
        trustToken = 0;
        encryptedToken = null;
        fingerprintLocked = false;
        locrawApplied = false;
        expectingLocraw = false;
        verifyStartTime = 0;
        retryCount = 0;
        invalidateCache();
    }

    private void invalidateCache() { cacheExpiry = 0; }

    private static byte[] encrypt(KeyPair kp, int value) throws Exception {
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.ENCRYPT_MODE, kp.getPublic());
        return c.doFinal(ByteBuffer.allocate(4).putInt(value).array());
    }

    private int decryptToken(byte[] data) {
        try {
            Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            c.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            return ByteBuffer.wrap(c.doFinal(data)).getInt();
        } catch (Exception e) {
            return 0;
        }
    }

    private List<String> deserialize(Text root) {
        List<String> colors = new ArrayList<>();
        List<Text> level1 = root.getSiblings();
        if (level1.size() < 2) return colors;
        List<Text> triplets = level1.get(1).getSiblings();
        for (int i = 0; i < triplets.size(); i += 3) {
            TextColor color = triplets.get(i).getStyle().getColor();
            if (color != null) colors.add(color.getName());
        }
        return colors;
    }
}