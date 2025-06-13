package com.zetalasis.mooncomputers.screen;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.MCBlocks;
import com.zetalasis.mooncomputers.block.entity.ScreenBlockEntity;
import com.zetalasis.mooncomputers.networking.MCPacketsC2S;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class ScreenBlockScreen extends HandledScreen<ScreenBlockScreenHandler> {
    private final ScreenBlockEntity screenEntity;

    public ScreenBlockScreen(ScreenBlockScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.screenEntity = handler.getScreenEntity();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

    }

    @Override
    protected void init() {
        super.init();
        titleY = 1000;
        playerInventoryTitleY = 1000;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.close();
            return true;
        }

        if (screenEntity.getWorld() == null)
            return super.keyPressed(keyCode, scanCode, modifiers);

        if (screenEntity.linkedComputer == null)
        {
            if (screenEntity.getLinkedComputer() == null)
                return super.keyPressed(keyCode, scanCode, modifiers);
        }

        String chr = InputUtil.fromKeyCode(keyCode, scanCode).getTranslationKey();
        if (chr.startsWith("key.keyboard.")) {
            chr = chr.substring("key.keyboard.".length());
        }

        BlockPos computerPos = screenEntity.linkedComputer;
        BlockState state = screenEntity.getWorld().getBlockState(computerPos);

        if (state.isOf(MCBlocks.COMPUTER_CASE))
        {
            MCPacketsC2S.computerInputEvent(computerPos, chr);
        }
        else
        {
            MoonComputers.LOGGER.error("Computer pos was not a computer!!!");
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        return true;
    }
}