package fuzs.universalenchants.init;

import fuzs.puzzleslib.capability.FabricCapabilityController;
import fuzs.puzzleslib.capability.data.CapabilityKey;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.capability.ArrowLootingCapabilityImpl;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class FabricModRegistry {
    private static final FabricCapabilityController CAPABILITIES = FabricCapabilityController.of(UniversalEnchants.MOD_ID);
    public static final CapabilityKey<ArrowLootingCapability> ARROW_LOOTING_CAPABILITY = CAPABILITIES.registerEntityCapability("arrow_looting", ArrowLootingCapability.class, entity -> new ArrowLootingCapabilityImpl(), AbstractArrow.class);

    public static void touch() {

    }
}
