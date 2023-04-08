package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class NBTCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_OP = (ctx, builder) -> {
        builder.suggest("get");
        return builder.buildFuture();
    };

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("nbt").requires(c -> c.hasPermission(2))
            .then(
                Commands.argument("op", StringArgumentType.string()).suggests(SUGGEST_OP)
                    .executes(ctx -> {
                        Player p = ctx.getSource().getPlayerOrException();
                        String op = StringArgumentType.getString(ctx, "op");
                        if (op.equals("get")) {
                            getCurrentItemNBT(p);
                            return 1;
                        } else {
                            p.sendSystemMessage(Component.literal("current only get is supported"));
                            return 0;
                        }
                    })
            );
    }

    private static void getCurrentItemNBT(Player p) {
        ItemStack is = p.getItemInHand(InteractionHand.MAIN_HAND);
        Component name = is.getDisplayName();
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey(is.getItem());
        CompoundTag tag = is.getTag();
        MutableComponent msg = (MutableComponent)(name);
        if (tag == null || resourceLocation == null) {
            msg = Component.literal("null tag or resource location");
        } else {
            msg.append("\n");
            msg.append(resourceLocation.toString());
            msg.append("\n");
            String tagString = tag.toString();
            msg.append(tagString);
            Style style = msg.getStyle();
            style = style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, tagString));
            msg.setStyle(style);
        }
        p.sendSystemMessage(msg);
    }
}
