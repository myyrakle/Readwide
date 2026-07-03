package com.readwide.manager;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.readwide.manager.adapter.DrawerEntryAdapter;
import com.readwide.manager.model.DrawerEntry;
import com.readwide.manager.model.ReaderState;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class MainDrawerController {
    private final MainActivity activity;

    MainDrawerController(@NonNull MainActivity activity) {
        this.activity = activity;
    }

    void setupDrawerStorageList() {
        activity.drawerFixedList = activity.findViewById(R.id.drawer_fixed_list);
        activity.drawerStorageList = activity.findViewById(R.id.drawer_storage_list);
        setupDrawerSystemInsets();

        activity.drawerFixedEntryAdapter = new DrawerEntryAdapter();
        activity.drawerEntryAdapter = new DrawerEntryAdapter();
        activity.drawerFixedEntryAdapter.setUseShortcutBoxColor(false);
        activity.drawerEntryAdapter.setUseShortcutBoxColor(false);

        if (activity.drawerFixedList != null) {
            activity.drawerFixedList.setLayoutManager(new LinearLayoutManager(activity));
            activity.drawerFixedList.setAdapter(activity.drawerFixedEntryAdapter);
            activity.drawerFixedList.setNestedScrollingEnabled(false);
        }
        if (activity.drawerStorageList != null) {
            activity.drawerStorageList.setLayoutManager(new LinearLayoutManager(activity));
            activity.drawerStorageList.setAdapter(activity.drawerEntryAdapter);
        }
        DrawerEntryAdapter.OnEntryClickListener clickListener = activity::queueDrawerNavigation;
        DrawerEntryAdapter.OnEntryLongClickListener longClickListener = activity::handleDrawerEntryLongClick;
        activity.drawerFixedEntryAdapter.setListener(clickListener);
        activity.drawerEntryAdapter.setListener(clickListener);
        activity.drawerFixedEntryAdapter.setLongClickListener(longClickListener);
        activity.drawerEntryAdapter.setLongClickListener(longClickListener);

        rebuildDrawerStorageEntries();
    }

    private void setupDrawerSystemInsets() {
        View navDrawer = activity.findViewById(R.id.nav_drawer);
        if (navDrawer == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            navDrawer.setOnApplyWindowInsetsListener((v, insets) -> {
                int topInset = insets != null ? insets.getSystemWindowInsetTop() : 0;
                int bottomInset = insets != null ? insets.getSystemWindowInsetBottom() : 0;
                boolean changed = activity.drawerTopInsetPx != topInset || activity.drawerBottomInsetPx != bottomInset;
                activity.drawerTopInsetPx = topInset;
                activity.drawerBottomInsetPx = bottomInset;
                if (v.getPaddingTop() != topInset || v.getPaddingBottom() != bottomInset) {
                    v.setPadding(v.getPaddingLeft(), topInset, v.getPaddingRight(), bottomInset);
                }
                if (changed) {
                    Toolbar toolbar = activity.findViewById(R.id.toolbar);
                    if (toolbar != null) {
                        activity.applyMainReadableTheme(toolbar);
                    }
                }
                return insets;
            });
            navDrawer.requestApplyInsets();
        }
    }

    void rebuildDrawerStorageEntries() {
        List<DrawerEntry> fixedEntries = new ArrayList<>();

        fixedEntries.add(new DrawerEntry(
                DrawerEntry.ACTION_RECENT,
                R.drawable.ic_recent,
                activity.getString(R.string.recent),
                null,
                null));

        File internal = Environment.getExternalStorageDirectory();
        if (internal != null) {
            fixedEntries.add(new DrawerEntry(
                    DrawerEntry.ACTION_INTERNAL,
                    R.drawable.ic_storage_internal,
                    activity.getString(R.string.internal_storage),
                    internal.getAbsolutePath(),
                    internal.getAbsolutePath()));
        }

        for (File sd : detectExternalSdCards()) {
            fixedEntries.add(new DrawerEntry(
                    DrawerEntry.ACTION_EXTERNAL_SD,
                    R.drawable.ic_storage_sdcard,
                    activity.getString(R.string.external_storage),
                    sd.getAbsolutePath(),
                    sd.getAbsolutePath()));
        }

        File downloads = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        if (downloads != null) {
            fixedEntries.add(new DrawerEntry(
                    DrawerEntry.ACTION_DOWNLOADS,
                    R.drawable.ic_download,
                    activity.getString(R.string.downloads),
                    downloads.getAbsolutePath(),
                    downloads.getAbsolutePath()));
        }

        addShortcutFolderEntries(fixedEntries);
        addRecentFolderEntries(fixedEntries);

        if (activity.drawerEntryAdapter != null) activity.drawerEntryAdapter.setEntries(fixedEntries);
        if (activity.drawerFixedEntryAdapter != null) activity.drawerFixedEntryAdapter.setEntries(new ArrayList<>());

        if (activity.drawerStorageList != null) {
            applySingleDrawerListHeight(activity.drawerStorageList);
        }
        if (activity.drawerFixedList != null) {
            applyFixedRowListHeight(activity.drawerFixedList, 0, 0);
        }
    }

    private void applyFixedRowListHeight(@NonNull RecyclerView list, int itemCount, int maxRows) {
        android.view.ViewGroup.LayoutParams lp = list.getLayoutParams();
        if (lp == null) return;

        int rows = Math.max(0, Math.min(itemCount, maxRows));
        lp.height = rows <= 0 ? 0 : activity.dpToPx(rows * 48);
        list.setLayoutParams(lp);
        list.setVisibility(rows <= 0 ? View.GONE : View.VISIBLE);
        list.setNestedScrollingEnabled(itemCount > maxRows);
        list.setOverScrollMode(itemCount > maxRows
                ? View.OVER_SCROLL_IF_CONTENT_SCROLLS
                : View.OVER_SCROLL_NEVER);
        list.setVerticalScrollBarEnabled(itemCount > maxRows);
    }

    private void applySingleDrawerListHeight(@NonNull RecyclerView list) {
        android.view.ViewGroup.LayoutParams rawLp = list.getLayoutParams();
        if (rawLp == null) return;

        if (rawLp instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rawLp;
            lp.height = 0;
            lp.weight = 1f;
            list.setLayoutParams(lp);
        } else {
            rawLp.height = 0;
            list.setLayoutParams(rawLp);
        }

        list.setVisibility(View.VISIBLE);
        list.setNestedScrollingEnabled(true);
        list.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        list.setVerticalScrollBarEnabled(true);
    }

    private void addShortcutFolderEntries(@NonNull List<DrawerEntry> entries) {
        if (activity.prefs == null) return;

        for (String path : activity.prefs.getFolderShortcuts(30)) {
            if (path == null || path.trim().isEmpty()) continue;
            File folder = new File(path.trim());
            if (!folder.exists() || !folder.isDirectory() || !folder.canRead()) continue;
            if (isBuiltInDrawerPath(folder.getAbsolutePath())) continue;

            String name = folder.getName();
            if (name.isEmpty()) name = folder.getAbsolutePath();
            entries.add(new DrawerEntry(
                    DrawerEntry.ACTION_FOLDER_SHORTCUT,
                    R.drawable.ic_folder,
                    name,
                    folder.getAbsolutePath(),
                    folder.getAbsolutePath()));
            if (entries.size() >= 30) break;
        }
    }

    private void addRecentFolderEntries(@NonNull List<DrawerEntry> entries) {
        if (activity.bookmarkManager == null && activity.prefs == null) return;

        LinkedHashSet<String> recentPaths = new LinkedHashSet<>();
        String lastDirectory = activity.prefs != null ? activity.prefs.getLastDirectory() : null;
        if (lastDirectory != null && !lastDirectory.trim().isEmpty()) {
            recentPaths.add(lastDirectory);
        }

        if (activity.prefs != null) {
            recentPaths.addAll(activity.prefs.getRecentFolders(16));
        }

        if (activity.bookmarkManager != null) {
            for (ReaderState state : activity.bookmarkManager.getRecentFiles(50)) {
                File file = new File(state.getFilePath());
                File parent = file.isDirectory() ? file : file.getParentFile();
                if (parent != null) recentPaths.add(parent.getAbsolutePath());
                if (recentPaths.size() >= 12) break;
            }
        }

        List<DrawerEntry> folderEntries = new ArrayList<>();
        for (String path : recentPaths) {
            if (path == null || path.trim().isEmpty()) continue;
            File folder = new File(path);
            if (!folder.exists() || !folder.isDirectory() || !folder.canRead()) continue;
            if (isBuiltInDrawerPath(folder.getAbsolutePath())) continue;
            if (activity.prefs != null && activity.prefs.isRecentFolderHidden(folder.getAbsolutePath())) continue;
            if (activity.prefs != null && activity.prefs.isFolderShortcut(folder.getAbsolutePath())) continue;
            String name = folder.getName();
            if (name.isEmpty()) name = folder.getAbsolutePath();
            folderEntries.add(new DrawerEntry(
                    DrawerEntry.ACTION_RECENT_FOLDER,
                    R.drawable.ic_folder,
                    name,
                    folder.getAbsolutePath(),
                    folder.getAbsolutePath()));
            if (folderEntries.size() >= 10) break;
        }

        entries.addAll(folderEntries);
    }

    boolean isBuiltInDrawerPath(@NonNull String path) {
        File internal = Environment.getExternalStorageDirectory();
        if (internal != null && path.equals(internal.getAbsolutePath())) return true;

        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloads != null && path.equals(downloads.getAbsolutePath())) return true;

        if (path.equals("/storage")) return true;

        for (File sd : detectExternalSdCards()) {
            if (path.equals(sd.getAbsolutePath())) return true;
        }
        return false;
    }

    /**
     * Locate non-emulated external storage volumes (SD cards, USB OTG).
     * Cached after the first successful detection so the drawer rebuild
     * does not re-scan /storage and re-query getExternalFilesDirs() up to
     * 50 times per onResume.
     */
    List<File> detectExternalSdCards() {
        if (activity.cachedSdCards != null) return activity.cachedSdCards;

        Set<String> seen = new LinkedHashSet<>();
        List<File> result = new ArrayList<>();

        // Method 1: scan /storage for siblings of "emulated"
        File storage = new File("/storage");
        File[] storageChildren = storage.listFiles();
        if (storageChildren != null) {
            for (File f : storageChildren) {
                String name = f.getName();
                if (name.equals("emulated") || name.equals("self")
                        || name.equals("enc_emulated") || name.startsWith(".")) continue;
                if (!f.isDirectory() || !f.canRead()) continue;
                String path = f.getAbsolutePath();
                if (seen.add(path)) result.add(f);
            }
        }

        // Method 2: derive from getExternalFilesDirs (skip the first = internal)
        File[] appDirs = ContextCompat.getExternalFilesDirs(activity, null);
        if (appDirs.length > 1) {
            for (int i = 1; i < appDirs.length; i++) {
                File d = appDirs[i];
                if (d == null) continue;
                String p = d.getAbsolutePath();
                int idx = p.indexOf("/Android");
                if (idx > 0) {
                    File root = new File(p.substring(0, idx));
                    if (root.exists() && root.canRead() && seen.add(root.getAbsolutePath())) {
                        result.add(root);
                    }
                }
            }
        }

        activity.cachedSdCards = result;
        return activity.cachedSdCards;
    }

    void setupDrawerBottomActions() {
        View openFile = activity.findViewById(R.id.drawer_btn_open_file);
        View bookmarks = activity.findViewById(R.id.drawer_btn_bookmarks);

        if (openFile != null) {
            openFile.setOnClickListener(v -> {
                runActionThenCloseDrawerInBackground(() ->
                        activity.openFileLauncher.launch(getSupportedOpenMimeTypes()));
            });
        }
        if (bookmarks != null) {
            bookmarks.setOnClickListener(v -> {
                runActionThenCloseDrawerInBackground(() ->
                        activity.startActivity(new Intent(activity, BookmarkListActivity.class)));
            });
        }
    }

    private void runActionThenCloseDrawerInBackground(@NonNull Runnable action) {
        action.run();
        if (activity.drawerLayout == null
                || !activity.drawerLayout.isDrawerVisible(GravityCompat.START)) {
            return;
        }
        activity.drawerLayout.post(activity::closeDrawerAfterSelection);
    }

    private String[] getSupportedOpenMimeTypes() {
        return new String[]{
                "text/plain",
                "text/*",
                "application/pdf",
                "application/epub+zip",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-word.document.macroEnabled.12",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                "application/vnd.ms-word.template.macroEnabled.12",
                "image/*",
                "application/octet-stream"
        };
    }
}
