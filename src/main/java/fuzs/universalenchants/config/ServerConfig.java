package fuzs.universalenchants.config;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.annotation.Config;
import fuzs.puzzleslib.config.serialization.EntryCollectionBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;

public class ServerConfig extends AbstractConfig {
    @Config
    public ItemCompatibilityConfig itemCompatibility = new ItemCompatibilityConfig();
    @Config(category = "enchantment_compatibility", description = "Allow infinity and mending to be applied at the same time.")
    public boolean infinityMendingFix = false;
    @Config(category = "enchantment_compatibility", description = "Allow multishot and piercing to be applied at the same time.")
    public boolean multishotPiercingFix = false;
    @Config(category = "enchantment_improvements", description = "Infinity enchantment no longer requires a single arrow to be present in the player inventory.")
    public boolean trueInfinity = true;
    @Config(category = "enchantment_improvements", description = "Disables damage immunity when hit by a projectile. Makes it possible for entities to be hit by multiple projectiles at once (mainly useful for the multishot enchantment).")
    public boolean noProjectileImmunity = true;
    @Config(name = "looting_boosts_xp", category = "enchantment_improvements", description = "Looting also affects dropped experience points.")
    public boolean lootingBoostsXp = true;

    public ServerConfig() {
        super("");
    }

    @Override
    protected void afterConfigReload() {
        this.itemCompatibility.afterConfigReload();
    }

    public static class ItemCompatibilityConfig extends AbstractConfig {
        private static final String COMPATIBILITY_DESCRIPTION = "Additional enchantments to be made usable with ";
        private static final String BLACKLIST_DESCRIPTION = " to be disabled from receiving additional enchantments.";

        @Config(name = "sword_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "swords.")
        private List<String> swordEnchantmentsRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ENCHANTMENTS, Enchantments.IMPALING);
        @Config(name = "axe_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "axes.")
        private List<String> axeEnchantmentsRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ENCHANTMENTS, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.IMPALING);
        @Config(name = "trident_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "tridents.")
        private List<String> tridentEnchantmentsRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ENCHANTMENTS, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.QUICK_CHARGE, Enchantments.PIERCING);
        @Config(name = "bow_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "bows.")
        private List<String> bowEnchantmentsRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ENCHANTMENTS, Enchantments.PIERCING, Enchantments.MULTISHOT, Enchantments.QUICK_CHARGE, Enchantments.MOB_LOOTING);
        @Config(name = "crossbow_enchantments", category = "enchantments", description = COMPATIBILITY_DESCRIPTION + "crossbows.")
        private List<String> crossbowEnchantmentsRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ENCHANTMENTS, Enchantments.FLAMING_ARROWS, Enchantments.PUNCH_ARROWS, Enchantments.POWER_ARROWS, Enchantments.INFINITY_ARROWS, Enchantments.MOB_LOOTING);
        @Config(name = "sword_blacklist", category = "blacklists", description = "Swords" + BLACKLIST_DESCRIPTION)
        private List<String> swordBlacklistRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ITEMS);
        @Config(name = "axe_blacklist", category = "blacklists", description = "Axes" + BLACKLIST_DESCRIPTION)
        private List<String> axeBlacklistRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ITEMS);
        @Config(name = "trident_blacklist", category = "blacklists", description = "Tridents" + BLACKLIST_DESCRIPTION)
        private List<String> tridentBlacklistRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ITEMS);
        @Config(name = "bow_blacklist", category = "blacklists", description = "Bows" + BLACKLIST_DESCRIPTION)
        private List<String> bowBlacklistRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ITEMS);
        @Config(name = "crossbow_blacklist", category = "blacklists", description = "Crossbows" + BLACKLIST_DESCRIPTION)
        private List<String> crossbowBlacklistRaw = EntryCollectionBuilder.getKeyList(ForgeRegistries.ITEMS);

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

        public ItemCompatibilityConfig() {
            super("item_compatibility");
            this.addComment("Only enchantments included by default are guaranteed to work. While any modded enchantments or other vanilla enchantments can work, they are highly unlikely to do so.",
                    "The blacklists for each item group are supposed to disable items which can be enchanted, but where the enchantments do not function as expected.",
                    EntryCollectionBuilder.CONFIG_DESCRIPTION);
        }

        @Override
        protected void afterConfigReload() {
            EntryCollectionBuilder<Enchantment> enchantmentBuilder = EntryCollectionBuilder.of(ForgeRegistries.ENCHANTMENTS);
            this.swordEnchantments = enchantmentBuilder.buildSet(this.swordEnchantmentsRaw);
            this.axeEnchantments = enchantmentBuilder.buildSet(this.axeEnchantmentsRaw);
            this.tridentEnchantments = enchantmentBuilder.buildSet(this.tridentEnchantmentsRaw);
            this.bowEnchantments = enchantmentBuilder.buildSet(this.bowEnchantmentsRaw);
            this.crossbowEnchantments = enchantmentBuilder.buildSet(this.crossbowEnchantmentsRaw);
            EntryCollectionBuilder<Item> itemBuilder = EntryCollectionBuilder.of(ForgeRegistries.ITEMS);
            this.swordBlacklist = itemBuilder.buildSet(this.swordBlacklistRaw);
            this.axeBlacklist = itemBuilder.buildSet(this.axeBlacklistRaw);
            this.tridentBlacklist = itemBuilder.buildSet(this.tridentBlacklistRaw);
            this.bowBlacklist = itemBuilder.buildSet(this.bowBlacklistRaw);
            this.crossbowBlacklist = itemBuilder.buildSet(this.crossbowBlacklistRaw);
        }
    }
}
