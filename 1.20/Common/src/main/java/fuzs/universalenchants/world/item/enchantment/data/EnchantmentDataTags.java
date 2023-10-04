package fuzs.universalenchants.world.item.enchantment.data;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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
        if (!EnchantmentDataManager.isVanillaCategory(category)) throw new IllegalArgumentException("invalid category: " + category);
        ResourceLocation resourceLocation = getResourceLocationFromCategory(category);
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return getTagKeyFromCategory(resourceLocation);
    }

    public static TagKey<Item> getTagKeyFromCategory(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("enchantment_target/"));
    }

    public static TagKey<Item> getTagKeyForItems(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("enchanting_items/"));
    }

    public static TagKey<Item> getTagKeyForDisabledItems(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("disabled_enchanting_items/"));
    }

    public static TagKey<Item> getTagKeyForAnvilItems(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("anvil_enchanting_items/"));
    }

    public static TagKey<Item> getTagKeyForDisabledAnvilItems(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resource location is null");
        return TagKey.create(Registries.ITEM, resourceLocation.withPrefix("disabled_anvil_enchanting_items/"));
    }

    @Nullable
    public static ResourceLocation getResourceLocationFromCategory(EnchantmentCategory category) {
        Objects.requireNonNull(category, "category is null");
        if (!EnchantmentDataManager.isVanillaCategory(category)) return null;
        String s = category.name().replaceAll("\\W", "_").toLowerCase(Locale.ROOT);
        if (s.startsWith(LOWERCASE_ENCHANTMENT_CATEGORY_PREFIX)) {
            s = s.substring(LOWERCASE_ENCHANTMENT_CATEGORY_PREFIX.length());
            return UniversalEnchants.id(s);
        } else {
            return new ResourceLocation(s);
        }
    }
}
