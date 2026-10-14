package folds.midastimer.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThumbTimerClient implements ClientModInitializer {
    public static final String MOD_ID = "thumbtimer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String TRIGGER = "(1/4) You found a thumb";

    private static KeyMapping openConfigKey;

    @Override
    public void onInitializeClient() {
        ThumbConfig.load();

        // Keybind (shows up in Options > Controls > Key Binds under "Thumb Timer")
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));
        openConfigKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.thumbtimer.open_config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openConfigKey.consumeClick()) {
                mc.setScreen(new ConfigScreen(mc.screen));
            }
        });

        // Chat listening: server system messages arrive via GAME, player chat via CHAT. Check both.
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) check(message);
        });
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, timestamp) -> check(message));

        // HUD element
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "timer"),
                (graphics, deltaTracker) -> TimerHud.renderHud(graphics));
    }

    private static void check(Component message) {
        String text = ChatFormatting.stripFormatting(message.getString());
        if (text != null && text.contains(TRIGGER)) {
            TimerHud.start();
        }
    }
}
