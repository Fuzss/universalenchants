package fuzs.universalenchants.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import fuzs.puzzleslib.json.JsonConfigFileUtil;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.core.ModServices;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.enchantment.*;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantmentDataManager {
    /**
     * vanilla doesn't have an enchantment category just for axes, so we make our own
     */
    private static final EnchantmentCategory AXE_ENCHANTMENT_CATEGORY = ModServices.ABSTRACTIONS.createEnchantmentCategory("AXE", item -> item instanceof AxeItem);
    private static final EnchantmentCategory HORSE_ARMOR_ENCHANTMENT_CATEGORY = ModServices.ABSTRACTIONS.createEnchantmentCategory("HORSE_ARMOR", item -> item instanceof HorseArmorItem);
    /**
     * we store these manually as vanilla's categories are the only ones we want to mess with, don't accidentally do something with our own or other mods' categories
     */
    public static final BiMap<EnchantmentCategory, ResourceLocation> ENCHANTMENT_CATEGORIES_BY_ID = EnchantmentCategoryMapBuilder.create()
            .putVanillaCategories(EnchantmentCategory.ARMOR, EnchantmentCategory.ARMOR_FEET, EnchantmentCategory.ARMOR_LEGS, EnchantmentCategory.ARMOR_CHEST, EnchantmentCategory.ARMOR_HEAD, EnchantmentCategory.WEAPON, EnchantmentCategory.DIGGER, EnchantmentCategory.FISHING_ROD, EnchantmentCategory.TRIDENT, EnchantmentCategory.BREAKABLE, EnchantmentCategory.BOW, EnchantmentCategory.WEARABLE, EnchantmentCategory.CROSSBOW, EnchantmentCategory.VANISHABLE)
            .putCategory(UniversalEnchants.MOD_ID, AXE_ENCHANTMENT_CATEGORY)
            .putCategory(UniversalEnchants.MOD_ID, HORSE_ARMOR_ENCHANTMENT_CATEGORY)
            .get();
    public static final Map<Enchantment, EnchantmentCategory> DEFAULT_ENCHANTMENT_CATEGORIES = Registry.ENCHANTMENT.stream().collect(ImmutableMap.toImmutableMap(Function.identity(), e -> e.category));
    private static final List<AdditionalEnchantmentsData> ADDITIONAL_ENCHANTMENTS_DATA = ImmutableList.of(
            new AdditionalEnchantmentsData(EnchantmentCategory.WEAPON, Enchantments.IMPALING),
            new AdditionalEnchantmentsData(AXE_ENCHANTMENT_CATEGORY, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.IMPALING),
            new AdditionalEnchantmentsData(EnchantmentCategory.TRIDENT, Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS, Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.MOB_LOOTING, Enchantments.SWEEPING_EDGE, Enchantments.QUICK_CHARGE, Enchantments.PIERCING),
            new AdditionalEnchantmentsData(EnchantmentCategory.BOW, Enchantments.PIERCING, Enchantments.MULTISHOT, Enchantments.QUICK_CHARGE, Enchantments.MOB_LOOTING),
            new AdditionalEnchantmentsData(EnchantmentCategory.CROSSBOW, Enchantments.FLAMING_ARROWS, Enchantments.PUNCH_ARROWS, Enchantments.POWER_ARROWS, Enchantments.INFINITY_ARROWS, Enchantments.MOB_LOOTING),
            new AdditionalEnchantmentsData(HORSE_ARMOR_ENCHANTMENT_CATEGORY, Enchantments.ALL_DAMAGE_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.FALL_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.RESPIRATION, Enchantments.THORNS, Enchantments.DEPTH_STRIDER, Enchantments.FROST_WALKER, Enchantments.BINDING_CURSE, Enchantments.SOUL_SPEED, Enchantments.VANISHING_CURSE)
    );
    private static final Map<Enchantment, EnchantmentDataHolder> ENCHANTMENT_DATA_HOLDERS = Registry.ENCHANTMENT.stream().collect(Collectors.toMap(Function.identity(), EnchantmentDataHolder::new));
    
    private static Map<Enchantment, List<EnchantmentDataEntry<?>>> getDefaultCategoryEntries() {
        // constructing default builders on Forge is quite expensive, so only do this when necessary
        Map<Enchantment, EnchantmentDataEntry.Builder> builders = getVanillaEnchantments().collect(Collectors.toMap(Function.identity(), ModServices.ABSTRACTIONS::defaultEnchantmentDataBuilder));
        ADDITIONAL_ENCHANTMENTS_DATA.forEach(data -> data.addToBuilder(builders));
        setupAdditionalCompatibility(builders);
        return builders.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> e.getValue().build()));
    }

    private static void setupAdditionalCompatibility(Map<Enchantment, EnchantmentDataEntry.Builder> builders) {
        applyIncompatibilityToBoth(builders, Enchantments.INFINITY_ARROWS, Enchantments.MENDING, false);
        applyIncompatibilityToBoth(builders, Enchantments.MULTISHOT, Enchantments.PIERCING, false);
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment instanceof DamageEnchantment && enchantment != Enchantments.SHARPNESS) {
                applyIncompatibilityToBoth(builders, Enchantments.SHARPNESS, enchantment, false);
                // we make impaling incompatible with damage enchantments as both can be applied to the same weapons now
                applyIncompatibilityToBoth(builders, Enchantments.IMPALING, enchantment, true);
            }
        }
        for (Enchantment enchantment : Registry.ENCHANTMENT) {
            if (enchantment instanceof ProtectionEnchantment && enchantment != Enchantments.ALL_DAMAGE_PROTECTION && enchantment != Enchantments.FALL_PROTECTION) {
                applyIncompatibilityToBoth(builders, Enchantments.ALL_DAMAGE_PROTECTION, enchantment, false);
            }
        }
    }

    private static void applyIncompatibilityToBoth(Map<Enchantment, EnchantmentDataEntry.Builder> builders, Enchantment enchantment, Enchantment other, boolean add) {
        BiConsumer<Enchantment, Enchantment> operation = (e1, e2) -> {
            EnchantmentDataEntry.Builder builder = builders.get(e1);
            // this might be called for non-vanilla enchantments (currently possible through DamageEnchantment and ProtectionEnchantment instanceof checks)
            // they won't have a builder, so be careful
            if (builder == null) return;
            if (add) {
                builder.add(e2);
            } else {
                builder.remove(e2);
            }
        };
        operation.accept(enchantment, other);
        operation.accept(other, enchantment);
    }

    public static void loadAll() {
        JsonConfigFileUtil.getAllAndLoad(UniversalEnchants.MOD_ID, EnchantmentDataManager::serializeDefaultDataEntries, EnchantmentDataManager::deserializeDataEntry, () -> ENCHANTMENT_DATA_HOLDERS.values().forEach(EnchantmentDataHolder::invalidate));
        ENCHANTMENT_DATA_HOLDERS.values().forEach(EnchantmentDataHolder::setEnchantmentCategory);
    }

    public static boolean isCompatibleWith(Enchantment enchantment, Enchantment other, boolean fallback) {
        return ENCHANTMENT_DATA_HOLDERS.get(enchantment).isCompatibleWith(other, fallback) && ENCHANTMENT_DATA_HOLDERS.get(other).isCompatibleWith(enchantment, fallback);
    }

    private static void serializeDefaultDataEntries(File directory) {
        serializeAllDataEntries(directory, getDefaultCategoryEntries());
    }

    private static void serializeAllDataEntries(File directory, Map<Enchantment, List<EnchantmentDataEntry<?>>> categoryEntries) {
        for (Map.Entry<Enchantment, List<EnchantmentDataEntry<?>>> entry : categoryEntries.entrySet()) {
            String fileName = "%s.json".formatted(Registry.ENCHANTMENT.getKey(entry.getKey()).getPath());
            File file = new File(directory, fileName);
            JsonConfigFileUtil.saveToFile(file, serializeDataEntry(entry.getKey(), entry.getValue()));
        }
    }

    private static JsonElement serializeDataEntry(Enchantment enchantment, Collection<EnchantmentDataEntry<?>> entries) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", Registry.ENCHANTMENT.getKey(enchantment).toString());
        JsonArray jsonArray = new JsonArray();
        JsonArray jsonArray1 = new JsonArray();
        for (EnchantmentDataEntry<?> entry : entries) {
            if (entry instanceof EnchantmentCategoryEntry) {
                entry.serialize(jsonArray);
            } else if (entry instanceof EnchantmentDataEntry.IncompatibleEntry) {
                entry.serialize(jsonArray1);
            }
        }
        jsonObject.add("items", jsonArray);
        jsonObject.add("incompatible", jsonArray1);
        return jsonObject;
    }

    private static void deserializeDataEntry(FileReader reader) throws JsonSyntaxException {
        JsonElement jsonElement = JsonConfigFileUtil.GSON.fromJson(reader, JsonElement.class);
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "enchantment config");
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(jsonObject, "id"));
        if (!Registry.ENCHANTMENT.containsKey(id)) throw new JsonSyntaxException("Enchantment %s not found in registry, skipping...".formatted(id));
        Enchantment key = Registry.ENCHANTMENT.get(id);
        EnchantmentDataHolder holder = ENCHANTMENT_DATA_HOLDERS.get(key);
        if (jsonObject.has("items")) {
            JsonArray items = GsonHelper.getAsJsonArray(jsonObject, "items");
            for (JsonElement jsonElement1 : items) {
                String item;
                boolean exclude = false;
                if (jsonElement1.isJsonObject()) {
                    JsonObject jsonObject1 = jsonElement1.getAsJsonObject();
                    item = GsonHelper.getAsString(jsonObject1, "id");
                    exclude = GsonHelper.getAsBoolean(jsonObject1, "exclude");
                } else {
                    item = GsonHelper.convertToString(jsonElement1, "item");
                }
                EnchantmentCategoryEntry entry;
                if (item.startsWith("$")) {
                    entry = EnchantmentCategoryEntry.CategoryEntry.deserialize(item);
                } else if (item.startsWith("#")) {
                    entry = EnchantmentCategoryEntry.TagEntry.deserialize(item);
                } else {
                    entry = EnchantmentCategoryEntry.ItemEntry.deserialize(item);
                }
                entry.setExclude(exclude);
                holder.submit(entry);
            }
        }
        if (jsonObject.has("incompatible")) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "incompatible");
            String[] incompatibles = JsonConfigFileUtil.GSON.fromJson(jsonArray, String[].class);
            holder.submit(EnchantmentDataEntry.IncompatibleEntry.deserialize(incompatibles));
        }
    }

    private static Stream<Enchantment> getVanillaEnchantments() {
        return Registry.ENCHANTMENT.entrySet().stream().filter(entry -> entry.getKey().location().getNamespace().equals("minecraft")).map(Map.Entry::getValue);
    }

    private static class EnchantmentCategoryMapBuilder {
        private final BiMap<EnchantmentCategory, ResourceLocation> map = EnumHashBiMap.create(EnchantmentCategory.class);

        public EnchantmentCategoryMapBuilder putVanillaCategories(EnchantmentCategory... categories) {
            for (EnchantmentCategory category : categories) {
                this.putCategory("minecraft", category);
            }
            return this;
        }

        public EnchantmentCategoryMapBuilder putCategory(String namespace, EnchantmentCategory category) {
            ResourceLocation location = new ResourceLocation(namespace, category.name().toLowerCase(Locale.ROOT));
            this.map.put(category, location);
            return this;
        }

        public BiMap<EnchantmentCategory, ResourceLocation> get() {
            return this.map;
        }

        public static EnchantmentCategoryMapBuilder create() {
            return new EnchantmentCategoryMapBuilder();
        }
    }

    private record AdditionalEnchantmentsData(EnchantmentCategory category, List<Enchantment> enchantments) {

        AdditionalEnchantmentsData(EnchantmentCategory category, Enchantment... enchantments) {
            this(category, ImmutableList.copyOf(enchantments));
        }

        public void addToBuilder(Map<Enchantment, EnchantmentDataEntry.Builder> builders) {
            for (Enchantment enchantment : this.enchantments) {
                builders.get(enchantment).add(this.category);
            }
        }
    }
}
