package nz.carso.the_toolkits;

import com.mojang.logging.LogUtils;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.slf4j.Logger;

@Mod(Constants.MOD_ID)
public class TheToolkits
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROTOCOL_VERSION = "1";

    public static IJeiRuntime jeiRuntime;

    public TheToolkits()
    {
        LOGGER.info("init");
        TheToolkitsPacketHandler.init();
        TheToolkitsEventHandler.init();
    }
}
