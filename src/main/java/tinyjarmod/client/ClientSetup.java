package tinyjarmod.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import tinyjarmod.client.render.TinyJarRenderer;

import static tinyjarmod.TinyJarMod.*;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public  class ClientSetup
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(()->
                    ItemBlockRenderTypes.setRenderLayer(TINY_JAR.get(), RenderType.cutout())
            );
            event.enqueueWork(() ->
                    BlockEntityRenderers.register(TINY_JAR_ENTITY.get(), TinyJarRenderer::new)
            );
        }

        @SubscribeEvent
        public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
            event.register(new ResourceLocation("tinyjarmod", "item/tiny_jar_shell"));
    }

}