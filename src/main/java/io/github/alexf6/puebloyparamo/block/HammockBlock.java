package io.github.alexf6.puebloyparamo.block;

import com.mojang.serialization.MapCodec;
import io.github.alexf6.puebloyparamo.entity.SeatEntity;
import io.github.alexf6.puebloyparamo.logic.HammockLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Hamaca tejida tradicional de dos bloques.
 * Permite mecerse en cualquier momento y dormir de noche o en tormenta
 * sin sobrescribir el punto de reaparición del jugador.
 */
public class HammockBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<HammockBlock> CODEC = simpleCodec(HammockBlock::new);
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;

    private static final VoxelShape HAMMOCK_NS = Block.box(2.0, 4.0, 0.0, 14.0, 10.0, 16.0);
    private static final VoxelShape HAMMOCK_EW = Block.box(0.0, 4.0, 2.0, 16.0, 10.0, 14.0);

    public HammockBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(PART, BedPart.FOOT)
        );
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction axis = state.getValue(FACING);
        return (axis == Direction.NORTH || axis == Direction.SOUTH) ? HAMMOCK_NS : HAMMOCK_EW;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        BlockPos partnerPos = pos.relative(facing);
        Level level = context.getLevel();
        if (level.getBlockState(partnerPos).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(partnerPos)) {
            return this.defaultBlockState().setValue(FACING, facing).setValue(PART, BedPart.FOOT);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockPos partnerPos = pos.relative(state.getValue(FACING));
            level.setBlock(partnerPos, state.setValue(PART, BedPart.HEAD), Block.UPDATE_ALL);
            level.updateNeighborsAt(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random
    ) {
        Direction expectedNeighbour = getNeighbourDirection(state.getValue(PART), state.getValue(FACING));
        if (direction == expectedNeighbour) {
            return neighbourState.is(this) && neighbourState.getValue(PART) != state.getValue(PART)
                    ? state
                    : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative()) {
            BedPart part = state.getValue(PART);
            BlockPos partnerPos = pos.relative(getNeighbourDirection(part, state.getValue(FACING)));
            BlockState partnerState = level.getBlockState(partnerPos);
            if (partnerState.is(this) && partnerState.getValue(PART) != part) {
                level.setBlock(partnerPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private static Direction getNeighbourDirection(BedPart part, Direction facing) {
        return part == BedPart.FOOT ? facing : facing.getOpposite();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (serverPlayer.isShiftKeyDown()) {
            return sitInHammock(serverLevel, pos, state, serverPlayer) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        boolean overworld = serverLevel.dimension() == Level.OVERWORLD;
        boolean monstersNearby = hasNearbyMonsters(serverLevel, pos, serverPlayer);
        HammockLogic.RestCheckResult check = HammockLogic.evaluateRest(
                overworld,
                serverLevel.getDayTime(),
                serverLevel.isThundering(),
                monstersNearby
        );

        switch (check) {
            case WRONG_DIMENSION -> {
                serverPlayer.displayClientMessage(Component.translatable("message.puebloyparamo.hammock.wrong_dimension"), true);
                return InteractionResult.CONSUME;
            }
            case MONSTERS_NEARBY -> {
                serverPlayer.displayClientMessage(Component.translatable("message.puebloyparamo.hammock.monsters"), true);
                return InteractionResult.CONSUME;
            }
            case NOT_NIGHT_OR_STORM -> {
                sitInHammock(serverLevel, pos, state, serverPlayer);
                serverPlayer.displayClientMessage(Component.translatable("message.puebloyparamo.hammock.day_swing"), true);
                return InteractionResult.CONSUME;
            }
            case ALLOW -> {
                long nextDawn = HammockLogic.computeNextDawnTick(serverLevel.getDayTime());
                serverLevel.setDayTime(nextDawn);
                if (serverLevel.isRaining() || serverLevel.isThundering()) {
                    serverLevel.setWeatherParameters(6000, 0, false, false);
                }
                serverPlayer.heal(6.0F);
                serverPlayer.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
                serverLevel.playSound(null, pos, SoundEvents.WOOL_STEP, SoundSource.BLOCKS, 0.9F, 0.85F);
                serverPlayer.displayClientMessage(Component.translatable("message.puebloyparamo.hammock.slept"), true);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean sitInHammock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player) {
        float yaw = state.getValue(FACING).getClockWise().toYRot();
        return SeatEntity.sitPlayer(level, pos, 0.30, yaw, player);
    }

    private static boolean hasNearbyMonsters(ServerLevel level, BlockPos pos, ServerPlayer player) {
        AABB zone = new AABB(pos).inflate(8.0, 5.0, 8.0);
        List<Monster> monsters = level.getEntitiesOfClass(
                Monster.class, zone, m -> m.isAlive() && m.isPreventingPlayerRest(level, player)
        );
        return !monsters.isEmpty();
    }
}
