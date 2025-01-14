package net.blay09.mods.balm.api;

import net.blay09.mods.balm.api.event.BalmEvents;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.LevelChunkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalmTest {

    public static final Logger LOG = LoggerFactory.getLogger(BalmTest.class);

    public static void init()
    {
        if(true) {
            //return;
        }

        EventPriority p = EventPriority.Normal;
        BalmEvents registry = Balm.getEvents();
        registry.onEvent( LevelChunkEvent.Load.class, BalmTest::onChunkLoad, p);
    }

    public static void onChunkLoad(LevelChunkEvent event) {
        LOG.info("Chunk loaded: " + event.getChunkPos());
    }

}
