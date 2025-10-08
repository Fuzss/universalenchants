package fuzs.universalenchants.init;

import fuzs.puzzleslib.api.init.v3.tags.TagFactory;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.data.ModDatapackRegistriesProvider;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

public class ModRegistry {
    public static final RegistrySetBuilder REGISTRY_SET_BUILDER = new RegistrySetBuilder().add(Registries.ENCHANTMENT,
            ModDatapackRegistriesProvider::boostrapEnchantments);
    static final TagFactory TAGS = TagFactory.make(UniversalEnchants.MOD_ID);
    public static final TagKey<Block> FROSTED_ICE_REPLACEABLES_BLOCK_TAG = TAGS.registerBlockTag(
            "frosted_ice_replaceables");
    public static final TagKey<Item> ANIMAL_ARMOR_ITEM_TAG = TAGS.registerItemTag("animal_armor");

    public static void bootstrap() {
        // NO-OP
    }

    public static TagKey<Item> getPrimaryEnchantableItemTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ITEM, resourceKey.location().withPrefix("primary_enchantable/"));
    }

    public static TagKey<Item> getSecondaryEnchantableItemTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ITEM, resourceKey.location().withPrefix("secondary_enchantable/"));
    }

    public static TagKey<Enchantment> getInclusiveSetEnchantmentTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ENCHANTMENT, resourceKey.location().withPrefix("inclusive_set/"));
    }
}
