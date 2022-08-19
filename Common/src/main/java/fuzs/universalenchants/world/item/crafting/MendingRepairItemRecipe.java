package fuzs.universalenchants.world.item.crafting;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public class MendingRepairItemRecipe extends CustomRecipe {

    public MendingRepairItemRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        if (vanilla(craftingContainer)) {
            return true;
        }
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).easyMendingRepair) return false;
        List<ItemStack> list = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            if (!itemStack.isEmpty()) {
                if (itemStack.getCount() == 1 && itemStack.getItem().canBeDepleted()) {
                    if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, itemStack) > 0) {
                        list.add(itemStack);
                    }
                }
            }
        }
        if (list.size() == 1) {
            ItemStack itemStack1 = list.get(0);
            List<ItemStack> list2 = Lists.newArrayList();
            for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
                ItemStack itemStack = craftingContainer.getItem(i);
                if (!itemStack.isEmpty() && itemStack != itemStack1) {
                    if (itemStack1.getItem().isValidRepairItem(itemStack1, itemStack)) {
                        list2.add(itemStack);
                    } else {
                        return false;
                    }
                }
            }
            return !list2.isEmpty();
        }
        return false;
    }

    private static boolean vanilla(CraftingContainer craftingContainer) {
        List<ItemStack> list = Lists.<ItemStack>newArrayList();

        for(int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            if (!itemStack.isEmpty()) {
                list.add(itemStack);
                if (list.size() > 1) {
                    ItemStack itemStack2 = (ItemStack)list.get(0);
                    if (!itemStack.is(itemStack2.getItem()) || itemStack2.getCount() != 1 || itemStack.getCount() != 1 || !itemStack2.getItem().canBeDepleted()) {
                        return false;
                    }
                }
            }
        }

        return list.size() == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        List<ItemStack> repairItems = Lists.newArrayList();
        for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
            ItemStack itemStack = craftingContainer.getItem(i);
            if (!itemStack.isEmpty()) {
                if (itemStack.getCount() == 1 && itemStack.getItem().canBeDepleted()) {
                    repairItems.add(itemStack);
                    if (repairItems.size() > 1) {
                        ItemStack itemStack2 = repairItems.get(0);
                        if (!itemStack.is(itemStack2.getItem())) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
        }

        List<ItemStack> materialItems = Lists.newArrayList();
        if (repairItems.size() == 1 && UniversalEnchants.CONFIG.get(ServerConfig.class).easyMendingRepair) {
            ItemStack stackToRepair = repairItems.get(0);
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, stackToRepair) > 0) {
                for (int i = 0; i < craftingContainer.getContainerSize(); ++i) {
                    ItemStack itemStack = craftingContainer.getItem(i);
                    if (!itemStack.isEmpty() && itemStack != stackToRepair) {
                        if (stackToRepair.getItem().isValidRepairItem(stackToRepair, itemStack)) {
                            materialItems.add(itemStack);
                        } else {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                if (!materialItems.isEmpty()) {
                    stackToRepair = stackToRepair.copy();
                    if (materialItems.size() <= Math.ceil(stackToRepair.getDamageValue() * 4.0F / stackToRepair.getMaxDamage())) {
                        int repairAmount = Math.min(stackToRepair.getDamageValue(), stackToRepair.getMaxDamage() / 4);
                        if (repairAmount > 0) {
                            for (int i = 0; i < materialItems.size(); i++) {
                                stackToRepair.setDamageValue(stackToRepair.getDamageValue() - repairAmount);
                                repairAmount = Math.min(stackToRepair.getDamageValue(), stackToRepair.getMaxDamage() / 4);
                            }
                            return stackToRepair;
                        }
                    }
                }
            }
            return ItemStack.EMPTY;
        } else if (repairItems.size() == 2) {
            ItemStack stack = ItemStack.EMPTY;
            ItemStack otherStack = ItemStack.EMPTY;
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, repairItems.get(0)) > 0) {
                stack = repairItems.get(0);
                otherStack = repairItems.get(1);
            } else if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MENDING, repairItems.get(1)) > 0) {
                stack = repairItems.get(1);
                otherStack = repairItems.get(0);
            }
            if (!stack.isEmpty() && UniversalEnchants.CONFIG.get(ServerConfig.class).easyMendingRepair) {
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
                return stack;
            } else {
                ItemStack itemStack3 = repairItems.get(0);
                ItemStack itemStack = repairItems.get(1);
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

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistry.REPAIR_MENDING_ITEM_RECIPE_SERIALIZER.get();
    }
}
