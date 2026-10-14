package folds.midastimer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public class TimerHud {
    public static final long DURATION_MS = 10 * 60 * 1000L;
    private static long endTime = 0;

    public static void start() {
        endTime = System.currentTimeMillis() + DURATION_MS;
    }

    public static void stop() {
        endTime = 0;
    }

    public static long remainingMs() {
        return Math.max(0, endTime - System.currentTimeMillis());
    }

    public static boolean isActive() {
        return remainingMs() > 0;
    }

    public static String format(long ms) {
        long totalSeconds = (ms + 999) / 1000;
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    /** Called by the HUD every frame. */
    public static void renderHud(GuiGraphics graphics) {
        if (!isActive()) return;
        draw(graphics, "Thumb: " + format(remainingMs()));
    }

    /** Draws the text using the current config. Also used for the preview in the config screen. */
    public static void draw(GuiGraphics graphics, String text) {
        ThumbConfig cfg = ThumbConfig.get();
        Font font = Minecraft.getInstance().font;

        Style style = Style.EMPTY;
        try {
            style = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.parse(cfg.fontId)));
        } catch (Exception ignored) {
            // invalid font id in config -> fall back to default font
        }
        Component component = Component.literal(text).withStyle(style);

        float w = font.width(component) * cfg.scale;
        float h = font.lineHeight * cfg.scale;
        float px = cfg.x * (graphics.guiWidth() - w);
        float py = cfg.y * (graphics.guiHeight() - h);

        graphics.pose().pushMatrix();
        graphics.pose().translate(px, py);
        graphics.pose().scale(cfg.scale, cfg.scale);
        graphics.drawString(font, component, 0, 0, 0xFF000000 | cfg.color, cfg.shadow);
        graphics.pose().popMatrix();
    }
}
