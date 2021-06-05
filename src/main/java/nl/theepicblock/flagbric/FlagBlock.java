package nl.theepicblock.flagbric;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
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
			world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		return VoxelShapes.cuboid(7f/16, 0f/16, 7f/16, 9f/16, 16f/16, 9f/16);
	}

	@Override
	public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
		if (!(world.getBlockEntity(blockPos) instanceof FlagBlockEntity)) return ActionResult.PASS;

		FlagBlockEntity blockEntity = (FlagBlockEntity) world.getBlockEntity(blockPos);
		if (blockEntity == null) return ActionResult.PASS;

		if (!blockEntity.isValid(0, player.getStackInHand(hand))) return ActionResult.PASS;
		if (world.isClient) return ActionResult.SUCCESS;

		Direction playerFacing = player.getHorizontalFacing().getOpposite();
		ItemStack bStack = blockEntity.getStack(0).copy();
		ItemStack pStack = player.getStackInHand(hand).copy();
		if (pStack.getCount() > 1) {
			if (bStack.isEmpty()) {
				pStack.decrement(1);
				player.setStackInHand(hand, pStack.copy());
				pStack.setCount(1);
				blockEntity.setDirection(playerFacing);
				blockEntity.setStack(0, pStack.copy());
			} else {
				return ActionResult.FAIL;
			}
		} else {
			blockEntity.setDirection(playerFacing);
			blockEntity.setStack(0, pStack);
			player.setStackInHand(hand, bStack);
			blockEntity.sync();
		}


		return ActionResult.SUCCESS;
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
