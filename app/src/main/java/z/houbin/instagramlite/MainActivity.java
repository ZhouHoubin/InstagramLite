package z.houbin.instagramlite;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private WebView web;
    private long backTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        web = findViewById(R.id.web);
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setAllowFileAccess(true);

        web.loadUrl("http://www.instagram.com");
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) {
            web.goBack();
        } else {
            if (backTime == 0 || System.currentTimeMillis() - backTime > 2000) {
                Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
            } else if (System.currentTimeMillis() - backTime < 2000) {
                finish();
            }
        }
        backTime = System.currentTimeMillis();
    }

    private class WebViewClient extends android.webkit.WebViewClient {

    }

    private void hideMarket(WebView view) {
        view.evaluateJavascript("document.querySelector(\"#react-root > section > div\").setAttribute(\"style\",\"display:none\");", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE && resultCode == Activity.RESULT_OK && filePathCallback != null) {
            String dataString = data.getDataString();
            ClipData clipData = data.getClipData();
            Uri[] results = null;
            if (clipData != null) {
                results = new Uri[clipData.getItemCount()];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    results[i] = item.getUri();
                }
            }
            if (dataString != null) {
                results = new Uri[]{Uri.parse(dataString)};
            }

            filePathCallback.onReceiveValue(results);
        }
    }

    private ValueCallback<Uri[]> filePathCallback;

    private int FILE_CHOOSER_RESULT_CODE = 1;

    private class WebChromeClient extends android.webkit.WebChromeClient {

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            MainActivity.this.filePathCallback = filePathCallback;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/jpeg");
            MainActivity.this.startActivityForResult(Intent.createChooser(i, "选择文件"), FILE_CHOOSER_RESULT_CODE);
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress >= 70) {
                if (view.getUrl().equalsIgnoreCase("https://www.instagram.com/")) {
                    hideMarket(view);
                }
            }
            System.out.println("view = [" + view + "], newProgress = [" + newProgress + "]");
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            System.out.println(consoleMessage.message());
            return super.onConsoleMessage(consoleMessage);
        }
    }
}
