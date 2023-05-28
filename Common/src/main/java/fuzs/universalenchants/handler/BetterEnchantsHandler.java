package fuzs.universalenchants.handler;

import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.event.v1.data.DefaultedInt;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.mixin.accessor.ExperienceOrbAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BetterEnchantsHandler {

    public static EventResultHolder<InteractionResultHolder<ItemStack>> onUseItem(Player player, Level level, InteractionHand hand) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).trueInfinity) return EventResultHolder.pass();
        // true infinity for bows and crossbows
        ItemStack stack = player.getItemInHand(hand);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0) {
            if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem && !CrossbowItem.isCharged(stack)) {
                // resetting startSoundPlayed and midLoadSoundPlayed on crossbow is not required as they're reset in CrossbowItem#onUseTick anyways
                player.startUsingItem(hand);
                // important to return success even on client since this is how the fabric event works
                return EventResultHolder.interrupt(InteractionResultHolder.success(stack));
            }
        }
        return EventResultHolder.pass();
    }

    public static EventResult onLivingHurt(LivingEntity entity, DamageSource source, MutableFloat amount) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).noProjectileImmunity) return EventResult.PASS;
        // immediately reset damage immunity after being hit by any projectile, fixes multishot
        if (!(entity instanceof Player) && source.is(DamageTypeTags.IS_PROJECTILE)) {
            entity.invulnerableTime = 0;
        }
        return EventResult.PASS;
    }

    public static EventResult onFarmlandTrample(Level level, BlockPos pos, BlockState state, float fallDistance, Entity entity) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).noFarmlandTrample) return EventResult.PASS;
        if (entity instanceof LivingEntity livingEntity) {
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FALL_PROTECTION, livingEntity) > 0) {
                return EventResult.INTERRUPT;
            }
        }
        return EventResult.PASS;
    }

    public static EventResult onLivingExperienceDrop(LivingEntity entity, @Nullable Player attackingPlayer, DefaultedInt droppedExperience) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).lootingBoostsXp) return EventResult.PASS;
        // very basic hack for multiplying xp by looting level
        // e.g. our code for looting on ranged weapons will not trigger as the damage source is not correct
        // (it will still trigger though when they ranged weapon is still in the main hand, since vanilla checks the main hand enchantments)
        // unfortunately the original damage source is not obtainable in this context
        int level = CommonAbstractions.INSTANCE.getMobLootingLevel(entity, attackingPlayer, attackingPlayer != null ? entity.level.damageSources().playerAttack(attackingPlayer) : null);
        if (level > 0) {
            droppedExperience.mapDefaultInt(experience -> getDroppedXp(experience, level));
        }
        return EventResult.PASS;
    }

    private static int getDroppedXp(int droppedXp, int level) {
        float multiplier = (level * (level + 1)) / 10.0F;
        return droppedXp + Math.min(50, (int) (droppedXp * multiplier));
    }

    public static EventResult onPickupXp(Player player, ExperienceOrb orb) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).mendingCraftingRepair) return EventResult.PASS;
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
        return EventResult.INTERRUPT;
    }
}
