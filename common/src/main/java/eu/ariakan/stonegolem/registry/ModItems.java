package eu.ariakan.stonegolem.registry;

import eu.ariakan.stonegolem.Constants;
import eu.ariakan.stonegolem.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ModItems {

    public static final Supplier<Item> STONE_GOLEM_SPAWN_EGG = register("stone_golem_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.STONE_GOLEM.get())));

    /** Rare drop without a function yet; it becomes the heart of the buildable golem in phase 2. */
    public static final Supplier<Item> GOLEM_CORE = register("golem_core",
            properties -> new Item(properties.stacksTo(16).rarity(Rarity.UNCOMMON).component(DataComponents.LORE, new ItemLore(List.of(
                    Component.translatable("item.stonegolem.golem_core.tooltip").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))))));

    private ModItems() {
    }

    private static Supplier<Item> register(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Constants.id(name));
        return Services.REGISTRAR.register(Registries.ITEM, name, () -> factory.apply(new Item.Properties().setId(key)));
    }

    /** Forces class loading so the static registrations above run. */
    public static void init() {
    }
}
