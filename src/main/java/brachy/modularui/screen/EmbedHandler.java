package brachy.modularui.screen;

import brachy.modularui.api.IMuiScreen;
import brachy.modularui.utils.Rectangle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class EmbedHandler {

    public static int getEmbedWidth(ModularScreen screen) {
        return screen.getMainPanel().getArea().width;
    }

    public static int getEmbedHeight(ModularScreen screen) {
        return screen.getMainPanel().getArea().height;
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawEmbed(screen, graphics, mouseX, mouseY, partialTicks, null, null);
    }

    public static void drawEmbedNoVanillaElements(ModularScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawEmbedNoVanillaElements(screen, graphics, mouseX, mouseY, partialTicks, null, null);
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks,
                                 @Nullable Runnable beforeDraw, @Nullable Runnable afterDraw) {
        drawEmbed(screen, graphics, mouseX, mouseY, partialTicks, r -> true, beforeDraw, afterDraw);
    }

    public static void drawEmbedNoVanillaElements(ModularScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks,
                                                  @Nullable Runnable beforeDraw, @Nullable Runnable afterDraw) {
        drawEmbed(screen, graphics, mouseX, mouseY, partialTicks, r -> false, beforeDraw, afterDraw);
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks,
                                 Predicate<Renderable> vanillaElementFilter) {
        drawEmbed(screen, graphics, mouseX, mouseY, partialTicks, vanillaElementFilter, null, null);
    }

    public static void drawEmbed(ModularScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks,
                                 Predicate<Renderable> vanillaElementFilter, @Nullable Runnable beforeDraw, @Nullable Runnable afterDraw) {
        screen.getContext().reset();
        graphics.pose().pushPose();

        if (beforeDraw != null) beforeDraw.run();

        int mx = screen.getContext().unTransformX(mouseX, mouseY);
        int my = screen.getContext().unTransformY(mouseX, mouseY);
        screen.render(graphics, mx, my, partialTicks);

        if (vanillaElementFilter != null) {
            RenderSystem.disableDepthTest();
            ClientScreenHandler.drawVanillaElements(graphics, screen.getScreenWrapper().wrappedScreen(), mouseX, mouseY, partialTicks, vanillaElementFilter);
            RenderSystem.enableDepthTest();
        }

        screen.drawForeground(graphics);

        if (afterDraw != null) afterDraw.run();

        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        graphics.pose().popPose();
    }

    public record EmbedWrapper(ModularScreen screen) implements IMuiScreen {

        @Override
        public Screen wrappedScreen() {
            return Minecraft.getInstance().screen;
        }

        @Override
        public void updateGuiArea(Rectangle area) {}
    }
}
