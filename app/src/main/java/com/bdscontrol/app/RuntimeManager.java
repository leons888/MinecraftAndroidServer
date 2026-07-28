package com.bdscontrol.app;

import android.content.Context;import org.json.JSONObject;import java.io.File;

final class RuntimeManager {
 private final File root;
 RuntimeManager(Context c){root=new File(c.getFilesDir(),"bds-runtime/runtime");}
 File root(){return root;}File box64(){return new File(root,"bin/box64");}File proot(){return new File(root,"bin/proot");}File hostLoader(){return new File(root,"rootfs/lib/ld-linux-aarch64.so.1");}
 boolean ready(){return box64().isFile()&&box64().canExecute()&&proot().isFile()&&proot().canExecute()&&hostLoader().isFile();}
 String status(){try{JSONObject o=new JSONObject();o.put("ready",ready());o.put("hostRootfs",new File(root,"rootfs").getAbsolutePath());o.put("hostLoader",hostLoader().getAbsolutePath());o.put("box64",box64().getAbsolutePath());o.put("proot",proot().getAbsolutePath());o.put("x86GuestLibraries","required from Box64 bundle or compatible Ubuntu amd64 userspace");o.put("reason",ready()?"host runtime files present; BDS compatibility still requires x86_64 libraries":"Verified ARM64 host rootfs, PRoot or Box64 is missing");return o.toString();}catch(Exception e){return "{\"ready\":false}";}}
}
