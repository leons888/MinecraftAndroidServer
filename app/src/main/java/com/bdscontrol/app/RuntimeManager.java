package com.bdscontrol.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Locates the runtime pieces for running an x86_64 BDS build on ARM64 Android.
 *
 * There is no PRoot and no Linux rootfs. Box64 ships inside the APK as
 * jniLibs/arm64-v8a/libbox64.so and is executed from nativeLibraryDir, the only
 * app-owned directory Android still allows exec() from. Box64 then loads the
 * x86_64 bedrock_server and the x86_64 glibc guest libraries out of app data
 * using its own ELF loader, so those files never need an exec bit.
 */
final class RuntimeManager {
 private final Context context;
 private final File root;
 private final File x86Libs;
 private final File bin;

 RuntimeManager(Context c){
  context=c.getApplicationContext();
  root=new File(c.getFilesDir(),"bds-runtime");
  x86Libs=new File(root,"x86lib");
  bin=new File(root,"bin");
  root.mkdirs();
 }

 File root(){return root;}
 File x86Libs(){return x86Libs;}
 File bin(){return bin;}
 File serverDir(){return new File(root,"server");}

 File box64(){return new File(context.getApplicationInfo().nativeLibraryDir,"libbox64.so");}

 boolean box64Present(){File b=box64();return b.isFile()&&b.canExecute();}
 boolean guestLibsPresent(){return !guestLibDirs().isEmpty();}
 boolean ready(){return box64Present()&&guestLibsPresent();}

 /** Colon separated guest search path: server dir first, then every extracted lib dir. */
 String guestLibPath(File serverDir){
  StringBuilder sb=new StringBuilder();
  if(serverDir!=null)sb.append(serverDir.getAbsolutePath());
  for(String d:guestLibDirs()){if(sb.length()>0)sb.append(':');sb.append(d);}
  return sb.toString();
 }

 /** The bundle layout is not guaranteed, so discover any directory holding shared objects. */
 private List<String> guestLibDirs(){List<String> out=new ArrayList<>();collect(x86Libs,out,0);return out;}

 private void collect(File dir,List<String> out,int depth){
  if(depth>8||dir==null||!dir.isDirectory())return;
  File[] kids=dir.listFiles();
  if(kids==null)return;
  for(File f:kids){if(!f.isDirectory()&&f.getName().contains(".so")){out.add(dir.getAbsolutePath());break;}}
  for(File f:kids)if(f.isDirectory())collect(f,out,depth+1);
 }

 /** Environment for any x86_64 process launched through Box64. */
 Map<String,String> env(File workDir){
  File tmp=new File(context.getCacheDir(),"tmp");tmp.mkdirs();
  String nativeDir=context.getApplicationInfo().nativeLibraryDir;
  Map<String,String> e=new HashMap<>();
  e.put("HOME",workDir.getAbsolutePath());
  e.put("TMPDIR",tmp.getAbsolutePath());
  e.put("PATH",nativeDir);
  e.put("LD_LIBRARY_PATH",nativeDir);
  e.put("BOX64_LD_LIBRARY_PATH",guestLibPath(workDir));
  e.put("BOX64_PATH",workDir.getAbsolutePath());
  e.put("BOX64_DYNAREC","1");
  e.put("BOX64_LOG","1");
  e.put("BOX64_NOBANNER","0");
  return e;
 }

 String reason(){
  if(!box64Present())return "libbox64.so is missing from "+context.getApplicationInfo().nativeLibraryDir+"; rebuild the APK with jniLibs/arm64-v8a/libbox64.so";
  if(!guestLibsPresent())return "x86_64 guest libraries are not installed yet; run the runtime install first";
  return "Box64 and x86_64 guest libraries are present; BDS start is still unverified on this device";
 }

 String status(){
  try{
   JSONObject o=new JSONObject();
   o.put("mode","box64-direct");
   o.put("ready",ready());
   o.put("box64",box64().getAbsolutePath());
   o.put("box64Present",box64Present());
   o.put("x86LibsRoot",x86Libs.getAbsolutePath());
   o.put("guestLibDirs",new JSONArray(guestLibDirs()));
   o.put("server",serverDir().getAbsolutePath());
   o.put("proot",false);
   o.put("rootfs",false);
   o.put("reason",reason());
   return o.toString();
  }catch(Exception e){return "{\"ready\":false}";}
 }
}
