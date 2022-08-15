package fuzs.universalenchants.init;

import fuzs.puzzleslib.capability.ForgeCapabilityController;
import fuzs.puzzleslib.capability.data.CapabilityKey;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.capability.ArrowLootingCapabilityImpl;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ForgeModRegistry {
    private static final ForgeCapabilityController CAPABILITIES = ForgeCapabilityController.of(UniversalEnchants.MOD_ID);
    public static final CapabilityKey<ArrowLootingCapability> ARROW_LOOTING_CAPABILITY = CAPABILITIES.registerEntityCapability("arrow_looting", ArrowLootingCapability.class, entity -> new ArrowLootingCapabilityImpl(), AbstractArrow.class, new CapabilityToken<ArrowLootingCapability>() {});

    public static void touch() {

    }
}
