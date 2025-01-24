## Defaulted

[![Get a Bisect Hosting Server with 25% off using code atlasdev!](https://www.bisecthosting.com/partners/custom-banners/51837ee1-91bf-4a03-abff-c5dd3dacac79.webp)](https://alexandra-myers.github.io/Promolink)

***

A simple mod, provides functionality to customise default item components using a datapack registry.

Required on both the server AND client.

***

### Default Component Patches Registry

***

The JSON files for the defaulted:default_component_patches registry should be put in <namespace>/defaulted/default_component_patches/<path>.json.

Default component patches have two fields:
1. `items`: A list of items to apply the following patch to.
2. `patch`: The data component patch to apply to the set of items.

Example:
```json
{
  "items": [
    "minecraft:netherite_sword",
    "minecraft:diamond_sword",
    "minecraft:golden_sword"
  ],
  "patch": {
    "minecraft:enchantments": {
      "sweeping_edge": 3,
      "sharpness": 9,
      "looting": 3
    }
  }
}
```
* Makes Netherite, Diamond, and Golden Swords have a default set of enchantments.

In game:
![Screenshot 2025-01-10 155610](https://github.com/user-attachments/assets/59166cd2-5f36-4cd7-8946-67bae8f6b805)
*Note: This screenshot was taken without the Golden Sword modified.*

The given example is just one of many ways to use this registry, any data component for an item can be modified here, and it will replace or add the component to the item.
Default components can also be removed by appending an ! in front of the component type's name.
The following shows how you could that in a JSON file:
```json
{
  "items": [
    "minecraft:netherite_sword",
    "minecraft:diamond_pickaxe"
  ],
  "patch": {
    "minecraft:enchantments": {
      "fortune": 5,
      "looting": 3,
      "sweeping_edge": 3
    },
    "minecraft:unbreakable": {},
    "!minecraft:attribute_modifiers": {}
  }
}
```
* This example removes the attribute modifiers from Netherite Swords and Diamond Pickaxes, gives them default enchantments, and makes them unbreakable.

Note that all changes for the registry will be applied alphabetically, namespace before path.
For example, `custom:patches` (which would be located in `custom/defaulted/default_component_patches/patches.json`) would apply after `custom:amazing_patches`, but before `d:amazing_patches`.
This should be kept in mind for how different datapacks utilising this system could interact.
