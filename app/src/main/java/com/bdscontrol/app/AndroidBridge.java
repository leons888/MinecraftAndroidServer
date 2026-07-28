package com.bdscontrol.app;

import android.webkit.JavascriptInterface;

public final class AndroidBridge {
    private final MainActivity activity; private final ServerManager manager;
    public AndroidBridge(MainActivity a, ServerManager m){activity=a;manager=m;}
    @JavascriptInterface public String capabilities(){ return "{\"officialBds\":true,\"nativeArm64Bds\":false,\"embeddedRuntime\":true,\"termux\":false,\"playit\":\"not bundled\"}"; }
    @JavascriptInterface public void configure(String json){ manager.configure(json); }
    @JavascriptInterface public void install(String version,String expectedSha256){ manager.install(version,expectedSha256); }
    @JavascriptInterface public void start(){ manager.start(); }
    @JavascriptInterface public void stop(){ manager.stop(); }
    @JavascriptInterface public void restart(){ manager.restart(); }
    @JavascriptInterface public void sendCommand(String command){ manager.sendCommand(command); }
    @JavascriptInterface public String status(){ return manager.statusJson(); }
    @JavascriptInterface public String system(){ return SystemMonitor.snapshot(); }
}
