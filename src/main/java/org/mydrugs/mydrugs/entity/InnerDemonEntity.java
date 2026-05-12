package org.mydrugs.mydrugs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.BadTripState;
import org.mydrugs.mydrugs.effects.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.effects.addiction.network.BadTripScreamerPayload;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.Optional;
import java.util.UUID;

public class InnerDemonEntity extends Vex {
    private static final int DEFAULT_LIFETIME = 20 * 120;
    private static final int BAD_TRIP_DESPAWN_GRACE = 20 * 25;
    private static final int MELEE_COOLDOWN_TICKS = 32;
    private static final int RANGED_MIN_COOLDOWN_TICKS = 100;
    private static final int RANGED_RANDOM_COOLDOWN_TICKS = 80;
    private static final int RANGED_CHARGE_TICKS = 24;
    private static final double RANGED_RANGE = 15.0D;
    private static final float LASER_DAMAGE = 0.5F;
    private static final float REMAINS_DROP_BASE_CHANCE = 0.125F;
    private static final float REMAINS_DROP_LOOTING_BONUS = 0.05F;
    private static final float REMAINS_DROP_CAP = 0.30F;

    private @Nullable UUID ownerPlayer;
    private int lifetime = DEFAULT_LIFETIME;
    private boolean canDropRemains;
    private boolean boundToBadTrip;
    private int meleeAttackCooldown;
    private int rangedAttackCooldown = RANGED_MIN_COOLDOWN_TICKS;
    private int rangedChargeTicks;
    private int naturalDespawnTicks = -1;

    public InnerDemonEntity(EntityType<? extends Vex> type, Level level) {
        super(type, level);
        this.xpReward = 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FLYING_SPEED, 0.42D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.05D);
    }

    public void configure(@Nullable ServerPlayer owner, boolean droppable, boolean badTripBound) {
        this.ownerPlayer = owner == null ? null : owner.getUUID();
        this.canDropRemains = droppable;
        this.boundToBadTrip = badTripBound;
        this.lifetime = DEFAULT_LIFETIME;
        this.rangedAttackCooldown = RANGED_MIN_COOLDOWN_TICKS + this.random.nextInt(RANGED_RANDOM_COOLDOWN_TICKS + 1);
        if (owner != null) {
            this.setTarget(owner);
            this.setBoundOrigin(owner.blockPosition());
        }
    }

    public boolean isOwnedBy(UUID playerId) {
        return playerId.equals(this.ownerPlayer);
    }

    public boolean canDropRemains() {
        return canDropRemains;
    }

    public void beginNaturalDespawn(int graceTicks) {
        this.canDropRemains = false;
        if (this.naturalDespawnTicks < 0 || graceTicks < this.naturalDespawnTicks) {
            this.naturalDespawnTicks = Math.max(1, graceTicks);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        if (meleeAttackCooldown > 0) meleeAttackCooldown--;
        if (rangedAttackCooldown > 0) rangedAttackCooldown--;

        ServerPlayer owner = resolveOwner();
        if (owner != null && owner.isAlive() && !owner.isSpectator()) {
            this.setTarget(owner);
            this.setBoundOrigin(owner.blockPosition());
            if (boundToBadTrip && !isOwnerStillInBadTrip(owner)) {
                beginNaturalDespawn(BAD_TRIP_DESPAWN_GRACE);
            }
        } else if (boundToBadTrip || --lifetime <= 0) {
            beginNaturalDespawn(20 * 5);
        }

        tickNaturalDespawn();
        tickRangedAttack(owner);

        if (this.tickCount % 12 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new DustParticleOptions(0x240010, 0.8F),
                    this.getX(),
                    this.getY() + 0.35D,
                    this.getZ(),
                    1,
                    0.16D,
                    0.18D,
                    0.16D,
                    0.0D
            );
        }
    }

    private void tickNaturalDespawn() {
        if (naturalDespawnTicks < 0) {
            return;
        }
        naturalDespawnTicks--;
        if (naturalDespawnTicks <= 0) {
            this.discard();
        }
    }

    private void tickRangedAttack(@Nullable ServerPlayer owner) {
        LivingEntity target = owner != null ? owner : this.getTarget();
        if (!(this.level() instanceof ServerLevel serverLevel) || target == null || !target.isAlive()) {
            rangedChargeTicks = 0;
            return;
        }

        double distanceSqr = this.distanceToSqr(target);
        if (distanceSqr > RANGED_RANGE * RANGED_RANGE || !hasClearLaserLine(target)) {
            rangedChargeTicks = 0;
            return;
        }

        if (rangedChargeTicks > 0) {
            rangedChargeTicks--;
            drawLaser(serverLevel, target, false);
            if (rangedChargeTicks <= 0) {
                fireLaser(serverLevel, target);
            }
            return;
        }

        if (rangedAttackCooldown <= 0 && this.random.nextInt(4) == 0) {
            rangedChargeTicks = RANGED_CHARGE_TICKS;
            rangedAttackCooldown = RANGED_MIN_COOLDOWN_TICKS + this.random.nextInt(RANGED_RANDOM_COOLDOWN_TICKS + 1);
            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 0.45F, 0.55F);
        }
    }

    private void fireLaser(ServerLevel serverLevel, LivingEntity target) {
        if (!hasClearLaserLine(target)) {
            return;
        }

        drawLaser(serverLevel, target, true);
        if (target instanceof ServerPlayer player) {
            player.hurtServer(serverLevel, this.damageSources().magic(), LASER_DAMAGE);
            PacketDistributor.sendToPlayer(player, new BadTripScreamerPayload(18, 0.85F));
            StressManager.addStress(player, 0.035F);
            DrugEffectRuntimeManager.addEffect(player, EffectType.BLUR, 0.28F, 20 * 2);
            DrugEffectRuntimeManager.addEffect(player, EffectType.CONFUSION, 0.18F, 20 * 2);
        } else {
            target.hurtServer(serverLevel, this.damageSources().magic(), LASER_DAMAGE);
        }
    }

    private boolean hasClearLaserLine(LivingEntity target) {
        Vec3 from = this.getEyePosition();
        Vec3 to = target.getEyePosition();
        HitResult result = this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return result.getType() == HitResult.Type.MISS || result.getLocation().distanceToSqr(to) < 0.20D;
    }

    private void drawLaser(ServerLevel serverLevel, LivingEntity target, boolean impact) {
        Vec3 from = this.getEyePosition();
        Vec3 to = target.getEyePosition();
        Vec3 delta = to.subtract(from);
        int steps = Mth.clamp((int)(delta.length() * 3.0D), 4, 48);
        DustParticleOptions particle = new DustParticleOptions(impact ? 0xFF1111 : 0x660000, impact ? 1.15F : 0.75F);
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3 pos = from.add(delta.scale(t));
            serverLevel.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.01D, 0.01D, 0.01D, 0.0D);
        }
    }

    private boolean isOwnerStillInBadTrip(ServerPlayer owner) {
        PlayerAddictionStats stats = owner.getData(ModAttachments.PLAYER_ADDICTION.get());
        BadTripState state = stats.badTrip;
        return state.active
                && state.sourceDrug != DrugId.ALCOHOL
                && state.severity >= AddictionConstants.BAD_TRIP_STRONG_THRESHOLD;
    }

    private @Nullable ServerPlayer resolveOwner() {
        if (ownerPlayer == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(ownerPlayer);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity entity) {
        if (meleeAttackCooldown > 0) {
            return false;
        }
        meleeAttackCooldown = MELEE_COOLDOWN_TICKS;
        return super.doHurtTarget(level, entity);
    }

    @Override
    protected void populateDefaultEquipmentSlots(net.minecraft.util.RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            @Nullable SpawnGroupData spawnData
    ) {
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (!canDropRemains || !(damageSource.getEntity() instanceof Player killer)) {
            return;
        }

        ServerPlayer owner = resolveOwner();
        ServerPlayer relevant = owner != null ? owner : (killer instanceof ServerPlayer sp ? sp : null);

        // FRACTURED_IMPULSE: 25% drop when killed by stimulant-overdosed bad-trip player
        if (relevant != null && shouldDropFracturedImpulse(relevant) && level.random.nextFloat() < 0.25F) {
            this.drop(new ItemStack(ModItems.FRACTURED_IMPULSE.get()), true, false);
        }

        if (owner != null) {
            PlayerAddictionStats stats = owner.getData(ModAttachments.PLAYER_ADDICTION.get());
            if (stats.badTrip.demonRemainsDropped >= 2) {
                return;
            }
            stats.badTrip.demonRemainsDropped++;
        }

        int looting = getLootingLevel(level, damageSource.getWeaponItem());
        float chance = Math.min(REMAINS_DROP_CAP, REMAINS_DROP_BASE_CHANCE + looting * REMAINS_DROP_LOOTING_BONUS);
        if (this.random.nextFloat() < chance) {
            this.drop(new ItemStack(ModItems.INNER_DEMON_REMAINS.get()), true, false);
        }
    }

    private static boolean shouldDropFracturedImpulse(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        if (!org.mydrugs.mydrugs.effects.addiction.manager.state.BadTripManager.isActive(stats)) {
            return false;
        }
        for (org.mydrugs.mydrugs.core.drug.DrugId drugId : stats.getTrackedDrugIds()) {
            if (org.mydrugs.mydrugs.core.drug.DrugRegistry.getCategory(drugId) != org.mydrugs.mydrugs.core.drug.DrugCategory.STIMULANT) {
                continue;
            }
            var drugStats = stats.getDrugStats(drugId);
            if (drugStats == null) continue;
            org.mydrugs.mydrugs.effects.addiction.dose.DosePath path = org.mydrugs.mydrugs.effects.addiction.dose.DosePath.DRUG;
            org.mydrugs.mydrugs.effects.addiction.dose.DoseState state =
                    org.mydrugs.mydrugs.effects.addiction.manager.dose.DoseManager.resolveState(path, drugStats.currentDose());
            if (state == org.mydrugs.mydrugs.effects.addiction.dose.DoseState.VERY_HIGH
                    || state == org.mydrugs.mydrugs.effects.addiction.dose.DoseState.OVERDOSE) {
                return true;
            }
        }
        return false;
    }

    private static int getLootingLevel(ServerLevel level, ItemStack weapon) {
        if (weapon == null || weapon.isEmpty()) {
            return 0;
        }
        try {
            Optional<net.minecraft.core.Holder.Reference<net.minecraft.world.item.enchantment.Enchantment>> looting =
                    level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                            .get(Enchantments.LOOTING);
            return looting.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, weapon)).orElse(0);
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        long msb = input.getLongOr("owner_player_msb", 0L);
        long lsb = input.getLongOr("owner_player_lsb", 0L);
        this.ownerPlayer = (msb == 0L && lsb == 0L) ? null : new UUID(msb, lsb);
        this.lifetime = input.getIntOr("lifetime", DEFAULT_LIFETIME);
        this.canDropRemains = input.getBooleanOr("can_drop_remains", false);
        this.boundToBadTrip = input.getBooleanOr("bound_to_bad_trip", false);
        this.meleeAttackCooldown = input.getIntOr("melee_attack_cooldown", 0);
        this.rangedAttackCooldown = input.getIntOr("ranged_attack_cooldown", RANGED_MIN_COOLDOWN_TICKS);
        this.rangedChargeTicks = input.getIntOr("ranged_charge_ticks", 0);
        this.naturalDespawnTicks = input.getIntOr("natural_despawn_ticks", -1);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (ownerPlayer != null) {
            output.putLong("owner_player_msb", ownerPlayer.getMostSignificantBits());
            output.putLong("owner_player_lsb", ownerPlayer.getLeastSignificantBits());
        }
        output.putInt("lifetime", lifetime);
        output.putBoolean("can_drop_remains", canDropRemains);
        output.putBoolean("bound_to_bad_trip", boundToBadTrip);
        output.putInt("melee_attack_cooldown", meleeAttackCooldown);
        output.putInt("ranged_attack_cooldown", rangedAttackCooldown);
        output.putInt("ranged_charge_ticks", rangedChargeTicks);
        output.putInt("natural_despawn_ticks", naturalDespawnTicks);
    }
}
