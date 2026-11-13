package io.github.alexf6.puebloyparamo.block;

import com.mojang.serialization.MapCodec;
import io.github.alexf6.puebloyparamo.PuebloYParamoConfig;
import io.github.alexf6.puebloyparamo.entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Icónica Silla Rimax plástica sentable y resistente a explosiones como meme tradicional.
 */
public class RimaxChairBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<RimaxChairBlock> CODEC = simpleCodec(RimaxChairBlock::new);

    private static final VoxelShape SEAT = Block.box(2.0, 0.0, 2.0, 14.0, 8.0, 14.0);
    private static final VoxelShape BACK_NORTH = Block.box(2.0, 8.0, 11.5, 14.0, 17.0, 14.0);
    private static final VoxelShape BACK_SOUTH = Block.box(2.0, 8.0, 2.0, 14.0, 17.0, 4.5);
    private static final VoxelShape BACK_WEST = Block.box(11.5, 8.0, 2.0, 14.0, 17.0, 14.0);
    private static final VoxelShape BACK_EAST = Block.box(2.0, 8.0, 2.0, 4.5, 17.0, 14.0);

    private static final VoxelShape SHAPE_NORTH = Shapes.or(SEAT, BACK_NORTH);
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(SEAT, BACK_SOUTH);
    private static final VoxelShape SHAPE_WEST = Shapes.or(SEAT, BACK_WEST);
    private static final VoxelShape SHAPE_EAST = Shapes.or(SEAT, BACK_EAST);

    public RimaxChairBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public float getExplosionResistance() {
        return PuebloYParamoConfig.get().enableRimaxExplosionImmunity() ? 3_600_000.0F : 3.0F;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        float yaw = state.getValue(FACING).toYRot();
        return SeatEntity.sitPlayer(level, pos, 0.36, yaw, player) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }
}
