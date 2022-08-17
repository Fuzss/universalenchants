package fuzs.universalenchants.config;

import fuzs.puzzleslib.config.ConfigCore;
import fuzs.puzzleslib.config.annotation.Config;

public class ClientConfig implements ConfigCore {
    @Config(description = "Vanilla only has translation keys for a few roman numerals (used for enchantment levels and mob effect amplifiers), this option dynamically adds full support.")
    public boolean fixRomanNumerals = true;
}
