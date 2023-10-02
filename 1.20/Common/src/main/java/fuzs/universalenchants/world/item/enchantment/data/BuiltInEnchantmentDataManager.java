package fuzs.universalenchants.world.item.enchantment.data;

import com.google.common.collect.*;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.mixin.accessor.EnchantmentAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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
    private Map<EnchantmentCategory, ResourceLocation> toIdMap;
    private Map<ResourceLocation, EnchantmentCategory> toCategoryMap;
    private Map<EnchantmentCategory, TagKey<Item>> toTagKeyMap;
    private int lastEnchantmentCategoriesSize;

    private BuiltInEnchantmentDataManager() {

    }

    public EnchantmentCategory getVanillaCategory(Enchantment enchantment) {
        EnchantmentCategory category = enchantment.category;
        if (this.requireVanillaCategory(category)) return category;
        category = this.defaultEnchantmentCategories.get(enchantment);
        Objects.requireNonNull(category, "vanilla category for enchantment %s is missing".formatted(BuiltInRegistries.ENCHANTMENT.getKey(enchantment)));
        return category;
    }

    public void setEnchantmentCategory(Enchantment enchantment, EnchantmentCategory category) {
        EnchantmentCategory currentCategory = enchantment.category;
        if (this.requireVanillaCategory(currentCategory)) {
            this.defaultEnchantmentCategories.put(enchantment, currentCategory);
        }
        ((EnchantmentAccessor) enchantment).universalenchants$setCategory(category);
        if (!this.requireVanillaCategory(category)) {
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
        ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        return AdditionalEnchantmentDataProvider.ENCHANTMENT_CATEGORY_PREFIX + "%s_%s".formatted(id.getNamespace(), id.getPath()).toUpperCase(Locale.ROOT);
    }

    public boolean requireVanillaCategory(EnchantmentCategory category) {
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

    public Map<EnchantmentCategory, ResourceLocation> getToIdMap() {
        this.tryRebuildCategoriesIdMap();
        return this.toIdMap;
    }

    public Map<ResourceLocation, EnchantmentCategory> getToCategoryMap() {
        this.tryRebuildCategoriesIdMap();
        return this.toCategoryMap;
    }

    public Map<EnchantmentCategory, TagKey<Item>> getToTagKeyMap() {
        this.tryRebuildCategoriesIdMap();
        return this.toTagKeyMap;
    }

    private void tryRebuildCategoriesIdMap() {
        AdditionalEnchantmentDataProvider.INSTANCE.initialize();
        EnchantmentCategory[] values = EnchantmentCategory.values();
        if (this.toIdMap == null || this.toCategoryMap == null || this.toTagKeyMap == null || this.lastEnchantmentCategoriesSize != values.length) {
            ImmutableMap.Builder<EnchantmentCategory, ResourceLocation> toId = ImmutableMap.builder();
            ImmutableMap.Builder<ResourceLocation, EnchantmentCategory> toCategory = ImmutableMap.builder();
            ImmutableMap.Builder<EnchantmentCategory, TagKey<Item>> toTagKey = ImmutableMap.builder();
            for (EnchantmentCategory category : values) {
                if (this.requireVanillaCategory(category)) {
                    ResourceLocation resourceLocation = getResourceLocationFromCategory(category);
                    toId.put(category, resourceLocation);
                    toCategory.put(resourceLocation, category);
                    toTagKey.put(category, getTagKeyFromCategory(resourceLocation));
                    if (resourceLocation.getNamespace().equals(UniversalEnchants.MOD_ID)) {
                        // for legacy compat, remove this in the future
                        toCategory.put(new ResourceLocation(resourceLocation.getPath()), category);
                    }
                }
            }
            this.toIdMap = toId.build();
            this.toCategoryMap = toCategory.build();
            this.toTagKeyMap = toTagKey.build();
            this.lastEnchantmentCategoriesSize = values.length;
        }
    }

    public static ResourceLocation getResourceLocationFromCategory(EnchantmentCategory category) {
        String s = category.name().replaceAll("\\W", "_").toLowerCase(Locale.ROOT);
        if (s.startsWith(AdditionalEnchantmentDataProvider.LOWERCASE_ENCHANTMENT_CATEGORY_PREFIX)) {
            s = s.substring(AdditionalEnchantmentDataProvider.LOWERCASE_ENCHANTMENT_CATEGORY_PREFIX.length());
            return UniversalEnchants.id(s);
        } else {
            return new ResourceLocation(s);
        }
    }

    public static TagKey<Item> getTagKeyFromCategory(EnchantmentCategory category) {
        return getTagKeyFromCategory(getResourceLocationFromCategory(category));
    }

    private static TagKey<Item> getTagKeyFromCategory(ResourceLocation resourceLocation) {
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("enchantment_target/"));
    }
}
