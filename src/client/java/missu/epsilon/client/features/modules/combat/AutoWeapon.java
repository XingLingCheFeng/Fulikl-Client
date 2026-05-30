package missu.epsilon.client.features.modules.combat;

import missu.epsilon.client.Client;
import missu.epsilon.client.event.events.game.AttackEvent;
import missu.epsilon.client.event.events.network.PacketEvent;
import missu.epsilon.client.event.impl.CancellableEvent;
import missu.epsilon.client.event.impl.EventTarget;
import missu.epsilon.client.features.Module;
import missu.epsilon.client.features.ModuleCategory;
import missu.epsilon.client.features.ModuleInfo;
import missu.epsilon.client.features.modules.player.AutoTool;
import missu.epsilon.client.features.modules.world.BedBreaker;
import missu.epsilon.client.features.value.impl.BoolValue;
import missu.epsilon.client.utils.entity.InventoryUtils;
import missu.epsilon.client.utils.entity.ItemSpoofUtils;
import missu.epsilon.client.utils.packets.PacketUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;

import static missu.epsilon.client.utils.Wrapper.mc;
import static net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.ATTACK;

/**
 * Author Daniel
 */

@ModuleInfo(name = "AutoWeapon", description = "Automatically switch best weapon to your slot", category = ModuleCategory.COMBAT)
public class AutoWeapon extends Module {
    public static BoolValue onlySword = new BoolValue("OnlySword", true);

    private boolean attackEnemy = false;
    public static LinkedBlockingDeque<UpdateSelectedSlotC2SPacket> packets = new LinkedBlockingDeque<>();

    private record WeaponSlot(int slot, float damage) {
    }

    @SuppressWarnings("unused")
    @EventTarget
    public void onAttack(AttackEvent event) {
        attackEnemy = true;
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.packetState == CancellableEvent.PacketState.RECEIVE || mc.player == null) return;

        if (event.packet instanceof PlayerInteractEntityC2SPacket packet && packet.type == ATTACK && attackEnemy) {
            attackEnemy = false;

            WeaponSlot bestWeapon = findBestWeapon();
            if (bestWeapon == null) {
                return;
            }

            int currentSlot = mc.player.getInventory().selectedSlot;
            float currentDamage = InventoryUtils.getAttackDamage(mc.player.getInventory().getStack(currentSlot));

            if (currentDamage >= bestWeapon.damage) {
                return;
            }

            int slot = bestWeapon.slot;
            if (slot == currentSlot || (Client.moduleManager.getModule(BedBreaker.class).getState() && BedBreaker.breakingBlockPos != null && slot == ItemSpoofUtils.originalSlot && Client.moduleManager.getModule(AutoTool.class).getState() && AutoTool.spoof.get())) {
                return;
            }

            if (Client.moduleManager.getModule(BedBreaker.class).getState() && BedBreaker.breakingBlockPos != null && Client.moduleManager.getModule(AutoTool.class).getState() && AutoTool.spoof.get()) {
                ItemSpoofUtils.originalSlot = slot;
            } else {
                mc.player.getInventory().selectedSlot = slot;
            }
            PacketUtils.sendPacketNoEvent(packet);
            event.cancelEvent();
        }
    }

    private WeaponSlot findBestWeapon() {
        WeaponSlot bestWeapon = null;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = Objects.requireNonNull(mc.player).getInventory().getStack(i);

            if (onlySword.getValue() && !(stack.getItem() instanceof SwordItem)) {
                continue;
            }

            float damage = InventoryUtils.getAttackDamage(stack);

            if (bestWeapon == null || damage > bestWeapon.damage) {
                bestWeapon = new WeaponSlot(i, damage);
            }
        }

        return bestWeapon;
    }
}
