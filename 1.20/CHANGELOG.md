# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v8.1.0-1.20.1] - 2023-10-02
Please note that all previous configuration files will still work with this update, there is no need to adjust existing setups at the moment.
### Added
- Added item tags for all entries in vanilla's `EnchantmentCategory` enum, those tags are dynamically created and populated at runtime to guarantee mod compatibility
- Added `anvil_items` field to enchantment configs for controlling applying enchantments at an anvil
### Changed
- Individual entries can now be excluded by prefixing the entry with `!` in addition to the previous method
- Shears can now be directly enchanted in an enchanting table
- Fortune now affects the experience points dropped from mining certain blocks such as ores
### Removed
- Removed `$` syntax for entries, it has been replaced by the new item tags

## [v8.0.0-1.20.1] - 2023-06-27
- Ported to Minecraft 1.20.1

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
