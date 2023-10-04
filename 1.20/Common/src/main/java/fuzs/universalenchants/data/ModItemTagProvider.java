package fuzs.universalenchants.data;

import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.universalenchants.world.item.enchantment.EnchantmentCategoryManager;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentData;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentDataTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.Map;

public class ModItemTagProvider extends AbstractTagProvider.Items {
    private final Map<Enchantment, EnchantmentData> data;

    public ModItemTagProvider(DataProviderContext context, Map<Enchantment, EnchantmentData> data) {
        super(context);
        this.data = data;
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (EnchantmentCategory category : EnchantmentCategory.values()) {
            if (EnchantmentCategoryManager.isVanillaCategory(category)) {
                TagKey<Item> tagKey = EnchantmentDataTags.getTagKeyFromCategory(category);
                IntrinsicTagAppender<Item> tagAppender = this.tag(tagKey);
                EnchantmentCategoryManager.getItems(category).forEach(tagAppender::add);
            }
        }
        for (Map.Entry<Enchantment, EnchantmentData> entry : this.data.entrySet()) {
            entry.getValue().buildItemTags(entry.getKey(), this::tag, false);
        }
    }
}
