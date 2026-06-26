package cinema.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Application theme — supports Dark (default) and Light modes.
 * Call applyDark() / applyLight() then notifyListeners() to switch.
 */
public class UITheme {

    // ── Theme state ───────────────────────────────────────────────
    public static boolean IS_DARK = true;

    // ── Dynamic color fields (mutated on theme switch) ────────────
    public static Color BG_PRIMARY;
    public static Color BG_SECONDARY;
    public static Color BG_CARD;
    public static Color BG_HOVER;
    public static Color BG_INPUT;

    public static Color TEXT_PRIMARY;
    public static Color TEXT_SECONDARY;
    public static Color TEXT_MUTED;

    // Accents stay the same across themes
    public static final Color ACCENT_GOLD   = new Color(255, 190,  50);
    public static final Color ACCENT_PURPLE = new Color(138,  43, 226);
    public static final Color ACCENT_PINK   = new Color(255,  64, 129);
    public static final Color ACCENT_CYAN   = new Color(  0, 188, 212);
    public static final Color ACCENT_GREEN  = new Color( 46, 213, 115);

    // Seat colours (fixed)
    public static final Color SEAT_AVAILABLE = new Color( 46, 213, 115);
    public static final Color SEAT_SELECTED  = new Color(255, 190,  50);
    public static final Color SEAT_BOOKED    = new Color(220,  53,  69);
    public static final Color SEAT_VIP       = new Color(138,  43, 226);
    public static final Color SEAT_COUPLE    = new Color(255,  64, 129);

    // ── Fonts (unchanged) ─────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  32);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 13);

    public static final int ARC = 14;

    // ── Theme change listeners ─────────────────────────────────────
    private static final List<Runnable> LISTENERS = new ArrayList<>();

    public static void addThemeListener(Runnable r) { LISTENERS.add(r); }

    public static void notifyListeners() {
        for (Runnable r : LISTENERS) r.run();
    }

    // ── Apply themes ──────────────────────────────────────────────
    public static void applyDark() {
        IS_DARK      = true;
        BG_PRIMARY   = new Color(  8,   8,  22);
        BG_SECONDARY = new Color( 16,  16,  38);
        BG_CARD      = new Color( 24,  24,  52);
        BG_HOVER     = new Color( 34,  34,  68);
        BG_INPUT     = new Color( 20,  20,  46);
        TEXT_PRIMARY   = new Color(240, 240, 255);
        TEXT_SECONDARY = new Color(160, 160, 200);
        TEXT_MUTED     = new Color(100, 100, 140);
    }

    public static void applyLight() {
        IS_DARK      = false;
        BG_PRIMARY   = new Color(245, 246, 255);
        BG_SECONDARY = new Color(232, 234, 250);
        BG_CARD      = new Color(255, 255, 255);
        BG_HOVER     = new Color(220, 224, 245);
        BG_INPUT     = new Color(238, 240, 252);
        TEXT_PRIMARY   = new Color( 18,  18,  40);
        TEXT_SECONDARY = new Color( 55,  55,  90);
        TEXT_MUTED     = new Color(110, 110, 150);
    }

    // Initialise with dark
    static { applyDark(); }

    // ── Helpers ───────────────────────────────────────────────────
    public static Color withAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /** Gold accent colour — darker in light mode for readability on white */
    public static Color accentGold() {
        return IS_DARK ? ACCENT_GOLD : new Color(180, 110, 0);
    }

    /** Nav bar background */
    public static Color navBg() {
        return IS_DARK ? withAlpha(BG_SECONDARY, 240) : withAlpha(new Color(255,255,255), 250);
    }

    /** Card background for movie cards / detail */
    public static Color cardBg() {
        return IS_DARK ? BG_CARD : new Color(255, 255, 255);
    }

    public static GradientPaint goldGradient(float x1, float y1, float x2, float y2) {
        if (IS_DARK)
            return new GradientPaint(x1, y1, new Color(255, 200, 50),
                                     x2, y2, new Color(255, 100, 30));
        else
            return new GradientPaint(x1, y1, new Color(200, 120, 0),
                                     x2, y2, new Color(160,  70, 0));
    }

    public static GradientPaint purpleGradient(float x1, float y1, float x2, float y2) {
        return new GradientPaint(x1, y1, new Color(138, 43, 226),
                                 x2, y2, new Color(75, 0, 130));
    }

    public static GradientPaint bgGradient(int w, int h) {
        if (IS_DARK)
            return new GradientPaint(0, 0, BG_PRIMARY, w, h, new Color(12, 8, 35));
        else
            return new GradientPaint(0, 0, new Color(235, 238, 255),
                                     w, h, new Color(218, 224, 252));
    }
}
