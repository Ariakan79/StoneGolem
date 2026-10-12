package eu.ariakan.stonegolem.gametest;

import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.function.Consumer;

/** Registers the common game tests with NeoForge. */
public final class StoneGolemNeoForgeGameTests {

    private StoneGolemNeoForgeGameTests() {
    }

    public static void register(IEventBus modBus) {
        StoneGolemGameTests.TESTS.forEach((name, test) -> Services.REGISTRAR.register(Registries.TEST_FUNCTION, name, test::function));
        modBus.addListener(StoneGolemNeoForgeGameTests::onRegisterGameTests);
    }

    private static void onRegisterGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(Constants.id("default"));
        StoneGolemGameTests.TESTS.forEach((name, test) -> {
            TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(environment, Level.OVERWORLD, test.structure(),
                    StoneGolemGameTests.MAX_TICKS, 0, true, Rotation.NONE, false, 1, 1, test.skyAccess(), 0);
            ResourceKey<Consumer<GameTestHelper>> function = ResourceKey.create(Registries.TEST_FUNCTION, Constants.id(name));
            event.registerTest(Constants.id(name), new FunctionGameTestInstance(function, data));
        });
    }
}
