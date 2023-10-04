package fuzs.universalenchants.data;

import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentDataManager;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentDataTags;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentData;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Collection;
import java.util.Map;

public class DynamicItemTagProvider extends AbstractTagProvider.Items {

    public DynamicItemTagProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (EnchantmentCategory category : EnchantmentCategory.values()) {
            if (EnchantmentDataManager.isVanillaCategory(category)) {
                TagKey<Item> tagKey = EnchantmentDataTags.getTagKeyFromCategory(category);
                Collection<Item> items = EnchantmentDataManager.INSTANCE.getItems(category);
                this.tag(tagKey).add(items.toArray(Item[]::new));
            }
        }
        for (Map.Entry<Enchantment, EnchantmentData> entry : EnchantmentData.DEFAULT_ENCHANTMENT_DATA.get().entrySet()) {
            entry.getValue().buildItemTags(entry.getKey(), this::tag);
        }
    }
}
