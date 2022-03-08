package fuzs.universalenchants.handler;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.mixin.accessor.LivingEntityAccessor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
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
                // resetting isLoadingStart and isLoadingMiddle is not required as they're reset in CrossbowItem#func_219972_a anyways
                evt.getPlayer().startUsingItem(evt.getHand());
                evt.setCancellationResult(InteractionResult.CONSUME);
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLootingLevel(final LootingLevelEvent evt) {
        if (!UniversalEnchants.CONFIG.server().lootingBoostsXp) return;
        for (int i = 0; i < evt.getLootingLevel(); i++) {
            ((LivingEntityAccessor) evt.getEntityLiving()).callDropExperience();
        }
    }
}
