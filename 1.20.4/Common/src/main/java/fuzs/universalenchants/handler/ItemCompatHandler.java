package fuzs.universalenchants.handler;

import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedFloat;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
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
import org.jetbrains.annotations.Nullable;

public class ItemCompatHandler {
    private static final float BOW_MULTISHOT_ANGLE = 6.25F;

    public static EventResult onArrowLoose(Player player, ItemStack stack, Level level, MutableInt charge, boolean hasAmmo) {
        // multishot enchantment for bows
        if (hasAmmo && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack) > 0) {
            float velocity = BowItem.getPowerForTime(charge.getAsInt());
            if (!level.isClientSide && velocity >= 0.1F) {
                ItemStack projectile = player.getProjectile(stack);
                ArrowItem item = (ArrowItem) (projectile.getItem() instanceof ArrowItem ?
                        projectile.getItem() :
                        Items.ARROW);
                for (int i = 0; i < 2; i++) {
                    createAndShootArrow(player, stack, level, item, projectile, -BOW_MULTISHOT_ANGLE + i * BOW_MULTISHOT_ANGLE * 2.0F, velocity);
                }
            }
        }

        return EventResult.PASS;
    }

    private static void createAndShootArrow(Player player, ItemStack stack, Level level, ArrowItem item, ItemStack projectile, float shootAngle, float velocity) {
        AbstractArrow abstractArrow = item.createArrow(level, projectile, player);
        abstractArrow.shootFromRotation(player,
                player.getXRot() + shootAngle,
                player.getYRot(),
                0.0F,
                velocity * 3.0F,
                1.5F
        );
        if (velocity == 1.0F) {
            abstractArrow.setCritArrow(true);
        }
        applyPowerEnchantment(abstractArrow, stack);
        applyPunchEnchantment(abstractArrow, stack);
        applyFlameEnchantment(abstractArrow, stack);
        applyPiercingEnchantment(abstractArrow, stack);
        applyLootingEnchantment(abstractArrow, stack);
        abstractArrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        level.addFreshEntity(abstractArrow);
        level.playSound(null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + velocity * 0.5F
        );
    }

    public static EventResult onUseItemTick(LivingEntity entity, ItemStack useItem, MutableInt useItemRemaining) {
        Item item2 = useItem.getItem();
        int duration2 = useItem.getUseDuration() - useItemRemaining.getAsInt();
        if (item2 instanceof BowItem && duration2 < 20 || item2 instanceof TridentItem && duration2 < 10) {
            // quick charge enchantment for bows and tridents
            int quickChargeLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.QUICK_CHARGE, useItem);
            useItemRemaining.mapInt(duration -> duration - quickChargeLevel);
        }

        return EventResult.PASS;
    }

    public static void onLootingLevel(LivingEntity entity, @Nullable DamageSource damageSource, MutableInt lootingLevel) {
        if (damageSource == null) return;
        Entity source = damageSource.getDirectEntity();
        if (source instanceof AbstractArrow abstractArrow) {
            // the whole trident stack is saved anyway, so use it
            if (source instanceof ThrownTrident) {
                ItemStack stack = abstractArrow.getPickupItemStackOrigin();
                int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, stack);
                if (level > 0) {
                    lootingLevel.accept(level);
                }
            } else {
                // overwrite anything set by vanilla, even when enchantment is not present (since the whole holding a looting sword and getting looting applied to ranged kills doesn't make a lot of sense)
                lootingLevel.accept(ModRegistry.ARROW_LOOTING_CAPABILITY.get(abstractArrow).getLevel());
            }
        }
    }

    public static EventResult onShieldBlock(LivingEntity blocker, DamageSource source, DefaultedFloat blockedDamage) {
        if (!source.is(DamageTypeTags.IS_PROJECTILE) && source.getDirectEntity() instanceof LivingEntity attacker) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.THORNS, blocker.getUseItem());
            Enchantments.THORNS.doPostHurt(blocker, attacker, level);
        }

        return EventResult.PASS;
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

    public static void applyLootingEnchantment(AbstractArrow abstractArrow, ItemStack stack) {
        int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MOB_LOOTING, stack);
        if (level > 0) {
            ModRegistry.ARROW_LOOTING_CAPABILITY.get(abstractArrow).setLevel((byte) level);
        }
    }
}
