package fuzs.universalenchants.handler;

import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.mixin.accessor.AbstractArrowAccessor;
import fuzs.universalenchants.registry.ModRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class ItemCompatHandler {
    @SubscribeEvent
    public void onArrowLoose(final ArrowLooseEvent evt) {
        // multishot enchantment for bows
        ItemStack stack = evt.getBow();
        if (evt.hasAmmo() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack) > 0) {
            float velocity = BowItem.getPowerForTime(evt.getCharge());
            if (velocity >= 0.1F) {
                Level level = evt.getWorld();
                Player player = evt.getPlayer();
                if (!level.isClientSide) {
                    ItemStack itemstack = player.getProjectile(stack);
                    ArrowItem arrowitem = (ArrowItem) (itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
                    float[] shotPitches = getShotPitches(level.random, velocity);
                    for (int i = 0; i < 2; i++) {
                        AbstractArrow abstractarrow = arrowitem.createArrow(level, itemstack, player);
                        if (stack.getItem() instanceof BowItem bow) {
                            abstractarrow = bow.customArrow(abstractarrow);
                        }
                        abstractarrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity * 3.0F, 1.5F);
                        applyPowerEnchantment(abstractarrow, stack);
                        applyPunchEnchantment(abstractarrow, stack);
                        applyFlameEnchantment(abstractarrow, stack);
                        applyPiercingEnchantment(abstractarrow, stack);
                        applyLootingEnchantment(abstractarrow, stack);
                        abstractarrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        level.addFreshEntity(abstractarrow);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, shotPitches[i + 1]);
                    }
                }
            }
        }
    }

    private static float[] getShotPitches(Random random, float velocity) {
        boolean flag = random.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(flag, random, velocity), getRandomShotPitch(!flag, random, velocity)};
    }

    private static float getRandomShotPitch(boolean p_150798_, Random random, float velocity) {
        float f = p_150798_ ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f * velocity;
    }

    @SubscribeEvent
    public void onItemUseTick(final LivingEntityUseItemEvent.Tick evt) {
        Item item = evt.getItem().getItem();
        int duration = evt.getItem().getUseDuration() - evt.getDuration();
        if (item instanceof BowItem && duration < 20 || item instanceof TridentItem && duration < 10) {
            // quick charge enchantment for bows and tridents
            int quickChargeLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, evt.getItem());
            evt.setDuration(evt.getDuration() - quickChargeLevel);
        }
    }

    @SubscribeEvent
    public void onLootingLevel(final LootingLevelEvent evt) {
        if (evt.getDamageSource() == null) return;
        Entity source = evt.getDamageSource().getDirectEntity();
        if (source instanceof AbstractArrow) {
            // the whole trident stack is saved anyways, so use it
            if (source instanceof ThrownTrident) {
                ItemStack stack = ((AbstractArrowAccessor) source).callGetPickupItem();
                int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, stack);
                if (level > 0) evt.setLootingLevel(level);
            } else {
                // overwrite anything set by vanilla, even when enchantment is not present (since the whole holding a looting sword and getting looting applied to ranged kills doesn't make a lot of sense)
                evt.setLootingLevel(source.getCapability(ModRegistry.ARROW_LOOTING_CAPABILITY).map(ArrowLootingCapability::getLevel).orElse((byte) 0));
            }
        }
    }

    public static void applyPowerEnchantment(AbstractArrow arrow, ItemStack stack) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerLevel * 0.5 + 0.5);
        }
    }

    public static void applyPunchEnchantment(AbstractArrow arrow, ItemStack stack) {
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            arrow.setKnockback(punchLevel);
        }
    }

    public static void applyFlameEnchantment(AbstractArrow arrow, ItemStack stack) {
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrow.setSecondsOnFire(100);
        }
    }

    public static void applyPiercingEnchantment(AbstractArrow arrow, ItemStack stack) {
        int pierceLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, stack);
        if (pierceLevel > 0) {
            arrow.setPierceLevel((byte) pierceLevel);
        }
    }

    public static void applyLootingEnchantment(AbstractArrow arrow, ItemStack stack) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, stack);
        if (level > 0) {
            arrow.getCapability(ModRegistry.ARROW_LOOTING_CAPABILITY).ifPresent(capability -> capability.setLevel((byte) level));
        }
    }
}
