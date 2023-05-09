package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.crafting.RecipeManager;

public class TestCommand {

    private static final SuggestionProvider<CommandSource> SUGGEST_OP = (ctx, builder) -> {
        builder.suggest("get");
        return builder.buildFuture();
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("test").requires(c -> c.hasPermission(2))
            .executes(ctx -> {
                return 1;
            });
    }

}
