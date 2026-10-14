package eu.ariakan.stonegolem.entity;

import eu.ariakan.stonegolem.registry.ModEntities;
import eu.ariakan.stonegolem.registry.ModTags;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import org.jspecify.annotations.Nullable;

/**
 * Building a Stone Golem, like an iron golem: a T of stone (two stacked for the body, one on each side as arms) with a
 * block of diamond placed on top last. The blocks are data-driven: {@code #stonegolem:stone_golem_body} and
 * {@code #stonegolem:stone_golem_head}.
 *
 * <pre>
 *   ~ D ~      D = head (block of diamond)
 *   S S S      S = body and arms (stone)
 *   ~ S ~      ~ = free: air or anything without collision (grass, flowers, saplings)
 * </pre>
 */
public final class StoneGolemBuilding {

    private static @Nullable BlockPattern pattern;

    private StoneGolemBuilding() {
    }

    private static BlockPattern pattern() {
        if (pattern == null) {
            pattern = BlockPatternBuilder.start()
                    .aisle("~^~", "###", "~#~")
                    .where('^', BlockInWorld.hasState(state -> state.is(ModTags.Blocks.STONE_GOLEM_HEAD)))
                    .where('#', BlockInWorld.hasState(state -> state.is(ModTags.Blocks.STONE_GOLEM_BODY)))
                    // Free space next to the leg: anything you can walk through (grass, flowers, saplings, thin snow)
                    // counts as free, unlike vanilla golems; fluids do not.
                    .where('~', block -> block.getState().getFluidState().isEmpty()
                            && block.getState().getCollisionShape(block.getLevel(), block.getPos()).isEmpty())
                    .build();
        }
        return pattern;
    }

    /** Called by the loaders after a block was placed. Spawns a guardian golem if this completed the pattern. */
    public static void onBlockPlaced(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel && level.getBlockState(pos).is(ModTags.Blocks.STONE_GOLEM_HEAD)) {
            tryBuild(serverLevel, pos);
        }
    }

    /**
     * @param headPos position of the head block
     * @return the built golem, or null if the blocks around {@code headPos} do not form the pattern
     */
    public static @Nullable StoneGolem tryBuild(ServerLevel level, BlockPos headPos) {
        BlockPattern.BlockPatternMatch match = pattern().find(level, headPos);
        if (match == null) {
            return null;
        }
        StoneGolem golem = ModEntities.STONE_GOLEM.get().create(level, EntitySpawnReason.TRIGGERED);
        if (golem == null) {
            return null;
        }
        BlockPos feet = match.getBlock(1, 2, 0).getPos();
        CarvedPumpkinBlock.clearPatternBlocks(level, match);
        golem.setPlayerCreated(true);
        golem.snapTo(feet.getX() + 0.5, feet.getY() + 0.05, feet.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(golem);
        golem.wakeUp();
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, golem.getBoundingBox().inflate(5.0))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(player, golem);
        }
        CarvedPumpkinBlock.updatePatternBlocks(level, match);
        return golem;
    }
}
