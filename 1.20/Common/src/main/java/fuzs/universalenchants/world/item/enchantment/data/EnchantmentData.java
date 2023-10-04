package fuzs.universalenchants.world.item.enchantment.data;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.universalenchants.core.CommonAbstractions;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public record EnchantmentData(Pair<ItemBuilder, ItemBuilder> items,
                              Pair<ItemBuilder, ItemBuilder> anvilItems,
                              Pair<Builder<Enchantment>, Builder<Enchantment>> incompatible) {
    public static final Supplier<Map<Enchantment, EnchantmentData>> DEFAULT_ENCHANTMENT_DATA = Suppliers.memoize(() -> {
        return Maps.toMap(BuiltInRegistries.ENCHANTMENT, EnchantmentData::getEnchantmentDataBuilder);
    });

    public EnchantmentData() {
        this(Pair.of(new ItemBuilder(), new ItemBuilder()), Pair.of(new ItemBuilder(), new ItemBuilder()), Pair.of(new Builder<>(), new Builder<>()));
    }

    private static EnchantmentData getEnchantmentDataBuilder(Enchantment enchantment) {
        EnchantmentData holder = new EnchantmentData();
        if (!ModLoaderEnvironment.INSTANCE.isForge()) {
            EnchantmentCategory category = EnchantmentDataManager.INSTANCE.getVanillaCategory(enchantment);
            holder.items().left().add(category);
        }
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack itemStack = new ItemStack(item);
            if (ModLoaderEnvironment.INSTANCE.isForge() && CommonAbstractions.INSTANCE.canApplyAtEnchantingTable(itemStack, enchantment)) {
                holder.items().left().add(item);
            }
            if (enchantment.canEnchant(itemStack)) {
                holder.anvilItems().left().add(item);
            }
        }
        for (Enchantment currentEnchantment : BuiltInRegistries.ENCHANTMENT) {
            if (!enchantment.isCompatibleWith(currentEnchantment)) {
                holder.incompatible().left().add(currentEnchantment);
            }
        }
        return holder;
    }

    public void buildItemTags(Enchantment enchantment, Function<TagKey<Item>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item>> tagAppenderFactory) {
        ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        this.items.left().build(tagAppenderFactory.apply(EnchantmentDataTags.getTagKeyForItems(resourceLocation)));
        this.items.right().build(tagAppenderFactory.apply(EnchantmentDataTags.getTagKeyForDisabledItems(resourceLocation)));
        this.anvilItems.left().build(tagAppenderFactory.apply(EnchantmentDataTags.getTagKeyForAnvilItems(resourceLocation)));
        this.anvilItems.right().build(tagAppenderFactory.apply(EnchantmentDataTags.getTagKeyForDisabledAnvilItems(resourceLocation)));
    }

    public void buildEnchantmentTags(Function<TagKey<Enchantment>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Enchantment>> tagAppenderFactory) {
//        this.incompatible().left().build(tagAppenderFactory.apply(EnchantmentDataTags.getTagKeyForItems(resourceLocation)));
//        this.incompatible().right().build(tagAppenderFactory.apply(EnchantmentDataTags.getTagKeyForDisabledItems(resourceLocation)));
    }

    public static class Builder<T> {
        final Set<T> items = Sets.newIdentityHashSet();
        final Set<TagKey<T>> tags = Sets.newIdentityHashSet();

        public void add(T item) {
            Objects.requireNonNull(item, "item is null");
            this.items.add(item);
        }

        public void add(TagKey<T> tag) {
            Objects.requireNonNull(tag, "tag is null");
            this.tags.add(tag);
        }

        public void build(IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> tagAppender) {
            this.items.forEach(tagAppender::add);
            this.tags.forEach(tagAppender::addTag);
        }
    }

    public static class ItemBuilder extends Builder<Item> {
        final Set<EnchantmentCategory> categories = Sets.newIdentityHashSet();

        public void add(EnchantmentCategory category) {
            Objects.requireNonNull(category, "category is null");
            if (!EnchantmentDataManager.isVanillaCategory(category)) throw new IllegalArgumentException("invalid category: " + category);
            this.categories.add(category);
        }

        @Override
        public void build(IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tagAppender) {
            this.items.removeIf(item -> {
                for (EnchantmentCategory category : this.categories) {
                    if (category.canEnchant(item)) return true;
                }
                return false;
            });
            super.build(tagAppender);
            this.categories.stream().map(t -> {
                TagKey<Item> tagKey = EnchantmentDataTags.getTagKeyFromCategory(t);
                Objects.requireNonNull(tagKey, "tag key is null");
                return tagKey;
            }).forEach(tagAppender::addTag);
        }
    }
}
