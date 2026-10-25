package dev.fartiqs.auction;

import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class AuctionState {
    public int tab = 0;
    public Item selectedItem = Items.DIAMOND;
    public String itemSearch = "";
    public long minimumBid = 100;
    public long minimumIncrement = 10;
    public int durationSeconds = 60;
    public boolean running = false;
    public long endAtMillis = 0;
    public long highestBid = 0;
    public String highestBidder = "Nobody yet";

    public void tick() {
        if (running && System.currentTimeMillis() >= endAtMillis) {
            running = false;
        }
    }

    public long secondsLeft() {
        if (!running) return 0;
        return Math.max(0, (endAtMillis - System.currentTimeMillis() + 999) / 1000);
    }
}
