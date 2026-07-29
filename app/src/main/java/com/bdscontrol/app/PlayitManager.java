package com.bdscontrol.app;

import android.content.Context;import org.json.JSONObject;import java.io.File;import java.util.*;import java.util.regex.*;

final class PlayitManager {
 private final RuntimeManager runtime;private final LogManager log;private final File dir;private final ProcessManager pm=new ProcessManager();private volatile String address="";private volatile String state="stopped";
 PlayitManager(Context c,RuntimeManager r,LogManager l){runtime=r;log=l;dir=new File(c.getFilesDir(),"bds-runtime/playit");dir.mkdirs();}
 File binary(){return new File(runtime.bin(),"playit-cli-linux-amd64");}
 void start(String secret){if(pm.alive())return;new Thread(()->{try{if(!runtime.ready())throw new IllegalStateException(runtime.reason());if(!binary().isFile())throw new IllegalStateException("playit-cli is not installed yet");List<String> cmd=new ArrayList<>();cmd.add(runtime.box64().getAbsolutePath());cmd.add(binary().getAbsolutePath());if(secret!=null&&!secret.isEmpty()){cmd.add("--secret");cmd.add(secret);log.event("info","Playit запущен с Secret Key");}else log.event("info","Playit запущен без Secret Key; агент использует локальную конфигурацию и claim flow");address="";if(!pm.start(cmd,dir,runtime.env(dir),(src,line)->{parse(line);log.line("playit",line);})){throw new IOException("Playit уже запущен");}state="starting";new Thread(()->watch(),"playit-watch").start();}catch(Exception e){state="error";log.event("error","Playit failed: "+e.getMessage());}} ,"playit-start").start();}
 private void watch(){long deadline=System.currentTimeMillis()+30000;while(pm.alive()&&System.currentTimeMillis()<deadline&&address.isEmpty()){try{Thread.sleep(250);}catch(InterruptedException ignored){break;}}if(pm.alive()&&(!address.isEmpty()||System.currentTimeMillis()>=deadline)){state="running";if(!address.isEmpty())log.event("ready","Playit address: "+address);}else if(state.equals("starting")){state="error";log.event("error","Playit завершился до получения публичного адреса");}}
 private void parse(String line){String s=line.replaceAll("\\u001B\\[[;\\d]*m"," ");Matcher m=Pattern.compile("(?i)([a-z0-9-]+\\.gl\\.at\\.ply\\.gg:[0-9]+)").matcher(s);if(m.find())address=m.group(1);}
 void send(String line){try{pm.write(line);}catch(Exception e){log.event("error","playit stdin: "+e);}}
 void stop(){pm.stop();state="stopped";address="";}String status(){try{return new JSONObject().put("state",state).put("alive",pm.alive()).put("address",address).toString();}catch(Exception e){return "{\"state\":\"error\"}";}}
}
