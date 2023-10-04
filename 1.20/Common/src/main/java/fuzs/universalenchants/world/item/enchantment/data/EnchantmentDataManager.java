package fuzs.universalenchants.world.item.enchantment.data;

import com.google.common.collect.*;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.init.ModRegistry;
import fuzs.universalenchants.mixin.accessor.EnchantmentAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.*;
import java.util.function.Predicate;

public class EnchantmentDataManager {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Set<EnchantmentCategory> SPECIALIZED_ARMOR_CATEGORIES = Set.of(EnchantmentCategory.ARMOR_FEET, EnchantmentCategory.ARMOR_LEGS, EnchantmentCategory.ARMOR_CHEST, EnchantmentCategory.ARMOR_HEAD);
    public static final EnchantmentDataManager INSTANCE = new EnchantmentDataManager();

    private final BiMap<Enchantment, EnchantmentCategory> newCategories = HashBiMap.create();
    private final Map<Enchantment, EnchantmentCategory> vanillaCategories = Maps.newIdentityHashMap();
    private final Map<EnchantmentCategory, Collection<Item>> toItemMap = Maps.newIdentityHashMap();
    private Map<ResourceLocation, EnchantmentCategory> toCategoryMap;
    private int categoriesLastSize;

    private EnchantmentDataManager() {

    }

    public EnchantmentCategory getVanillaCategory(Enchantment enchantment) {
        EnchantmentCategory category = enchantment.category;
        if (isVanillaCategory(category)) return category;
        category = this.vanillaCategories.get(enchantment);
        Objects.requireNonNull(category, () -> {
            ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
            return "vanilla category for enchantment " + resourceLocation + " is missing";
        });
        return category;
    }

    public void setEnchantmentCategory(Enchantment enchantment, EnchantmentCategory category) {
        EnchantmentCategory currentCategory = enchantment.category;
        if (isVanillaCategory(currentCategory)) {
            if (this.vanillaCategories.put(enchantment, currentCategory) != null) {
                ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
                throw new IllegalStateException("vanilla category for enchantment " + resourceLocation + "already stored");
            }
        }
        ((EnchantmentAccessor) enchantment).universalenchants$setCategory(category);
        if (isVanillaCategory(currentCategory) && !isVanillaCategory(category)) {
            this.tryUnlockEnchantmentSlots(enchantment);
        }
    }

    private void tryUnlockEnchantmentSlots(Enchantment enchantment) {
        // need this to make horse armor work for frost walker and soul speed (kinda breaks soul speed as horse armor is equipped in chest slot, but soul speed attempts to damage boots slot, but since horse armor has no durability anyway and there's nothing equipped in the boots slot that's fine)
        // all other armor enchantments already set all slots (even the specialized ones such as respiration or feather falling)
        // do this here dynamically to better support modded enchantments
        EnchantmentCategory vanillaCategory = this.vanillaCategories.get(enchantment);
        if (SPECIALIZED_ARMOR_CATEGORIES.contains(vanillaCategory)) {
            ((EnchantmentAccessor) enchantment).universalenchants$setSlots(ARMOR_SLOTS.clone());
        }
        // need this for thorns to work on shields
        if (enchantment == Enchantments.THORNS) {
            ((EnchantmentAccessor) enchantment).universalenchants$setSlots(EquipmentSlot.values().clone());
        }
    }

    public EnchantmentCategory getCustomCategory(Enchantment enchantment, Predicate<Item> canApplyTo) {
        return this.newCategories.computeIfAbsent(enchantment, $ -> {
            String categoryName = computeCategoryName(enchantment);
            return CommonAbstractions.INSTANCE.createEnchantmentCategory(categoryName, canApplyTo);
        });
    }

    private static String computeCategoryName(Enchantment enchantment) {
        ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        String s = ModRegistry.ENCHANTMENT_CATEGORY_PREFIX + id.toDebugFileName();
        return s.toUpperCase(Locale.ROOT);
    }

    public static boolean isVanillaCategory(EnchantmentCategory category) {
        return !INSTANCE.newCategories.containsValue(category);
    }

    public EnchantmentCategory convertToVanillaCategory(EnchantmentCategory customCategory) {
        Enchantment enchantment = this.newCategories.inverse().get(customCategory);
        if (enchantment != null) {
            EnchantmentCategory vanillaCategory = this.vanillaCategories.get(enchantment);
            if (vanillaCategory != null) {
                return vanillaCategory;
            }
        }
        return customCategory;
    }

    public Map<ResourceLocation, EnchantmentCategory> getToCategoryMap() {
        this.tryRebuildCategoriesIdMap();
        return this.toCategoryMap;
    }

    private void tryRebuildCategoriesIdMap() {
        EnchantmentCategory[] values = EnchantmentCategory.values();
        if (this.toCategoryMap == null || this.categoriesLastSize != values.length) {
            ImmutableMap.Builder<ResourceLocation, EnchantmentCategory> toCategory = ImmutableMap.builder();
            for (EnchantmentCategory category : values) {
                ResourceLocation resourceLocation = EnchantmentDataTags.getResourceLocationFromCategory(category);
                if (resourceLocation != null) {
                    toCategory.put(resourceLocation, category);
                    if (resourceLocation.getNamespace().equals(UniversalEnchants.MOD_ID)) {
                        // for legacy compat, remove this in the future
                        String s = category.name().replaceAll("\\W", "_").toLowerCase(Locale.ROOT);
                        toCategory.put(new ResourceLocation(s), category);
                    }
                }
            }
            this.toCategoryMap = toCategory.build();
            this.categoriesLastSize = values.length;
        }
    }

    public Collection<Item> getItems(EnchantmentCategory category) {
        return this.toItemMap.computeIfAbsent(category, $ -> {
            if (isVanillaCategory(category)) {
                Set<Item> items = Sets.newIdentityHashSet();
                for (Item item : BuiltInRegistries.ITEM) {
                    if (category.canEnchant(item)) items.add(item);
                }
                return Collections.unmodifiableSet(items);
            }
            return Collections.emptySet();
        });
    }
}
