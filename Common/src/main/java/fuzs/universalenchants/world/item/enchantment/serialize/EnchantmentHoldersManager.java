package fuzs.universalenchants.world.item.enchantment.serialize;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import fuzs.puzzleslib.json.JsonConfigFileUtil;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.world.item.enchantment.data.AdditionalEnchantmentDataProvider;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.DataEntry;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.IncompatibleEntry;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.TypeEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.enchantment.*;

import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EnchantmentHoldersManager {
    private static final Map<Enchantment, EnchantmentHolder> ENCHANTMENT_DATA_HOLDERS = Maps.newIdentityHashMap();

    public static void loadAll() {
        JsonConfigFileUtil.getAllAndLoad(UniversalEnchants.MOD_ID, EnchantmentHoldersManager::serializeDefaultDataEntries, EnchantmentHoldersManager::deserializeDataEntry, () -> ENCHANTMENT_DATA_HOLDERS.values().forEach(EnchantmentHolder::invalidate));
        ENCHANTMENT_DATA_HOLDERS.values().forEach(EnchantmentHolder::setEnchantmentCategory);
    }

    private static EnchantmentHolder getEnchantmentHolder(Enchantment enchantment) {
        return ENCHANTMENT_DATA_HOLDERS.computeIfAbsent(enchantment, EnchantmentHolder::new);
    }

    public static boolean isCompatibleWith(Enchantment enchantment, Enchantment other, boolean fallback) {
        return getEnchantmentHolder(enchantment).isCompatibleWith(other, fallback) && getEnchantmentHolder(other).isCompatibleWith(enchantment, fallback);
    }

    private static void serializeDefaultDataEntries(File directory) {
        serializeAllDataEntries(directory, AdditionalEnchantmentDataProvider.INSTANCE.getDefaultCategoryEntries());
    }

    private static void serializeAllDataEntries(File directory, Map<Enchantment, List<DataEntry<?>>> categoryEntries) {
        for (Map.Entry<Enchantment, List<DataEntry<?>>> entry : categoryEntries.entrySet()) {
            String fileName = "%s.json".formatted(Registry.ENCHANTMENT.getKey(entry.getKey()).getPath());
            File file = new File(directory, fileName);
            JsonConfigFileUtil.saveToFile(file, serializeDataEntry(entry.getKey(), entry.getValue()));
        }
    }

    private static JsonElement serializeDataEntry(Enchantment enchantment, Collection<DataEntry<?>> entries) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", Registry.ENCHANTMENT.getKey(enchantment).toString());
        JsonArray jsonArray = new JsonArray();
        JsonArray jsonArray1 = new JsonArray();
        for (DataEntry<?> entry : entries) {
            if (entry instanceof TypeEntry) {
                entry.serialize(jsonArray);
            } else if (entry instanceof IncompatibleEntry) {
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
        Enchantment enchantment = Registry.ENCHANTMENT.get(id);
        EnchantmentHolder holder = getEnchantmentHolder(enchantment);
        if (jsonObject.has("items")) {
            JsonArray items = GsonHelper.getAsJsonArray(jsonObject, "items");
            for (JsonElement itemElement : items) {
                deserializeCategoryEntry(id, holder, itemElement);
            }
        }
        if (jsonObject.has("incompatible")) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "incompatible");
            String[] incompatibles = JsonConfigFileUtil.GSON.fromJson(jsonArray, String[].class);
            holder.submit(IncompatibleEntry.deserialize(id, incompatibles));
        }
    }

    private static void deserializeCategoryEntry(ResourceLocation enchantment, EnchantmentHolder holder, JsonElement jsonElement) throws JsonSyntaxException {
        String item;
        boolean exclude = false;
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject1 = jsonElement.getAsJsonObject();
            item = GsonHelper.getAsString(jsonObject1, "id");
            exclude = GsonHelper.getAsBoolean(jsonObject1, "exclude");
        } else {
            item = GsonHelper.convertToString(jsonElement, "item");
        }
        try {
            TypeEntry entry;
            if (item.startsWith("$")) {
                entry = TypeEntry.CategoryEntry.deserialize(enchantment, item);
            } else if (item.startsWith("#")) {
                entry = TypeEntry.TagEntry.deserialize(enchantment, item);
            } else {
                entry = TypeEntry.ItemEntry.deserialize(enchantment, item);
            }
            if (!entry.isEmpty()) {
                entry.setExclude(exclude);
                holder.submit(entry);
            }
        } catch (Exception e) {
            UniversalEnchants.LOGGER.warn("Failed to deserialize {} enchantment config entry {}: {}", enchantment, item, e);
        }
    }
}
