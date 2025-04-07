package fuzs.universalenchants.handler;

import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedInt;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import fuzs.puzzleslib.api.init.v3.registry.LookupHelper;
import fuzs.puzzleslib.api.item.v2.EnchantingHelper;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BetterEnchantsHandler {

    public static void onGetProjectile(LivingEntity livingEntity, ItemStack weaponItemStack, MutableValue<ItemStack> projectileItemStack) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).trueInfinity) return;
        if (livingEntity.level() instanceof ServerLevel serverLevel && projectileItemStack.get().isEmpty()) {
            ItemStack itemStack = new ItemStack(Items.ARROW);
            if (EnchantmentHelper.processAmmoUse(serverLevel, weaponItemStack, itemStack, 1) == 0) {
                projectileItemStack.accept(itemStack);
            }
        }
    }

    public static EventResult onLivingHurt(LivingEntity entity, DamageSource source, MutableFloat amount) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).noProjectileImmunity) return EventResult.PASS;
        // immediately reset damage immunity after being hit by any projectile, fixes multishot
        if (!(entity instanceof Player) && source.is(DamageTypeTags.IS_PROJECTILE)) {
            entity.invulnerableTime = 0;
        }
        return EventResult.PASS;
    }

    public static EventResult onFarmlandTrample(Level level, BlockPos blockPos, BlockState blockState, double fallDistance, Entity entity) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).noFarmlandTrample) return EventResult.PASS;
        if (entity instanceof LivingEntity livingEntity) {
            Holder<Enchantment> enchantment = LookupHelper.lookupEnchantment(livingEntity,
                    Enchantments.FEATHER_FALLING);
            if (EnchantmentHelper.getEnchantmentLevel(enchantment, livingEntity) > 0) {
                return EventResult.INTERRUPT;
            }
        }
        return EventResult.PASS;
    }

    public static EventResult onLivingExperienceDrop(LivingEntity entity, @Nullable Player attackingPlayer, DefaultedInt droppedExperience) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).lootBonusBoostsXp) return EventResult.PASS;
        // very basic hack for multiplying xp by looting level
        // e.g. our code for looting on ranged weapons will not trigger as the damage source is not correct
        // (it will still trigger though when they ranged weapon is still in the main hand, since vanilla checks the main hand enchantments)
        // unfortunately the original damage source is not obtainable in this context
        int enchantmentLevel = EnchantingHelper.getMobLootingLevel(entity,
                attackingPlayer,
                attackingPlayer != null ? entity.level().damageSources().playerAttack(attackingPlayer) : null);
        if (enchantmentLevel > 0) {
            droppedExperience.mapDefaultInt(value -> getDroppedXp(value, enchantmentLevel));
        }
        return EventResult.PASS;
    }

    public static void onDropExperience(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, Player player, ItemStack itemInHand, MutableInt experienceToDrop) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).lootBonusBoostsXp) return;
        Holder<Enchantment> enchantment = LookupHelper.lookupEnchantment(serverLevel, Enchantments.FORTUNE);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemInHand);
        if (enchantmentLevel > 0) {
            experienceToDrop.mapInt(value -> getDroppedXp(value, enchantmentLevel));
        }
    }

    private static int getDroppedXp(int originalExperience, int enchantmentLevel) {
        float experienceMultiplier = (enchantmentLevel * (enchantmentLevel + 1)) / 10.0F;
        return originalExperience + Math.min(50, (int) Math.floor(originalExperience * experienceMultiplier));
    }
}
