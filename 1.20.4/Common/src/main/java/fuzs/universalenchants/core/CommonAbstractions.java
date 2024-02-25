package fuzs.universalenchants.core;

import fuzs.puzzleslib.api.core.v1.ServiceProviderHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.function.Predicate;

public interface CommonAbstractions {
    CommonAbstractions INSTANCE = ServiceProviderHelper.load(CommonAbstractions.class);

    boolean isArrowInfinite(LivingEntity entity, ItemStack rangedStack, ItemStack arrowStack);

    EnchantmentCategory createEnchantmentCategory(String enumConstantName, Predicate<Item> predicate);

    boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment);
}
