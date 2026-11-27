package de.chriis86.gta6guide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/** Zeigt die interaktive MapGenie-Karte direkt in der App. Login und Fortschritt bleiben gespeichert. */
public class KartenActivity extends Activity {

    static final String MAPGENIE = "https://mapgenie.io/grand-theft-auto-6/maps/leonida";

    private WebView web;
    private ProgressBar prog;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.parseColor("#160A30"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#120B1E"));

        // Obere Leiste: zurück zum Guide, Titel, neu laden
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(Color.parseColor("#160A30"));
        bar.setPadding(dp(4), 0, dp(4), 0);

        TextView back = button("‹ Guide");
        back.setOnClickListener(v -> finish());
        TextView title = new TextView(this);
        title.setText("Karte · MapGenie");
        title.setTextColor(Color.parseColor("#F4EEFF"));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        TextView reload = button("↻");
        reload.setOnClickListener(v -> web.reload());

        bar.addView(back, new LinearLayout.LayoutParams(dp(90), dp(52)));
        bar.addView(title, new LinearLayout.LayoutParams(0, dp(52), 1));
        bar.addView(reload, new LinearLayout.LayoutParams(dp(90), dp(52)));

        prog = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        prog.setMax(100);
        prog.getProgressDrawable().setTint(Color.parseColor("#FF5DA2"));

        View line = new View(this);
        line.setBackgroundColor(Color.parseColor("#FF5DA2"));

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setMediaPlaybackRequiresUserGesture(true);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                String sc = u.getScheme();
                if ("http".equals(sc) || "https".equals(sc)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onPageFinished(WebView v, String url) {
                prog.setVisibility(View.GONE);
                CookieManager.getInstance().flush();
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView v, int p) {
                prog.setVisibility(p < 100 ? View.VISIBLE : View.GONE);
                prog.setProgress(p);
            }
        });

        root.addView(bar, new LinearLayout.LayoutParams(-1, dp(52)));
        root.addView(line, new LinearLayout.LayoutParams(-1, dp(2)));
        root.addView(prog, new LinearLayout.LayoutParams(-1, dp(3)));
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        if (state != null) web.restoreState(state);
        else web.loadUrl(MAPGENIE);
    }

    private TextView button(String text) {
        TextView b = new TextView(this);
        b.setText(text);
        b.setTextColor(Color.parseColor("#FF5DA2"));
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        return b;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }
}
