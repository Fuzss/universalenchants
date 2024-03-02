package fuzs.universalenchants.data;

import fuzs.enchantmentcontrol.api.v1.EnchantmentCategories;
import fuzs.enchantmentcontrol.api.v1.tags.EnchantmentCategoryTags;
import fuzs.enchantmentcontrol.api.v1.tags.EnchantmentTags;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.tags.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class ModItemTagProvider extends AbstractTagProvider<Item> {

    public ModItemTagProvider(DataProviderContext context) {
        super(Registries.ITEM, context);
    }

    @Override
    public void addTags(HolderLookup.Provider provider) {
        this.registerAdditionalEnchantments(EnchantmentCategories.SWORD, Enchantments.IMPALING);
        this.registerAdditionalEnchantments(EnchantmentCategories.AXE,
                Enchantments.SHARPNESS,
                Enchantments.SMITE,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.KNOCKBACK,
                Enchantments.FIRE_ASPECT,
                Enchantments.MOB_LOOTING,
                Enchantments.SWEEPING_EDGE,
                Enchantments.IMPALING
        );
        this.registerAdditionalEnchantments(EnchantmentCategories.TRIDENT,
                Enchantments.SHARPNESS,
                Enchantments.SMITE,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.KNOCKBACK,
                Enchantments.FIRE_ASPECT,
                Enchantments.MOB_LOOTING,
                Enchantments.SWEEPING_EDGE,
                Enchantments.QUICK_CHARGE,
                Enchantments.PIERCING
        );
        this.registerAdditionalEnchantments(EnchantmentCategories.BOW,
                Enchantments.PIERCING,
                Enchantments.MULTISHOT,
                Enchantments.QUICK_CHARGE,
                Enchantments.MOB_LOOTING
        );
        this.registerAdditionalEnchantments(EnchantmentCategories.CROSSBOW,
                Enchantments.FLAMING_ARROWS,
                Enchantments.PUNCH_ARROWS,
                Enchantments.POWER_ARROWS,
                Enchantments.INFINITY_ARROWS,
                Enchantments.MOB_LOOTING
        );
        this.registerAdditionalEnchantments(EnchantmentCategories.HORSE_ARMOR,
                Enchantments.ALL_DAMAGE_PROTECTION,
                Enchantments.FIRE_PROTECTION,
                Enchantments.FALL_PROTECTION,
                Enchantments.BLAST_PROTECTION,
                Enchantments.PROJECTILE_PROTECTION,
                Enchantments.RESPIRATION,
                Enchantments.THORNS,
                Enchantments.DEPTH_STRIDER,
                Enchantments.FROST_WALKER,
                Enchantments.BINDING_CURSE,
                Enchantments.SOUL_SPEED,
                Enchantments.VANISHING_CURSE
        );
        this.registerAdditionalEnchantments(EnchantmentCategories.SHIELD, Enchantments.THORNS, Enchantments.KNOCKBACK);
        this.registerAdditionalEnchantments(EnchantmentCategories.ARMOR, Enchantments.THORNS);
    }

    private void registerAdditionalEnchantments(EnchantmentCategory enchantmentCategory, Enchantment... enchantments) {
        for (Enchantment enchantment : enchantments) {
            // have to set tags to optional as they are not found from the Enchantment Control mod during data generation and are reported as invalid
            this.add(EnchantmentTags.getEnchantingTableTag(enchantment))
                    .addOptionalTag(EnchantmentCategoryTags.getTagKey(enchantmentCategory));
            this.add(EnchantmentTags.getAnvilTag(enchantment))
                    .addOptionalTag(EnchantmentCategoryTags.getTagKey(enchantmentCategory));
        }
    }
}
