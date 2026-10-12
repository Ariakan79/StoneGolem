package eu.ariakan.stonegolem.platform;

import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.platform.services.IRegistrar;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/** NeoForge registers through one {@link DeferredRegister} per registry, attached to the mod event bus. */
public class NeoForgeRegistrar implements IRegistrar {

    private static IEventBus modBus;
    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new HashMap<>();

    /** Must be called from the mod constructor before any common registration happens. */
    public static void setModBus(IEventBus bus) {
        modBus = bus;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registryKey, String name, Supplier<T> factory) {
        DeferredRegister<R> register = (DeferredRegister<R>) REGISTERS.computeIfAbsent(registryKey, key -> {
            DeferredRegister<R> created = DeferredRegister.create(registryKey, Constants.MOD_ID);
            created.register(Objects.requireNonNull(modBus, "NeoForgeRegistrar used before the mod bus was set"));
            return created;
        });
        return register.register(name, factory);
    }
}
