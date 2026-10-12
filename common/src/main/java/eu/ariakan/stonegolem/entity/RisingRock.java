package eu.ariakan.stonegolem.entity;

import eu.ariakan.stonegolem.config.StoneGolemBalance;
import eu.ariakan.stonegolem.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;

/**
 * A large rock summoned by a {@link StoneGolem}. It bursts out of the ground, throws everything standing on it high
 * into the air, then slowly sinks back into the ground so the victims fall down.
 * <p>
 * Purely visual apart from the launch: it has no collision. The client derives the rise/sink animation from the
 * entity's age, which starts in sync with the server because the entity spawns already erupting.
 */
public class RisingRock extends Entity {

    private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(RisingRock.class, EntityDataSerializers.FLOAT);
    /** Height of the full-size rock in blocks (38 px model at render scale 1.5). */
    public static final float BASE_HEIGHT = 3.5F;
    private static final float BASE_RADIUS = 0.75F;

    private final Set<Integer> launched = new HashSet<>();

    public RisingRock(EntityType<? extends RisingRock> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setScale(float scale) {
        this.entityData.set(DATA_SCALE, scale);
    }

    public float getScale() {
        return this.entityData.get(DATA_SCALE);
    }

    public static int lifetime() {
        return StoneGolemBalance.ROCK_RISE_TICKS + StoneGolemBalance.ROCK_HOLD_TICKS + StoneGolemBalance.ROCK_SINK_TICKS;
    }

    /**
     * How far the rock sticks out of the ground, from 0 (buried) to 1 (fully out).
     */
    public float getEmergence(float partialTicks) {
        float age = this.tickCount + partialTicks;
        int rise = StoneGolemBalance.ROCK_RISE_TICKS;
        int sinkStart = rise + StoneGolemBalance.ROCK_HOLD_TICKS;
        if (age < rise) {
            return Mth.clamp(age / rise, 0.0F, 1.0F);
        }
        if (age < sinkStart) {
            return 1.0F;
        }
        float sink = (age - sinkStart) / StoneGolemBalance.ROCK_SINK_TICKS;
        return 1.0F - Mth.clamp(sink * sink, 0.0F, 1.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SCALE, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        if (this.tickCount == 1) {
            this.playSound(ModSounds.ROCK_ERUPT.get(), 1.5F, 1.2F - this.getScale() * 0.3F);
            this.burstParticles(level, 40);
        }
        if (this.tickCount <= StoneGolemBalance.ROCK_RISE_TICKS) {
            this.launchEntities(level);
        } else if (this.tickCount > StoneGolemBalance.ROCK_RISE_TICKS + StoneGolemBalance.ROCK_HOLD_TICKS && this.tickCount % 4 == 0) {
            this.burstParticles(level, 4);
        }
        if (this.tickCount >= lifetime()) {
            this.discard();
        }
    }

    private void launchEntities(ServerLevel level) {
        float radius = BASE_RADIUS * this.getScale();
        AABB area = new AABB(this.getX() - radius, this.getY() - 0.5, this.getZ() - radius,
                this.getX() + radius, this.getY() + BASE_HEIGHT * this.getScale(), this.getZ() + radius);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, this::canLaunch)) {
            if (this.launched.add(entity.getId())) {
                // An eruption, not knockback: knockback resistance does not reduce it.
                double velocity = entity instanceof Player ? StoneGolemBalance.ROCK_LAUNCH_VELOCITY_PLAYER : StoneGolemBalance.ROCK_LAUNCH_VELOCITY;
                entity.setDeltaMovement(entity.getDeltaMovement().x * 0.2, velocity, entity.getDeltaMovement().z * 0.2);
                entity.needsSync = true;
                // Players simulate their own movement; only syncVelocity sends the new motion to the player itself.
                entity.syncVelocity = true;
                entity.resetFallDistance();
            }
        }
    }

    private boolean canLaunch(LivingEntity entity) {
        if (!entity.isAlive() || entity instanceof StoneGolem || entity.isSpectator()) {
            return false;
        }
        return !(entity instanceof Player player) || !player.getAbilities().flying;
    }

    private void burstParticles(ServerLevel level, int count) {
        BlockState ground = level.getBlockState(BlockPos.containing(this.getX(), this.getY() - 0.5, this.getZ()));
        if (ground.isAir()) {
            ground = Blocks.STONE.defaultBlockState();
        }
        float radius = BASE_RADIUS * this.getScale();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), this.getX(), this.getY() + 0.1, this.getZ(),
                count, radius, 0.1, radius, 0.15);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        // Transient effect entity (EntityType is noSave), nothing to persist.
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        // Transient effect entity (EntityType is noSave), nothing to persist.
    }
}
