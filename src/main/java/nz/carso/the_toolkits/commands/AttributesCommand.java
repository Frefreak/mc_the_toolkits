package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.registries.ForgeRegistries;



import java.util.Collection;

public class AttributesCommand {

    private static final SuggestionProvider<CommandSource> SUGGEST_OP = (ctx, builder) -> {
        builder.suggest("get");
        return builder.buildFuture();
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("attributes").requires(c -> c.hasPermission(2))
                .then(
                        Commands.argument("op", StringArgumentType.string()).suggests(SUGGEST_OP)
                                .executes(ctx -> {
                                    PlayerEntity p = ctx.getSource().getPlayerOrException();
                                    String op = StringArgumentType.getString(ctx, "op");
                                    if (op.equals("get")) {
                                        getCurrentAttributes(p);
                                        return 1;
                                    } else {
                                        p.sendMessage(new StringTextComponent("current only get is supported"), Util.NIL_UUID);
                                        return 0;
                                    }
                                })
                );
    }

    private static void getCurrentAttributes(PlayerEntity p) {
        AttributeModifierManager attributes = p.getAttributes();
        Collection<ModifiableAttributeInstance> syncableAttributes = attributes.getSyncableAttributes();
        Collection<ModifiableAttributeInstance> dirtyAttributes = attributes.getSyncableAttributes();
        StringBuilder msg = new StringBuilder();
        msg.append("Syncable Attributes:\n");
        syncableAttributes.forEach((consumer) -> {
            ResourceLocation name = ForgeRegistries.ATTRIBUTES.getKey(consumer.getAttribute());
            double value = consumer.getValue();
            msg.append(name).append(": ").append(value).append("\n");
        });

        msg.append('\n');

        msg.append("Dirty Attributes:\n");
        dirtyAttributes.forEach((consumer) -> {
            ResourceLocation name = ForgeRegistries.ATTRIBUTES.getKey(consumer.getAttribute());
            double value = consumer.getValue();
            msg.append(name).append(": ").append(value).append("\n");
        });
        String msgString = msg.toString();
        StringTextComponent mc = new StringTextComponent(msgString);
        Style style = mc.getStyle();
        style = style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, msgString));
        mc.setStyle(style);
        p.sendMessage(mc, Util.NIL_UUID);
    }
}
