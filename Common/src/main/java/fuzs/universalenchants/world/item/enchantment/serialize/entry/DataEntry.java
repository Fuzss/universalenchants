package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public abstract class DataEntry<T> {

    public abstract void dissolve(Set<T> items) throws JsonSyntaxException;

    public abstract void serialize(JsonArray jsonArray);

    public static Builder defaultBuilder(Enchantment enchantment) {
        Builder builder = new Builder(enchantment).add(BuiltInEnchantmentDataManager.INSTANCE.getVanillaCategory(enchantment));
        // don't add the enchantment itself, the user is not supposed to remove it
        // we still need this, it will be manually added back later
        BuiltInRegistries.ENCHANTMENT.stream().filter(Predicate.not(enchantment::isCompatibleWith)).filter(other -> enchantment != other).forEach(builder::add);
        return builder;
    }

    public static class Builder {
        private final Enchantment enchantment;
        private final List<DataEntry<?>> entries = Lists.newArrayList();
        private final IncompatibleEntry incompatibleEntry = new IncompatibleEntry();

        private Builder(Enchantment enchantment) {
            this.enchantment = enchantment;
            this.entries.add(this.incompatibleEntry);
        }

        public Builder add(Item item) {
            return this.add(item, false);
        }

        public Builder add(Item item, boolean exclude) {
            Objects.requireNonNull(item, "item for enchantment %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            TypeEntry.ItemEntry entry = new TypeEntry.ItemEntry(item);
            entry.setExclude(exclude);
            this.entries.add(entry);
            return this;
        }

        public Builder add(EnchantmentCategory category) {
            return this.add(category, false);
        }

        public Builder add(EnchantmentCategory category, boolean exclude) {
            Objects.requireNonNull(category, "category for enchantment %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            if (!BuiltInEnchantmentDataManager.INSTANCE.testVanillaCategory(category)) throw new IllegalArgumentException("Cannot add custom category %s to enchantment data entry builder for %s".formatted(category, BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            TypeEntry.CategoryEntry entry = new TypeEntry.CategoryEntry(category);
            entry.setExclude(exclude);
            this.entries.add(entry);
            return this;
        }

        public Builder add(TagKey<Item> tag) {
            return this.add(tag, false);
        }

        public Builder add(TagKey<Item> tag, boolean exclude) {
            Objects.requireNonNull(tag, "tag for enchantment %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            TypeEntry.TagEntry entry = new TypeEntry.TagEntry(tag);
            entry.setExclude(exclude);
            this.entries.add(entry);
            return this;
        }

        public Builder add(Enchantment incompatible) {
            Objects.requireNonNull(incompatible, "incompatible enchantment for %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            this.incompatibleEntry.incompatibles.add(incompatible);
            return this;
        }

        public Builder remove(Enchantment incompatible) {
            Objects.requireNonNull(incompatible, "incompatible enchantment for %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            this.incompatibleEntry.incompatibles.remove(incompatible);
            return this;
        }

        public List<DataEntry<?>> build() {
            return ImmutableList.copyOf(this.entries);
        }
    }
}
