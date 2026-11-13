package io.github.alexf6.puebloyparamo.block;

import com.mojang.serialization.MapCodec;
import io.github.alexf6.puebloyparamo.PuebloYParamoConfig;
import io.github.alexf6.puebloyparamo.item.ModItems;
import io.github.alexf6.puebloyparamo.logic.FrailejonLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Frailejón andino (Espeletia) de dos bloques de altura.
 * Almacena agua pura extraíble con frasco de vidrio, se recarga con lluvia/humedad
 * y libera bruma ambiental alrededor para recrear la atmósfera de páramo en cumbres frías.
 */
public class FrailejonBlock extends DoublePlantBlock implements BonemealableBlock {

    public static final MapCodec<FrailejonBlock> CODEC = simpleCodec(FrailejonBlock::new);
    public static final BooleanProperty MOIST = BooleanProperty.create("moist");

    private static final VoxelShape LOWER_SHAPE = Block.box(3.5, 0.0, 3.5, 12.5, 16.0, 12.5);
    private static final VoxelShape UPPER_SHAPE = Block.box(2.5, 0.0, 2.5, 13.5, 14.0, 13.5);

    public FrailejonBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(HALF, DoubleBlockHalf.LOWER)
                        .setValue(MOIST, true)
        );
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MOIST);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.SNOW_BLOCK);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? UPPER_SHAPE : LOWER_SHAPE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && !state.getValue(MOIST);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean raining = level.isRainingAt(pos.above(2));
        if (FrailejonLogic.shouldRechargeWater(state.getValue(MOIST), raining, pos.getY(), random.nextDouble())) {
            setMoistBothHalves(level, pos, true);
        }
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
        if (!stack.is(Items.GLASS_BOTTLE)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState lowerState = level.getBlockState(lowerPos);
        if (!lowerState.is(this) || !lowerState.getValue(MOIST)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            setMoistBothHalves(serverLevel, lowerPos, false);
            if (!player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            ItemStack pureWater = new ItemStack(ModItems.AGUA_PURA);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, pureWater);
            } else if (!player.getInventory().add(pureWater)) {
                player.drop(pureWater, false);
            }
            serverLevel.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.9F, 1.15F);
            serverLevel.sendParticles(
                    ParticleTypes.SPLASH,
                    pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    10, 0.25, 0.25, 0.25, 0.04
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return;
        }
        long timeOfDay = Math.floorMod(level.getDayTime(), 24000L);
        boolean nightOrDawn = timeOfDay < 1500L || timeOfDay > 12000L;
        int count = FrailejonLogic.computeMistParticleCount(
                PuebloYParamoConfig.get().enableFrailejonMistParticles(),
                level.isRainingAt(pos.above()),
                pos.getY(),
                nightOrDawn
        );
        if (count <= 0 || random.nextInt(3) != 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            double px = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 3.4;
            double py = pos.getY() + 0.1 + random.nextDouble() * 1.1;
            double pz = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 3.4;
            level.addParticle(ParticleTypes.CLOUD, px, py, pz, (random.nextDouble() - 0.5) * 0.015, 0.004, (random.nextDouble() - 0.5) * 0.015);
        }
    }

    private void setMoistBothHalves(Level level, BlockPos lowerPos, boolean moist) {
        BlockState lower = level.getBlockState(lowerPos);
        if (lower.is(this)) {
            level.setBlock(lowerPos, lower.setValue(MOIST, moist), Block.UPDATE_CLIENTS);
        }
        BlockPos upperPos = lowerPos.above();
        BlockState upper = level.getBlockState(upperPos);
        if (upper.is(this)) {
            level.setBlock(upperPos, upper.setValue(MOIST, moist), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockState lowerState = level.getBlockState(lowerPos);
        if (lowerState.is(this) && !lowerState.getValue(MOIST)) {
            setMoistBothHalves(level, lowerPos, true);
            return;
        }
        for (int attempt = 0; attempt < 6; attempt++) {
            int dx = random.nextIntBetweenInclusive(-2, 2);
            int dz = random.nextIntBetweenInclusive(-2, 2);
            if (dx == 0 && dz == 0) {
                continue;
            }
            BlockPos target = lowerPos.offset(dx, random.nextIntBetweenInclusive(-1, 1), dz);
            if (level.isEmptyBlock(target) && level.isEmptyBlock(target.above()) && mayPlaceOn(level.getBlockState(target.below()), level, target.below())) {
                DoublePlantBlock.placeAt(level, this.defaultBlockState().setValue(MOIST, true), target, Block.UPDATE_ALL);
                return;
            }
        }
    }
}
