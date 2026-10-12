# Stone Golem

An ancient, sluggish guardian of stone for Minecraft **26.3** — for **Fabric** and **NeoForge**.

It sleeps as a mossy boulder deep in stone caves and on rocky mountain slopes. Hit it, or mine stone or ore right next to it, and it slowly heaves itself up. It towers about 5.4 blocks tall, twice an iron golem, and is neutral to everyone except whoever attacked it: two-fisted ground slams, a telegraphed stomp that throws everything around it into the air, and from a distance it calls up great rocks under its target that hurl it 8 blocks high before sinking back into the earth. Swords and arrows barely scratch it — bring a pickaxe.

Free and donation-funded. Code and assets are MIT-licensed (see [LICENSE](LICENSE)).

## Features

- **Dormant → awakening → active → settling**: a synchronized state machine with matching animations. While dormant the golem does not move, look around or make a sound. It stands up in about 1.25 s (invulnerable meanwhile) and settles down again after 30 s without a target.
- **Wakes up** when a player attacks it or breaks stone/ore (`#stonegolem:wakes_stone_golem`) within 4 blocks.
- **Ground slam** (melee): both fists up, impact 0.4 s later in front of it — 8 damage (4 hearts on Normal), only if the target is still in reach.
- **Ground stomp**: 0.75 s wind-up, 3-block area damage, launches entities, block particles of the ground, 8 s cooldown.
- **Rock summon** (targets 4–16 blocks away): one hand rises while the ground under the target crumbles (0.9 s warning), then one large and two smaller rocks erupt, throw everything on them ~8 blocks up and slowly sink away; 10 s cooldown. The fall hurts.
- **Weakness by tags, not hard-coded items**: weapons in `#stonegolem:effective_against_stone_golem` (pickaxes) deal ×2, weapons in `#stonegolem:ineffective_against_stone_golem` (swords) and damage types in `#stonegolem:ineffective_against_stone_golem` (projectiles) deal ×0.5.
- Immune to knockback, fall damage and drowning.
- **Spawning**: rare, one at a time; in covered caves below Y 40 in every biome of `#stonegolem:spawns_stone_golem` and on stone surfaces in `#stonegolem:stone_golem_surface_spawns` (mountains, stony shores, windswept hills).
- **Drops**: 4–8 cobblestone, 0–2 moss blocks, ~10 % (+ Looting) **Golem Core**, ~10 XP.
- English and German translations.

## Building

Requires nothing but a JDK to start Gradle; Gradle downloads JDK 25 for itself and the build (see `gradle/gradle-daemon-jvm.properties`).

```bash
./gradlew build                        # jars in fabric/build/libs and neoforge/build/libs
./gradlew :fabric:runClient            # or :neoforge:runClient
./gradlew :fabric:runGametest          # headless game tests (also :neoforge:runGameTestServer)
./gradlew :fabric:runClientGametest    # scripted client smoke test with screenshots
```

See [TESTING.md](TESTING.md) for the manual checklist and [ROADMAP.md](ROADMAP.md) for what comes next.

## Project layout

| Module     | Contents |
|------------|----------|
| `common`   | All gameplay: entity, AI, state machine, model, animations, renderer, tags, loot, assets, game tests. Vanilla code only. |
| `fabric`   | Entry points, registration, biome modification, events, test wrappers. |
| `neoforge` | Entry points, registration, biome modifier type, events, test registration. |

- **Balancing**: every number lives in [`StoneGolemBalance`](common/src/main/java/eu/ariakan/stonegolem/config/StoneGolemBalance.java).
- **Loader abstraction**: `IRegistrar` and `IPlatformHelper` services (Java `ServiceLoader`), implemented per loader.
- **Art**: [`tools/generate_textures.py`](tools/generate_textures.py) produces the first-draft textures. The model in `StoneGolemModel` uses Blockbench-style part names (`body`, `head`, `left_arm`, `right_arm`, `left_leg`, `right_leg`); animations in `StoneGolemAnimations` follow Blockbench's Java export format, so both can be replaced by Blockbench exports.

Built on [jaredlll08/MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template). Mod ID `stonegolem`, package `eu.ariakan.stonegolem`.
