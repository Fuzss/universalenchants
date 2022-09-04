# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v4.2.4-1.19.2] - 2022-09-04
### Changed
- Improved support for enchantment compatibility on custom modded items, this is a dedicated config option that needs to be manually enabled (all on Forge only)

## [v4.2.3-1.19.2] - 2022-09-01
### Fixed
- Recompiled to fix start-up crash on Forge due to mixins not having been remapped

## [v4.2.2-1.19.2] - 2022-09-01
- Recompile for Puzzles Lib v4.3.0

## [v4.2.1-1.19.2] - 2022-08-26
- Cardinal Components once again doesn't need a separate download anymore, it is now bundled with Puzzles Lib
### Fixed
- Fixed a small bug where xp would not be properly affected from a looting weapon on Fabric

## [v4.2.0-1.19.2] - 2022-08-21
- Compiled for Minecraft 1.19.2

## [v4.1.2-1.19.1] - 2022-08-20
### Fixed
- Fixed broken Forge translation keys

## [v4.1.1-1.19.1] - 2022-08-19
### Fixed
- Fixed crash on dedicated server due to faulty import

## [v4.1.0-1.19.1] - 2022-08-19
### Added
- Completely overhauled config for what enchantments are compatible with what items and which enchantments can be applied together
- The new system uses individual `json` files for each enchantment, this makes configuring every single vanilla enchantment possible
- To find out how exactly the new system works check out the documentation on the GitHub repository
- Added dynamic support for roman numerals outside of vanilla's default translation range
- Overhauled vanilla's `/enchant` command, it can now apply enchantment levels above the default max level, supports overriding and removing enchantments, and also handles books now
- Horse armor can now receive the following enchantments at an enchanting table or anvil: protection, blast protection, fire protection, projectile protection, feather falling, respiration, thorns, depth strider, frost walker, curse of binding, soul speed, curse of vanishing
- The enchantment glint now renders on horse armor
- Added an option to overhaul mending (disabled by default): mending no longer repairs items, instead mending allows the item to be repaired in the crafting menu in the same way as in an anvil without any experience cost: combining a tool with another one or the appropriate repair item is possible, all enchantments will be preserved
- Additionally, it makes repairing (just repairing, not adding new enchantments or anything alike) in an anvil not increase the items repair cost
### Removed
- Removed a bunch of server config options as they've been moved to `json`

## [v4.0.0-1.19.1] - 2022-08-16
- Ported to Minecraft 1.19.1
- Split into multi-loader project
### Added
- Specialized damage and protection enchantments (e.g. smite and blast protection) are now compatible with the respective base enchantment (sharpness and protection)
### Changed
- Enchantment compatibility options are now enabled by default

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
