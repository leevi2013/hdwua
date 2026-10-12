package de.gambamod.client;

import org.lwjgl.glfw.GLFW;

import java.util.Random;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import net.minecraft.class_437;

/** Client-side, inventory-inspired Gamba minigame UI. All clickable controls are icon-only. */
public final class GambaScreen extends class_437 {
    public enum Mode { MENU, SLOTS, CHEST }

    private static final Random RANDOM = new Random();
    private static final int PANEL_W = 200;
    private static final int PANEL_H = 150;
    private static final int CELL = 18;
    private static final int GRID_W = 9 * CELL;
    private static final int GRID_H = 3 * CELL;

    private final Mode mode;
    private final class_1792[] reels = new class_1792[9];
    private final class_1792[] chestRow = new class_1792[9];

    private int bet = 10;
    private boolean spinning;
    private long spinStartedAt;
    private long spinDurationMs;
    private long spinEndAt;
    private long nextReelChangeAt;
    private long nextReelSoundAt;
    private String status = "";

    private boolean chestOpening;
    private boolean chestRevealed;
    private boolean chestPendingWin;
    private long chestEndAt;
    private long nextChestChangeAt;

    public GambaScreen(Mode mode) {
        super(class_2561.method_43470(switch (mode) {
            case MENU -> "Gamba";
            case SLOTS -> "Slotsmaschine";
            case CHEST -> "Endertruhe";
        }));
        this.mode = mode;
        for (int i = 0; i < reels.length; i++) {
            reels[i] = randomSlotItem();
        }
        for (int i = 0; i < chestRow.length; i++) {
            chestRow[i] = randomJunkItem();
        }
    }

    @Override
    public boolean method_25421() {
        return false;
    }

    @Override
    public void method_25393() {
        super.method_25393();
        long now = System.currentTimeMillis();

        if (spinning) {
            if (now >= spinEndAt) {
                finishSpin();
            } else if (now >= nextReelChangeAt) {
                for (int i = 0; i < reels.length; i++) {
                    reels[i] = randomSlotItem();
                }
                double progress = Math.min(1.0, (now - spinStartedAt) / (double) spinDurationMs);
                long interval = 50L + Math.round(180.0 * progress * progress);
                nextReelChangeAt = now + interval;
                if (now >= nextReelSoundAt) {
                    GambaClient.playSound(class_3417.field_14622.comp_349(),
                            0.7f + RANDOM.nextFloat() * 0.5f);
                    nextReelSoundAt = now + 180L;
                }
            }
        }

        if (chestOpening) {
            if (now >= chestEndAt) {
                finishChestOpen();
            } else if (now >= nextChestChangeAt) {
                for (int i = 0; i < chestRow.length; i++) {
                    chestRow[i] = randomJunkItem();
                }
                nextChestChangeAt = now + 220;
                GambaClient.playSound(class_3417.field_14622.comp_349(), 0.8f + RANDOM.nextFloat() * 0.35f);
            }
        }
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        method_25420(context, mouseX, mouseY, delta);

        int x = (this.field_22789 - PANEL_W) / 2;
        int y = (this.field_22790 - PANEL_H) / 2;
        int gridX = x + (PANEL_W - GRID_W) / 2;
        int gridY = y + 42;

        drawPanel(context, x, y, PANEL_W, PANEL_H);
        drawCentered(context, this.field_22785, this.field_22789 / 2, y + 6, 0xFFFFB6F4);
        context.method_27534(this.field_22793,
                class_2561.method_43470("✦ " + GambaClient.getCoins() + " Münzen ✦"), this.field_22789 / 2, y + 21, 0xFFFFE7FA);

        drawSlotGrid(context, gridX, gridY);

        if (mode == Mode.MENU) {
            drawItemAt(context, class_1802.field_8565, cellX(gridX, 3), cellY(gridY, 1));
            drawItemAt(context, class_1802.field_8466, cellX(gridX, 5), cellY(gridY, 1));
            context.method_27534(this.field_22793, class_2561.method_43470("Musikbox: Slots"), this.field_22789 / 2 - 28, y + 101, 0xFFEBC8FF);
            context.method_27534(this.field_22793, class_2561.method_43470("Endertruhe: 100 Münzen · 5% Chance"), this.field_22789 / 2, y + 113, 0xFFEBC8FF);
            context.method_27534(this.field_22793, class_2561.method_43470("ESC schließt das Menü"), this.field_22789 / 2, y + 125, 0xFFBFA8D8);
        } else if (mode == Mode.SLOTS) {
            drawSlotsScreen(context, gridX, gridY, x, y);
        } else {
            drawChestScreen(context, gridX, gridY, x, y);
        }

        super.method_25394(context, mouseX, mouseY, delta);
    }

    private void drawSlotsScreen(class_332 context, int gridX, int gridY, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int reel = 0; reel < 3; reel++) {
                drawItemAt(context, reels[row * 3 + reel], cellX(gridX, 3 + reel), cellY(gridY, row));
            }
        }

        // Icon-only wager controls. Left-click changes by 1; right-click changes by 10.
        drawItemAt(context, class_1802.field_8725, cellX(gridX, 0), cellY(gridY, 0));
        drawItemAt(context, class_1802.field_8397, cellX(gridX, 1), cellY(gridY, 0));
        context.method_27534(this.field_22793, class_2561.method_43470(Integer.toString(bet)),
                cellX(gridX, 1) + 9, cellY(gridY, 0) + 5, 0xFFFFFFFF);
        drawItemAt(context, class_1802.field_8601, cellX(gridX, 2), cellY(gridY, 0));
        drawItemAt(context, class_1802.field_8107, cellX(gridX, 0), cellY(gridY, 2));
        drawItemAt(context, class_1802.field_8639, cellX(gridX, 8), cellY(gridY, 2));

        context.method_27534(this.field_22793,
                class_2561.method_43470("Einsatz: " + bet + " Münzen"),
                this.field_22789 / 2, y + 101, 0xFFEBC8FF);
        context.method_27534(this.field_22793, class_2561.method_43470("2er +50  |  3er bis +1000"),
                this.field_22789 / 2, y + 113, 0xFFEBC8FF);
        String lower = spinning ? "Dreht sich …" : (status.isEmpty() ? "Pfeile: ±1 · Rechtsklick: ±10" : status);
        context.method_27534(this.field_22793, class_2561.method_43470(lower), this.field_22789 / 2, y + 125,
                lower.startsWith("Nicht genug") ? 0xFFFF7777 : 0xFFFFD9FA);
    }

    private void drawChestScreen(class_332 context, int gridX, int gridY, int x, int y) {
        for (int col = 0; col < 9; col++) {
            drawItemAt(context, class_1802.field_8157, cellX(gridX, col), cellY(gridY, 0));
            drawItemAt(context, class_1802.field_8157, cellX(gridX, col), cellY(gridY, 2));
        }

        if (!chestOpening && !chestRevealed) {
            drawItemAt(context, class_1802.field_8466, cellX(gridX, 4), cellY(gridY, 1));
        } else {
            for (int col = 0; col < 9; col++) {
                drawItemAt(context, chestRow[col], cellX(gridX, col), cellY(gridY, 1));
            }
        }

        context.method_27534(this.field_22793,
                class_2561.method_43470("Öffnen kostet 100 Münzen"),
                this.field_22789 / 2, y + 101, 0xFFEBC8FF);
        context.method_27534(this.field_22793, class_2561.method_43470("Totem: 5% Chance · +500 Münzen"),
                this.field_22789 / 2, y + 113, 0xFFEBC8FF);
        String lower;
        if (chestOpening) lower = "Die Endertruhe öffnet sich …";
        else if (!status.isEmpty()) lower = status;
        else if (chestRevealed && chestPendingWin) lower = "Glück gehabt! +500 Münzen";
        else if (chestRevealed) lower = "Pech gehabt – erneut versuchen?";
        else lower = "Klicke auf die Truhe · ESC zurück";
        context.method_27534(this.field_22793, class_2561.method_43470(lower), this.field_22789 / 2, y + 125,
                lower.startsWith("Nicht genug") ? 0xFFFF7777 : 0xFFFFD9FA);
    }

    private void drawPanel(class_332 context, int x, int y, int w, int h) {
        context.method_25294(x - 3, y - 3, x + w + 3, y + h + 3, 0xFF100A18);
        context.method_25294(x - 1, y - 1, x + w + 1, y + h + 1, 0xFFB36CCB);
        context.method_25294(x, y, x + w, y + h, 0xFF2A1A38);
        context.method_25294(x + 3, y + 3, x + w - 3, y + 4, 0xFFFFB7F4);
        context.method_25294(x + 3, y + h - 4, x + w - 3, y + h - 3, 0xFF79539B);
    }

    private void drawSlotGrid(class_332 context, int gridX, int gridY) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = cellX(gridX, col);
                int sy = cellY(gridY, row);
                context.method_25294(sx, sy, sx + CELL, sy + CELL, 0xFF120E19);
                context.method_25294(sx + 1, sy + 1, sx + CELL - 1, sy + CELL - 1, 0xFF725381);
                context.method_25294(sx + 2, sy + 2, sx + CELL - 2, sy + CELL - 2, 0xFF3C2A4B);
                context.method_25294(sx + 2, sy + 2, sx + CELL - 2, sy + 3, 0xFF9D7AB1);
                context.method_25294(sx + 2, sy + CELL - 3, sx + CELL - 2, sy + CELL - 2, 0xFF251A31);
            }
        }
    }

    private void drawItemAt(class_332 context, class_1792 item, int x, int y) {
        context.method_51427(new class_1799(item), x + 1, y + 1);
    }

    private void drawCentered(class_332 context, class_2561 text, int x, int y, int color) {
        context.method_27534(this.field_22793, text, x, y, color);
    }

    private static int cellX(int gridX, int col) {
        return gridX + col * CELL;
    }

    private static int cellY(int gridY, int row) {
        return gridY + row * CELL;
    }

    private static class_1792 randomSlotItem() {
        int roll = RANDOM.nextInt(100);
        if (roll < 3) return class_1802.field_22020; // Netherite Ingot: 3%
        if (roll < 12) return class_1802.field_8477; // Diamond: 9%
        if (roll < 27) return class_1802.field_8695; // Gold Ingot: 15%
        if (roll < 55) return class_1802.field_8620; // Iron Ingot: 28%
        return class_1802.field_27022;
    }

    private static class_1792 randomJunkItem() {
        return switch (RANDOM.nextInt(5)) {
            case 0 -> class_1802.field_8276;
            case 1 -> class_1802.field_8600;
            case 2 -> class_1802.field_8858;
            case 3 -> class_1802.field_8110;
            default -> class_1802.field_8831;
        };
    }

    private void beginSpin() {
        if (spinning) return;
        if (bet <= 0) {
            status = "Der Einsatz muss mindestens 1 Münze betragen!";
            GambaClient.playSound(class_3417.field_14624.comp_349(), 0.8f);
            return;
        }
        if (!GambaClient.spendCoins(bet)) {
            status = "Nicht genug Münzen!";
            GambaClient.playSound(class_3417.field_14624.comp_349(), 0.8f);
            return;
        }
        status = "";
        spinning = true;
        long now = System.currentTimeMillis();
        spinStartedAt = now;
        spinDurationMs = 2000L + RANDOM.nextInt(1001);
        spinEndAt = now + spinDurationMs;
        nextReelChangeAt = now;
        nextReelSoundAt = now;
        GambaClient.playSound(class_3417.field_14622.comp_349(), 0.8f);
    }

    private void finishSpin() {
        spinning = false;
        for (int i = 0; i < reels.length; i++) {
            reels[i] = randomSlotItem();
        }
        int winnings = 0;
        for (int row = 0; row < 3; row++) {
            class_1792 a = reels[row * 3];
            class_1792 b = reels[row * 3 + 1];
            class_1792 c = reels[row * 3 + 2];
            if (a == b && b == c) {
                winnings += threeOfAKindPayout(a);
            } else if (a == b || a == c || b == c) {
                winnings += 50;
            }
        }
        if (winnings > 0) {
            GambaClient.addCoins(winnings);
            status = "Gewonnen: +" + winnings + " Münzen!";
            GambaClient.playSound(class_3417.field_14709, 1.1f);
        } else {
            status = "Kein Gewinn – nächstes Mal!";
            GambaClient.playSound(class_3417.field_14823, 0.8f);
        }
    }

    private int threeOfAKindPayout(class_1792 item) {
        if (item == class_1802.field_22020) return 1000; // Netherite
        if (item == class_1802.field_8477) return 500;   // Diamond
        if (item == class_1802.field_8695) return 250;   // Gold
        if (item == class_1802.field_8620) return 150;   // Iron
        return 100;                                      // Copper
    }

    private void beginChestOpen() {
        if (chestOpening) return;
        if (!GambaClient.spendCoins(100)) {
            status = "Nicht genug Münzen!";
            GambaClient.playSound(class_3417.field_14624.comp_349(), 0.8f);
            return;
        }
        status = "";
        chestPendingWin = RANDOM.nextInt(100) < 5;
        chestOpening = true;
        chestRevealed = false;
        long now = System.currentTimeMillis();
        chestEndAt = now + 1800;
        nextChestChangeAt = now;
        for (int i = 0; i < chestRow.length; i++) {
            chestRow[i] = randomJunkItem();
        }
        GambaClient.playSound(class_3417.field_14952, 0.9f);
    }

    private void finishChestOpen() {
        chestOpening = false;
        chestRevealed = true;
        for (int i = 0; i < chestRow.length; i++) {
            chestRow[i] = randomJunkItem();
        }
        if (chestPendingWin) {
            chestRow[4] = class_1802.field_8288;
            GambaClient.addCoins(500);
            status = "";
            GambaClient.playSound(class_3417.field_14709, 1.0f);
        } else {
            chestRow[4] = randomJunkItem();
            status = "";
            GambaClient.playSound(class_3417.field_15206, 0.9f);
        }
    }

    @Override
    public boolean method_25402(class_11909 click, boolean doubled) {
        double mouseX = click.comp_4798();
        double mouseY = click.comp_4799();
        int button = click.method_74245();
        int x = (this.field_22789 - PANEL_W) / 2;
        int y = (this.field_22790 - PANEL_H) / 2;
        int gridX = x + (PANEL_W - GRID_W) / 2;
        int gridY = y + 42;
        int col = -1;
        int row = -1;

        if (mouseX >= gridX && mouseX < gridX + GRID_W && mouseY >= gridY && mouseY < gridY + GRID_H) {
            col = (int) ((mouseX - gridX) / CELL);
            row = (int) ((mouseY - gridY) / CELL);
        }
        if (row < 0 || col < 0) return super.method_25402(click, doubled);

        if (mode == Mode.MENU) {
            if (row == 1 && col == 3) {
                GambaClient.playSound(class_3417.field_15015.comp_349(), 1.0f);
                this.field_22787.method_1507(new GambaScreen(Mode.SLOTS));
                return true;
            }
            if (row == 1 && col == 5) {
                GambaClient.playSound(class_3417.field_14952, 0.9f);
                this.field_22787.method_1507(new GambaScreen(Mode.CHEST));
                return true;
            }
        } else if (mode == Mode.SLOTS && !spinning) {
            int delta = button == 1 ? 10 : 1;
            if (row == 0 && col == 0) {
                bet = Math.max(1, bet - delta);
                status = "";
                GambaClient.playSound(class_3417.field_15015.comp_349(), 1.0f);
                return true;
            }
            if (row == 0 && col == 2) {
                bet = Math.min(100, bet + delta);
                status = "";
                GambaClient.playSound(class_3417.field_15015.comp_349(), 1.15f);
                return true;
            }
            if (row == 2 && col == 0) {
                this.field_22787.method_1507(new GambaScreen(Mode.MENU));
                GambaClient.playSound(class_3417.field_15015.comp_349(), 1.0f);
                return true;
            }
            if (row == 2 && col == 8) {
                beginSpin();
                return true;
            }
        } else if (mode == Mode.CHEST && !chestOpening) {
            if (!chestRevealed && row == 1 && col == 4) {
                beginChestOpen();
                return true;
            }
            if (chestRevealed && row == 1) {
                beginChestOpen();
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean method_25404(class_11908 input) {
        if (input.comp_4795() == GLFW.GLFW_KEY_ESCAPE) {
            if (mode == Mode.MENU) {
                class_310.method_1551().method_1507(null);
            } else {
                class_310.method_1551().method_1507(new GambaScreen(Mode.MENU));
            }
            GambaClient.playSound(class_3417.field_15015.comp_349(), 0.9f);
            return true;
        }
        return super.method_25404(input);
    }
}
