package nz.carso.the_toolkits.messages;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageLinkItem implements AbstractMessage<MessageLinkItem> {
    ItemStack itemStack;

    public MessageLinkItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public MessageLinkItem() {
    }

    @Override
    public void write(MessageLinkItem msg, PacketBuffer fb)
    {
        fb.writeItemStack(msg.itemStack, false);
    }

    @Override
    public MessageLinkItem read(PacketBuffer fb)
    {
        return new MessageLinkItem(fb.readItem());
    }

    @Override
    public void handle(MessageLinkItem msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity sender = ctx.get().getSender();
            if (sender != null) {
                PlayerList players = sender.server.getPlayerList();

                IFormattableTextComponent component = new StringTextComponent(sender.getDisplayName().getString() + " just referenced a thing: ");
                IFormattableTextComponent comp = (IFormattableTextComponent) msg.itemStack.getDisplayName();
                ResourceLocation location = msg.itemStack.getItem().getRegistryName();
// only add click event if registry name is found
                if (location != null) {
                    Style style = comp.getStyle();
                    style = style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            String.format("/the-toolkits jei \"@%s &%s\"", location.getNamespace(), location.getPath())));
                    comp = comp.setStyle(style);
                }
                component.append(comp);
                players.broadcastMessage(component, ChatType.SYSTEM, Util.NIL_UUID);

            }

        });
        ctx.get().setPacketHandled(true);
    }

}