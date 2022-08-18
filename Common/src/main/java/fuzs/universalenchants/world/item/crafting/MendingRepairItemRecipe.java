package fuzs.universalenchants.world.item.crafting;

import com.google.common.collect.Lists;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.List;

public class MendingRepairItemRecipe extends RepairItemRecipe {

    public MendingRepairItemRecipe(ResourceLocation resourceLocation) {
        super(resourceLocation);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        if (super.matches(craftingContainer, level)) {
            return true;
        }
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).cheapMendingRepair) return false;
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

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer) {
        return super.assemble(craftingContainer);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return super.getSerializer();
    }
}
