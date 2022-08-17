package fuzs.universalenchants.handler.v2;

import com.google.common.collect.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemCompatManagerV2 {
    /**
     * we store these manually as vanilla's categories are the only ones we want to mess with, don't accidentally do something with our own or other mods' categories
     */
    private static final EnchantmentCategory[] VANILLA_ENCHANTMENT_CATEGORY_VALUES = {EnchantmentCategory.ARMOR, EnchantmentCategory.ARMOR_FEET, EnchantmentCategory.ARMOR_LEGS, EnchantmentCategory.ARMOR_CHEST, EnchantmentCategory.ARMOR_HEAD, EnchantmentCategory.WEAPON, EnchantmentCategory.DIGGER, EnchantmentCategory.FISHING_ROD, EnchantmentCategory.TRIDENT, EnchantmentCategory.BREAKABLE, EnchantmentCategory.BOW, EnchantmentCategory.WEARABLE, EnchantmentCategory.CROSSBOW, EnchantmentCategory.VANISHABLE};
    public static final BiMap<EnchantmentCategory, ResourceLocation> ENCHANTMENT_CATEGORIES_BY_ID = Stream.of(VANILLA_ENCHANTMENT_CATEGORY_VALUES).collect(Collectors.toMap(Function.identity(), category -> new ResourceLocation(category.name().toLowerCase(Locale.ROOT)), (o1, o2) -> o1, () -> EnumHashBiMap.create(EnchantmentCategory.class)));
    private static final AdditionalEnchantmentsData ADDITIONAL_SWORD_ENCHANTMENTS = new AdditionalEnchantmentsData(EnchantmentCategory.WEAPON, Enchantments.IMPALING);
    private static final AdditionalEnchantmentsData ADDITIONAL_AXE_ENCHANTMENTS = new AdditionalEnchantmentsData(EnchantmentCategory.WEAPON, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.IMPALING);
    private static final AdditionalEnchantmentsData ADDITIONAL_TRIDENT_ENCHANTMENTS = new AdditionalEnchantmentsData(EnchantmentCategory.TRIDENT, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.QUICK_CHARGE, Enchantments.PIERCING);
    private static final AdditionalEnchantmentsData ADDITIONAL_BOW_ENCHANTMENTS = new AdditionalEnchantmentsData(EnchantmentCategory.BOW, Enchantments.PIERCING, Enchantments.MULTISHOT, Enchantments.QUICK_CHARGE, Enchantments.MOB_LOOTING);
    private static final AdditionalEnchantmentsData ADDITIONAL_CROSSBOW_ENCHANTMENTS = new AdditionalEnchantmentsData(EnchantmentCategory.CROSSBOW, Enchantments.FLAMING_ARROWS, Enchantments.PUNCH_ARROWS, Enchantments.POWER_ARROWS, Enchantments.INFINITY_ARROWS, Enchantments.MOB_LOOTING);
    private static final List<AdditionalEnchantmentsData> ADDITIONAL_ENCHANTMENTS_DATA = ImmutableList.of(ADDITIONAL_SWORD_ENCHANTMENTS, ADDITIONAL_AXE_ENCHANTMENTS, ADDITIONAL_TRIDENT_ENCHANTMENTS, ADDITIONAL_BOW_ENCHANTMENTS, ADDITIONAL_CROSSBOW_ENCHANTMENTS);
    private static final Map<Enchantment, List<EnchantmentCategoryEntry>> DEFAULT_CATEGORY_ENTRIES;
    
    static {
        Map<Enchantment, EnchantmentCategoryEntry.Builder> builders = getDefaultBuilders();
        ADDITIONAL_ENCHANTMENTS_DATA.forEach(data -> data.addToBuilder(builders));
        DEFAULT_CATEGORY_ENTRIES = builders.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> e.getValue().build()));
    }

    private static Map<Enchantment, EnchantmentCategoryEntry.Builder> getDefaultBuilders() {
        Map<Enchantment, EnchantmentCategoryEntry.Builder> builders = Maps.newHashMap();
        for (Map.Entry<ResourceKey<Enchantment>, Enchantment> entry : Registry.ENCHANTMENT.entrySet()) {
            if (entry.getKey().location().getNamespace().equals("minecraft")) {
                builders.put(entry.getValue(), EnchantmentCategoryEntry.defaultBuilder(entry.getValue()));
            }
        }
        return builders;
    }

    private record AdditionalEnchantmentsData(EnchantmentCategory category, List<Enchantment> enchantments) {

        AdditionalEnchantmentsData(EnchantmentCategory category, Enchantment... enchantments) {
            this(category, ImmutableList.copyOf(enchantments));
        }

        public void addToBuilder(Map<Enchantment, EnchantmentCategoryEntry.Builder> builders) {
            for (Enchantment enchantment : this.enchantments) {
                builders.get(enchantment).add(this.category);
            }
        }
    }
}
