package fuzs.universalenchants.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.function.Predicate;

public class ForgeAbstractions implements CommonAbstractions {

    @Override
    public boolean isArrowInfinite(LivingEntity entity, ItemStack rangedStack, ItemStack arrowStack) {
        return arrowStack.getItem() instanceof ArrowItem item && entity instanceof Player player && item.isInfinite(arrowStack, rangedStack, player);
    }

    @Override
    public EnchantmentCategory createEnchantmentCategory(String enumConstantName, Predicate<Item> predicate) {
        return EnchantmentCategory.create(enumConstantName, predicate);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return stack.canApplyAtEnchantingTable(enchantment);
    }
}
