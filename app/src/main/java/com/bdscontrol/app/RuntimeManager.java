package com.bdscontrol.app;

import android.content.Context;import org.json.*;import java.io.*;import java.util.*;import java.util.concurrent.TimeUnit;

final class RuntimeManager {
 private final Context context;private final File root,x86Libs,bin;
 RuntimeManager(Context c){context=c.getApplicationContext();root=new File(c.getFilesDir(),"bds-runtime");x86Libs=new File(root,"x86lib");bin=new File(root,"bin");root.mkdirs();}
 File root(){return root;}File x86Libs(){return x86Libs;}File bin(){return bin;}File serverDir(){return new File(root,"server");}
 File box64(){return new File(context.getApplicationInfo().nativeLibraryDir,"libbox64.so");}
 boolean box64Present(){File b=box64();return b.isFile()&&(b.canExecute()||b.setExecutable(true,false));}
 boolean guestLibsPresent(){return !guestLibDirs().isEmpty();}boolean ready(){return box64Present()&&guestLibsPresent();}
 String guestLibPath(File server){StringBuilder s=new StringBuilder();if(server!=null)s.append(server.getAbsolutePath());for(String d:guestLibDirs()){if(s.length()>0)s.append(':');s.append(d);}return s.toString();}
 private List<String> guestLibDirs(){List<String> o=new ArrayList<>();collect(x86Libs,o,0);return o;}
 private void collect(File d,List<String> o,int depth){if(depth>12||d==null||!d.isDirectory())return;File[] fs=d.listFiles();if(fs==null)return;for(File f:fs)if(!f.isDirectory()&&f.getName().contains(".so")){o.add(d.getAbsolutePath());break;}for(File f:fs)if(f.isDirectory())collect(f,o,depth+1);}
 Map<String,String> env(File work){File tmp=new File(context.getCacheDir(),"tmp");tmp.mkdirs();String nd=context.getApplicationInfo().nativeLibraryDir;Map<String,String> e=new HashMap<>();e.put("HOME",work.getAbsolutePath());e.put("TMPDIR",tmp.getAbsolutePath());e.put("PATH",nd+":"+work.getAbsolutePath());e.put("LD_LIBRARY_PATH",nd);e.put("BOX64_LD_LIBRARY_PATH",guestLibPath(work));e.put("BOX64_PATH",work.getAbsolutePath());e.put("BOX64_DYNAREC","1");e.put("BOX64_LOG","1");e.put("BOX64_NOBANNER","0");e.put("BOX64_PREFER_EMULATED","1");e.put("BOX64_EMULATED_LIBS","libc.so.6:libpthread.so.0:libdl.so.2:libm.so.6:libgcc_s.so.1:libstdc++.so.6:libbsd.so.0:ld-linux-x86-64.so.2");e.put("BOX64_SHOWSEGV","1");e.put("BOX64_SHOWBT","1");return e;}
 String selfTest(){File b=box64();if(!b.isFile())return "файла нет: "+b.getAbsolutePath();if(!b.canExecute()&&!b.setExecutable(true,false))return "нет права на запуск: "+b.getAbsolutePath();Process p=null;try{ProcessBuilder pb=new ProcessBuilder(b.getAbsolutePath());pb.redirectErrorStream(true);pb.environment().put("BOX64_VERSION","1");p=pb.start();StringBuilder out=new StringBuilder();try(BufferedReader r=new BufferedReader(new InputStreamReader(p.getInputStream()))){String l;while((l=r.readLine())!=null&&out.length()<600)out.append(l).append(" | ");}if(!p.waitFor(15,TimeUnit.SECONDS)){p.destroy();return "не ответил за 15 секунд: "+out;}String t=out.toString().trim();return t.isEmpty()?"код выхода "+p.exitValue():t;}catch(Exception e){if(p!=null)p.destroy();return "не запускается: "+e;}}
 String reason(){if(!box64Present())return "Box64 не найден или не исполняем: "+box64().getAbsolutePath()+"; nativeLibraryDir="+context.getApplicationInfo().nativeLibraryDir;if(!guestLibsPresent())return "Guest glibc ещё не загружен";return "Runtime готов";}
 String status(){try{JSONObject o=new JSONObject();o.put("mode","box64-direct");o.put("ready",ready());o.put("box64Present",box64Present());o.put("box64",box64().getAbsolutePath());o.put("nativeLibraryDir",context.getApplicationInfo().nativeLibraryDir);o.put("guestLibDirs",new JSONArray(guestLibDirs()));o.put("server",serverDir().getAbsolutePath());o.put("proot",false);o.put("rootfs",false);o.put("reason",reason());return o.toString();}catch(Exception e){return "{\"ready\":false}";}}
}
