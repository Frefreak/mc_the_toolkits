package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;
import nz.carso.the_toolkits.Constants;
import nz.carso.the_toolkits.TheToolkits;
import nz.carso.the_toolkits.TheToolkitsEventHandler;
import nz.carso.the_toolkits.compat.jei.TheToolkitsJEI;
import net.minecraft.network.chat.Component;

import org.slf4j.Logger;


public class JEISearchItemCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("jei")
            .then(
                Commands.argument("text", StringArgumentType.string())
                    .executes(ctx -> {

                        if (FMLEnvironment.dist != Dist.CLIENT) {
                            return 1;
                        }
                        if (TheToolkits.isJEIAvailable()) {
                            Entity entity = ctx.getSource().getEntity();
                            if (entity instanceof LocalPlayer player) {
                                TheToolkitsJEI.doSearch(player, StringArgumentType.getString(ctx, "text"));
                            }
                        } else {
                            ctx.getSource().sendFailure(
                                new TranslatableComponent("commands."+Constants.MOD_ID+".jei_not_available")
                            );
                        }
                        return 1;
                    })
            );
    }
}
