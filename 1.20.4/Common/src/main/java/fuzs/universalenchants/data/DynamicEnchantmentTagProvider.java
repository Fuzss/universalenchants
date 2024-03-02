package fuzs.universalenchants.data;

import com.google.common.collect.Sets;
import fuzs.enchantmentcontrol.api.v1.data.EnchantmentDataHelper;
import fuzs.enchantmentcontrol.api.v1.tags.EnchantmentTags;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagAppender;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

public class DynamicEnchantmentTagProvider extends AbstractTagProvider<Enchantment> {

    public DynamicEnchantmentTagProvider(DataProviderContext context) {
        super(Registries.ENCHANTMENT, context);
    }

    @Override
    public void addTags(HolderLookup.Provider registries) {
        this.adjustIncompatibilities(Enchantments.INFINITY_ARROWS, Enchantments.MENDING);
        this.adjustIncompatibilities(Enchantments.MENDING, Enchantments.INFINITY_ARROWS);
        this.adjustIncompatibilities(Enchantments.MULTISHOT, Enchantments.PIERCING);
        this.adjustIncompatibilities(Enchantments.PIERCING, Enchantments.MULTISHOT);
        this.adjustIncompatibilities(Enchantments.SHARPNESS,
                enchantment -> !(enchantment instanceof DamageEnchantment)
        );
        this.adjustIncompatibilities(Enchantments.ALL_DAMAGE_PROTECTION,
                enchantment -> !(enchantment instanceof ProtectionEnchantment protectionEnchantment) ||
                        protectionEnchantment.type == ProtectionEnchantment.Type.FALL
        );
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            if (enchantment != Enchantments.SHARPNESS && enchantment instanceof DamageEnchantment) {
                this.adjustIncompatibilities(enchantment, Enchantments.SHARPNESS);
                // no need to add additional impaling incompatibilities here, those can be properly read on Fabric from the tag
            }
            if (enchantment != Enchantments.ALL_DAMAGE_PROTECTION &&
                    enchantment instanceof ProtectionEnchantment protectionEnchantment &&
                    protectionEnchantment.type != ProtectionEnchantment.Type.FALL) {
                this.adjustIncompatibilities(enchantment, Enchantments.ALL_DAMAGE_PROTECTION);
            }
        }
    }

    private void adjustIncompatibilities(Enchantment enchantment, Enchantment... compatibleEnchantments) {
        Set<Enchantment> enchantments = Sets.newIdentityHashSet();
        enchantments.addAll(Arrays.asList(compatibleEnchantments));
        this.adjustIncompatibilities(enchantment, Predicate.not(enchantments::contains));
    }

    private void adjustIncompatibilities(Enchantment enchantment, Predicate<Enchantment> predicate) {
        EnchantmentDataHelper.isOriginalState(enchantment);
        AbstractTagAppender<Enchantment> tagAppender = this.add(EnchantmentTags.getIncompatibleTag(enchantment));
        for (Enchantment other : BuiltInRegistries.ENCHANTMENT) {
            if (enchantment != other && !enchantment.isCompatibleWith(other)) {
                if (predicate.test(other)) {
                    tagAppender.add(other);
                }
            }
        }
    }

    @Override
    public AbstractTagAppender<Enchantment> add(TagKey<Enchantment> tagKey) {
        // set this to replace everything in the Enchantment Control tag, and make sure this is sorted on top of the other mod's dynamic pack
        return super.add(tagKey).setReplace();
    }
}
