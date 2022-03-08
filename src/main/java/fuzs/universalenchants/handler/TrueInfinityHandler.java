package fuzs.universalenchants.handler;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.mixin.accessor.LivingEntityAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrueInfinityHandler {
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

    @SubscribeEvent
    public void onLootingLevel(final LootingLevelEvent evt) {
        if (!UniversalEnchants.CONFIG.server().luckBoostsXp) return;
        if (evt.getLootingLevel() > 0) {
            this.dropExperience(evt.getEntityLiving(), evt.getLootingLevel());
        }
    }

    private void dropExperience(LivingEntity entity, int level) {
        if (entity.level instanceof ServerLevel && (((LivingEntityAccessor) entity).callIsAlwaysExperienceDropper() || ((LivingEntityAccessor) entity).getLastHurtByPlayerTime() > 0 && ((LivingEntityAccessor) entity).callShouldDropExperience() && entity.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))) {
            int reward = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(entity, ((LivingEntityAccessor) entity).getLastHurtByPlayer(), ((LivingEntityAccessor) entity).callGetExperienceReward(((LivingEntityAccessor) entity).getLastHurtByPlayer()));
            ExperienceOrb.award((ServerLevel) entity.level, entity.position(), reward * level);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(final BlockEvent.BreakEvent evt) {
        if (!UniversalEnchants.CONFIG.server().luckBoostsXp) return;
        if (evt.getExpToDrop() > 0) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, evt.getPlayer().getMainHandItem());
            if (level > 0) evt.setExpToDrop(evt.getExpToDrop() * level);
        }
    }
}
