package com.zetalasis.mooncomputers.networking;

import com.zetalasis.mooncomputers.MoonComputers;
import net.minecraft.util.Identifier;

public class MCMessages {
    public static final Identifier UPDATE_COMPUTER_STATE_C2S = MoonComputers.location("update_computer_state_c2s");
    public static final Identifier COMPUTER_INPUT_EVENT_C2S = MoonComputers.location("computer_input_event");

    public static final Identifier UPDATE_COMPUTER_STATE_S2C = MoonComputers.location("update_computer_state_s2c");
    public static final Identifier UPDATE_SCREEN_STATE_S2C = MoonComputers.location("update_screen_state_s2c");
}