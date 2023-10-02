package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import fuzs.universalenchants.core.CommonAbstractions;
import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public interface DataEntry<T> {

    void dissolve(Set<T> items) throws JsonSyntaxException;

    void serialize(JsonArray jsonArray);

    static BuilderHolder getDefaultEnchantmentDataBuilder(Enchantment enchantment) {
        BuilderHolder holder = new BuilderHolder(enchantment);
        holder.add(BuiltInEnchantmentDataManager.INSTANCE.getVanillaCategory(enchantment));
        // don't add the enchantment itself, the user is not supposed to remove it, this will be manually added back later
        BuiltInRegistries.ENCHANTMENT.stream().filter(Predicate.not(enchantment::isCompatibleWith)).filter(other -> enchantment != other).forEach(holder::addIncompatible);
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack itemStack = new ItemStack(item);
            if (enchantment.canEnchant(itemStack)) {
                holder.anvilBuilder().add(item);
            }
            // Forge has IForgeItemStack::canApplyAtEnchantingTable method for making an item compatible with enchantments outside the Enchantment#category
            // to honor this we need to find all those additional enchantments and add them manually (this means configs will have to be recreated when such mods are added)
            // example: Farmer's Delight's skillet item and various items from Twilight Forest
            if (CommonAbstractions.INSTANCE.canApplyAtEnchantingTable(itemStack, enchantment)) {
                holder.categoryBuilder().add(item);
            }
        }
        return holder;
    }

    record BuilderHolder(Enchantment enchantment, Builder categoryBuilder, Builder anvilBuilder, Set<Enchantment> incompatibleEnchantments) {

        public BuilderHolder(Enchantment enchantment) {
            this(enchantment, new Builder(enchantment), new Builder(enchantment), Sets.newLinkedHashSet());
        }

        public BuilderHolder add(Item item) {
            this.categoryBuilder.add(item);
            this.anvilBuilder.add(item);
            return this;
        }

        public BuilderHolder add(EnchantmentCategory category) {
            this.categoryBuilder.add(category);
            this.anvilBuilder.add(category);
            return this;
        }

        public BuilderHolder add(TagKey<Item> tag) {
            this.categoryBuilder.add(tag);
            this.anvilBuilder.add(tag);
            return this;
        }

        public BuilderHolder addIncompatible(Enchantment enchantment) {
            Objects.requireNonNull(enchantment, "incompatible enchantment for %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            this.incompatibleEnchantments.add(enchantment);
            return this;
        }

        public BuilderHolder removeIncompatible(Enchantment enchantment) {
            Objects.requireNonNull(enchantment, "incompatible enchantment for %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            this.incompatibleEnchantments.remove(enchantment);
            return this;
        }

        public List<DataEntry<?>> build() {
            ImmutableList.Builder<DataEntry<?>> builder = ImmutableList.builder();
            builder.addAll(this.categoryBuilder.build());
            builder.addAll(Util.make(this.anvilBuilder.build(), list -> list.forEach(t -> t.anvil = true)));
            builder.add(new IncompatibleEntry(this.incompatibleEnchantments));
            return builder.build();
        }
    }

    class Builder {
        public final Enchantment enchantment;
        public final Set<Item> items = Sets.newLinkedHashSet();
        public final Set<EnchantmentCategory> categories = Sets.newLinkedHashSet();
        public final Set<TagKey<Item>> tags = Sets.newLinkedHashSet();

        private Builder(Enchantment enchantment) {
            this.enchantment = enchantment;
        }

        public void add(Item item) {
            Objects.requireNonNull(item, "item for enchantment %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            this.items.add(item);
        }

        public void add(EnchantmentCategory category) {
            Objects.requireNonNull(category, "category for enchantment %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            if (!BuiltInEnchantmentDataManager.INSTANCE.requireVanillaCategory(category)) throw new IllegalArgumentException("Cannot add custom category %s to enchantment data entry builder for %s".formatted(category, BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            this.categories.add(category);
        }

        public void add(TagKey<Item> tag) {
            Objects.requireNonNull(tag, "tag for enchantment %s is null".formatted(BuiltInRegistries.ENCHANTMENT.getKey(this.enchantment)));
            this.tags.add(tag);
        }

        public List<TypeEntry> build() {
            this.items.removeIf(item -> {
                for (EnchantmentCategory category : this.categories) {
                    if (category.canEnchant(item)) return true;
                }
                return false;
            });
            List<TypeEntry> entries = Lists.newArrayList();
            this.items.stream().map(TypeEntry.ItemEntry::new).forEach(entries::add);
            this.categories.stream().map(TypeEntry.TagEntry::new).forEach(entries::add);
            this.tags.stream().map(TypeEntry.TagEntry::new).forEach(entries::add);
            return entries;
        }
    }
}
