package fuzs.universalenchants.data;

import fuzs.enchantmentcontrol.api.v1.tags.EnchantmentTags;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagAppender;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

import java.util.function.BiConsumer;

public class ModEnchantmentTagProvider extends AbstractTagProvider<Enchantment> {

    public ModEnchantmentTagProvider(DataProviderContext context) {
        super(Registries.ENCHANTMENT, context);
    }

    @Override
    public void addTags(HolderLookup.Provider registries) {
        // removing from tags is not supported on Fabric, so we rely on dynamically regenerating the incompatibility tags there
        this.adjustIncompatibilities(AbstractTagAppender::remove, Enchantments.INFINITY_ARROWS, Enchantments.MENDING);
        this.adjustIncompatibilities(AbstractTagAppender::remove, Enchantments.MULTISHOT, Enchantments.PIERCING);
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            if (enchantment instanceof DamageEnchantment && enchantment != Enchantments.SHARPNESS) {
                this.adjustIncompatibilities(AbstractTagAppender::remove, Enchantments.SHARPNESS, enchantment);
                this.adjustIncompatibilities(AbstractTagAppender::add, Enchantments.IMPALING, enchantment);
            }
            if (enchantment instanceof ProtectionEnchantment && enchantment != Enchantments.ALL_DAMAGE_PROTECTION &&
                    enchantment != Enchantments.FALL_PROTECTION) {
                this.adjustIncompatibilities(AbstractTagAppender::remove,
                        Enchantments.ALL_DAMAGE_PROTECTION,
                        enchantment
                );
            }
        }
    }

    private void adjustIncompatibilities(BiConsumer<AbstractTagAppender<Enchantment>, Enchantment> consumer, Enchantment... enchantments) {
        for (Enchantment enchantment : enchantments) {
            AbstractTagAppender<Enchantment> tagAppender = this.add(EnchantmentTags.getIncompatibleTag(enchantment));
            for (Enchantment currentEnchantment : enchantments) {
                if (enchantment != currentEnchantment) {
                    consumer.accept(tagAppender, currentEnchantment);
                }
            }
        }
    }
}
