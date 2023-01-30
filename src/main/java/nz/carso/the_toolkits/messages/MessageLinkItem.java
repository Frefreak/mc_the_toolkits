package nz.carso.the_toolkits.messages;

import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

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
                ServerLevel level = sender.getLevel();
                List<ServerPlayer> players = level.getPlayers((p) -> true);
                for (ServerPlayer player : players) {
                    TranslatableComponent component = new TranslatableComponent(((TextComponent)sender.getName()).getText() + " just referenced a thing: ");
                    Item item = msg.itemStack.getItem();
                    TranslatableComponent itemText = new TranslatableComponent(item.getDescriptionId());
                    Style style = Style.EMPTY
                            .withColor(TextColor.fromLegacyFormat(ChatFormatting.GREEN))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(msg.itemStack)));
                    itemText.setStyle(style);
                    component.append(itemText);
                    player.displayClientMessage(component, false);
                }
            }

        });
        ctx.get().setPacketHandled(true);
    }

}