package com.zetalasis.mooncomputers.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.ComputerCase;
import com.zetalasis.mooncomputers.networking.MCPacketsC2S;
import com.zetalasis.mooncomputers.networking.MCPacketsS2C;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComputerCaseScreen extends HandledScreen<ComputerCaseScreenHandler> {
    private static final Identifier BACKGROUND = MoonComputers.location("textures/gui/computer_case_gui.png");
    private final ComputerCaseScreenHandler handler;

    public ComputerCaseScreen(ComputerCaseScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, BACKGROUND);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(BACKGROUND, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    @Override
    protected void init() {
        super.init();
//        titleY = 1000;
//        playerInventoryTitleY = 1000;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        int guiPosX = mouseX - x;
        int guiPosY = mouseY - y;

        if (isHoveringOverPowerButton(guiPosX, guiPosY))
        {
            drawSlotHighlight(context, 135, 33, 0);
        }
    }

    private boolean isHoveringOverPowerButton(int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        int buttonX = x + 135;
        int buttonY = y + 33;

        return mouseX >= buttonX && mouseY >= buttonY &&
                mouseX <= buttonX + 15 && mouseY <= buttonY + 15;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHoveringOverPowerButton((int) Math.round(mouseX), (int) Math.round(mouseY)))
        {
            MoonComputers.LOGGER.info("Computer powering on!");

            MCPacketsC2S.updateComputerState(handler.pos, MCPacketsS2C.ComputerUpdateContext.TOGGLE_POWER);

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}