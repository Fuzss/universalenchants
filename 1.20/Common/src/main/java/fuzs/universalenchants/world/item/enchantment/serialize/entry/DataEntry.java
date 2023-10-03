package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.init.v3.RegistryHelper;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import fuzs.universalenchants.world.item.enchantment.serialize.EnchantmentHolder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public abstract class DataEntry<T> {
    private final EnchantmentDataKey dataType;
    public boolean exclude;

    protected DataEntry(EnchantmentDataKey dataType) {
        this.dataType = dataType;
    }

    @SuppressWarnings("unchecked")
    public static <T> void deserialize(EnchantmentDataKey dataType, EnchantmentHolder holder, JsonElement jsonElement) throws JsonSyntaxException {
        String item;
        boolean exclude = false;
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject1 = jsonElement.getAsJsonObject();
            item = GsonHelper.getAsString(jsonObject1, "id");
            exclude = GsonHelper.getAsBoolean(jsonObject1, "exclude");
        } else {
            item = GsonHelper.convertToString(jsonElement, "item");
            if (item.startsWith("!")) {
                exclude = true;
                item = item.substring(1);
            }
        }
        try {
            DataEntry<T> entry;
            if (item.startsWith("$")) {
                if (!dataType.registryKey.equals(Registries.ITEM)) throw new IllegalStateException("Category entry only supported by item registry");
                entry = (DataEntry<T>) new CategoryEntry(dataType, item);
            } else if (item.startsWith("#")) {
                entry = new TagEntry<>(dataType, item);
            } else {
                entry = new ItemEntry<>(dataType, item);
            }
            entry.exclude = exclude;
            holder.submit(entry);
        } catch (Exception e) {
            UniversalEnchants.LOGGER.warn("Failed to deserialize {} enchantment config entry {}: {}", holder.id(), item, e);
        }
    }

    public abstract void dissolve(Set<T> items) throws JsonSyntaxException;

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

    protected abstract String serialize();

    public EnchantmentDataKey getDataType() {
        return this.dataType;
    }

    @SuppressWarnings("unchecked")
    protected final Registry<T> getRegistry() {
        return RegistryHelper.findBuiltInRegistry((ResourceKey<Registry<T>>) (ResourceKey<?>) this.dataType.registryKey);
    }

    public static BuilderHolder getEnchantmentDataBuilder(Enchantment enchantment) {
        BuilderHolder holder = new BuilderHolder(enchantment);
        EnchantmentCategory category = BuiltInEnchantmentDataManager.INSTANCE.getVanillaCategory(enchantment);
        holder.categoryBuilder().add(category);
        holder.anvilBuilder().add(category);
        // don't add the enchantment itself, the user is not supposed to remove it, this will be manually added back later
        BuiltInRegistries.ENCHANTMENT.stream().filter(Predicate.not(enchantment::isCompatibleWith)).filter(other -> enchantment != other).forEach(holder.incompatibleBuilder()::add);
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack itemStack = new ItemStack(item);
            if (enchantment.canEnchant(itemStack)) {
                holder.anvilBuilder().add(item);
            }
            // Forge has IForgeItemStack::canApplyAtEnchantingTable method for making an item compatible with enchantments outside the Enchantment#category
            // to honor this we need to find all those additional enchantments and add them manually (this means configs will have to be recreated when such mods are added)
            // example: Farmer's Delight's skillet item and various items from Twilight Forest
            if (ModLoaderEnvironment.INSTANCE.isForge() && CommonAbstractions.INSTANCE.canApplyAtEnchantingTable(itemStack, enchantment)) {
                holder.categoryBuilder().add(item);
            }
        }
        return holder;
    }

    public record BuilderHolder(ItemBuilder categoryBuilder, ItemBuilder anvilBuilder, Builder<Enchantment> incompatibleBuilder) {

        public BuilderHolder(Enchantment enchantment) {
            this(new ItemBuilder(EnchantmentDataKey.ITEMS, enchantment), new ItemBuilder(EnchantmentDataKey.ANVIL_ITEMS, enchantment), new Builder<>(EnchantmentDataKey.INCOMPATIBLE, enchantment));
        }

        public List<DataEntry<?>> build() {
            ImmutableList.Builder<DataEntry<?>> builder = ImmutableList.builder();
            builder.addAll(this.categoryBuilder.build());
            builder.addAll(this.anvilBuilder.build());
            builder.addAll(this.incompatibleBuilder.build());
            return builder.build();
        }
    }

    public static class Builder<T> {
        final EnchantmentDataKey dataKey;
        final ResourceLocation enchantment;
        public final Set<T> items = Sets.newLinkedHashSet();
        public final Set<TagKey<T>> tags = Sets.newLinkedHashSet();

        Builder(EnchantmentDataKey dataKey, Enchantment enchantment) {
            this.dataKey = dataKey;
            this.enchantment = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        }

        public void add(T item) {
            Objects.requireNonNull(item, "item for enchantment %s is null".formatted(this.enchantment));
            this.items.add(item);
        }

        public void remove(T item) {
            Objects.requireNonNull(item, "item for enchantment %s is null".formatted(this.enchantment));
            this.items.remove(item);
        }

        public void add(TagKey<T> tag) {
            Objects.requireNonNull(tag, "tag for enchantment %s is null".formatted(this.enchantment));
            this.tags.add(tag);
        }

        public void remove(TagKey<T> tag) {
            Objects.requireNonNull(tag, "tag for enchantment %s is null".formatted(this.enchantment));
            this.tags.remove(tag);
        }

        public List<DataEntry<T>> build() {
            List<DataEntry<T>> entries = Lists.newArrayList();
            this.items.stream().map(t -> new ItemEntry<>(this.dataKey, t)).forEach(entries::add);
            this.tags.stream().map(t -> new TagEntry<>(this.dataKey, t)).forEach(entries::add);
            return entries;
        }
    }

    public static class ItemBuilder extends Builder<Item> {
        public final Set<EnchantmentCategory> categories = Sets.newLinkedHashSet();

        private ItemBuilder(EnchantmentDataKey dataKey, Enchantment enchantment) {
            super(dataKey, enchantment);
        }

        public void add(EnchantmentCategory category) {
            Objects.requireNonNull(category, "category for enchantment %s is null".formatted(this.enchantment));
            if (!BuiltInEnchantmentDataManager.requireVanillaCategory(category)) throw new IllegalArgumentException("Cannot add custom category %s to enchantment data entry builder for %s".formatted(category, this.enchantment));
            this.categories.add(category);
        }

        @Override
        public List<DataEntry<Item>> build() {
            this.items.removeIf(item -> {
                for (EnchantmentCategory category : this.categories) {
                    if (category.canEnchant(item)) return true;
                }
                return false;
            });
            List<DataEntry<Item>> entries = super.build();
            this.categories.stream().map(t -> {
                TagKey<Item> tagKey = BuiltInEnchantmentDataManager.getTagKeyFromCategory(t);
                Objects.requireNonNull(tagKey, "tag key is null");
                return new TagEntry<>(this.dataKey, tagKey);
            }).forEach(entries::add);
            return entries;
        }
    }
}
