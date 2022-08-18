package fuzs.universalenchants.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Set;

public abstract class EnchantmentCategoryEntry extends EnchantmentDataEntry<Item> {
    private boolean exclude;

    public final void serialize(JsonArray jsonArray) {
        if (!this.exclude) {
            jsonArray.add(this.serialize());
        } else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", this.serialize());
            jsonObject.addProperty("exclude", true);
            jsonArray.add(jsonObject);
        }
    }

    abstract String serialize();

    public boolean isExclude() {
        return this.exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public static class ItemEntry extends EnchantmentCategoryEntry {
        private final Item item;

        ItemEntry(Item item) {
            this.item = item;
        }

        @Override
        void dissolve(Set<Item> items) throws JsonSyntaxException {
            items.add(this.item);
        }

        @Override
        String serialize() {
            return Registry.ITEM.getKey(this.item).toString();
        }

        public static EnchantmentCategoryEntry deserialize(String s) throws JsonSyntaxException {
            ResourceLocation id = new ResourceLocation(s);
            if (!Registry.ITEM.containsKey(id)) throw new JsonSyntaxException("No item with name %s found".formatted(id));
            return new ItemEntry(Registry.ITEM.get(id));
        }
    }

    public static class CategoryEntry extends EnchantmentCategoryEntry {
        private final EnchantmentCategory category;

        CategoryEntry(EnchantmentCategory category) {
            this.category = category;
        }

        @Override
        void dissolve(Set<Item> items) throws JsonSyntaxException {
            for (Item item : Registry.ITEM) {
                if (this.category.canEnchant(item)) {
                    items.add(item);
                }
            }
        }

        @Override
        String serialize() {
            return "$" + EnchantmentDataManager.ENCHANTMENT_CATEGORIES_BY_ID.get(this.category);
        }

        public static EnchantmentCategoryEntry deserialize(String s) throws JsonSyntaxException {
            if (s.startsWith("$")) {
                s = s.substring(1);
            }
            ResourceLocation id = new ResourceLocation(s);
            EnchantmentCategory category = EnchantmentDataManager.ENCHANTMENT_CATEGORIES_BY_ID.inverse().get(id);
            if (category == null) throw new JsonSyntaxException("No category with name %s found".formatted(id));
            return new CategoryEntry(category);
        }
    }

    public static class TagEntry extends EnchantmentCategoryEntry {
        private final TagKey<Item> tag;

        TagEntry(TagKey<Item> tag) {
            this.tag = tag;
        }

        @Override
        void dissolve(Set<Item> items) throws JsonSyntaxException {
            for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(this.tag)) {
                items.add(holder.value());
            }
        }

        @Override
        String serialize() {
            return "#" + this.tag.location();
        }

        public static EnchantmentCategoryEntry deserialize(String s) throws JsonSyntaxException {
            if (s.startsWith("#")) {
                s = s.substring(1);
            }
            ResourceLocation id = new ResourceLocation(s);
            TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, id);
            if (!Registry.ITEM.isKnownTagName(tag)) throw new JsonSyntaxException("No tag with name %s found".formatted(id));
            return new TagEntry(tag);
        }
    }
}
