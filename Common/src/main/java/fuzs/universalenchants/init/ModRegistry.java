package fuzs.universalenchants.init;

import fuzs.puzzleslib.capability.CapabilityController;
import fuzs.puzzleslib.capability.data.CapabilityKey;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.init.RegistryManager;
import fuzs.puzzleslib.init.RegistryReference;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.capability.ArrowLootingCapability;
import fuzs.universalenchants.world.item.crafting.MendingRepairItemRecipe;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;

public class ModRegistry {
    private static final RegistryManager REGISTRY = CoreServices.FACTORIES.registration(UniversalEnchants.MOD_ID);
    public static final RegistryReference<RecipeSerializer<MendingRepairItemRecipe>> REPAIR_MENDING_ITEM_RECIPE_SERIALIZER = REGISTRY.register(Registry.RECIPE_SERIALIZER_REGISTRY, "crafting_special_repair_mending_item", () -> new SimpleRecipeSerializer<>(MendingRepairItemRecipe::new));

    public static final CapabilityKey<ArrowLootingCapability> ARROW_LOOTING_CAPABILITY = CapabilityController.makeCapabilityKey(UniversalEnchants.MOD_ID, "arrow_looting", ArrowLootingCapability.class);

    public static void touch() {

    }
}
