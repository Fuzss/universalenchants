package fuzs.universalenchants.data;

import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public class ModEnchantmentTagProvider extends AbstractTagProvider.Intrinsic<Enchantment> {
    private final Map<Enchantment, EnchantmentData> data;

    public ModEnchantmentTagProvider(DataProviderContext context, Map<Enchantment, EnchantmentData> data) {
        super(Registries.ENCHANTMENT, context);
        this.data = data;
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (Map.Entry<Enchantment, EnchantmentData> entry : this.data.entrySet()) {
            entry.getValue().buildEnchantmentTags(entry.getKey(), this::tag, false);
        }
    }
}
