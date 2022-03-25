package fuzs.universalenchants.handler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.mixin.accessor.EnchantmentAccessor;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemCompatManager {
    public static final ItemCompatManager INSTANCE = new ItemCompatManager();

    private static final ExtendedEnchantmentCategory SWORD = new ExtendedEnchantmentCategory(item -> item instanceof SwordItem, () -> UniversalEnchants.CONFIG.server().itemCompatibility.swordEnchantments, () -> UniversalEnchants.CONFIG.server().itemCompatibility.swordBlacklist);
    private static final ExtendedEnchantmentCategory AXE = new ExtendedEnchantmentCategory(item -> item instanceof AxeItem, () -> UniversalEnchants.CONFIG.server().itemCompatibility.axeEnchantments, () -> UniversalEnchants.CONFIG.server().itemCompatibility.axeBlacklist);
    private static final ExtendedEnchantmentCategory TRIDENT = new ExtendedEnchantmentCategory(item -> item instanceof TridentItem, () -> UniversalEnchants.CONFIG.server().itemCompatibility.tridentEnchantments, () -> UniversalEnchants.CONFIG.server().itemCompatibility.tridentBlacklist);
    private static final ExtendedEnchantmentCategory BOW = new ExtendedEnchantmentCategory(item -> item instanceof BowItem, () -> UniversalEnchants.CONFIG.server().itemCompatibility.bowEnchantments, () -> UniversalEnchants.CONFIG.server().itemCompatibility.bowBlacklist);
    private static final ExtendedEnchantmentCategory CROSSBOW = new ExtendedEnchantmentCategory(item -> item instanceof CrossbowItem, () -> UniversalEnchants.CONFIG.server().itemCompatibility.crossbowEnchantments, () -> UniversalEnchants.CONFIG.server().itemCompatibility.crossbowBlacklist);
    private static final Set<ExtendedEnchantmentCategory> EXTENDED_CATEGORIES = ImmutableSet.of(SWORD, AXE, TRIDENT, BOW, CROSSBOW);

    private final Map<Enchantment, EnchantmentCategoryData> enchantsToCategoryData = Maps.newHashMap();

    public void buildData() {
        this.enchantsToCategoryData.values().forEach(EnchantmentCategoryData::beginRebuilding);
        for (ExtendedEnchantmentCategory category : EXTENDED_CATEGORIES) {
            for (Enchantment enchantment : category.additionalEnchantments().get()) {
                EnchantmentCategoryData data = this.enchantsToCategoryData.get(enchantment);
                if (data != null) {
                    data.merge(enchantment, category);
                } else {
                    this.enchantsToCategoryData.put(enchantment, new EnchantmentCategoryData(enchantment, category));
                }
            }
        }
        this.enchantsToCategoryData.values().forEach(EnchantmentCategoryData::finishRebuilding);
    }

    private static record ExtendedEnchantmentCategory(Predicate<Item> delegate, Supplier<Set<Enchantment>> additionalEnchantments, Supplier<Set<Item>> itemBlacklist) {

    }

    private static class EnchantmentCategoryData {
        private final Enchantment enchantment;
        private final EnchantmentCategory vanillaCategory;
        private final List<ExtendedEnchantmentCategory> customCategories = Lists.newArrayList();
        private EnchantmentCategory customBuiltCategory;
        private Set<Item> blacklistCache;
        private boolean rebuilding = true;

        public EnchantmentCategoryData(Enchantment enchantment, ExtendedEnchantmentCategory customCategory) {
            this.enchantment = enchantment;
            this.vanillaCategory = enchantment.category;
            this.customCategories.add(customCategory);
            this.customBuildCategory();
        }

        public void merge(Enchantment enchantment, ExtendedEnchantmentCategory customCategory) {
            if (this.enchantment != enchantment) throw new IllegalArgumentException("Can only merge enchantment data for same type");
            this.customCategories.add(customCategory);
        }

        public void beginRebuilding() {
            this.rebuilding = true;
            this.clear();
        }

        public void finishRebuilding() {
            this.buildBlacklistCache();
            this.setEnchantmentCategory();
            this.rebuilding = false;
        }

        private void clear() {
            this.customCategories.clear();
            this.blacklistCache = null;
        }

        private void buildBlacklistCache() {
            Set<Item> blacklistCache = Sets.newHashSet();
            for (ExtendedEnchantmentCategory category : this.customCategories) {
                Set<Item> blacklist = category.itemBlacklist().get();
                blacklistCache.addAll(blacklist);
            }
            this.blacklistCache = blacklistCache;
        }

        private void setEnchantmentCategory() {
            if (this.customCategories.isEmpty()) {
                ((EnchantmentAccessor) this.enchantment).setCategory(this.vanillaCategory);
            } else {
                ((EnchantmentAccessor) this.enchantment).setCategory(this.customBuiltCategory);
            }
        }

        private void customBuildCategory() {
            if (this.customBuiltCategory == null) {
                String name = ForgeRegistries.ENCHANTMENTS.getKey(this.enchantment).getPath().toUpperCase(Locale.ROOT);
                this.customBuiltCategory = EnchantmentCategory.create(UniversalEnchants.MOD_ID.toUpperCase(Locale.ROOT).concat("_" + name), this::canEnchant);
            }
        }

        private boolean canEnchant(Item item) {
            if (this.vanillaCategory.canEnchant(item)) return true;
            if (this.rebuilding) return false;
            if (this.blacklistCache.contains(item)) return false;
            for (ExtendedEnchantmentCategory category : this.customCategories) {
                if (category.delegate().test(item)) return true;
            }
            return false;
        }
    }
}
