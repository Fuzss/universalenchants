package fuzs.universalenchants.data;

import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentData;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public class ModItemTagProvider extends AbstractTagProvider.Items {

    public ModItemTagProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Map.Entry<Enchantment, EnchantmentData> entry : EnchantmentDataProvider.ADDITIONAL_ENCHANTMENT_DATA.get().entrySet()) {
            entry.getValue().buildItemTags(entry.getKey(), this::tag);
        }
    }
}
