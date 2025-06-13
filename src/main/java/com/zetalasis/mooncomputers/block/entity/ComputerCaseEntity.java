package com.zetalasis.mooncomputers.block.entity;

import com.zetalasis.mooncomputers.MoonComputers;
import com.zetalasis.mooncomputers.block.ComputerCase;
import com.zetalasis.mooncomputers.block.MCBlocks;
import com.zetalasis.mooncomputers.computer.LuaMethods;
import com.zetalasis.mooncomputers.computer.VirtualizedComputer;
import com.zetalasis.mooncomputers.computer.device.FileIO;
import com.zetalasis.mooncomputers.computer.device.GraphicsCard;
import com.zetalasis.mooncomputers.computer.device.IMemoryMappedIO;
import com.zetalasis.mooncomputers.computer.device.NetworkIO;
import com.zetalasis.mooncomputers.entity.MCEntities;
import com.zetalasis.mooncomputers.screen.ComputerCaseScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

public class ComputerCaseEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private boolean powered = false;
    public @Nullable BlockPos linkedScreenPos;
    public @Nullable ScreenBlockEntity screen;
    public VirtualizedComputer computer;

    private static final int FLOPPY_SLOT = 1;

    public ComputerCaseEntity(BlockPos pos, BlockState state) {
        super(MCEntities.COMPUTER_CASE_ENTITY, pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {
        packetByteBuf.writeBlockPos(pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.moon-computers.computer_case.title");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ComputerCaseScreenHandler(syncId, playerInventory, this);
    }

    private int tickCount = 0;
    public void tick(World world, BlockPos pos, BlockState state)
    {
        if (world.isClient)
            return;

        if (this.computer == null || this.screen == null)
            return;

        this.computer.tick();

        tickCount++;

        if (tickCount % 10 == 1)
        {
            NetworkIO network = this.computer.getDevice(NetworkIO.class);
            network.send(("Hello from "+this.pos).getBytes(StandardCharsets.UTF_8));
        }
    }

    public void onComputerCreate()
    {
        assert screen != null;

        NetworkIO network = this.computer.getDevice(NetworkIO.class);
        network.listen((rfPacket) -> {
            if (network.owns(rfPacket))
                return;

            screen.printLine("[Network]: Got message \"" + new String(rfPacket.data, StandardCharsets.UTF_8) + "\"");

            screen.flush();
        });

        FileIO fileIO = this.computer.getDevice(FileIO.class);

        byte[] rawShellCode = fileIO.read(fileIO.resolve("shell.lua"));
        if (rawShellCode == null)
        {
            screen.printLine("Failed to execute shell.lua\nFileNotFound exception");
            return;
        }

        computer.loadScript(new String(rawShellCode, StandardCharsets.UTF_8));
        MoonComputers.LOGGER.info("Executing shell code!");
    }

    public void destroy()
    {
        if (this.screen != null)
        {
            this.screen.clear();
        }

        if (this.computer != null)
        {
            this.computer.shutdown();
            this.computer = null;
        }
    }

    private @Nullable ScreenBlockEntity searchForScreen()
    {
        if (this.world == null)
            return null;

        for (Direction d : Direction.values())
        {
            BlockPos dPos = pos.offset(d);
            BlockState dState = world.getBlockState(dPos);

            if (dState.isOf(MCBlocks.SCREEN))
            {
                linkedScreenPos = dPos;
                ScreenBlockEntity entity = (ScreenBlockEntity) world.getBlockEntity(dPos);

                assert entity != null;

                return entity;
            }
        }

        return null;
    }

    public @Nullable ScreenBlockEntity getScreen()
    {
        if (this.world == null)
            return null;

        if (this.linkedScreenPos == null)
            return searchForScreen();

        BlockState state = world.getBlockState(this.linkedScreenPos);

        if (!state.isOf(MCBlocks.SCREEN))
            return searchForScreen();

        ScreenBlockEntity entity = (ScreenBlockEntity) world.getBlockEntity(this.linkedScreenPos);
        assert entity != null;

        this.screen = entity;

        return entity;
    }

    public void update(World world, BlockPos pos, BlockState state, boolean powerState)
    {
        if (powerState == !powered)
        {
            world.setBlockState(pos, state.with(ComputerCase.POWERED, powerState));
            powered = powerState;

            if (powered)
            {
                getScreen();
                assert screen != null;

                screen.linkedComputer = this.pos;

                this.computer = new VirtualizedComputer(16, (string)->{
                    screen.printLine(string);
                    screen.flush();
                });

                screen.clear();

                screen.printLine("===============================");
                screen.printLine("Linked to computer!");
                screen.printLine(
                        "Computer Info: " + computer.PAGE_TABLE.size() + " pages | "
                                + (computer.PAGE_TABLE.size() * 4096) + "kb Hardware Memory | Device Tree:");

                for (IMemoryMappedIO device : computer.DEVICE_TREE.values())
                {
                    screen.printLine(device.getHID() + " | ID 0x" + Integer.toHexString(device.getId()) + " | Address 0x" + Integer.toHexString(device.getBaseAddress()));
                }
                screen.printLine("===============================");
                screen.printLine("Loading Lua...");
                LuaMethods.bootstrap(computer.luaGlobals, this);
                computer.loadScript("print('Printing works!') warn('So does warnings!') error('And errors!') function tick() end");
                screen.printLine("Loaded!");
                screen.printLine("===============================");


                if (computer.DEVICE_TREE.get(computer.PAGE_TABLE.get(0)) instanceof GraphicsCard graphicsCard)
                {
                    graphicsCard.render();
                }

                screen.flush();

                onComputerCreate();
            }
            else
            {
                if (this.computer == null)
                    return;

                this.computer.shutdown();
                this.computer = null;
            }
        }
    }

    public void handleInput(World world, BlockPos pos, BlockState state, String input)
    {
        MoonComputers.LOGGER.info("Handling input {}",input);
        if (powered && computer != null && screen != null)
        {
            computer.handleInput(input);
        }
    }
}