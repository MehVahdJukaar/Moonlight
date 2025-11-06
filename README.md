# Moonlight Library
Formerly Selene Library, Moonlight Lib is a set of specialized utilities and shared code that I use for my mods.
These include some highly specialized features such as dynamic asset generation, 2 new data registries, dynamic registration system as well as your usual multi loader helpers and wrappers to develop for both Fabric and Forge

## Main Features

- Dynamic Assets creation and texture manipulation. A very extensive and powerful system.
- Runtime data and resource packs.
- Dynamic Block registration and BlockSetAPI.
- BlockColorAPI with automatic color grouping and detection.
- Usual Cross-Platform utilities to develop for (Neo)Forge and Fabric. Simple and all you'll need really.
- Custom Baked Models.
- Dispenser Behavior wrapping helper.
- VillagerAIHooks to add new schedules and activities to villagers.
- Data driven Villager Trades
- Data driven Map Markers system.
- Data driven bottle based "Soft Fluid" virtual fluid system
- Custom first and third person hand animations interfaces for items.
- Many helpful interfaces for blocks, entities and items.
- Texture Renderer system, allows to create texture containing rendered objects like items or entities.
- Many helper classes

## Documentation
All the classes in the api package have some javadocs explaining their use

Additionally the repo includes many example classes that explain in detaul all the most important features of the mod
[Examples Here](https://github.com/MehVahdJukaar/Moonlight/tree/1.20/common/src/example/java)


The mod also adds 3 datapack registry systems. These by nature are mostly meant to use via datapack so we cant have many javadocs.
These are
- Soft fluid
- Custom Map markers
- Custom Villager Trades

You can find additional documentation on Supplementaries wiki here https://github.com/MehVahdJukaar/Supplementaries/wiki/Customization#custom-villager-trades
