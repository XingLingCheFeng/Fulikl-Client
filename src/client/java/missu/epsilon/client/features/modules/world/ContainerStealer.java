package missu.epsilon.client.features.modules.world;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import missu.epsilon.client.event.events.player.UpdateEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.EnumAutoDisableType;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.features.value.impl.ListValue;
import missu.epsilon.client.features.value.impl.MultiBoolValue;
import missu.epsilon.client.features.value.impl.NumberValue;
import missu.epsilon.client.utils.entity.InventoryUtils;
import missu.epsilon.client.utils.miscs.TimerUtils;
import missu.epsilon.client.utils.network.ServerUtils;
import missu.epsilon.mixin.screen.AbstractFurnaceScreenHandlerAccessor;
import missu.epsilon.mixin.screen.BrewingStandScreenHandlerAccessor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static missu.epsilon.client.utils.Wrapper.mc;

/**
 * Author Daniel
 * Date 251124
 */

@ModuleInfo(name = "ContainerStealer",category = ModuleCategory.WORLD,description = "Automatically steal item in container", autoDisable = EnumAutoDisableType.GAME_END)
public class ContainerStealer extends Module {
    public static ListValue pickMode = new ListValue("PickMode",new String[]{"ClickSlot","Packet"},"ClickSlot");
    public static NumberValue openDelay = new NumberValue("OpenDelay",50,0,1000,10);
    public static NumberValue stealDelay = new NumberValue("StealDelay",0,0,1000,10);
    public static NumberValue closeDelay = new NumberValue("CloseDelay",50,0,1000,10);
    public static MultiBoolValue container = new MultiBoolValue("Interactive Container",new BoolValue[]{
            new BoolValue("Chest",true),
            new BoolValue("Furnace",false),
            new BoolValue("BlastFurnace",false),
            new BoolValue("SmokerFurnace", false),
            new BoolValue("BrewingStand", false)
    });
    public static BoolValue cancelContainerGui = new BoolValue("CancelContainerGui",true);
    public static BoolValue randomiseTakingItem = new BoolValue("RandomiseTakingItem",false);
    public static BoolValue filter = new BoolValue("FilterUselessItem",false);
    public static MultiBoolValue customisedItems = (MultiBoolValue) new MultiBoolValue("CustomisedItems", new BoolValue[]{
            new BoolValue("Slime Ball", false),
            new BoolValue("Fire Charge", false),
            new BoolValue("Totem Of Undying", false),
            new BoolValue("Golden Apple", false),
            new BoolValue("Enchanted Golden Apple", false),
            new BoolValue("Fishing Rod", false),
            new BoolValue("Snow Ball", false),
            new BoolValue("Egg", false),
            new BoolValue("Coal", false),
            new BoolValue("Iron Ingot", false),
            new BoolValue("Gold Ingot", false),
            new BoolValue("Diamond", false),
            new BoolValue("Emerald", false),
            new BoolValue("Nether Star", false),
            new BoolValue("Bucket", false),
            new BoolValue("Water Bucket", false),
            new BoolValue("Lava Bucket", false),
            new BoolValue("Milk Bucket", false),
            new BoolValue("Ender Pearl", false),
            new BoolValue("Arrow", false),
            new BoolValue("Shears", false),
            new BoolValue("Compass", false),
            new BoolValue("Stick", false),
            new BoolValue("Experience Bottle", false),
            new BoolValue("Elytra", false),
            new BoolValue("Book", false),
            new BoolValue("Enchanted Book", false),
            new BoolValue("Shield", false),
            new BoolValue("End Crystal", false),
            new BoolValue("Consumable", false)
    }).displayable(() -> filter.get());

    public static BoolValue disableInLobby = new BoolValue("DisableInLobby",false);

    public static List<Integer> slots = new ArrayList<>();
    public static TimerUtils openTimer = new TimerUtils();
    public static TimerUtils stealTimer = new TimerUtils();
    public static TimerUtils closeTimer = new TimerUtils();
    public static boolean stealing = false;
    public static boolean opened = false;
    public static boolean randomised;
    public static boolean canSteal;

    @Override
    public void onEnable() {
        opened = false;
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        canSteal = false;
        if (disableInLobby.get() && ServerUtils.isInLobby()){
            return;
        }

        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        if (!isVanillaChest(mc.currentScreen)) return;

        if (inScreenHandler(screenHandler)) {
            if (!opened) {
                openTimer.reset();
                stealTimer.reset();
                closeTimer.reset();
                randomised = false;
                opened = true;
                stealing = true;
                return;
            }

            if (!openTimer.hasTimeElapsed(openDelay.get())) {
                return;
            }

            Inventory inventory;
            switch (screenHandler) {
                case GenericContainerScreenHandler handler -> inventory = handler.getInventory();
                case AbstractFurnaceScreenHandler handler -> inventory = ((AbstractFurnaceScreenHandlerAccessor) handler).getInventory();
                case BrewingStandScreenHandler handler -> inventory = ((BrewingStandScreenHandlerAccessor) handler).getInventory();
                default -> {
                    return;
                }
            }

            if (!randomised || !randomiseTakingItem.get()) {
                slots.clear();
                for (int slot = 0; slot < inventory.size(); slot++) {
                    slots.add(slot);
                }
            }

            if (randomiseTakingItem.get() && !randomised) {
                Collections.shuffle(slots);
                randomised = true;
            }

            for (int slotID = 0; slotID < inventory.size(); slotID++) {
                Slot slot = screenHandler.getSlot(slotID);

                if (!slot.hasStack()) {
                    continue;
                }

                if (!InventoryUtils.isContainerUsefulItem(slot.getStack()) && filter.get()) {
                    continue;
                }

                if (InventoryUtils.playerInventoryHasEmptySlot()) {
                    canSteal = true;
                }
            }

            if (canSteal) {
                takeItems(screenHandler);
                closeTimer.reset();
            }

            if (!canSteal) {
                if (closeTimer.hasTimeElapsed(closeDelay.get()) || closeDelay.get() == 0) {
                    if (mc.player.currentScreenHandler != null) {
                        mc.player.closeHandledScreen();
                        stealing = false;
                    }
                }
            }
            return;
        } else {
            randomised = false;
        }

        opened = false;
        openTimer.reset();
        stealTimer.reset();
        closeTimer.reset();
    }

    private static void takeItems(ScreenHandler screenHandler) {
        if (mc.interactionManager == null || mc.player == null || mc.getNetworkHandler() == null ) return;

        int delay = stealDelay.get().intValue();

        Inventory inventory;
        switch (screenHandler) {
            case GenericContainerScreenHandler handler -> inventory = handler.getInventory();
            case AbstractFurnaceScreenHandler handler -> inventory = ((AbstractFurnaceScreenHandlerAccessor) handler).getInventory();
            case BrewingStandScreenHandler handler -> inventory = ((BrewingStandScreenHandlerAccessor) handler).getInventory();
            default -> {
                return;
            }
        }

        for (int slotID = 0; slotID < inventory.size(); slotID++) {
            int currentSlot = slots.get(slotID);

            Slot slot = screenHandler.getSlot(currentSlot);

            if (!slot.hasStack()) {
                continue;
            }

            if (!InventoryUtils.isContainerUsefulItem(slot.getStack()) && filter.get()) {
                continue;
            }

            if (InventoryUtils.playerInventoryHasEmptySlot()) {
                if (stealTimer.hasTimeElapsed(delay) || delay == 0) {
                    Int2ObjectMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<>();

                    switch (pickMode.get()) {
                        case "ClickSlot":
                            mc.interactionManager.clickSlot(screenHandler.syncId, currentSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                            break;
                        case "Packet":
                            screenHandler.onSlotClick(currentSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                            mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(screenHandler.syncId, screenHandler.getRevision(), currentSlot, 0, SlotActionType.QUICK_MOVE, screenHandler.getCursorStack().copy(), int2ObjectMap));
                            break;
                    }
                    stealTimer.reset();
                }
            }
        }
    }

    public static boolean inScreenHandler(ScreenHandler screenHandler) {
        if (container.get("Chest") && screenHandler instanceof GenericContainerScreenHandler handle) {
            return true;
        }
        if (container.get("Furnace") && screenHandler instanceof FurnaceScreenHandler) {
            return true;
        }
        if (container.get("BlastFurnace") && screenHandler instanceof BlastFurnaceScreenHandler) {
            return true;
        }
        if (container.get("SmokerFurnace") && screenHandler instanceof SmokerScreenHandler) {
            return true;
        }
        return container.get("BrewingStand") && screenHandler instanceof BrewingStandScreenHandler;
    }



    public static boolean isVanillaChest(Screen scr){
        if (scr == null) return false;
        Text titleText = scr.getTitle();
        String formattedName = titleText.getString().toLowerCase().trim();
        String vanillaChestName = Text.translatable("container.chest").getString().toLowerCase().trim();
        return formattedName.equals(vanillaChestName)
                || formattedName.equalsIgnoreCase("low")
                || formattedName.equalsIgnoreCase("chest");
    }
}
