package fuzs.universalenchants.data;

import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.universalenchants.world.item.enchantment.data.BuiltInEnchantmentDataManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Collection;

public class DynamicItemTagProvider extends AbstractTagProvider.Items {

    public DynamicItemTagProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (EnchantmentCategory category : EnchantmentCategory.values()) {
            TagKey<Item> tagKey = BuiltInEnchantmentDataManager.getTagKeyFromCategory(category);
            if (tagKey != null) {
                Collection<Item> items = BuiltInEnchantmentDataManager.INSTANCE.getItems(category);
                this.tag(tagKey).add(items.toArray(Item[]::new));
            }
        }
    }
}
