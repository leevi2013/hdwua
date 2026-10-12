package de.gambamod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1109;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3417;
import net.minecraft.class_3675;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GambaClient implements ClientModInitializer {
    private static class_304 openMenuKey;
    private static final Path COIN_FILE = FabricLoader.getInstance().getConfigDir().resolve("gambamod-coins.txt");
    private static int coins = 1000;

    @Override
    public void onInitializeClient() {
        loadCoins();
        openMenuKey = KeyBindingHelper.registerKeyBinding(new class_304(
                "key.gambamod.open",
                class_3675.class_307.field_1668,
                GLFW.GLFW_KEY_K,
                net.minecraft.class_304.class_11900.method_74698(class_2960.method_60655("gambamod", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.method_1436()) {
                if (client.field_1724 != null && client.field_1687 != null) {
                    client.method_1507(new GambaScreen(GambaScreen.Mode.MENU));
                    playSound(class_3417.field_15015.comp_349(), 1.0f);
                }
            }
        });
    }

    public static int getCoins() {
        return coins;
    }

    public static void addCoins(int amount) {
        coins = Math.max(0, coins + amount);
        saveCoins();
    }

    public static boolean spendCoins(int amount) {
        if (amount <= 0 || coins < amount) {
            return false;
        }
        coins -= amount;
        saveCoins();
        return true;
    }

    public static void playSound(net.minecraft.class_3414 sound, float pitch) {
        class_310 client = class_310.method_1551();
        if (client != null) {
            client.method_1483().method_4873(class_1109.method_4758(sound, pitch));
        }
    }

    private static void loadCoins() {
        try {
            if (Files.exists(COIN_FILE)) {
                String raw = Files.readString(COIN_FILE, StandardCharsets.UTF_8).trim();
                coins = Math.max(0, Integer.parseInt(raw));
            } else {
                saveCoins();
            }
        } catch (IOException | NumberFormatException ignored) {
            coins = 1000;
        }
    }

    private static void saveCoins() {
        try {
            Files.createDirectories(COIN_FILE.getParent());
            Files.writeString(COIN_FILE, Integer.toString(coins), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // The current session continues to work even if the config file cannot be written.
        }
    }
}
