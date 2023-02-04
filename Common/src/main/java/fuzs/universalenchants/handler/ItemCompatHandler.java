package fuzs.universalenchants.handler;

import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.init.ModRegistry;
import fuzs.universalenchants.mixin.accessor.AbstractArrowAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.OptionalInt;

public class ItemCompatHandler {

    public void onArrowLoose(Player player, ItemStack stack, Level level, int charge, boolean hasAmmo) {
        // multishot enchantment for bows
        if (hasAmmo && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack) > 0) {
            float velocity = BowItem.getPowerForTime(charge);
            if (!level.isClientSide && velocity >= 0.1F) {
                ItemStack itemstack = player.getProjectile(stack);
                ArrowItem arrowitem = (ArrowItem) (itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
                float[] shotPitches = getShotPitches(level.random, velocity);
                for (int i = 0; i < 2; i++) {
                    AbstractArrow abstractarrow = arrowitem.createArrow(level, itemstack, player);
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

    private static float[] getShotPitches(RandomSource random, float velocity) {
        boolean flag = random.nextBoolean();
        return new float[]{1.0F, getRandomShotPitch(flag, random, velocity), getRandomShotPitch(!flag, random, velocity)};
    }

    private static float getRandomShotPitch(boolean p_150798_, RandomSource random, float velocity) {
        float f = p_150798_ ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f * velocity;
    }

    public OptionalInt onItemUseTick(LivingEntity entity, ItemStack item, int duration) {
        Item item2 = item.getItem();
        int duration2 = item.getUseDuration() - duration;
        if (item2 instanceof BowItem && duration2 < 20 || item2 instanceof TridentItem && duration2 < 10) {
            // quick charge enchantment for bows and tridents
            int quickChargeLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, item);
            return OptionalInt.of(duration - quickChargeLevel);
        }
        return OptionalInt.empty();
    }

    public OptionalInt onLootingLevel(LivingEntity entity, @Nullable DamageSource damageSource, int lootingLevel) {
        if (damageSource == null) return OptionalInt.empty();
        Entity source = damageSource.getDirectEntity();
        if (source instanceof AbstractArrow) {
            // the whole trident stack is saved anyway, so use it
            if (source instanceof ThrownTrident) {
                ItemStack stack = ((AbstractArrowAccessor) source).callGetPickupItem();
                int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, stack);
                if (level > 0) return OptionalInt.of(level);
            } else {
                // overwrite anything set by vanilla, even when enchantment is not present (since the whole holding a looting sword and getting looting applied to ranged kills doesn't make a lot of sense)
                return OptionalInt.of(ModRegistry.ARROW_LOOTING_CAPABILITY.maybeGet(source).map(ArrowLootingCapability::getLevel).orElse((byte) 0));
            }
        }
        return OptionalInt.empty();
    }

    public static Optional<Unit> onShieldBlock(LivingEntity blocker, DamageSource source, float amount) {
        if (!source.isProjectile() && source.getDirectEntity() instanceof LivingEntity attacker) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.THORNS, blocker.getUseItem());
            Enchantments.THORNS.doPostHurt(blocker, attacker, level);
        }
        return Optional.empty();
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
            ModRegistry.ARROW_LOOTING_CAPABILITY.maybeGet(arrow).ifPresent(capability -> capability.setLevel((byte) level));
        }
    }
}
