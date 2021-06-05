package nl.theepicblock.flagbric;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FlagItem extends BlockItem {
	public FlagItem(Block block, Settings settings) {
		super(block, settings);
	}

	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos blockPos = context.getBlockPos();
		BlockState blockState = world.getBlockState(blockPos);

		if (blockState.isOf(this.getBlock())) {
			return recursivePlace(blockPos, world, context.getPlayer(), context);
		} else {
			BlockPos blockPos1 = blockPos.offset(context.getSide());
			if (blockState.isOf(this.getBlock())) {
				return recursivePlace(blockPos1, world, context.getPlayer(), context);
			}
		}
		return super.useOnBlock(context);
	}

	private ActionResult recursivePlace(BlockPos pos, World world, PlayerEntity playerEntity, ItemUsageContext context) {
		BlockPos.Mutable mPos = pos.mutableCopy();
		while (true) {
			mPos.move(0,1,0);
			if (!world.isInBuildLimit(mPos)) {
				if (!world.isClient) {
					int j = world.getHeight();
					if (playerEntity instanceof ServerPlayerEntity && mPos.getY() >= j) {
						GameMessageS2CPacket gameMessageS2CPacket = new GameMessageS2CPacket((new TranslatableText("build.tooHigh", new Object[]{j})).formatted(Formatting.RED), MessageType.GAME_INFO, Util.NIL_UUID);
						((ServerPlayerEntity)playerEntity).networkHandler.sendPacket(gameMessageS2CPacket);
					}
				}
				return ActionResult.FAIL;
			}

			BlockState state = world.getBlockState(mPos);
			if (state.isOf(this.getBlock())) {
				continue;
			} else {
				ItemPlacementContext context1 = new ItemPlacementContext(context);
				context1 = ItemPlacementContext.offset(context1, mPos, Direction.UP);
				if (state.canReplace(context1)) {
					return this.place(context1);
				} else {
					return ActionResult.FAIL;
				}
			}
		}
	}

	protected boolean checkStatePlacement() {
		return false;
	}
}
