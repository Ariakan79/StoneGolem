package eu.ariakan.stonegolem.client.gametest;

import eu.ariakan.stonegolem.entity.GolemState;
import eu.ariakan.stonegolem.entity.RisingRock;
import eu.ariakan.stonegolem.entity.StoneGolem;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.Heightmap;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Scripted showcase for README / mod-page media, staged on a real plains landscape. Only runs with
 * {@code -Dstonegolem.showcase} ({@code ./gradlew :fabric:runClientShowcase}). Stills (1920x1080) land in
 * {@code fabric/runs/clientShowcase/screenshots}. With {@code -Dstonegolem.showcase.frames=<dir>} every tick of the
 * filmed parts is rendered to {@code <dir>/frame_NNNNN.png} (1280x720, {@value #FPS} fps; slow-motion parts render more
 * frames per tick). Lines prefixed {@code [showcase]} report the first frame of each scene.
 * <p>
 * Note: rendering frames makes the game window flicker; the frames themselves are clean.
 */
public class StoneGolemShowcase implements FabricClientGameTest {

    private static final int FPS = 40;
    private static final int NORMAL = FPS / 20;
    private static final int SLOW_MOTION = NORMAL * 3;

    private final Path frameDir = System.getProperty("stonegolem.showcase.frames") == null ? null
            : Path.of(System.getProperty("stonegolem.showcase.frames"));
    private int frame;
    private TestServerContext server;
    private ClientGameTestContext context;
    /** Runs before every rendered tick, e.g. to move the camera. */
    private Runnable camera = () -> { };
    private int baseX;
    private int cx;
    private int cy;
    private int cz;

    @Override
    public void runTest(ClientGameTestContext context) {
        if (System.getProperty("stonegolem.showcase") == null) {
            return;
        }
        this.context = context;
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            this.server = world.getServer();
            setup();

            // 1: a mossy boulder in the meadow wakes up and calls the first rocks (side view, golem at G, target at T).
            stage("intro", 9);
            fixedCamera(4, 1.4, -4.5, 0, 1.2, 0);
            film(60, NORMAL);
            shot("01_dormant_boulder");
            fixedCamera(10, 2, -4.5, 0, 2, -4.5);
            wake();
            film(16, NORMAL);
            shot("02_awakening");
            filmUntilRocks();
            film(10, NORMAL);
            shot("03_rocks_erupt");
            film(24, NORMAL);

            // 2: slow motion from behind the target, looking past the rocks at the golem.
            stage("rocks_slowmo", 9);
            fixedCamera(2.5, 0.5, -12.5, 0, 2.4, -9);
            wake();
            filmUntilRocks();
            film(14, SLOW_MOTION);
            shot("04_rocks_close_up");
            film(16, NORMAL);

            // 3: over the golem's shoulder.
            stage("rocks_shoulder", 11);
            fixedCamera(1.8, 5.8, 3.5, 0, 1.5, -11);
            wake();
            filmUntilRocks();
            film(8, NORMAL);
            shot("05_rocks_over_shoulder");
            film(10, NORMAL);
            shot("06_target_thrown");
            film(20, NORMAL);

            // 4: the victim's view: thrown eight blocks into the air.
            stage("rocks_victim", 8);
            camera = () -> run("execute as %s at @s anchored eyes run tp @p ^ ^ ^0.4 facing entity %s eyes", target(), golem());
            wake();
            filmUntilRocks();
            film(40, NORMAL);

            // 5: close combat: stomp and ground slams against a small group.
            stage("fight", 3);
            run("summon minecraft:husk %s", at(2.5, 0, -2.5));
            run("summon minecraft:husk %s", at(-2.5, 0, -2.5));
            prepareTargets("@e[type=husk,distance=..8,tag=!" + targetTag() + "]", at(0, 0, 0));
            camera = () -> run("execute at %s run tp @p ~5 ~3.5 ~-5.5 facing entity %s feet", golem(), golem());
            film(1, NORMAL);
            wake();
            film(110, NORMAL);
            shot("07_ground_slam");

            // 6: size comparison.
            stage("size", 30);
            // Standing, not as a boulder: wake it (the far husk is out of its follow range) and keep it in place.
            run("effect give %s minecraft:slowness infinite 255 true", golem());
            wake();
            server.waitFor(s -> golemIn(s, GolemState.ACTIVE), 200);
            context.waitTicks(5);
            run("tp %s %s 180 0", golem(), at(0, 0, 0));
            run("summon minecraft:iron_golem %s", at(3.5, 0, 0));
            run("summon minecraft:villager %s", at(-3, 0, 0.5));
            run("execute positioned %s as @e[type=!player,distance=..6] at @s run tp @s ~ ~ ~ 180 0", at(0, 0, 0));
            run("execute positioned %s run effect give @e[type=!player,distance=..6] minecraft:slowness infinite 255 true", at(0, 0, 0));
            fixedCamera(1.5, 2.6, -10, 1.5, 2.6, 0);
            film(50, NORMAL);
            shot("08_size_comparison");
            film(30, NORMAL);
            scene("end");
        }
    }

    // --- Stage ------------------------------------------------------------------------------------------------------

    private void setup() {
        context.runOnClient(mc -> {
            mc.options.chatVisibility().set(ChatVisiblity.HIDDEN);
            mc.options.tutorialStep = TutorialSteps.NONE;
        });
        for (String rule : new String[] {"send_command_feedback false", "advance_time false", "advance_weather false",
                "spawn_mobs false", "show_death_messages false"}) {
            run("gamerule " + rule);
        }
        run("time set 5000");
        run("weather clear");
        run("difficulty normal");
        run("gamemode spectator @p");

        // Find plains and flatten a small meadow there; the natural landscape around it stays untouched.
        BlockPos plains = server.computeOnServer(s -> {
            var found = s.overworld().findClosestBiome3d(b -> b.is(Biomes.PLAINS), BlockPos.ZERO.atY(64), 6400, 32, 64);
            return found == null ? BlockPos.ZERO : found.getFirst();
        });
        baseX = plains.getX() - 48 * 3;
        cz = plains.getZ();
        run("forceload add %d %d %d %d", baseX, cz - 32, baseX + 48 * 7, cz + 32);
        run("tp @p %d 150 %d", plains.getX(), cz);
        context.waitTicks(160);
        context.getInput().pressKey(290); // F1: hide the HUD
        context.waitTicks(10);
        context.runOnClient(mc -> mc.gui.toastManager().clear());
    }

    private int stage;

    /**
     * Starts a scene at its own spot of the landscape (48 blocks apart, so nothing from earlier scenes can interfere):
     * flattens a meadow there and places a dormant golem at G = (0, 0, 0) and a husk at T = (0, 0, -targetDistance).
     */
    private void stage(String name, double targetDistance) {
        stage++;
        camera = () -> { };
        // Clear the previous scenes so they do not show up on the horizon.
        for (String type : new String[] {"stonegolem:stone_golem", "stonegolem:rising_rock", "husk", "iron_golem", "villager"}) {
            run("tp @e[type=%s] ~ -80 ~", type);
            run("kill @e[type=%s]", type);
        }
        cx = baseX + stage * 48;
        run("tp @p %d 150 %d", cx, cz);
        context.waitTicks(40);
        cy = server.computeOnServer(s -> s.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz));
        run("fill %d %d %d %d %d %d minecraft:air", cx - 14, cy, cz - 18, cx + 14, cy + 14, cz + 8);
        run("fill %d %d %d %d %d %d minecraft:dirt", cx - 14, cy - 3, cz - 18, cx + 14, cy - 2, cz + 8);
        run("fill %d %d %d %d %d %d minecraft:grass_block", cx - 14, cy - 1, cz - 18, cx + 14, cy - 1, cz + 8);
        run("fill %d %d %d %d %d %d minecraft:short_grass replace minecraft:air", cx - 14, cy, cz - 18, cx - 6, cy, cz + 8);
        run("fill %d %d %d %d %d %d minecraft:short_grass replace minecraft:air", cx + 6, cy, cz - 18, cx + 14, cy, cz + 8);
        run("summon stonegolem:stone_golem %s", at(0, 0, 0));
        run("execute positioned %s run tag @e[type=stonegolem:stone_golem,distance=..2] add %s", at(0, 0, 0), golemTag());
        run("tp %s %s 0 0", golem(), at(0, 0, 0));
        run("summon minecraft:husk %s", at(0, 0, -targetDistance));
        run("execute positioned %s run tag @e[type=husk,distance=..2] add %s", at(0, 0, -targetDistance), targetTag());
        prepareTargets(target(), at(0, 0, 0));
        context.waitTicks(10);
        scene(name);
    }

    private String golemTag() {
        return "sg_golem_" + stage;
    }

    private String targetTag() {
        return "sg_target_" + stage;
    }

    private String golem() {
        return "@e[tag=" + golemTag() + ",limit=1]";
    }

    private String target() {
        return "@e[tag=" + targetTag() + ",limit=1]";
    }

    private void prepareTargets(String selector, String position) {
        for (String effect : new String[] {"slowness infinite 255", "resistance infinite 4", "weakness infinite 255"}) {
            run("execute positioned %s run effect give %s minecraft:%s true", position, selector, effect);
        }
    }

    private void wake() {
        run("damage %s 1 minecraft:mob_attack by %s", golem(), target());
    }

    // --- Filming ----------------------------------------------------------------------------------------------------

    private void fixedCamera(double x, double y, double z, double lookX, double lookY, double lookZ) {
        camera = () -> { };
        run("tp @p %s facing %s", at(x, y, z), at(lookX, lookY, lookZ));
    }

    /**
     * Films (at normal speed; the stand-up is fast-forwarded) until the scene's first rock breaks out of the ground,
     * so the eruption itself can be filmed at any speed.
     */
    private void filmUntilRocks() {
        for (int i = 0; i < 300 && !rocksOut(); i++) {
            boolean standingUp = server.computeOnServer(s -> golemIn(s, GolemState.AWAKENING));
            film(1, standingUp && stage > 1 ? 0 : NORMAL);
        }
    }

    private boolean rocksOut() {
        return server.computeOnServer(s -> !s.overworld().getEntities(EntityTypeTest.forClass(RisingRock.class),
                r -> r.isAlive() && Math.abs(r.getX() - (cx + 0.5)) < 8).isEmpty());
    }

    /** Advances the given ticks, rendering {@code framesPerTick} interpolated frames per tick in frame mode. */
    private void film(int ticks, int framesPerTick) {
        for (int i = 0; i < ticks; i++) {
            camera.run();
            if (frameDir != null) {
                for (int k = 0; k < framesPerTick; k++) {
                    context.takeScreenshot(TestScreenshotOptions.of(String.format(Locale.ROOT, "frame_%05d", frame++))
                            .withSize(1280, 720).withDeltaTicks((float) k / framesPerTick)
                            .withDestinationDir(frameDir).disableCounterPrefix());
                }
            }
            context.waitTick();
        }
    }

    private void shot(String name) {
        context.takeScreenshot(TestScreenshotOptions.of(name).withSize(1920, 1080).disableCounterPrefix());
    }

    private void scene(String name) {
        System.out.println("[showcase] scene " + name + " at frame " + frame);
    }

    // --- Helpers ----------------------------------------------------------------------------------------------------

    /** Whether the current scene's golem is in the given state. */
    private boolean golemIn(MinecraftServer s, GolemState state) {
        String tag = golemTag();
        return !s.overworld().getEntities(EntityTypeTest.forClass(StoneGolem.class),
                g -> g.isAlive() && g.getState() == state && g.entityTags().contains(tag)).isEmpty();
    }

    private String at(double dx, double dy, double dz) {
        return String.format(Locale.ROOT, "%.2f %.2f %.2f", cx + 0.5 + dx, cy + dy, cz + 0.5 + dz);
    }

    private void run(String command, Object... args) {
        server.runCommand(args.length == 0 ? command : String.format(Locale.ROOT, command, args));
    }
}
