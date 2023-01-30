package nz.carso.the_toolkits;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import nz.carso.the_toolkits.messages.AbstractMessage;
import nz.carso.the_toolkits.messages.MessageSendChatToAllPlayer;

import static nz.carso.the_toolkits.Constants.PROTOCOL_VERSION;

public class TheToolkitsPacketHandler {
    static int messageID = 0;
    protected static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init()
    {
        registerMessage(MessageSendChatToAllPlayer.class, new MessageSendChatToAllPlayer());
    }

    public static<T> void registerMessage(Class<T> cls, AbstractMessage<T> msg) {
        INSTANCE.registerMessage(messageID, cls, msg::write, msg::read, msg::handle);
    }

    public static<T> void sendMessage(AbstractMessage<T> msg) {
        INSTANCE.sendToServer(msg);
    }

}
