package eu.ariakan.stonegolem.gametest;

import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.config.StoneGolemBalance;
import eu.ariakan.stonegolem.entity.GolemState;
import eu.ariakan.stonegolem.entity.RisingRock;
import eu.ariakan.stonegolem.entity.StoneGolem;
import eu.ariakan.stonegolem.event.StoneGolemEvents;
import eu.ariakan.stonegolem.registry.ModEntities;
import eu.ariakan.stonegolem.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Loader-independent game tests. The loader modules only wrap these functions in their own registration mechanism
 * (Fabric: {@code @GameTest} methods, NeoForge: {@code RegisterGameTestsEvent}).
 * <p>
 * Husks serve as dummy targets because, unlike zombies, they do not burn in daylight (fire damage would fake hits).
 * <p>
 * Tests run in {@code stonegolem:arena} (9x8x9, stone floor, barrier ceiling) unless they need room to fly:
 * those use {@code stonegolem:arena_large} (17x12x17) with sky access.
 */
public final class StoneGolemGameTests {

    public static final Identifier ARENA = Constants.id("arena");
    public static final Identifier ARENA_LARGE = Constants.id("arena_large");
    public static final int MAX_TICKS = 200;

    private static final BlockPos CENTER = new BlockPos(4, 1, 4);
    private static final float TEST_HIT = 6.0F;

    /** A test function plus the structure it runs in. */
    public record TestDefinition(Consumer<GameTestHelper> function, Identifier structure, boolean skyAccess) {
    }

    /** Test name to definition, in a stable order. */
    public static final Map<String, TestDefinition> TESTS = new LinkedHashMap<>();

    static {
        add("spawns_dormant", StoneGolemGameTests::spawnsDormant);
        add("wakes_when_attacked", StoneGolemGameTests::wakesWhenAttacked);
        add("wakes_when_stone_mined_nearby", StoneGolemGameTests::wakesWhenStoneMinedNearby);
        add("pickaxe_beats_sword", StoneGolemGameTests::pickaxeBeatsSword);
        add("projectiles_are_weak", StoneGolemGameTests::projectilesAreWeak);
        add("stomp_hits_and_starts_cooldown", StoneGolemGameTests::stompHitsAndStartsCooldown);
        add("slam_hits_at_impact", StoneGolemGameTests::slamHitsAtImpact);
        TESTS.put("rock_summon_launches_target", new TestDefinition(StoneGolemGameTests::rockSummonLaunchesTarget, ARENA_LARGE, true));
        add("loot_table_drops_cobblestone", StoneGolemGameTests::lootTableDropsCobblestone);
        add("biome_spawn_lists_contain_golem", StoneGolemGameTests::biomeSpawnListsContainGolem);
        add("spawn_rules_require_stone_and_cave", StoneGolemGameTests::spawnRulesRequireStoneAndCave);
    }

    private static void add(String name, Consumer<GameTestHelper> function) {
        TESTS.put(name, new TestDefinition(function, ARENA, false));
    }

    private StoneGolemGameTests() {
    }

    private static StoneGolem spawnGolem(GameTestHelper helper, BlockPos pos) {
        return helper.spawn(ModEntities.STONE_GOLEM.get(), pos);
    }

    private static void assertState(GameTestHelper helper, StoneGolem golem, GolemState expected) {
        helper.assertValueEqual(golem.getState(), expected, "golem state");
    }

    public static void spawnsDormant(GameTestHelper helper) {
        StoneGolem golem = spawnGolem(helper, CENTER);
        Vec3[] start = new Vec3[1];
        // Stays dormant and does not wander (falling onto the arena floor after spawning is fine).
        helper.startSequence()
                .thenIdle(5)
                .thenExecute(() -> {
                    assertState(helper, golem, GolemState.DORMANT);
                    start[0] = golem.position();
                })
                .thenIdle(100)
                .thenExecute(() -> {
                    assertState(helper, golem, GolemState.DORMANT);
                    helper.assertTrue(golem.getTarget() == null, "dormant golem must not have a target");
                    helper.assertValueEqual(golem.getBbHeight(), 3.0F, "dormant hitbox height");
                    helper.assertTrue(golem.position().subtract(start[0]).horizontalDistance() < 0.01, "dormant golem must not move: " + start[0] + " -> " + golem.position());
                })
                .thenSucceed();
    }

    public static void wakesWhenAttacked(GameTestHelper helper) {
        StoneGolem golem = spawnGolem(helper, CENTER);
        Zombie attacker = helper.spawn(EntityTypes.HUSK, CENTER.offset(3, 0, 0));
        attacker.setNoAi(true);
        DamageSource hit = helper.getLevel().damageSources().mobAttack(attacker);
        float[] health = new float[1];

        helper.startSequence()
                // Hit after the first tick: HurtByTargetGoal ignores hits stamped with the golem's tick 0.
                .thenIdle(1)
                .thenExecute(() -> {
                    helper.assertTrue(golem.hurtServer(helper.getLevel(), hit, 1.0F), "first hit must land");
                    assertState(helper, golem, GolemState.AWAKENING);
                    health[0] = golem.getHealth();
                })
                // Wait out the regular hurt cooldown (i-frames) so only the awakening invulnerability can block.
                .thenIdle(12)
                .thenExecute(() -> {
                    assertState(helper, golem, GolemState.AWAKENING);
                    helper.assertFalse(golem.hurtServer(helper.getLevel(), hit, TEST_HIT), "golem must be invulnerable while awakening");
                    helper.assertValueEqual(golem.getHealth(), health[0], "health while awakening");
                })
                .thenWaitUntil(() -> assertState(helper, golem, GolemState.ACTIVE))
                .thenExecute(() -> helper.assertValueEqual(golem.getBbHeight(), 5.4F, "standing hitbox height"))
                .thenWaitUntil(() -> helper.assertTrue(golem.getTarget() == attacker, "attacker must become the target"))
                .thenSucceed();
    }

    public static void wakesWhenStoneMinedNearby(GameTestHelper helper) {
        StoneGolem near = spawnGolem(helper, new BlockPos(2, 1, 2));
        StoneGolem far = spawnGolem(helper, new BlockPos(8, 1, 8));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos mined = helper.absolutePos(new BlockPos(1, 0, 1));

        StoneGolemEvents.onBlockBroken(helper.getLevel(), player, mined, Blocks.STONE.defaultBlockState());
        assertState(helper, near, GolemState.AWAKENING);
        assertState(helper, far, GolemState.DORMANT);

        // Non-stone blocks do not count.
        StoneGolem other = spawnGolem(helper, new BlockPos(6, 1, 2));
        StoneGolemEvents.onBlockBroken(helper.getLevel(), player, helper.absolutePos(new BlockPos(6, 0, 1)), Blocks.OAK_PLANKS.defaultBlockState());
        assertState(helper, other, GolemState.DORMANT);
        helper.succeed();
    }

    public static void pickaxeBeatsSword(GameTestHelper helper) {
        float pickaxeDamage = damageWith(helper, new BlockPos(2, 1, 4), new ItemStack(Items.IRON_PICKAXE));
        float swordDamage = damageWith(helper, new BlockPos(6, 1, 4), new ItemStack(Items.IRON_SWORD));
        helper.assertTrue(pickaxeDamage > swordDamage,
                "pickaxe (" + pickaxeDamage + ") must hurt more than sword (" + swordDamage + ")");
        helper.assertTrue(pickaxeDamage >= swordDamage * 3.0F, "pickaxe should deal about 4x the sword damage (2.0 vs 0.5)");
        helper.succeed();
    }

    private static float damageWith(GameTestHelper helper, BlockPos pos, ItemStack weapon) {
        StoneGolem golem = spawnGolem(helper, pos);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, weapon);
        float before = golem.getHealth();
        golem.hurtServer(helper.getLevel(), helper.getLevel().damageSources().playerAttack(player), TEST_HIT);
        return before - golem.getHealth();
    }

    public static void projectilesAreWeak(GameTestHelper helper) {
        DamageSource arrow = new DamageSource(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.ARROW));
        DamageSource generic = helper.getLevel().damageSources().generic();
        helper.assertValueEqual(StoneGolem.getDamageMultiplier(arrow), StoneGolemBalance.INEFFECTIVE_DAMAGE_TYPE_MULTIPLIER, "arrow multiplier");
        helper.assertValueEqual(StoneGolem.getDamageMultiplier(generic), 1.0F, "generic multiplier");
        helper.succeed();
    }

    public static void stompHitsAndStartsCooldown(GameTestHelper helper) {
        StoneGolem golem = spawnGolem(helper, CENTER);
        Zombie victim = helper.spawn(EntityTypes.HUSK, CENTER.offset(2, 0, 0));
        victim.setNoAi(true);
        float victimHealth = victim.getHealth();

        helper.startSequence()
                .thenIdle(1)
                .thenExecute(() -> golem.hurtServer(helper.getLevel(), helper.getLevel().damageSources().mobAttack(victim), 1.0F))
                .thenWaitUntil(() -> assertState(helper, golem, GolemState.STOMPING))
                .thenWaitUntil(() -> helper.assertTrue(golem.getStompCooldown() > 0, "stomp must start the cooldown"))
                .thenExecute(() -> helper.assertTrue(victim.getHealth() < victimHealth, "stomp must hurt nearby entities"))
                .thenWaitUntil(() -> assertState(helper, golem, GolemState.ACTIVE))
                // While the cooldown runs, the golem must not stomp again.
                .thenExecuteFor(40, () -> helper.assertFalse(golem.getState() == GolemState.STOMPING, "stomp while on cooldown"))
                .thenSucceed();
    }

    public static void slamHitsAtImpact(GameTestHelper helper) {
        StoneGolem golem = spawnGolem(helper, CENTER);
        Zombie target = helper.spawn(EntityTypes.HUSK, CENTER.offset(0, 0, 2));
        target.setNoAi(true);
        float[] health = new float[1];
        helper.startSequence()
                .thenIdle(2)
                .thenExecute(() -> {
                    health[0] = target.getHealth();
                    helper.assertTrue(golem.doHurtTarget(helper.getLevel(), target), "slam must start");
                    helper.assertValueEqual(target.getHealth(), health[0], "health before impact");
                })
                .thenIdle(StoneGolemBalance.SLAM_WINDUP_TICKS - 2)
                .thenExecute(() -> helper.assertValueEqual(target.getHealth(), health[0], "health just before impact"))
                .thenWaitUntil(() -> helper.assertTrue(target.getHealth() <= health[0] - 6.0F,
                        "slam must deal about 8 damage at impact, health " + health[0] + " -> " + target.getHealth()))
                .thenSucceed();
    }

    public static void rockSummonLaunchesTarget(GameTestHelper helper) {
        StoneGolem golem = spawnGolem(helper, new BlockPos(2, 1, 2));
        // An armor stand: has physics but no AI, so it stays on the warned spot.
        ArmorStand target = helper.spawn(EntityTypes.ARMOR_STAND, new BlockPos(12, 1, 12));
        double[] startY = new double[1];
        double[] maxY = new double[1];
        golem.wakeUp();
        helper.startSequence()
                .thenWaitUntil(() -> assertState(helper, golem, GolemState.ACTIVE))
                .thenExecute(() -> {
                    golem.setTarget(target);
                    startY[0] = target.getY();
                    maxY[0] = target.getY();
                })
                .thenWaitUntil(() -> assertState(helper, golem, GolemState.SUMMONING))
                .thenWaitUntil(() -> {
                    maxY[0] = Math.max(maxY[0], target.getY());
                    helper.assertTrue(target.getY() < maxY[0] - 0.5, "waiting for the target to fall again");
                })
                .thenExecute(() -> {
                    helper.assertTrue(maxY[0] - startY[0] >= 6.5 && maxY[0] - startY[0] <= 9.5,
                            "rock must throw the target about 8 blocks up, got " + (maxY[0] - startY[0]));
                    helper.assertTrue(golem.getSummonCooldown() > 0, "summon must start its cooldown");
                })
                .thenWaitUntil(() -> helper.assertTrue(helper.getLevel().getEntitiesOfClass(RisingRock.class,
                        golem.getBoundingBox().inflate(32.0)).isEmpty(), "rocks must sink away"))
                .thenSucceed();
    }

    public static void lootTableDropsCobblestone(GameTestHelper helper) {
        StoneGolem golem = spawnGolem(helper, CENTER);
        golem.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), Float.MAX_VALUE);
        helper.succeedWhen(() -> helper.assertItemEntityPresent(Items.COBBLESTONE, CENTER, 3.0));
    }

    /**
     * Verifies the loader-specific biome modifiers through the same lookup natural spawning uses: the overworld test
     * biome must offer the golem as a monster with the configured weight and group size.
     */
    public static void biomeSpawnListsContainGolem(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(CENTER);
        boolean listed = level.getChunkSource().getGenerator()
                .getMobsAt(level, level.structureManager(), MobCategory.MONSTER, pos)
                .unwrap().stream()
                .anyMatch(entry -> entry.value().type() == ModEntities.STONE_GOLEM.get()
                        && entry.weight() == StoneGolemBalance.spawnWeight()
                        && entry.value().count().maxInclusive() == StoneGolemBalance.SPAWN_MAX_GROUP);
        helper.assertTrue(listed, "monster spawn list at " + pos + " must contain the stone golem");
        helper.succeed();
    }

    public static void spawnRulesRequireStoneAndCave(GameTestHelper helper) {
        var level = helper.getLevel();
        // Work in absolute coordinates: find the first air block above the arena's stone floor.
        BlockPos pos = helper.absolutePos(CENTER);
        while (level.getBlockState(pos.below()).isAir()) {
            pos = pos.below();
        }
        EntityType<StoneGolem> type = ModEntities.STONE_GOLEM.get();
        helper.assertTrue(pos.getY() < StoneGolemBalance.UNDERGROUND_MAX_Y, "test arena must be below the cave limit");

        // Open sky: remove the framework's barrier ceiling above the column, then only surface biomes allow spawns.
        for (int dy = 1; dy <= 8; dy++) {
            if (level.getBlockState(pos.above(dy)).is(Blocks.BARRIER)) {
                level.setBlockAndUpdate(pos.above(dy), Blocks.AIR.defaultBlockState());
            }
        }
        boolean surfaceBiome = level.getBiome(pos).is(ModTags.Biomes.STONE_GOLEM_SURFACE_SPAWNS);
        helper.assertValueEqual(StoneGolem.checkStoneGolemSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, level.getRandom()),
                surfaceBiome, "spawn allowed under open sky");

        // Roofed over (a cave): allowed on stone.
        level.setBlockAndUpdate(pos.above(3), Blocks.STONE.defaultBlockState());
        helper.assertTrue(StoneGolem.checkStoneGolemSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, level.getRandom()),
                "spawn must be allowed in a covered stone cave");

        // Not on dirt.
        level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        helper.assertFalse(StoneGolem.checkStoneGolemSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, level.getRandom()),
                "spawn must require stone below");
        helper.succeed();
    }
}
