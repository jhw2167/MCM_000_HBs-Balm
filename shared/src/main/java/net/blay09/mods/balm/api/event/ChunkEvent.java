package net.blay09.mods.balm.api.event;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

public abstract class ChunkEvent {
    private final LevelAccessor level;
    private final ChunkAccess chunk;
    private final ChunkPos chunkPos;

    public ChunkEvent(LevelAccessor level, ChunkAccess chunk) {
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

    public static class Load extends ChunkEvent {
        public Load(LevelAccessor level, ChunkAccess chunk) {
            super(level, chunk);
        }
    }

    public static class Unload extends ChunkEvent {
        public Unload(LevelAccessor level, ChunkAccess chunk) {
            super(level, chunk);
        }
    }

}
