package fuzs.universalenchants.handler;

import com.fuzs.puzzleslib_sm.capability.CapabilityController;
import com.fuzs.puzzleslib_sm.config.ConfigManager;
import com.fuzs.puzzleslib_sm.config.serialization.EntryCollectionBuilder;
import com.fuzs.puzzleslib_sm.element.AbstractElement;
import com.fuzs.puzzleslib_sm.element.side.ICommonElement;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.capability.ArrowLootingCapabilityImpl;
import fuzs.universalenchants.mixin.accessor.IAbstractArrowAccessor;
import fuzs.universalenchants.registry.ModRegistry;
import net.minecraft.enchantment.*;
import net.minecraft.item.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import java.util.ArrayList;
import java.util.Set;

public class CompatibilityElement extends AbstractElement implements ICommonElement {

    @ObjectHolder(UniversalEnchants.MOD_ID + ":" + "plundering")
    public static final Enchantment PLUNDERING_ENCHANTMENT = null;

    @CapabilityInject(ArrowLootingCapability.class)
    public static final Capability<ArrowLootingCapabilityImpl> ARROW_PLUNDERING_CAPABILITY = null;

    public Set<Enchantment> swordEnchantments;
    public Set<Enchantment> axeEnchantments;
    public Set<Enchantment> tridentEnchantments;
    public Set<Enchantment> bowEnchantments;
    public Set<Enchantment> crossbowEnchantments;
    public Set<Item> swordBlacklist;
    public Set<Item> axeBlacklist;
    public Set<Item> tridentBlacklist;
    public Set<Item> bowBlacklist;
    public Set<Item> crossbowBlacklist;

    @Override
    public String getDescription() {

        return "Most vanilla enchantments can be applied to a lot more tools and weapons.";
    }

    @Override
    public void setupCommon() {
        this.addListener(this::onArrowLoose);
        this.addListener(this::onItemUseTick);
        this.addListener(this::onLootingLevel);
    }

    @Override
    public void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        String compatibility = "Additional enchantments to be made usable with ";
        String blacklist = " to be disabled from receiving additional enchantments.";
        addToConfig(builder.comment(compatibility + "swords.").define("Sword Enchantments", ConfigManager.getKeyList(Enchantments.IMPALING)), v -> this.swordEnchantments = deserializeToSet(v, ForgeRegistries.ENCHANTMENTS));
        addToConfig(builder.comment(compatibility + "axes.").define("Axe Enchantments", ConfigManager.getKeyList(Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.LOOTING, Enchantments.SWEEPING, Enchantments.IMPALING)), v -> this.axeEnchantments = deserializeToSet(v, ForgeRegistries.ENCHANTMENTS));
        addToConfig(builder.comment(compatibility + "tridents.").define("Trident Enchantments", ConfigManager.getKeyList(Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.LOOTING, Enchantments.SWEEPING, Enchantments.QUICK_CHARGE, Enchantments.PIERCING)), v -> this.tridentEnchantments = deserializeToSet(v, ForgeRegistries.ENCHANTMENTS));
        addToConfig(builder.comment(compatibility + "bows.").define("Bow Enchantments", ConfigManager.getKeyList(Enchantments.PIERCING, Enchantments.MULTISHOT, Enchantments.QUICK_CHARGE)), v -> this.bowEnchantments = deserializeToSet(v, ForgeRegistries.ENCHANTMENTS));
        addToConfig(builder.comment(compatibility + "crossbows.").define("Crossbow Enchantments", ConfigManager.getKeyList(Enchantments.FLAME, Enchantments.PUNCH, Enchantments.POWER, Enchantments.INFINITY)), v -> this.crossbowEnchantments = deserializeToSet(v, ForgeRegistries.ENCHANTMENTS));
        addToConfig(builder.comment("Swords" + blacklist).define("Sword Blacklist", new ArrayList<String>()), v -> this.swordBlacklist = deserializeToSet(v, ForgeRegistries.ITEMS));
        addToConfig(builder.comment("Axes" + blacklist).define("Axe Blacklist", new ArrayList<String>()), v -> this.axeBlacklist = deserializeToSet(v, ForgeRegistries.ITEMS));
        addToConfig(builder.comment("Tridents" + blacklist).define("Trident Blacklist", new ArrayList<String>()), v -> this.tridentBlacklist = deserializeToSet(v, ForgeRegistries.ITEMS));
        addToConfig(builder.comment("Bows" + blacklist).define("Bow Blacklist", new ArrayList<String>()), v -> this.bowBlacklist = deserializeToSet(v, ForgeRegistries.ITEMS));
        addToConfig(builder.comment("Crossbows" + blacklist).define("Crossbow Blacklist", new ArrayList<String>()), v -> this.crossbowBlacklist = deserializeToSet(v, ForgeRegistries.ITEMS));
    }

    @Override
    public String[] getCommonDescription() {

        return new String[]{"Only enchantments included by default are guaranteed to work. While any modded enchantments or other vanilla enchantments can work, they are highly unlikely to do so.",
                "The blacklists for each item group are supposed to disable items which can be enchanted, but where the enchantments do not function as expected.",
                EntryCollectionBuilder.CONFIG_STRING};
    }

    @SubscribeEvent
    public void onArrowLoose(final ArrowLooseEvent evt) {
        // multishot enchantment for bows
        ItemStack stack = evt.getBow();
        if (evt.hasAmmo() && EnchantmentHelper.getEnchantmentLevel(Enchantments.MULTISHOT, stack) > 0) {
            float velocity = BowItem.getArrowVelocity(evt.getCharge());
            if (velocity >= 0.1F) {
                Player playerentity = evt.getPlayer();
                ItemStack itemstack = playerentity.findAmmo(stack);
                ArrowItem arrowitem = (ArrowItem) (itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
                for (int i = 0; i < 2; i++) {
                    AbstractArrow arrow = arrowitem.createArrow(evt.getWorld(), itemstack, playerentity);
                    // shoot
                    arrow.func_234612_a_(playerentity, playerentity.rotationPitch, playerentity.rotationYaw - 10.0F + i * 20.0F, 0.0F, velocity * 3.0F, 1.0F);
                    applyPowerEnchantment(arrow, stack);
                    applyPunchEnchantment(arrow, stack);
                    applyFlameEnchantment(arrow, stack);
                    applyPiercingEnchantment(arrow, stack);
                    applyLootingEnchantment(arrow, stack);
                    arrow.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY;
                    evt.getWorld().addEntity(arrow);
                }
            }
        }
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
        if (evt.getDamageSource() != null) {
            Entity source = evt.getDamageSource().getImmediateSource();
            if (source instanceof AbstractArrow) {
                if (source instanceof ThrownTrident) {
                    ItemStack trident = ((IAbstractArrowAccessor) source).callGetArrowStack();
                    int lootLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.MOB_LOOTING, trident);
                    if (lootLevel > 0) {
                        evt.setLootingLevel(lootLevel);
                    }
                } else {

                    // overwrite anything set by vanilla, even when enchantment is not present (since the whole holding a looting sword and getting looting applied to ranged kills doesn't make a lot of sense)
                    evt.setLootingLevel(CapabilityController.getCapability(source, ARROW_PLUNDERING_CAPABILITY)
                            .map(ArrowLootingCapabilityImpl::getPlunderingLevel)
                            .orElse((byte) 0));
                }
            }
        }
    }

    public static void applyPowerEnchantment(AbstractArrow arrow, ItemStack stack) {
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
        if (powerLevel > 0) {
            arrow.setDamage(arrow.getDamage() + (double) powerLevel * 0.5 + 0.5);
        }
    }

    public static void applyPunchEnchantment(AbstractArrow arrow, ItemStack stack) {
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
        if (punchLevel > 0) {
            arrow.setKnockbackStrength(punchLevel);
        }
    }

    public static void applyFlameEnchantment(AbstractArrow arrow, ItemStack stack) {
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
            arrow.setFire(100);
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
            arrow.getCapability(ModRegistry.ARROW_LOOTING_CAPABILITY)
                    .ifPresent(capability -> capability.setLevel((byte) level));
        }
    }
}
