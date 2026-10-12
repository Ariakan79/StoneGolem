// Stone Golem (Bedrock edition): state machine, target tracking, ground stomp, rock summon, waking and Golem Core drop.
// Mirrors the Java edition's StoneGolem; all numbers come from balance.js (generated from bedrock/balance.json).
import { EntityDamageCause, GameMode, ItemStack, Player, system, world } from "@minecraft/server";
import { B } from "./balance.js";

const GOLEM = "stonegolem:stone_golem";
const ROCK = "stonegolem:rising_rock";
const STATE = "stonegolem:state";
const ROCK_SCALE = "stonegolem:scale";
const GOLEM_HALF_WIDTH = 0.9;
const ROCK_RADIUS = 0.75;
const ROCK_HEIGHT = 3.5;

// Blocks whose breaking within WAKE_RADIUS wakes a dormant golem (any "*_ore" block counts as well).
const WAKE_BLOCKS = new Set([
  "minecraft:stone", "minecraft:deepslate", "minecraft:andesite", "minecraft:diorite", "minecraft:granite", "minecraft:tuff",
  "minecraft:cobblestone", "minecraft:mossy_cobblestone", "minecraft:cobbled_deepslate", "minecraft:calcite",
]);

const STATE_EVENTS = {
  dormant: "stonegolem:to_dormant",
  awakening: "stonegolem:to_awakening",
  active: "stonegolem:to_active",
  stomping: "stonegolem:to_rooted",
  summoning: "stonegolem:to_rooted",
  settling: "stonegolem:to_settling",
};
const STATE_SOUNDS = {
  awakening: "stonegolem.awaken",
  settling: "stonegolem.settle",
  stomping: "stonegolem.stomp_windup",
  summoning: "stonegolem.summon",
};

/** Per-golem runtime data (not persisted; a reloaded golem simply restarts its timers). */
const golems = new Map();
/** Per-rock runtime data; untracked rocks (e.g. after a reload) are removed. */
const rocks = new Map();

function data(golem) {
  let d = golems.get(golem.id);
  if (!d) {
    d = { stateTicks: 0, idleTicks: 0, stompCooldown: 0, summonCooldown: 0, targetId: undefined, summonPos: undefined };
    golems.set(golem.id, d);
  }
  return d;
}

export function getState(golem) {
  return golem.getProperty(STATE);
}

function setState(golem, state) {
  const d = data(golem);
  golem.setProperty(STATE, state);
  golem.triggerEvent(STATE_EVENTS[state]);
  d.stateTicks = 0;
  if (state === "dormant" || state === "settling") {
    d.targetId = undefined;
  }
  const sound = STATE_SOUNDS[state];
  if (sound) {
    golem.dimension.playSound(sound, golem.location, { volume: 1.5 });
  }
}

/** Wakes a dormant or settling golem. Returns whether it started waking up. */
export function wakeUp(golem) {
  const state = getState(golem);
  if (state === "dormant" || state === "settling") {
    setState(golem, "awakening");
    return true;
  }
  return false;
}

function isAttackablePlayer(entity) {
  if (!(entity instanceof Player)) return true;
  const mode = entity.getGameMode();
  return mode !== GameMode.Creative && mode !== GameMode.Spectator;
}

function distance(a, b) {
  return Math.hypot(a.x - b.x, a.y - b.y, a.z - b.z);
}

function resolveTarget(golem, d) {
  if (!d.targetId) return undefined;
  const target = world.getEntity(d.targetId);
  const valid = target && target.isValid && target.dimension.id === golem.dimension.id
    && (target.getComponent("minecraft:health")?.currentValue ?? 0) > 0
    && distance(target.location, golem.location) <= B.FOLLOW_RANGE
    && isAttackablePlayer(target);
  if (!valid) {
    d.targetId = undefined;
    return undefined;
  }
  return target;
}

function hasLineOfSight(golem, target) {
  const from = golem.getHeadLocation();
  const to = target.getHeadLocation();
  const length = distance(from, to);
  const direction = { x: (to.x - from.x) / length, y: (to.y - from.y) / length, z: (to.z - from.z) / length };
  const hit = golem.dimension.getBlockFromRay(from, direction, { maxDistance: length, includePassableBlocks: false, includeLiquidBlocks: false });
  return hit === undefined;
}

function faceTarget(golem, target) {
  const dx = target.location.x - golem.location.x;
  const dz = target.location.z - golem.location.z;
  golem.setRotation({ x: 0, y: -Math.atan2(dx, dz) * 180 / Math.PI });
}

/** First air block above solid ground at or up to 4 blocks below the given location, or undefined. */
function findGround(dimension, location) {
  let y = Math.floor(location.y);
  for (let i = 0; i < 5; i++, y--) {
    const below = dimension.getBlock({ x: Math.floor(location.x), y: y - 1, z: Math.floor(location.z) });
    if (below && !below.isAir && !below.isLiquid) {
      return { x: Math.floor(location.x) + 0.5, y, z: Math.floor(location.z) + 0.5 };
    }
  }
  return undefined;
}

function debris(dimension, center, radius, count) {
  for (let i = 0; i < count; i++) {
    const angle = Math.random() * Math.PI * 2;
    const r = Math.random() * radius;
    try {
      dimension.spawnParticle("stonegolem:stone_debris", { x: center.x + Math.cos(angle) * r, y: center.y + 0.1, z: center.z + Math.sin(angle) * r });
    } catch {
      // Chunk not loaded for particles; purely cosmetic.
    }
  }
}

function canBeHit(entity) {
  if (entity.typeId === GOLEM || entity.typeId === ROCK) return false;
  if (!entity.getComponent("minecraft:health")) return false;
  return isAttackablePlayer(entity);
}

// --- Special attacks -------------------------------------------------------------------------------------------------

function performStomp(golem, d) {
  d.stompCooldown = B.STOMP_COOLDOWN_TICKS;
  const reach = B.STOMP_RADIUS + GOLEM_HALF_WIDTH;
  const victims = golem.dimension.getEntities({ location: golem.location, maxDistance: reach + 0.5 })
    .filter(e => canBeHit(e) && Math.abs(e.location.y - golem.location.y) <= 1.5);
  for (const victim of victims) {
    const dx = victim.location.x - golem.location.x;
    const dz = victim.location.z - golem.location.z;
    const horizontal = Math.hypot(dx, dz);
    if (horizontal > reach + 0.3) continue;
    victim.applyDamage(B.STOMP_DAMAGE, { cause: EntityDamageCause.entityAttack, damagingEntity: golem });
    const nx = horizontal > 0.01 ? dx / horizontal : 0;
    const nz = horizontal > 0.01 ? dz / horizontal : 0;
    try {
      victim.applyKnockback({ x: nx * B.STOMP_OUTWARD, z: nz * B.STOMP_OUTWARD }, B.STOMP_LAUNCH);
    } catch {
      // Some entities (e.g. minecarts) do not support knockback.
    }
  }
  debris(golem.dimension, golem.location, B.STOMP_RADIUS, 40);
  golem.dimension.playSound("stonegolem.stomp", golem.location, { volume: 2.0 });
}

function spawnRock(dimension, location, scale) {
  const rock = dimension.spawnEntity(ROCK, location);
  rock.setProperty(ROCK_SCALE, scale);
  rock.setRotation({ x: 0, y: Math.random() * 360 });
  rocks.set(rock.id, { age: 0, launched: new Set() });
}

function summonRocks(golem, d) {
  d.summonCooldown = B.SUMMON_COOLDOWN_TICKS;
  const center = d.summonPos;
  d.summonPos = undefined;
  if (!center) return;
  spawnRock(golem.dimension, center, 1.0);
  for (let i = 0; i < B.SUMMON_EXTRA_ROCKS; i++) {
    const angle = Math.random() * Math.PI * 2;
    const dist = 1.6 + Math.random() * 0.6;
    const ground = findGround(golem.dimension, { x: center.x + Math.cos(angle) * dist, y: center.y + 1, z: center.z + Math.sin(angle) * dist });
    if (ground) {
      spawnRock(golem.dimension, { x: center.x + Math.cos(angle) * dist, y: ground.y, z: center.z + Math.sin(angle) * dist },
        0.55 + Math.random() * 0.15);
    }
  }
}

// --- Per-tick logic --------------------------------------------------------------------------------------------------

function tickGolem(golem) {
  const d = data(golem);
  d.stateTicks++;
  if (d.stompCooldown > 0) d.stompCooldown--;
  if (d.summonCooldown > 0) d.summonCooldown--;
  const target = resolveTarget(golem, d);

  switch (getState(golem)) {
    case "awakening":
      if (d.stateTicks >= B.AWAKEN_TICKS) {
        d.idleTicks = 0;
        setState(golem, "active");
      }
      break;
    case "active": {
      if (!target) {
        if (++d.idleTicks >= B.RETURN_TO_DORMANT_TICKS) setState(golem, "settling");
        break;
      }
      d.idleTicks = 0;
      if (!golem.isOnGround) break;
      const dist = Math.hypot(target.location.x - golem.location.x, target.location.z - golem.location.z);
      if (d.stompCooldown <= 0 && dist <= B.STOMP_TRIGGER_RANGE + 0.3) {
        setState(golem, "stomping");
      } else if (d.summonCooldown <= 0 && dist >= B.SUMMON_MIN_RANGE && dist <= B.SUMMON_MAX_RANGE && hasLineOfSight(golem, target)) {
        // The spot is fixed when the cast starts, so a target that notices the warning can step away.
        const ground = findGround(golem.dimension, target.location);
        if (ground) {
          d.summonPos = ground;
          setState(golem, "summoning");
        }
      }
      break;
    }
    case "stomping":
      if (target && d.stateTicks < B.STOMP_WINDUP_TICKS) faceTarget(golem, target);
      if (d.stateTicks === B.STOMP_WINDUP_TICKS) performStomp(golem, d);
      else if (d.stateTicks >= B.STOMP_WINDUP_TICKS + B.STOMP_RECOVERY_TICKS) setState(golem, "active");
      break;
    case "summoning":
      if (target && d.stateTicks < B.SUMMON_WINDUP_TICKS) faceTarget(golem, target);
      if (d.summonPos && d.stateTicks < B.SUMMON_WINDUP_TICKS && d.stateTicks % 3 === 0) {
        debris(golem.dimension, d.summonPos, 0.8, 6); // telegraph: the ground under the target crumbles
      }
      if (d.stateTicks === B.SUMMON_WINDUP_TICKS) summonRocks(golem, d);
      else if (d.stateTicks >= B.SUMMON_WINDUP_TICKS + B.SUMMON_RECOVERY_TICKS) setState(golem, "active");
      break;
    case "settling":
      if (d.stateTicks >= B.SETTLE_TICKS) setState(golem, "dormant");
      break;
    default:
      break;
  }
}

function rockLifetime() {
  return B.ROCK_RISE_TICKS + B.ROCK_HOLD_TICKS + B.ROCK_SINK_TICKS;
}

function tickRock(rock) {
  const r = rocks.get(rock.id);
  if (!r) {
    rock.remove(); // left over from before a reload
    return;
  }
  r.age++;
  const scale = rock.getProperty(ROCK_SCALE) ?? 1.0;
  if (r.age === 1) {
    rock.dimension.playSound("stonegolem.rock_erupt", rock.location, { volume: 1.5, pitch: 1.2 - scale * 0.3 });
    debris(rock.dimension, rock.location, ROCK_RADIUS * scale, 20);
  }
  if (r.age <= B.ROCK_RISE_TICKS) {
    const radius = ROCK_RADIUS * scale;
    const nearby = rock.dimension.getEntities({ location: rock.location, maxDistance: radius + 1.5 });
    for (const entity of nearby) {
      if (r.launched.has(entity.id) || !canBeHit(entity)) continue;
      if (entity instanceof Player && entity.isFlying) continue;
      const dx = entity.location.x - rock.location.x;
      const dz = entity.location.z - rock.location.z;
      const dy = entity.location.y - rock.location.y;
      if (Math.hypot(dx, dz) > radius + 0.3 || dy < -0.5 || dy > ROCK_HEIGHT * scale) continue;
      r.launched.add(entity.id);
      // An eruption, not knockback: thrown straight up.
      try {
        entity.applyKnockback({ x: 0, z: 0 }, B.ROCK_LAUNCH);
      } catch {
        // Entity without knockback support.
      }
    }
  } else if (r.age > B.ROCK_RISE_TICKS + B.ROCK_HOLD_TICKS && r.age % 4 === 0) {
    debris(rock.dimension, rock.location, ROCK_RADIUS * scale, 3);
  }
  if (r.age >= rockLifetime()) {
    rocks.delete(rock.id);
    rock.remove();
  }
}

system.runInterval(() => {
  for (const dimensionId of ["overworld", "nether", "the_end"]) {
    const dimension = world.getDimension(dimensionId);
    for (const golem of dimension.getEntities({ type: GOLEM })) tickGolem(golem);
    for (const rock of dimension.getEntities({ type: ROCK })) tickRock(rock);
  }
}, 1);

// --- Events ----------------------------------------------------------------------------------------------------------

function retaliate(golem, attacker) {
  if (attacker && attacker.isValid && attacker.typeId !== GOLEM && isAttackablePlayer(attacker)) {
    data(golem).targetId = attacker.id;
  }
}

// Any melee or projectile hit wakes the golem. Bedrock does not always report the attacker in entityHurt (player
// hits arrive without damagingEntity), so the target also comes from the hit events below.
const WAKING_CAUSES = new Set([EntityDamageCause.entityAttack, EntityDamageCause.projectile, EntityDamageCause.entityExplosion]);
const MELEE_REACH = 6;
world.afterEvents.entityHurt.subscribe(event => {
  const golem = event.hurtEntity;
  let attacker = event.damageSource.damagingEntity;
  if (!attacker && event.damageSource.cause === EntityDamageCause.entityAttack) {
    // Unknown melee attacker: it must stand within reach, so the closest attackable player is the culprit.
    attacker = golem.dimension.getEntities({ type: "minecraft:player", location: golem.location, maxDistance: MELEE_REACH })
      .filter(isAttackablePlayer)
      .sort((a, b) => distance(a.location, golem.location) - distance(b.location, golem.location))[0];
  }
  retaliate(golem, attacker);
  if (attacker || WAKING_CAUSES.has(event.damageSource.cause)) wakeUp(golem);
}, { entityTypes: [GOLEM] });

world.afterEvents.entityHitEntity.subscribe(event => {
  if (event.hitEntity.typeId !== GOLEM) return;
  retaliate(event.hitEntity, event.damagingEntity);
});

world.afterEvents.projectileHitEntity.subscribe(event => {
  const golem = event.getEntityHit()?.entity;
  if (golem?.typeId === GOLEM) retaliate(golem, event.source);
});

world.afterEvents.playerBreakBlock.subscribe(event => {
  const id = event.brokenBlockPermutation.type.id;
  if (!WAKE_BLOCKS.has(id) && !id.endsWith("_ore")) return;
  const center = { x: event.block.location.x + 0.5, y: event.block.location.y + 0.5, z: event.block.location.z + 0.5 };
  for (const golem of event.dimension.getEntities({ type: GOLEM, location: center, maxDistance: B.WAKE_RADIUS })) {
    if (getState(golem) === "dormant") wakeUp(golem);
  }
});

world.afterEvents.entityDie.subscribe(event => {
  const golem = event.deadEntity;
  golems.delete(golem.id);
  const killer = event.damageSource.damagingEntity;
  if (!(killer instanceof Player)) return;
  const weapon = killer.getComponent("minecraft:equippable")?.getEquipment("Mainhand");
  const looting = weapon?.getComponent("minecraft:enchantable")?.getEnchantment("looting")?.level ?? 0;
  if (Math.random() < B.GOLEM_CORE_CHANCE + B.GOLEM_CORE_CHANCE_PER_LOOTING * looting) {
    const core = new ItemStack("stonegolem:golem_core", 1);
    core.setLore([{ translate: "item.stonegolem:golem_core.tooltip" }]);
    golem.dimension.spawnItem(core, golem.location);
  }
}, { entityTypes: [GOLEM] });

world.afterEvents.entityRemove.subscribe(event => {
  golems.delete(event.removedEntityId);
  rocks.delete(event.removedEntityId);
});

/**
 * Diagnostics (needs cheats): /scriptevent stonegolem:state prints the nearest golem's state to the caller's chat,
 * /scriptevent stonegolem:wake wakes it.
 */
system.afterEvents.scriptEventReceive.subscribe(event => {
  const source = event.sourceEntity;
  if (!source || !event.id.startsWith("stonegolem:")) return;
  const golem = source.dimension.getEntities({ type: GOLEM, location: source.location, closest: 1 })[0];
  const reply = message => (source instanceof Player ? source.sendMessage(message) : console.warn(message));
  if (!golem) {
    reply("[Stone Golem] no golem loaded nearby");
    return;
  }
  if (event.id === "stonegolem:wake") wakeUp(golem);
  if (event.id === "stonegolem:state") {
    const d = data(golem);
    const target = d.targetId ? world.getEntity(d.targetId) : undefined;
    const health = golem.getComponent("minecraft:health")?.currentValue;
    reply(`[Stone Golem] state=${getState(golem)} health=${health} target=${target?.nameTag || target?.typeId || "-"} `
      + `distance=${distance(golem.location, source.location).toFixed(1)} stomp_cd=${d.stompCooldown} summon_cd=${d.summonCooldown} `
      + `version=${B.VERSION}`);
  }
});
