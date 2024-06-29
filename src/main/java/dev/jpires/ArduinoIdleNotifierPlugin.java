package dev.jpires;

import com.fazecast.jSerialComm.SerialPort;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

import java.time.Instant;

import static net.runelite.api.AnimationID.IDLE;

@Slf4j
@PluginDescriptor(
        name = "Arduino Idle Notifier",
        description = "Run a command on a connected arduino when the player goes into idle",
        tags = {"arduino", "idle", "notifier"}
)
public class ArduinoIdleNotifierPlugin extends Plugin {
    private SerialPort serialPort;
    private boolean isIdle = false;
    private boolean idleCommandSent = false;
    private Instant lastIdleTime;

    @Inject
    private Client client;

    @Inject
    private ArduinoIdleNotifierConfig config;

    @Override
    protected void startUp() throws Exception {
    }

    @Override
    protected void shutDown() throws Exception {
        if (serialPort != null) {
            serialPort.closePort();
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            serialPort = SerialPort.getCommPort(config.port());
            serialPort.setBaudRate(config.baudRate());
            if (serialPort.openPort())
                if (config.debug())
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Connected to Arduino on port " + config.port(), null);
            else
                if (config.debug())
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Failed to connect to Arduino on port " + config.port(), null);
            lastIdleTime = Instant.now();
        } else if (gameStateChanged.getGameState() == GameState.LOGIN_SCREEN) {
            if (serialPort != null)
                serialPort.closePort();
        }
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        Player player = client.getLocalPlayer();
        int idlePose = player.getIdlePoseAnimation();
        int currentPose = player.getPoseAnimation();
        int currentAnimation = player.getAnimation();

        if (currentAnimation == IDLE && currentPose == idlePose && !isIdle) {
            isIdle = true;
            lastIdleTime = Instant.now();
        }

        if (currentAnimation == IDLE && currentPose == idlePose && isIdle && !idleCommandSent) {
            if (lastIdleTime.plusSeconds(config.idleBuffer()).isAfter(Instant.now()))
                return;
            sendCommandToArduino(config.command());
            if (config.debug())
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Player is idle, sending command to Arduino", null);
            idleCommandSent = true;
        } else if ((currentAnimation != IDLE || currentPose != idlePose) && isIdle) {
            isIdle = false;
            idleCommandSent = false;
            sendCommandToArduino(config.notIdleCommand());
            if (config.debug())
                client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Player is no longer in idle, sending command to Arduino", null);
        }
    }

    @Provides
    ArduinoIdleNotifierConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ArduinoIdleNotifierConfig.class);
    }

    public void sendCommandToArduino(String message) {
        byte[] bytes = message.getBytes();
        serialPort.writeBytes(bytes, bytes.length);
    }
}
