package org.telegram.ui.ActionBar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * Pure black & white theme layer.
 * Letakkan di: TMessagesProj/src/main/java/org/telegram/ui/ActionBar/MonoTheme.java
 */
public final class MonoTheme {

    /** Matikan dengan false untuk kembali ke tema asli. */
    public static final boolean ENABLED = true;

    /** Luminance <= ini jadi #000000, >= LIGHT_CUT jadi #FFFFFF. */
    private static final float DARK_CUT = 0.18f;
    private static final float LIGHT_CUT = 0.82f;

    /**
     * Abu-abu untuk warna berwarna (aksen biru, link, badge).
     * 255 = putih murni (teks putih di atas tombol aksen jadi tak terbaca),
     * 0x9A = kompromi yang tetap terbaca. Ubah sesuai selera.
     */
    private static final int ACCENT_GRAY = 0x9A;

    // getColor() dipanggil sangat sering -> cache direct-mapped, tanpa lock.
    private static final int CACHE_BITS = 12;
    private static final AtomicLongArray CACHE = new AtomicLongArray(1 << CACHE_BITS);

    private MonoTheme() {}

    /**
     * Konversi satu warna ke hitam/putih/abu-abu. Alpha dipertahankan.
     * Nilai dengan alpha 0 (termasuk angka biasa seperti rotasi gradient/flag
     * animasi yang disimpan di map warna) dikembalikan apa adanya.
     */
    public static int apply(int color) {
        if (!ENABLED) return color;
        if ((color >>> 24) == 0) return color;

        int idx = (color * 0x9E3779B1) >>> (32 - CACHE_BITS);
        long e = CACHE.get(idx);
        if ((int) (e >>> 32) == color) return (int) e;

        int res = compute(color);
        CACHE.set(idx, ((long) color << 32) | (res & 0xFFFFFFFFL));
        return res;
    }

    private static int compute(int color) {
        int a = color >>> 24;
        int r = (color >> 16) & 0xFF, g = (color >> 8) & 0xFF, b = color & 0xFF;
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        float sat = max == 0 ? 0f : (max - min) / (float) max;
        float val = max / 255f;
        float lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;

        int v;
        if (sat > 0.35f && val > 0.30f) {
            v = ACCENT_GRAY;
        } else if (lum <= DARK_CUT) {
            v = 0;
        } else if (lum >= LIGHT_CUT) {
            v = 255;
        } else {
            v = Math.round(lum * 255f);
        }
        return (a << 24) | (v << 16) | (v << 8) | v;
    }

    /** Status bar & navigation bar hitam dengan ikon terang. Panggil di Activity.onCreate/onResume. */
    @SuppressWarnings("deprecation")
    public static void applySystemBars(Activity activity) {
        if (!ENABLED || activity == null) return;
        Window w = activity.getWindow();
        w.setStatusBarColor(Color.BLACK);
        w.setNavigationBarColor(Color.BLACK);
        View decor = w.getDecorView();
        int flags = decor.getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= 26) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }
}
