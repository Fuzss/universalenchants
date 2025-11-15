package fuzs.universalenchants.neoforge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.puzzleslib.neoforge.api.data.v2.core.NeoForgeDataProviderContext;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.data.ModDatapackRegistriesProvider;
import fuzs.universalenchants.data.tags.ModBlockTagsProvider;
import fuzs.universalenchants.data.tags.ModEnchantmentTagsProvider;
import fuzs.universalenchants.data.tags.ModItemTagsProvider;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(UniversalEnchants.MOD_ID)
public class UniversalEnchantsNeoForge {

    public UniversalEnchantsNeoForge() {
        ModConstructor.construct(UniversalEnchants.MOD_ID, UniversalEnchants::new);
        registerEventHandlers(NeoForge.EVENT_BUS);
        DataProviderHelper.registerDataProviders(UniversalEnchants.MOD_ID,
                ModDatapackRegistriesProvider::new,
                ModItemTagsProvider::new,
                ModBlockTagsProvider::new);
        registerBuiltInDataProviders(UniversalEnchants.COMPATIBLE_BOW_ENCHANTMENTS_LOCATION,
                Enchantments.INFINITY,
                Enchantments.MENDING);
        registerBuiltInDataProviders(UniversalEnchants.COMPATIBLE_CROSSBOW_ENCHANTMENTS_LOCATION,
                Enchantments.MULTISHOT,
                Enchantments.PIERCING);
        registerBuiltInDataProviders(UniversalEnchants.COMPATIBLE_MACE_ENCHANTMENTS_LOCATION,
                Enchantments.DENSITY,
                Enchantments.BREACH);
        registerBuiltInDataProviders(UniversalEnchants.COMPATIBLE_DAMAGE_ENCHANTMENTS_LOCATION,
                Enchantments.SHARPNESS,
                Enchantments.SMITE,
                Enchantments.BANE_OF_ARTHROPODS,
                Enchantments.IMPALING,
                Enchantments.DENSITY,
                Enchantments.BREACH);
        registerBuiltInDataProviders(UniversalEnchants.COMPATIBLE_PROTECTION_ENCHANTMENTS_LOCATION,
                Enchantments.PROTECTION,
                Enchantments.BLAST_PROTECTION,
                Enchantments.FIRE_PROTECTION,
                Enchantments.PROJECTILE_PROTECTION);
    }

    private static void registerEventHandlers(IEventBus eventBus) {
        eventBus.addListener((final BlockEvent.FarmlandTrampleEvent evt) -> {
            if (!(evt.getLevel() instanceof ServerLevel serverLevel)) return;
            if (BetterEnchantsHandler.onFarmlandTrample(serverLevel,
                    evt.getPos(),
                    evt.getState(),
                    evt.getFallDistance(),
                    evt.getEntity()).isInterrupt()) {
                evt.setCanceled(true);
            }
        });
    }

    @SafeVarargs
    private static void registerBuiltInDataProviders(ResourceLocation resourceLocation, ResourceKey<Enchantment> primaryEnchantment, ResourceKey<Enchantment>... secondaryEnchantments) {
        DataProviderHelper.registerDataProviders(resourceLocation,
                PackType.SERVER_DATA,
                (NeoForgeDataProviderContext context) -> {
                    return new ModEnchantmentTagsProvider(context) {
                        @Override
                        public void addTags(HolderLookup.Provider registries) {
                            this.addInclusiveEnchantments(primaryEnchantment, secondaryEnchantments);
                        }
                    };
                });
    }
}
