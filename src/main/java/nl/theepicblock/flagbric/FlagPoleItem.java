package nl.theepicblock.flagbric;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class FlagPoleItem extends BlockItem {
	public FlagPoleItem(Block block, Settings settings) {
		super(block, settings);
	}

	@Override
	public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
		BlockPos blockPos = new BlockPos(context.getHitPos());
		World world = context.getWorld();
		BlockState blockState = world.getBlockState(blockPos);

		if (blockState.isOf(this.getBlock()) && !context.shouldCancelInteraction()) {
			// We're right-clicking another flag pole. This should trigger special placing behaviour
			var cursor = blockPos.mutableCopy();
			while (true) {
				cursor.move(0,1,0);

				// Check if we're still within build limit
				if (!world.isInBuildLimit(cursor)) {
					PlayerEntity playerEntity = context.getPlayer();
					int j = world.getTopY();
					if (playerEntity instanceof ServerPlayerEntity player) {
						player.sendMessage((new TranslatableText("build.tooHigh", j - 1)).formatted(Formatting.RED), MessageType.GAME_INFO, Util.NIL_UUID);
					}
					return null;
				}

				var state = world.getBlockState(cursor);
				if (!state.isOf(this.getBlock())) {
					// We've reached a block that's not a flag block, so we can attempt to place it here
					return ItemPlacementContext.offset(context, cursor, Direction.UP);
				}
			}
		}
		return context;
	}

	protected boolean checkStatePlacement() {
		return false;
	}
}
