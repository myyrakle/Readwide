package com.readwide.manager;

import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.readwide.manager.util.ReaderKeyMap;

final class PdfPageTurnController {
    private final PdfReaderActivity activity;

    PdfPageTurnController(@NonNull PdfReaderActivity activity) {
        this.activity = activity;
    }

    boolean handlePageTurnKey(KeyEvent event) {
        if (event == null || activity.prefs == null || !activity.prefs.getVolumeKeyScroll()) {
            return false;
        }

        int direction = ReaderKeyMap.pageTurnDirectionForKey(event.getKeyCode());
        if (direction == 0) return false;

        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            if (ReaderKeyMap.shouldTurnPageOnKeyDown(event.getRepeatCount())) {
                pageBy(direction);
            }
            return true;
        }
        return action == KeyEvent.ACTION_UP;
    }

    private void pageBy(int direction) {
        if (activity.pageCount <= 0) return;
        int target = Math.max(0, Math.min(activity.pageCount - 1, activity.currentPage + direction));
        if (target != activity.currentPage) {
            activity.goToPage(target, Integer.compare(target, activity.currentPage));
        }
    }
}
