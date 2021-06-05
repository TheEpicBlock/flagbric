package nl.theepicblock.flagbric;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class FlagBlockEntity extends BlockEntity implements Inventory,BlockEntityClientSerializable {
	private ItemStack banner = ItemStack.EMPTY;
	private Direction direction = Direction.NORTH;

	public FlagBlockEntity(BlockPos pos, BlockState state) {
		super(Flagbric.FLAG_BLOCK_ENTITY, pos, state);
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
	public void readNbt(NbtCompound tag) {
		if (tag.contains("item", 10)) {
			banner = ItemStack.fromNbt(tag.getCompound("item"));
		}
		if (tag.contains("direction")) {
			direction = Direction.fromRotation(tag.getFloat("direction"));
		}
		super.readNbt(tag);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		tag.put("item", banner.writeNbt(new NbtCompound()));
		tag.putFloat("direction", direction.asRotation());
		return super.writeNbt(tag);
	}

	@Override
	public void fromClientTag(NbtCompound compoundTag) {
		this.readNbt(compoundTag);
	}

	@Override
	public NbtCompound toClientTag(NbtCompound compoundTag) {
		return writeNbt(compoundTag);
	}
}
