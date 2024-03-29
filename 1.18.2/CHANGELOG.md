# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v3.1.0-1.18.2] - 2023-08-16
- Ported to Minecraft 1.18.2

## [v3.0.6-1.18.2] - 2022-04-02
### Fixed
- Fixed a potential crash when attempting to create an already existing enum value

## [v3.0.5-1.18.2] - 2022-04-01
### Added
- Feather falling prevents farmland from being trampled
### Changed
- Multishot on bows now shoots all arrows centered on the crosshair, they are no longer spread out as they are when using a crossbow

## [v3.0.4-1.18.2] - 2022-03-25
### Fixed
- Fixed game crashing during config reload while a custom enchantment category is accessed

## [v3.0.3-1.18.2] - 2022-03-17
### Fixed
- Fixed mod preventing servers from starting due to faulty import

## [v3.0.2-1.18.2] - 2022-03-17
### Fixed
- Fixed sweeping edge triggering when it shouldn't

## [v3.0.1-1.18.2] - 2022-03-16
### Changed
- Only experience from mobs can be boosted now, experience from blocks is no longer affected
### Fixed
- Fixed an issue where new enum values would be created multiple times
- Fixed problematic mixin for enabling sweeping edge for all weapons with the sweeping edge enchantment

## [v3.0.0-1.18.2] - 2022-03-14
- Initial release

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
