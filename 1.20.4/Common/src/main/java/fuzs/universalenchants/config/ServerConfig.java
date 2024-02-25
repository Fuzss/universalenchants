package fuzs.universalenchants.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.serialization.ConfigDataSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class ServerConfig implements ConfigCore {
    private static final String NEWLY_ENCHANTABLE_NOTICE = "Disabling this will still allow for applying enchantments in an anvil, that needs to be disabled per enchantment in the custom .json configs.";

    @Config(category = "adjustments", description = "Infinity enchantment no longer requires a single arrow to be present in the player inventory.")
    public boolean trueInfinity = true;
    @Config(category = "adjustments", description = "Disables damage immunity when hit by a projectile. Makes it possible for entities to be hit by multiple projectiles at once (mainly useful for the multishot enchantment).")
    public boolean noProjectileImmunity = true;
    @Config(category = "adjustments", description = "Any level of feather falling prevents farmland being trampled.")
    public boolean noFarmlandTrample = true;
    @Config(category = "adjustments", description = "Looting and fortune also affect dropped experience points.")
    public boolean lootBonusBoostsXp = true;
    @Config(category = "adjustments", description = {"Mending no longer repairs items using xp.", "Instead mending allows the item to be repaired in the crafting menu in the same way as in an anvil without any experience cost: combining a tool with another one or the appropriate repair item is possible, all enchantments will be preserved (but no new ones from the item repaired with will be added as an anvil would do).", "Additionally, repairing items enchanted with mending (only repairing, no renaming or adding more enchantments) in an anvil doesn't increase the items repair cost any further."})
    public boolean mendingCraftingRepair = false;
    @Config(category = "enchantable_items", name = "horse_armor", description = {"Allow all kinds of horse armor to be enchanted directly in the enchanting table.", NEWLY_ENCHANTABLE_NOTICE})
    public boolean enchantableHorseArmor = true;
    @Config(category = "enchantable_items", name = "shields", description = {"Allow shields to be enchanted directly in the enchanting table.", NEWLY_ENCHANTABLE_NOTICE})
    public boolean enchantableShields = true;
    @Config(category = "enchantable_items", name = "shears", description = {"Allow shears to be enchanted directly in the enchanting table.", NEWLY_ENCHANTABLE_NOTICE})
    public boolean enchantableShears = true;
    @Config(description = "Apply item tag configuration for enchantments usable in an enchanting table.")
    public boolean adjustEnchantingTableEnchantments = true;
    @Config(description = "Apply item tag configuration for enchantments usable in an anvil as well as enchanting table enchantments.")
    public boolean adjustAnvilEnchantments = true;
    @Config(description = "Apply configurations for compatibility between individual enchantments.")
    public boolean adjustEnchantmentCompatibilities = true;
    @Config(name = "max_level_overrides", description = "Provides overrides for maximum enchantment levels.")
    List<String> rawMaxLevelOverrides = List.of("minecraft:efficiency,10");

    public ConfigDataSet<Enchantment> maxLevelOverrides;

    @Override
    public void afterConfigReload() {
        this.maxLevelOverrides = ConfigDataSet.from(Registries.ENCHANTMENT, this.rawMaxLevelOverrides, (integer, o) -> integer != 1 || ((int) o) > 0, int.class);
    }
}
