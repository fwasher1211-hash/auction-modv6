package dev.fartiqs.auction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuctionScreen extends Screen {
    private final AuctionState state = FartiqsAuctionClient.STATE;
    private TextFieldWidget searchField;
    private TextFieldWidget minimumField;
    private TextFieldWidget incrementField;
    private TextFieldWidget durationField;
    private List<Item> visibleItems = new ArrayList<>();
    private int itemPage = 0;
    private static final int PANEL_W = 390;
    private static final int PANEL_H = 250;

    public AuctionScreen() {
        super(Text.literal("Fartiqs Lounge • Auction"));
    }

    @Override
    protected void init() {
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("1  ITEM"), b -> state.tab = 0)
            .dimensions(left + 16, top + 42, 112, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("2  PRICE"), b -> { saveFields(); state.tab = 1; })
            .dimensions(left + 139, top + 42, 112, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("3  TIMER"), b -> { saveFields(); state.tab = 2; })
            .dimensions(left + 262, top + 42, 112, 22).build());

        searchField = new TextFieldWidget(textRenderer, left + 22, top + 82, 346, 20, Text.literal("Search items"));
        searchField.setPlaceholder(Text.literal("Search every registered item..."));
        searchField.setText(state.itemSearch);
        addDrawableChild(searchField);

        minimumField = new TextFieldWidget(textRenderer, left + 22, top + 100, 160, 20, Text.literal("Minimum bid"));
        minimumField.setText(Long.toString(state.minimumBid));
        addDrawableChild(minimumField);
        incrementField = new TextFieldWidget(textRenderer, left + 208, top + 100, 160, 20, Text.literal("Minimum increment"));
        incrementField.setText(Long.toString(state.minimumIncrement));
        addDrawableChild(incrementField);
        durationField = new TextFieldWidget(textRenderer, left + 22, top + 100, 346, 20, Text.literal("Duration"));
        durationField.setText(Integer.toString(state.durationSeconds));
        addDrawableChild(durationField);

        addDrawableChild(ButtonWidget.builder(Text.literal("‹"), b -> { itemPage = Math.max(0, itemPage - 1); refreshItems(); })
            .dimensions(left + 22, top + 184, 28, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("›"), b -> { itemPage++; refreshItems(); })
            .dimensions(left + 340, top + 184, 28, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("START AUCTION"), b -> {
            saveFields();
            FartiqsAuctionClient.startAuction(MinecraftClient.getInstance());
        }).dimensions(left + 22, top + 210, 170, 24).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("STOP / CLOSE"), b -> {
            saveFields();
            state.running = false;
            MinecraftClient.getInstance().setScreen(null);
        }).dimensions(left + 198, top + 210, 170, 24).build());

        refreshItems();
        updateTabVisibility();
    }

    private void updateTabVisibility() {
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;
        boolean itemTab = state.tab == 0;
        boolean priceTab = state.tab == 1;
        boolean timerTab = state.tab == 2;
        searchField.visible = itemTab;
        minimumField.visible = priceTab;
        incrementField.visible = priceTab;
        durationField.visible = timerTab;
        searchField.active = itemTab;
        minimumField.active = priceTab;
        incrementField.active = priceTab;
        durationField.active = timerTab;
        minimumField.setY(top + 100);
        incrementField.setY(top + 100);
        durationField.setY(top + 100);
    }

    private void refreshItems() {
        String query = state.itemSearch.toLowerCase(Locale.ROOT).trim();
        visibleItems = Registries.ITEM.stream()
            .filter(item -> !query.isEmpty()
                ? Registries.ITEM.getId(item).toString().toLowerCase(Locale.ROOT).contains(query)
                : true)
            .toList();
        if (visibleItems.isEmpty()) itemPage = 0;
        else itemPage = Math.min(itemPage, Math.max(0, (visibleItems.size() - 1) / 6));
    }

    private void saveFields() {
        if (searchField != null) state.itemSearch = searchField.getText();
        if (minimumField != null) {
            try { state.minimumBid = Math.max(0, Long.parseLong(minimumField.getText())); } catch (Exception ignored) {}
        }
        if (incrementField != null) {
            try { state.minimumIncrement = Math.max(1, Long.parseLong(incrementField.getText())); } catch (Exception ignored) {}
        }
        if (durationField != null) {
            try { state.durationSeconds = Math.max(10, Math.min(3600, Integer.parseInt(durationField.getText()))); } catch (Exception ignored) {}
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;
        context.fill(left, top, left + PANEL_W, top + PANEL_H, 0xF0101420);
        context.fill(left, top, left + PANEL_W, top + 3, 0xFF19BFFF);
        context.drawText(textRenderer, "FARTIQS LOUNGE", left + 18, top + 14, 0xFFFFFFFF, false);
        context.drawText(textRenderer, "AUCTION CONTROL  /  1.21.11", left + 18, top + 28, 0xFF70CFFF, false);
        context.drawText(textRenderer, state.running ? "● LIVE" : "○ READY", left + 300, top + 16,
            state.running ? 0xFF4CFF9B : 0xFF9BA7B8, false);

        if (state.tab == 0) {
            refreshItems();
            int start = itemPage * 6;
            int end = Math.min(visibleItems.size(), start + 6);
            int y = top + 112;
            for (int i = start; i < end; i++) {
                Item item = visibleItems.get(i);
                int col = (i - start) % 3;
                int row = (i - start) / 3;
                int x = left + 22 + col * 116;
                int iy = y + row * 29;
                boolean selected = item == state.selectedItem;
                context.fill(x, iy, x + 108, iy + 24, selected ? 0xFF164C67 : 0xFF202A3A);
                context.drawItem(item.getDefaultStack(), x + 4, iy + 4);
                String label = Registries.ITEM.getId(item).getPath();
                if (label.length() > 13) label = label.substring(0, 12) + "…";
                context.drawText(textRenderer, label, x + 25, iy + 8, selected ? 0xFF7FE0FF : 0xFFE2EAF4, false);
            }
            context.drawText(textRenderer, "Selected: " + Registries.ITEM.getId(state.selectedItem), left + 22, top + 169, 0xFFB7C8D9, false);
        } else if (state.tab == 1) {
            context.drawText(textRenderer, "Minimum starting bid", left + 22, top + 86, 0xFFB7C8D9, false);
            context.drawText(textRenderer, "Minimum raise per bid", left + 208, top + 86, 0xFFB7C8D9, false);
            context.drawText(textRenderer, "Starting at: " + state.minimumBid + "  •  Raise: " + state.minimumIncrement,
                left + 22, top + 145, 0xFF70CFFF, false);
        } else {
            context.drawText(textRenderer, "Duration in seconds (10–3600)", left + 22, top + 86, 0xFFB7C8D9, false);
            context.drawText(textRenderer, "Time remaining: " + state.secondsLeft() + "s", left + 22, top + 145, 0xFF70CFFF, false);
        }

        context.drawText(textRenderer, "Highest: " + state.highestBid + "  •  " + state.highestBidder,
            left + 22, top + 196, 0xFFFFFFFF, false);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;
        if (state.tab == 0 && button == 0) {
            int start = itemPage * 6;
            int end = Math.min(visibleItems.size(), start + 6);
            for (int i = start; i < end; i++) {
                int col = (i - start) % 3;
                int row = (i - start) / 3;
                int x = left + 22 + col * 116;
                int y = top + 112 + row * 29;
                if (mouseX >= x && mouseX <= x + 108 && mouseY >= y && mouseY <= y + 24) {
                    state.selectedItem = visibleItems.get(i);
                    return true;
                }
            }
        }
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (result) updateTabVisibility();
        return result;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean handled = super.charTyped(chr, modifiers);
        if (handled && state.tab == 0) {
            state.itemSearch = searchField.getText();
            itemPage = 0;
        }
        return handled;
    }

    @Override
    public void close() {
        saveFields();
        super.close();
    }
}
