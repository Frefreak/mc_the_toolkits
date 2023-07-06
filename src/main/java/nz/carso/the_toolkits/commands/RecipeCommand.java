package nz.carso.the_toolkits.commands;

import com.google.gson.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import nz.carso.the_toolkits.TheToolkitsPacketHandler;
import nz.carso.the_toolkits.messages.MessageDumpRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static net.minecraft.ChatFormatting.RED;
import static net.minecraftforge.registries.ForgeRegistries.RECIPE_SERIALIZERS;

public class RecipeCommand {
    static final Logger logger = LogManager.getLogger();
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_OP = (ctx, builder) -> {
        builder.suggest("list");
        builder.suggest("count");
        builder.suggest("random");
        builder.suggest("dump");
        return builder.buildFuture();
    };
    public static HashMap<String, HashMap<String, List<Recipe<?>>>> recipes = new HashMap<>();
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_NS = (ctx, builder) -> {
        if (check(ctx) == null) {
            return builder.buildFuture();
        }
        for (String key : recipes.keySet()) {
            builder.suggest(key);
        }
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PATH = (ctx, builder) -> {
        String ns = StringArgumentType.getString(ctx, "ns");
        if (check(ctx) == null) {
            return builder.buildFuture();
        }
        HashMap<String, List<Recipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
        for (String key : map.keySet()) {
            builder.suggest(key);
        }
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_RECIPE_ID = (ctx, builder) -> {
        String ns = StringArgumentType.getString(ctx, "ns");
        String path = StringArgumentType.getString(ctx, "path");
        if (check(ctx) == null) {
            return builder.buildFuture();
        }
        HashMap<String, List<Recipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
        List<Recipe<?>> recs = map.getOrDefault(path, new ArrayList<>());
        for (Recipe<?> rec : recs) {
            builder.suggest(String.format("\"%s\"", rec.getId()));
        }
        return builder.buildFuture();
    };

    public static void initRecipes(RecipeManager mgr) {
        int count = 0;
        for (Recipe<?> recipe : mgr.getRecipes()) {
            RecipeSerializer<?> serializer = recipe.getSerializer();
            ResourceLocation type = RECIPE_SERIALIZERS.getKey(serializer);
            if (type == null) {
                continue;
            }
            String ns = type.getNamespace();
            String path = type.getPath();
            if (recipes.containsKey(ns)) {
                HashMap<String, List<Recipe<?>>> submap = recipes.get(ns);
                if (submap.containsKey(path)) {
                    submap.get(path).add(recipe);
                } else {
                    List<Recipe<?>> newList = new ArrayList<>();
                    newList.add(recipe);
                    submap.put(path, newList);
                }
            } else {
                List<Recipe<?>> newList = new ArrayList<>();
                newList.add(recipe);
                HashMap<String, List<Recipe<?>>> submap = new HashMap<>();
                submap.put(path, newList);
                recipes.put(ns, submap);
            }
            count += 1;
        }
        logger.info("{} recipes populated", count);
    }

    private static ServerPlayer check(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
            return null;
        }
        ServerLevel lvl = player.getLevel();
        RecipeManager mgr = lvl.getRecipeManager();
        if (recipes.isEmpty()) {
            logger.info("populating recipes");
            RecipeCommand.initRecipes(mgr);
        }
        return player;
    }

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("recipe")
                .then(Commands.literal("search")
                        .then(Commands.argument("recipe_id", StringArgumentType.string()).executes(ctx -> {
                                    ServerPlayer player = check(ctx);
                                    if (player == null) {
                                        return 0;
                                    }
                                    String recipeId = StringArgumentType.getString(ctx, "recipe_id");
                                    ServerLevel lvl = player.getLevel();
                                    RecipeManager mgr = lvl.getRecipeManager();
                                    for (Recipe<?> recipe : mgr.getRecipes()) {
                                        if (recipe.getId().toString().equals(recipeId)) {
                                            ResourceLocation sid = RECIPE_SERIALIZERS.getKey(recipe.getSerializer());
                                            if (sid == null) {
                                                player.sendSystemMessage(Component.literal(String.format("failed to get id for serializer: %s",
                                                        recipe.getSerializer())));
                                                return 0;
                                            } else {
                                                ResourceLocation type = RECIPE_SERIALIZERS.getKey(recipe.getSerializer());
                                                if (type == null) {
                                                    return 0;
                                                }
                                                player.sendSystemMessage(Component.literal(type.toString()));
                                                return 1;
                                            }
                                        }
                                    }
                                    player.sendSystemMessage(Component.literal(String.format("failed to find this recipe id: %s", recipeId)));
                                    return 0;
                                })
                        ))
                .then(Commands.literal("list").executes(ctx -> {
                    ServerPlayer player = check(ctx);
                    if (player == null) {
                        return 0;
                    }
                    StringBuilder builder = new StringBuilder();
                    for (String key : recipes.keySet()) {
                        builder.append(key).append('\n');
                    }
                    player.sendSystemMessage(Component.literal(builder.toString()));
                    return 1;
                }))
                .then(Commands.argument("ns", StringArgumentType.string()).suggests(SUGGEST_NS)
                        .then(Commands.literal("list").executes(ctx -> {
                            ServerPlayer player = check(ctx);
                            if (player == null) {
                                return 0;
                            }
                            String ns = StringArgumentType.getString(ctx, "ns");

                            HashMap<String, List<Recipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
                            if (map.isEmpty()) {
                                player.sendSystemMessage(Component.literal("invalid ns"));
                            }
                            StringBuilder builder = new StringBuilder();
                            for (String key : map.keySet()) {
                                builder.append(key).append('\n');
                            }
                            player.sendSystemMessage(Component.literal(builder.toString()));
                            return 1;

                        }))
                        .then(Commands.argument("path", StringArgumentType.string()).suggests(SUGGEST_PATH)
                                .then(Commands.argument("op", StringArgumentType.string()).suggests(SUGGEST_OP)
                                        .executes(ctx -> {
                                            ServerPlayer player = check(ctx);
                                            if (player == null) {
                                                return 0;
                                            }
                                            String ns = StringArgumentType.getString(ctx, "ns");
                                            String path = StringArgumentType.getString(ctx, "path");
                                            String op = StringArgumentType.getString(ctx, "op");
                                            if (!recipes.containsKey(ns)) {
                                                player.sendSystemMessage(Component.literal("invalid ns"));
                                                return 0;
                                            }
                                            HashMap<String, List<Recipe<?>>> submap = recipes.get(ns);
                                            if (!submap.containsKey(path)) {
                                                player.sendSystemMessage(Component.literal("invalid path"));
                                                return 0;
                                            }
                                            List<Recipe<?>> recs = submap.get(path);
                                            switch (op) {
                                                case "list" -> player.sendSystemMessage(listRecipe(recs));
                                                case "count" ->
                                                        player.sendSystemMessage(Component.literal(String.format("%d", recs.size())));
                                                case "random" -> player.sendSystemMessage(randomRecipe(recs));
                                                case "dump" -> dumpRecipe(player, ns, path);
                                                default -> {
                                                    player.sendSystemMessage(Component.literal("unknown operation"));
                                                    return 0;
                                                }
                                            }
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("print")
                                        .then(Commands.argument("recipe_id", StringArgumentType.string()).suggests(SUGGEST_RECIPE_ID)
                                                .executes(ctx -> {
                                                    ServerPlayer player = check(ctx);
                                                    if (player == null) {
                                                        return 0;
                                                    }
                                                    String ns = StringArgumentType.getString(ctx, "ns");
                                                    String path = StringArgumentType.getString(ctx, "path");
                                                    String recipeId = StringArgumentType.getString(ctx, "recipe_id");
                                                    HashMap<String, List<Recipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
                                                    List<Recipe<?>> recs = map.getOrDefault(path, new ArrayList<>());
                                                    for (Recipe<?> rec : recs) {
                                                        if (rec.getId().toString().equals(recipeId)) {
                                                            player.sendSystemMessage(uglyPrint(materialize(rec)));
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
                                                    ServerPlayer player = check(ctx);
                                                    if (player == null) {
                                                        return 0;
                                                    }
                                                    String ns = StringArgumentType.getString(ctx, "ns");
                                                    String path = StringArgumentType.getString(ctx, "path");
                                                    String recipeId = StringArgumentType.getString(ctx, "recipe_id");
                                                    HashMap<String, List<Recipe<?>>> map = recipes.getOrDefault(ns, new HashMap<>());
                                                    List<Recipe<?>> recs = map.getOrDefault(path, new ArrayList<>());
                                                    for (Recipe<?> rec : recs) {
                                                        if (rec.getId().toString().equals(recipeId)) {
                                                            try {
                                                                String j = toJSON(materialize(rec));
                                                                MutableComponent comp = Component.literal(j);
                                                                comp.setStyle(comp.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, j)));
                                                                player.sendSystemMessage(comp);
                                                            } catch (Exception | AssertionError e) {
                                                                player.sendSystemMessage(Component.literal(e.toString()));
                                                            }
                                                            break;
                                                        }
                                                    }
                                                    return 1;

                                                })
                                        )
                                )));
    }

    private static void dumpRecipe(ServerPlayer player, String namespace, String path) {
        TheToolkitsPacketHandler.sendTo(PacketDistributor.PLAYER.with(() -> player), new MessageDumpRecipe(namespace, path));
    }

    private static Component randomRecipe(List<Recipe<?>> recs) {
        Random rand = new Random();
        Recipe<?> result = recs.get(rand.nextInt(recs.size()));
        MaterializedRecipe mr = materialize(result);
        return uglyPrint(mr);
    }

    private static Component uglyPrint(MaterializedRecipe mr) {
        MutableComponent tc = Component.literal("");
        tc.append(Component.literal("id").withStyle(RED)).append(": ")
                .append(Component.literal(mr.id.toString())).append("\n");
        tc.append(Component.literal("group").withStyle(RED)).append(": ")
                .append(Component.literal(mr.group)).append("\n");
        tc.append(Component.literal("type").withStyle(RED)).append(": ")
                .append(Component.literal(mr.type)).append("\n");
        tc.append(Component.literal("isSpecial").withStyle(RED)).append(": ")
                .append(Component.literal(mr.isSpecial.toString())).append("\n");
        String ingredientsString = mr.ingredients.stream().map(ingredient -> ingredient.toJson().toString())
                .collect(Collectors.joining(","));
        tc.append(Component.literal("ingredients").withStyle(RED)).append(": ")
                .append(Component.literal(ingredientsString)).append("\n");
        tc.append(Component.literal("result").withStyle(RED)).append(": ")
                .append(Component.literal(getResourceID(mr.result))).append("\n");
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

    private static Component listRecipe(List<Recipe<?>> recs) {
        StringBuilder sb = new StringBuilder();
        for (Recipe<?> rec : recs.subList(0, Math.min(recs.size(), 50))) {
            sb.append(rec.getId());
            sb.append('\n');
        }
        return Component.literal(sb.toString());
    }

    public static MaterializedRecipe materialize(Recipe<?> recipe) {
        ResourceLocation id = recipe.getId();
        String group = recipe.getGroup();
        String type = recipe.getType().toString();
        Boolean isSpecial = recipe.isSpecial();
        ItemStack result = recipe.getResultItem();
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int width = -1;
        int height = -1;
        if (recipe instanceof ShapedRecipe shapedRecipe) {
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

    public static JsonObject compoundNBTToJson(CompoundTag nbt) {
        JsonObject jsonObject = new JsonObject();

        for (String key : nbt.getAllKeys()) {
            Tag value = nbt.get(key);
            JsonElement jsonValue = convertNBTToJson(value);
            jsonObject.add(key, jsonValue);
        }

        return jsonObject;
    }

    private static JsonElement convertNBTToJson(Tag value) {
        if (value instanceof CompoundTag) {
            return compoundNBTToJson((CompoundTag) value);
        } else if (value instanceof ListTag listNBT) {
            JsonArray jsonArray = new JsonArray();
            for (Tag element : listNBT) {
                jsonArray.add(convertNBTToJson(element));
            }
            return jsonArray;
        } else if (value instanceof StringTag) {
            return new JsonPrimitive((value).toString());
        } else if (value instanceof IntTag) {
            return new JsonPrimitive(((IntTag) value).getAsInt());
        } else if (value instanceof LongTag) {
            return new JsonPrimitive(((LongTag) value).getAsLong());
        } else if (value instanceof FloatTag) {
            return new JsonPrimitive(((FloatTag) value).getAsFloat());
        } else if (value instanceof DoubleTag) {
            return new JsonPrimitive(((DoubleTag) value).getAsDouble());
        } else if (value instanceof ByteArrayTag) {
            JsonArray jsonArray = new JsonArray();
            byte[] byteArray = ((ByteArrayTag) value).getAsByteArray();
            for (byte b : byteArray) {
                jsonArray.add(new JsonPrimitive(b));
            }
            return jsonArray;
        } else if (value instanceof IntArrayTag) {
            JsonArray jsonArray = new JsonArray();
            int[] intArray = ((IntArrayTag) value).getAsIntArray();
            for (int i : intArray) {
                jsonArray.add(new JsonPrimitive(i));
            }
            return jsonArray;
        } else if (value instanceof LongArrayTag) {
            JsonArray jsonArray = new JsonArray();
            long[] longArray = ((LongArrayTag) value).getAsLongArray();
            for (long l : longArray) {
                jsonArray.add(new JsonPrimitive(l));
            }
            return jsonArray;
        } else {
            return new JsonPrimitive(value.toString());
        }
    }

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
            CompoundTag nbt = src.getTag();
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
