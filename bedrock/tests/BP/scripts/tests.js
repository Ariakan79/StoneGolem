// GameTests for the Stone Golem Bedrock edition. Run on a dev world with the Beta APIs experiment:
//   /gametest runset stonegolem
// They exercise the real paths (a simulated player hits, mines and gets thrown) instead of internal shortcuts.
import { EntityDamageCause, GameMode, ItemStack } from "@minecraft/server";
import * as gt from "@minecraft/server-gametest";

const ARENA = "stonegolem:arena"; // 24x12x24, stone floor at y = 0 (saved into the test world, see bedrock/README.md)
const GOLEM = "stonegolem:stone_golem";
const STATE = "stonegolem:state";
const CENTER = { x: 12, y: 1, z: 12 };

function state(golem) {
  return golem.getProperty(STATE);
}

function health(entity) {
  return entity.getComponent("minecraft:health").currentValue;
}

function hitBy(golem, attacker, amount = 1) {
  golem.applyDamage(amount, { cause: EntityDamageCause.entityAttack, damagingEntity: attacker });
}

function register(name, fn, maxTicks = 300) {
  gt.register("stonegolem", name, fn).structureName(ARENA).maxTicks(maxTicks).tag("stonegolem");
}

register("spawns_dormant", test => {
  const golem = test.spawn(GOLEM, CENTER);
  let start;
  test.startSequence()
    .thenExecuteAfter(5, () => { start = golem.location; })
    .thenExecuteAfter(100, () => {
      test.assert(state(golem) === "dormant", `state ${state(golem)}`);
      const moved = Math.hypot(golem.location.x - start.x, golem.location.z - start.z);
      test.assert(moved < 0.01, `dormant golem moved ${moved}`);
    })
    .thenSucceed();
});

register("wakes_when_attacked_and_is_invulnerable_meanwhile", test => {
  const golem = test.spawn(GOLEM, CENTER);
  const attacker = test.spawn("minecraft:husk", { x: 12, y: 1, z: 16 });
  attacker.addEffect("slowness", 600, { amplifier: 255, showParticles: false });
  let before;
  test.startSequence()
    .thenExecuteAfter(2, () => hitBy(golem, attacker))
    // After-events (entityHurt) run at the end of the tick, so the state changes one tick later.
    .thenExecuteAfter(1, () => {
      test.assert(state(golem) === "awakening", `state after hit: ${state(golem)}`);
      before = health(golem);
    })
    .thenExecuteAfter(12, () => {
      hitBy(golem, attacker, 6);
      test.assert(health(golem) === before, `took damage while awakening: ${before} -> ${health(golem)}`);
    })
    .thenWait(() => test.assert(state(golem) !== "awakening", "still awakening"))
    .thenExecute(() => test.assert(["active", "stomping", "summoning"].includes(state(golem)), `state ${state(golem)}`))
    .thenSucceed();
});

register("wakes_when_stone_mined_nearby", test => {
  const near = test.spawn(GOLEM, { x: 6, y: 1, z: 6 });
  const far = test.spawn(GOLEM, { x: 20, y: 1, z: 20 });
  const player = test.spawnSimulatedPlayer({ x: 6, y: 1, z: 9 }, "Miner", GameMode.Survival);
  test.startSequence()
    .thenExecuteAfter(2, () => {
      test.getDimension().setBlockType(test.worldBlockLocation({ x: 6, y: 1, z: 8 }), "minecraft:stone");
      player.setItem(new ItemStack("minecraft:diamond_pickaxe"), 0, true);
    })
    .thenExecuteAfter(5, () => player.breakBlock({ x: 6, y: 1, z: 8 }))
    .thenWait(() => test.assert(state(near) === "awakening" || state(near) === "active", `near: ${state(near)}`))
    .thenExecute(() => test.assert(state(far) === "dormant", `far: ${state(far)}`))
    .thenSucceed();
});

// Diagnoses "attacking does nothing": a real hit by a player in each game mode must wake the golem.
for (const [mode, name] of [[GameMode.Creative, "creative"], [GameMode.Survival, "survival"]]) {
  register(`player_hit_wakes_golem_${name}`, test => {
    const golem = test.spawn(GOLEM, CENTER);
    const player = test.spawnSimulatedPlayer({ x: 12, y: 1, z: 14 }, `Hitter_${name}`, mode);
    test.startSequence()
      .thenExecuteAfter(5, () => {
        player.lookAtEntity(golem);
        test.assert(player.attackEntity(golem), "attack was not performed");
      })
      .thenWait(() => test.assert(state(golem) !== "dormant", `still dormant after a ${name} hit`))
      .thenWait(() => test.assert(state(golem) === "active", `state ${state(golem)}`))
      .thenSucceed();
  });
}

register("pickaxe_beats_sword", test => {
  const a = test.spawn(GOLEM, { x: 5, y: 1, z: 12 });
  const b = test.spawn(GOLEM, { x: 19, y: 1, z: 12 });
  const pickaxer = test.spawnSimulatedPlayer({ x: 5, y: 1, z: 10 }, "Pick", GameMode.Survival);
  const swordsman = test.spawnSimulatedPlayer({ x: 19, y: 1, z: 10 }, "Sword", GameMode.Survival);
  let lossA, lossB;
  test.startSequence()
    .thenExecuteAfter(5, () => {
      pickaxer.setItem(new ItemStack("minecraft:iron_pickaxe"), 0, true);
      swordsman.setItem(new ItemStack("minecraft:iron_sword"), 0, true);
      pickaxer.lookAtEntity(a);
      swordsman.lookAtEntity(b);
    })
    .thenExecuteAfter(5, () => {
      const ha = health(a), hb = health(b);
      pickaxer.attackEntity(a);
      swordsman.attackEntity(b);
      lossA = ha - health(a);
      lossB = hb - health(b);
      test.assert(lossA > 0 && lossB > 0, `no damage: pickaxe ${lossA}, sword ${lossB}`);
      test.assert(lossA >= lossB * 2, `pickaxe ${lossA} should clearly beat sword ${lossB}`);
      test.print(`pickaxe ${lossA} vs sword ${lossB}`);
    })
    .thenSucceed();
});

register("stomp_then_slam", test => {
  const golem = test.spawn(GOLEM, CENTER);
  const victim = test.spawn("minecraft:husk", { x: 12, y: 1, z: 14.5 });
  victim.addEffect("slowness", 600, { amplifier: 255, showParticles: false });
  let afterStomp;
  let sawStomp = false;
  test.startSequence()
    .thenExecuteAfter(2, () => hitBy(golem, victim))
    .thenWait(() => {
      if (state(golem) === "stomping") sawStomp = true;
      test.assert(sawStomp && state(golem) === "active", "waiting for a complete stomp");
    })
    .thenExecute(() => {
      afterStomp = health(victim);
      test.assert(afterStomp < 20, `stomp did not hurt: ${afterStomp}`);
    })
    // The stomp is on cooldown now; the golem keeps fighting with its delayed ground slam.
    .thenWait(() => test.assert(!victim.isValid || health(victim) <= afterStomp - 10, "waiting for a slam"))
    .thenExecute(() => test.assert(state(golem) !== "stomping", "stomped again while on cooldown"))
    .thenSucceed();
}, 400);

// Note: simulated players are invisible to entity queries (and to damage attribution), so a husk stands in for the
// player here. Real players are regular entities; their flight height is checked by hand (see TESTING.md).
register("rock_summon_throws_target", test => {
  const golem = test.spawn(GOLEM, { x: 3, y: 1, z: 12 });
  const target = test.spawn("minecraft:husk", { x: 16, y: 1, z: 12 });
  target.addEffect("resistance", 600, { amplifier: 255, showParticles: false });
  target.addEffect("slowness", 600, { amplifier: 255, showParticles: false });
  let startY, maxY;
  test.startSequence()
    .thenExecuteAfter(2, () => hitBy(golem, target))
    .thenExecuteAfter(3, () => {
      startY = target.location.y;
      maxY = startY;
    })
    .thenWait(() => {
      maxY = Math.max(maxY, target.location.y);
      test.assert(maxY - startY > 1 && target.location.y < maxY - 0.5, "waiting for launch and fall");
    })
    .thenExecute(() => {
      const rise = maxY - startY;
      test.print(`target thrown ${rise.toFixed(2)} blocks`);
      test.assert(rise >= 7.3 && rise <= 8.7, `target should be thrown about 8 blocks, got ${rise.toFixed(2)}`);
    })
    .thenWait(() => test.assert(test.getDimension().getEntities({ type: "stonegolem:rising_rock" }).length === 0, "rocks must sink away"))
    .thenSucceed();
}, 500);

register("loot_drops_cobblestone", test => {
  const golem = test.spawn(GOLEM, CENTER);
  test.startSequence()
    .thenExecuteAfter(2, () => golem.kill())
    .thenWait(() => test.assertItemEntityPresent("minecraft:cobblestone", CENTER, 3, true))
    .thenSucceed();
});
