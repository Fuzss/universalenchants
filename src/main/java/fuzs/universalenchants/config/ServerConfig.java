package fuzs.universalenchants.config;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.annotation.Config;

public class ServerConfig extends AbstractConfig {
    @Config(category = "compatibility", description = "Allow infinity and mending to be applied at the same time.")
    public boolean infinityMendingFix = true;
    @Config(category = "compatibility", description = "Allow multishot and piercing to be applied at the same time.")
    public boolean multishotPiercingFix = true;
    @Config(category = "improvements", description = "Infinity enchantment no longer requires a single arrow to be present in the player inventory.")
    public boolean trueInfinity = true;
    @Config(category = "improvements", description = "Looting also affects dropped experience points.")
    public boolean lootingBoostsXp = true;

    public ServerConfig() {
        super("");
    }
}
