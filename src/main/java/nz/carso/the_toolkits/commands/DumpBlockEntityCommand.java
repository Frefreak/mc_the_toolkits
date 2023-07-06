package nz.carso.the_toolkits.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.PacketDistributor;
import nz.carso.the_toolkits.TheToolkitsPacketHandler;
import nz.carso.the_toolkits.messages.MessageSaveFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DumpBlockEntityCommand {
    private static final Logger logger = LogManager.getLogger();

    private static Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunkMap(ServerLevel level) {
        ChunkMap mgr = level.getChunkSource().chunkMap;
        return mgr.visibleChunkMap;
    }

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("block_entity").requires(c -> c.hasPermission(2))
                .then(Commands.literal("summary").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                        ServerLevel level = ctx.getSource().getLevel();
                        HashMap<String, Integer> summary = new HashMap<>();
                        Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkMap = getChunkMap(level);
                        if (chunkMap == null) {
                            return 0;
                        }
                        for (ChunkHolder ch : chunkMap.values()) {
                            LevelChunk tch = ch.getTickingChunk();
                            if (tch != null) {
                                Map<BlockPos, BlockEntity> entities = tch.getBlockEntities();
                                for (BlockEntity ent: entities.values()) {
                                    ResourceLocation rl = BlockEntityType.getKey(ent.getType());

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
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    ServerLevel level = ctx.getSource().getLevel();
                    String targetName = StringArgumentType.getString(ctx, "name");
                    Long2ObjectLinkedOpenHashMap<ChunkHolder> chunkMap = getChunkMap(level);
                    if (chunkMap == null) {
                        return 0;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (ChunkHolder ch : chunkMap.values()) {
                        LevelChunk tch = ch.getTickingChunk();
                        if (tch != null) {
                            Map<BlockPos, BlockEntity> entities = tch.getBlockEntities();
                            for (BlockEntity ent: entities.values()) {
                                ResourceLocation rl = BlockEntityType.getKey(ent.getType());
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
