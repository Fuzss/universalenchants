package fuzs.universalenchants.init;

import fuzs.puzzleslib.api.capability.v3.CapabilityController;
import fuzs.puzzleslib.api.capability.v3.data.EntityCapabilityKey;
import fuzs.puzzleslib.api.init.v3.tags.BoundTagFactory;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.Block;

public class ModRegistry {
    static final BoundTagFactory TAGS = BoundTagFactory.make(UniversalEnchants.MOD_ID);
    public static final TagKey<Block> FROSTED_ICE_REPLACEABLES = TAGS.registerBlockTag("frosted_ice_replaceables");
    static final CapabilityController CAPABILITIES = CapabilityController.from(UniversalEnchants.MOD_ID);
    public static final EntityCapabilityKey<AbstractArrow, ArrowLootingCapability> ARROW_LOOTING_CAPABILITY = CAPABILITIES.registerEntityCapability(
            "arrow_looting",
            ArrowLootingCapability.class,
            ArrowLootingCapability::new,
            AbstractArrow.class
    );

    public static void touch() {

    }
}
