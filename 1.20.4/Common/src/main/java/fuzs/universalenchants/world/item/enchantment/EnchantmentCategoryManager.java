package fuzs.universalenchants.world.item.enchantment;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

public final class EnchantmentCategoryManager {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Set<EnchantmentCategory> SPECIALIZED_ARMOR_CATEGORIES = Set.of(EnchantmentCategory.ARMOR_FEET, EnchantmentCategory.ARMOR_LEGS, EnchantmentCategory.ARMOR_CHEST, EnchantmentCategory.ARMOR_HEAD);

    private static final Map<Enchantment, EnchantmentCategory> VANILLA_CATEGORIES = Maps.newIdentityHashMap();
    private static final BiMap<Enchantment, EnchantmentCategory> NEW_CATEGORIES = HashBiMap.create();
    private static final Map<EnchantmentCategory, Collection<Item>> TO_ITEM_MAP = Maps.newIdentityHashMap();

    private EnchantmentCategoryManager() {

    }

    public static EnchantmentCategory getVanillaCategory(Enchantment enchantment) {
        EnchantmentCategory category = enchantment.category;
        if (isVanillaCategory(category)) return category;
        category = VANILLA_CATEGORIES.get(enchantment);
        Objects.requireNonNull(category, () -> {
            ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
            return "vanilla category for enchantment " + resourceLocation + " is missing";
        });
        return category;
    }

    public static void setEnchantmentCategory(Enchantment enchantment, EnchantmentCategory newCategory) {
        EnchantmentCategory currentCategory = enchantment.category;
        if (isVanillaCategory(currentCategory)) {
            if (VANILLA_CATEGORIES.put(enchantment, currentCategory) != null) {
                ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
                throw new IllegalStateException("vanilla category for enchantment " + resourceLocation + "already stored");
            }
        }
        ((EnchantmentAccessor) enchantment).universalenchants$setCategory(newCategory);
        if (isVanillaCategory(currentCategory) && !isVanillaCategory(newCategory)) {
            tryUnlockEnchantmentSlots(enchantment, currentCategory);
        }
    }

    private static void tryUnlockEnchantmentSlots(Enchantment enchantment, EnchantmentCategory category) {
        // need this to make horse armor work for frost walker and soul speed (kinda breaks soul speed as horse armor is equipped in chest slot, but soul speed attempts to damage boots slot, but since horse armor has no durability anyway and there's nothing equipped in the boots slot that's fine)
        // all other armor enchantments already set all slots (even the specialized ones such as respiration or feather falling)
        // do this here dynamically to better support modded enchantments
        if (SPECIALIZED_ARMOR_CATEGORIES.contains(category)) {
            ((EnchantmentAccessor) enchantment).universalenchants$setSlots(ARMOR_SLOTS.clone());
        }
        // need this for thorns to work on shields
        if (enchantment == Enchantments.THORNS) {
            ((EnchantmentAccessor) enchantment).universalenchants$setSlots(EquipmentSlot.values().clone());
        }
    }

    public static EnchantmentCategory createEnchantmentCategory(Enchantment enchantment, Predicate<Item> canApplyTo) {
        return NEW_CATEGORIES.computeIfAbsent(enchantment, $ -> {
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
        return !NEW_CATEGORIES.containsValue(category);
    }

    public static Collection<Item> getItems(EnchantmentCategory category) {
        return TO_ITEM_MAP.computeIfAbsent(category, $ -> {
            if (isVanillaCategory(category)) {
                Set<Item> items = Sets.newLinkedHashSet();
                for (Item item : BuiltInRegistries.ITEM) {
                    if (category.canEnchant(item)) items.add(item);
                }
                return Collections.unmodifiableSet(items);
            }
            return Collections.emptySet();
        });
    }
}
