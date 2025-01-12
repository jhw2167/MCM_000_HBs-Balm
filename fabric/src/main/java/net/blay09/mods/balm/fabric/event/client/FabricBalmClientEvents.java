package net.blay09.mods.balm.fabric.event.client;

import net.blay09.mods.balm.api.event.client.*;
import net.blay09.mods.balm.fabric.event.FabricBalmEvents;
import net.blay09.mods.balm.api.event.TickPhase;
import net.blay09.mods.balm.api.event.TickType;
import net.blay09.mods.balm.api.event.client.screen.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FabricBalmClientEvents {

    private static final List<Consumer<Screen>> screenDrawPreInitializers = new ArrayList<>();
    private static final List<Consumer<Screen>> screenDrawPostInitializers = new ArrayList<>();

    private static final List<Consumer<Screen>> screenMousePressPreInitializers = new ArrayList<>();
    private static final List<Consumer<Screen>> screenMousePressPostInitializers = new ArrayList<>();

    private static final List<Consumer<Screen>> screenMouseReleasePreInitializers = new ArrayList<>();
    private static final List<Consumer<Screen>> screenMouseReleasePostInitializers = new ArrayList<>();

    private static final List<Consumer<Screen>> screenMouseClickPreInitializers = new ArrayList<>();
    private static final List<Consumer<Screen>> screenMouseClickPostInitializers = new ArrayList<>();

    private static final List<Consumer<Screen>> screenKeyPressPreInitializers = new ArrayList<>();
    private static final List<Consumer<Screen>> screenKeyPressPostInitializers = new ArrayList<>();

    private static final List<Consumer<Screen>> screenKeyReleasePreInitializers = new ArrayList<>();
    private static final List<Consumer<Screen>> screenKeyReleasePostInitializers = new ArrayList<>();

    private static ScreenEvents.BeforeInit beforeInitListener = null;

    private static void initializeScreenEvents() {
        if (beforeInitListener == null) {
            final List<List<Consumer<Screen>>> initializers = new ArrayList<>();

            initializers.add(screenDrawPreInitializers);
            initializers.add(screenDrawPostInitializers);

            initializers.add(screenMousePressPreInitializers);
            initializers.add(screenMousePressPostInitializers);

            initializers.add(screenMouseReleasePreInitializers);
            initializers.add(screenMouseReleasePostInitializers);

            initializers.add(screenMouseClickPreInitializers);
            initializers.add(screenMouseClickPostInitializers);

            initializers.add(screenKeyPressPreInitializers);
            initializers.add(screenKeyPressPostInitializers);

            initializers.add(screenKeyReleasePreInitializers);
            initializers.add(screenKeyReleasePostInitializers);

            beforeInitListener = (client, scr, scaledWidth, scaledHeight) -> {
                for (List<Consumer<Screen>> list : initializers) {
                    for (Consumer<Screen> initializer : list) {
                        initializer.accept(scr);
                    }
                }
            };
            ScreenEvents.BEFORE_INIT.register(beforeInitListener);
        }
    }

    public static void registerEvents(FabricBalmEvents events) {
        events.registerTickEvent(TickType.Client, TickPhase.Start, (ClientTickHandler handler) -> ClientTickEvents.START_CLIENT_TICK.register(handler::handle));
        events.registerTickEvent(TickType.Client, TickPhase.End, (ClientTickHandler handler) -> ClientTickEvents.END_CLIENT_TICK.register(handler::handle));
        events.registerTickEvent(TickType.ClientLevel, TickPhase.Start, (ClientLevelTickHandler handler) -> ClientTickEvents.START_WORLD_TICK.register(handler::handle));
        events.registerTickEvent(TickType.ClientLevel, TickPhase.End, (ClientLevelTickHandler handler) -> ClientTickEvents.END_WORLD_TICK.register(handler::handle));

        events.registerEvent(ClientStartedEvent.class, () -> ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            final ClientStartedEvent event = new ClientStartedEvent(client);
            events.fireEventHandlers(event);
        }));

        events.registerEvent(ConnectedToServerEvent.class, () -> ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            final ConnectedToServerEvent event = new ConnectedToServerEvent(client);
            events.fireEventHandlers(event);
        }));

        events.registerEvent(DisconnectedFromServerEvent.class, () -> ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            final DisconnectedFromServerEvent event = new DisconnectedFromServerEvent(client);
            events.fireEventHandlers(event);
        }));

        events.registerEvent(ScreenInitEvent.Pre.class, () -> ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            final ScreenInitEvent.Pre event = new ScreenInitEvent.Pre(screen);
            events.fireEventHandlers(event);
        }));

        events.registerEvent(ScreenInitEvent.Post.class, () -> ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            final ScreenInitEvent.Post event = new ScreenInitEvent.Post(screen);
            events.fireEventHandlers(event);
        }));

        events.registerEvent(ScreenDrawEvent.Pre.class, () -> {
            initializeScreenEvents();
            screenDrawPreInitializers.add((scr) -> ScreenEvents.beforeRender(scr).register((screen, guiGraphics, mouseX, mouseY, tickDelta) -> {
                final ScreenDrawEvent.Pre event = new ScreenDrawEvent.Pre(screen, guiGraphics, mouseX, mouseY, tickDelta);
                events.fireEventHandlers(event);
            }));
        });

        events.registerEvent(ScreenDrawEvent.Post.class, () -> {
            initializeScreenEvents();
            screenDrawPostInitializers.add((scr) -> ScreenEvents.afterRender(scr).register((screen, guiGraphics, mouseX, mouseY, tickDelta) -> {
                final ScreenDrawEvent.Post event = new ScreenDrawEvent.Post(screen, guiGraphics, mouseX, mouseY, tickDelta);
                events.fireEventHandlers(event);
            }));
        });

        events.registerEvent(ScreenMouseEvent.Click.Pre.class, () -> {
            initializeScreenEvents();
            screenMouseClickPreInitializers.add((scr) -> ScreenMouseEvents.allowMouseClick(scr).register((screen, mouseX, mouseY, button) -> {
                final ScreenMouseEvent.Click.Pre event = new ScreenMouseEvent.Click.Pre(screen, mouseX, mouseY, button);
                events.fireEventHandlers(event);
                return !event.isCanceled();
            }));
        });

        events.registerEvent(ScreenMouseEvent.Click.Post.class, () -> {
            initializeScreenEvents();
            screenMouseClickPostInitializers.add((scr) -> ScreenMouseEvents.afterMouseClick(scr).register((screen, mouseX, mouseY, button) -> {
                final ScreenMouseEvent.Click.Post event = new ScreenMouseEvent.Click.Post(screen, mouseX, mouseY, button);
                events.fireEventHandlers(event);
            }));
        });

        events.registerEvent(ScreenMouseEvent.Release.Pre.class, () -> {
            initializeScreenEvents();
            screenMouseReleasePreInitializers.add((scr) -> ScreenMouseEvents.allowMouseRelease(scr).register((screen, mouseX, mouseY, button) -> {
                final ScreenMouseEvent.Release.Pre event = new ScreenMouseEvent.Release.Pre(screen, mouseX, mouseY, button);
                events.fireEventHandlers(event);
                return !event.isCanceled();
            }));
        });

        events.registerEvent(ScreenMouseEvent.Release.Post.class, () -> {
            initializeScreenEvents();
            screenMouseReleasePostInitializers.add((scr) -> ScreenMouseEvents.afterMouseRelease(scr).register((screen, mouseX, mouseY, button) -> {
                final ScreenMouseEvent.Release.Post event = new ScreenMouseEvent.Release.Post(screen, mouseX, mouseY, button);
                events.fireEventHandlers(event);
            }));
        });

        events.registerEvent(ScreenKeyEvent.Press.Pre.class, () -> {
            initializeScreenEvents();
            screenKeyPressPreInitializers.add((scr) -> ScreenKeyboardEvents.allowKeyPress(scr).register((screen, KeyX, KeyY, button) -> {
                final ScreenKeyEvent.Press.Pre event = new ScreenKeyEvent.Press.Pre(screen, KeyX, KeyY, button);
                events.fireEventHandlers(event);
                return !event.isCanceled();
            }));
        });

        events.registerEvent(ScreenKeyEvent.Press.Post.class, () -> {
            initializeScreenEvents();
            screenKeyPressPostInitializers.add((scr) -> ScreenKeyboardEvents.afterKeyPress(scr).register((screen, KeyX, KeyY, button) -> {
                final ScreenKeyEvent.Press.Post event = new ScreenKeyEvent.Press.Post(screen, KeyX, KeyY, button);
                events.fireEventHandlers(event);
            }));
        });

        events.registerEvent(ScreenKeyEvent.Release.Pre.class, () -> {
            initializeScreenEvents();
            screenKeyReleasePreInitializers.add((scr) -> ScreenKeyboardEvents.allowKeyRelease(scr).register((screen, KeyX, KeyY, button) -> {
                final ScreenKeyEvent.Release.Pre event = new ScreenKeyEvent.Release.Pre(screen, KeyX, KeyY, button);
                events.fireEventHandlers(event);
                return !event.isCanceled();
            }));
        });

        events.registerEvent(ScreenKeyEvent.Release.Post.class, () -> {
            initializeScreenEvents();
            screenKeyReleasePostInitializers.add((scr) -> ScreenKeyboardEvents.afterKeyRelease(scr).register((screen, KeyX, KeyY, button) -> {
                final ScreenKeyEvent.Release.Post event = new ScreenKeyEvent.Release.Post(screen, KeyX, KeyY, button);
                events.fireEventHandlers(event);
            }));
        });

        events.registerEvent(ItemTooltipEvent.class, () -> ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            final ItemTooltipEvent event = new ItemTooltipEvent(stack, Minecraft.getInstance().player, lines, context);
            events.fireEventHandlers(event);
        }));

    }

}
