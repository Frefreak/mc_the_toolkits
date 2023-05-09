package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RecipeCommand {
    static final Logger logger = LogManager.getLogger();
    static HashMap<String, HashMap<String, List<IRecipe<?>>>> recipes = new HashMap<>();
    private static final SuggestionProvider<CommandSource> SUGGEST_NS = (ctx, builder) -> {
        if (check(ctx) == null) {
            return builder.buildFuture();
        }
        for (String key : recipes.keySet()) {
            logger.info(key);
            builder.suggest(key);
        }
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSource> SUGGEST_PATH = (ctx, builder) -> {
        String ns = StringArgumentType.getString(ctx, "ns");
        if (check(ctx) == null) {
            return builder.buildFuture();
        }
        HashMap<String, List<IRecipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
        for (String key : map.keySet()) {
            builder.suggest(key);
        }
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSource> SUGGEST_OP = (ctx, builder) -> {
        builder.suggest("list");
        builder.suggest("count");
        builder.suggest("print");
        builder.suggest("dump");
        return builder.buildFuture();
    };
    private static void initRecipes(RecipeManager mgr) {
        int count = 0;
        for (IRecipe<?> recipe : mgr.getRecipes()) {
            ResourceLocation type = recipe.getSerializer().getRegistryName();
            if (type == null) {
                continue;
            }
            String ns = type.getNamespace();
            String path = type.getPath();
            if (recipes.containsKey(ns)) {
                HashMap<String, List<IRecipe<?>>> submap = recipes.get(ns);
                if (submap.containsKey(path)) {
                    submap.get(path).add(recipe);
                } else {
                    List<IRecipe<?>> newList = new ArrayList<>();
                    newList.add(recipe);
                    submap.put(path, newList);
                }
            } else {
                List<IRecipe<?>> newList = new ArrayList<>();
                newList.add(recipe);
                HashMap<String, List<IRecipe<?>>> submap = new HashMap<>();
                submap.put(path, newList);
                recipes.put(ns, submap);
            }
            count += 1;
        }
        logger.info("{} recipes populated", count);
    }
    private static ServerPlayerEntity check(CommandContext<CommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity)) {
            return null;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntity();
        ServerWorld lvl = player.getLevel();
        RecipeManager mgr = lvl.getRecipeManager();
        if (recipes.isEmpty()) {
            logger.info("populating recipes");
            RecipeCommand.initRecipes(mgr);
        }
        return player;
    }

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("recipe")
            .then(Commands.literal("list").executes(ctx -> {
                ServerPlayerEntity player = check(ctx);
                if (player == null) {
                    return 0;
                }
                StringBuilder builder = new StringBuilder();
                for (String key: recipes.keySet()) {
                    builder.append(key).append('\n');
                }
                player.sendMessage(new StringTextComponent(builder.toString()), Util.NIL_UUID);
                return 1;
            }))
            .then(Commands.argument("ns", StringArgumentType.string()).suggests(SUGGEST_NS)
                .then(Commands.literal("list").executes(ctx-> {
                    ServerPlayerEntity player = check(ctx);
                    if (player == null) {
                        return 0;
                    }
                    String ns = StringArgumentType.getString(ctx, "ns");

                    HashMap<String, List<IRecipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
                    StringBuilder builder = new StringBuilder();
                    for (String key: map.keySet()) {
                        builder.append(key).append('\n');
                    }
                    player.sendMessage(new StringTextComponent(builder.toString()), Util.NIL_UUID);
                    return 1;

                }))
                .then(Commands.argument("path", StringArgumentType.string()).suggests(SUGGEST_PATH)
                    .then(Commands.argument("op", StringArgumentType.string()).suggests(SUGGEST_OP)
                        .executes(ctx -> {
                            ServerPlayerEntity player = check(ctx);
                            if (player == null) {
                                return 0;
                            }

                            return 1;
                        })
                )));
    }

}
