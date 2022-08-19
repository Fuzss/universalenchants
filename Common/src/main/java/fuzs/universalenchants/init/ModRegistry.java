package fuzs.universalenchants.init;

import fuzs.puzzleslib.capability.CapabilityController;
import fuzs.puzzleslib.capability.data.CapabilityKey;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;

public class ModRegistry {
    public static final CapabilityKey<ArrowLootingCapability> ARROW_LOOTING_CAPABILITY = CapabilityController.makeCapabilityKey(UniversalEnchants.MOD_ID, "arrow_looting", ArrowLootingCapability.class);

    public static void touch() {

    }
}
