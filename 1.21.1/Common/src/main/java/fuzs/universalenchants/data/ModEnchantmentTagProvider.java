package fuzs.universalenchants.data;

import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModEnchantmentTagProvider extends AbstractTagProvider<Enchantment> {

    public ModEnchantmentTagProvider(DataProviderContext context) {
        super(Registries.ENCHANTMENT, context);
    }

    @Override
    public void addTags(HolderLookup.Provider registries) {
        this.add(EnchantmentTags.BOW_EXCLUSIVE).remove(Enchantments.INFINITY, Enchantments.MENDING);
        this.add(EnchantmentTags.CROSSBOW_EXCLUSIVE).remove(Enchantments.MULTISHOT, Enchantments.PIERCING);
        this.add(EnchantmentTags.DAMAGE_EXCLUSIVE).remove(Enchantments.SHARPNESS);
        this.add(EnchantmentTags.ARMOR_EXCLUSIVE).remove(Enchantments.PROTECTION);
    }
}
