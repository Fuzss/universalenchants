package fuzs.universalenchants.world.item.enchantment.serialize;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.IncompatibleEntry;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.TypeEntry;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EnchantmentHolder {
    private final Enchantment enchantment;
    private final EnchantmentCategory vanillaCategory;
    private final EnchantmentCategory category;
    private List<TypeEntry> categoryEntries;
    private IncompatibleEntry incompatibleEntry;
    @Nullable
    private Set<Item> items;
    @Nullable
    private Set<Enchantment> incompatibles;

    public EnchantmentHolder(Enchantment enchantment) {
        this.enchantment = enchantment;
        this.vanillaCategory = BuiltInEnchantmentDataManager.INSTANCE.getVanillaCategory(enchantment);
        this.category = BuiltInEnchantmentDataManager.INSTANCE.getOrBuildCustomCategory(enchantment, this::canEnchant);
    }

    public void invalidate() {
        this.categoryEntries = null;
        this.incompatibleEntry = null;
        this.items = null;
        this.incompatibles = null;
        // reset to vanilla enchantment category for now just in case something goes wrong during rebuilding
        BuiltInEnchantmentDataManager.INSTANCE.setEnchantmentCategory(this.enchantment, this.vanillaCategory);
    }

    public void initializeCategoryEntries() {
        if (this.categoryEntries == null) this.categoryEntries = Lists.newArrayList();
    }

    public void submit(TypeEntry entry) {
        Objects.requireNonNull(this.categoryEntries, "category entries for enchantment %s is null".formatted(Registry.ENCHANTMENT.getKey(this.enchantment)));
        if (!entry.isEmpty()) this.categoryEntries.add(entry);
    }

    public void submit(IncompatibleEntry entry) {
        if (this.incompatibleEntry != null) throw new IllegalStateException("Incompatible entry on enchantment data holder for %s already set".formatted(Registry.ENCHANTMENT.getKey(this.enchantment)));
        this.incompatibleEntry = entry;
    }

    public void applyEnchantmentCategory() {
        if (this.categoryEntries != null) {
            BuiltInEnchantmentDataManager.INSTANCE.setEnchantmentCategory(this.enchantment, this.category);
        }
    }

    private boolean canEnchant(Item item) {
        this.dissolveItems();
        return this.items.contains(item);
    }

    private void dissolveItems() {
        if (this.items == null) {
            Set<Item> include = Sets.newIdentityHashSet();
            Set<Item> exclude = Sets.newIdentityHashSet();
            Objects.requireNonNull(this.categoryEntries, "Using invalid enchantment category for enchantment %s, expected vanilla category to be used".formatted(Registry.ENCHANTMENT.getKey(this.enchantment)));
            for (TypeEntry entry : this.categoryEntries) {
                entry.dissolve(entry.isExclude() ? exclude : include);
            }
            include.removeAll(exclude);
            this.items = Collections.unmodifiableSet(include);
        }
    }

    public boolean isCompatibleWith(Enchantment other, boolean fallback) {
        if (this.incompatibleEntry != null) {
            this.dissolveIncompatibles();
            return !this.incompatibles.contains(other);
        }
        return fallback;
    }

    private void dissolveIncompatibles() {
        if (this.incompatibles == null) {
            Objects.requireNonNull(this.incompatibleEntry, "Using invalid enchantment incompatibility check for enchantment %s, expected vanilla check to be used".formatted(Registry.ENCHANTMENT.getKey(this.enchantment)));
            Set<Enchantment> incompatibles = this.incompatibleEntry.incompatibles;
            // adding this back manually as the user isn't supposed to be able to remove it
            incompatibles.add(this.enchantment);
            this.incompatibles = Collections.unmodifiableSet(incompatibles);
        }
    }
}
