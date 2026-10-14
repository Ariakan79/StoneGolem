# Roadmap

## Phase 1 — Stone Golem (done, version 0.1.0)
Standalone neutral mob with dormant/awaken/active/settle cycle, ground stomp, tag-based weapon weakness, natural spawning, loot (incl. Golem Core), en_us/de_de, game tests on Fabric and NeoForge.

## Phase 2 — notes only, starts after approval

### Tameable golem (player-built) — first step done
- Done (Java): build from four stone + a block of diamond (T shape, head last) → guardian golem that never attacks
  players, never sleeps, hunts monsters. Bedrock port pending.
- Still open: a purpose for the **Golem Core** (e.g. upgrade or heal a built golem), commands (follow/guard/stay),
  healing (repair with stone?), an optional "guard post" mode in which a built golem rests as a boulder.
- Must not be an Iron Golem clone: keep the dormant boulder identity.

### Biome variants (data-driven)
- Deepslate, sandstone and basalt golems as **data-driven variants** (a variant registry, like vanilla wolf/cat variants): texture, drops, maybe stats per variant, selected by biome tags at spawn.
- Variant synced via entity data; renderer picks the texture from the variant.

### Config file for balancing
- Load the values from `StoneGolemBalance` from a config file (per world or global), keeping today's numbers as defaults.
- Choose between the loaders' native config systems (NeoForge `ModConfigSpec`, a small common JSON/TOML loader for Fabric) or one common format — decision pending, no third-party config library without approval.

### Polish (candidates)
- Blockbench rework of model, texture and animations; own sounds.
- Crack overlay at low health; emissive eyes.
- Advancements (e.g. "Rock Bottom": defeat a golem with a pickaxe).
