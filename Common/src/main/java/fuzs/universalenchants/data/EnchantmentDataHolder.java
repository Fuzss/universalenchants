package fuzs.universalenchants.data;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fuzs.universalenchants.core.ModServices;
import fuzs.universalenchants.mixin.accessor.EnchantmentAccessor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class EnchantmentDataHolder {
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Set<EnchantmentCategory> SPECIALIZED_ARMOR_CATEGORIES = ImmutableSet.of(EnchantmentCategory.ARMOR_FEET, EnchantmentCategory.ARMOR_LEGS, EnchantmentCategory.ARMOR_CHEST, EnchantmentCategory.ARMOR_HEAD);
    private static final Map<String, EnchantmentCategory> CUSTOM_ENCHANTMENT_CATEGORIES = Maps.newHashMap();

    private final Enchantment enchantment;
    private final EnchantmentCategory category;
    @Nullable
    private List<EnchantmentCategoryEntry> categoryEntries;
    @Nullable
    private EnchantmentDataEntry.IncompatibleEntry incompatibleEntry;
    private Set<Item> items;
    private Set<Enchantment> incompatibles;

    public EnchantmentDataHolder(Enchantment enchantment) {
        this.enchantment = enchantment;
        this.category = getOrBuildCategory(enchantment, this::canEnchant);
    }

    public void invalidate() {
        this.categoryEntries = null;
        this.incompatibleEntry = null;
        this.items = null;
        this.incompatibles = null;
        // reset to vanilla enchantment category for now just in case something goes wrong during rebuilding
        this.setEnchantmentCategory();
    }

    public void submit(EnchantmentCategoryEntry entry) {
        if (this.categoryEntries == null) this.categoryEntries = Lists.newArrayList();
        this.categoryEntries.add(entry);
    }

    public void submit(EnchantmentDataEntry.IncompatibleEntry entry) {
        if (this.incompatibleEntry != null) throw new IllegalStateException("Incompatible entry on enchantment data holder for %s already set".formatted(Registry.ENCHANTMENT.getKey(this.enchantment)));
        this.incompatibleEntry = entry;
    }

    public void setEnchantmentCategory() {
        if (this.categoryEntries != null) {
            ((EnchantmentAccessor) this.enchantment).setCategory(this.category);
            // need this to make horse armor work for frost walker and soul speed (kinda breaks soul speed as horse armor is equipped in chest slot, but soul speed attempts to damage boots slot, but since horse armor has no durability anyway and there's nothing equipped in the boots slot that's fine)
            // all other armor enchantments already set all slots (even the specialized ones such as respiration or feather falling)
            // do this here dynamically to better support modded enchantments
            if (SPECIALIZED_ARMOR_CATEGORIES.contains(this.vanillaCategory())) {
                ((EnchantmentAccessor) this.enchantment).setSlots(ARMOR_SLOTS);
            }
        } else {
            ((EnchantmentAccessor) this.enchantment).setCategory(this.vanillaCategory());
        }
    }

    private EnchantmentCategory vanillaCategory() {
        return EnchantmentDataManager.DEFAULT_ENCHANTMENT_CATEGORIES.get(this.enchantment);
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
            for (EnchantmentCategoryEntry entry : this.categoryEntries) {
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

    private static EnchantmentCategory getOrBuildCategory(Enchantment enchantment, Predicate<Item> canApplyTo) {
        ResourceLocation id = Registry.ENCHANTMENT.getKey(enchantment);
        String name = "%s_%s".formatted(id.getNamespace(), id.getPath()).toUpperCase(Locale.ROOT);
        return CUSTOM_ENCHANTMENT_CATEGORIES.computeIfAbsent(name, name1 -> ModServices.ABSTRACTIONS.createEnchantmentCategory(name1, canApplyTo));
    }
}
