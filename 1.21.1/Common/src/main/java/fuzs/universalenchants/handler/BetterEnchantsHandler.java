package fuzs.universalenchants.handler;

import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import fuzs.puzzleslib.api.event.v1.data.DefaultedInt;
import fuzs.puzzleslib.api.event.v1.data.MutableFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

public class BetterEnchantsHandler {
    private static final int MAX_EXPERIENCE_BONUS = 50;

    public static EventResultHolder<InteractionResult> onUseItem(Player player, Level level, InteractionHand hand) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).trueInfinity) return EventResultHolder.pass();
        // true infinity for bows and crossbows
        ItemStack stack = player.getItemInHand(hand);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0) {
            if (stack.getItem() instanceof BowItem ||
                    stack.getItem() instanceof CrossbowItem && !CrossbowItem.isCharged(stack)) {
                // resetting startSoundPlayed and midLoadSoundPlayed on crossbow is not required as they're reset in CrossbowItem#onUseTick anyway
                player.startUsingItem(hand);
                // important to return success even on client since this is how the fabric event works
                return EventResultHolder.interrupt(InteractionResult.SUCCESS);
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
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).lootBonusBoostsXp) return EventResult.PASS;
        // very basic hack for multiplying xp by looting level
        // e.g. our code for looting on ranged weapons will not trigger as the damage source is not correct
        // (it will still trigger though when they ranged weapon is still in the main hand, since vanilla checks the main hand enchantments)
        // unfortunately the original damage source is not obtainable in this context
        int enchantmentLevel = CommonAbstractions.INSTANCE.getMobLootingLevel(entity,
                attackingPlayer,
                attackingPlayer != null ? entity.level().damageSources().playerAttack(attackingPlayer) : null
        );
        if (enchantmentLevel > 0) {
            droppedExperience.mapDefaultInt(value -> getDroppedXp(value, enchantmentLevel));
        }
        return EventResult.PASS;
    }

    public static void onDropExperience(ServerLevel level, BlockPos pos, BlockState state, Player player, ItemStack itemInHand, MutableInt experienceToDrop) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).lootBonusBoostsXp) return;
        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, itemInHand);
        if (enchantmentLevel > 0) {
            experienceToDrop.mapInt(value -> getDroppedXp(value, enchantmentLevel));
        }
    }

    private static int getDroppedXp(int originalExperience, int enchantmentLevel) {
        float multiplier = (enchantmentLevel * (enchantmentLevel + 1)) / 10.0F;
        return originalExperience + Math.min(MAX_EXPERIENCE_BONUS, (int) Math.floor(originalExperience * multiplier));
    }

    public static EventResult onLivingTick(LivingEntity entity) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).betterFrostWalker) return EventResult.PASS;
        if (!entity.level().isClientSide && entity.tickCount % 20 == 0) {
            int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.FROST_WALKER, entity);
            if (i > 0) {
                onEntityMoved(entity, entity.level(), entity.blockPosition(), i);
            }
        }

        return EventResult.PASS;
    }

    public static void onEntityMoved(LivingEntity livingEntity, Level level, BlockPos blockPos, int enchantmentLevel) {
        BlockState blockState = Blocks.FROSTED_ICE.defaultBlockState();
        int effectiveRadius = Math.min(16, 2 + enchantmentLevel);
        BlockPos.MutableBlockPos blockPosAbove = new BlockPos.MutableBlockPos();

        if (level.getBlockState(blockPos.below(1)).isAir()) {
            if (!level.getBlockState(blockPos.below(2)).isAir()) {
                blockPos = blockPos.below();
            } else {
                return;
            }
        }

        for (BlockPos currentBlockPos : BlockPos.betweenClosed(blockPos.offset(-effectiveRadius, -1, -effectiveRadius),
                blockPos.offset(effectiveRadius, -1, effectiveRadius)
        )) {
            if (currentBlockPos.closerToCenterThan(livingEntity.position(), effectiveRadius)) {
                blockPosAbove.set(currentBlockPos.getX(), currentBlockPos.getY() + 1, currentBlockPos.getZ());
                BlockState currentBlockState = level.getBlockState(currentBlockPos);
                if (level.getBlockState(blockPosAbove).isAir() && isFrostedIceReplaceable(level, currentBlockPos, currentBlockState)) {
                    if (blockState.canSurvive(level, currentBlockPos) &&
                            level.isUnobstructed(blockState, currentBlockPos, CollisionContext.empty())) {
                        if (currentBlockState.is(ModRegistry.FROSTED_ICE_REPLACEABLES)) {
                            level.destroyBlock(currentBlockPos, true, livingEntity);
                        }
                        level.setBlockAndUpdate(currentBlockPos, blockState);
                        level.scheduleTick(currentBlockPos,
                                Blocks.FROSTED_ICE,
                                Mth.nextInt(livingEntity.getRandom(), 60, 120)
                        );
                    }
                }
            }
        }
    }

    private static boolean isFrostedIceReplaceable(Level level, BlockPos blockPos, BlockState blockState) {
        if (blockState == FrostedIceBlock.meltsInto() || blockState.is(Blocks.FROSTED_ICE)) {
            return true;
        } else {
            return level.getFluidState(blockPos).is(FluidTags.WATER) && blockState.is(ModRegistry.FROSTED_ICE_REPLACEABLES);
        }
    }
}
