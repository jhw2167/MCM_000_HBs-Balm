package net.blay09.mods.balm.api.client;

import net.blay09.mods.balm.api.client.keymappings.BalmKeyMappings;
import net.blay09.mods.balm.api.client.rendering.BalmModels;
import net.blay09.mods.balm.api.client.rendering.BalmRenderers;
import net.blay09.mods.balm.api.client.rendering.BalmTextures;
import net.blay09.mods.balm.api.client.screen.BalmScreens;

public interface BalmClientRuntime {
    BalmRenderers getRenderers();
    BalmTextures getTextures();
    BalmScreens getScreens();
    BalmModels getModels();
    BalmKeyMappings getKeyMappings();
    void initialize(String modId, Runnable initializer);
}
