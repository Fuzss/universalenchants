package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Objects;
import java.util.Set;

public abstract class TypeEntry extends DataEntry<Item> {
    private static final TypeEntry EMPTY = new TypeEntry() {

        @Override
        String serialize() {
            throw new UnsupportedOperationException("serialize called on empty enchantment category entry");
        }

        @Override
        public void dissolve(Set<Item> items) throws JsonSyntaxException {
            throw new UnsupportedOperationException("dissolve called on empty enchantment category entry");
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };

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

    public boolean isEmpty() {
        return false;
    }

    public boolean isExclude() {
        return this.exclude;
    }

    public void setExclude(boolean exclude) {
        this.exclude = exclude;
    }

    public static class ItemEntry extends TypeEntry {
        private final Item item;

        ItemEntry(Item item) {
            this.item = item;
        }

        @Override
        public void dissolve(Set<Item> items) throws JsonSyntaxException {
            items.add(this.item);
        }

        @Override
        String serialize() {
            return Registry.ITEM.getKey(this.item).toString();
        }

        public static TypeEntry deserialize(ResourceLocation enchantment, String s) throws JsonSyntaxException {
            ResourceLocation id = new ResourceLocation(s);
            if (!Registry.ITEM.containsKey(id)) {
                JsonSyntaxException e = new JsonSyntaxException("No item with name %s found".formatted(id));
                UniversalEnchants.LOGGER.warn("Failed to deserialize {} enchantment config entry {}: {}", enchantment, id, e);
                return EMPTY;
            }
            return new ItemEntry(Registry.ITEM.get(id));
        }
    }

    public static class CategoryEntry extends TypeEntry {
        private final EnchantmentCategory category;

        CategoryEntry(EnchantmentCategory category) {
            this.category = category;
        }

        @Override
        public void dissolve(Set<Item> items) throws JsonSyntaxException {
            for (Item item : Registry.ITEM) {
                if (this.category.canEnchant(item)) {
                    items.add(item);
                }
            }
        }

        @Override
        String serialize() {
            Objects.requireNonNull(this.category, "category is null");
            ResourceLocation identifier = BuiltInEnchantmentDataManager.INSTANCE.getEnchantmentCategoriesIdMap().get(this.category);
            Objects.requireNonNull(identifier, "identifier for category %s is null".formatted(this.category));
            return "$" + identifier;
        }

        public static TypeEntry deserialize(ResourceLocation enchantment, String s) throws JsonSyntaxException {
            if (s.startsWith("$")) {
                s = s.substring(1);
            }
            ResourceLocation id = new ResourceLocation(s);
            EnchantmentCategory category = BuiltInEnchantmentDataManager.INSTANCE.getEnchantmentCategoriesIdMap().inverse().get(id);
            if (category == null) {
                JsonSyntaxException e = new JsonSyntaxException("No category with name %s found".formatted(id));
                UniversalEnchants.LOGGER.warn("Failed to deserialize {} enchantment config entry {}: {}", enchantment, id, e);
                return EMPTY;
            }
            return new CategoryEntry(category);
        }
    }

    public static class TagEntry extends TypeEntry {
        private final TagKey<Item> tag;

        TagEntry(TagKey<Item> tag) {
            this.tag = tag;
        }

        @Override
        public void dissolve(Set<Item> items) throws JsonSyntaxException {
            for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(this.tag)) {
                items.add(holder.value());
            }
        }

        @Override
        String serialize() {
            return "#" + this.tag.location();
        }

        public static TypeEntry deserialize(ResourceLocation enchantment, String s) throws JsonSyntaxException {
            if (s.startsWith("#")) {
                s = s.substring(1);
            }
            ResourceLocation id = new ResourceLocation(s);
            TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, id);
            if (!Registry.ITEM.isKnownTagName(tag)) {
                JsonSyntaxException e = new JsonSyntaxException("No tag with name %s found".formatted(id));
                UniversalEnchants.LOGGER.warn("Failed to deserialize {} enchantment config entry {}: {}", enchantment, id, e);
                return EMPTY;
            }
            return new TagEntry(tag);
        }
    }
}
