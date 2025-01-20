package fuzs.universalenchants.init;

import fuzs.puzzleslib.api.init.v3.tags.TagFactory;
import fuzs.universalenchants.UniversalEnchants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

public class ModRegistry {
    static final TagFactory TAGS = TagFactory.make(UniversalEnchants.MOD_ID);
    public static final TagKey<Block> FROSTED_ICE_REPLACEABLES_BLOCK_TAG = TAGS.registerBlockTag(
            "frosted_ice_replaceables");

    public static void bootstrap() {
        // NO-OP
    }

    public static TagKey<Item> getPrimaryEnchantableItemTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ITEM, resourceKey.location().withPrefix("primary_enchantable/"));
    }

    public static TagKey<Item> getSecondaryEnchantableItemTag(ResourceKey<Enchantment> resourceKey) {
        return TagKey.create(Registries.ITEM, resourceKey.location().withPrefix("secondary_enchantable/"));
    }
}
