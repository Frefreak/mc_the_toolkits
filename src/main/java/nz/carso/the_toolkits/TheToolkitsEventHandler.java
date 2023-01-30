package nz.carso.the_toolkits;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nz.carso.the_toolkits.messages.MessageSendChatToAllPlayer;
import org.slf4j.Logger;

public class TheToolkitsEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientOnlyEventHandler {
        @SubscribeEvent
        public static void chat(ClientChatReceivedEvent evt)
        {
            Component msg = evt.getMessage();
            if (msg instanceof TranslatableComponent comp) {
                if (comp.getArgs().length > 1) {
                    Object str = comp.getArgs()[1];
                    if (str instanceof String text) {
                        if (text.equals("trigger")) {
                            TheToolkitsPacketHandler.sendMessage(new MessageSendChatToAllPlayer(text));
                        }
                    }
                }
            }
        }
    }
}
