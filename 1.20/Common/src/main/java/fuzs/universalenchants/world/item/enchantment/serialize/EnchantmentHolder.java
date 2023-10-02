package fuzs.universalenchants.world.item.enchantment.serialize;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.DataEntry;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.IncompatibleEntry;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.TypeEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnchantmentHolder {
    private final Enchantment enchantment;
    private final ResourceLocation id;
    private final EnchantmentCategory vanillaCategory;
    private final EnchantmentCategory category;
    private List<TypeEntry> categoryEntries;
    private List<TypeEntry> anvilEntries;
    private IncompatibleEntry incompatibleEntry;
    @Nullable
    private Set<Item> categoryItems;
    @Nullable
    private Set<Item> anvilItems;
    @Nullable
    private Set<Enchantment> incompatibles;

    public EnchantmentHolder(Enchantment enchantment) {
        this.enchantment = enchantment;
        this.vanillaCategory = BuiltInEnchantmentDataManager.INSTANCE.getVanillaCategory(enchantment);
        this.category = BuiltInEnchantmentDataManager.INSTANCE.getOrBuildCustomCategory(enchantment, this::canEnchant);
        this.id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
    }

    public ResourceLocation id() {
        return this.id;
    }
    
    public void ensureInvalidated() {
        if (this.categoryItems != null || this.anvilItems != null || this.incompatibles != null) {
            throw new IllegalStateException("Holder for enchantment %s has not been invalidated".formatted(this.id));
        }
    }

    public void invalidate() {
        this.categoryEntries = this.anvilEntries = null;
        this.incompatibleEntry = null;
        this.categoryItems = this.anvilItems = null;
        this.incompatibles = null;
        // reset to vanilla enchantment category for now just in case something goes wrong during rebuilding
        BuiltInEnchantmentDataManager.INSTANCE.setEnchantmentCategory(this.enchantment, this.vanillaCategory);
    }

    public void initializeCategoryEntries() {
        if (this.categoryEntries == null) this.categoryEntries = Lists.newArrayList();
    }

    public void initializeAnvilEntries() {
        if (this.anvilEntries == null) this.anvilEntries = Lists.newArrayList();
    }

    public void submitAll(Collection<DataEntry<?>> dataEntries) {
        for (DataEntry<?> entry : dataEntries) {
            if (entry instanceof TypeEntry typeEntry) {
                this.submit(typeEntry);
            } else if (entry instanceof IncompatibleEntry incompatibleEntry1) {
                this.submit(incompatibleEntry1);
            } else {
                throw new IllegalStateException("Unknown data entry type %s".formatted(entry.getClass()));
            }
        }
    }

    public void submit(TypeEntry entry) {
        List<TypeEntry> entries = entry.anvil ? this.anvilEntries : this.categoryEntries;
        Objects.requireNonNull(entries, "entries for enchantment %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
        if (!entry.isEmpty()) entries.add(entry);
    }

    public void submit(IncompatibleEntry entry) {
        if (this.incompatibleEntry != null) throw new IllegalStateException("Incompatible entry on enchantment data holder for %s already set".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
        this.incompatibleEntry = entry;
    }

    public void applyEnchantmentCategory() {
        if (this.categoryEntries != null) {
            BuiltInEnchantmentDataManager.INSTANCE.setEnchantmentCategory(this.enchantment, this.category);
        }
    }

    private boolean canEnchant(Item item) {
        // might be called by other mods when category entries haven't been set up yet, so use vanilla then
        if (this.categoryEntries != null) {
            if (this.categoryItems == null) {
                this.categoryItems = this.dissolveItems(this.categoryEntries);
            }
            return this.categoryItems.contains(item);
        } else {
            return this.vanillaCategory.canEnchant(item);
        }
    }

    public boolean canApplyAtAnvil(ItemStack itemStack) {
        // might be called by other mods when category entries haven't been set up yet, so use vanilla then
        if (this.anvilEntries != null) {
            if (this.anvilItems == null) {
                this.anvilItems = this.dissolveItems(this.anvilEntries);
            }
            return this.anvilItems.contains(itemStack.getItem());
        } else {
            return this.enchantment.canEnchant(itemStack);
        }
    }

    private Set<Item> dissolveItems(List<TypeEntry> entries) {
        Set<Item> include = Sets.newIdentityHashSet();
        Set<Item> exclude = Sets.newIdentityHashSet();
        Objects.requireNonNull(entries, "Using invalid enchantment category for enchantment %s, expected vanilla category to be used".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
        for (TypeEntry entry : entries) {
            entry.dissolve(entry.exclude ? exclude : include);
        }
        include.removeAll(exclude);
        return Collections.unmodifiableSet(include);
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
            Objects.requireNonNull(this.incompatibleEntry, "Using invalid enchantment incompatibility check for enchantment %s, expected vanilla check to be used".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            ImmutableSet.Builder<Enchantment> builder = ImmutableSet.builder();
            builder.addAll(this.incompatibleEntry.getIncompatibles());
            // adding this back manually as the user isn't supposed to be able to remove it
            builder.add(this.enchantment);
            this.incompatibles = builder.build();
        }
    }
}
