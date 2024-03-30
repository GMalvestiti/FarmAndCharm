package satisfy.farm_and_charm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import satisfy.farm_and_charm.entity.MincerBlockEntity;
import satisfy.farm_and_charm.registry.BlockEntityTypeRegistry;

import java.util.Collections;
import java.util.List;

//TODO

@SuppressWarnings("deprecation")
public class MincerBlock extends BaseEntityBlock {
    public static final int STIRS_NEEDED = 50;
    public static final IntegerProperty CRANK = IntegerProperty.create("crank", 0, 32);
    public static final IntegerProperty CRANKED = IntegerProperty.create("cranked", 0, 100);

    public MincerBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(CRANK, 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(CRANKED, 0));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(world, pos);
        return (Shapes.or(box(3, 0, 3, 13, 8, 13), box(4, 3, 4, 12, 8, 12))).move(offset.x, offset.y, offset.z);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CRANK);
        builder.add(CRANKED);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }

    @Override
    public @NotNull InteractionResult use(BlockState blockState, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        ItemStack itemStack = player.getItemInHand(hand);
        if (blockEntity instanceof MincerBlockEntity mincerEntity) {
            int crank = blockState.getValue(CRANK);
            int cranked = blockState.getValue(CRANKED);

            if (!itemStack.isEmpty() && crank == 0) {
                if (mincerEntity.canAddItem(itemStack)) {
                    mincerEntity.addItemStack(itemStack.copy());
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            } else if (itemStack.isEmpty()) {
                if (cranked >= STIRS_NEEDED && crank == 0) {
                    player.getInventory().add(mincerEntity.getItem(4));
                    mincerEntity.setItem(4, ItemStack.EMPTY);
                    world.setBlock(pos, blockState.setValue(CRANKED, 0), 3);
                    return InteractionResult.SUCCESS;
                }
                if (world instanceof ServerLevel serverWorld) {
                    RandomSource randomSource = serverWorld.random;
                    for (ItemStack stack : mincerEntity.getItems()) {
                        if (!stack.isEmpty() && mincerEntity.getItem(4) != stack) {
                            ItemParticleOption particleOption = new ItemParticleOption(ParticleTypes.ITEM, stack);
                            serverWorld.sendParticles(particleOption, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, 1, randomSource.nextGaussian() * 0.15D, 0.05D, randomSource.nextGaussian() * 0.15D, 0.05D);
                        }
                    }
                }
                if (crank <= 6) {
                    world.setBlock(pos, blockState.setValue(CRANK, 10), 3);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }


    @Override
    public void tick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(blockState, level, pos, random);
    }

    @Override
    public void onPlace(BlockState blockstate, Level world, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(blockstate, world, pos, oldState, moving);
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        return tileEntity instanceof MenuProvider ? (MenuProvider) tileEntity : null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MincerBlockEntity(pos, state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
        super.triggerEvent(state, world, pos, eventID, eventParam);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(eventID, eventParam);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof MincerBlockEntity be) {
                Containers.dropContents(world, pos, be);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntityTypeRegistry.MINCER_BLOCK_ENTITY.get(), (world1, pos, state1, be) -> be.tick(world1, pos, state1, be));
    }


    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}
