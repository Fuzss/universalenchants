package fuzs.universalenchants.data;

import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModEnchantmentTagProvider extends AbstractTagProvider<Enchantment> {

    public ModEnchantmentTagProvider(DataProviderContext context) {
        super(Registries.ENCHANTMENT, context);
    }

    @Override
    public void addTags(HolderLookup.Provider registries) {
        this.addInclusiveEnchantments(Enchantments.INFINITY, Enchantments.MENDING);
        this.addInclusiveEnchantments(Enchantments.MULTISHOT, Enchantments.PIERCING);
        this.addInclusiveEnchantments(Enchantments.SHARPNESS,
                Enchantments.SMITE,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.IMPALING,
                Enchantments.DENSITY,
                Enchantments.BREACH);
        this.addInclusiveEnchantments(Enchantments.DENSITY, Enchantments.SHARPNESS, Enchantments.BREACH);
        this.addInclusiveEnchantments(Enchantments.BREACH, Enchantments.SHARPNESS, Enchantments.DENSITY);
        this.addInclusiveEnchantments(Enchantments.PROTECTION,
                Enchantments.BLAST_PROTECTION,
                Enchantments.FIRE_PROTECTION,
                Enchantments.PROJECTILE_PROTECTION);
    }

    @SafeVarargs
    private void addInclusiveEnchantments(ResourceKey<Enchantment> primaryEnchantment, ResourceKey<Enchantment>... secondaryEnchantments) {
        for (ResourceKey<Enchantment> secondaryEnchantment : secondaryEnchantments) {
            this.addInclusiveEnchantments(primaryEnchantment, secondaryEnchantment);
        }
    }

    private void addInclusiveEnchantments(ResourceKey<Enchantment> primaryEnchantment, ResourceKey<Enchantment> secondaryEnchantment) {
        this.tag(ModRegistry.getInclusiveSetEnchantmentTag(primaryEnchantment)).addKey(secondaryEnchantment);
        this.tag(ModRegistry.getInclusiveSetEnchantmentTag(secondaryEnchantment)).addKey(primaryEnchantment);
    }
}
