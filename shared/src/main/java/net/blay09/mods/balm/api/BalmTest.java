package net.blay09.mods.balm.api;

import net.blay09.mods.balm.api.event.BalmEvents;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.ChunkEvent;
import net.blay09.mods.balm.api.event.LevelEvent;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalmTest {

    public static final Logger LOG = LoggerFactory.getLogger(BalmTest.class);

    public static void init()
    {
        if(true) {
            return;
        }

        EventPriority p = EventPriority.Normal;
        BalmEvents registry = Balm.getEvents();
        //registry.onEvent( ChunkEvent.Load.class, BalmTest::onChunkLoad, p);

        registry.onEvent(LevelEvent.Load.class, BalmTest::onLevelLoad, p);
        registry.onEvent(LevelEvent.Unload.class, BalmTest::onLevelUnload, p);
    }

    public static void onChunkLoad(ChunkEvent event) {
        LOG.info("Chunk loaded: " + event.getChunkPos());
    }

    public static void onLevelLoad(LevelEvent event) {
        if( event.getLevel().isClientSide() ) return;
        LOG.info("Level loaded: " + ( (ServerLevel) event.getLevel() ).dimensionTypeId() );
    }

    public static void onLevelUnload(LevelEvent event) {
        if( event.getLevel().isClientSide() ) return;
        LOG.info("Level unloaded: " + ( (ServerLevel) event.getLevel() ).dimensionTypeId() );
    }

}
