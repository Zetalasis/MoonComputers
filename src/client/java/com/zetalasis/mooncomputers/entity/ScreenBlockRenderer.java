package com.zetalasis.mooncomputers.entity;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.MCBlocks;
import com.zetalasis.mooncomputers.block.ScreenBlock;
import com.zetalasis.mooncomputers.block.entity.ScreenBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.List;

public class ScreenBlockRenderer implements BlockEntityRenderer<ScreenBlockEntity> {
    public ScreenBlockRenderer(BlockEntityRendererFactory.Context context)
    {

    }

    @Override
    public void render(ScreenBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        World world = entity.getWorld();
        if (world == null) return;

        BlockPos pos = entity.getPos();
        BlockState state = world.getBlockState(pos);
        if (!state.isOf(MCBlocks.SCREEN)) return;

        Direction direction = state.get(ScreenBlock.FACING);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (entity.vram == null || entity.vram.length < 3) return;

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);

        float angle = switch (direction) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST  -> 90f;
            case EAST  -> -90f;
            default    -> 0f;
        };
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));

        if (direction == Direction.NORTH || direction == Direction.SOUTH)
            matrices.translate(0.0, 0.0, 0.501);
        else
            matrices.translate(0.501, 0.0, 0.0);

        // ==== TEXT RENDERING ====
        List<String> lines = entity.getScreenLines();
        float textScale = (1f / 512f);
        matrices.push();
        matrices.scale(textScale, -textScale, textScale);
        matrices.translate((-1 / textScale / 2f) - (-(1f / 8f) / textScale), (-1 / textScale / 2f) - (-(1f / 8f) / textScale), 0);

        int y = 0;
        for (String line : lines) {
            textRenderer.draw(
                    line,
                    0, y,
                    0xFFFFFFFF,
                    false,
                    matrices.peek().getPositionMatrix(),
                    vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL,
                    0,
                    LightmapTextureManager.MAX_LIGHT_COORDINATE
            );
            y += textRenderer.fontHeight + 1;
        }
        matrices.pop();

        // ==== BITMAP RENDERING ====
        /* VRAM Layout
        *   0x0: Mode
        *   0x1: Width
        *   0x2: Height
        *   0x3 - 0x16k: Pixel Data */
        byte[] vram = entity.vram;
        int mode = Byte.toUnsignedInt(vram[0]);
        int width = Byte.toUnsignedInt(vram[1]);
        int height = Byte.toUnsignedInt(vram[2]);

        /* Modes
        *   0: Text Only
        *   1: 128x128 Monochrome (unimplemented)
        *   2. 64x64 Color */
        if (mode != 1 && mode != 2) {
            matrices.pop();
            return; // not in bitmap mode
        }

        float pixelScale = 1.0f / width;
        float borderScale = 0.7f;

        matrices.scale(pixelScale, pixelScale, pixelScale);
        matrices.translate(-width / 2.0, -height / 2.0, 0.01f);
        matrices.scale(borderScale, borderScale, borderScale);
        matrices.translate(width * 0.2f, height * 0.2f, 0);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getDebugQuads());

        for (int yPix = 0; yPix < height; yPix++) {
            for (int xPix = 0; xPix < width; xPix++) {
                int pixelIndex = yPix * width + xPix;
                int base = 3 + pixelIndex * 3;
                if (base + 2 >= vram.length) continue;

                float r = Byte.toUnsignedInt(vram[base]) / 255f;
                float g = Byte.toUnsignedInt(vram[base + 1]) / 255f;
                float b = Byte.toUnsignedInt(vram[base + 2]) / 255f;

                vc.vertex(matrix, xPix, yPix, 0).color(r, g, b, 1.0f).next();
                vc.vertex(matrix, xPix + 1, yPix, 0).color(r, g, b, 1.0f).next();
                vc.vertex(matrix, xPix + 1, yPix + 1, 0).color(r, g, b, 1.0f).next();
                vc.vertex(matrix, xPix, yPix + 1, 0).color(r, g, b, 1.0f).next();
            }
        }

        matrices.pop();
    }
}