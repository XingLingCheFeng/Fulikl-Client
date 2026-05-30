package missu.epsilon.client.features.modules.render;

import lombok.Getter;
import missu.epsilon.client.event.events.game.TickEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static missu.epsilon.client.utils.Wrapper.mc;

@ModuleInfo(name = "FreeLook", category = ModuleCategory.RENDER)
public class FreeLook extends Module {

    @Getter
    private static FreeLook instance;
    @Getter
    private boolean active;
    private boolean toggled;
    private Perspective prevPerspective;

    private ListValue mode = new ListValue("Perspective", new String[]{"Front", "Behind"}, "Behind");
    private BoolValue toggleSetting = new BoolValue("Toggle", false);
    public final ListValue bind = new ListValue("Key Set",
            new String[]{"None", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"},
            "G");

    @Override
    public void onInitialize() {
        instance = this;
        active = false;
    }

    @EventTarget
    public void Tick(TickEvent event) {
        int keyCode = getKeyCodeFromName(bind.getValue());

        // 添加调试信息
        if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
            boolean isKeyPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyCode);

            if (toggleSetting.get()) {
                if (isKeyPressed && !wasKeyPressed) {
                    toggled = !toggled;

                    if (toggled) {
                        if (!active) {
                            this.start();
                        }
                    } else {
                        if (active) {
                            this.stop();
                        }
                    }
                }
            } else {
                if (isKeyPressed) {
                    if (!active) {
                        this.start();
                    }
                } else if (active) {
                    this.stop();
                }
            }

            wasKeyPressed = isKeyPressed;
        }
    }

    private boolean wasKeyPressed = false;

    private void start() {
        Perspective perspective = mode.is("Front") ? Perspective.THIRD_PERSON_FRONT
                : Perspective.THIRD_PERSON_BACK;

        active = true;
        prevPerspective = mc.options.getPerspective();
        mc.options.setPerspective(perspective);
    }

    private void stop() {
        active = false;
        if (prevPerspective != null) {
            mc.options.setPerspective(prevPerspective);
        }
    }

    private int getKeyCodeFromName(String keyName) {
        if (keyName == null || keyName.equals("None")) {
            return GLFW.GLFW_KEY_UNKNOWN;
        }

        switch (keyName.toUpperCase()) {
            case "A": return GLFW.GLFW_KEY_A;
            case "B": return GLFW.GLFW_KEY_B;
            case "C": return GLFW.GLFW_KEY_C;
            case "D": return GLFW.GLFW_KEY_D;
            case "E": return GLFW.GLFW_KEY_E;
            case "F": return GLFW.GLFW_KEY_F;
            case "G": return GLFW.GLFW_KEY_G;
            case "H": return GLFW.GLFW_KEY_H;
            case "I": return GLFW.GLFW_KEY_I;
            case "J": return GLFW.GLFW_KEY_J;
            case "K": return GLFW.GLFW_KEY_K;
            case "L": return GLFW.GLFW_KEY_L;
            case "M": return GLFW.GLFW_KEY_M;
            case "N": return GLFW.GLFW_KEY_N;
            case "O": return GLFW.GLFW_KEY_O;
            case "P": return GLFW.GLFW_KEY_P;
            case "Q": return GLFW.GLFW_KEY_Q;
            case "R": return GLFW.GLFW_KEY_R;
            case "S": return GLFW.GLFW_KEY_S;
            case "T": return GLFW.GLFW_KEY_T;
            case "U": return GLFW.GLFW_KEY_U;
            case "V": return GLFW.GLFW_KEY_V;
            case "W": return GLFW.GLFW_KEY_W;
            case "X": return GLFW.GLFW_KEY_X;
            case "Y": return GLFW.GLFW_KEY_Y;
            case "Z": return GLFW.GLFW_KEY_Z;
            default: return GLFW.GLFW_KEY_UNKNOWN;
        }
    }
}