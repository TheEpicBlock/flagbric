package nl.theepicblock.flagbric;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import java.util.List;

public class FlagBlockRenderer implements BlockEntityRenderer<FlagBlockEntity> {
	private final ModelPart banner;
	private static final boolean BANNERPP = FabricLoader.getInstance().isModLoaded("bannerpp");

	public FlagBlockRenderer(BlockEntityRendererFactory.Context ctx) {
		ModelPart layerModel = ctx.getLayerModelPart(EntityModelLayers.BANNER);
		this.banner = layerModel.getChild("flag");
	}

	@Override
	public void render(FlagBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		ItemStack stack = entity.getStack(0);
		if (!stack.isEmpty()) {
			if (!(stack.getItem() instanceof BannerItem)) return;

			DyeColor color = ((BannerItem)stack.getItem()).getColor();
			List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.getPatternsFromNbt(color, BannerBlockEntity.getPatternListTag(stack));

			if (BANNERPP) {
				BannerppHandler.onPreRender(stack);
			}

			matrices.push();

			//matrices.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(entity.getDirection().asRotation()));
			switch (entity.getDirection()) {
				case SOUTH -> {
					matrices.translate(1.3, 0, 0D);
				}
				case WEST -> {
					matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90));
					matrices.translate(1.3, 0, -1);
				}
				case EAST -> {
					matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
					matrices.translate(0.3, 0, 0);
				}
				case NORTH -> {
					matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
					matrices.translate(0.3, 0, -1);
				}
			}
			matrices.translate(0.5D, 0.5, 0.5D);
			matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(90));
			matrices.scale(0.6666667F, -0.6666667F, -0.6666667F);

			BlockPos blockPos = entity.getPos();
			long time = entity.getWorld() == null ? 0 : entity.getWorld().getTime();
			float n = ((float)Math.floorMod(blockPos.getX() * 7L + blockPos.getY() * 9L + blockPos.getZ() * 13L + time, 100L) + tickDelta) / 100.0F;
			this.banner.pitch = (-0.0125F + 0.01F * MathHelper.cos(6.2831855F * n)) * 3.1415927F;
			this.banner.pivotY = -32.0F;
			BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, this.banner, ModelLoader.BANNER_BASE, true, list);

			matrices.pop();
		}
	}
}
