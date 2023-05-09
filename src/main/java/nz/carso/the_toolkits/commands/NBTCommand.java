package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class NBTCommand {

    private static final SuggestionProvider<CommandSource> SUGGEST_OP = (ctx, builder) -> {
        builder.suggest("get");
        return builder.buildFuture();
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("nbt").requires(c -> c.hasPermission(2))
            .then(
                Commands.argument("op", StringArgumentType.string()).suggests(SUGGEST_OP)
                    .executes(ctx -> {
                        PlayerEntity p = ctx.getSource().getPlayerOrException();
                        String op = StringArgumentType.getString(ctx, "op");
                        if (op.equals("get")) {
                            getCurrentItemNBT(p);
                            return 1;
                        } else {
                            p.sendMessage(new TranslationTextComponent("current only get is supported"), Util.NIL_UUID);
                            return 0;
                        }
                    })
            );
    }

    private static void getCurrentItemNBT(PlayerEntity p) {
        ItemStack is = p.getItemInHand(Hand.MAIN_HAND);
        ITextComponent name = is.getDisplayName();
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey(is.getItem());
        CompoundNBT tag = is.getTag();
        IFormattableTextComponent msg = (IFormattableTextComponent)name;
        if (tag == null || resourceLocation == null) {
            msg = new TranslationTextComponent("null tag or resource location");
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
        p.sendMessage(msg, Util.NIL_UUID);
    }
}
