package nz.carso.the_toolkits.messages;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import nz.carso.the_toolkits.Constants;
import nz.carso.the_toolkits.Utils;
import nz.carso.the_toolkits.commands.RecipeCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageDumpRecipe implements AbstractMessage<MessageDumpRecipe> {
    private final Logger logger = LogManager.getLogger();
    String namespace;
    String path;

    public MessageDumpRecipe() {
    }

    public MessageDumpRecipe(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }


    @Override
    public void write(MessageDumpRecipe msg, FriendlyByteBuf fb) {
        fb.writeUtf(msg.namespace);
        fb.writeUtf(msg.path);
    }

    @Override
    public MessageDumpRecipe read(FriendlyByteBuf fb) {
        return new MessageDumpRecipe(fb.readUtf(), fb.readUtf());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handle(MessageDumpRecipe msg, Supplier<NetworkEvent.Context> ctx) {
        LocalPlayer entityPlayer = Minecraft.getInstance().player;
        if (entityPlayer == null) {
            return;
        }


        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            logger.warn("got null level");
            return;
        }
        RecipeManager mgr = level.getRecipeManager();
        if (RecipeCommand.recipes.isEmpty()) {
            logger.info("populating recipes");
            RecipeCommand.initRecipes(mgr);
        }
        String filename = String.format("%s-%s.json", msg.namespace, msg.path);
        HashMap<String, List<Recipe<?>>> submap = RecipeCommand.recipes.getOrDefault(msg.namespace, new HashMap<>());
        List<Recipe<?>> recipes = submap.getOrDefault(msg.path, new ArrayList<>());
        List<RecipeCommand.MaterializedRecipe> mrs = recipes.stream().map(RecipeCommand::materialize).toList();
        JsonArray result = new JsonArray();
        List<String> failed = new ArrayList<>();

        entityPlayer.sendMessage(new TextComponent(String.format("%d recipes before transform", mrs.size())), Util.NIL_UUID);
        for (RecipeCommand.MaterializedRecipe mr: mrs) {
            try {
                JsonElement tree = RecipeCommand.toJSONTree(mr);
                result.add(tree);
            } catch (Exception | AssertionError e) {
                failed.add(mr.id.toString());
            }
        }
        entityPlayer.sendMessage(new TextComponent(String.format("got %d entries after transformation", result.size())), Util.NIL_UUID);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String content = gson.toJson(result);
        ctx.get().enqueueWork(() -> {
            if (failed.size() > 0) {
                entityPlayer.sendMessage(new TextComponent(
                        String.format("there are §c%d§r failed recipes during conversion, see logs for a list (maximum 10)," +
                                        " and use printj to try to pinpoint the issue",
                                failed.size())), Util.NIL_UUID);
                StringBuilder sb = new StringBuilder();
                for (String f : failed.subList(0, Math.min(10, failed.size()))) {
                    sb.append(f);
                    sb.append('\n');
                }
                logger.warn(sb.toString());
            }
            Boolean ok = Utils.saveFile(content, filename);
            if (ok) {
                entityPlayer.sendMessage(new TextComponent(
                                String.format("saved success: §a%s/%s-%s.json§r", Constants.MOD_ID, msg.namespace, msg.path)),
                        Util.NIL_UUID);
            } else {
                entityPlayer.sendMessage(new TextComponent("save process failed, see log for more info"),
                        Util.NIL_UUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

