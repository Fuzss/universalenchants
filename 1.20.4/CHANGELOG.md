# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v20.4.0-1.20.4] - 2024-02-25
- All functionality for controlling enchantments (like defining item compatibility, and enchantment compatibility) has been moved to the new [Enchantment Control](https://github.com/Fuzss/enchantmentcontrol) mod
- That mod is now a required dependency, and Universal Enchants uses it to provide some default implementations for additional enchantment compatibility
- Port to Minecraft 1.20.4
- Port to NeoForge
### Added
- New improvements for frost walker
  - Works when jumping and falling
  - Refreshes the ice below the wearer while standing still
  - Is able to replace waterlogged blocks such as kelp and seagrass
### Changed
- Arrows shot from bows enchanted with multishot are now spread out vertically
- Simplified `/enchant` command syntax a little for vanilla parity
