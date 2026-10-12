package eu.ariakan.stonegolem.entity;

import eu.ariakan.stonegolem.config.StoneGolemBalance;
import eu.ariakan.stonegolem.registry.ModSounds;
import eu.ariakan.stonegolem.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An ancient, sluggish guardian of stone. Sleeps as a mossy boulder until a player hits it or mines stone close by,
 * then hunts its attacker with heavy punches and a telegraphed ground stomp, and finally settles down again.
 */
public class StoneGolem extends PathfinderMob {

    private static final EntityDataAccessor<Byte> DATA_STATE = SynchedEntityData.defineId(StoneGolem.class, EntityDataSerializers.BYTE);
    private static final Goal.Flag[] AI_FLAGS = {Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.TARGET};

    // Client-side animation states, driven by the synchronized golem state (see setupAnimationStates).
    public final AnimationState dormantAnimationState = new AnimationState();
    public final AnimationState awakenAnimationState = new AnimationState();
    public final AnimationState settleAnimationState = new AnimationState();
    public final AnimationState stompAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();

    // Server-side bookkeeping.
    private int stateTicks;
    private int ticksWithoutTarget;
    private int stompCooldown;

    public StoneGolem(EntityType<? extends StoneGolem> type, Level level) {
        super(type, level);
        this.xpReward = StoneGolemBalance.EXPERIENCE;
        this.applyStateToAi(GolemState.DORMANT);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, StoneGolemBalance.MAX_HEALTH)
                .add(Attributes.ARMOR, StoneGolemBalance.ARMOR)
                .add(Attributes.ATTACK_DAMAGE, StoneGolemBalance.ATTACK_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK, StoneGolemBalance.ATTACK_KNOCKBACK)
                .add(Attributes.MOVEMENT_SPEED, StoneGolemBalance.MOVEMENT_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, StoneGolemBalance.KNOCKBACK_RESISTANCE)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE, StoneGolemBalance.KNOCKBACK_RESISTANCE)
                .add(Attributes.FOLLOW_RANGE, StoneGolemBalance.FOLLOW_RANGE)
                .add(Attributes.STEP_HEIGHT, StoneGolemBalance.STEP_HEIGHT)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.0);
    }

    /**
     * Natural spawn rule: needs stone underfoot, and either a dark cave below {@link StoneGolemBalance#UNDERGROUND_MAX_Y}
     * or a biome from {@code #stonegolem:stone_golem_surface_spawns}.
     */
    public static boolean checkStoneGolemSpawnRules(EntityType<StoneGolem> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos.below()).is(ModTags.Blocks.STONE_GOLEM_SPAWNABLE_ON)) {
            return false;
        }
        if (pos.getY() < StoneGolemBalance.UNDERGROUND_MAX_Y && !level.canSeeSky(pos)) {
            return true;
        }
        return level.getBiome(pos).is(ModTags.Biomes.STONE_GOLEM_SURFACE_SPAWNS);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, (byte) GolemState.DORMANT.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        // No "nearest attackable target" goal: the golem is neutral and only retaliates.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // --- State machine ------------------------------------------------------------------------------------------

    public GolemState getState() {
        return GolemState.byId(this.entityData.get(DATA_STATE));
    }

    /** Switches state with the matching sound and game event. */
    private void setState(GolemState state) {
        this.applyState(state);
        switch (state) {
            case AWAKENING -> this.playSound(ModSounds.STONE_GOLEM_AWAKEN.get(), 1.5F, 1.0F);
            case SETTLING -> this.playSound(ModSounds.STONE_GOLEM_SETTLE.get(), 1.0F, 1.0F);
            case STOMPING -> this.playSound(ModSounds.STONE_GOLEM_STOMP_WINDUP.get(), 1.5F, 1.0F);
            default -> {
            }
        }
        this.gameEvent(GameEvent.ENTITY_ACTION);
    }

    /** Switches state silently, e.g. when loading from disk. */
    private void applyState(GolemState state) {
        this.entityData.set(DATA_STATE, (byte) state.ordinal());
        this.stateTicks = 0;
        this.applyStateToAi(state);
        if (!state.isAwake()) {
            this.setTarget(null);
            this.getNavigation().stop();
        }
    }

    /** Goals only run while the golem is active; disabling their control flags also stops running goals. */
    private void applyStateToAi(GolemState state) {
        if (state == GolemState.ACTIVE) {
            super.updateControlFlags();
            this.targetSelector.enableControlFlag(Goal.Flag.TARGET);
            return;
        }
        for (Goal.Flag flag : AI_FLAGS) {
            this.goalSelector.disableControlFlag(flag);
        }
        // Keep the current target while stomping, drop targeting otherwise.
        if (state != GolemState.STOMPING) {
            this.targetSelector.disableControlFlag(Goal.Flag.TARGET);
        }
    }

    /** Vanilla re-enables the movement flags every few ticks; keep them disabled unless the golem is active. */
    @Override
    protected void updateControlFlags() {
        this.applyStateToAi(this.getState());
    }

    public boolean isDormant() {
        return this.getState() == GolemState.DORMANT;
    }

    /**
     * Wakes the golem if it is dormant or settling down.
     *
     * @return whether the golem started waking up
     */
    public boolean wakeUp() {
        GolemState state = this.getState();
        if (state == GolemState.DORMANT || state == GolemState.SETTLING) {
            this.setState(GolemState.AWAKENING);
            return true;
        }
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.stateTicks++;
        if (this.stompCooldown > 0) {
            this.stompCooldown--;
        }

        switch (this.getState()) {
            case DORMANT -> this.getNavigation().stop();
            case AWAKENING -> {
                if (this.stateTicks >= StoneGolemBalance.AWAKEN_TICKS) {
                    this.ticksWithoutTarget = 0;
                    this.setState(GolemState.ACTIVE);
                }
            }
            case ACTIVE -> this.tickActive();
            case STOMPING -> this.tickStomp(level);
            case SETTLING -> {
                if (this.stateTicks >= StoneGolemBalance.SETTLE_TICKS) {
                    this.setState(GolemState.DORMANT);
                }
            }
        }
    }

    private void tickActive() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            if (++this.ticksWithoutTarget >= StoneGolemBalance.RETURN_TO_DORMANT_TICKS) {
                this.setState(GolemState.SETTLING);
            }
            return;
        }
        this.ticksWithoutTarget = 0;
        double triggerRange = StoneGolemBalance.STOMP_TRIGGER_RANGE + target.getBbWidth() / 2.0;
        if (this.stompCooldown <= 0 && this.onGround() && this.distanceToSqr(target) <= triggerRange * triggerRange) {
            this.setState(GolemState.STOMPING);
        }
    }

    private void tickStomp(ServerLevel level) {
        this.getNavigation().stop();
        LivingEntity target = this.getTarget();
        if (target != null && this.stateTicks < StoneGolemBalance.STOMP_WINDUP_TICKS) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
        if (this.stateTicks == StoneGolemBalance.STOMP_WINDUP_TICKS) {
            this.performStomp(level);
        } else if (this.stateTicks >= StoneGolemBalance.STOMP_WINDUP_TICKS + StoneGolemBalance.STOMP_RECOVERY_TICKS) {
            this.setState(GolemState.ACTIVE);
        }
    }

    private void performStomp(ServerLevel level) {
        this.stompCooldown = StoneGolemBalance.STOMP_COOLDOWN_TICKS;
        double radius = StoneGolemBalance.STOMP_RADIUS;
        AABB area = this.getBoundingBox().inflate(radius, 1.0, radius);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, area, this::isStompVictim);
        for (LivingEntity victim : victims) {
            Vec3 offset = victim.position().subtract(this.position());
            double reach = radius + this.getBbWidth() / 2.0 + victim.getBbWidth() / 2.0;
            if (offset.horizontalDistanceSqr() > reach * reach) {
                continue;
            }
            if (victim.hurtServer(level, this.damageSources().mobAttack(this), StoneGolemBalance.STOMP_DAMAGE)) {
                double scale = Math.max(0.0, 1.0 - victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                Vec3 outward = offset.horizontalDistanceSqr() > 1.0E-4 ? new Vec3(offset.x, 0.0, offset.z).normalize() : Vec3.ZERO;
                victim.push(
                        outward.x * StoneGolemBalance.STOMP_OUTWARD_VELOCITY * scale,
                        StoneGolemBalance.STOMP_LAUNCH_VELOCITY * scale,
                        outward.z * StoneGolemBalance.STOMP_OUTWARD_VELOCITY * scale);
            }
        }

        BlockState ground = this.getBlockStateOn();
        if (ground.isAir()) {
            ground = Blocks.STONE.defaultBlockState();
        }
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, ground);
        level.sendParticles(particle, this.getX(), this.getY() + 0.1, this.getZ(), 120, radius / 2.0, 0.05, radius / 2.0, 0.2);
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.2, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        this.playSound(ModSounds.STONE_GOLEM_STOMP.get(), 2.0F, 1.0F);
        this.gameEvent(GameEvent.HIT_GROUND);
    }

    private boolean isStompVictim(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || entity instanceof StoneGolem) {
            return false;
        }
        return !(entity instanceof Player player) || !(player.isCreative() || player.isSpectator());
    }

    public int getStompCooldown() {
        return this.stompCooldown;
    }

    // --- Combat -------------------------------------------------------------------------------------------------

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EntityEvent.START_ATTACKING);
        boolean hurt = super.doHurtTarget(level, target);
        if (hurt && target instanceof LivingEntity living) {
            double scale = Math.max(0.0, 1.0 - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            living.push(0.0, StoneGolemBalance.MELEE_LIFT * scale, 0.0);
        }
        return hurt;
    }

    @Override
    protected void playAttackSound() {
        this.playSound(ModSounds.STONE_GOLEM_ATTACK.get(), 1.0F, 1.0F);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (this.getState() == GolemState.AWAKENING && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        boolean hurt = super.hurtServer(level, source, damage * getDamageMultiplier(source));
        if (hurt && this.isAlive() && source.getEntity() instanceof LivingEntity) {
            this.wakeUp();
        }
        return hurt;
    }

    /**
     * The core gameplay twist: pickaxes crack stone, blades and arrows glance off. Everything is decided by tags so
     * packs and other mods can extend the lists.
     */
    public static float getDamageMultiplier(DamageSource source) {
        ItemStack weapon = source.getWeaponItem();
        if (weapon != null && !weapon.isEmpty() && source.getDirectEntity() == source.getEntity()) {
            if (weapon.is(ModTags.Items.EFFECTIVE_AGAINST_STONE_GOLEM)) {
                return StoneGolemBalance.EFFECTIVE_WEAPON_MULTIPLIER;
            }
            if (weapon.is(ModTags.Items.INEFFECTIVE_AGAINST_STONE_GOLEM)) {
                return StoneGolemBalance.INEFFECTIVE_WEAPON_MULTIPLIER;
            }
        }
        if (source.is(ModTags.DamageTypes.INEFFECTIVE_AGAINST_STONE_GOLEM)) {
            return StoneGolemBalance.INEFFECTIVE_DAMAGE_TYPE_MULTIPLIER;
        }
        return 1.0F;
    }

    // --- Ticking and client animation ---------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();
        if (!this.getState().isAwake()) {
            // A boulder does not turn its head.
            this.setYHeadRot(this.yBodyRot);
            this.setXRot(0.0F);
        }
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        GolemState state = this.getState();
        this.updateAnimation(this.dormantAnimationState, state == GolemState.DORMANT);
        this.updateAnimation(this.awakenAnimationState, state == GolemState.AWAKENING);
        this.updateAnimation(this.settleAnimationState, state == GolemState.SETTLING);
        this.updateAnimation(this.stompAnimationState, state == GolemState.STOMPING);
    }

    private void updateAnimation(AnimationState animation, boolean running) {
        if (running) {
            animation.startIfStopped(this.tickCount);
        } else {
            animation.stop();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EntityEvent.START_ATTACKING) {
            this.attackAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    // --- Sounds -------------------------------------------------------------------------------------------------

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.getState() == GolemState.ACTIVE ? ModSounds.STONE_GOLEM_AMBIENT.get() : null;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.STONE_GOLEM_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return ModSounds.STONE_GOLEM_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.STONE_GOLEM_STEP.get(), 1.0F, 1.0F);
    }

    // --- Persistence --------------------------------------------------------------------------------------------

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("dormant", !this.getState().isAwake() && this.getState() != GolemState.AWAKENING);
        output.putInt("stomp_cooldown", this.stompCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        // Transitional states (waking, stomping, settling) collapse to their stable end state on load.
        this.applyState(input.getBooleanOr("dormant", true) ? GolemState.DORMANT : GolemState.ACTIVE);
        this.stompCooldown = input.getIntOr("stomp_cooldown", 0);
    }
}
