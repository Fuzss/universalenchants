package fuzs.universalenchants.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

public class ServerConfig implements ConfigCore {
    @Config(category = "enchantment_compatibility", description = "Allow infinity and mending to be applied at the same time.")
    public boolean infinityMendingFix = true;
    @Config(category = "enchantment_compatibility", description = "Allow multishot and piercing to be applied at the same time.")
    public boolean multishotPiercingFix = true;
    @Config(category = "enchantment_compatibility", description = {"Allow sharpness and specialized damage enchantments (e.g. smite, impaling and bane of arthropods) to be applied at the same time.", "This does not make the specialized enchantments compatible with each other."})
    public boolean damageEnchantmentsFix = true;
    @Config(category = "enchantment_compatibility", description = {"Allow protection and specialized protection enchantments (e.g. fire protection, projectile protection and explosion protection) to be applied at the same time.", "This does not make the specialized enchantments compatible with each other."})
    public boolean protectionEnchantmentsFix = true;
    @Config(category = "enchantment_improvements", description = "Infinity enchantment no longer requires a single arrow to be present in the player inventory.")
    public boolean trueInfinity = true;
    @Config(category = "enchantment_improvements", description = "Disables damage immunity when hit by a projectile. Makes it possible for entities to be hit by multiple projectiles at once (mainly useful for the multishot enchantment).")
    public boolean noProjectileImmunity = true;
    @Config(category = "enchantment_improvements", description = "Any level of feather falling prevents farmland being trampled.")
    public boolean noFarmlandTrample = true;
    @Config(category = "enchantment_improvements", description = "Looting also affects dropped experience points.")
    public boolean lootingBoostsXp = true;
    @Config(category = "enchantment_improvements", description = {"Remove the max level cap from the /enchant command, also allow overriding and removing (by setting the level to 0) existing enchantment levels.", "Additionally make enchanting books work via the command."})
    public boolean fixEnchantCommand = true;
}
