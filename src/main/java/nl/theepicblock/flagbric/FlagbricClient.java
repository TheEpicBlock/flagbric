package nl.theepicblock.flagbric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;

public class FlagbricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockEntityRendererRegistry.INSTANCE.register(Flagbric.FLAG_BLOCK_ENTITY, FlagBlockRenderer::new);
	}
}
