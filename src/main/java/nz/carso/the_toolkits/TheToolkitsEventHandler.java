package nz.carso.the_toolkits;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import nz.carso.the_toolkits.compat.jei.TheToolkitsJEI;
import nz.carso.the_toolkits.messages.MessageLinkItem;
import org.slf4j.Logger;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SLASH;

public class TheToolkitsEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientOnlyForgeEventHandler.init();
        }
    }


    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientOnlyForgeEventHandler {
        static KeyMapping linkKey = new KeyMapping("key." + Constants.MOD_ID + ".link_key", KeyConflictContext.GUI, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW_KEY_SLASH, "key.categories." + Constants.MOD_ID);
        public static void init() {
            ClientRegistry.registerKeyBinding(linkKey);
        }
        @SubscribeEvent
        public static void keyPress(ScreenEvent.KeyboardKeyPressedEvent.Post event)
        {
            if (!linkKey.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
                return;
            }
            if (TheToolkits.isJEIAvailable()) {
                ItemStack is = TheToolkitsJEI.getItemStackUnderMouse();
                if (is == null) {
                    return;
                }
                TheToolkitsPacketHandler.INSTANCE.sendToServer(new MessageLinkItem(is));
            }

        }
    }
}
