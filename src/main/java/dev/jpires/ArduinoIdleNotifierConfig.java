package dev.jpires;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("arduinoidlenotifier")
public interface ArduinoIdleNotifierConfig extends Config {

    @ConfigItem(
            keyName = "port",
            name = "Serial Port",
            description = "The serial port to use for the Arduino",
            position = 0
    )
    default String port() {
        return "COM3";
    }

    @ConfigItem(
            keyName = "baudRate",
            name = "Baud Rate",
            description = "The baud rate to use for the Arduino",
            position = 1
    )
    default int baudRate() {
        return 9600;
    }

    @ConfigItem(
            keyName = "idleBuffer",
            name = "Idle Buffer",
            description = "The time in seconds before the player is considered idle",
            position = 2
    )
    default int idleBuffer() {
        return 5;
    }

    @ConfigItem(
            keyName = "idleCommand",
            name = "Idle Command",
            description = "The command to run when the player goes into idle",
            position = 3
    )
    default String command() {
        return "1";
    }

    @ConfigItem(
            keyName = "notIdleCommand",
            name = "Not Idle Command",
            description = "The command to run when the player is no longer in idle",
            position = 4
    )
    default String notIdleCommand() {
        return "0";
    }


    @ConfigItem(
            keyName = "debug",
            name = "Debug",
            description = "Enable debug messages",
            position = 5
    )
    default boolean debug() {
        return false;
    }
}
