package fuzs.universalenchants.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.core.ModServices;
import fuzs.universalenchants.mixin.accessor.EnchantmentAccessor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantmentDataHolder {
    private static final Map<String, EnchantmentCategory> CUSTOM_ENCHANTMENT_CATEGORIES = Maps.newHashMap();

    private final Enchantment enchantment;
    private final EnchantmentCategory vanillaCategory;
    private final EnchantmentCategory customBuiltCategory;
    @Nullable
    private List<EnchantmentDataEntry> entries;
    private Set<Item> cache;
    @Nullable
    private Set<Enchantment> incompatible;

    public EnchantmentDataHolder(Enchantment enchantment) {
        this.enchantment = enchantment;
        this.vanillaCategory = enchantment.category;
        this.customBuiltCategory = getOrBuildCategory(enchantment, this::canEnchant);
    }

    public void invalidate() {
        this.entries = null;
    }

    public void submit(EnchantmentDataEntry entry) {
        if (this.entries == null) this.entries = Lists.newArrayList();
        this.entries.add(entry);
    }

    public void setEnchantmentCategory() {
        if (this.entries == null) {
            ((EnchantmentAccessor) this.enchantment).setCategory(this.vanillaCategory);
        } else {
            ((EnchantmentAccessor) this.enchantment).setCategory(this.customBuiltCategory);
        }
    }

    public boolean isCompatibleWith(Enchantment other, boolean fallback) {
        return this.incompatible != null ? this.incompatible.contains(other) : fallback;
    }

    private boolean canEnchant(Item item) {
        this.dissolve();
        return this.cache.contains(item);
    }

    private void dissolve() {
        if (this.cache == null) {
            Set<Item> include = Sets.newIdentityHashSet();
            Set<Item> exclude = Sets.newIdentityHashSet();
            Objects.requireNonNull(this.entries, "Using invalid enchantment category for enchantment %s, expected vanilla category to be used".formatted(Registry.ENCHANTMENT.getKey(this.enchantment)));
            for (EnchantmentDataEntry entry : this.entries) {
                entry.dissolve(entry.isExclude() ? exclude : include);
            }
            include.removeAll(exclude);
            this.cache = Collections.unmodifiableSet(include);
        }
    }

    private static EnchantmentCategory getOrBuildCategory(Enchantment enchantment, Predicate<Item> canApplyTo) {
        ResourceLocation id = Registry.ENCHANTMENT.getKey(enchantment);
        String name = Stream.of(UniversalEnchants.MOD_ID, id.getPath())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .collect(Collectors.joining("_"));
        return CUSTOM_ENCHANTMENT_CATEGORIES.computeIfAbsent(name, name1 -> ModServices.ABSTRACTIONS.createEnchantmentCategory(name, canApplyTo));
    }
}
