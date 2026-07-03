package com.readwide.manager;

import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.readwide.manager.util.ReaderKeyMap;

final class DocumentPageTurnController {
    private final DocumentPageActivity activity;

    DocumentPageTurnController(@NonNull DocumentPageActivity activity) {
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

    void pageBy(int direction) {
        int pageCount = activity.documentPageCount();
        if (pageCount <= 0) return;
        if (activity.isMarkdownDocument()) {
            activity.pageMarkdownBy(direction);
            return;
        }
        int target = Math.max(0, Math.min(pageCount - 1, activity.currentPage + direction));
        if (target != activity.currentPage) {
            activity.showPage(target, Integer.compare(target, activity.currentPage));
        }
    }
}
