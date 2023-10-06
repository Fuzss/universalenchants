package fuzs.universalenchants.world.item.enchantment;

import fuzs.puzzleslib.api.core.v1.CommonAbstractions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface EnchantmentFeature extends FeatureElement {

    default boolean universalenchants$isEnabled() {
        MinecraftServer minecraftServer = CommonAbstractions.INSTANCE.getGameServer();
        if (minecraftServer != null) {
            FeatureFlagSet featureFlagSet = minecraftServer.getWorldData().enabledFeatures();
            return this.isEnabled(featureFlagSet);
        }
        return true;
    }

    void universalenchants$setRequiredFeatures(FeatureFlagSet requiredFeatures);
}
