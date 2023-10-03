package fuzs.universalenchants.world.item.enchantment.serialize;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import fuzs.puzzleslib.api.config.v3.json.JsonConfigFileUtil;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.world.item.enchantment.data.AdditionalEnchantmentDataProvider;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.DataEntry;
import fuzs.universalenchants.world.item.enchantment.serialize.entry.EnchantmentDataKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
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
import java.util.stream.Collectors;

public class EnchantmentHoldersManager {
    private static final int SCHEMA_VERSION = 2;
    private static final Map<Enchantment, EnchantmentHolder> ENCHANTMENT_DATA_HOLDERS = Maps.newIdentityHashMap();

    public static boolean isCompatibleWith(Enchantment enchantment, Enchantment other, boolean fallback) {
        return getHolder(enchantment).isCompatibleWith(other, fallback) && getHolder(other).isCompatibleWith(enchantment, fallback);
    }

    public static boolean canApplyAtAnvil(Enchantment enchantment, ItemStack itemStack) {
        return getHolder(enchantment).canApplyAtAnvil(itemStack);
    }

    public static void loadAll() {
        ENCHANTMENT_DATA_HOLDERS.values().forEach(EnchantmentHolder::clear);
        Path modConfigPath = ModLoaderEnvironment.INSTANCE.getConfigDirectory().resolve(UniversalEnchants.MOD_ID);
        JsonConfigFileUtil.mkdirs(modConfigPath.toFile());
        for (Map.Entry<Enchantment, List<DataEntry<?>>> entry : AdditionalEnchantmentDataProvider.INSTANCE.getEnchantmentDataEntries().entrySet()) {
            ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(entry.getKey());
            Path configRootPath = modConfigPath.resolve(id.getNamespace());
            JsonConfigFileUtil.mkdirs(configRootPath.toFile());
            String fileName = id.getPath() + ".json";
            Path configFilePath = configRootPath.resolve(fileName);
            File configFile = configFilePath.toFile();
            EnchantmentHolder holder = getHolder(entry.getKey());
            holder.requireEmpty();
            if (!loadFromFile(holder, configFilePath, configFile)) {
                if (JsonConfigFileUtil.saveToFile(configFile, serializeAllEntries(entry.getValue()))) {
                    UniversalEnchants.LOGGER.info("Created new enchantment config file for {}", holder.id());
                }
                entry.getValue().forEach(holder::submit);
            }
        }
        ENCHANTMENT_DATA_HOLDERS.values().forEach(EnchantmentHolder::applyEnchantmentCategory);
    }

    private static EnchantmentHolder getHolder(Enchantment enchantment) {
        return ENCHANTMENT_DATA_HOLDERS.computeIfAbsent(enchantment, EnchantmentHolder::new);
    }

    private static boolean loadFromFile(EnchantmentHolder holder, Path configFilePath, File configFile) {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                deserializeAllEntries(holder, reader);
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

    private static void deserializeAllEntries(EnchantmentHolder holder, FileReader reader) throws JsonSyntaxException {
        JsonElement jsonElement = JsonConfigFileUtil.GSON.fromJson(reader, JsonElement.class);
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "enchantment config");
        int schemaVersion = GsonHelper.getAsInt(jsonObject, "schemaVersion");
        if (schemaVersion != SCHEMA_VERSION) throw new JsonSyntaxException("Invalid config file schema %s (current format is %s) for enchantment %s".formatted(schemaVersion, SCHEMA_VERSION, holder.id()));
        for (EnchantmentDataKey dataType : EnchantmentDataKey.values()) {
            if (jsonObject.has(dataType.getName())) {
                JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, dataType.getName());
                for (JsonElement itemElement : jsonArray) {
                    DataEntry.deserialize(dataType, holder, itemElement);
                }
            }
        }
    }

    private static JsonElement serializeAllEntries(Collection<DataEntry<?>> entries) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("schemaVersion", SCHEMA_VERSION);
        Map<EnchantmentDataKey, List<DataEntry<?>>> groupedEntries = entries.stream().collect(Collectors.groupingBy(DataEntry::getDataType, () -> Maps.newEnumMap(EnchantmentDataKey.class), Collectors.toList()));
        for (Map.Entry<EnchantmentDataKey, List<DataEntry<?>>> entry : groupedEntries.entrySet()) {
            JsonArray jsonArray = new JsonArray();
            for (DataEntry<?> dataEntry : entry.getValue()) {
                dataEntry.serialize(jsonArray);
            }
            jsonObject.add(entry.getKey().getName(), jsonArray);
        }
        return jsonObject;
    }
}
