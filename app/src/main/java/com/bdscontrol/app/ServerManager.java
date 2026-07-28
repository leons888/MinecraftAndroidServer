package com.bdscontrol.app;

import android.content.Context;import android.content.Intent;import android.os.Build;import org.json.JSONObject;import java.io.*;import java.util.*;

public final class ServerManager {
 private final MainActivity a;private final Context context;private final File root;private final ProcessManager pm=new ProcessManager();private final LogManager logs;private final RuntimeManager runtime;private final AppConfig config;private final MonitorManager monitor;private final PlayitManager playit;private volatile String state="stopped",version="";private long started;private volatile boolean shuttingDown;private int restarts;
 public ServerManager(Context c){context=c;a=(MainActivity)c;root=new File(c.getFilesDir(),"bds-runtime");root.mkdirs();logs=new LogManager(a);runtime=new RuntimeManager(c);config=new AppConfig(c);monitor=new MonitorManager(c);playit=new PlayitManager(c,runtime,logs);version=config.get("version","");installRuntime();}
 void configure(String json){config.put("config",json==null?"{}":json);logs.event("info","Configuration saved locally");}
 void installRuntime(){new Thread(()->{try{String manifest=read(new java.net.URL(RuntimeManifest.URL));new RuntimeInstaller(context,new RuntimeInstaller.Events(){public void message(String t,String x){logs.event(t,x);}public void progress(String id,long d,long total){try{JSONObject o=new JSONObject();o.put("id",id);o.put("done",d);o.put("total",total);a.emit("runtimeProgress",o.toString());}catch(Exception ignored){}}}).install(RuntimeManifest.parse(manifest));}catch(Exception e){logs.event("error","Runtime manifest rejected: "+e.getMessage());}},"runtime-manifest").start();}
 private String read(java.net.URL u)throws Exception{try(InputStream in=u.openStream();ByteArrayOutputStream o=new ByteArrayOutputStream()){byte[]b=new byte[8192];int n;while((n=in.read(b))>0)o.write(b,0,n);return o.toString("UTF-8");}}
 void install(String v,String expected){version=v;config.put("version",v);new Thread(()->{try{state="installing";emitState();File zip=new File(root,"bds.zip");String url="https://www.minecraft.net/bedrockdedicatedserver/bin-linux/bedrock-server-"+v+".zip";DownloadManager.download(url,zip,(d,t)->progress(d,t));String hash=DownloadManager.sha256(zip);if(expected==null||expected.isEmpty()||!hash.equalsIgnoreCase(expected))throw new SecurityException("BDS requires a matching SHA-256 checksum");File server=new File(root,"server");ArchiveExtractor.zip(zip,server);zip.delete();File props=new File(server,"server.properties");if(!props.exists())PropertiesManager.writeDefaults(props,"Bedrock level","19132");logs.event("ready","BDS verified and extracted; SHA-256="+hash);state="stopped";emitState();}catch(Exception e){state="error";logs.event("error",e.toString());emitState();}},"bds-install").start();}
 private void progress(long d,long t){try{JSONObject o=new JSONObject();o.put("done",d);o.put("total",t);a.emit("progress",o.toString());}catch(Exception ignored){}}
 /**
  * Launches the x86_64 bedrock_server through Box64 directly on Android.
  * No PRoot, no chroot, no Linux rootfs: Box64 runs from nativeLibraryDir and
  * loads bedrock_server plus the x86_64 guest libraries out of app data itself.
  */
 void start(){shuttingDown=false;new Thread(()->{try{
  File dir=new File(root,"server");
  File server=new File(dir,"bedrock_server");
  if(!server.isFile())throw new FileNotFoundException("bedrock_server is not installed");
  if(!runtime.box64Present())throw new IllegalStateException(runtime.reason());
  if(!runtime.guestLibsPresent())throw new IllegalStateException(runtime.reason());
  Intent service=new Intent(context,ServerService.class);
  if(Build.VERSION.SDK_INT>=26)context.startForegroundService(service);else context.startService(service);
  Map<String,String> env=runtime.env(dir);
  List<String> cmd=Arrays.asList(runtime.box64().getAbsolutePath(),server.getAbsolutePath());
  logs.event("info","Starting: "+cmd+" (BOX64_LD_LIBRARY_PATH="+env.get("BOX64_LD_LIBRARY_PATH")+")");
  state="starting";emitState();
  pm.start(cmd,dir,env,(src,line)->logs.line(src,line));
  started=System.currentTimeMillis();state="running";emitState();watch();
 }catch(Exception e){state="error";logs.event("error",e.toString());emitState();}},"bds-start").start();}
 private void watch(){new Thread(()->{try{while(pm.alive()&&!shuttingDown)Thread.sleep(1000);if(!shuttingDown){if(restarts<3){restarts++;logs.event("error","BDS exited; restarting attempt "+restarts+"/3");start();}else{state="error";logs.event("error","BDS exited after 3 restart attempts; manual action required");emitState();}}}catch(InterruptedException ignored){}} ,"bds-watchdog").start();}
 void stop(){shuttingDown=true;pm.stop();context.stopService(new Intent(context,ServerService.class));state="stopped";emitState();}void restart(){restarts=0;stop();try{Thread.sleep(250);}catch(Exception ignored){}start();}void stopAll(){shuttingDown=true;pm.stop();playit.stop();context.stopService(new Intent(context,ServerService.class));}void sendCommand(String c){try{pm.write(c);}catch(Exception e){logs.event("error","stdin: "+e);}}
 void playitStart(String args){playit.start(args);}
 void playitCommand(String line){playit.send(line);}
 void playitStop(){playit.stop();}
 String systemJson(){return monitor.snapshot();}String statusJson(){try{JSONObject o=new JSONObject();o.put("state",state);o.put("version",version);o.put("alive",pm.alive());o.put("startedAt",started);o.put("runtime",new JSONObject(runtime.status()));return o.toString();}catch(Exception e){return "{}";}}private void emitState(){a.emit("state",statusJson());}
}
