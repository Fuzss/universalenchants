package fuzs.universalenchants;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class UniversalEnchantsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchants::new);
        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((CreativeModeTab group, FabricItemGroupEntries entries) -> {
            entries.getDisplayStacks().removeIf(itemStack -> {
                if (itemStack.is(Items.ENCHANTED_BOOK)) {
                    for (Enchantment enchantment : EnchantmentHelper.getEnchantments(itemStack).keySet()) {
                        if (!((FeatureElement) enchantment).isEnabled(entries.getEnabledFeatures())) {
                            return true;
                        }
                    }
                }
                return false;
            });
            entries.getSearchTabStacks().removeIf(itemStack -> {
                if (itemStack.is(Items.ENCHANTED_BOOK)) {
                    for (Enchantment enchantment : EnchantmentHelper.getEnchantments(itemStack).keySet()) {
                        if (!((FeatureElement) enchantment).isEnabled(entries.getEnabledFeatures())) {
                            return true;
                        }
                    }
                }
                return false;
            });
        });
    }
}
