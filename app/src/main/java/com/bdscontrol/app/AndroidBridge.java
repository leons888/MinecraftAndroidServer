package com.bdscontrol.app;

import android.net.Uri;import android.webkit.JavascriptInterface;

public final class AndroidBridge {
 private final MainActivity activity; private final ServerManager manager;
 public AndroidBridge(MainActivity a,ServerManager m){activity=a;manager=m;}
 @JavascriptInterface public String capabilities(){return "{\"officialBds\":true,\"embeddedRuntime\":true,\"verifiedArtifactsOnly\":true,\"termux\":false,\"root\":false,\"proot\":false,\"runtime\":\"box64-direct\"}";}
 @JavascriptInterface public void installRuntime(){manager.installRuntime();}@JavascriptInterface public void configure(String json){manager.configure(json);}@JavascriptInterface public void listBdsVersions(){manager.listBdsVersions();}
 @JavascriptInterface public void install(String version,String url,String sha256){manager.install(version,url,sha256);}@JavascriptInterface public void pickLocalZip(){activity.pickLocalZip();}@JavascriptInterface public void copyLogs(String text){activity.copyToClipboard(text);}
 @JavascriptInterface public void start(){manager.start();}@JavascriptInterface public void stop(){manager.stop();}@JavascriptInterface public void restart(){manager.restart();}@JavascriptInterface public void sendCommand(String command){manager.sendCommand(command);}@JavascriptInterface public String status(){return manager.statusJson();}@JavascriptInterface public String system(){return manager.systemJson();}
 @JavascriptInterface public void playitStart(String secret){manager.playitStart(secret);}@JavascriptInterface public void playitCommand(String line){manager.playitCommand(line);}@JavascriptInterface public void playitStop(){manager.playitStop();}
 public void onLocalZip(Uri uri){manager.installLocal(uri);}
}
