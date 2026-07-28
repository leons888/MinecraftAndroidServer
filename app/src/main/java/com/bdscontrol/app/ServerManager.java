package com.bdscontrol.app;

import android.content.Context;import android.os.Environment;import org.json.JSONObject;import java.io.*;import java.util.*;import java.util.zip.*;

public final class ServerManager {
 private final MainActivity a; private final File root; private final ProcessManager pm=new ProcessManager(); private final LogManager logs; private final PlayitManager playit; private volatile String state="stopped",version=""; private long started;
 public ServerManager(Context c){a=(MainActivity)c;root=new File(c.getFilesDir(),"bds-runtime");root.mkdirs();logs=new LogManager(a);playit=new PlayitManager(logs);}
 void configure(String json){logs.event("info","Configuration saved locally");}
 void install(String v,String expected){version=v;new Thread(()->{try{state="installing";emitState();File zip=new File(root,"bds.zip");String url="https://www.minecraft.net/bedrockdedicatedserver/bin-linux/bedrock-server-"+v+".zip";DownloadManager.download(url,zip,(d,t)->progress(d,t));String hash=DownloadManager.sha256(zip);if(expected!=null&&!expected.isEmpty()&&!hash.equalsIgnoreCase(expected))throw new SecurityException("SHA-256 mismatch: "+hash);unzip(zip,new File(root,"server"));logs.event("ready","BDS downloaded and extracted; SHA-256="+hash);state="stopped";emitState();}catch(Exception e){state="error";logs.event("error",e.toString());emitState();}},"bds-install").start();}
 private void progress(long d,long t){try{JSONObject o=new JSONObject();o.put("done",d);o.put("total",t);a.emit("progress",o.toString());}catch(Exception ignored){}}
 private void unzip(File z,File to)throws Exception{to.mkdirs();try(ZipInputStream in=new ZipInputStream(new FileInputStream(z))){ZipEntry e;byte[]b=new byte[65536];while((e=in.getNextEntry())!=null){File f=new File(to,e.getName());if(!f.toPath().normalize().startsWith(to.toPath().normalize()))throw new SecurityException("unsafe zip path");if(e.isDirectory())f.mkdirs();else{f.getParentFile().mkdirs();try(OutputStream o=new FileOutputStream(f)){int n;while((n=in.read(b))>0)o.write(b,0,n);}}}}}
 void start(){new Thread(()->{try{File dir=new File(root,"server");File bin=new File(dir,"bedrock_server");if(!bin.exists())throw new FileNotFoundException("bedrock_server not installed");Map<String,String> env=new HashMap<>();env.put("LD_LIBRARY_PATH",dir.getAbsolutePath());List<String> cmd=Arrays.asList("/system/bin/linker64",bin.getAbsolutePath());state="starting";emitState();pm.start(cmd,dir,env,(src,line)->logs.line(src,line));started=System.currentTimeMillis();state="running";emitState();}catch(Exception e){state="error";logs.event("error",e.toString());emitState();}},"bds-start").start();}
 void stop(){pm.stop();state="stopped";emitState();} void restart(){stop();try{Thread.sleep(250);}catch(Exception ignored){}start();} void stopAll(){pm.stop();}
 void sendCommand(String c){try{pm.write(c);}catch(Exception e){logs.event("error","stdin: "+e);}}
 String statusJson(){try{JSONObject o=new JSONObject();o.put("state",state);o.put("version",version);o.put("alive",pm.alive());o.put("startedAt",started);return o.toString();}catch(Exception e){return "{}";}}
 private void emitState(){a.emit("state",statusJson());}
}
