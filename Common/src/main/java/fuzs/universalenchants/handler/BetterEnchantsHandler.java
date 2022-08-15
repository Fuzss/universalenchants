package fuzs.universalenchants.handler;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.OptionalInt;

public class BetterEnchantsHandler {

    @SubscribeEvent
    public void onArrowNock(final ArrowNockEvent evt) {
        if (!UniversalEnchants.CONFIG.server().trueInfinity) return;
        // true infinity for bows
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, evt.getBow()) > 0) {
            evt.getPlayer().startUsingItem(evt.getHand());
            evt.setAction(InteractionResultHolder.consume(evt.getBow()));
        }
    }

    @SubscribeEvent
    public void onRightClickItem(final PlayerInteractEvent.RightClickItem evt) {
        if (!UniversalEnchants.CONFIG.server().trueInfinity) return;
        // true infinity for crossbows
        ItemStack stack = evt.getPlayer().getItemInHand(evt.getHand());
        if (stack.getItem() instanceof CrossbowItem && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0) {
            if (!CrossbowItem.isCharged(stack)) {
                // resetting startSoundPlayed and midLoadSoundPlayed is not required as they're reset in CrossbowItem#func_219972_a anyways
                evt.getPlayer().startUsingItem(evt.getHand());
                evt.setCancellationResult(InteractionResult.CONSUME);
                evt.setCanceled(true);
            }
        }
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

    @SubscribeEvent
    public void onLivingExperienceDrop(final LivingExperienceDropEvent evt) {
        if (!UniversalEnchants.CONFIG.server().lootingBoostsXp) return;
        // very basic hack for multiplying xp by looting level
        // e.g. our code for looting on ranged weapons will not trigger as the damage source is not correct
        // (it will still trigger though when they ranged weapon is still in the main hand, since vanilla checks the main hand enchantments)
        // unfortunately the original damage source is not obtainable in this context
        Player lastHurtByPlayer = evt.getAttackingPlayer();
        int level = net.minecraftforge.common.ForgeHooks.getLootingLevel(evt.getEntityLiving(), lastHurtByPlayer, lastHurtByPlayer != null ? DamageSource.playerAttack(lastHurtByPlayer) : null);
        if (level > 0) evt.setDroppedExperience(this.getDroppedXp(evt.getDroppedExperience(), level));
    }

    public OptionalInt onLivingExperienceDrop(LivingEntity entity, @Nullable Player attackingPlayer, int originalExperience, int droppedExperience) {
        if (!UniversalEnchants.CONFIG.get(ServerConfig.class).lootingBoostsXp) return OptionalInt.empty();
        // very basic hack for multiplying xp by looting level
        // e.g. our code for looting on ranged weapons will not trigger as the damage source is not correct
        // (it will still trigger though when they ranged weapon is still in the main hand, since vanilla checks the main hand enchantments)
        // unfortunately the original damage source is not obtainable in this context
        int level = attackingPlayer != null ? EnchantmentHelper.getMobLooting(entity) : 0;
        if (level > 0) return OptionalInt.of(this.getDroppedXp(droppedExperience, level));
        return OptionalInt.empty();
    }

    private int getDroppedXp(int droppedXp, int level) {
        float multiplier = (level * (level + 1)) / 10.0F;
        return droppedXp + Math.min(50, (int) (droppedXp * multiplier));
    }
}
