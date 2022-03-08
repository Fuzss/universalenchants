package fuzs.universalenchants.registry;

import fuzs.puzzleslib.capability.CapabilityController;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.capability.ArrowLootingCapabilityImpl;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModRegistry {
    private static final CapabilityController CAPABILITIES = CapabilityController.of(UniversalEnchants.MOD_ID);
    public static final Capability<ArrowLootingCapability> ARROW_LOOTING_CAPABILITY = CAPABILITIES.registerEntityCapability("arrow_looting", ArrowLootingCapability.class, entity -> new ArrowLootingCapabilityImpl(), AbstractArrow.class, new CapabilityToken<ArrowLootingCapability>() {});

    public static void touch() {

    }
}
