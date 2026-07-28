package com.bdscontrol.app;

import android.content.Context;import org.json.JSONObject;import java.io.File;

final class RuntimeManager {
 private final File root;
 RuntimeManager(Context c){root=new File(c.getFilesDir(),"bds-runtime/runtime");}
 File root(){return root;}
 File box64(){return new File(root,"bin/box64");}
 File proot(){return new File(root,"bin/proot");}
 File loader(){return new File(root,"rootfs/usr/lib/x86_64-linux-gnu/ld-linux-x86-64.so.2");}
 boolean ready(){return box64().isFile()&&box64().canExecute()&&proot().isFile()&&proot().canExecute()&&loader().isFile();}
 String status(){try{JSONObject o=new JSONObject();o.put("ready",ready());o.put("box64",box64().getAbsolutePath());o.put("proot",proot().getAbsolutePath());o.put("loader",loader().getAbsolutePath());o.put("reason",ready()?"ok":"Embedded PRoot, Box64 or glibc rootfs is missing");return o.toString();}catch(Exception e){return "{\"ready\":false}";}}
}
