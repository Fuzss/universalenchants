package fuzs.universalenchants.core;

import fuzs.extensibleenums.core.EnumFactories;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.function.Predicate;

public class FabricAbstractions implements CommonAbstractions{

    @Override
    public boolean isArrowInfinite(LivingEntity entity, ItemStack rangedStack, ItemStack arrowStack) {
        return arrowStack.is(Items.ARROW);
    }

    @Override
    public EnchantmentCategory createEnchantmentCategory(String enumConstantName, Predicate<Item> predicate) {
        return EnumFactories.createEnchantmentCategory(enumConstantName, predicate);
    }
}
