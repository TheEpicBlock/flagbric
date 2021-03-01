package nl.theepicblock.flagbric;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
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
import org.jetbrains.annotations.Nullable;

public class FlagBlock extends BlockWithEntity {
	protected FlagBlock(Settings settings) {
		super(settings);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
		return VoxelShapes.cuboid(7f/16, 0f/16, 7f/16, 9f/16, 16f/16, 9f/16);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockView world) {
		return new FlagBlockEntity();
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

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
}
