package com.zetalasis.mooncomputers.block.entity;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.MCBlocks;
import com.zetalasis.mooncomputers.computer.memory.MemoryPage;
import com.zetalasis.mooncomputers.entity.MCEntities;
import com.zetalasis.mooncomputers.networking.MCPacketsS2C;
import com.zetalasis.mooncomputers.screen.ScreenBlockScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ScreenBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory {
    private List<String> screenLines = new ArrayList<>();
    public BlockPos linkedComputer;
    public byte[] vram;

    public ScreenBlockEntity(BlockPos pos, BlockState state) {
        super(MCEntities.SCREEN_ENTITY, pos, state);
    }

    public void tick(World world, BlockPos pos, BlockState state)
    {

    }

    public List<String> getScreenLines() {
        return screenLines;
    }

    public void setScreenLines(List<String> lines)
    {
        this.screenLines = lines;
    }

    public void printLine(String line) {
        if (screenLines.size() > 17) {
            screenLines.remove(0);
        }
        screenLines.add(line);
    }

    public void clear()
    {
        vram = new byte[4096*4];
        screenLines.clear();
        this.flush();
    }

    /** Flush the new text data and send it to the client */
    public void flush() {
        if (world == null || world.isClient || linkedComputer == null)
            return;

        BlockState computerState = world.getBlockState(linkedComputer);
        if (!computerState.isOf(MCBlocks.COMPUTER_CASE))
            return;

        ComputerCaseEntity computer = (ComputerCaseEntity) world.getBlockEntity(linkedComputer);
        assert computer != null;

        // Read first few pages — match however many you give the GPU (e.g., 4)
        int pageCount = 4;
        ByteArrayOutputStream combined = new ByteArrayOutputStream(4096 * pageCount);

        for (int i = 0; i < pageCount; i++) {
            MemoryPage page = computer.computer.PAGE_TABLE.get(i);
            if (page == null) continue;

            byte[] chunk = page.read(page.getBaseAddress(), 4096);
            if (chunk != null)
                combined.write(chunk, 0, chunk.length);
        }

        this.vram = combined.toByteArray();

        for (PlayerEntity sp : world.getPlayers())
            MCPacketsS2C.updateScreenState((ServerPlayerEntity) sp, this.pos, screenLines, vram);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        packetByteBuf.writeBlockPos(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Screen");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ScreenBlockScreenHandler(syncId, playerInventory, this);
    }

    public BlockPos getLinkedComputer()
    {
        for (Direction d : Direction.values())
        {
            BlockPos dPos = pos.offset(d);
            BlockState dState = world.getBlockState(dPos);

            if (dState.isOf(MCBlocks.COMPUTER_CASE))
            {
                linkedComputer = dPos;
                return dPos;
            }
        }

        linkedComputer = null;
        return null;
    }
}
