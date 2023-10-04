package fuzs.universalenchants.world.item.enchantment.data;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class EnchantmentDataProvider {
    public static final Supplier<Map<Enchantment, EnchantmentData>> ADDITIONAL_ENCHANTMENT_DATA;
    private static final List<AdditionalEnchantmentsData> ADDITIONAL_ENCHANTMENTS_DATA = Lists.newArrayList();

    static {
        registerAll();
        ADDITIONAL_ENCHANTMENT_DATA = Suppliers.memoize(() -> {
            Map<Enchantment, EnchantmentData> data = Maps.newIdentityHashMap();
            ADDITIONAL_ENCHANTMENTS_DATA.forEach(datum -> datum.addToBuilder(data));
            setupAdditionalCompatibility(data);
            return Collections.unmodifiableMap(data);
        });
    }

    private EnchantmentDataProvider() {

    }

    private static void registerAll() {
        register(EnchantmentCategory.WEAPON, Enchantments.IMPALING);
        register(ModRegistry.AXE_ENCHANTMENT_CATEGORY, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.IMPALING);
        register(EnchantmentCategory.TRIDENT, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.QUICK_CHARGE, Enchantments.PIERCING);
        register(EnchantmentCategory.BOW, Enchantments.PIERCING, Enchantments.MULTISHOT, Enchantments.QUICK_CHARGE, Enchantments.MOB_LOOTING);
        register(EnchantmentCategory.CROSSBOW, Enchantments.FLAMING_ARROWS, Enchantments.PUNCH_ARROWS, Enchantments.POWER_ARROWS, Enchantments.INFINITY_ARROWS, Enchantments.MOB_LOOTING);
        register(ModRegistry.HORSE_ARMOR_ENCHANTMENT_CATEGORY, Enchantments.ALL_DAMAGE_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.FALL_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.RESPIRATION, Enchantments.THORNS, Enchantments.DEPTH_STRIDER, Enchantments.FROST_WALKER, Enchantments.BINDING_CURSE, Enchantments.SOUL_SPEED, Enchantments.VANISHING_CURSE);
        register(ModRegistry.SHIELD_ENCHANTMENT_CATEGORY, Enchantments.THORNS, Enchantments.KNOCKBACK);
        register(EnchantmentCategory.ARMOR, Enchantments.THORNS);
    }

    private static void register(EnchantmentCategory category, Enchantment... enchantments) {
        AdditionalEnchantmentsData data = new AdditionalEnchantmentsData(category, ImmutableList.copyOf(enchantments));
        ADDITIONAL_ENCHANTMENTS_DATA.add(data);
    }

    private static void setupAdditionalCompatibility(Map<Enchantment, EnchantmentData> builders) {
        applyIncompatibilityToBoth(builders, Enchantments.INFINITY_ARROWS, Enchantments.MENDING, false);
        applyIncompatibilityToBoth(builders, Enchantments.MULTISHOT, Enchantments.PIERCING, false);
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            if (enchantment instanceof DamageEnchantment && enchantment != Enchantments.SHARPNESS) {
                applyIncompatibilityToBoth(builders, Enchantments.SHARPNESS, enchantment, false);
                // we make impaling incompatible with damage enchantments as both can be applied to the same weapons now
                applyIncompatibilityToBoth(builders, Enchantments.IMPALING, enchantment, true);
            }
            if (enchantment instanceof ProtectionEnchantment && enchantment != Enchantments.ALL_DAMAGE_PROTECTION && enchantment != Enchantments.FALL_PROTECTION) {
                applyIncompatibilityToBoth(builders, Enchantments.ALL_DAMAGE_PROTECTION, enchantment, false);
            }
        }
    }

    private static void applyIncompatibilityToBoth(Map<Enchantment, EnchantmentData> data, Enchantment enchantment, Enchantment other, boolean add) {
        BiConsumer<Enchantment, Enchantment> operation = (e1, e2) -> {
            EnchantmentData datum = data.computeIfAbsent(e1, $ -> new EnchantmentData());
            if (add) {
                datum.incompatible().left().add(e2);
            } else {
                datum.incompatible().right().add(e2);
            }
        };
        operation.accept(enchantment, other);
        operation.accept(other, enchantment);
    }

    private record AdditionalEnchantmentsData(EnchantmentCategory category, List<Enchantment> enchantments) {

        public void addToBuilder(Map<Enchantment, EnchantmentData> data) {
            for (Enchantment enchantment : this.enchantments) {
                EnchantmentData datum = data.computeIfAbsent(enchantment, $ -> new EnchantmentData());
                datum.items().left().add(this.category);
                datum.anvilItems().left().add(this.category);
            }
        }
    }
}
