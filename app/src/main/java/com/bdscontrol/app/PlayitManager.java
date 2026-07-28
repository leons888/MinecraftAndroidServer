package com.bdscontrol.app;

import android.content.Context;import java.io.File;import java.util.*;

/**
 * Runs the playit tunnel agent.
 *
 * The official aarch64 playit build is a glibc binary and cannot run on Android's
 * bionic userspace, so the pinned x86_64 build is executed through Box64 instead,
 * exactly like BDS. stdin stays open so the interactive claim flow can be driven
 * from the WebView UI.
 */
final class PlayitManager {
 private final RuntimeManager runtime;private final LogManager log;private final File dir;private final ProcessManager pm=new ProcessManager();
 PlayitManager(Context c,RuntimeManager r,LogManager l){runtime=r;log=l;dir=new File(c.getFilesDir(),"bds-runtime/playit");dir.mkdirs();}
 File binary(){return new File(runtime.bin(),"playit-cli-linux-amd64");}
 void start(String argLine){new Thread(()->{try{
  if(!runtime.box64Present())throw new IllegalStateException(runtime.reason());
  if(!binary().isFile())throw new IllegalStateException("playit-cli is not installed yet; run the runtime install first");
  List<String> cmd=new ArrayList<>();
  cmd.add(runtime.box64().getAbsolutePath());
  cmd.add(binary().getAbsolutePath());
  if(argLine!=null)for(String s:argLine.trim().split("\\s+"))if(!s.isEmpty())cmd.add(s);
  pm.start(cmd,dir,runtime.env(dir),(src,line)->log.line("playit",line));
  log.event("info","playit-cli started under Box64: "+cmd);
 }catch(Exception e){log.event("error","playit-cli not started: "+e.getMessage());}},"playit-start").start();}
 void send(String line){try{pm.write(line);}catch(Exception e){log.event("error","playit stdin: "+e);}}
 void stop(){pm.stop();}
 boolean alive(){return pm.alive();}
}
