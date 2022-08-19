package fuzs.universalenchants.handler;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.core.ModServices;
import fuzs.universalenchants.mixin.accessor.ExperienceOrbAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.OptionalInt;

public class BetterEnchantsHandler {

    public InteractionResultHolder<ItemStack> onArrowNock(Player player, ItemStack stack, Level level, InteractionHand hand) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).trueInfinity) return InteractionResultHolder.pass(ItemStack.EMPTY);
        // true infinity for bows
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0) {
            player.startUsingItem(hand);
            // important to return success even on client since this is how the fabric event works
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(ItemStack.EMPTY);
    }

    public InteractionResultHolder<ItemStack> onRightClickItem(Player player, Level level, InteractionHand hand) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).trueInfinity) return InteractionResultHolder.pass(ItemStack.EMPTY);
        // true infinity for crossbows
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof CrossbowItem && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0) {
            if (!CrossbowItem.isCharged(stack)) {
                // resetting startSoundPlayed and midLoadSoundPlayed is not required as they're reset in CrossbowItem#onUseTick anyways
                player.startUsingItem(hand);
                // important to return success even on client since this is how the fabric event works
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(ItemStack.EMPTY);
    }

    public Optional<Unit> onLivingHurt(LivingEntity entity, DamageSource source, float amount) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).noProjectileImmunity) return Optional.empty();
        // immediately reset damage immunity after being hit by any projectile, fixes multishot
        if (!(entity instanceof Player) && source.isProjectile()) {
            entity.invulnerableTime = 0;
        }
        return Optional.empty();
    }

    public Optional<Unit> onFarmlandTrample(Level level, BlockPos pos, BlockState state, float fallDistance, Entity entity) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).noFarmlandTrample) return Optional.empty();
        if (entity instanceof LivingEntity livingEntity) {
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FALL_PROTECTION, livingEntity) > 0) {
                return Optional.of(Unit.INSTANCE);
            }
        }
        return Optional.empty();
    }

    public OptionalInt onLivingExperienceDrop(LivingEntity target, @Nullable Player killer, int originalExperience, int droppedExperience) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).lootingBoostsXp) return OptionalInt.empty();
        // very basic hack for multiplying xp by looting level
        // e.g. our code for looting on ranged weapons will not trigger as the damage source is not correct
        // (it will still trigger though when they ranged weapon is still in the main hand, since vanilla checks the main hand enchantments)
        // unfortunately the original damage source is not obtainable in this context
        int level = ModServices.ABSTRACTIONS.getMobLootingLevel(target, killer, killer != null ? DamageSource.playerAttack(killer) : null);
        if (level > 0) return OptionalInt.of(this.getDroppedXp(droppedExperience, level));
        return OptionalInt.empty();
    }

    private int getDroppedXp(int droppedXp, int level) {
        float multiplier = (level * (level + 1)) / 10.0F;
        return droppedXp + Math.min(50, (int) (droppedXp * multiplier));
    }

    public Optional<Unit> onPickupXp(Player player, ExperienceOrb orb) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).mendingCraftingRepair) return Optional.empty();
        player.takeXpDelay = 2;
        player.take(orb, 1);
        if (orb.getValue() > 0) {
            player.giveExperiencePoints(orb.getValue());
        }
        int count = ((ExperienceOrbAccessor) orb).getCount() - 1;
        ((ExperienceOrbAccessor) orb).setCount(count);
        if (count == 0) {
            orb.discard();
        }
        return Optional.of(Unit.INSTANCE);
    }
}
