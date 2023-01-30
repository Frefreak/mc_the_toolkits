package nz.carso.the_toolkits.messages;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class MessageSendChatToAllPlayer implements AbstractMessage<MessageSendChatToAllPlayer> {
    String content;
    public MessageSendChatToAllPlayer(String content) {
        this.content = content;
    }

    public MessageSendChatToAllPlayer() {
    }

    @Override
    public void write(MessageSendChatToAllPlayer msg, FriendlyByteBuf fb)
    {
        fb.writeUtf(msg.content);
    }

    @Override
    public MessageSendChatToAllPlayer read(FriendlyByteBuf fb)
    {
        return new MessageSendChatToAllPlayer(fb.readUtf());
    }

    @Override
    public void handle(MessageSendChatToAllPlayer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                ServerLevel level = sender.getLevel();
                List<ServerPlayer> players = level.getPlayers((p) -> true);
                for (ServerPlayer player : players) {
                    TranslatableComponent component = new TranslatableComponent(((TextComponent)sender.getName()).getText() + " just referenced a thing: ");
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft:dirt"));
                    if (item != null) {
                        Style style = Style.EMPTY
                                .withColor(TextColor.fromLegacyFormat(ChatFormatting.RED));
                        TranslatableComponent itemText = new TranslatableComponent(item.getDescriptionId());
                        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(new ItemStack(item, 1))));
                        itemText.setStyle(style);
                        component.append(itemText);
                    }
                    player.displayClientMessage(component, false);
                }
            }

        });
        ctx.get().setPacketHandled(true);
    }

}