- Added Debug Config in **moonlight-client.toml** to help with debugging an issue for StoneZone, EveryCompat, & GemsRealm 
- Improved the detection system for WoodType & LeavesType to ensure the same type is not added twice 
- Updated a method in BlockType for StoneZone, EveryCompat, and GemsRealm
- CompatWoodTypes: Removed a duplicated LeavesType.Finder

---

## v2.14.11

- Improved the RegEx in BlockTypeResTrnsformer to fix [#968](https://github.com/MehVahdJukaar/WoodGood/issues/968)

---

## v2.14.10

- CompatWoodType: Fixed the crash with "Index 1 out of bounds" from a rare case

---

## v2.14.9

- Simplified the CompatWoodType's code
  - <span style="color: RED;">WARNING: ENSURE your world is backup before updating Moonlight Lib</span>
  - **Every Compat** - REASON: some WoodType may be not detected, check your log to ensure no blocks are missing from the world. 
- Simplified the RegEx in BlockTypeResTransformer
- configs will no longer be synced when on an integrated server. This should make them editable again from the client
- improvements to utilities helper functions that alter models for resource gen