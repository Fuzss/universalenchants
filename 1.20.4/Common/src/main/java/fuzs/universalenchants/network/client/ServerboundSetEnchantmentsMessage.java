package fuzs.universalenchants.network.client;

import fuzs.puzzleslib.api.network.v3.ServerMessageListener;
import fuzs.puzzleslib.api.network.v3.ServerboundMessage;
import fuzs.universalenchants.util.StoredEnchantmentHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public record ServerboundSetEnchantmentsMessage(int containerId,
                                                int slotIndex,
                                                Collection<Enchantment> storedEnchantments) implements ServerboundMessage<ServerboundSetEnchantmentsMessage> {
    @Override
    public ServerMessageListener<ServerboundSetEnchantmentsMessage> getHandler() {
        return new ServerMessageListener<>() {
            @Override
            public void handle(ServerboundSetEnchantmentsMessage message, MinecraftServer server, ServerGamePacketListenerImpl handler, ServerPlayer player, ServerLevel level) {
                setEnchantments(player, message.containerId, message.slotIndex, message.storedEnchantments);
            }
        };
    }

    public static void setEnchantments(Player player, int containerId, int slotIndex, Collection<Enchantment> storedEnchantments) {
        AbstractContainerMenu menu = player.containerMenu;
        if (containerId == menu.containerId) {
            if (slotIndex < menu.slots.size()) {
                Slot slot = menu.getSlot(slotIndex);
                if (slot.hasItem() && slot.allowModification(player)) {
                    ItemStack itemStack = slot.getItem();

                    Map<Enchantment, Integer> enchantmentLookup = StoredEnchantmentHelper.getAllEnchantments(itemStack);
                    Map<Enchantment, Integer> enchantmentsMap = new LinkedHashMap<>();
                    Map<Enchantment, Integer> storedEnchantmentsMap = new LinkedHashMap<>();
                    for (Map.Entry<Enchantment, Integer> entry : enchantmentLookup.entrySet()) {
                        Enchantment enchantment = entry.getKey();
                        // once again verify we don't allow curses to be stored nor incompatible enchantments to be enabled
                        if (!enchantment.isCurse() && (storedEnchantments.contains(enchantment) ||
                                !EnchantmentHelper.isEnchantmentCompatible(enchantmentsMap.keySet(), enchantment))) {
                            storedEnchantmentsMap.put(enchantment, entry.getValue());
                        } else {
                            enchantmentsMap.put(enchantment, entry.getValue());
                        }
                    }

                    EnchantmentHelper.setEnchantments(enchantmentsMap, itemStack);
                    StoredEnchantmentHelper.setStoredEnchantments(storedEnchantmentsMap, itemStack);
                    // required for the container to save
                    slot.set(itemStack);
                }
            }
        }
    }
}
