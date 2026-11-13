package io.github.alexf6.puebloyparamo.block;

import com.mojang.serialization.MapCodec;
import io.github.alexf6.puebloyparamo.block.entity.ModBlockEntities;
import io.github.alexf6.puebloyparamo.block.entity.PicoBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Equipo de sonido tradicional costeño (Picó) colorido.
 * Reproduce discos personalizados (cumbia, vallenato, salsa y rap local) y discos vanilla
 * a largo alcance con notas musicales y ambiente festivo.
 */
public class PicoBlock extends FacingEntityBlock {

    public static final MapCodec<PicoBlock> CODEC = simpleCodec(PicoBlock::new);
    public static final BooleanProperty PLAYING = BooleanProperty.create("playing");

    private static final VoxelShape SHAPE = Block.box(1.5, 0.0, 1.5, 14.5, 15.0, 14.5);

    public PicoBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(PLAYING, false)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PLAYING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PicoBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.PICO, PicoBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof PicoBlockEntity pico)) {
            return InteractionResult.PASS;
        }
        if (tryEjectRecord(level, pico, player)) {
            return InteractionResult.SUCCESS;
        }
        if (stack.has(DataComponents.JUKEBOX_PLAYABLE)) {
            if (!level.isClientSide()) {
                ItemStack discCopy = stack.copyWithCount(1);
                if (!player.hasInfiniteMaterials()) {
                    stack.shrink(1);
                }
                pico.insertRecord((ServerLevel) level, discCopy, player);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PicoBlockEntity pico)) {
            return InteractionResult.PASS;
        }
        return tryEjectRecord(level, pico, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private static boolean tryEjectRecord(Level level, PicoBlockEntity pico, Player player) {
        if (!pico.hasRecord()) {
            return false;
        }
        if (!level.isClientSide()) {
            pico.ejectRecord((ServerLevel) level, player);
        }
        return true;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof PicoBlockEntity pico) {
            pico.stopAndDrop(serverLevel);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(PLAYING)) {
            return;
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(
                    ParticleTypes.NOTE,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    pos.getY() + 1.05,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    random.nextInt(24) / 24.0,
                    0.0,
                    0.0
            );
        }
    }
}
