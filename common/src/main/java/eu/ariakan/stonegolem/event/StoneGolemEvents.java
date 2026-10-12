package eu.ariakan.stonegolem.event;

import eu.ariakan.stonegolem.config.StoneGolemBalance;
import eu.ariakan.stonegolem.entity.StoneGolem;
import eu.ariakan.stonegolem.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Loader-independent game event handlers. The loader modules only forward their native events to these methods.
 */
public final class StoneGolemEvents {

    private StoneGolemEvents() {
    }

    /** Called on the logical server after a player broke a block. */
    public static void onBlockBroken(Level level, Player player, BlockPos pos, BlockState state) {
        if (level.isClientSide() || !state.is(ModTags.Blocks.WAKES_STONE_GOLEM)) {
            return;
        }
        double radius = StoneGolemBalance.WAKE_RADIUS;
        Vec3 center = Vec3.atCenterOf(pos);
        for (StoneGolem golem : level.getEntitiesOfClass(StoneGolem.class, new AABB(pos).inflate(radius),
                golem -> golem.isDormant() && golem.distanceToSqr(center) <= radius * radius)) {
            golem.wakeUp();
        }
    }
}
