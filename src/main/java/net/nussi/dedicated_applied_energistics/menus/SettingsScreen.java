package net.nussi.dedicated_applied_energistics.menus;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.nussi.dedicated_applied_energistics.DedicatedAppliedEnegistics;

public class SettingsScreen extends Screen {

    private static final int IMAGE_SETTINGS_WIDTH = 800;
    private static final int IMAGE_SETTINGS_HEIGHT = 500;

    private static final ResourceLocation IMAGE_SETTINGS_INFO = new ResourceLocation(DedicatedAppliedEnegistics.MODID, "textures/gui/container/settings_info.png");
    private static final ResourceLocation IMAGE_SETTINGS_CREDITS = new ResourceLocation(DedicatedAppliedEnegistics.MODID, "textures/gui/container/settings_credits.png");
    private static final ResourceLocation IMAGE_SETTINGS_DATABASE = new ResourceLocation(DedicatedAppliedEnegistics.MODID, "textures/gui/container/settings_database.png");

    public SettingsScreen() {
        super(Component.literal("Settings Menu"));
    }

    Button button;

    @Override
    protected void init() {
        super.init();
        this.minecraft = Minecraft.getInstance();

        int buttonX = (width - 200) / 2;
        int buttonY = (height - 20) / 2;

        this.button = this.addRenderableWidget(new Button(buttonX, buttonY, 200, 20, Component.literal("Click Me"), (button) -> {
            // Handle button click
            // You can perform actions when the button is clicked
            this.minecraft.player.sendSystemMessage(Component.literal("Hello World!"));
        }));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.minecraft = Minecraft.getInstance();
        Font font = minecraft.font;



        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.setShaderTexture(0, IMAGE_SETTINGS_INFO);
        blit(poseStack, 0, 0, 0, 0, IMAGE_SETTINGS_WIDTH, IMAGE_SETTINGS_HEIGHT, IMAGE_SETTINGS_WIDTH, IMAGE_SETTINGS_HEIGHT);

//        String text = "Hello, Minecraft!";
//        int x = width / 2 - font.width(text);
//        int y = height / 2 - 10;
//        int color = 0xFFFFFF;
//        GuiComponent.drawString(poseStack, font, text, x, y, color);


        // Add ticking logic for EditBox in editBox
        if(this.button != null) this.button.renderButton(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
    }
}
