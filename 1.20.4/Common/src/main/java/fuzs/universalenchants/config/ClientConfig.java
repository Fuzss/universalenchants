package fuzs.universalenchants.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.universalenchants.client.handler.TriggerLockRenderHandler;

public class ClientConfig implements ConfigCore {
    @Config(description = {"Vanilla only has translation keys for a few roman numerals (used for enchantment levels and mob effect amplifiers), this option dynamically adds full support.", "Resource packs must be reloaded to apply by pressing F3+T."})
    public NumeralLanguage fixRomanNumerals = NumeralLanguage.ROMAN;
    @Config(description = "Maximum time in ticks it takes to open the enchantments editor for an item.")
    @Config.IntRange(min = 1, max = TriggerLockRenderHandler.MAX_TRIGGER_TIME)
    public int openEnchantmentsEditorTicks = 12;

    public enum NumeralLanguage {
        NONE, ARABIC, ROMAN
    }
}
