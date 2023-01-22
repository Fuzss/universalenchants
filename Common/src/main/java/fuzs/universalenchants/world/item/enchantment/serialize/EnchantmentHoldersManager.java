package fuzs.universalenchants.world.item.enchantment.serialize;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.json.JsonConfigFileUtil;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.world.item.enchantment.data.AdditionalEnchantmentDataProvider;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.DataEntry;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.IncompatibleEntry;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.TypeEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.enchantment.Enchantment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EnchantmentHoldersManager {
    private static final int SCHEMA_VERSION = 2;
    private static final Map<Enchantment, EnchantmentHolder> ENCHANTMENT_DATA_HOLDERS = Maps.newIdentityHashMap();

    public static boolean isCompatibleWith(Enchantment enchantment, Enchantment other, boolean fallback) {
        return getEnchantmentHolder(enchantment).isCompatibleWith(other, fallback) && getEnchantmentHolder(other).isCompatibleWith(enchantment, fallback);
    }

    public static void loadAll() {
        ENCHANTMENT_DATA_HOLDERS.values().forEach(EnchantmentHolder::invalidate);
        Path modConfigPath = CoreServices.ENVIRONMENT.getConfigDir().resolve(UniversalEnchants.MOD_ID);
        JsonConfigFileUtil.mkdirs(modConfigPath.toFile());
        for (Map.Entry<Enchantment, List<DataEntry<?>>> entry : AdditionalEnchantmentDataProvider.INSTANCE.getEnchantmentDataEntries().entrySet()) {
            ResourceLocation id = Registry.ENCHANTMENT.getKey(entry.getKey());
            Path configRootPath = modConfigPath.resolve(id.getNamespace());
            JsonConfigFileUtil.mkdirs(configRootPath.toFile());
            String fileName = id.getPath() + ".json";
            Path configFilePath = configRootPath.resolve(fileName);
            File configFile = configFilePath.toFile();
            EnchantmentHolder holder = getEnchantmentHolder(entry.getKey());
            holder.ensureInvalidated();
            if (!loadFromFile(holder, configFilePath, configFile)) {
                if (JsonConfigFileUtil.saveToFile(configFile, serializeDataEntry(entry.getValue()))) {
                    UniversalEnchants.LOGGER.info("Created new enchantment config file for {} in config directory", holder.id());
                }
                holder.initializeCategoryEntries();
                holder.submitAll(entry.getValue());
            }
        }
        ENCHANTMENT_DATA_HOLDERS.values().forEach(EnchantmentHolder::applyEnchantmentCategory);
    }

    private static EnchantmentHolder getEnchantmentHolder(Enchantment enchantment) {
        return ENCHANTMENT_DATA_HOLDERS.computeIfAbsent(enchantment, EnchantmentHolder::new);
    }

    private static boolean loadFromFile(EnchantmentHolder holder, Path configFilePath, File configFile) {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                deserializeDataEntry(holder, reader);
                UniversalEnchants.LOGGER.debug("Read enchantment config file for {} in config directory", holder.id());
                return true;
            } catch (IOException | JsonSyntaxException e) {
                UniversalEnchants.LOGGER.error("Failed to read enchantment config file for {} in config directory: {}", holder.id(), e);
                try {
                    Files.move(configFilePath, configFilePath.getParent().resolve(configFilePath.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {

                }
            }
        }
        return false;
    }

    private static void deserializeDataEntry(EnchantmentHolder holder, FileReader reader) throws JsonSyntaxException {
        JsonElement jsonElement = JsonConfigFileUtil.GSON.fromJson(reader, JsonElement.class);
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "enchantment config");
        int schemaVersion = GsonHelper.getAsInt(jsonObject, "schemaVersion");
        if (schemaVersion != SCHEMA_VERSION) throw new JsonSyntaxException("Invalid config file schema %s (current format is %s) for enchantment %s".formatted(schemaVersion, SCHEMA_VERSION, holder.id()));
        if (jsonObject.has("items")) {
            holder.initializeCategoryEntries();
            JsonArray items = GsonHelper.getAsJsonArray(jsonObject, "items");
            for (JsonElement itemElement : items) {
                TypeEntry.deserializeCategoryEntry(holder.id(), holder, itemElement);
            }
        }
        if (jsonObject.has("incompatible")) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "incompatible");
            String[] incompatibles = JsonConfigFileUtil.GSON.fromJson(jsonArray, String[].class);
            holder.submit(IncompatibleEntry.deserialize(holder.id(), incompatibles));
        }
    }

    private static JsonElement serializeDataEntry(Collection<DataEntry<?>> entries) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("schemaVersion", SCHEMA_VERSION);
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
}
