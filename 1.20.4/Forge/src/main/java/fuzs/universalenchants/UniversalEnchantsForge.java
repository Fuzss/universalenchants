package fuzs.universalenchants;

import fuzs.puzzleslib.api.capability.v2.ForgeCapabilityHelper;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.data.v2.core.DataProviderHelper;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.data.ModEnchantmentTagProvider;
import fuzs.universalenchants.data.ModItemTagProvider;
import fuzs.universalenchants.init.ModRegistry;
import fuzs.universalenchants.world.item.enchantment.data.EnchantmentData;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

import java.util.Iterator;
import java.util.Map;

@Mod(UniversalEnchants.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class UniversalEnchantsForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchants::new);
        registerCapabilities();
        DataProviderHelper.registerDataProviders(UniversalEnchants.MOD_ID, data -> {
            return new ModEnchantmentTagProvider(data, EnchantmentData.getDefaultEnchantmentData(true));
        }, data -> {
            return new ModItemTagProvider(data, EnchantmentData.getDefaultEnchantmentData(true));
        });
    }

    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(final BuildCreativeModeTabContentsEvent evt) {
        Iterator<Map.Entry<ItemStack, CreativeModeTab.TabVisibility>> iterator = evt.getEntries().iterator();
        while (iterator.hasNext()) {
            ItemStack itemStack = iterator.next().getKey();
            if (itemStack.is(Items.ENCHANTED_BOOK)) {
                for (Enchantment enchantment : EnchantmentHelper.getEnchantments(itemStack).keySet()) {
                    if (!((FeatureElement) enchantment).isEnabled(evt.getFlags())) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

    private static void registerCapabilities() {
        ForgeCapabilityHelper.setCapabilityToken(ModRegistry.ARROW_LOOTING_CAPABILITY, new CapabilityToken<ArrowLootingCapability>() {});
    }
}
