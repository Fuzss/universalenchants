package fuzs.universalenchants.world.item.enchantment.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.DataEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdditionalEnchantmentDataProvider {
    public static final String ENCHANTMENT_CATEGORY_PREFIX = UniversalEnchants.MOD_NAME.toUpperCase(Locale.ROOT).replace(" ", "_") + "_";
    public static final EnchantmentCategory SHIELD_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "SHIELD", item -> item instanceof ShieldItem);
    public static final EnchantmentCategory HORSE_ARMOR_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "HORSE_ARMOR", item -> item instanceof HorseArmorItem);
    public static final EnchantmentCategory AXE_ENCHANTMENT_CATEGORY = CommonAbstractions.INSTANCE.createEnchantmentCategory(ENCHANTMENT_CATEGORY_PREFIX + "AXE", item -> item instanceof AxeItem);
    static final String LOWERCASE_ENCHANTMENT_CATEGORY_PREFIX = ENCHANTMENT_CATEGORY_PREFIX.toLowerCase(Locale.ROOT);
    public static final AdditionalEnchantmentDataProvider INSTANCE = new AdditionalEnchantmentDataProvider();

    private final List<AdditionalEnchantmentsData> additionalEnchantmentsData = ImmutableList.of(
            new AdditionalEnchantmentsData(EnchantmentCategory.WEAPON, Enchantments.IMPALING),
            new AdditionalEnchantmentsData(AXE_ENCHANTMENT_CATEGORY, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.IMPALING),
            new AdditionalEnchantmentsData(EnchantmentCategory.TRIDENT, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.QUICK_CHARGE, Enchantments.PIERCING),
            new AdditionalEnchantmentsData(EnchantmentCategory.BOW, Enchantments.PIERCING, Enchantments.MULTISHOT, Enchantments.QUICK_CHARGE, Enchantments.MOB_LOOTING),
            new AdditionalEnchantmentsData(EnchantmentCategory.CROSSBOW, Enchantments.FLAMING_ARROWS, Enchantments.PUNCH_ARROWS, Enchantments.POWER_ARROWS, Enchantments.INFINITY_ARROWS, Enchantments.MOB_LOOTING),
            new AdditionalEnchantmentsData(HORSE_ARMOR_ENCHANTMENT_CATEGORY, Enchantments.ALL_DAMAGE_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.FALL_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.RESPIRATION, Enchantments.THORNS, Enchantments.DEPTH_STRIDER, Enchantments.FROST_WALKER, Enchantments.BINDING_CURSE, Enchantments.SOUL_SPEED, Enchantments.VANISHING_CURSE),
            new AdditionalEnchantmentsData(SHIELD_ENCHANTMENT_CATEGORY, Enchantments.THORNS, Enchantments.KNOCKBACK),
            new AdditionalEnchantmentsData(EnchantmentCategory.ARMOR, Enchantments.THORNS)
    );
    private Map<Enchantment, List<DataEntry<?>>> defaultCategoryEntries;

    private AdditionalEnchantmentDataProvider() {

    }

    public static void initialize() {
        // make sure enum values are created
    }

    public Map<Enchantment, List<DataEntry<?>>> getEnchantmentDataEntries() {
        if (this.defaultCategoryEntries == null) {
            // constructing default builders on Forge is quite expensive, so only do this when necessary
            Map<Enchantment, DataEntry.BuilderHolder> builders = BuiltInRegistries.ENCHANTMENT.stream().collect(Collectors.toMap(Function.identity(), DataEntry::getEnchantmentDataBuilder));
            this.additionalEnchantmentsData.forEach(data -> data.addToBuilder(builders));
            setupAdditionalCompatibility(builders);
            this.defaultCategoryEntries = builders.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, entry -> entry.getValue().build()));
        }
        return this.defaultCategoryEntries;
    }

    private static void setupAdditionalCompatibility(Map<Enchantment, DataEntry.BuilderHolder> builders) {
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

    private static void applyIncompatibilityToBoth(Map<Enchantment, DataEntry.BuilderHolder> builders, Enchantment enchantment, Enchantment other, boolean add) {
        BiConsumer<Enchantment, Enchantment> operation = (e1, e2) -> {
            DataEntry.BuilderHolder builder = builders.get(e1);
            // this might be called for non-vanilla enchantments (currently possible through DamageEnchantment and ProtectionEnchantment instanceof checks)
            // they won't have a builder, so be careful
            if (builder != null) {
                if (add) {
                    builder.incompatibleBuilder().add(e2);
                } else {
                    builder.incompatibleBuilder().remove(e2);
                }
            }
        };
        operation.accept(enchantment, other);
        operation.accept(other, enchantment);
    }

    private record AdditionalEnchantmentsData(EnchantmentCategory category, List<Enchantment> enchantments) {

        AdditionalEnchantmentsData(EnchantmentCategory category, Enchantment... enchantments) {
            this(category, ImmutableList.copyOf(enchantments));
        }

        public void addToBuilder(Map<Enchantment, DataEntry.BuilderHolder> builders) {
            for (Enchantment enchantment : this.enchantments) {
                if (builders.containsKey(enchantment)) {
                    DataEntry.BuilderHolder holder = builders.get(enchantment);
                    holder.categoryBuilder().add(this.category);
                    holder.anvilBuilder().add(this.category);
                }
            }
        }
    }
}
