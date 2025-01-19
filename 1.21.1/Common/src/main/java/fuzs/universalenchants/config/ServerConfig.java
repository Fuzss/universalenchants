package fuzs.universalenchants.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

public class ServerConfig implements ConfigCore {
    private static final String ENCHANTABLE_DESCRIPTION = "Disabling this will still allow for applying enchantments in an anvil, that needs to be disabled per enchantment.";

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
    @Config(
            category = "adjustments",
            description = "Frost walker regenerates ice blocks while standing still, destroys water plants, and works while jumping."
    )
    public boolean betterFrostWalker = true;
    @Config(
            category = "enchantable", name = "horse_armor", description = {
            "Allow all kinds of horse armor to be enchanted directly in the enchanting table.", ENCHANTABLE_DESCRIPTION
    }
    )
    public boolean enchantableHorseArmor = true;
    @Config(
            category = "enchantable", name = "shields", description = {
            "Allow shields to be enchanted directly in the enchanting table.", ENCHANTABLE_DESCRIPTION
    }
    )
    public boolean enchantableShields = true;
    @Config(
            category = "enchantable", name = "shears", description = {
            "Allow shears to be enchanted directly in the enchanting table.", ENCHANTABLE_DESCRIPTION
    }
    )
    public boolean enchantableShears = true;
    @Config(description = "Active enchantments can be toggled for individual items, allowing for switching between otherwise incompatible enchantments such as Fortune and Silk Touch on the fly.")
    public boolean allowStoringEnchantments = true;
}
