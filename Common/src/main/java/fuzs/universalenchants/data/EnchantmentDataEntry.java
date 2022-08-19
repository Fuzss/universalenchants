package fuzs.universalenchants.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class EnchantmentDataEntry<T> {

    abstract void dissolve(Set<T> items) throws JsonSyntaxException;

    public abstract void serialize(JsonArray jsonArray);

    public static Builder defaultBuilder(Enchantment enchantment) {
        Builder builder = new Builder().add(EnchantmentDataManager.VANILLA_ENCHANTMENT_CATEGORIES.get(enchantment));
        // don't add the enchantment itself, the user is not supposed to remove it
        // we still need this, it will be manually added back later
        Registry.ENCHANTMENT.stream().filter(Predicate.not(enchantment::isCompatibleWith)).filter(other -> enchantment != other).forEach(builder::add);
        return builder;
    }

    public static class IncompatibleEntry extends EnchantmentDataEntry<Enchantment> {
        public final Set<Enchantment> incompatibles = Sets.newHashSet();

        @Override
        void dissolve(Set<Enchantment> items) throws JsonSyntaxException {
            items.addAll(this.incompatibles);
        }

        @Override
        public void serialize(JsonArray jsonArray) {
            for (Enchantment enchantment : this.incompatibles) {
                jsonArray.add(Registry.ENCHANTMENT.getKey(enchantment).toString());
            }
        }

        public static IncompatibleEntry deserialize(String... s) throws JsonSyntaxException {
            IncompatibleEntry entry = new IncompatibleEntry();
            Stream.of(s).map(ResourceLocation::new)
                    .peek(id -> {
                        if (!Registry.ENCHANTMENT.containsKey(id)) throw new JsonSyntaxException("No enchantment with name %s found".formatted(id));
                    })
                    .map(Registry.ENCHANTMENT::get)
                    .forEach(entry.incompatibles::add);
            return entry;
        }
    }

    public static class Builder {
        private final List<EnchantmentDataEntry<?>> entries = Lists.newArrayList();
        private final IncompatibleEntry incompatibleEntry = new IncompatibleEntry();

        private Builder() {
            this.entries.add(this.incompatibleEntry);
        }

        public Builder add(Item item) {
            return this.add(item, false);
        }

        public Builder add(Item item, boolean exclude) {
            EnchantmentCategoryEntry.ItemEntry entry = new EnchantmentCategoryEntry.ItemEntry(item);
            entry.setExclude(exclude);
            this.entries.add(entry);
            return this;
        }

        public Builder add(EnchantmentCategory category) {
            return this.add(category, false);
        }

        public Builder add(EnchantmentCategory category, boolean exclude) {
            EnchantmentCategoryEntry.CategoryEntry entry = new EnchantmentCategoryEntry.CategoryEntry(category);
            entry.setExclude(exclude);
            this.entries.add(entry);
            return this;
        }

        public Builder add(TagKey<Item> tag) {
            return this.add(tag, false);
        }

        public Builder add(TagKey<Item> tag, boolean exclude) {
            EnchantmentCategoryEntry.TagEntry entry = new EnchantmentCategoryEntry.TagEntry(tag);
            entry.setExclude(exclude);
            this.entries.add(entry);
            return this;
        }

        public Builder add(Enchantment incompatible) {
            this.incompatibleEntry.incompatibles.add(incompatible);
            return this;
        }

        public Builder remove(Enchantment incompatible) {
            this.incompatibleEntry.incompatibles.remove(incompatible);
            return this;
        }

        public List<EnchantmentDataEntry<?>> build() {
            return ImmutableList.copyOf(this.entries);
        }
    }
}
