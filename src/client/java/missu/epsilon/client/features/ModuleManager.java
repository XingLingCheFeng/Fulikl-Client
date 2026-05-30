package missu.epsilon.client.features;

import lombok.Getter;
import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.WorldEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.events.player.KeyEvent;
import missu.epsilon.client.event.events.player.MotionEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.modules.combat.*;
import missu.epsilon.client.features.modules.exploit.*;
import missu.epsilon.client.features.modules.movement.*;
import missu.epsilon.client.features.modules.player.*;
import missu.epsilon.client.features.modules.render.*;
import missu.epsilon.client.features.modules.visual.*;
import missu.epsilon.client.features.modules.visual.targethud.TargetHUD;
import missu.epsilon.client.features.modules.world.*;
import missu.epsilon.client.utils.client.ClientUtils;
import missu.epsilon.client.utils.client.KeyCodeConverter;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

import java.util.*;

import static missu.epsilon.client.utils.Wrapper.mc;

public class ModuleManager {

    @Getter private final List<Module> modules = new ArrayList<>();
    private final Map<Class<?>, Module> moduleClassMap = new HashMap<>();

    public ModuleManager(){
        Client.getInstance().getEventManager().subscribe(this);
        registerModules();
    }

    private float derpYaw = 0f;

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (mc.player == null) return;
        if (Client.moduleManager.getModule(Derp.class).isEnabled() && Derp.mode.is("Render")) {
            derpYaw += 15f;
            if (derpYaw > 360f) derpYaw -= 360f;

            mc.player.headYaw = derpYaw;
            mc.player.bodyYaw = derpYaw;
        }
    }

    @EventTarget
    public void onKeyInput(KeyEvent event){
        Client.moduleManager.getModules().forEach(module -> {
            try {
                if (KeyCodeConverter.stringToKeycode(module.bind.get()) == event.getKey() && !ClientUtils.isNull() && mc.currentScreen == null)
                    module.toggle();
            } catch (Exception e) {
                Client.logger.error("Exception while executing module {}: ", module.getName());
                e.printStackTrace();
            }
        });
    }

    /**
     * Register all modules
     */
    public void registerModules() {
        ClientUtils.printToLog("[ModuleManager] Loading modules...");
        // Combat
        registerModule(new FakeLag());
        registerModule(new AntiCrystal());
        registerModule(new AntiFireball());
        registerModule(new AntiKnockback());
        registerModule(new AutoClicker());
        registerModule(new AutoSoup());
        registerModule(new AutoWeapon());
        registerModule(new KillAura());
        registerModule(new AutoRod());
        registerModule(new MoreKnockBack());
        registerModule(new ThrowableAura());
        // Exploit
        registerModule(new AntiBot());
        registerModule(new MurderMystery());
        registerModule(new ChatBypass());
        registerModule(new PartySpammer());
        registerModule(new Disabler());
        registerModule(new TestingModule());
        registerModule(new PacketDebugger());
        registerModule(new OldHitting());
        registerModule(new CrazyMace());
        // Movement
        registerModule(new FastClimb());
        registerModule(new AntiVoid());
        registerModule(new Speed());
        registerModule(new Eagle());
        registerModule(new LongJump());
        registerModule(new GuiMove());
        registerModule(new NoFall());
        registerModule(new NoJumpDelay());
        registerModule(new NoSlow());
        registerModule(new Sprint());
        // Player
        registerModule(new AutoGG());
        registerModule(new AntiAFK());
        registerModule(new AutoPlay());
        registerModule(new HackDefender());
        registerModule(new AutoRespawn());
        registerModule(new KillInsult());
        registerModule(new AutoTool());
        registerModule(new Blink());
        registerModule(new Delay());
        registerModule(new Derp());
        registerModule(new FastPlace());
        registerModule(new InvManager());
        registerModule(new MidPearl());
        registerModule(new NameProtect());
        registerModule(new NoAttackDelay());
        registerModule(new IllegalInteract());
        registerModule(new Target());
        registerModule(new Teams());
        registerModule(new AutoAcceptPolicy());
        // Render
        registerModule(new BedPlates());
        registerModule(new AntiBlind());
        registerModule(new AttackEffect());
        registerModule(new BedESP());
        registerModule(new Camera());
        registerModule(new Chams());
        registerModule(new ContainerESP());
        registerModule(new Particles());
        registerModule(new JumpCircle());
        registerModule(new ESP());
        registerModule(new FastItems());
        registerModule(new FreeLook());
        registerModule(new FullBright());
        registerModule(new Notification());
        registerModule(new PlayerNameTags());
        registerModule(new Projectiles());
        registerModule(new WorldColor());
        registerModule(new AspectRatio());
        registerModule(new Animations());
        // Visual
        registerModule(new ClickGUI());
        registerModule(new ClientSettings());
        registerModule(new ModuleList());
        registerModule(new MusicInfo());
        registerModule(new PostProcessing());
        registerModule(new PotionHUD());
        registerModule(new Scoreboard());
        registerModule(new TargetHUD());
        registerModule(new TargetMarker());
        registerModule(new WaterMark());
        // World
        registerModule(new Ambience());
        registerModule(new SpeedMine());
        registerModule(new BedBreaker());
        registerModule(new QuickPlay());
        registerModule(new ContainerAura());
        registerModule(new ContainerStealer());
        registerModule(new Extinguisher());
        registerModule(new FastWeb());
        registerModule(new PlayerTracker());
        registerModule(new Scaffold());
        registerModule(new GameSpeed());
        modules.forEach(Module::onInitialize);
        ClientUtils.printToLog("[ModuleManager] Loaded " + modules.size() + "modules...");
    }

    /**
     * Register module
     */
    public void registerModule(Module module) {
        modules.add(module);
        moduleClassMap.put(module.getClass(), module);
        modules.sort(Comparator.comparing(m -> m.name));
    }

    /**
     * Get module by module class
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> moduleClass) {
        return (T) moduleClassMap.get(moduleClass);
    }

    /**
     * Get module by module name
     */
    public Module getModule(String moduleName) {
        for (Module module : modules) {
            if (module.name.trim().equalsIgnoreCase(moduleName)) {
                return module;
            }
        }
        return null;
    }

    @EventTarget
    public void onWorld(WorldEvent event) {
        for (Module module : modules) {
            if (module.enumAutoDisableType == EnumAutoDisableType.GAME_END && ClientSettings.autoDis.get()) {
                module.setEnabled(false);
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) {
            for (Module module : modules) {
                if (module.enumAutoDisableType == EnumAutoDisableType.FLAG && ClientSettings.autoDis.get()) {
                    module.setEnabled(false);
                }
            }
        }
    }

}
