package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;
import nz.carso.the_toolkits.TheToolkitsPacketHandler;
import nz.carso.the_toolkits.messages.MessageSaveFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DumpBlockEntityCommand {
    private static final Logger logger = LogManager.getLogger();

    private static Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunkMap(ServerWorld level) {
        ChunkManager mgr = level.getChunkSource().chunkMap;
        Class<?> chunkManagerClass = mgr.getClass();
        Field visibleChunkMapField = null;
        try {
            visibleChunkMapField = chunkManagerClass.getDeclaredField("visibleChunkMap");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
        visibleChunkMapField.setAccessible(true);
        Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkMap = null;
        try {
            return (Long2ObjectLinkedOpenHashMap<ChunkHolder>)visibleChunkMapField.get(mgr);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("block_entity").requires(c -> c.hasPermission(2))
                .then(Commands.literal("summary").executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrException();
                        ServerWorld level = ctx.getSource().getLevel();
                        HashMap<String, Integer> summary = new HashMap<>();
                        Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkMap = getChunkMap(level);
                        if (chunkMap == null) {
                            return 0;
                        }
                        for (ChunkHolder ch : chunkMap.values()) {
                            Chunk tch = ch.getTickingChunk();
                            if (tch != null) {
                                Map<BlockPos, TileEntity> entities = tch.getBlockEntities();
                                for (TileEntity ent: entities.values()) {
                                    ResourceLocation rl = ent.getType().getRegistryName();
                                    if (rl != null) {
                                        String name = rl.toString();
                                        summary.put(name, summary.getOrDefault(name, 0) + 1);
                                    }
                                }
                            }
                        }

                        StringBuilder sb = new StringBuilder();
                        for (String kv : summary.keySet()) {
                            sb.append(kv);
                            sb.append(" ");
                            sb.append(summary.get(kv));
                            sb.append("\n");
                        }
                        MessageSaveFile msg = new MessageSaveFile("block_entity_summary.txt", sb.toString());
                        TheToolkitsPacketHandler.sendTo(PacketDistributor.PLAYER.with(() -> p), msg);
                        return 1;
                    }))
            .then(Commands.literal("locations").then(
                Commands.argument("name", StringArgumentType.string()).executes(ctx -> {
                    ServerPlayerEntity p = ctx.getSource().getPlayerOrException();
                    ServerWorld level = ctx.getSource().getLevel();
                    String targetName = StringArgumentType.getString(ctx, "name");
                    Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkMap = getChunkMap(level);
                    if (chunkMap == null) {
                        return 0;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (ChunkHolder ch : chunkMap.values()) {
                        Chunk tch = ch.getTickingChunk();
                        if (tch != null) {
                            Map<BlockPos, TileEntity> entities = tch.getBlockEntities();
                            for (TileEntity ent: entities.values()) {
                                ResourceLocation rl = ent.getType().getRegistryName();
                                if (rl != null) {
                                    String name = rl.toString();
                                    if (name.equals(targetName)) {
                                        BlockPos pos = ent.getBlockPos();
                                        sb.append(String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ()));
                                        sb.append("\n");
                                    }
                                }
                            }
                        }
                    }

                    String namePath = targetName.replace(":", "_");
                    MessageSaveFile msg = new MessageSaveFile(String.format("block_entity_%s.txt", namePath), sb.toString());
                    TheToolkitsPacketHandler.sendTo(PacketDistributor.PLAYER.with(() -> p), msg);
                    return 1;
                })
            )
        );
}

}
