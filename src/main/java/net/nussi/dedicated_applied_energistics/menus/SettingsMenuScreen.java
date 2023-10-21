package net.nussi.dedicated_applied_energistics.menus;

import appeng.client.gui.WidgetContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.server.command.TextComponentHelper;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;

import java.awt.*;

public class SettingsMenuScreen extends Screen {

    private static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation(DedicatedAppliedEnegistics.MODID, "textures/gui/container/icon.png");

    public SettingsMenuScreen() {
        super(Component.literal("Settings Menu"));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(poseStack);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, BACKGROUND_LOCATION);
        GuiComponent.blit(poseStack, 0, 0, 0, 0, 1000, 1000, 1000, 1000);

        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
