package eu.ariakan.stonegolem.client.gametest;

import eu.ariakan.stonegolem.config.StoneGolemBalance;
import eu.ariakan.stonegolem.entity.GolemState;
import eu.ariakan.stonegolem.entity.StoneGolem;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.MinecraftServer;

import java.util.List;

/**
 * Client smoke test: summon and spawn-egg spawning, rendering in every main state (screenshots) and client/server
 * state agreement. Run with {@code ./gradlew :fabric:runClientGametest}; screenshots land in
 * {@code fabric/runs/clientGametest/screenshots}.
 */
public class StoneGolemClientGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        if (System.getProperty("stonegolem.showcase") != null) {
            return; // showcase run: only StoneGolemShowcase
        }
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            // Survive slams and falls during the test; launches still apply.
            world.getServer().runCommand("effect give @p minecraft:resistance infinite 4 true");
            world.getServer().runCommand("execute as @p at @p run summon stonegolem:stone_golem ^ ^ ^6");
            context.waitTicks(5);
            lookAtGolem(context);
            context.waitTicks(40);
            assertStates(context, world, GolemState.DORMANT);
            context.takeScreenshot("stone_golem_1_dormant");

            // The player's hit wakes the golem (and makes the player its target; the test world is survival).
            world.getServer().runCommand("damage @e[type=stonegolem:stone_golem,limit=1] 1 minecraft:player_attack by @p");
            context.waitTicks(12);
            assertStates(context, world, GolemState.AWAKENING);
            context.takeScreenshot("stone_golem_2_awakening");

            // Once active, the distant player gets the rock summon: track the real client player's height every tick.
            double startY = context.computeOnClient(mc -> mc.player.getY());
            double maxY = startY;
            int summonSeenAt = -1;
            for (int tick = 0; tick < 160; tick++) {
                context.waitTick();
                maxY = Math.max(maxY, context.computeOnClient(mc -> mc.player.getY()));
                GolemState state = world.getServer().computeOnServer(server -> serverGolems(server).getFirst().getState());
                if (summonSeenAt < 0 && state == GolemState.SUMMONING) {
                    summonSeenAt = tick;
                }
                if (summonSeenAt >= 0 && tick == summonSeenAt + StoneGolemBalance.SUMMON_WINDUP_TICKS + 3) {
                    context.takeScreenshot("stone_golem_3_rocks");
                }
                if (summonSeenAt >= 0 && tick > summonSeenAt + 70) {
                    break;
                }
            }
            if (summonSeenAt < 0) {
                throw new AssertionError("golem never summoned rocks against the distant player");
            }
            if (maxY - startY < 7.3 || maxY - startY > 8.7) {
                throw new AssertionError("player should be thrown about 8 blocks up, rose " + (maxY - startY));
            }
            System.out.println("STONEGOLEM player launched " + (maxY - startY) + " blocks");
            lookAtGolem(context);
            context.waitTicks(5);
            context.takeScreenshot("stone_golem_4_after_rocks");

            // Spawn egg: remove the (now close) golem so the click hits the ground, then use the egg.
            world.getServer().runCommand("kill @e[type=stonegolem:stone_golem]");
            context.waitTicks(25);
            int before = countGolems(world);
            world.getServer().runCommand("item replace entity @p weapon.mainhand with stonegolem:stone_golem_spawn_egg");
            context.runOnClient(mc -> mc.player.setXRot(60.0F));
            context.waitTicks(5);
            context.getInput().pressKey(options -> options.keyUse);
            context.waitTicks(20);
            int after = countGolems(world);
            if (after != before + 1) {
                throw new AssertionError("spawn egg should spawn one golem, had " + before + ", now " + after);
            }
            context.takeScreenshot("stone_golem_5_spawn_egg");
        }
    }

    private static void lookAtGolem(ClientGameTestContext context) {
        context.runOnClient(mc -> {
            StoneGolem golem = clientGolems(mc).getFirst();
            mc.player.lookAt(EntityAnchorArgument.Anchor.EYES, golem.position().add(0.0, 0.9, 0.0));
        });
    }

    private static List<StoneGolem> clientGolems(Minecraft mc) {
        return mc.level.getEntitiesOfClass(StoneGolem.class, mc.player.getBoundingBox().inflate(32.0));
    }

    private static int countGolems(TestSingleplayerContext world) {
        return world.getServer().computeOnServer(server -> serverGolems(server).size());
    }

    private static List<StoneGolem> serverGolems(MinecraftServer server) {
        var player = server.getPlayerList().getPlayers().getFirst();
        return player.level().getEntitiesOfClass(StoneGolem.class, player.getBoundingBox().inflate(32.0));
    }

    /** Server and client must agree on the synced state (no desync). */
    private static void assertStates(ClientGameTestContext context, TestSingleplayerContext world, GolemState expected) {
        GolemState server = world.getServer().computeOnServer(s -> serverGolems(s).getFirst().getState());
        GolemState client = context.computeOnClient(mc -> clientGolems(mc).getFirst().getState());
        if (server != expected || client != expected) {
            throw new AssertionError("expected " + expected + " but server=" + server + ", client=" + client);
        }
    }
}
