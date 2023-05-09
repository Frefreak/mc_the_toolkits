package nz.carso.the_toolkits.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// TODO: find a way to use static?
public interface AbstractMessage<T> {

    void write(T msg, PacketBuffer fb);

    T read(PacketBuffer fb);

    void handle(T msg, Supplier<NetworkEvent.Context> ctx);
}
