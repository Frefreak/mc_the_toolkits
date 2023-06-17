package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import nz.carso.the_toolkits.TheToolkitsPacketHandler;
import nz.carso.the_toolkits.messages.MessageSaveFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

public class DumpEntityCommand {
    private static final Logger logger = LogManager.getLogger();

    private static final SuggestionProvider<CommandSource> SUGGEST_TYPE = (ctx, builder) -> {
        builder.suggest("classname");
        builder.suggest("registry_name");
        builder.suggest("name");
        return builder.buildFuture();
    };
    private static String getRegistryName(Entity ent) {
        ResourceLocation rl = ent.getType().getRegistryName();
        if (rl != null) {
            return rl.toString();
        }
        return "NULL";
    }
    private static String getClassname(Entity ent) {
        return ent.getClass().getName();
    }
    private static String getName(Entity ent) {
        return ent.getType().getDescription().getString();
    }
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("entity").requires(c -> c.hasPermission(2))
            .then(Commands.literal("summary").then(
                    Commands.argument("type", StringArgumentType.string()).suggests(SUGGEST_TYPE).executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayerOrException();
                        ServerWorld level = ctx.getSource().getLevel();
                        String type = StringArgumentType.getString(ctx, "type");
                        HashMap<String, Integer> summary = new HashMap<>();
                        Iterable<Entity> entities = level.getAllEntities();
                        switch (type) {
                            case "classname":
                                for (Entity entity : entities) {
                                    String name = getClassname(entity);
                                    summary.put(name, summary.getOrDefault(name, 0) + 1);
                                }
                                break;
                            case "name":
                                for (Entity entity : entities) {
                                    String name = getName(entity);
                                    summary.put(name, summary.getOrDefault(name, 0) + 1);
                                }
                                break;
                            case "registry_name":
                                for (Entity entity : entities) {
                                    String name = getRegistryName(entity);
                                    summary.put(name, summary.getOrDefault(name, 0) + 1);
                                }
                                break;
                            default:
                                p.sendMessage(new StringTextComponent("invalid type"), Util.NIL_UUID);
                                return 0;
                        }

                        StringBuilder sb = new StringBuilder();
                        for (String kv : summary.keySet()) {
                            sb.append(kv);
                            sb.append(" ");
                            sb.append(summary.get(kv));
                            sb.append("\n");
                        }
                        MessageSaveFile msg = new MessageSaveFile("entity_summary.txt", sb.toString());
                        TheToolkitsPacketHandler.sendTo(PacketDistributor.PLAYER.with(() -> p), msg);
                        return 1;
                    })))
            .then(Commands.literal("locations").then(
                Commands.argument("type", StringArgumentType.string()).suggests(SUGGEST_TYPE).then(
                    Commands.argument("name", StringArgumentType.string()).executes(ctx -> {
                        ServerPlayerEntity p = ctx.getSource().getPlayerOrException();
                        ServerWorld level = ctx.getSource().getLevel();
                        String type = StringArgumentType.getString(ctx, "type");
                        String targetName = StringArgumentType.getString(ctx, "name");
                        StringBuilder sb = new StringBuilder();
                        Iterable<Entity> entities = level.getAllEntities();
                        for (Entity entity : entities) {
                            String name;
                            switch (type) {
                                case "classname":
                                    name = getClassname(entity);
                                    break;
                                case "registry_name":
                                    name = getRegistryName(entity);
                                    break;
                                case "name":
                                    name = getName(entity);
                                    break;
                                default:
                                    p.sendMessage(new StringTextComponent("invalid type"), Util.NIL_UUID);
                                    return 0;
                            }
                            if (name.equals(targetName)) {
                                Vector3d pos = entity.position();
                                sb.append(String.format("%.3f %.3f %.3f", pos.x, pos.y, pos.z));
                                sb.append("\n");
                            }
                        }
                        String namePath = targetName.replace(":", "_");
                        MessageSaveFile msg = new MessageSaveFile(String.format("entity_%s.txt", namePath), sb.toString());
                        TheToolkitsPacketHandler.sendTo(PacketDistributor.PLAYER.with(() -> p), msg);
                        return 1;
                    })
                )
            )
        );
    }

}
