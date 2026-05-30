package missu.epsilon.client.features.modules.player;

import lombok.Getter;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.RespawnEvent;
import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.ingameui.notification.NotificationManager;
import missu.epsilon.client.ingameui.notification.NotificationType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static missu.epsilon.client.utils.Wrapper.mc;

@Getter
@ModuleInfo(name = "AntiBot", category = ModuleCategory.PLAYER, description = "Automatically remove the bot from the attack target")
public class AntiBot extends Module {
    public static MultiBoolValue botChecks = new MultiBoolValue("CheckType", new BoolValue[]{
            new BoolValue("Ground",false),
            new BoolValue("EntityID", false),
            new BoolValue("Sleep", false),
            new BoolValue("Armor", false),
            new BoolValue("Dead", false),
            new BoolValue("Name", false),
            new BoolValue("Uuid", false),
            new BoolValue("LivingTime",false),
            new BoolValue("PlayerInfo", false),
            new BoolValue("Hypixel", false)
    });
    public static NumberValue livingValue = (NumberValue) new NumberValue("LivingTime(ms)",2500,0,10000,100).displayable(() -> botChecks.get("LivingTime"));

    public static final List<Integer> groundList = new ArrayList<>();
    public static final Map<UUID, String> uuidDisplayNames = new ConcurrentHashMap<>();
    public static final Map<Integer, String> entityIdDisplayNames = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> uuids = new ConcurrentHashMap<>();
    public static final Set<Integer> ids = new HashSet<>();
    public static final Map<UUID, Long> livingTime = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        uuidDisplayNames.clear();
        entityIdDisplayNames.clear();
        ids.clear();
        uuids.clear();
        groundList.clear();
    }

    @Override
    public void onDisable() {
        uuidDisplayNames.clear();
        entityIdDisplayNames.clear();
        ids.clear();
        uuids.clear();
        groundList.clear();
    }
    @SuppressWarnings("unused")
    @EventTarget
    public void onRespawn(RespawnEvent event) {
        uuidDisplayNames.clear();
        entityIdDisplayNames.clear();
        ids.clear();
        uuids.clear();
        groundList.clear();
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onWorld(WorldEvent event) {
        uuidDisplayNames.clear();
        entityIdDisplayNames.clear();
        ids.clear();
        uuids.clear();
        groundList.clear();
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.eventState == CancellableEvent.EventState.PRE) {
            for (Map.Entry<UUID, Long> entry : uuids.entrySet()) {
                if (System.currentTimeMillis() - entry.getValue() > 500L) {
                    uuids.remove(entry.getKey());
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.packetState == CancellableEvent.PacketState.RECEIVE) {
            if (event.getPacket() instanceof PlayerListS2CPacket packet) {
                if (packet.getActions().contains(PlayerListS2CPacket.Action.ADD_PLAYER)) {
                    for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {

                        Text displayName = entry.displayName();
                        if (displayName == null) continue;
                        if (!displayName.getSiblings().isEmpty()) continue;
                        if (entry.gameMode() != GameMode.SURVIVAL) continue;
                        UUID uuid = Objects.requireNonNull(entry.profile()).getId();
                        NotificationManager.post(NotificationType.AntiBot, "Bot Detected, Name: " + entry.profile().getName());
                        livingTime.put(uuid,System.currentTimeMillis());

                        uuids.put(uuid, System.currentTimeMillis());
                        uuidDisplayNames.put(uuid, displayName.getString());
                    }
                }
            } else if (event.getPacket() instanceof EntityS2CPacket packet) {
                Entity entity = packet.getEntity(mc.world);
                if (entity != null && entity.onGround && !groundList.contains(entity.getId()))
                    groundList.add(entity.getId());
            } else if (event.getPacket() instanceof EntityAnimationS2CPacket packet) {
                Entity entity = Objects.requireNonNull(mc.world).getEntityById(packet.getEntityId());

                if (entity != null && packet.getAnimationId() == 0) {
                    livingTime.remove(entity.getUuid());
                }
            } else if (event.getPacket() instanceof EntitySpawnS2CPacket packet) {
                if (uuids.containsKey(packet.getUuid())) {
                    String displayName = uuidDisplayNames.get(packet.getUuid());
                    entityIdDisplayNames.put(packet.getEntityId(), displayName);
                    uuids.remove(packet.getUuid());
                    ids.add(packet.getEntityId());
                }
            } else if (event.getPacket() instanceof EntitiesDestroyS2CPacket packet) {
                for (int entityID : packet.getEntityIds()) {
                    if (groundList.contains(entityID)) groundList.remove(entityID);
                }

                for (Integer entityId : packet.getEntityIds()) {
                    ids.remove(entityId);
                }
            }
        }
    }

    public static boolean isBot(LivingEntity player) {
        AntiBot antiBot = Client.moduleManager.getModule(AntiBot.class);
        if (antiBot.isEnabled()) {
            if (botChecks.get("Ground") && !groundList.contains(player.getId())) {
                return true;
            }
            if (botChecks.get("Hypixel")) {
                if (player.getName() == null || player.getDisplayName() == null || player.getName().getString().isEmpty())
                    return true;
                if (player.isDead())
                    return true;
                if (player.getName().getString().contains("§k"))
                    return true;
                if (!getTablist().contains(player.getName().getString())){
                    return true;
                }
                if (player.getHealth() != 20F && player.getName().getString().startsWith("§c"))
                    return true;
                if (player.maxHurtTime == 0) {
                    final String unformattedText = player.getDisplayName().getString();

                    if (player.getHealth() == 20F) {
                        if (unformattedText.length() == 10 && unformattedText.charAt(0) != '§')
                            return true;
                        if (unformattedText.length() == 12 && player.isSleeping() && unformattedText.charAt(0) == '§')
                            return true;
                        if (unformattedText.length() >= 7 && unformattedText.charAt(2) == '[' && unformattedText.charAt(3) == 'N' && unformattedText.charAt(6) == ']')
                            return true;
                        if (player.getName().getString().contains(" "))
                            return true;
                    } else if (player.isInvisible()) {
                        if (unformattedText.length() >= 3 && unformattedText.charAt(0) == '§' && unformattedText.charAt(1) == 'c')
                            return true;
                    }
                }
            }
            if (botChecks.get("Dead") && player.isDead()) {
                return true;
            }
            if (botChecks.get("Sleep") && player.isSleeping()) {
                return true;
            }
            if (botChecks.get("PlayerInfo") && ids.contains(player.getId())) {
                return true;
            }

            if (botChecks.get("LivingTime")) {
                return livingValue.get() >= 1.0F && livingTime.containsKey(player.getUuid()) && (float) (System.currentTimeMillis() - livingTime.get(player.getUuid())) < livingValue.get();
            }

            if (botChecks.get("EntityID") && (player.getId() >= 1000000000 || player.getId() <= -1)) {
                return true;
            }

            if (botChecks.get("Uuid") && mc.getNetworkHandler() != null && mc.getNetworkHandler().getPlayerListEntry(player.getUuid()) == null) {
                return true;
            }

            if (botChecks.get("Name")) {
                if (Objects.requireNonNull(player.getDisplayName()).getString().contains("CIT-")) {
                    return true;
                } else if (Objects.requireNonNull(player.getDisplayName()).getString().contains("[NPC]")) {
                    return true;
                }
            }

            if (player instanceof PlayerEntity playerEntity) {
                return botChecks.get("Armor")
                        && playerEntity.getInventory().getArmorStack(0).isEmpty()
                        && playerEntity.getInventory().getArmorStack(1).isEmpty()
                        && playerEntity.getInventory().getArmorStack(2).isEmpty()
                        && playerEntity.getInventory().getArmorStack(3).isEmpty();
            }
        }

        return false;
    }
    private static List<String> getTablist() {
        List<String> tab = new ArrayList<>();

        for (PlayerListEntry entry : getTablist(true)) {
            if (entry == null) continue;
            tab.add(entry.getProfile().getName());
        }

        return tab;
    }

    public static List<PlayerListEntry> getTablist(boolean removeSelf) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) {
            return new ArrayList<>();
        }

        List<PlayerListEntry> list =
                new ArrayList<>(mc.getNetworkHandler().getPlayerList());

        removeDuplicates(list);

        if (removeSelf) {
            list.removeIf(entry ->
                    entry.getProfile().getId().equals(mc.player.getUuid())
            );
        }

        return list;
    }

    private static void removeDuplicates(List<PlayerListEntry> list) {
        Set<UUID> seen = new HashSet<>();
        list.removeIf(entry -> !seen.add(entry.getProfile().getId()));
    }

}
