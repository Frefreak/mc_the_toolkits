package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestCommand {
    static final Logger logger = LogManager.getLogger();
    static int idx = 0;

    private static final SuggestionProvider<CommandSource> SUGGEST_OP = (ctx, builder) -> {
        builder.suggest("get");
        return builder.buildFuture();
    };

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("test").requires(c -> c.hasPermission(2))
            .executes(ctx -> {
                if (ctx.getSource().getEntity() instanceof ServerPlayerEntity) {
                    ServerPlayerEntity player = (ServerPlayerEntity) ctx.getSource().getEntity();
                    ServerWorld lvl = player.getLevel();
                    RecipeManager mgr = lvl.getRecipeManager();
                    for (ResourceLocation key : ForgeRegistries.RECIPE_SERIALIZERS.getKeys()) {
                        logger.info(key);
                    }

                    ResourceLocation mechanicalCraftingType = new ResourceLocation("create", "mechanical_crafting");
                    //ResourceLocation mechanicalCraftingType = new ResourceLocation("minecraft", "crafting_shaped");

                    List<IRecipe<?>> mechanicalCraftingRecipes = mgr.getRecipes().stream()
                            .filter(recipe -> Objects.equals(recipe.getSerializer().getRegistryName(), mechanicalCraftingType))
                            .collect(Collectors.toList());

                    logger.info(mechanicalCraftingRecipes.size());
                    logger.info("---------------");
                    logger.info(mechanicalCraftingRecipes.get(idx).toString());
                    logger.info(mechanicalCraftingRecipes.get(idx).getType());
                    logger.info(mechanicalCraftingRecipes.get(idx).getGroup());
                    logger.info(mechanicalCraftingRecipes.get(idx).getId());
                    NonNullList<Ingredient> ingredients = mechanicalCraftingRecipes.get(idx).getIngredients();
                    logger.info(ingredients);
                    logger.info(ingredients.size());
                    for (Ingredient ingred : ingredients) {
                        logger.info(ingred.getItems()[idx]);
                    }
                    logger.info(mechanicalCraftingRecipes.get(idx).getResultItem());
                    logger.info(mechanicalCraftingRecipes.get(idx).getSerializer().getRegistryName());
                    idx = (idx + 1) % mechanicalCraftingRecipes.size();
                }
                return 1;
            });
    }

}
