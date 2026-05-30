package missu.epsilon.client;

import ddev.SmtcLoader.Loader;
import missu.epsilon.client.command.CommandManager;
import missu.epsilon.client.config.ConfigManager;
import missu.epsilon.client.config.impl.DragConfig;
import missu.epsilon.client.event.EventManager;
import missu.epsilon.client.event.events.init.ClientInitEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleManager;
import missu.epsilon.client.features.modules.visual.ClientSettings;
import missu.epsilon.client.ingameui.Dragging;
import missu.epsilon.client.ingameui.clickgui.ClickGUI;
import missu.epsilon.client.ingameui.clickgui.ClickGUIListener;
import missu.epsilon.client.ingameui.progressbar.PBManager;
import missu.epsilon.client.management.rotation.RotationManager;
import missu.epsilon.client.socket.VerifySocket;
import missu.epsilon.client.socket.utils.VerifyUtils;
import missu.epsilon.client.utils.client.ClientData;
import missu.epsilon.client.utils.client.TargetManager;
import missu.epsilon.client.utils.client.security.AntiLeak;
import missu.epsilon.client.utils.client.security.BypassProtection;
import missu.epsilon.client.utils.client.security.KillSwitch;
import missu.epsilon.client.utils.client.security.dev.DevelopmentUtil;
import missu.epsilon.client.utils.entity.BlinkUtils;
import missu.epsilon.client.utils.entity.CombatUtils;
import missu.epsilon.client.utils.entity.PacketLockUtils;
import missu.epsilon.client.utils.entity.PlayerUtils;
import missu.epsilon.client.utils.client.security.HostsChecker;
import missu.epsilon.client.utils.multithreading.CoroutineScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import tech.skidonion.obfuscator.annotations.NativeObfuscation;

@NativeObfuscation
public class Client {
    private static final Client INSTANCE = new Client();

    //YES
    private EventManager eventManager;

    public static String CLIENT_NAME = "Epsilon";
    public static String CLIENT_VERSION = "2.2";

    //PLEASEEEE USE GETTER INSTEAD OF STATIC VARIABLES
    public static ModuleManager moduleManager;
    public static RotationManager rotationManager;
    public static missu.epsilon.client.command.CommandManager commandManager;
    public static ConfigManager configManager;
    public static PBManager pbManager;
    public static TargetManager targetManager;

    public static BypassProtection bypassProtection;
    public static ClickGUIListener clickGUIListener;
    public static ClickGUI clickGUI;

    public static Loader smtcLoader;
    public static String username = "Quest";

    public static final Logger logger = LoggerFactory.getLogger(CLIENT_NAME);
    public static boolean started;

    public static Systems SYSTEM;

    public static String getClientName() {
        if (moduleManager != null && moduleManager.getModule(ClientSettings.class) != null && moduleManager.getModule(ClientSettings.class).isEnabled() && !ClientSettings.clientName.get().isEmpty()) {
            return ClientSettings.clientName.get();
        }
        return CLIENT_NAME;
    }

    public Client(){
        getEventManager().subscribe(this);
    }

    @EventTarget
    public void onClientInit(ClientInitEvent event){
        startClient();
    }

    @NativeObfuscation
    public static void startClient() {
        try {
            logger.info("[{}] Init Client", CLIENT_NAME);
            long first = System.currentTimeMillis();

            DevelopmentUtil.runInRelease(AntiLeak::init);

            CoroutineScope.scoped(scope -> {
                //SMTC Loader
                scope.launch(() -> {
                    SYSTEM = Systems.track();

                    if (SYSTEM == Systems.WINDOWS) {
                        smtcLoader = Loader.startSmtc();
                    }
                });

                //Module stuff
                scope.launch(() -> {
                    moduleManager = new ModuleManager();
                    clickGUI = ClickGUI.register();
                    clickGUIListener = ClickGUIListener.register();
                    configManager = ConfigManager.register();
                });

                //Other
                scope.launch(() -> pbManager = new PBManager());
                scope.launch(() -> targetManager = new TargetManager());
                scope.launch(() -> commandManager = new CommandManager());
                scope.launch(() -> bypassProtection = new BypassProtection());
                scope.launch(() -> rotationManager = new RotationManager());

                scope.launch(PlayerUtils::register);
                scope.launch(BlinkUtils::register);
                scope.launch(ClientData::register);
                scope.launch(CombatUtils::register);
                scope.launch(PacketLockUtils::register);
            });

            long second = System.currentTimeMillis();

            logger.info("[{}] Client init in {} seconds", CLIENT_NAME, (second - first) / 1000.0);
            started = true;
        } catch (Exception ignored) {
            KillSwitch.suicide();
        }
    }



    public EventManager getEventManager() {
        if (eventManager == null) {
            eventManager = new EventManager();
        }
        return eventManager;
    }

    public static Client getInstance() {
        return INSTANCE;
    }

    public static Dragging createDrag(Module module, String name, float x, float y) {
        DragConfig.draggables.put(name, new Dragging(module, name, x, y));
        return DragConfig.draggables.get(name);
    }

    public static void stopClient() {
        configManager.saveAllConfig();
    }

    public enum Systems {
        WINDOWS, MAC, LINUX;

        public static Systems track() {
            String family = new SystemInfo().getOperatingSystem().getFamily().toLowerCase();
            if (family.contains("windows")) return WINDOWS;
            if (family.contains("mac") || family.contains("darwin")) return MAC;
            return LINUX;
        }
    }
}