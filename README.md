# Moonlight Library
Formerly Selene Library, Moonlight Lib is a set of specialized utilities and shared code that I use for my mods.
These include some highly specialized features such as dynamic asset generation, 2 new data registries, dynamic registration system as well as your usual multi loader helpers and wrappers to develop for both Fabric and Forge

## Main Features

- Dynamic Assets creation and texture manipulation
- Dynamic Block registration and BlockSetAPI
- BlockColorAPI with automatic color grouping and detection
- Usual cross platform utilities to develop for Forge and Fabric
- Custom Baked Models
- Dispenser Behavior wrapping helper
- VillagerAIHooks to add new schedules and activities to villagers
- Data driven Map Markers system
- Data driven bottle based "Soft Fluid" virtual fluid system
- Many helper classes

## Depending

Add this to your repositories block:
```maven { url = "https://registry.somethingcatchy.net/repository/maven-releases/" }```

Fabric Loom import line:

```modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")```

Neoforge Loom import line:

```modImplementation("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")```

Neoforge Mod Gradle import line:
```implementation fg.deobf("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")```

Where `moonlight_version` could be for instance `1.21-2.25.5`

        

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
