package fuzs.universalenchants.world.item.enchantment.data;

import com.google.common.collect.*;
import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.mixin.accessor.EnchantmentAccessor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public class BuiltInEnchantmentDataManager {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Set<EnchantmentCategory> SPECIALIZED_ARMOR_CATEGORIES = ImmutableSet.of(EnchantmentCategory.ARMOR_FEET, EnchantmentCategory.ARMOR_LEGS, EnchantmentCategory.ARMOR_CHEST, EnchantmentCategory.ARMOR_HEAD);
    public static final BuiltInEnchantmentDataManager INSTANCE = new BuiltInEnchantmentDataManager();

    private final BiMap<Enchantment, EnchantmentCategory> customEnchantmentCategories = HashBiMap.create();
    private final Map<Enchantment, EnchantmentCategory> defaultEnchantmentCategories = Maps.newIdentityHashMap();
    private BiMap<EnchantmentCategory, ResourceLocation> enchantmentCategoriesIdMap;
    private int lastEnchantmentCategoriesSize;

    private BuiltInEnchantmentDataManager() {

    }

    public EnchantmentCategory getVanillaCategory(Enchantment enchantment) {
        EnchantmentCategory category = enchantment.category;
        if (this.testVanillaCategory(category)) return category;
        category = this.defaultEnchantmentCategories.get(enchantment);
        Objects.requireNonNull(category, "vanilla category for enchantment %s is missing".formatted(Registry.ENCHANTMENT.getKey(enchantment)));
        return category;
    }

    public void setEnchantmentCategory(Enchantment enchantment, EnchantmentCategory category) {
        EnchantmentCategory currentCategory = enchantment.category;
        if (this.testVanillaCategory(currentCategory)) {
            this.defaultEnchantmentCategories.put(enchantment, currentCategory);
        }
        ((EnchantmentAccessor) enchantment).universalenchants$setCategory(category);
        if (!this.testVanillaCategory(category)) {
            this.tryUnlockEnchantmentSlots(enchantment);
        }
    }

    private void tryUnlockEnchantmentSlots(Enchantment enchantment) {
        // need this to make horse armor work for frost walker and soul speed (kinda breaks soul speed as horse armor is equipped in chest slot, but soul speed attempts to damage boots slot, but since horse armor has no durability anyway and there's nothing equipped in the boots slot that's fine)
        // all other armor enchantments already set all slots (even the specialized ones such as respiration or feather falling)
        // do this here dynamically to better support modded enchantments
        EnchantmentCategory vanillaCategory = this.defaultEnchantmentCategories.get(enchantment);
        if (SPECIALIZED_ARMOR_CATEGORIES.contains(vanillaCategory)) {
            ((EnchantmentAccessor) enchantment).universalenchants$setSlots(ARMOR_SLOTS.clone());
        }
        // need this for thorns to work on shields
        if (enchantment == Enchantments.THORNS) {
            ((EnchantmentAccessor) enchantment).universalenchants$setSlots(EquipmentSlot.values().clone());
        }
    }

    public EnchantmentCategory getOrBuildCustomCategory(Enchantment enchantment, Predicate<Item> canApplyTo) {
        return this.customEnchantmentCategories.computeIfAbsent(enchantment, enchantment1 -> CommonAbstractions.INSTANCE.createEnchantmentCategory(createCategoryName(enchantment1), canApplyTo));
    }

    private static String createCategoryName(Enchantment enchantment) {
        ResourceLocation id = Registry.ENCHANTMENT.getKey(enchantment);
        return AdditionalEnchantmentDataProvider.ENCHANTMENT_CATEGORY_PREFIX + "%s_%s".formatted(id.getNamespace(), id.getPath()).toUpperCase(Locale.ROOT);
    }

    public boolean testVanillaCategory(EnchantmentCategory category) {
        return !this.customEnchantmentCategories.containsValue(category);
    }

    public EnchantmentCategory convertToVanillaCategory(EnchantmentCategory customCategory) {
        Enchantment enchantment = this.customEnchantmentCategories.inverse().get(customCategory);
        if (enchantment != null) {
            EnchantmentCategory vanillaCategory = this.defaultEnchantmentCategories.get(enchantment);
            if (vanillaCategory != null) {
                return vanillaCategory;
            }
        }
        return customCategory;
    }

    public BiMap<EnchantmentCategory, ResourceLocation> getEnchantmentCategoriesIdMap() {
        this.tryRebuildCategoriesIdMap();
        return this.enchantmentCategoriesIdMap;
    }

    private void tryRebuildCategoriesIdMap() {
        AdditionalEnchantmentDataProvider.INSTANCE.initialize();
        EnchantmentCategory[] values = EnchantmentCategory.values();
        if (this.enchantmentCategoriesIdMap == null || this.lastEnchantmentCategoriesSize != values.length) {
            ImmutableBiMap.Builder<EnchantmentCategory, ResourceLocation> builder = ImmutableBiMap.builder();
            for (EnchantmentCategory category : values) {
                if (this.testVanillaCategory(category)) {
                    String identifier = category.name().replaceAll("\\W", "_").toLowerCase(Locale.ROOT);
                    builder.put(category, new ResourceLocation(identifier));
                }
            }
            this.enchantmentCategoriesIdMap = builder.build();
            this.lastEnchantmentCategoriesSize = values.length;
        }
    }
}
