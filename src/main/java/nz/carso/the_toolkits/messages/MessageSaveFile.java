package nz.carso.the_toolkits.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import nz.carso.the_toolkits.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class MessageSaveFile implements AbstractMessage<MessageSaveFile> {
    // file path without folder name
    public String path;
    public String content;

    public static final Logger logger = LogManager.getLogger();

    public MessageSaveFile(String path, String content) {
        this.path = path;
        this.content = content;
    }
    public MessageSaveFile() {
    }

    @Override
    public void write(MessageSaveFile msg, PacketBuffer fb)
    {
        fb.writeUtf(msg.path);
        fb.writeUtf(msg.content);
    }

    @Override
    public MessageSaveFile read(PacketBuffer fb)
    {
        return new MessageSaveFile(fb.readUtf(), fb.readUtf());
    }

    @Override
    public void handle(MessageSaveFile msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = Minecraft.getInstance().player;
            if (player != null) {
                Utils.saveFile(msg.content, msg.path);
                player.sendMessage(new TranslationTextComponent(String.format("filename saved: %s", msg.path)), Util.NIL_UUID);
            }

        });
        ctx.get().setPacketHandled(true);
    }

}