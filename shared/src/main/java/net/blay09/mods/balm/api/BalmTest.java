package net.blay09.mods.balm.api;

import net.blay09.mods.balm.api.event.BalmEvents;
import net.blay09.mods.balm.api.event.EventPriority;
import net.blay09.mods.balm.api.event.ChunkEvent;
import net.blay09.mods.balm.api.event.LevelEvent;
import net.blay09.mods.balm.api.event.server.ServerStartedEvent;
import net.blay09.mods.balm.api.event.server.ServerBeforeStartingEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
        //registry.onEvent( ChunkEvent.Load.class, BalmTest::onChunkLoad, p);

        registry.onEvent(LevelEvent.Load.class, BalmTest::onLevelLoad, p);
        //registry.onEvent(LevelEvent.Unload.class, BalmTest::onLevelUnload, p);

        registry.onEvent(LevelEvent.Load.class, BalmTest::onClientLevelLoad, p);
        registry.onEvent(LevelEvent.Unload.class, BalmTest::onClientLevelUnload, p);

        registry.onEvent(ServerBeforeStartingEvent.class, BalmTest::onServerStarting, p);
        registry.onEvent(ServerStartedEvent.class, BalmTest::onServerStarted, p);

    }

    public static void onChunkLoad(ChunkEvent event) {

        LOG.info("Chunk loaded: " + event.getChunkPos() + " is client side " + event.getLevel().isClientSide());
    }

    public static void onLevelLoad(LevelEvent event) {
        if( event.getLevel().isClientSide() ) return;
        LOG.info("Level loaded: " + ( (ServerLevel) event.getLevel() ).dimensionTypeId() );
    }

    public static void onClientLevelLoad(LevelEvent event) {
        if( !event.getLevel().isClientSide() ) return;
        LOG.info("Client Level loaded: " + ( (ClientLevel) event.getLevel() ).dimensionTypeId() );
    }
    public static void onClientLevelUnload(LevelEvent event) {
        if( !event.getLevel().isClientSide() ) return;
        LOG.info("Client Level UNloaded: " + ( (ClientLevel) event.getLevel() ).dimensionTypeId() );
    }

    public static void onLevelUnload(LevelEvent event) {
        if( event.getLevel().isClientSide() ) return;
        LOG.info("Level unloaded: " + ( (ServerLevel) event.getLevel() ).dimensionTypeId() );
    }

    public static void onServerStarting(ServerBeforeStartingEvent event) {
        MinecraftServer s = event.getServer();
        LOG.info("Server starting: " );
    }

    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer s = event.getServer();
        LOG.info("Server started fully: " );
    }


}
