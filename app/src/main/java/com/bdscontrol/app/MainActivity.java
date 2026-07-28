package com.bdscontrol.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.net.Uri;
import android.content.Intent;

public final class MainActivity extends Activity {
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private ServerManager serverManager;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(1024,1024);
        serverManager = new ServerManager(this);
        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true);
        s.setAllowContentAccess(true); s.setDatabaseEnabled(true); s.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient(){
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p){
                fileCallback=cb; startActivityForResult(p.createIntent(), 41); return true;
            }
        });
        webView.addJavascriptInterface(new AndroidBridge(this, serverManager), "AndroidBridge");
        setContentView(webView); webView.loadUrl("file:///android_asset/index.html");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    public void emit(String event, String json){ runOnUiThread(()->webView.evaluateJavascript("window.BDS&&window.BDS.onNative("+js(event)+","+json+")", null)); }
    private static String js(String s){ return "\""+s.replace("\\","\\\\").replace("\"","\\\"")+"\""; }
    @Override protected void onActivityResult(int r,int c,Intent d){ super.onActivityResult(r,c,d); if(r==41&&fileCallback!=null){ fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(c,d)); fileCallback=null; } }
    @Override protected void onDestroy(){ if(serverManager!=null) serverManager.stopAll(); if(webView!=null) webView.destroy(); super.onDestroy(); }
}
