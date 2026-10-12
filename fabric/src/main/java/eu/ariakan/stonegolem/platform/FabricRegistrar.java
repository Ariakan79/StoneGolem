package eu.ariakan.stonegolem.platform;

import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.platform.services.IRegistrar;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/** Fabric registers immediately while the mod initializes. */
public class FabricRegistrar implements IRegistrar {

    @Override
    @SuppressWarnings("unchecked")
    public <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registryKey, String name, Supplier<T> factory) {
        Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.getValueOrThrow((ResourceKey) registryKey);
        T value = Registry.register(registry, Constants.id(name), factory.get());
        return () -> value;
    }
}
