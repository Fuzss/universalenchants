package fuzs.universalenchants.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ServerConfig implements ConfigCore {
    @Config(
            category = "adjustments",
            description = "Infinity enchantment no longer requires a single arrow to be present in the player inventory."
    )
    public boolean trueInfinity = true;
    @Config(
            category = "adjustments",
            description = "Disables damage immunity when hit by a projectile. Makes it possible for entities to be hit by multiple projectiles at once (mainly useful for the multishot enchantment)."
    )
    public boolean noProjectileImmunity = true;
    @Config(category = "adjustments", description = "Any level of feather falling prevents farmland being trampled.")
    public boolean noFarmlandTrample = true;
    @Config(category = "adjustments", description = "Looting and fortune also affect dropped experience points.")
    public boolean lootBonusBoostsXp = true;
}
