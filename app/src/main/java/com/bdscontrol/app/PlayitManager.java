package com.bdscontrol.app;

import android.content.Context;import org.json.JSONObject;import java.io.File;import java.util.*;

final class PlayitManager {
 private final RuntimeManager runtime;private final LogManager log;private final File dir;private final ProcessManager pm=new ProcessManager();private volatile String address="";private volatile String state="stopped";
 PlayitManager(Context c,RuntimeManager r,LogManager l){runtime=r;log=l;dir=new File(c.getFilesDir(),"bds-runtime/playit");dir.mkdirs();}
 File binary(){return new File(runtime.bin(),"playit-cli-linux-amd64");}
 void start(String secret){new Thread(()->{try{if(!runtime.box64Present())throw new IllegalStateException(runtime.reason());if(!binary().isFile())throw new IllegalStateException("playit-cli is not installed yet");List<String> cmd=new ArrayList<>();cmd.add(runtime.box64().getAbsolutePath());cmd.add(binary().getAbsolutePath());if(secret!=null&&!secret.isEmpty()){cmd.add("--secret");cmd.add(secret);}pm.start(cmd,dir,runtime.env(dir),(src,line)->{parse(line);log.line("playit",line);});state="running";log.event("info","Playit tunnel started");}catch(Exception e){state="error";log.event("error","Playit failed: "+e.getMessage());}},"playit-start").start();}
 private void parse(String line){String s=line.trim();if(s.matches(".*[a-z0-9-]+\\.gl\\.at\\.ply\\.gg:[0-9]+.*")){String[] p=s.split("\\s+");for(String x:p)if(x.contains(".gl.at.ply.gg:")){address=x.replaceAll("[^a-zA-Z0-9.:-]","");break;} } }
 void send(String line){try{pm.write(line);}catch(Exception e){log.event("error","playit stdin: "+e);}}
 void stop(){pm.stop();state="stopped";address="";}String status(){try{return new JSONObject().put("state",state).put("alive",pm.alive()).put("address",address).toString();}catch(Exception e){return "{\"state\":\"error\"}";}}
}
