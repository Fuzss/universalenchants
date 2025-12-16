package fuzs.universalenchants.data.tags;

import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public abstract class ModEnchantmentTagsProvider extends AbstractTagProvider<Enchantment> {

    public ModEnchantmentTagsProvider(DataProviderContext context) {
        super(Registries.ENCHANTMENT, context);
    }

    @SafeVarargs
    protected final void addInclusiveEnchantments(ResourceKey<Enchantment> primaryEnchantment, ResourceKey<Enchantment>... secondaryEnchantments) {
        for (ResourceKey<Enchantment> secondaryEnchantment : secondaryEnchantments) {
            this.addInclusiveEnchantments(primaryEnchantment, secondaryEnchantment);
        }
    }

    protected final void addInclusiveEnchantments(ResourceKey<Enchantment> primaryEnchantment, ResourceKey<Enchantment> secondaryEnchantment) {
        this.tag(ModRegistry.getInclusiveSetEnchantmentTag(primaryEnchantment)).addKey(secondaryEnchantment);
        this.tag(ModRegistry.getInclusiveSetEnchantmentTag(secondaryEnchantment)).addKey(primaryEnchantment);
    }
}
