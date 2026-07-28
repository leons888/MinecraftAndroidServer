package com.bdscontrol.app;

import android.content.Context;import org.json.JSONArray;import org.json.JSONObject;import java.io.*;import java.net.*;import java.util.*;

final class PlayitManager {
    private final Context context; private final File dir; private final LogManager log; private Process process;
    PlayitManager(Context c,LogManager l){context=c;dir=new File(c.getFilesDir(),"bds-runtime/playit");log=l;dir.mkdirs();}
    void installAndStart(String rootfs, String localPort){new Thread(()->{try{String url=asset();if(url==null)throw new IOException("official Playit release has no ARM64 Linux asset");File f=new File(dir,"playit-agent");download(url,f);f.setExecutable(true,false);ProcessBuilder b=new ProcessBuilder(rootfs+"/bin/box64","/server/playit-agent");b.directory(dir);b.redirectErrorStream(true);b.environment().put("LOCAL_PORT",localPort);process=b.start();read(process.getInputStream());log.event("ready","Playit agent started from official release; claim/setup may be required");}catch(Exception e){log.event("error","Playit agent not started: "+e.getMessage());}},"playit-start").start();}
    private String asset()throws Exception{HttpURLConnection c=(HttpURLConnection)new URL("https://api.github.com/repos/playit-cloud/playit-agent/releases/latest").openConnection();c.setRequestProperty("Accept","application/vnd.github+json");c.setRequestProperty("User-Agent","BDS-Control/0.1");try(InputStream in=c.getInputStream()){String s=new String(in.readAllBytes());JSONObject o=new JSONObject(s);JSONArray a=o.optJSONArray("assets");for(int i=0;i<a.length();i++){String n=a.getJSONObject(i).optString("name").toLowerCase(Locale.US);if((n.contains("aarch64")||n.contains("arm64"))&&!n.endsWith(".sha256"))return a.getJSONObject(i).optString("browser_download_url");}}return null;}
    private void download(String u,File f)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(u).openConnection();c.setRequestProperty("User-Agent","BDS-Control/0.1");if(c.getResponseCode()/100!=2)throw new IOException("HTTP "+c.getResponseCode());try(InputStream in=c.getInputStream();OutputStream out=new FileOutputStream(f)){in.transferTo(out);}}
    private void read(InputStream in){new Thread(()->{try(BufferedReader r=new BufferedReader(new InputStreamReader(in))){String s;while((s=r.readLine())!=null)log.line("playit",s);}catch(IOException ignored){}}).start();}
    void stop(){if(process!=null)process.destroy();}
}
