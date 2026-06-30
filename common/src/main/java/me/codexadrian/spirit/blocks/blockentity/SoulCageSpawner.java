package me.codexadrian.spirit.blocks.blockentity;

import me.codexadrian.spirit.Corrupted;
import me.codexadrian.spirit.SpiritConfig;
import me.codexadrian.spirit.data.Tier;
import me.codexadrian.spirit.menu.SoulCageMenu;
import me.codexadrian.spirit.registry.SpiritBlocks;
import me.codexadrian.spirit.utils.SoulUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class SoulCageSpawner {

    private double spin;
    private int spawnDelay = 20;
    private final SoulCageBlockEntity soulCageBlockEntity;

    public SoulCageSpawner(SoulCageBlockEntity entity) {
        this.soulCageBlockEntity = entity;
    }

    public void tick() {
        Level level = this.getLevel();
        BlockPos blockPos = this.getPos();
        if (level.isClientSide) {
            if (this.isNearPlayer()) {
                double spinAmount = 20D;
                Tier tier = SoulUtils.getTier(soulCageBlockEntity.getItem(0), level);
                if (tier != null && tier.redstoneControlled()
                        && level.hasNeighborSignal(soulCageBlockEntity.getBlockPos())) {
                    spinAmount /= 30;
                } else {
                    double d = (double) blockPos.getX() + level.random.nextDouble();
                    double e = (double) blockPos.getY() + level.random.nextDouble();
                    double f = (double) blockPos.getZ() + level.random.nextDouble();
                    level.addParticle(ParticleTypes.SOUL, d, e, f, 0.0D, 0.0D, 0.0D);
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d, e, f, 0.0D, 0.0D, 0.0D);
                }
                this.spin = (this.spin + spinAmount) % 360D;
            }
        } else if (this.isNearPlayer()) {
            Tier tier = SoulUtils.getTier(soulCageBlockEntity.getItem(0), level);
            if (tier == null) {
                return;
            }

            if (this.spawnDelay == -1) {
                this.delay(tier);
            }

            if (this.spawnDelay > 0) {
                --this.spawnDelay;
                return;
            }

            if (tier.redstoneControlled() && level.hasNeighborSignal(soulCageBlockEntity.getBlockPos())) {
                return;
            }

            boolean bl = false;
            int i = 0;

            while (true) {
                if (i >= tier.spawnCount()) {
                    // Always reset the delay (even when nothing spawned) to avoid a per-tick retry storm.
                    this.delay(tier);
                    break;
                }

                if (soulCageBlockEntity.type == null) {
                    this.delay(tier);
                    return;
                }

                double x = blockPos.getX() + (level.random.nextDouble() - level.random.nextDouble()) * effectiveSpawnRange(tier)
                        + 0.5D;
                double y = blockPos.getY() + level.random.nextInt(3) - 1;
                double z = blockPos.getZ() + (level.random.nextDouble() - level.random.nextDouble()) * effectiveSpawnRange(tier)
                        + 0.5D;

                if (level.noCollision(soulCageBlockEntity.type.getDimensions().makeBoundingBox(x, y, z))) {
                    ServerLevel serverLevel = (ServerLevel) level;
                    if (tier.ignoreSpawnConditions() || SpawnPlacements.checkSpawnRules(soulCageBlockEntity.type,
                            serverLevel, MobSpawnType.TRIGGERED, BlockPos.containing(x, y, z), level.getRandom())) {
                        Entity spawned = soulCageBlockEntity.type.create(level);
                        if (spawned == null) {
                            this.delay(tier);
                            return;
                        }
                        ((Corrupted) spawned).setCorrupted();
                        spawned.moveTo(x, y, z, spawned.getYRot(), spawned.getXRot());

                        int l = level
                                .getEntitiesOfClass(spawned.getClass(), new AABB(blockPos).inflate(effectiveSpawnRange(tier)))
                                .size();
                        if (l >= 6) {
                            this.delay(tier);
                            return;
                        }

                        spawned.moveTo(spawned.getX(), spawned.getY(), spawned.getZ(),
                                level.random.nextFloat() * 360.0F, 0.0F);
                        if (spawned instanceof Mob mob) {
                            if ((!tier.ignoreSpawnConditions() && !mob.checkSpawnRules(level, MobSpawnType.TRIGGERED))
                                    || !mob.checkSpawnObstruction(level)) {
                                this.delay(tier);
                                return;
                            }

                            mob.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(spawned.blockPosition()),
                                    MobSpawnType.TRIGGERED, null);
                        }

                        if (!serverLevel.tryAddFreshEntityWithPassengers(spawned)) {
                            this.delay(tier);
                            return;
                        }

                        serverLevel.sendParticles(ParticleTypes.SOUL, blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                                20, 1, 1, 1, 0);

                        if (spawned instanceof Mob mob) {
                            mob.spawnAnim();
                        }

                        bl = true;
                    }
                }

                ++i;
            }
        }
    }

    private boolean isNearPlayer() {
        BlockPos blockPos = this.getPos();
        BlockState blockState = getLevel().getBlockState(getPos());
        if (blockState.is(SpiritBlocks.SOUL_CAGE.get())) {
            Tier tier = SoulUtils.getTier(soulCageBlockEntity.getItem(0), this.getLevel());
            if (tier == null) {
                return false;
            } else if (tier.nearbyRange() > 0) {
                return this.getLevel().hasNearbyAlivePlayer((double) blockPos.getX() + 0.5D,
                        (double) blockPos.getY() + 0.5D, (double) blockPos.getZ() + 0.5D, tier.nearbyRange());
            } else {
                return true;
            }
        }

        return false;

    }

    /** Tier spawn range plus the cage's purchased range bonus (clamped non-negative). */
    private int effectiveSpawnRange(Tier tier) {
        // Vanilla mode ignores purchased upgrades — fall back to the plain tier range.
        if (SpiritConfig.isVanillaMode()) {
            return Math.max(0, tier.spawnRange());
        }
        return Math.max(0, tier.spawnRange() + soulCageBlockEntity.spawnRangeBonus);
    }

    private void delay(Tier tier) {
        // Vanilla mode ignores netherite upgrades and range penalties — use the plain tier delay window.
        if (SpiritConfig.isVanillaMode()) {
            int vMin = tier.minSpawnDelay();
            int vMax = Math.max(vMin, tier.maxSpawnDelay());
            this.spawnDelay = vMax <= vMin ? vMin : vMin + this.getLevel().random.nextInt(vMax - vMin);
            this.broadcastEvent(1);
            if (this.getLevel() != null && !this.getLevel().isClientSide()) {
                this.soulCageBlockEntity.update(net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            }
            return;
        }
        // Netherite reduces min/max separately; lowering range adds a penalty back onto max. Floor at MIN_DELAY_FLOOR.
        int minReduction = Math.max(0, soulCageBlockEntity.minDelayReductionTicks);
        int min = Math.max(SoulCageMenu.MIN_DELAY_FLOOR, tier.minSpawnDelay() - minReduction);
        int max = Math.max(min, tier.maxSpawnDelay() - soulCageBlockEntity.maxDelayReductionTicks
                + Math.max(0, soulCageBlockEntity.rangeDelayPenaltyTicks));
        if (max <= min) {
            this.spawnDelay = min;
        } else {
            this.spawnDelay = min + this.getLevel().random.nextInt(max - min);
        }

        this.broadcastEvent(1);
        // Sync the fresh spawn delay to clients so the wand's "next spawn" countdown stays accurate.
        if (this.getLevel() != null && !this.getLevel().isClientSide()) {
            this.soulCageBlockEntity.update(net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    public boolean onEventTriggered(int i) {
        if (i == 1 && this.getLevel().isClientSide()) {
            Tier tier = SoulUtils.getTier(soulCageBlockEntity.getItem(0), this.getLevel());
            if (tier == null) {
                return false;
            }

            this.spawnDelay = SpiritConfig.isVanillaMode()
                    ? tier.minSpawnDelay()
                    : Math.max(SoulCageMenu.MIN_DELAY_FLOOR,
                            tier.minSpawnDelay() - Math.max(0, soulCageBlockEntity.minDelayReductionTicks));
            return true;
        } else {
            return false;
        }
    }

    public void broadcastEvent(int i) {
        Level level = soulCageBlockEntity.getLevel();
        if (level != null) {
            level.blockEvent(soulCageBlockEntity.getBlockPos(), SpiritBlocks.SOUL_CAGE.get(), i, 0);
        }
    }

    public Level getLevel() {
        return soulCageBlockEntity.getLevel();
    }

    public BlockPos getPos() {
        return soulCageBlockEntity.getBlockPos();
    }

    public double getSpin() {
        return spin;
    }

    public int getSpawnDelay() {
        return spawnDelay;
    }

}
