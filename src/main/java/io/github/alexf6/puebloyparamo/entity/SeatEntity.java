package io.github.alexf6.puebloyparamo.entity;

import io.github.alexf6.puebloyparamo.block.HammockBlock;
import io.github.alexf6.puebloyparamo.block.RimaxChairBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Entidad asiento invisible y ligera para sentarse en Sillas Rimax o mecerse en Hamacas.
 * Se elimina automáticamente cuando el jugador desmonta o desaparece el bloque soporte.
 */
public class SeatEntity extends Entity {

    public SeatEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        BlockPos pos = blockPosition();
        BlockState state = this.level().getBlockState(pos);
        boolean validHost = state.getBlock() instanceof RimaxChairBlock || state.getBlock() instanceof HammockBlock;
        if (!validHost || this.getPassengers().isEmpty()) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos pos = blockPosition();
        BlockPos[] candidates = new BlockPos[] {
                pos.above(),
                pos.relative(getDirection()),
                pos.north(),
                pos.south(),
                pos.east(),
                pos.west()
        };
        for (BlockPos candidate : candidates) {
            double floor = level().getBlockFloorHeight(candidate);
            if (!DismountHelper.isBlockFloorValid(floor)) {
                continue;
            }
            Vec3 target = new Vec3(candidate.getX() + 0.5, candidate.getY() + floor, candidate.getZ() + 0.5);
            for (Pose pose : passenger.getDismountPoses()) {
                if (DismountHelper.canDismountTo(level(), target, passenger, pose)) {
                    passenger.setPose(pose);
                    return target;
                }
            }
        }
        return new Vec3(getX(), pos.getY() + 1.0, getZ());
    }

    /**
     * Sienta al jugador en {@code pos} si el asiento está libre, o devuelve {@code false} si ya está ocupado.
     */
    public static boolean sitPlayer(Level level, BlockPos pos, double yOffset, float facingYaw, LivingEntity player) {
        if (level.isClientSide() || player.isPassenger()) {
            return false;
        }
        AABB searchBox = new AABB(pos).inflate(0.2);
        List<SeatEntity> existing = level.getEntitiesOfClass(SeatEntity.class, searchBox);
        for (SeatEntity seat : existing) {
            if (!seat.getPassengers().isEmpty()) {
                return false;
            }
            seat.discard();
        }
        SeatEntity seat = new SeatEntity(ModEntities.SEAT, level);
        seat.setPos(pos.getX() + 0.5, pos.getY() + yOffset, pos.getZ() + 0.5);
        seat.setYRot(facingYaw);
        player.setYRot(facingYaw);
        level.addFreshEntity(seat);
        return player.startRiding(seat);
    }
}
