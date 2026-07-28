package com.bdscontrol.app;

import android.webkit.JavascriptInterface;

public final class AndroidBridge {
 private final ServerManager manager;public AndroidBridge(MainActivity a,ServerManager m){manager=m;}
 @JavascriptInterface public String capabilities(){return "{\"officialBds\":true,\"embeddedRuntime\":true,\"verifiedArtifactsOnly\":true,\"termux\":false}";}
 @JavascriptInterface public void installRuntime(){manager.installRuntime();}
 @JavascriptInterface public void configure(String json){manager.configure(json);}
 @JavascriptInterface public void install(String version,String sha256){manager.install(version,sha256);}
 @JavascriptInterface public void start(){manager.start();}@JavascriptInterface public void stop(){manager.stop();}@JavascriptInterface public void restart(){manager.restart();}@JavascriptInterface public void sendCommand(String command){manager.sendCommand(command);}@JavascriptInterface public String status(){return manager.statusJson();}@JavascriptInterface public String system(){return manager.systemJson();}
}
