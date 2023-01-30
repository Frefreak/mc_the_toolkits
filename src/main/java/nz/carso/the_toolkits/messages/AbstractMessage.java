package nz.carso.the_toolkits.messages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// TODO: find a way to use static?
public interface AbstractMessage<T> {

    void write(T msg, FriendlyByteBuf fb);

    T read(FriendlyByteBuf fb);

    void handle(T msg, Supplier<NetworkEvent.Context> ctx);
}
