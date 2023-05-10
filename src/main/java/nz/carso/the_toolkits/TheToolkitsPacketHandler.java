package nz.carso.the_toolkits;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import nz.carso.the_toolkits.messages.AbstractMessage;
import nz.carso.the_toolkits.messages.MessageDoJEISearch;
import nz.carso.the_toolkits.messages.MessageDumpRecipe;
import nz.carso.the_toolkits.messages.MessageLinkItem;

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
        registerMessage(MessageLinkItem.class, new MessageLinkItem());
        registerMessage(MessageDoJEISearch.class, new MessageDoJEISearch());
        registerMessage(MessageDumpRecipe.class, new MessageDumpRecipe());
    }

    public static<T> void registerMessage(Class<T> cls, AbstractMessage<T> msg) {
        INSTANCE.registerMessage(messageID, cls, msg::write, msg::read, msg::handle);
        messageID += 1;
    }

    public static<T> void sendToServer(AbstractMessage<T> msg) {
        INSTANCE.sendToServer(msg);
    }
    public static<T> void sendTo(PacketDistributor.PacketTarget target, AbstractMessage<T> msg) {
        INSTANCE.send(target, msg);
    }

}
