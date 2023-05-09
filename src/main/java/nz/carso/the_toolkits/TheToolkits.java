package nz.carso.the_toolkits;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MOD_ID)
public class TheToolkits
{
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "1";

    private static Boolean jeiAvailable = null;


    public TheToolkits()
    {
        LOGGER.info("init");
        TheToolkitsPacketHandler.init();
        TheToolkitsEventHandler.init();
    }

    public static boolean isJEIAvailable() {
        if (jeiAvailable == null) {
            jeiAvailable = ModList.get().isLoaded("jei");
        }
        return jeiAvailable;
    }


}
