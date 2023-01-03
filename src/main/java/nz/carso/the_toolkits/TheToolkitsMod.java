package nz.carso.the_toolkits;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;


@Mod("the_toolkits")
public class TheToolkitsMod
{
    public static final String ModID = "the_toolkits";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModID);
    public static final RegistryObject<Block> ROCK_BLOCK = BLOCKS.register("rock",
            () -> new Block(BlockBehaviour.Properties.of(Material.STONE))
    );

    public static final CreativeModeTab group = null;

    public TheToolkitsMod()
    {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(new Block(BlockBehaviour.Properties.of(Material.AMETHYST)));
    }
}
