package nl.theepicblock.flagbric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Flagbric implements ModInitializer {
	public static Block FLAG_BLOCK = new FlagBlock(FabricBlockSettings.of(Material.WOOD));
	public static BlockEntityType<FlagBlockEntity> FLAG_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier("flagbric", "flag_block"), FLAG_BLOCK);
		Registry.register(Registry.ITEM, new Identifier("flagbric", "flag_block"), new FlagItem(FLAG_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));

		FLAG_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "flagbric:flag_block_entity", BlockEntityType.Builder.create(FlagBlockEntity::new, FLAG_BLOCK).build(null));
	}
}
