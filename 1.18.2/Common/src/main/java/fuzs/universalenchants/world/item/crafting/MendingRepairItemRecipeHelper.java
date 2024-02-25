package fuzs.universalenchants.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

/**
 * we apply this using mixin instead of simply providing our own recipe serializer
 * since this partially has the same functionality with a different outcome from vanilla's {@link net.minecraft.world.item.crafting.RepairItemRecipe}
 */
public class MendingRepairItemRecipeHelper {

    public static boolean matches(CraftingContainer craftingContainer, Level level) {
        // vanilla's RepairItemRecipe::matches is run before this, we only check our own case
        // find if there is a single item enchanted with mending
        List<ItemStack> itemToRepair = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            if (!itemStack.isEmpty()) {
                if (itemStack.getCount() == 1 && itemStack.getItem().canBeDepleted()) {
                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, itemStack) > 0) {
                        itemToRepair.add(itemStack);
                    }
                }
            }
        }
        // check if all other items are repair materials for the enchanted item
        if (itemToRepair.size() == 1) {
            ItemStack stackToRepair = itemToRepair.get(0);
            List<ItemStack> repairMaterialItems = Lists.newArrayList();
            for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
                ItemStack itemStack = craftingContainer.getItem(i);
                if (!itemStack.isEmpty() && itemStack != stackToRepair) {
                    if (stackToRepair.getItem().isValidRepairItem(stackToRepair, itemStack)) {
                        repairMaterialItems.add(itemStack);
                    } else {
                        return false;
                    }
                }
            }
            // make sure to not consume too many repair materials
            return !repairMaterialItems.isEmpty() && repairMaterialItems.size() <= Math.ceil(stackToRepair.getDamageValue() * 4.0F / stackToRepair.getMaxDamage());
        }
        return false;
    }

    public static ItemStack assemble(CraftingContainer craftingContainer) {
        // this replaces vanilla's RepairItemRecipe::assemble
        // we search for all repairable items, if we find multiples they must all be the same item
        List<ItemStack> itemsToRepair = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            if (!itemStack.isEmpty()) {
                if (itemStack.getCount() == 1 && itemStack.getItem().canBeDepleted()) {
                    itemsToRepair.add(itemStack);
                    if (itemsToRepair.size() > 1) {
                        ItemStack itemStack2 = itemsToRepair.get(0);
                        if (!itemStack.is(itemStack2.getItem())) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
        }
        // we search for repair materials if we just found a single item
        List<ItemStack> repairMaterialItems = Lists.newArrayList();
        if (itemsToRepair.size() == 1) {
            ItemStack stackToRepair = itemsToRepair.get(0);
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stackToRepair) > 0) {
                for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
                    ItemStack itemStack = craftingContainer.getItem(i);
                    if (!itemStack.isEmpty() && itemStack != stackToRepair) {
                        if (stackToRepair.getItem().isValidRepairItem(stackToRepair, itemStack)) {
                            repairMaterialItems.add(itemStack);
                        } else {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // repair the stack, just like in an anvil (make sure to not consume too many repair materials)
                if (!repairMaterialItems.isEmpty()) {
                    stackToRepair = stackToRepair.copy();
                    if (repairMaterialItems.size() <= Math.ceil(stackToRepair.getDamageValue() * 4.0F / stackToRepair.getMaxDamage())) {
                        int repairAmount = Math.min(stackToRepair.getDamageValue(), stackToRepair.getMaxDamage() / 4);
                        if (repairAmount > 0) {
                            for (int i = 0; i < repairMaterialItems.size(); i++) {
                                stackToRepair.setDamageValue(stackToRepair.getDamageValue() - repairAmount);
                                repairAmount = Math.min(stackToRepair.getDamageValue(), stackToRepair.getMaxDamage() / 4);
                            }
                            return stackToRepair;
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        } else if (itemsToRepair.size() == 2) {
            // check if an item has mending and which one it is
            ItemStack stack = ItemStack.EMPTY;
            ItemStack otherStack = ItemStack.EMPTY;
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, itemsToRepair.get(0)) > 0) {
                stack = itemsToRepair.get(0);
                otherStack = itemsToRepair.get(1);
            } else if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, itemsToRepair.get(1)) > 0) {
                stack = itemsToRepair.get(1);
                otherStack = itemsToRepair.get(0);
            }
            // repair the mending item with anvil code, only changes durability, no changes to enchantments
            // (enchantments on the item repaired with will be lost)
            if (!stack.isEmpty()) {
                stack = stack.copy();
                int l = stack.getMaxDamage() - stack.getDamageValue();
                int m = otherStack.getMaxDamage() - otherStack.getDamageValue();
                int n = m + stack.getMaxDamage() * 12 / 100;
                int o = l + n;
                int p = stack.getMaxDamage() - o;
                if (p < 0) {
                    p = 0;
                }
                if (p < stack.getDamageValue()) {
                    stack.setDamageValue(p);
                }
                // make sure curses are copied just like vanilla
                Map<Enchantment, Integer> otherEnchantments = EnchantmentHelper.getEnchantments(otherStack).entrySet().stream().filter(e -> e.getKey().isCurse()).collect(Util.toMap());
                if (!otherEnchantments.isEmpty()) {
                    Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
                    for (Map.Entry<Enchantment, Integer> entry : otherEnchantments.entrySet()) {
                        enchantments.merge(entry.getKey(), entry.getValue(), Math::max);
                    }
                    EnchantmentHelper.setEnchantments(enchantments, stack);
                }
                return stack;
            } else {
                // no mending is present, just let the vanilla code run
                ItemStack itemStack3 = itemsToRepair.get(0);
                ItemStack itemStack = itemsToRepair.get(1);
                if (itemStack3.is(itemStack.getItem()) && itemStack3.getCount() == 1 && itemStack.getCount() == 1 && itemStack3.getItem().canBeDepleted()) {
                    Item item = itemStack3.getItem();
                    int j = item.getMaxDamage() - itemStack3.getDamageValue();
                    int k = item.getMaxDamage() - itemStack.getDamageValue();
                    int l = j + k + item.getMaxDamage() * 5 / 100;
                    int m = item.getMaxDamage() - l;
                    if (m < 0) {
                        m = 0;
                    }

                    ItemStack itemStack4 = new ItemStack(itemStack3.getItem());
                    itemStack4.setDamageValue(m);
                    Map<Enchantment, Integer> map = Maps.newHashMap();
                    Map<Enchantment, Integer> map2 = EnchantmentHelper.getEnchantments(itemStack3);
                    Map<Enchantment, Integer> map3 = EnchantmentHelper.getEnchantments(itemStack);
                    Registry.ENCHANTMENT.stream().filter(Enchantment::isCurse).forEach(enchantment -> {
                        int i = Math.max(map2.getOrDefault(enchantment, 0), map3.getOrDefault(enchantment, 0));
                        if (i > 0) {
                            map.put(enchantment, i);
                        }

                    });
                    if (!map.isEmpty()) {
                        EnchantmentHelper.setEnchantments(map, itemStack4);
                    }

                    return itemStack4;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
