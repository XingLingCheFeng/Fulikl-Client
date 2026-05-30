package missu.epsilon.client.features.modules.player;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.SendCommandEvent;
import missu.epsilon.client.event.events.game.SendMessageEvent;
import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.EnumAutoDisableType;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.ingameui.notification.NotificationManager;
import missu.epsilon.client.ingameui.notification.NotificationType;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.miscs.Multithreading;
import missu.epsilon.client.utils.miscs.TimerUtils;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.Text;

import java.util.concurrent.TimeUnit;

@ModuleInfo(name = "AutoPlay", description = "Auto join next game", category = ModuleCategory.PLAYER, hide = true)
public class AutoPlay extends Module {

    public static ListValue server = new ListValue("Server", new String[]{"Hypixel"}, "Hypixel");

    public static String playCommand = "";
    public static NumberValue winDelay = new NumberValue("WinDelay",5000,0,10000,50);
    public static NumberValue loseDelay = new NumberValue("LoseDelay",0,0,10000,50);
    public static boolean startNextGame;
    public static boolean win;
    public static boolean lose;
    public static boolean canSend;
    public static TimerUtils timer = new TimerUtils();
    private boolean notified = false;
    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.packet instanceof TitleS2CPacket(Text text)) {
            if (text == null) return;

            String title = text.getString();

            if (server.get().equals("Hypixel")) {
                if (title.startsWith("§6§l") && (title.endsWith("!") || title.endsWith("！"))) {
                    startNextGame = true;
                    win = true;
                    timer.reset();
                } else if (title.startsWith("§c§l") && (title.endsWith("!") || title.endsWith("！"))) {
                    startNextGame = true;
                    lose = true;
                    timer.reset();
                }
            }
        } else if (event.packet instanceof ClickSlotC2SPacket packet) {
            String itemName = packet.getStack().getName().getString();
            if (server.is("Hypixel")) {
                int itemID = Item.getRawId(packet.getStack().getItem());
                if (itemID == 1042) {
                    if (itemName.contains("空岛战争") || itemName.contains("SkyWars")) {
                        if (itemName.contains("双人") || itemName.contains("Doubles")) {
                            if (itemName.contains("普通") || itemName.contains("Normal")) {
                                playCommand = "/play teams_normal";
                            } else if (itemName.contains("疯狂") || itemName.contains("Insane")) {
                                playCommand = "/play teams_insane";
                            }
                        } else if (itemName.contains("单挑") || itemName.contains("Solo")) {
                            if (itemName.contains("普通") || itemName.contains("Normal")) {
                                playCommand = "/play solo_normal";
                            } else if (itemName.contains("疯狂") || itemName.contains("Insane")) {
                                playCommand = "/play solo_insane";
                            }
                        }
                        canSend = true;
                    }
                } else if (itemID == 1027) {
                    if ((itemName.contains("起床战争") || itemName.contains("Bed Wars")) && !itemName.contains("Duel")) {
                        if (itemName.contains("4v4")) {
                            playCommand = "/play bedwars_four_four";
                        } else if (itemName.contains("3v3")) {
                            playCommand = "/play bedwars_four_three";
                        } else if (itemName.contains("双人模式") || itemName.contains("Doubles")) {
                            playCommand = "/play bedwars_eight_two";
                        } else if (itemName.contains("单挑") && !itemName.contains("决斗") || itemName.contains("Solo")) {
                            playCommand = "/play bedwars_eight_one";
                        } else if (itemName.contains("决斗") && itemName.contains("单挑") || itemName.contains("1v1")) {
                            playCommand = "/play bedwars_two_one_duels";
                        }
                        canSend = true;
                    }
                } else if (itemID == 223) {
                    if (itemName.contains("疾速起床决斗") || itemName.contains("Bed Rush")) {
                        if (itemName.contains("1v1") || itemName.contains("单挑")) {
                            playCommand = "/play bedwars_two_one_duels_rush";
                        }
                        canSend = true;
                    }
                } else if (itemID == 959) {
                    if (itemName.contains("Sumo Duel") || itemName.contains("相扑决斗")) {
                        playCommand = "/play duels_sumo_duel";
                        canSend = true;
                    }
                } else if (itemID == 980) {
                    if (itemName.contains("经典") || itemName.contains("Classic")) {
                        if (itemName.contains("决斗") || itemName.contains("Duel")) {
                            playCommand = "/play duels_classic_duel";
                        }
                        if (itemName.contains("双人") || itemName.contains("Doubles")) {
                            playCommand = "/play duels_classic_doubles";
                        }
                        canSend = true;
                    }
                }
            }
        }
    }

    @EventTarget
    public void onSendMessage(SendMessageEvent event) {
        if (server.is("Hypixel")) {
            if (event.message.startsWith("/play")) {
                playCommand = event.message;
            }
        }
    }

    @EventTarget
    public void onSendCommand(SendCommandEvent event) {
        if (server.is("Hypixel")) {
            if (event.command.startsWith("play")) {
                playCommand = "/" + event.command;
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (startNextGame && (win || lose)) {
            if (Client.moduleManager.getModule(AutoGG.class).isEnabled() && AutoGG.spoken || !Client.moduleManager.getModule(AutoGG.class).isEnabled()) {
                sendToGame(playCommand);
                startNextGame = false;
                win = false;
                lose = false;
            }
        }
    }

    private void sendToGame(String string) {
        float delay = (win) ? winDelay.get().floatValue() / 1000 : (lose) ? loseDelay.get().floatValue() / 1000 : 0;
        for (Module module : Client.moduleManager.getModules()) {
            if (module.enumAutoDisableType == EnumAutoDisableType.GAME_END && ClientSettings.autoDis.get()) {
                module.setEnabled(false);
            }
        }
        if (!notified && win) {
            NotificationManager.post(NotificationType.SUCCESS, "A new game will begin" + (delay > 0 ? " in " + delay + "s" : "") + "!", delay);
            notified = true;
        }
        Multithreading.schedule(() -> {
            if (canSend)
                ClientUtils.send(string);
            else {

            }
        }, (long) delay, TimeUnit.SECONDS);
    }

    @EventTarget
    public void onWorld(WorldEvent e) {
        notified = false;
    }
}
