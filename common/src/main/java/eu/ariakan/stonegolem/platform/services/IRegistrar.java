package eu.ariakan.stonegolem.platform.services;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/**
 * Registers content into vanilla registries in a loader-appropriate way (immediate on Fabric, deferred on NeoForge).
 */
public interface IRegistrar {

    /**
     * @param registry the target registry
     * @param name     the path of the registry entry within the mod namespace
     * @param factory  creates the entry; it is invoked exactly once, possibly later than this call
     * @return a supplier that yields the registered entry once registration has happened
     */
    <R, T extends R> Supplier<T> register(ResourceKey<? extends Registry<R>> registry, String name, Supplier<T> factory);
}
