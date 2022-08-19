# Universal Enchants

A Minecraft mod. Downloads can be found on [CurseForge](https://www.curseforge.com/members/fuzs_/projects) and [Modrinth](https://modrinth.com/user/Fuzs).

![](https://i.imgur.com/MynoO6R.png)

## Configuring the mod
Universal Enchants allows you to define what enchantments can be applied to what items, and which enchantments are compatible with each other (meaning can be applied on a single item at the same time). This is done via individual `json` config files (one per enchantment) found in `.minecraft/config/universalenchants`.

By default, only config files for all vanilla enchantments are generated. Config files for modded enchantments can be manually added though, but are not officially supported.

The internal implementation of individual enchantments is rather complex and relies on a bunch of hard-coded special cases. All items enabled by default in the `items` field are guaranteed to work, everything beyond that is untested, especially modded enchantments.

### Config file structure
```json
{
  "id": "minecraft:smite",
  "items": [
    "$minecraft:weapon",
    "$universalenchants:axe",
    "$minecraft:trident"
  ],
  "incompatible": [
    "minecraft:bane_of_arthropods",
    "minecraft:impaling"
  ]
}
```

- `id`: The enchantment this file is defining values for, the file name has no effect. There can only be one config file per enchantment.
- `items`: The items this enchantment can be applied to in an enchanting table or anvil. This entry supports three kinds of values: single enchantments, enchantment tags (prefixed using `#`) and built-in enchantment categories (prefixed using `$`, see [below](#built-in-enchantment-categories)). Each kind of entry can be set as an inverse, to force it to be excluded instead of included (see [below](#excluding-entries-from-items)).
- `incompatible`: Other enchantments that are incompatible with this enchantment (they cannot be applied together on a single item at the same time). An enchantment is always incompatible with itself, there is no setting for that.

### Excluding entries from `items`
Each entry in `items` (single enchantment, enchantment tag or enchantment category) can be inversed, allowing it to be excluded from other entries instead of included. This is useful when just a few values from a category or tag are not desired, to avoid having to include all contents of said category or tag manually minus the few supposed to be excluded.

To inverse an entry instead of
```json
{
  "id": "minecraft:fortune",
  "items": [
    "minecraft:diamond_pickaxe"
  ]
}
```
you add
```json
{
  "id": "minecraft:fortune",
  "items": [
    {
      "id": "minecraft:diamond_pickaxe",
      "exclude": true
    }
  ] 
}
```
When `exclude` is set to false the entry will behave normally as without the `exclude` tag.

### Built-in enchantment categories
This table includes vanilla's items compatible with each enchantment category. When other mods are installed, these categories will dynamically include additional items. Categories are shared here to be able to get a sense of the kind of items that are included. 

| Id        | Vanilla items |
|-----------|---------------|
| `minecraft:armor` | `minecraft:golden_boots`, `minecraft:chainmail_chestplate`, `minecraft:leather_chestplate`, `minecraft:diamond_helmet`, `minecraft:leather_helmet`, `minecraft:turtle_helmet`, `minecraft:chainmail_helmet`, `minecraft:leather_boots`, `minecraft:diamond_chestplate`, `minecraft:iron_leggings`, `minecraft:golden_chestplate`, `minecraft:golden_helmet`, `minecraft:netherite_boots`, `minecraft:leather_leggings`, `minecraft:netherite_chestplate`, `minecraft:iron_chestplate`, `minecraft:chainmail_leggings`, `minecraft:iron_helmet`, `minecraft:golden_leggings`, `minecraft:netherite_leggings`, `minecraft:chainmail_boots`, `minecraft:diamond_boots`, `minecraft:iron_boots`, `minecraft:netherite_helmet`, `minecraft:diamond_leggings` |
| `minecraft:armor_feet` | `minecraft:golden_boots`, `minecraft:chainmail_boots`, `minecraft:diamond_boots`, `minecraft:iron_boots`, `minecraft:leather_boots`, `minecraft:netherite_boots` |
| `minecraft:armor_legs` | `minecraft:iron_leggings`, `minecraft:chainmail_leggings`, `minecraft:diamond_leggings`, `minecraft:golden_leggings`, `minecraft:netherite_leggings`, `minecraft:leather_leggings` |
| `minecraft:armor_chest` | `minecraft:chainmail_chestplate`, `minecraft:netherite_chestplate`, `minecraft:diamond_chestplate`, `minecraft:leather_chestplate`, `minecraft:iron_chestplate`, `minecraft:golden_chestplate` |
| `minecraft:armor_head` | `minecraft:netherite_helmet`, `minecraft:diamond_helmet`, `minecraft:leather_helmet`, `minecraft:iron_helmet`, `minecraft:turtle_helmet`, `minecraft:golden_helmet`, `minecraft:chainmail_helmet` |
| `minecraft:weapon` | `minecraft:diamond_sword`, `minecraft:stone_sword`, `minecraft:iron_sword`, `minecraft:netherite_sword`, `minecraft:wooden_sword`, `minecraft:golden_sword` |
| `minecraft:digger` | `minecraft:netherite_pickaxe`, `minecraft:wooden_axe`, `minecraft:diamond_shovel`, `minecraft:iron_shovel`, `minecraft:iron_axe`, `minecraft:stone_shovel`, `minecraft:golden_pickaxe`, `minecraft:wooden_hoe`, `minecraft:stone_axe`, `minecraft:golden_axe`, `minecraft:iron_pickaxe`, `minecraft:stone_hoe`, `minecraft:netherite_axe`, `minecraft:diamond_axe`, `minecraft:wooden_shovel`, `minecraft:golden_shovel`, `minecraft:diamond_pickaxe`, `minecraft:diamond_hoe`, `minecraft:iron_hoe`, `minecraft:golden_hoe`, `minecraft:netherite_shovel`, `minecraft:netherite_hoe`, `minecraft:wooden_pickaxe`, `minecraft:stone_pickaxe` |
| `minecraft:fishing_rod` | `minecraft:fishing_rod` |
| `minecraft:trident` | `minecraft:trident` |
| `minecraft:breakable` | `minecraft:golden_boots`, `minecraft:wooden_axe`, `minecraft:iron_shovel`, `minecraft:iron_axe`, `minecraft:leather_helmet`, `minecraft:turtle_helmet`, `minecraft:golden_pickaxe`, `minecraft:warped_fungus_on_a_stick`, `minecraft:golden_axe`, `minecraft:leather_boots`, `minecraft:stone_hoe`, `minecraft:diamond_axe`, `minecraft:crossbow`, `minecraft:golden_shovel`, `minecraft:netherite_boots`, `minecraft:leather_leggings`, `minecraft:bow`, `minecraft:netherite_pickaxe`, `minecraft:iron_sword`, `minecraft:netherite_chestplate`, `minecraft:carrot_on_a_stick`, `minecraft:stone_shovel`, `minecraft:iron_helmet`, `minecraft:stone_axe`, `minecraft:diamond_boots`, `minecraft:golden_sword`, `minecraft:diamond_hoe`, `minecraft:iron_hoe`, `minecraft:netherite_shovel`, `minecraft:wooden_pickaxe`, `minecraft:shears`, `minecraft:chainmail_chestplate`, `minecraft:diamond_sword`, `minecraft:stone_sword`, `minecraft:leather_chestplate`, `minecraft:netherite_sword`, `minecraft:wooden_sword`, `minecraft:diamond_helmet`, `minecraft:fishing_rod`, `minecraft:wooden_hoe`, `minecraft:chainmail_helmet`, `minecraft:iron_pickaxe`, `minecraft:shield`, `minecraft:netherite_axe`, `minecraft:diamond_chestplate`, `minecraft:wooden_shovel`, `minecraft:iron_leggings`, `minecraft:trident`, `minecraft:golden_chestplate`, `minecraft:golden_helmet`, `minecraft:flint_and_steel`, `minecraft:stone_pickaxe`, `minecraft:iron_chestplate`, `minecraft:diamond_shovel`, `minecraft:chainmail_leggings`, `minecraft:golden_leggings`, `minecraft:netherite_leggings`, `minecraft:chainmail_boots`, `minecraft:iron_boots`, `minecraft:netherite_helmet`, `minecraft:diamond_leggings`, `minecraft:diamond_pickaxe`, `minecraft:golden_hoe`, `minecraft:netherite_hoe`, `minecraft:elytra` |
| `minecraft:bow` | `minecraft:bow` |
| `minecraft:wearable` | `minecraft:golden_boots`, `minecraft:chainmail_chestplate`, `minecraft:jack_o_lantern`, `minecraft:leather_chestplate`, `minecraft:dragon_head`, `minecraft:diamond_helmet`, `minecraft:leather_helmet`, `minecraft:turtle_helmet`, `minecraft:zombie_head`, `minecraft:chainmail_helmet`, `minecraft:leather_boots`, `minecraft:player_head`, `minecraft:diamond_chestplate`, `minecraft:iron_leggings`, `minecraft:wither_skeleton_skull`, `minecraft:golden_chestplate`, `minecraft:golden_helmet`, `minecraft:netherite_boots`, `minecraft:leather_leggings`, `minecraft:netherite_chestplate`, `minecraft:iron_chestplate`, `minecraft:chainmail_leggings`, `minecraft:iron_helmet`, `minecraft:golden_leggings`, `minecraft:netherite_leggings`, `minecraft:chainmail_boots`, `minecraft:diamond_boots`, `minecraft:creeper_head`, `minecraft:iron_boots`, `minecraft:carved_pumpkin`, `minecraft:netherite_helmet`, `minecraft:diamond_leggings`, `minecraft:skeleton_skull`, `minecraft:elytra` |
| `minecraft:crossbow` | `minecraft:crossbow` |
| `minecraft:vanishable` | `minecraft:golden_boots`, `minecraft:jack_o_lantern`, `minecraft:wooden_axe`, `minecraft:iron_shovel`, `minecraft:iron_axe`, `minecraft:leather_helmet`, `minecraft:turtle_helmet`, `minecraft:golden_pickaxe`, `minecraft:warped_fungus_on_a_stick`, `minecraft:golden_axe`, `minecraft:leather_boots`, `minecraft:stone_hoe`, `minecraft:player_head`, `minecraft:diamond_axe`, `minecraft:crossbow`, `minecraft:golden_shovel`, `minecraft:compass`, `minecraft:netherite_boots`, `minecraft:leather_leggings`, `minecraft:bow`, `minecraft:netherite_pickaxe`, `minecraft:iron_sword`, `minecraft:netherite_chestplate`, `minecraft:carrot_on_a_stick`, `minecraft:stone_shovel`, `minecraft:iron_helmet`, `minecraft:stone_axe`, `minecraft:diamond_boots`, `minecraft:carved_pumpkin`, `minecraft:golden_sword`, `minecraft:diamond_hoe`, `minecraft:iron_hoe`, `minecraft:netherite_shovel`, `minecraft:wooden_pickaxe`, `minecraft:shears`, `minecraft:chainmail_chestplate`, `minecraft:diamond_sword`, `minecraft:stone_sword`, `minecraft:leather_chestplate`, `minecraft:netherite_sword`, `minecraft:wooden_sword`, `minecraft:dragon_head`, `minecraft:diamond_helmet`, `minecraft:zombie_head`, `minecraft:fishing_rod`, `minecraft:wooden_hoe`, `minecraft:chainmail_helmet`, `minecraft:iron_pickaxe`, `minecraft:shield`, `minecraft:netherite_axe`, `minecraft:diamond_chestplate`, `minecraft:wooden_shovel`, `minecraft:iron_leggings`, `minecraft:trident`, `minecraft:wither_skeleton_skull`, `minecraft:golden_chestplate`, `minecraft:golden_helmet`, `minecraft:flint_and_steel`, `minecraft:stone_pickaxe`, `minecraft:iron_chestplate`, `minecraft:diamond_shovel`, `minecraft:chainmail_leggings`, `minecraft:golden_leggings`, `minecraft:netherite_leggings`, `minecraft:chainmail_boots`, `minecraft:creeper_head`, `minecraft:iron_boots`, `minecraft:netherite_helmet`, `minecraft:diamond_leggings`, `minecraft:diamond_pickaxe`, `minecraft:skeleton_skull`, `minecraft:golden_hoe`, `minecraft:netherite_hoe`, `minecraft:elytra` |
| `universalenchants:axe` | `minecraft:wooden_axe`, `minecraft:netherite_axe`, `minecraft:diamond_axe`, `minecraft:iron_axe`, `minecraft:stone_axe`, `minecraft:golden_axe` |
| `universalenchants:horse_armor` | `minecraft:golden_horse_armor`, `minecraft:diamond_horse_armor`, `minecraft:leather_horse_armor`, `minecraft:iron_horse_armor` |
