package nz.carso.the_toolkits.commands;

import com.google.gson.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.nbt.*;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import nz.carso.the_toolkits.TheToolkitsPacketHandler;
import nz.carso.the_toolkits.messages.MessageDumpRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeCommand {
    static final Logger logger = LogManager.getLogger();
    public static HashMap<String, HashMap<String, List<IRecipe<?>>>> recipes = new HashMap<>();
    private static final SuggestionProvider<CommandSource> SUGGEST_NS = (ctx, builder) -> {
        if (check(ctx) == null) {
            return builder.buildFuture();
        }
        for (String key : recipes.keySet()) {
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

    private static final SuggestionProvider<CommandSource> SUGGEST_RECIPE_ID = (ctx, builder) -> {
        String ns = StringArgumentType.getString(ctx, "ns");
        String path = StringArgumentType.getString(ctx, "path");
        if (check(ctx) == null) {
            return builder.buildFuture();
        }
        HashMap<String, List<IRecipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
        List<IRecipe<?>> recs = map.getOrDefault(path, new ArrayList<>());
        for (IRecipe<?> rec: recs) {
            builder.suggest(String.format("\"%s\"", rec.getId()));
        }
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSource> SUGGEST_OP = (ctx, builder) -> {
        builder.suggest("list");
        builder.suggest("count");
        builder.suggest("random");
        builder.suggest("dump");
        return builder.buildFuture();
    };
    public static void initRecipes(RecipeManager mgr) {
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
            .then(Commands.literal("search")
                .then(Commands.argument("recipe_id", StringArgumentType.string()).executes(ctx -> {
                    ServerPlayerEntity player = check(ctx);
                    if (player == null) {
                        return 0;
                    }
                    String recipeId = StringArgumentType.getString(ctx, "recipe_id");
                    ServerWorld lvl = player.getLevel();
                    RecipeManager mgr = lvl.getRecipeManager();
                    for (IRecipe<?> recipe : mgr.getRecipes()) {
                        if (recipe.getId().toString().equals(recipeId)) {
                            ResourceLocation sid = recipe.getSerializer().getRegistryName();
                            if (sid == null) {
                                player.sendMessage(new StringTextComponent(String.format("failed to get id for serializer: %s",
                                        recipe.getSerializer())), Util.NIL_UUID);
                                return 0;
                            } else {
                                player.sendMessage(new StringTextComponent(recipe.getSerializer().getRegistryName().toString()), Util.NIL_UUID);
                                return 1;
                            }
                        }
                    }
                    player.sendMessage(new StringTextComponent(String.format("failed to find this recipe id: %s", recipeId)), Util.NIL_UUID);
                    return 0;
                })
            ))
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
                    if (map.isEmpty()) {
                        player.sendMessage(new StringTextComponent("invalid ns"), Util.NIL_UUID);
                    }
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
                            String ns = StringArgumentType.getString(ctx, "ns");
                            String path = StringArgumentType.getString(ctx, "path");
                            String op = StringArgumentType.getString(ctx, "op");
                            if (!recipes.containsKey(ns)) {
                                player.sendMessage(new StringTextComponent("invalid ns"), Util.NIL_UUID);
                                return 0;
                            }
                            HashMap<String, List<IRecipe<?>>> submap = recipes.get(ns);
                            if (!submap.containsKey(path)) {
                                player.sendMessage(new StringTextComponent("invalid path"), Util.NIL_UUID);
                                return 0;
                            }
                            List<IRecipe<?>> recs  = submap.get(path);
                            switch (op) {
                                case "list":
                                    player.sendMessage(listRecipe(recs), Util.NIL_UUID);
                                    break;
                                case "count":
                                    player.sendMessage(new StringTextComponent(String.format("%d", recs.size())), Util.NIL_UUID);
                                    break;
                                case "random":
                                    player.sendMessage(randomRecipe(recs), Util.NIL_UUID);
                                    break;
                                case "dump":
                                    dumpRecipe(player, ns, path);
                                    break;
                                default:
                                    player.sendMessage(new StringTextComponent("unknown operation"), Util.NIL_UUID);
                                    return 0;
                            }
                            return 1;
                        })
                    )
                .then(Commands.literal("print")
                        .then(Commands.argument("recipe_id", StringArgumentType.string()).suggests(SUGGEST_RECIPE_ID)
                                .executes(ctx -> {
                                    ServerPlayerEntity player = check(ctx);
                                    if (player == null) {
                                        return 0;
                                    }
                                    String ns = StringArgumentType.getString(ctx, "ns");
                                    String path = StringArgumentType.getString(ctx, "path");
                                    String recipeId = StringArgumentType.getString(ctx, "recipe_id");
                                    HashMap<String, List<IRecipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
                                    List<IRecipe<?>> recs = map.getOrDefault(path, new ArrayList<>());
                                    for (IRecipe<?> rec: recs) {
                                        if (rec.getId().toString().equals(recipeId)) {
                                            player.sendMessage(uglyPrint(materialize(rec)), Util.NIL_UUID);
                                            break;
                                        }
                                    }
                                    return 1;

                                })
                        )
                )
                    .then(Commands.literal("printj")
                            .then(Commands.argument("recipe_id", StringArgumentType.string()).suggests(SUGGEST_RECIPE_ID)
                                    .executes(ctx -> {
                                        ServerPlayerEntity player = check(ctx);
                                        if (player == null) {
                                            return 0;
                                        }
                                        String ns = StringArgumentType.getString(ctx, "ns");
                                        String path = StringArgumentType.getString(ctx, "path");
                                        String recipeId = StringArgumentType.getString(ctx, "recipe_id");
                                        HashMap<String, List<IRecipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
                                        List<IRecipe<?>> recs = map.getOrDefault(path, new ArrayList<>());
                                        for (IRecipe<?> rec: recs) {
                                            if (rec.getId().toString().equals(recipeId)) {
                                                try {
                                                    String j = toJSON(materialize(rec));
                                                    TextComponent comp = new StringTextComponent(j);
                                                    comp.setStyle(comp.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, j)));
                                                    player.sendMessage(comp, Util.NIL_UUID);
                                                } catch (Exception | AssertionError e){
                                                    player.sendMessage(new StringTextComponent(e.toString()), Util.NIL_UUID);
                                                }
                                                break;
                                            }
                                        }
                                        return 1;

                                    })
                            )
                    )));
    }

    private static void dumpRecipe(ServerPlayerEntity player, String namespace, String path) {
        TheToolkitsPacketHandler.sendTo(PacketDistributor.PLAYER.with(() -> player), new MessageDumpRecipe(namespace, path));
    }
    private static ITextComponent randomRecipe(List<IRecipe<?>> recs) {
        Random rand = new Random();
        IRecipe<?> result = recs.get(rand.nextInt(recs.size()));
        MaterializedRecipe mr = materialize(result);
        return uglyPrint(mr);
    }
    private static ITextComponent uglyPrint(MaterializedRecipe mr) {
        TextComponent tc = new StringTextComponent("");
        tc.append(new StringTextComponent("id").withStyle(TextFormatting.RED)).append(": ")
                .append(new StringTextComponent(mr.id.toString())).append("\n");
        tc.append(new StringTextComponent("group").withStyle(TextFormatting.RED)).append(": ")
                .append(new StringTextComponent(mr.group)).append("\n");
        tc.append(new StringTextComponent("type").withStyle(TextFormatting.RED)).append(": ")
                .append(new StringTextComponent(mr.type)).append("\n");
        tc.append(new StringTextComponent("isSpecial").withStyle(TextFormatting.RED)).append(": ")
                .append(new StringTextComponent(mr.isSpecial.toString())).append("\n");
        String ingredientsString = mr.ingredients.stream().map(ingredient -> ingredient.toJson().toString())
                .collect(Collectors.joining(","));
        tc.append(new StringTextComponent("ingredients").withStyle(TextFormatting.RED)).append(": ")
                .append(new StringTextComponent(ingredientsString)).append("\n");
        tc.append(new StringTextComponent("result").withStyle(TextFormatting.RED)).append(": ")
                .append(new StringTextComponent(getResourceID(mr.result))).append("\n");
        tc.setStyle(tc.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, tc.getString())));
        return tc;
    }

    private static String getResourceID(ItemStack is) {
        ResourceLocation rl = ForgeRegistries.ITEMS.getKey(is.getItem());
        if (rl == null) {
            return "";
        }
        return rl.toString();
    }
    private static ITextComponent listRecipe(List<IRecipe<?>> recs) {
        StringBuilder sb = new StringBuilder();
        for (IRecipe<?> rec : recs.subList(0, Math.min(recs.size(), 50))) {
            sb.append(rec.getId());
            sb.append('\n');
        }
        return new StringTextComponent(sb.toString());
    }

    public static MaterializedRecipe materialize(IRecipe<?> recipe) {
        ResourceLocation id = recipe.getId();
        String group = recipe.getGroup();
        String type = recipe.getType().toString();
        Boolean isSpecial = recipe.isSpecial();
        ItemStack result = recipe.getResultItem();
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int width = -1;
        int height = -1;
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe)recipe;
            width = shapedRecipe.getWidth();
            height = shapedRecipe.getHeight();
        }
        return new MaterializedRecipe(id, group, type, isSpecial, result, ingredients, width, height);
    }

    public static String toJSON(MaterializedRecipe mr) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocationSerializer())
                .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
                .registerTypeAdapter(Ingredient.class, new IngredientSerializer())
                .setPrettyPrinting()
                .create();
        return gson.toJson(mr);
    }
    public static JsonElement toJSONTree(MaterializedRecipe mr) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ResourceLocation.class, new ResourceLocationSerializer())
                .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
                .registerTypeAdapter(Ingredient.class, new IngredientSerializer())
                .setPrettyPrinting()
                .create();
        return gson.toJsonTree(mr);
    }
    //public static String toJSON(List<MaterializedRecipe> mrs) {
    //    Gson gson = new GsonBuilder()
    //            .registerTypeAdapter(ResourceLocation.class, new ResourceLocationSerializer())
    //            .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
    //            .registerTypeAdapter(Ingredient.class, new IngredientSerializer())
    //            .setPrettyPrinting()
    //            .create();
    //    return gson.toJson(mrs);
    //}

    static class ResourceLocationSerializer implements JsonSerializer<ResourceLocation> {
        @Override
        public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }
    static class ItemStackSerializer implements JsonSerializer<ItemStack> {
        @Override
        public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", getResourceID(src));
            obj.addProperty("num", src.getCount());
            CompoundNBT nbt = src.getTag();
            if (nbt == null) {
                obj.add("nbt", new JsonObject());
            } else {
                obj.add("nbt", compoundNBTToJson(nbt));
            }
            return obj;
        }
    }

    static class IngredientSerializer implements JsonSerializer<Ingredient> {
        @Override
        public JsonElement serialize(Ingredient src, Type typeOfSrc, JsonSerializationContext context) {
            return src.toJson();
        }
    }
    public static JsonObject compoundNBTToJson(CompoundNBT nbt) {
        JsonObject jsonObject = new JsonObject();

        for (String key : nbt.getAllKeys()) {
            INBT value = nbt.get(key);
            JsonElement jsonValue = convertNBTToJson(value);
            jsonObject.add(key, jsonValue);
        }

        return jsonObject;
    }

    private static JsonElement convertNBTToJson(INBT value) {
        if (value instanceof CompoundNBT) {
            return compoundNBTToJson((CompoundNBT) value);
        } else if (value instanceof ListNBT) {
            JsonArray jsonArray = new JsonArray();
            ListNBT listNBT = (ListNBT) value;
            for (INBT element : listNBT) {
                jsonArray.add(convertNBTToJson(element));
            }
            return jsonArray;
        } else if (value instanceof StringNBT) {
            return new JsonPrimitive(((StringNBT) value).toString());
        } else if (value instanceof IntNBT) {
            return new JsonPrimitive(((IntNBT) value).getAsInt());
        } else if (value instanceof LongNBT) {
            return new JsonPrimitive(((LongNBT) value).getAsLong());
        } else if (value instanceof FloatNBT) {
            return new JsonPrimitive(((FloatNBT) value).getAsFloat());
        } else if (value instanceof DoubleNBT) {
            return new JsonPrimitive(((DoubleNBT) value).getAsDouble());
        } else if (value instanceof ByteArrayNBT) {
            JsonArray jsonArray = new JsonArray();
            byte[] byteArray = ((ByteArrayNBT) value).getAsByteArray();
            for (byte b : byteArray) {
                jsonArray.add(new JsonPrimitive(b));
            }
            return jsonArray;
        } else if (value instanceof IntArrayNBT) {
            JsonArray jsonArray = new JsonArray();
            int[] intArray = ((IntArrayNBT) value).getAsIntArray();
            for (int i : intArray) {
                jsonArray.add(new JsonPrimitive(i));
            }
            return jsonArray;
        } else if (value instanceof LongArrayNBT) {
            JsonArray jsonArray = new JsonArray();
            long[] longArray = ((LongArrayNBT) value).getAsLongArray();
            for (long l : longArray) {
                jsonArray.add(new JsonPrimitive(l));
            }
            return jsonArray;
        } else {
            return new JsonPrimitive(value.toString());
        }
    }
    public static class MaterializedRecipe {
        public ResourceLocation id;
        String group;
        String type;
        Boolean isSpecial;
        ItemStack result;

        int width;
        int height;

        NonNullList<Ingredient> ingredients;

        public MaterializedRecipe(ResourceLocation id, String group, String type, Boolean isSpecial, ItemStack result, NonNullList<Ingredient> ingredients, int width, int height) {
            this.id = id;
            this.group = group;
            this.type = type;
            this.isSpecial = isSpecial;
            this.result = result;
            this.ingredients = ingredients;
            this.width = width;
            this.height = height;
        }
    }
}
