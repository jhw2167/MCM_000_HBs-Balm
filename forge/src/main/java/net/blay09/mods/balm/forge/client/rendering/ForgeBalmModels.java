package net.blay09.mods.balm.forge.client.rendering;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Transformation;
import net.blay09.mods.balm.api.DeferredObject;
import net.blay09.mods.balm.api.client.rendering.BalmModels;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeBalmModels implements BalmModels {

    private static final Logger LOGGER = LogUtils.getLogger();

    private abstract static class DeferredModel extends DeferredObject<BakedModel> {
        public DeferredModel(ResourceLocation identifier) {
            super(identifier);
        }

        public void resolveAndSet(ModelBakery modelBakery, Map<ResourceLocation, BakedModel> modelRegistry, BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction) {
            try {
                set(resolve(modelBakery, modelRegistry, spriteBiFunction));
            } catch (Exception exception) {
                LOGGER.warn("Unable to bake model: '{}':", getIdentifier(), exception);
                set(modelBakery.getBakedTopLevelModels().get(ModelBakery.MISSING_MODEL_LOCATION));
            }
        }

        public abstract BakedModel resolve(ModelBakery modelBakery, Map<ResourceLocation, BakedModel> modelRegistry, BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction);
    }

    public final List<DeferredModel> modelsToBake = Collections.synchronizedList(new ArrayList<>());

    private static class Registrations {
        public final List<DeferredModel> additionalModels = new ArrayList<>();
        public final List<Pair<Supplier<Block>, Supplier<BakedModel>>> overrides = new ArrayList<>();

        private BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction;

        public void setSpriteBiFunction(BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction) {
            this.spriteBiFunction = spriteBiFunction;
        }

        @SubscribeEvent
        public void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
            additionalModels.forEach(it -> event.register(it.getIdentifier()));
        }

        @SubscribeEvent
        public void onModelBakingCompleted(ModelEvent.ModifyBakingResult event) {
            for (Pair<Supplier<Block>, Supplier<BakedModel>> override : overrides) {
                Block block = override.getFirst().get();
                BakedModel bakedModel = override.getSecond().get();
                block.getStateDefinition().getPossibleStates().forEach(state -> {
                    ModelResourceLocation modelLocation = BlockModelShaper.stateToModelLocation(state);
                    event.getModels().put(modelLocation, bakedModel);
                });
            }
        }

        @SubscribeEvent
        public void onModelBakingCompleted(ModelEvent.BakingCompleted event) {
            for (DeferredModel deferredModel : additionalModels) {
                deferredModel.resolveAndSet(event.getModelBakery(), event.getModels(), spriteBiFunction);
            }

            spriteBiFunction = null;
        }
    }

    private final Map<String, Registrations> registrations = new ConcurrentHashMap<>();
    private ModelBakery modelBakery;

    public void onBakeModels(ModelBakery modelBakery, BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction) {
        this.modelBakery = modelBakery;
        registrations.values().forEach(it -> it.setSpriteBiFunction(spriteBiFunction));

        synchronized (modelsToBake) {
            for (DeferredModel deferredModel : modelsToBake) {
                deferredModel.resolveAndSet(modelBakery, modelBakery.getBakedTopLevelModels(), spriteBiFunction);
            }
        }
    }

    @Override
    public DeferredObject<BakedModel> loadModel(ResourceLocation identifier) {
        DeferredModel deferredModel = new DeferredModel(identifier) {
            @Override
            public BakedModel resolve(ModelBakery bakery, Map<ResourceLocation, BakedModel> modelRegistry, BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction) {
                return modelRegistry.get(getIdentifier());
            }
        };
        getActiveRegistrations().additionalModels.add(deferredModel);
        return deferredModel;
    }

    @Override
    public DeferredObject<BakedModel> bakeModel(ResourceLocation identifier, UnbakedModel model) {
        DeferredModel deferredModel = new DeferredModel(identifier) {
            @Override
            public BakedModel resolve(ModelBakery bakery, Map<ResourceLocation, BakedModel> modelRegistry, BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction) {
                ModelBaker baker = createBaker(identifier, spriteBiFunction);
                return model.bake(baker, baker.getModelTextureGetter(), getModelState(Transformation.identity()), identifier);
            }
        };
        modelsToBake.add(deferredModel);
        return deferredModel;
    }

    @Override
    public DeferredObject<BakedModel> retexture(ResourceLocation identifier, Map<String, String> textureMap) {
        DeferredModel deferredModel = new DeferredModel(identifier) {
            @Override
            public BakedModel resolve(ModelBakery bakery, Map<ResourceLocation, BakedModel> modelRegistry, BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction) {
                UnbakedModel model = retexture(bakery, identifier, textureMap);
                ModelBaker baker = createBaker(identifier, spriteBiFunction);
                return model.bake(baker, baker.getModelTextureGetter(), getModelState(Transformation.identity()), identifier);
            }
        };
        modelsToBake.add(deferredModel);
        return deferredModel;
    }

    @Override
    public DeferredObject<BakedModel> loadDynamicModel(ResourceLocation identifier, @Nullable Function<BlockState, ResourceLocation> modelFunction, @Nullable Function<BlockState, Map<String, String>> textureMapFunction, @Nullable BiConsumer<BlockState, Matrix4f> transformFunction, List<RenderType> renderTypes) {
        Function<BlockState, ResourceLocation> effectiveModelFunction = modelFunction != null ? modelFunction : (it -> identifier);

        DeferredModel deferredModel = new DeferredModel(identifier) {
            @Override
            public BakedModel resolve(ModelBakery bakery, Map<ResourceLocation, BakedModel> modelRegistry, BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction) {
                return new ForgeCachedDynamicModel(bakery, effectiveModelFunction, null, textureMapFunction, transformFunction, renderTypes, identifier);
            }
        };
        modelsToBake.add(deferredModel);
        return deferredModel;
    }

    @Override
    public void overrideModel(Supplier<Block> block, Supplier<BakedModel> model) {
        getActiveRegistrations().overrides.add(Pair.of(block, model));
    }

    @Override
    public ModelState getModelState(Transformation transformation) {
        return new SimpleModelState(transformation);
    }

    @Override
    public UnbakedModel getUnbakedModelOrMissing(ResourceLocation location) {
        return modelBakery.getModel(location);
    }

    @Override
    public UnbakedModel getUnbakedMissingModel() {
        return modelBakery.getModel(ModelBakery.MISSING_MODEL_LOCATION);
    }

    public void register() {
        FMLJavaModLoadingContext.get().getModEventBus().register(getActiveRegistrations());
    }

    private Registrations getActiveRegistrations() {
        return registrations.computeIfAbsent(ModLoadingContext.get().getActiveNamespace(), it -> new Registrations());
    }

    @Override
    public ModelBaker createBaker(ResourceLocation location, BiFunction<ResourceLocation, Material, TextureAtlasSprite> spriteBiFunction) {
        try {
            Class<?> clazz = Class.forName("net.minecraft.client.resources.model.ModelBakery$ModelBakerImpl");
            Constructor<?> constructor = clazz.getDeclaredConstructor(ModelBakery.class, BiFunction.class, ResourceLocation.class);
            constructor.setAccessible(true);
            return (ModelBaker) constructor.newInstance(modelBakery, spriteBiFunction, location);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Balm failed to create model baker", e);
        }
    }
}
