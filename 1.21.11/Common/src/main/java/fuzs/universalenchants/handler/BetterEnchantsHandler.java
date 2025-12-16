package fuzs.universalenchants.handler;

import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import fuzs.puzzleslib.api.item.v2.EnchantingHelper;
import fuzs.puzzleslib.api.network.v4.MessageSender;
import fuzs.puzzleslib.api.network.v4.PlayerSet;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.network.ClientboundStopUsingItemMessage;
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
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class BetterEnchantsHandler {

    public static void onPickProjectile(LivingEntity livingEntity, ItemStack weaponItemStack, MutableValue<ItemStack> projectileItemStack) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).trueInfinity) {
            return;
        }

        if (livingEntity.level() instanceof ServerLevel serverLevel && projectileItemStack.get().isEmpty()) {
            ItemStack itemStack = new ItemStack(Items.ARROW);
            if (EnchantmentHelper.processAmmoUse(serverLevel, weaponItemStack, itemStack, 1) == 0) {
                projectileItemStack.accept(itemStack);
                // It is not possible for the client to know if a projectile weapon has infinity, so it will start using the offhand item when possible (like a shield).
                // The local player locks that used item in place, so even when the server correctly starts using the projectile weapon, the client keeps on using the wrong offhand item.
                // To enable the server to override the wrong use item again, the locked local player fields must be manually reset.
                if (livingEntity.getMainHandItem() == weaponItemStack && !livingEntity.getOffhandItem().isEmpty()) {
                    MessageSender.broadcast(PlayerSet.ofEntity(livingEntity), new ClientboundStopUsingItemMessage());
                }
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

    public static EventResult onFarmlandTrample(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, double fallDistance, Entity entity) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).noFarmlandTrample) return EventResult.PASS;
        if (entity instanceof LivingEntity livingEntity) {
            Holder<Enchantment> enchantment = EnchantingHelper.lookup(livingEntity, Enchantments.FEATHER_FALLING);
            if (EnchantmentHelper.getEnchantmentLevel(enchantment, livingEntity) > 0) {
                return EventResult.INTERRUPT;
            }
        }

        return EventResult.PASS;
    }

    public static EventResult onLivingExperienceDrop(LivingEntity entity, @Nullable Player attackingPlayer, MutableInt droppedExperience) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).lootBonusBoostsXp) return EventResult.PASS;
        // very basic hack for multiplying xp by looting level
        // e.g. our code for looting on ranged weapons will not trigger as the damage source is not correct
        // (it will still trigger though when the ranged weapon is still in the main hand, since vanilla checks the main hand enchantments)
        // unfortunately the original damage source is not obtainable in this context
        if (attackingPlayer != null) {
            int enchantmentLevel = EnchantingHelper.getEnchantmentLevel(Enchantments.LOOTING, attackingPlayer);
            if (enchantmentLevel > 0) {
                droppedExperience.mapAsInt((int value) -> getDroppedXp(value, enchantmentLevel));
            }
        }

        return EventResult.PASS;
    }

    public static void onDropExperience(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, Player player, ItemStack itemInHand, MutableInt experienceToDrop) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).lootBonusBoostsXp) return;
        Holder<Enchantment> enchantment = EnchantingHelper.lookup(serverLevel, Enchantments.FORTUNE);
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(enchantment, itemInHand);
        if (enchantmentLevel > 0) {
            experienceToDrop.mapAsInt((int value) -> getDroppedXp(value, enchantmentLevel));
        }
    }

    private static int getDroppedXp(int originalExperience, int enchantmentLevel) {
        float experienceMultiplier = (enchantmentLevel * (enchantmentLevel + 1)) / 10.0F;
        return originalExperience + Math.min(50, (int) Math.floor(originalExperience * experienceMultiplier));
    }
}
