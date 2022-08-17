package fuzs.universalenchants.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;
import fuzs.puzzleslib.config.serialization.EntryCollectionBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.Set;

public class ServerConfig implements ConfigCore {
    @Config(description = {"Only enchantments included by default are guaranteed to work. While any modded enchantments or other vanilla enchantments can work, they are highly unlikely to do so.",
            "The blacklists for each item group are supposed to disable items which can be enchanted, but where the enchantments do not function as expected.",
            EntryCollectionBuilder.CONFIG_DESCRIPTION})
    public ItemCompatibilityConfig itemCompatibility = new ItemCompatibilityConfig();
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

    public static class ItemCompatibilityConfig implements ConfigCore {
        private static final String COMPATIBILITY_DESCRIPTION = "Additional enchantments to be made usable with ";
        private static final String BLACKLIST_DESCRIPTION = " to be disabled from receiving additional enchantments.";

        @Config(name = "sword_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "swords.")
        List<String> swordEnchantmentsRaw = EntryCollectionBuilder.getKeyList(Registry.ENCHANTMENT_REGISTRY, Enchantments.IMPALING);
        @Config(name = "axe_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "axes.")
        List<String> axeEnchantmentsRaw = EntryCollectionBuilder.getKeyList(Registry.ENCHANTMENT_REGISTRY, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.IMPALING);
        @Config(name = "trident_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "tridents.")
        List<String> tridentEnchantmentsRaw = EntryCollectionBuilder.getKeyList(Registry.ENCHANTMENT_REGISTRY, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.QUICK_CHARGE, Enchantments.PIERCING);
        @Config(name = "bow_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "bows.")
        List<String> bowEnchantmentsRaw = EntryCollectionBuilder.getKeyList(Registry.ENCHANTMENT_REGISTRY, Enchantments.PIERCING, Enchantments.MULTISHOT, Enchantments.QUICK_CHARGE, Enchantments.MOB_LOOTING);
        @Config(name = "crossbow_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "crossbows.")
        List<String> crossbowEnchantmentsRaw = EntryCollectionBuilder.getKeyList(Registry.ENCHANTMENT_REGISTRY, Enchantments.FLAMING_ARROWS, Enchantments.PUNCH_ARROWS, Enchantments.POWER_ARROWS, Enchantments.INFINITY_ARROWS, Enchantments.MOB_LOOTING);
        @Config(name = "sword_blacklist", category = "blacklists", description = "Swords" + BLACKLIST_DESCRIPTION)
        List<String> swordBlacklistRaw = EntryCollectionBuilder.getKeyList(Registry.ITEM_REGISTRY);
        @Config(name = "axe_blacklist", category = "blacklists", description = "Axes" + BLACKLIST_DESCRIPTION)
        List<String> axeBlacklistRaw = EntryCollectionBuilder.getKeyList(Registry.ITEM_REGISTRY);
        @Config(name = "trident_blacklist", category = "blacklists", description = "Tridents" + BLACKLIST_DESCRIPTION)
        List<String> tridentBlacklistRaw = EntryCollectionBuilder.getKeyList(Registry.ITEM_REGISTRY);
        @Config(name = "bow_blacklist", category = "blacklists", description = "Bows" + BLACKLIST_DESCRIPTION)
        List<String> bowBlacklistRaw = EntryCollectionBuilder.getKeyList(Registry.ITEM_REGISTRY);
        @Config(name = "crossbow_blacklist", category = "blacklists", description = "Crossbows" + BLACKLIST_DESCRIPTION)
        List<String> crossbowBlacklistRaw = EntryCollectionBuilder.getKeyList(Registry.ITEM_REGISTRY);

        public Set<Enchantment> swordEnchantments;
        public Set<Enchantment> axeEnchantments;
        public Set<Enchantment> tridentEnchantments;
        public Set<Enchantment> bowEnchantments;
        public Set<Enchantment> crossbowEnchantments;
        public Set<Item> swordBlacklist;
        public Set<Item> axeBlacklist;
        public Set<Item> tridentBlacklist;
        public Set<Item> bowBlacklist;
        public Set<Item> crossbowBlacklist;

        @Override
        public void afterConfigReload() {
            EntryCollectionBuilder<Enchantment> enchantmentBuilder = EntryCollectionBuilder.of(Registry.ENCHANTMENT_REGISTRY);
            this.swordEnchantments = enchantmentBuilder.buildSet(this.swordEnchantmentsRaw);
            this.axeEnchantments = enchantmentBuilder.buildSet(this.axeEnchantmentsRaw);
            this.tridentEnchantments = enchantmentBuilder.buildSet(this.tridentEnchantmentsRaw);
            this.bowEnchantments = enchantmentBuilder.buildSet(this.bowEnchantmentsRaw);
            this.crossbowEnchantments = enchantmentBuilder.buildSet(this.crossbowEnchantmentsRaw);
            EntryCollectionBuilder<Item> itemBuilder = EntryCollectionBuilder.of(Registry.ITEM_REGISTRY);
            this.swordBlacklist = itemBuilder.buildSet(this.swordBlacklistRaw);
            this.axeBlacklist = itemBuilder.buildSet(this.axeBlacklistRaw);
            this.tridentBlacklist = itemBuilder.buildSet(this.tridentBlacklistRaw);
            this.bowBlacklist = itemBuilder.buildSet(this.bowBlacklistRaw);
            this.crossbowBlacklist = itemBuilder.buildSet(this.crossbowBlacklistRaw);
        }
    }
}
