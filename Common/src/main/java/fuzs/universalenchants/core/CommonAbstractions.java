package fuzs.universalenchants.core;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface CommonAbstractions {

    boolean isArrowInfinite(LivingEntity entity, ItemStack rangedStack, ItemStack arrowStack);

    EnchantmentCategory createEnchantmentCategory(String enumConstantName, Predicate<Item> predicate);

    int getMobLootingLevel(Entity target, @Nullable Entity killer, @Nullable DamageSource cause);
}
