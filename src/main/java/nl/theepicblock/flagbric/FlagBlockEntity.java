package nl.theepicblock.flagbric;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

public class FlagBlockEntity extends BlockEntity implements Inventory,BlockEntityClientSerializable {
	private ItemStack banner = ItemStack.EMPTY;
	private Direction direction = Direction.NORTH;

	public FlagBlockEntity() {
		super(Flagbric.FLAG_BLOCK_ENTITY);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return banner == null || banner.isEmpty();
	}

	@Override
	public ItemStack getStack(int slot) {
		if (slot == 0) {
			return banner;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		return removeStack(slot);
	}

	@Override
	public ItemStack removeStack(int slot) {
		if (slot == 0) {
			ItemStack a = banner.copy();
			banner = ItemStack.EMPTY;
			this.sync();
			return a;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		if (slot == 0) {
			banner = stack;
			this.sync();
		}
	}

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void clear() {
		this.banner = ItemStack.EMPTY;
		this.sync();
	}

	@Override
	public int getMaxCountPerStack() {
		return 1;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		if (slot != 0) {
			return false;
		} else {
			return stack.getItem() instanceof BannerItem || stack.isEmpty();
		}
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Direction getDirection() {
		return direction;
	}

	@Override
	public void fromTag(BlockState state, CompoundTag tag) {
		if (tag.contains("item", 10)) {
			banner = ItemStack.fromTag(tag.getCompound("item"));
		}
		if (tag.contains("direction")) {
			direction = Direction.fromRotation(tag.getFloat("direction"));
		}
		super.fromTag(state, tag);
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag.put("item", banner.toTag(new CompoundTag()));
		tag.putFloat("direction", direction.asRotation());
		return super.toTag(tag);
	}

	@Override
	public void fromClientTag(CompoundTag compoundTag) {
		this.fromTag(null, compoundTag);
	}

	@Override
	public CompoundTag toClientTag(CompoundTag compoundTag) {
		return toTag(compoundTag);
	}
}
