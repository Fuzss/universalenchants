package fuzs.universalenchants.world.item.enchantment.data;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.init.ModRegistry;
import fuzs.universalenchants.world.item.enchantment.EnchantmentCategoryManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

public final class EnchantmentDataTags {
    private static final String LOWERCASE_ENCHANTMENT_CATEGORY_PREFIX = ModRegistry.ENCHANTMENT_CATEGORY_PREFIX.toLowerCase(Locale.ROOT);

    private EnchantmentDataTags() {

    }

    public static TagKey<Item> getTagKeyFromCategory(EnchantmentCategory category) {
        Objects.requireNonNull(category, "category is null");
        if (!EnchantmentCategoryManager.isVanillaCategory(category)) throw new IllegalArgumentException("invalid category: " + category);
        ResourceLocation resourceLocation = getResourceLocationFromCategory(category);
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return getTagKeyFromCategory(resourceLocation);
    }

    public static TagKey<Item> getTagKeyFromCategory(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("enchantment_target/"));
    }

    public static TagKey<Item> getAllowAtEnchantingTable(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("allow_at_enchanting_table/"));
    }

    public static TagKey<Item> getDisallowAtEnchantingTable(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("disallow_at_enchanting_table/"));
    }

    public static TagKey<Item> getAllowAtAnvil(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("allow_at_anvil/"));
    }

    public static TagKey<Item> getDisallowAtAnvil(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("disallow_at_anvil/"));
    }

    public static TagKey<Enchantment> getIsIncompatible(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ENCHANTMENT, resourceLocation.withPrefix("is_incompatible/"));
    }

    public static TagKey<Enchantment> getIsNotIncompatible(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ENCHANTMENT, resourceLocation.withPrefix("is_not_incompatible/"));
    }

    @Nullable
    public static ResourceLocation getResourceLocationFromCategory(EnchantmentCategory category) {
        Objects.requireNonNull(category, "category is null");
        if (!EnchantmentCategoryManager.isVanillaCategory(category)) return null;
        String s = category.name().replaceAll("\\W", "_").toLowerCase(Locale.ROOT);
        if (s.startsWith(LOWERCASE_ENCHANTMENT_CATEGORY_PREFIX)) {
            s = s.substring(LOWERCASE_ENCHANTMENT_CATEGORY_PREFIX.length());
            return UniversalEnchants.id(s);
        } else {
            return new ResourceLocation(s);
        }
    }
}
