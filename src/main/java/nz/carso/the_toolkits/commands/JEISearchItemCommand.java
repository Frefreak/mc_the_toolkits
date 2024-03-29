package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import nz.carso.the_toolkits.Constants;
import nz.carso.the_toolkits.TheToolkits;
import nz.carso.the_toolkits.TheToolkitsPacketHandler;
import net.minecraft.network.chat.Component;

import nz.carso.the_toolkits.messages.MessageDoJEISearch;
import org.slf4j.Logger;


public class JEISearchItemCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("jei")
            .then(
                Commands.argument("text", StringArgumentType.string())
                    .executes(ctx -> {
                        if (TheToolkits.isJEIAvailable()) {
                            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                String text = StringArgumentType.getString(ctx, "text");
                                TheToolkitsPacketHandler.sendTo(PacketDistributor.PLAYER.with(() -> player), new MessageDoJEISearch(text));
                            }
                        } else {
                            ctx.getSource().sendFailure(
                                Component.translatable("commands."+Constants.MOD_ID+".jei_not_available")
                            );
                        }
                        return 1;
                    })
            );
    }
}
