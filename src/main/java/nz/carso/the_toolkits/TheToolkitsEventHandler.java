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
import net.minecraftforge.fml.common.Mod;
import nz.carso.the_toolkits.compat.jei.TheToolkitsJEI;
import nz.carso.the_toolkits.messages.MessageLinkItem;
import org.slf4j.Logger;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SLASH;

public class TheToolkitsEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    static KeyMapping whatKey = new KeyMapping("key." + Constants.MOD_ID + ".whatkey", KeyConflictContext.GUI, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW_KEY_SLASH, "key.categories." + Constants.MOD_ID);

    public static void init() {
        ClientRegistry.registerKeyBinding(whatKey);
    }


    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientOnlyForgeEventHandler {
        @SubscribeEvent
        public static void keyPress(ScreenEvent.KeyboardKeyPressedEvent.Post event)
        {
            if (!whatKey.consumeClick()) {
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