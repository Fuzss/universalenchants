package fuzs.universalenchants.core;

import fuzs.extensibleenums.api.extensibleenums.v1.BuiltInEnumFactories;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class FabricAbstractions implements CommonAbstractions{

    @Override
    public boolean isArrowInfinite(LivingEntity entity, ItemStack rangedStack, ItemStack arrowStack) {
        return arrowStack.is(Items.ARROW);
    }

    @Override
    public EnchantmentCategory createEnchantmentCategory(String enumConstantName, Predicate<Item> predicate) {
        return BuiltInEnumFactories.createEnchantmentCategory(enumConstantName, predicate);
    }

    @Override
    public int getMobLootingLevel(Entity target, @Nullable Entity killer, @Nullable DamageSource cause) {
        return killer instanceof LivingEntity entity ? EnchantmentHelper.getMobLooting(entity) : 0;
    }
}
