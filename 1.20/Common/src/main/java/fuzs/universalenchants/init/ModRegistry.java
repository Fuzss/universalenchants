package fuzs.universalenchants.init;

import fuzs.puzzleslib.api.capability.v2.CapabilityController;
import fuzs.puzzleslib.api.capability.v2.data.CapabilityKey;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.capability.ArrowLootingCapabilityImpl;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class ModRegistry {
    static final CapabilityController CAPABILITIES = CapabilityController.from(UniversalEnchants.MOD_ID);
    public static final CapabilityKey<ArrowLootingCapability> ARROW_LOOTING_CAPABILITY = CAPABILITIES.registerEntityCapability("arrow_looting", ArrowLootingCapability.class, entity -> new ArrowLootingCapabilityImpl(), AbstractArrow.class);

    public static void touch() {

    }
}
