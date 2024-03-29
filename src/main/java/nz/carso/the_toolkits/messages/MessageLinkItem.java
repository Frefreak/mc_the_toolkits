package nz.carso.the_toolkits.messages;

import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.Supplier;

public class MessageLinkItem implements AbstractMessage<MessageLinkItem> {
    ItemStack itemStack;

    public MessageLinkItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public MessageLinkItem() {
    }

    @Override
    public void write(MessageLinkItem msg, FriendlyByteBuf fb)
    {
        fb.writeItemStack(msg.itemStack, false);
    }

    @Override
    public MessageLinkItem read(FriendlyByteBuf fb)
    {
        return new MessageLinkItem(fb.readItem());
    }

    @Override
    public void handle(MessageLinkItem msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                PlayerList players = sender.server.getPlayerList();

                MutableComponent component = Component.translatable(sender.getName().getString() + " just referenced a thing: ");
                MutableComponent comp = (MutableComponent) msg.itemStack.getDisplayName();
                ResourceLocation location = ForgeRegistries.ITEMS.getKey(msg.itemStack.getItem());
                // only add click event if registry name is found
                if (location != null) {
                    Style style = comp.getStyle();
                    style = style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/the-toolkits jei \"@%s &%s\"".formatted(location.getNamespace(), location.getPath())));
                    comp.setStyle(style);
                }
                component.append(comp);
                players.broadcastSystemMessage(component, false);
            }

        });
        ctx.get().setPacketHandled(true);
    }

}