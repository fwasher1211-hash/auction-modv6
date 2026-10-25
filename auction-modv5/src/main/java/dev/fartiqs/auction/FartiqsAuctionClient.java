package dev.fartiqs.auction;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FartiqsAuctionClient implements ClientModInitializer {
    public static final AuctionState STATE = new AuctionState();
    private static KeyBinding openGui;
    private static final Pattern BID_PATTERN = Pattern.compile(
        "(?i)(?:highest bid|new bid|bid|paid)\\D{0,30}([A-Za-z0-9_]{3,16})?\\D{0,12}\\$?([0-9][0-9,]*)"
    );

    @Override
    public void onInitializeClient() {
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fartiqs_auction.open",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyBinding.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) {
                if (client.currentScreen instanceof AuctionScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new AuctionScreen());
                }
            }
            STATE.tick();
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!STATE.running) return;
            String line = message.getString();
            Matcher matcher = BID_PATTERN.matcher(line);
            if (matcher.find()) {
                String bidder = matcher.group(1);
                String amount = matcher.group(2).replace(",", "");
                try {
                    long bid = Long.parseLong(amount);
                    if (bid > STATE.highestBid) {
                        STATE.highestBid = bid;
                        STATE.highestBidder = (bidder == null || bidder.isBlank()) ? "Unknown" : bidder;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore chat numbers too large for a long.
                }
            }
        });
    }

    public static void startAuction(MinecraftClient client) {
        if (STATE.selectedItem == null) {
            if (client.player != null) client.player.sendMessage(Text.literal("§b[Fartiqs] §fChoose an item first."), false);
            return;
        }
        STATE.running = true;
        STATE.endAtMillis = System.currentTimeMillis() + STATE.durationSeconds * 1000L;
        STATE.highestBid = STATE.minimumBid;
        STATE.highestBidder = "Starting bid";
        if (client.player != null) {
            client.player.sendMessage(Text.literal("§b[Fartiqs] §fAuction timer started locally. This mod does not create a server auction or transfer money."), false);
        }
    }
}
