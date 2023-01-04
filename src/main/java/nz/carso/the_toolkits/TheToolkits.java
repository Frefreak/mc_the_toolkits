package nz.carso.the_toolkits;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.slf4j.Logger;


@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TheToolkits
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    public static final RegistryObject<Block> ROCK_BLOCK = BLOCKS.register("rock",
            () -> {
                LOGGER.info("registering rock");
                return new Block(BlockBehaviour.Properties.of(Material.STONE));
            }
    );
    //public static final RegistryObject<Item> ROCK_ITEM = ITEMS.register("rock",
    //        () -> {
    //            LOGGER.info("registering rock item");
    //            return new BlockItem(ROCK_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS));
    //        }
    //);
    public TheToolkits()
    {
        LOGGER.info("registering");
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public static void asdf(final RegistryEvent.Register<Item> event) {
        LOGGER.info("asdf");
        IForgeRegistry<Item> reg = event.getRegistry();
        Block blk = ROCK_BLOCK.get();
        if (blk.getRegistryName() != null) {
            LOGGER.info("Registering rock BlockItem");
            BlockItem item = new BlockItem(blk, new Item.Properties());
            item.setRegistryName(blk.getRegistryName());
            reg.register(item);
        }
    }
}
