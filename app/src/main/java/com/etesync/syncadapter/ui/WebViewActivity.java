package com.etesync.syncadapter.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.etesync.syncadapter.Constants;
import com.etesync.syncadapter.R;

public class WebViewActivity extends AppCompatActivity {

    private static final String KEY_URL = "url";
    private static final String QUERY_KEY_EMBEDDED = "embedded";

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;

    public static void openUrl(Context context, Uri uri) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.KEY_URL, uri);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Uri uri = getIntent().getParcelableExtra(KEY_URL);
        uri = addQueryParams(uri);
        mWebView = (WebView) findViewById(R.id.webView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(uri.toString());
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                setTitle(view.getTitle());
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return shouldOverrideUrl(Uri.parse(url));
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return shouldOverrideUrl(request.getUrl());
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                loadErrorPage(failingUrl);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                loadErrorPage(request.getUrl().toString());
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                mToolbar = (Toolbar) findViewById(R.id.toolbar);
                if (progress == 100) {
                    mToolbar.setTitle(view.getTitle());
                    mProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    mToolbar.setTitle(R.string.loading);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(progress);
                }
            }
        });
    }

    private Uri addQueryParams(Uri uri) {
        return uri.buildUpon().appendQueryParameter(QUERY_KEY_EMBEDDED, "1").build();
    }

    private void loadErrorPage(String failingUrl) {
        String htmlData = "<html><title>" +
                getString(R.string.loading_error_title) +
                "</title>" +
                "<style>" +
                ".btn {" +
                "    display: inline-block;" +
                "    padding: 6px 12px;" +
                "    font-size: 20px;" +
                "    font-weight: 400;" +
                "    line-height: 1.42857143;" +
                "    text-align: center;" +
                "    white-space: nowrap;" +
                "    vertical-align: middle;" +
                "    touch-action: manipulation;" +
                "    cursor: pointer;" +
                "    user-select: none;" +
                "    border: 1px solid #ccc;" +
                "    border-radius: 4px;" +
                "    color: #333;" +
                "    text-decoration: none;" +
                "    margin-top: 50px;" +
                "}" +
                "</style>" +
                "<body>" +
                "<div align=\"center\">" +
                "<a class=\"btn\" href=\"" + failingUrl + "\">" + getString(R.string.loading_error_content) +
                "</a>" +
                "</form></body></html>";

        mWebView.loadDataWithBaseURL("about:blank", htmlData, "text/html", "UTF-8", null);
        mWebView.invalidate();
    }

    private boolean shouldOverrideUrl(Uri uri) {
        if (Constants.webUri.getHost().equals(uri.getHost())) {
            if (uri.getQueryParameter(QUERY_KEY_EMBEDDED) != null) {
                return false;
            } else {
                uri = addQueryParams(uri);
                mWebView.loadUrl(uri.toString());
                return true;
            }

        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mWebView.saveState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}