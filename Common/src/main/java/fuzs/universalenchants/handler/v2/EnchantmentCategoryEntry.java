package fuzs.universalenchants.handler.v2;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Set;

public abstract class EnchantmentCategoryEntry {

    abstract void compile(Set<Item> items) throws JsonSyntaxException;

    abstract void serialize(JsonArray jsonArray);

    public static Builder defaultBuilder(Enchantment enchantment) {
        return new Builder().add(enchantment.category);
    }

    public static class ItemEntry extends EnchantmentCategoryEntry {
        private final Item item;

        public ItemEntry(Item item) {
            this.item = item;
        }

        @Override
        void compile(Set<Item> items) throws JsonSyntaxException {
            items.add(this.item);
        }

        @Override
        void serialize(JsonArray jsonArray) {
            jsonArray.add(Registry.ITEM.getKey(this.item).toString());
        }
    }

    public static class CategoryEntry extends EnchantmentCategoryEntry {
        private final EnchantmentCategory category;

        public CategoryEntry(EnchantmentCategory category) {
            this.category = category;
        }

        @Override
        void compile(Set<Item> items) throws JsonSyntaxException {
            for (Item item : Registry.ITEM) {
                if (this.category.canEnchant(item)) {
                    items.add(item);
                }
            }
        }

        @Override
        void serialize(JsonArray jsonArray) {
            jsonArray.add("$" + ItemCompatManagerV2.ENCHANTMENT_CATEGORIES_BY_ID.get(this.category));
        }
    }

    public static class TagEntry extends EnchantmentCategoryEntry {
        private final TagKey<Item> tag;

        public TagEntry(TagKey<Item> tag) {
            this.tag = tag;
        }

        @Override
        void compile(Set<Item> items) throws JsonSyntaxException {
            for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(this.tag)) {
                items.add(holder.value());
            }
        }

        @Override
        void serialize(JsonArray jsonArray) {
            jsonArray.add("#" + this.tag.location());
        }
    }

    public static class Builder {
        private final List<EnchantmentCategoryEntry> entries = Lists.newArrayList();

        private Builder() {

        }

        public Builder add(Item item) {
            this.entries.add(new ItemEntry(item));
            return this;
        }

        public Builder add(EnchantmentCategory category) {
            this.entries.add(new CategoryEntry(category));
            return this;
        }

        public Builder add(TagKey<Item> tag) {
            this.entries.add(new TagEntry(tag));
            return this;
        }

        public List<EnchantmentCategoryEntry> build() {
            return ImmutableList.copyOf(this.entries);
        }
    }
}
