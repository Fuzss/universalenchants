package fuzs.universalenchants.world.item.enchantment.data;

import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.mixin.accessor.EnchantmentAccessor;
import fuzs.universalenchants.world.item.enchantment.EnchantmentCategoryManager;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.Util;
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

public record EnchantmentData(Pair<ItemBuilder, ItemBuilder> items, Pair<ItemBuilder, ItemBuilder> anvilItems,
                              Pair<Builder<Enchantment>, Builder<Enchantment>> incompatible) {
    public static final Supplier<Map<Enchantment, EnchantmentData>> DEFAULT_ENCHANTMENT_DATA = Suppliers.memoize(() -> {
        return getDefaultEnchantmentData(false);
    });

    public EnchantmentData() {
        this(Pair.of(new ItemBuilder(), new ItemBuilder()), Pair.of(new ItemBuilder(false), new ItemBuilder(false)), Pair.of(new Builder<>(), new Builder<>()));
    }

    public static Map<Enchantment, EnchantmentData> getDefaultEnchantmentData(boolean dataGeneration) {
        return Maps.toMap(BuiltInRegistries.ENCHANTMENT, enchantment -> getEnchantmentDataBuilder(enchantment, dataGeneration));
    }

    private static EnchantmentData getEnchantmentDataBuilder(Enchantment enchantment, boolean dataGeneration) {
        EnchantmentData holder = new EnchantmentData();
        if (dataGeneration) {
            EnchantmentCategory enchantmentCategory = EnchantmentCategoryManager.getVanillaCategory(enchantment);
            holder.items().left().add(enchantmentCategory);
        }
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack itemStack = new ItemStack(item);
            if (!dataGeneration && CommonAbstractions.INSTANCE.canApplyAtEnchantingTable(itemStack, enchantment)) {
                holder.items().left().add(item);
            }
            if (enchantment.canEnchant(itemStack)) {
                holder.anvilItems().left().add(item);
            }
        }
        for (Enchantment currentEnchantment : BuiltInRegistries.ENCHANTMENT) {
            if (enchantment != currentEnchantment) {
                if (!(((EnchantmentAccessor) enchantment).universalenchants$callCheckCompatibility(currentEnchantment) && ((EnchantmentAccessor) enchantment).universalenchants$callCheckCompatibility(currentEnchantment))) {
                    holder.incompatible().left().add(currentEnchantment);
                }
            }
        }
        return holder;
    }

    public void buildItemTags(Enchantment enchantment, Function<TagKey<Item>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item>> tagAppenderFactory, boolean generateEmpty) {
        ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        Function<TagKey<Item>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item>> factory = Util.memoize(tagAppenderFactory);
        this.items.left().build(() -> factory.apply(EnchantmentDataTags.getAllowAtEnchantingTable(resourceLocation)), generateEmpty);
        this.items.right().build(() -> factory.apply(EnchantmentDataTags.getDisallowAtEnchantingTable(resourceLocation)), generateEmpty);
        this.anvilItems.left().build(() -> factory.apply(EnchantmentDataTags.getAllowAtAnvil(resourceLocation)), generateEmpty);
        this.anvilItems.right().build(() -> factory.apply(EnchantmentDataTags.getDisallowAtAnvil(resourceLocation)), generateEmpty);
    }

    public void buildEnchantmentTags(Enchantment enchantment, Function<TagKey<Enchantment>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Enchantment>> tagAppenderFactory, boolean generateEmpty) {
        ResourceLocation resourceLocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
        Function<TagKey<Enchantment>, IntrinsicHolderTagsProvider.IntrinsicTagAppender<Enchantment>> factory = Util.memoize(tagAppenderFactory);
        this.incompatible().left().build(() -> factory.apply(EnchantmentDataTags.getIsIncompatible(resourceLocation)), generateEmpty);
        this.incompatible().right().build(() -> factory.apply(EnchantmentDataTags.getIsNotIncompatible(resourceLocation)), generateEmpty);
    }

    public static class Builder<T> {
        final Set<T> items = Sets.newLinkedHashSet();
        final Set<TagKey<T>> tags = Sets.newLinkedHashSet();

        public void add(T item) {
            Objects.requireNonNull(item, "item is null");
            this.items.add(item);
        }

        public void add(TagKey<T> tag) {
            Objects.requireNonNull(tag, "tag is null");
            this.tags.add(tag);
        }

        public void build(Supplier<IntrinsicHolderTagsProvider.IntrinsicTagAppender<T>> tagAppender, boolean generateEmpty) {
            if (generateEmpty) tagAppender.get();
            this.items.forEach(value -> tagAppender.get().add(value));
            this.tags.forEach(tag -> tagAppender.get().addTag(tag));
        }
    }

    public static class ItemBuilder extends Builder<Item> {
        final Set<EnchantmentCategory> categories = Sets.newLinkedHashSet();
        final boolean addCategoryAsTag;

        public ItemBuilder() {
            this(true);
        }

        public ItemBuilder(boolean addCategoryAsTag) {
            this.addCategoryAsTag = addCategoryAsTag;
        }

        public void add(EnchantmentCategory category) {
            Objects.requireNonNull(category, "category is null");
            if (!EnchantmentCategoryManager.isVanillaCategory(category))
                throw new IllegalArgumentException("invalid category: " + category);
            this.categories.add(category);
        }

        @Override
        public void build(Supplier<IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item>> tagAppender, boolean generateEmpty) {
            if (generateEmpty) tagAppender.get();
            this.items.removeIf(item -> {
                for (EnchantmentCategory category : this.categories) {
                    if (category.canEnchant(item)) return true;
                }
                return false;
            });
            super.build(tagAppender, generateEmpty);
            for (EnchantmentCategory category : this.categories) {
                if (this.addCategoryAsTag) {
                    TagKey<Item> tagKey = EnchantmentDataTags.getTagKeyFromCategory(category);
                    Objects.requireNonNull(tagKey, "tag key is null");
                    tagAppender.get().addTag(tagKey);
                } else {
                    for (Item item : EnchantmentCategoryManager.getItems(category)) {
                        tagAppender.get().add(item);
                    }
                }
            }
        }
    }
}
