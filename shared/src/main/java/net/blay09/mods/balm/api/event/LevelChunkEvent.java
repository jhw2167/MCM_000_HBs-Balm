package net.blay09.mods.balm.api.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;

public abstract class LevelChunkEvent {
    private final LevelAccessor level;
    private final ChunkAccess chunk;
    private final ChunkPos chunkPos;

    public LevelChunkEvent(LevelAccessor level, ChunkAccess chunk) {
        this.level = level;
        this.chunk = chunk;
        this.chunkPos = chunk.getPos();
    }

    public LevelAccessor getLevel() {
        return level;
    }

    public ChunkAccess getChunk() {
        return chunk;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public static class Load extends LevelChunkEvent {
        public Load(LevelAccessor level, ChunkAccess chunk) {
            super(level, chunk);
        }
    }

    public static class Unload extends LevelChunkEvent {
        public Unload(LevelAccessor level, ChunkAccess chunk) {
            super(level, chunk);
        }
    }

}
