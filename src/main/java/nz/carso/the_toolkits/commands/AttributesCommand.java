package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.function.Consumer;

public class AttributesCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_OP = (ctx, builder) -> {
        builder.suggest("get");
        return builder.buildFuture();
    };

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("attributes").requires(c -> c.hasPermission(2))
                .then(
                        Commands.argument("op", StringArgumentType.string()).suggests(SUGGEST_OP)
                                .executes(ctx -> {
                                    Player p = ctx.getSource().getPlayerOrException();
                                    String op = StringArgumentType.getString(ctx, "op");
                                    if (op.equals("get")) {
                                        getCurrentAttributes(p);
                                        return 1;
                                    } else {
                                        p.sendMessage(new TextComponent("current only get is supported"), Util.NIL_UUID);
                                        return 0;
                                    }
                                })
                );
    }

    private static void getCurrentAttributes(Player p) {
        AttributeMap attributes = p.getAttributes();
        Collection<AttributeInstance> syncableAttributes = attributes.getSyncableAttributes();
        Collection<AttributeInstance> dirtyAttributes = attributes.getSyncableAttributes();
        StringBuilder msg = new StringBuilder();
        msg.append("Syncable Attributes:\n");
        syncableAttributes.forEach((consumer) -> {
            ResourceLocation name = consumer.getAttribute().getRegistryName();
            double value = consumer.getValue();
            msg.append(name).append(": ").append(value).append("\n");
        });

        msg.append('\n');

        msg.append("Dirty Attributes:\n");
        dirtyAttributes.forEach((consumer) -> {
            ResourceLocation name = consumer.getAttribute().getRegistryName();
            double value = consumer.getValue();
            msg.append(name).append(": ").append(value).append("\n");
        });
        String msgString = msg.toString();
        MutableComponent mc = new TextComponent(msgString);
        Style style = mc.getStyle();
        style = style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, msgString));
        mc.setStyle(style);
        p.sendMessage(mc, Util.NIL_UUID);
    }
}
