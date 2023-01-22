package fuzs.universalenchants.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.ValueCallback;
import fuzs.puzzleslib.config.annotation.Config;
import fuzs.puzzleslib.config.core.AbstractConfigBuilder;
import fuzs.puzzleslib.core.CoreServices;

public class ServerConfig implements ConfigCore {
    private static final String NEWLY_ENCHANTABLE_NOTICE = "Disabling this will still allow for applying enchantments in an anvil, that needs to be disabled per enchantment in the custom .json configs.";

    @Config(description = "Infinity enchantment no longer requires a single arrow to be present in the player inventory.")
    public boolean trueInfinity = true;
    @Config(description = "Disables damage immunity when hit by a projectile. Makes it possible for entities to be hit by multiple projectiles at once (mainly useful for the multishot enchantment).")
    public boolean noProjectileImmunity = true;
    @Config(description = "Any level of feather falling prevents farmland being trampled.")
    public boolean noFarmlandTrample = true;
    @Config(description = "Looting also affects dropped experience points.")
    public boolean lootingBoostsXp = true;
    @Config(description = {"Mending no longer repairs items using xp.", "Instead mending allows the item to be repaired in the crafting menu in the same way as in an anvil without any experience cost: combining a tool with another one or the appropriate repair item is possible, all enchantments will be preserved (but no new ones from the item repaired with will be added as an anvil would do).", "Additionally, repairing items enchanted with mending (only repairing, no renaming or adding more enchantments) in an anvil doesn't increase the items repair cost any further."})
    public boolean mendingCraftingRepair = false;
    @Config(description = {"Remove the max level cap from the /enchant command, also allow overriding and removing (by setting the level to 0) existing enchantment levels.", "Additionally make enchanting books work via the command."})
    public boolean fixEnchantCommand = true;
    @Config(description = {"Allow all kinds of horse armor to be enchanted directly in the enchanting table.", NEWLY_ENCHANTABLE_NOTICE})
    public boolean enchantableHorseArmor = true;
    @Config(description = {"Allow shields to be enchanted directly in the enchanting table.", NEWLY_ENCHANTABLE_NOTICE})
    public boolean enchantableShields = true;
    public boolean allowModItemSupport;

    @Override
    public void addToBuilder(AbstractConfigBuilder builder, ValueCallback callback) {
        if (CoreServices.ENVIRONMENT.getModLoader().isForge()) {
            callback.accept(builder.comment("Enchanting a few modded items (e.g. Farmer's Delight's skillet) is broken by the changes this mod makes to how enchantments may be applied to items. This option enables a patch for Forge itself to help better support such items.").define("allow_mod_item_support", true), v -> this.allowModItemSupport = v);
        }
    }
}
