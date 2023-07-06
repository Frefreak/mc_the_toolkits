package nz.carso.the_toolkits.messages;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import nz.carso.the_toolkits.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
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
    public void write(MessageSaveFile msg, FriendlyByteBuf fb) {
        logger.info(String.format("sending file, path %s, size %d", msg.path, msg.content.length()));
        fb.writeUtf(msg.path);
        byte[] content;
        try {
            content = Utils.compressString(msg.content);
            fb.writeByte(0); // gzip compressed []byte
            logger.info("after compression, size is " + content.length);
            fb.writeBytes(content);
        } catch (IOException e) {
            e.printStackTrace();
            fb.writeByte(1); // original utf8 string
            fb.writeUtf(Utils.getStackTraceAsString(e));
        }
    }

    @Override
    public MessageSaveFile read(FriendlyByteBuf fb)
    {
        String path = fb.readUtf();
        byte type = fb.readByte();
        if (type == 0) {
            byte[] content = new byte[fb.readableBytes()];
            fb.readBytes(content);
            String realContent = "";
            try {
                realContent = Utils.decompressString(content);
            } catch (IOException e) {
                e.printStackTrace();
                realContent = Utils.getStackTraceAsString(e);
            }
            return new MessageSaveFile(path, realContent);
        } else if (type == 1) {
            String content = fb.readUtf();
            return new MessageSaveFile(path, content);
        } else {
            logger.error("invalid format");
            return new MessageSaveFile(path, "");
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(MessageSaveFile msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                Utils.saveFile(msg.content, msg.path);
                player.sendSystemMessage(Component.literal(String.format("filename saved: %s", msg.path)));
            }

        });
        ctx.get().setPacketHandled(true);
    }

}