package com.sinscold.newfishing.block;

import com.mojang.serialization.MapCodec;
import com.sinscold.newfishing.block.entity.CrateBlockEntity;
import com.sinscold.newfishing.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Ящик (водяной или лавовый): блок вроде сундука. Выловил, поставил, открыл; закрыл — ящик исчезает и роняет оставшееся.
 * Если сломать, не открывая, тоже роняет добычу. Содержимое берётся из таблицы лута при первом открытии/разрушении.
 */
public class CrateBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** Запертый ящик: не открывается, при поломке не роняет лут, а роняет себя; замок снимается ПКМ топором. */
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    private final ResourceKey<LootTable> contentsLootTable;

    public CrateBlock(Properties properties, String lootTableName) {
        super(properties);
        this.contentsLootTable = ResourceKey.create(Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("newfishing", "chests/" + lootTableName));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH).setValue(LOCKED, false));
    }

    public ResourceKey<LootTable> getContentsLootTable() {
        return contentsLootTable;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(props -> new CrateBlock(props, "water_crate"));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOCKED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, ModBlockEntities.CRATE.get(), CrateBlockEntity::lidAnimateTick) : null;
    }

    /** ПКМ топором по запертому ящику снимает замок (топор теряет 1 прочность). */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(LOCKED) && stack.is(ItemTags.AXES)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(LOCKED, false), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 0.8F, 1.2F);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(LOCKED)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.newfishing.crate_locked"), true);
                level.playSound(null, pos, com.sinscold.newfishing.registry.ModSounds.CRATE_LOCKED.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof CrateBlockEntity crate) {
            player.openMenu(crate);
        }
        return InteractionResult.CONSUME;
    }

    /** Следим за открывшими; когда ящик закрыт — уничтожаем и роняем содержимое. */
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof CrateBlockEntity crate) {
            crate.recheckOpen();
            if (crate.shouldDestroy()) {
                level.destroyBlock(pos, false);
            }
        }
    }

    /** При любом удалении блока (закрыли, сломали) содержимое выпадает; сам ящик ничего не роняет. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            // запертый ящик лут не роняет (он выпадает самим ящиком по таблице блока)
            if (!state.getValue(LOCKED) && level.getBlockEntity(pos) instanceof CrateBlockEntity crate) {
                Containers.dropContents(level, pos, crate);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
