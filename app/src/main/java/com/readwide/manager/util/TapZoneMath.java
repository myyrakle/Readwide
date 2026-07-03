package com.readwide.manager.util;

public final class TapZoneMath {
    public static final int ACTION_MENU = 0;
    public static final int ACTION_PREVIOUS = -1;
    public static final int ACTION_NEXT = 1;

    private TapZoneMath() {}

    public static int actionForTap(float x,
                                   float y,
                                   int width,
                                   int height,
                                   boolean hasContent,
                                   boolean tapPagingEnabled,
                                   int tapZoneMode,
                                   int leadingPercent,
                                   int trailingPercent) {
        if (!hasContent || !tapPagingEnabled) return ACTION_MENU;

        float leading = clampF(leadingPercent, 5f, 80f);
        float leadingRatio = leading / 100f;

        if (tapZoneMode == PrefsManager.TAP_ZONE_HORIZONTAL) {
            if (width <= 0) return ACTION_MENU;
            float leftBoundary = width * leadingRatio;
            if (x < leftBoundary) return ACTION_PREVIOUS;
            return ACTION_NEXT;
        }

        if (height <= 0) return ACTION_MENU;
        float topBoundary = height * leadingRatio;
        if (y < topBoundary) return ACTION_PREVIOUS;
        return ACTION_NEXT;
    }

    private static float clampF(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
