<div align="center">
  <img src=".idea/icon.png" width="300"/>

  # ProductiveCows
  
  [![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-orange.svg)](https://neoforged.net/)
	<a href="https://www.curseforge.com/minecraft/mc-mods/productive-cows"><img src="https://img.shields.io/curseforge/dt/1554716?logo=curseforge&label=&suffix=%20&style=flat&color=242629&labelColor=F16436&logoColor=1C1C1C" alt="CurseForge"></a>
  [![Modrinth](https://img.shields.io/badge/Modrinth-Pending-1bd96a.svg)](#)
</div>

ProductiveCows is a modern, data-driven resource generation mod for Minecraft. Breed, milk, and process custom material cows to automate your resource gathering! 

---

## Adding Custom Cows (For Modpack Devs)

ProductiveCows is **100% Data-Driven**. You can add new cows without writing a single line of Java by creating a simple JSON file in your Datapack or via KubeJS!

### Method 1: KubeJS (Recommended)
You can easily register new cows in your `server_scripts`:

```javascript
ProductiveCowsEvents.registerCows(event => {
    event.create('vibranium')
        .name('Vibranium')
        .tier(5)
        .fluid('productivecows:molten_vibranium')
        .color('#800080')
        .baseYield(100)
        .breedChance(0.05)
        .parents('productivecows:diamond', 'productivecows:obsidian')
        .coolingResult('c:ingots/vibranium')
})
```

### Method 2: JSON Datapack
Create a file in your datapack at: `data/<your_namespace>/cows/my_custom_cow.json`

```json
{
  "name": "Vibranium",
  "tier": 5,
  "hex_color": "#800080",
  "fluid": "productivecows:molten_vibranium",
  "base_yield": 100,
  "breed_chance": 0.05,
  "parent1": "productivecows:diamond",
  "parent2": "productivecows:obsidian",
  "cooling_result_tag": "c:ingots/vibranium"
}
```

That's it! The mod will automatically register the cow and process the cooling into the correct tag/item.

---

## Issues and Bug Reports

Found a bug or have a suggestion? We'd love to hear it! 
Please open an issue in the **[Issues Tab](../../issues)**. 

**Important:** Please ensure you select and fill out the provided Issue Template. Bug reports that don't follow the template may take much longer to resolve.
