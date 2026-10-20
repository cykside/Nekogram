package org.telegram.ui.ActionBar;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;

/**
 * Pure black & white theme layer.
 */
public final class MonoTheme {

    /** Matikan dengan false untuk kembali ke tema asli. */
    public static final boolean ENABLED = true;

    /** Luminance <= ini jadi #000000, >= LIGHT_CUT jadi #FFFFFF. */
    private static final float DARK_CUT = 0.18f;
    private static final float LIGHT_CUT = 0.82f;

    /**
     * Nilai abu-abu untuk warna berwarna (aksen biru, link, badge).
     * 255 = putih murni (teks putih di atas tombol aksen jadi tidak terbaca),
     * 0x9A = kompromi yang tetap terbaca. Ubah sesuai selera.
     */
    private static final int ACCENT_GRAY = 0x9A;

    private static final float[] HSV = new float[3];

    private MonoTheme() {}

    /** Konversi satu warna ke hitam/putih/abu-abu. Alpha dipertahankan. */
    public static int apply(int color) {
        if (!ENABLED) return color;
        int a = Color.alpha(color);
        if (a == 0) return color;

        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        float lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;

        int v;
        synchronized (HSV) {
            Color.RGBToHSV(r, g, b, HSV);
            boolean colorful = HSV[1] > 0.35f && HSV[2] > 0.30f;
            if (colorful) {
                v = ACCENT_GRAY;
            } else if (lum <= DARK_CUT) {
                v = 0;
            } else if (lum >= LIGHT_CUT) {
                v = 255;
            } else {
                v = Math.round(lum * 255f);
            }
        }
        return Color.argb(a, v, v, v);
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
