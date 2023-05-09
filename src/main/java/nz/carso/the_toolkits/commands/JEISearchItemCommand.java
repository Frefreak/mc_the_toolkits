package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import nz.carso.the_toolkits.Constants;
import nz.carso.the_toolkits.TheToolkits;
import nz.carso.the_toolkits.TheToolkitsPacketHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import nz.carso.the_toolkits.messages.MessageDoJEISearch;


public class JEISearchItemCommand {
    private static final Logger LOGGER = LogManager.getLogger();

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("jei")
            .then(
                Commands.argument("text", StringArgumentType.string())
                    .executes(ctx -> {
                        if (TheToolkits.isJEIAvailable()) {
                            if (ctx.getSource().getEntity() instanceof ServerPlayerEntity) {
                                ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntity();
                                String text = StringArgumentType.getString(ctx, "text");
                                TheToolkitsPacketHandler.sendTo(PacketDistributor.PLAYER.with(() -> player), new MessageDoJEISearch(text));
                            }
                        } else {
                            ctx.getSource().sendFailure(
                                new TranslationTextComponent("commands."+Constants.MOD_ID+".jei_not_available")
                            );
                        }
                        return 1;
                    })
            );
    }
}
