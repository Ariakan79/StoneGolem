package eu.ariakan.stonegolem.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Registers the common game tests with Fabric's {@code fabric-gametest} entrypoint. Each method only delegates to
 * {@link StoneGolemGameTests}; keep this list in sync with {@link StoneGolemGameTests#TESTS}.
 */
public class StoneGolemFabricGameTests {

    private static final String ARENA = "stonegolem:arena";

    @GameTest(structure = ARENA, maxTicks = StoneGolemGameTests.MAX_TICKS)
    public void spawnsDormant(GameTestHelper helper) {
        StoneGolemGameTests.spawnsDormant(helper);
    }

    @GameTest(structure = ARENA, maxTicks = StoneGolemGameTests.MAX_TICKS)
    public void wakesWhenAttacked(GameTestHelper helper) {
        StoneGolemGameTests.wakesWhenAttacked(helper);
    }

    @GameTest(structure = ARENA, maxTicks = StoneGolemGameTests.MAX_TICKS)
    public void wakesWhenStoneMinedNearby(GameTestHelper helper) {
        StoneGolemGameTests.wakesWhenStoneMinedNearby(helper);
    }

    @GameTest(structure = ARENA, maxTicks = StoneGolemGameTests.MAX_TICKS)
    public void pickaxeBeatsSword(GameTestHelper helper) {
        StoneGolemGameTests.pickaxeBeatsSword(helper);
    }

    @GameTest(structure = ARENA, maxTicks = StoneGolemGameTests.MAX_TICKS)
    public void projectilesAreWeak(GameTestHelper helper) {
        StoneGolemGameTests.projectilesAreWeak(helper);
    }

    @GameTest(structure = ARENA, maxTicks = StoneGolemGameTests.MAX_TICKS)
    public void stompHitsAndStartsCooldown(GameTestHelper helper) {
        StoneGolemGameTests.stompHitsAndStartsCooldown(helper);
    }

    @GameTest(structure = ARENA, maxTicks = StoneGolemGameTests.MAX_TICKS)
    public void lootTableDropsCobblestone(GameTestHelper helper) {
        StoneGolemGameTests.lootTableDropsCobblestone(helper);
    }

    @GameTest(structure = ARENA, maxTicks = StoneGolemGameTests.MAX_TICKS)
    public void biomeSpawnListsContainGolem(GameTestHelper helper) {
        StoneGolemGameTests.biomeSpawnListsContainGolem(helper);
    }

    @GameTest(structure = ARENA, maxTicks = StoneGolemGameTests.MAX_TICKS)
    public void spawnRulesRequireStoneAndCave(GameTestHelper helper) {
        StoneGolemGameTests.spawnRulesRequireStoneAndCave(helper);
    }
}
