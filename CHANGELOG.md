# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v4.1.0-1.19.1] - 2022-08-18
### Added
- Completely overhauled config for what enchantments are compatible with what items and which enchantments can be applied together
- The new system uses individual `json` files for each enchantment, this makes configuring every single vanilla enchantment possible
- To find out how exactly the new system works check out the documentation on the GitHub repository
- Added dynamic support for roman numerals outside of vanilla's default translation range
- Overhauled vanilla's `/enchant` command, it can now apply enchantment levels above the default max level, supports overriding and removing enchantments, and also handles books now
- Horse armor can now receive the following enchantments at an enchanting table or anvil: protection, blast protection, fire protection, projectile protection, feather falling, respiration, thorns, depth strider, frost walker, curse of binding, soul speed, curse of vanishing
- The enchantment glint now renders on horse armor
- Added an option to overhaul mending (disabled by default): mending no longer repairs items, instead it makes repairing (just repairing, not adding new enchantments or anything alike) in an anvil not increase the items repair cost
- Additionally, mending allows the item to be repaired in the crafting menu in the same way as in an anvil without any experience cost: combining a tool with another one or the appropriate repair item is possible, all enchantments will be preserved
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
