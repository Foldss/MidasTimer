package folds.midastimer.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.DoubleConsumer;

public class ConfigScreen extends Screen {
    private static final List<String> FONTS = List.of(
            "minecraft:default", "minecraft:uniform", "minecraft:alt", "minecraft:illageralt");

    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Thumb Timer Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        ThumbConfig cfg = ThumbConfig.get();
        int w = 200;
        int x = this.width / 2 - w / 2;
        int y = 30;
        int step = 24;

        addRenderableWidget(new Slider(x, y, w, 20, "X", 0, 1, cfg.x, v -> cfg.x = (float) v));
        y += step;
        addRenderableWidget(new Slider(x, y, w, 20, "Y", 0, 1, cfg.y, v -> cfg.y = (float) v));
        y += step;
        addRenderableWidget(new Slider(x, y, w, 20, "Size", 0.5, 4, cfg.scale, v -> cfg.scale = (float) v));
        y += step;

        // Color (hex)
        EditBox colorBox = new EditBox(this.font, x + 90, y, w - 90, 20, Component.literal("Color"));
        colorBox.setMaxLength(6);
        colorBox.setValue(String.format("%06X", cfg.color & 0xFFFFFF));
        colorBox.setResponder(s -> {
            try {
                if (s.length() == 6) cfg.color = Integer.parseInt(s, 16);
            } catch (NumberFormatException ignored) {
            }
        });
        addRenderableWidget(colorBox);
        y += step;

        addRenderableWidget(Button.builder(fontLabel(cfg), b -> {
            int i = FONTS.indexOf(cfg.fontId);
            cfg.fontId = FONTS.get((i + 1) % FONTS.size());
            b.setMessage(fontLabel(cfg));
        }).bounds(x, y, w, 20).build());
        y += step;

        addRenderableWidget(Button.builder(shadowLabel(cfg), b -> {
            cfg.shadow = !cfg.shadow;
            b.setMessage(shadowLabel(cfg));
        }).bounds(x, y, w, 20).build());
        y += step;

        addRenderableWidget(Button.builder(Component.literal("Test Timer"), b -> TimerHud.start())
                .bounds(x, y, w / 2 - 2, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Stop Timer"), b -> TimerHud.stop())
                .bounds(x + w / 2 + 2, y, w / 2 - 2, 20).build());
        y += step;

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(x, y, w, 20).build());
    }

    private static Component fontLabel(ThumbConfig cfg) {
        return Component.literal("Font: " + cfg.fontId);
    }

    private static Component shadowLabel(ThumbConfig cfg) {
        return Component.literal("Shadow: " + (cfg.shadow ? "ON" : "OFF"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFFFF);
        graphics.drawString(this.font, "Color (hex)", this.width / 2 - 100, 30 + 24 * 3 + 6, 0xFFFFFFFF);

        // Live preview with the current settings
        TimerHud.draw(graphics, TimerHud.isActive()
                ? "Thumb: " + TimerHud.format(TimerHud.remainingMs())
                : "Thumb: 10:00");
    }

    @Override
    public void removed() {
        ThumbConfig.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    private static class Slider extends AbstractSliderButton {
        private final String label;
        private final double min, max;
        private final DoubleConsumer setter;

        Slider(int x, int y, int w, int h, String label, double min, double max, double current, DoubleConsumer setter) {
            super(x, y, w, h, Component.empty(), (current - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.setter = setter;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (label == null) return;
            setMessage(Component.literal(label + ": " + String.format("%.2f", min + value * (max - min))));
        }

        @Override
        protected void applyValue() {
            setter.accept(min + value * (max - min));
        }
    }
}
