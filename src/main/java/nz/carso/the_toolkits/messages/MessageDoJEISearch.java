package nz.carso.the_toolkits.messages;


import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nz.carso.the_toolkits.compat.jei.TheToolkitsJEI;

import java.util.function.Supplier;

public class MessageDoJEISearch implements AbstractMessage<MessageDoJEISearch> {
    String text;

    public MessageDoJEISearch() {}
    public MessageDoJEISearch(String text) {
        this.text = text;
    }


    @Override
    public void write(MessageDoJEISearch msg, PacketBuffer fb) {
        fb.writeUtf(msg.text);
    }

    @Override
    public MessageDoJEISearch read(PacketBuffer fb) {
        return new MessageDoJEISearch(fb.readUtf());
    }

    @Override
    public void handle(MessageDoJEISearch msg, Supplier<NetworkEvent.Context> ctx) {
        TheToolkitsJEI.doSearch(msg.text);
    }
}
