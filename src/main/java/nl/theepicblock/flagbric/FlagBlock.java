package nl.theepicblock.flagbric;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import nl.theepicblock.flagbric.mixin.PlayerInventoryAccessor;
import org.jetbrains.annotations.Nullable;

public class FlagBlock extends BlockWithEntity implements Waterloggable {
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	protected FlagBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.getStateManager().getDefaultState().with(WATERLOGGED, false));
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(WATERLOGGED,
				ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
	}

	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) {
			world.createAndScheduleBlockTick(pos, Blocks.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		return VoxelShapes.cuboid(7f/16, 0f/16, 7f/16, 9f/16, 16f/16, 9f/16);
	}

	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (!(world.getBlockEntity(blockPos) instanceof FlagBlockEntity blockEntity)) return ActionResult.PASS;

		// Check if item is a banner
		if (!blockEntity.isValid(0, player.getStackInHand(hand))) return ActionResult.PASS;
		if (world.isClient()) return ActionResult.SUCCESS;

		ItemStack handStack = player.getStackInHand(hand);

		// This is true if the item the player is holding is the same as the item already on the pole
		// In this case, it doesn't make sense to swap the banner with the same banner
		// So instead, we're just going to remove the banner already there but not replace it with anything
		var holdingTheSameItem = ItemStack.canCombine(handStack, blockEntity.getStack(0));

		if (!blockEntity.isEmpty()) {
			// Remove the existing item
			ItemStack originalStack = blockEntity.getStack(0).copy();
			if (player.isCreative()) {
				// Don't give a new banner if the player already has a banner
				if (!inventoryContainsTagEquals(player, originalStack)) {
					player.getInventory().offerOrDrop(originalStack);
				}
			} else {
				// Not creative, so always give the item
				boolean succes = player.giveItemStack(originalStack);
				if (!succes) {
					ItemScatterer.spawn(world, blockPos.getX(), blockPos.getY(), blockPos.getZ(), originalStack);
				}
			}
			blockEntity.setStack(0, ItemStack.EMPTY);
		}

		if (handStack.isIn(ItemTags.BANNERS) && !holdingTheSameItem) {
			assert blockEntity.isEmpty();
			if (!player.isCreative())
				handStack.decrement(1);
			player.setStackInHand(hand, handStack.copy());
			handStack.setCount(1);
			blockEntity.setDirection(player.getHorizontalFacing().getOpposite());
			blockEntity.setStack(0, handStack.copy());
		}

		return ActionResult.CONSUME_PARTIAL;
	}

	public static boolean inventoryContainsTagEquals(PlayerEntity playerEntity, ItemStack stackToFind) {
		var inventoryLists = ((PlayerInventoryAccessor)playerEntity.getInventory()).getCombinedInventory();
		for (DefaultedList<ItemStack> stacks : inventoryLists) {
			for (ItemStack stack : stacks) {
				if (!stack.isEmpty()) {
					if (ItemStack.canCombine(stack, stackToFind))
						return true;
				}
			}
		}
		return false;
	}

	public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!state.isOf(newState.getBlock())) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof Inventory) {
				ItemScatterer.spawn(world, pos, (Inventory)blockEntity);
				world.updateComparators(pos, this);
			}

			super.onStateReplaced(state, world, pos, newState, moved);
		}
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		if (!world.isClient() && world.getBlockEntity(pos) instanceof FlagBlockEntity be && placer != null) {
			be.setDirection(placer.getHorizontalFacing().getOpposite());
		}
		super.onPlaced(world, pos, state, placer, itemStack);
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new FlagBlockEntity(pos, state);
	}
}
