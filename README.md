# Moonlight Library

Formerly Selene Library, Moonlight Lib is a set of specialized utilities and shared code that I use for my mods.
These include some highly specialized features such as dynamic asset generation, 2 new data registries, dynamic
registration system as well as your usual multi loader helpers and wrappers to develop for both Fabric and Forge

## Main Features

- Dynamic Assets creation and texture manipulation. A very extensive and powerful system.
- Runtime data and resource packs.
- Dynamic Block registration and BlockSetAPI.
- BlockColorAPI with automatic color grouping and detection.
- Usual Cross-Platform utilities to develop for (Neo)Forge and Fabric. Simple and all you'll need really.
- Custom Baked Models.
- Global datapack folder
- Spawn Box Block, allows to have dedicated spawning areas in a structure
- Dispenser Behavior wrapping helper.
- VillagerAIHooks to add new schedules and activities to villagers.
- Data driven Villager Trades
- Data driven Map Markers system.
- Data driven bottle based "Soft Fluid" virtual fluid system
- Custom first and third person hand animations interfaces for items.
- Many helpful interfaces for blocks, entities and items.
- Texture Renderer system, allows to create texture containing rendered objects like items or entities.
- Many helper classes
- Helper commands like debug renderers or registry search (/mnl)

## Documentation

All the classes in the api package have some javadocs explaining their use

Additionally the repo includes many example classes that explain in detaul all the most important features of the mod
[Examples Here](https://github.com/MehVahdJukaar/Moonlight/tree/1.20/common/src/example/java)

The mod also adds 3 datapack registry systems. These by nature are mostly meant to use via datapack so we cant have many
javadocs.
These are

- Soft fluid
- Custom Map markers
- Custom Villager Trades

You can find additional documentation on Supplementaries wiki
here https://github.com/MehVahdJukaar/Supplementaries/wiki/Customization#custom-villager-trades

### Spawn Box Block

The mod adds a new structure related technical block, the Spawn Box Block.
To use it simply place it in a Jigsaw Structure piece or your own custom structure that implements ISpawnBoxStructure.
To configure it you'll have to give it a name and a size.

Then inside your structure .json file add a "spawn_boxes" entry like. The "name" parameter has to match the one you set.
The rest is very similar to "spawn_overrides".
Contrary to Spawn Overrides, this feature allows to spawn entities ONLY inside the boxes and allow one to have multiple
pools per structure (identified by their different names).

```json
{
  "spawn_boxes": [
    {
      "category": "monster",
      "name": "galleon_enemies",
      "spawns": [
        {
          "type": "minecraft:pillager",
          "maxCount": 4,
          "minCount": 1,
          "weight": 4
        },
        {
          "type": "supplementaries:plunderer",
          "maxCount": 3,
          "minCount": 1,
          "weight": 7
        }
      ]
    }
  ]
}
```

### Data Driven Villager Trades

This feature allows you to add and remove trades from villagers, modded and vanilla ones.

To do so you'll have to place a json file in `data/[villager mod id]/moonlight/villager_trades/[villager profession name]` folder.
For instance for a vanilla farmer villager the path would be `data/minecraft/moonlight/villager_trades/farmer`

Here is the basic simple trade json type. Note that other more specialized ones exist. See mod datapack for example.

Here only first 3 parameters are required, rest is optional.

``` json
{
  "type": "simple",
  "offer": {
    "id": "minecraft:stone",
    "Count": 1
  },
  "price": {
    "id": "minecraft:emerald",
    "Count": 4
  },
  "price_secondary": {
    "id": "minecraft:cobblestone",
    "Count": 1
  },
  "max_trades": 16,
  "price_multiplier": 0.05,
  "xp": 5,
  "level": 2
}
```

You can also add biome variant trades as follows (1.21+):

``` json
{
  "type": "villager_type_variant",
  "default": {
     "type": "simple",
     //... trade content, same as above
     
  }
  "trades_per_type":{
    "minecraft:desert":{
       "type": "simple"
       // another trade content
    },
    "minecraft:jungle":{
       "type": "simple"
       // another trade content
    }
  }
}
```

Finally to remove trades you can use the "remove" trade type like so. It will remove all non data trades from the given level.
The "no_op" type can also be used to override data files from other mods.

``` json
{
  "type": "remove_all_non_data",
   "level": 3
}
```

Custom trade types can also be registered via code using the ItemListingManager API class.
