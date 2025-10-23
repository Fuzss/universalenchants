package fuzs.universalenchants.data.tags;

import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModEnchantmentTagsProvider extends AbstractTagProvider<Enchantment> {

    public ModEnchantmentTagsProvider(DataProviderContext context) {
        super(Registries.ENCHANTMENT, context);
    }

    @Override
    public void addTags(HolderLookup.Provider registries) {
        this.addInclusiveEnchantments(Enchantments.INFINITY, Enchantments.MENDING);
        this.addInclusiveEnchantments(Enchantments.MULTISHOT, Enchantments.PIERCING);
        this.addInclusiveEnchantments(Enchantments.DENSITY, Enchantments.BREACH);
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

    public static class DamageEnchantments extends ModEnchantmentTagsProvider {

        public DamageEnchantments(DataProviderContext context) {
            super(context);
        }

        @Override
        public void addTags(HolderLookup.Provider registries) {
            this.addInclusiveEnchantments(Enchantments.SHARPNESS,
                    Enchantments.SMITE,
                    Enchantments.BANE_OF_ARTHROPODS,
                    Enchantments.IMPALING,
                    Enchantments.DENSITY,
                    Enchantments.BREACH);
        }
    }

    public static class ProtectionEnchantments extends ModEnchantmentTagsProvider {

        public ProtectionEnchantments(DataProviderContext context) {
            super(context);
        }

        @Override
        public void addTags(HolderLookup.Provider registries) {
            this.addInclusiveEnchantments(Enchantments.PROTECTION,
                    Enchantments.BLAST_PROTECTION,
                    Enchantments.FIRE_PROTECTION,
                    Enchantments.PROJECTILE_PROTECTION);
        }
    }
}
