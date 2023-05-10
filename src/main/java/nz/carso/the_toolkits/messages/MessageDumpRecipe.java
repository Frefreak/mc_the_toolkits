package nz.carso.the_toolkits.messages;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import nz.carso.the_toolkits.Constants;
import nz.carso.the_toolkits.Utils;
import nz.carso.the_toolkits.commands.RecipeCommand;
import nz.carso.the_toolkits.compat.jei.TheToolkitsJEI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.entity.player.PlayerEntity;

public class MessageDumpRecipe implements AbstractMessage<MessageDumpRecipe> {
    String namespace;
    String path;

    public MessageDumpRecipe() {
    }

    public MessageDumpRecipe(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }


    @Override
    public void write(MessageDumpRecipe msg, PacketBuffer fb) {
        fb.writeUtf(msg.namespace);
        fb.writeUtf(msg.path);
    }

    @Override
    public MessageDumpRecipe read(PacketBuffer fb) {
        return new MessageDumpRecipe(fb.readUtf(), fb.readUtf());
    }

    @Override
    public void handle(MessageDumpRecipe msg, Supplier<NetworkEvent.Context> ctx) {
        String filename = String.format("%s-%s.json", msg.namespace, msg.path);
        HashMap<String, List<IRecipe<?>>> submap = RecipeCommand.recipes.getOrDefault(msg.namespace, new HashMap<>());
        List<IRecipe<?>> recipes = submap.getOrDefault(msg.path, new ArrayList<>());
        List<RecipeCommand.MaterializedRecipe> mrs = recipes.stream().map(RecipeCommand::materialize).collect(Collectors.toList());
        String content = RecipeCommand.toJSON(mrs);
        ctx.get().enqueueWork(() -> {
            Boolean ok = Utils.saveFile(content, filename);
            PlayerEntity entityPlayer = Minecraft.getInstance().player;
            if (entityPlayer == null) {
                return;
            }
            if (ok) {
                entityPlayer.sendMessage(new StringTextComponent(
                                String.format("saved success: %s/%s-%s.json", Constants.MOD_ID, msg.namespace, msg.path)),
                        Util.NIL_UUID);
            } else {
                entityPlayer.sendMessage(new StringTextComponent("save failed, see log for more info"),
                        Util.NIL_UUID);

            }
        });
        ctx.get().setPacketHandled(true);
    }
}

