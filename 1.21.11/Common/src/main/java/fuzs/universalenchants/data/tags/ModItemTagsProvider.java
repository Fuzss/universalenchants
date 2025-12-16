package fuzs.universalenchants.data.tags;

import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagAppender;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.function.Consumer;

public class ModItemTagsProvider extends AbstractTagProvider<Item> {

    public ModItemTagsProvider(DataProviderContext context) {
        super(Registries.ITEM, context);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.ANIMAL_ARMOR_ITEM_TAG)
                .add(Items.LEATHER_HORSE_ARMOR,
                        Items.COPPER_HORSE_ARMOR,
                        Items.IRON_HORSE_ARMOR,
                        Items.GOLDEN_HORSE_ARMOR,
                        Items.DIAMOND_HORSE_ARMOR,
                        Items.WOLF_ARMOR);
        this.addSupportedItems(ItemTags.SWORDS, Enchantments.IMPALING, Enchantments.BREACH);
        this.addSupportedItems(ItemTags.AXES,
                Enchantments.SHARPNESS,
                Enchantments.SMITE,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.LOOTING,
                Enchantments.IMPALING,
                Enchantments.BREACH);
        this.addSupportedItems("c:tools/spear",
                Enchantments.SHARPNESS,
                Enchantments.SMITE,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.LOOTING,
                Enchantments.BREACH,
                Enchantments.QUICK_CHARGE,
                Enchantments.PIERCING);
        this.addSupportedItems("c:tools/mace",
                Enchantments.SHARPNESS,
                Enchantments.SMITE,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.LOOTING,
                Enchantments.IMPALING);
        this.addSupportedItems("c:tools/bow",
                Enchantments.PIERCING,
                Enchantments.MULTISHOT,
                Enchantments.QUICK_CHARGE,
                Enchantments.LOOTING);
        this.addSupportedItems("c:tools/crossbow",
                Enchantments.FLAME,
                Enchantments.PUNCH,
                Enchantments.POWER,
                Enchantments.INFINITY,
                Enchantments.LOOTING);
        this.addSupportedItems(ModRegistry.ANIMAL_ARMOR_ITEM_TAG,
                Enchantments.PROTECTION,
                Enchantments.FIRE_PROTECTION,
                Enchantments.FEATHER_FALLING,
                Enchantments.BLAST_PROTECTION,
                Enchantments.PROJECTILE_PROTECTION,
                Enchantments.RESPIRATION,
                Enchantments.THORNS,
                Enchantments.DEPTH_STRIDER,
                Enchantments.FROST_WALKER,
                Enchantments.BINDING_CURSE,
                Enchantments.SOUL_SPEED,
                Enchantments.VANISHING_CURSE);
        this.addSupportedItems("c:tools/shield", Enchantments.THORNS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT);
        this.addSupportedItems("c:armors", Enchantments.THORNS);
    }

    @SafeVarargs
    private void addSupportedItems(String tagKey, ResourceKey<Enchantment>... enchantments) {
        this.addSupportedItems(tagAppender -> tagAppender.addOptionalTag(tagKey), enchantments);
    }

    @SafeVarargs
    private void addSupportedItems(TagKey<Item> tagKey, ResourceKey<Enchantment>... enchantments) {
        this.addSupportedItems(tagAppender -> tagAppender.addTag(tagKey), enchantments);
    }

    @SafeVarargs
    private void addSupportedItems(Consumer<AbstractTagAppender<Item>> consumer, ResourceKey<Enchantment>... enchantments) {
        for (ResourceKey<Enchantment> enchantment : enchantments) {
            TagKey<Item> tagKey = ModRegistry.getSecondaryEnchantableItemTag(enchantment);
            consumer.accept(this.tag(tagKey));
        }
    }
}
