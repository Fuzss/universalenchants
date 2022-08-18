package fuzs.universalenchants.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public abstract class EnchantmentDataEntry<T> {

    abstract void dissolve(Set<T> items) throws JsonSyntaxException;

    public abstract void serialize(JsonArray jsonArray);

    public static Builder defaultBuilder(Enchantment enchantment) {
        Builder builder = new Builder().add(enchantment.category);
        Registry.ENCHANTMENT.stream().filter(Predicate.not(enchantment::isCompatibleWith)).forEach(builder::add);
        return builder;
    }

    public static class IncompatibleEntry extends EnchantmentDataEntry<Enchantment> {
        private final Enchantment incompatible;

        public IncompatibleEntry(Enchantment incompatible) {
            this.incompatible = incompatible;
        }

        @Override
        void dissolve(Set<Enchantment> items) throws JsonSyntaxException {
            items.add(this.incompatible);
        }

        @Override
        public void serialize(JsonArray jsonArray) {
            jsonArray.add(Registry.ENCHANTMENT.getKey(this.incompatible).toString());
        }

        public static EnchantmentDataEntry<?> deserialize(String s) throws JsonSyntaxException {
            ResourceLocation id = new ResourceLocation(s);
            if (!Registry.ENCHANTMENT.containsKey(id)) throw new JsonSyntaxException("No enchantment with name %s found".formatted(id));
            return new IncompatibleEntry(Registry.ENCHANTMENT.get(id));
        }
    }

    public static class Builder {
        private final List<EnchantmentDataEntry<?>> entries = Lists.newArrayList();

        private Builder() {

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
            this.entries.add(new IncompatibleEntry(incompatible));
            return this;
        }

        public List<EnchantmentDataEntry<?>> build() {
            return ImmutableList.copyOf(this.entries);
        }
    }
}
