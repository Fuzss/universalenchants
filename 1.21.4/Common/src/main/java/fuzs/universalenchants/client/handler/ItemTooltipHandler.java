package fuzs.universalenchants.client.handler;

import fuzs.universalenchants.handler.ItemCompatHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ItemTooltipHandler {
    /**
     * Do not use {@link CommonComponents#EMPTY}, this has to be a unique instance.
     */
    static final Component PLACEHOLDER_COMPONENT = Component.empty();

    public static void onItemTooltip(ItemStack itemStack, List<Component> tooltipLines, Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag) {
        // armor enchantments are now compatible with player and animals armor slots, attributes therefore are displayed for both slot types
        // remove the attribute lines again that do not fit the type of armor
        ItemAttributeModifiers itemAttributeModifiers = itemStack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (itemAttributeModifiers != null && itemAttributeModifiers.showInTooltip()) {
            Collection<EquipmentSlotGroup> equipmentSlotGroups = getEquipmentSlotGroupsToRemove(itemStack);
            for (EquipmentSlotGroup equipmentSlotGroup : equipmentSlotGroups) {
                boolean isRemoving = false;
                for (int i = 0; i < tooltipLines.size(); i++) {
                    Component component = tooltipLines.get(i);
                    if (component.getContents() instanceof TranslatableContents contents) {
                        if (contents.getKey().equals("item.modifiers." + equipmentSlotGroup.getSerializedName())) {
                            isRemoving = true;
                            if (i - 1 >= 0 && tooltipLines.get(i - 1) == CommonComponents.EMPTY) {
                                tooltipLines.set(i - 1, PLACEHOLDER_COMPONENT);
                            }
                        } else if (!contents.getKey().startsWith("attribute.modifier.")) {
                            isRemoving = false;
                        }
                        if (isRemoving) {
                            tooltipLines.set(i, PLACEHOLDER_COMPONENT);
                        }
                    } else {
                        isRemoving = false;
                    }
                }
            }
            tooltipLines.removeIf((Component component) -> component == PLACEHOLDER_COMPONENT);
        }
    }

    private static Collection<EquipmentSlotGroup> getEquipmentSlotGroupsToRemove(ItemStack itemStack) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == EquipmentSlot.BODY) {
            return ItemCompatHandler.ARMOR_EQUIPMENT_SLOT_GROUPS;
        } else {
            return Collections.singleton(EquipmentSlotGroup.BODY);
        }
    }
}
