package com.bdscontrol.app;

import android.content.Context;import java.io.*;import java.util.*;

/** Downloads guest libraries and the tunnel agent; Box64 is packaged in the APK. */
final class RuntimeInstaller {
 private static final String[] REQUIRED_GUEST={"libc.so.6","libm.so.6","libpthread.so.0","libdl.so.2","librt.so.1","libgcc_s.so.1","libstdc++.so.6","ld-linux-x86-64.so.2"};
 interface Events{void message(String type,String text);void progress(String id,long done,long total);}
 private final File root;private final Events events;
 RuntimeInstaller(Context c,Events e){root=new File(c.getFilesDir(),"bds-runtime");events=e;root.mkdirs();}
 void install(List<RuntimeManifest.Artifact> artifacts)throws Exception{
  for(RuntimeManifest.Artifact a:artifacts){
   File marker=new File(root,".verified-"+a.id);
   if(marker.isFile()&&validInstalled(a)){events.message("info",a.id+" уже установлен и проверен");continue;}
   if(marker.exists()){events.message("info",a.id+" помечен, но набор файлов неполный; переустанавливаю");marker.delete();}
   events.message("info","Загрузка "+a.id+": "+a.url);File tmp=new File(root,"downloads/"+a.id);
   try{VerifiedDownloader.fetch(a.url,a.sha256,tmp,(d,t)->events.progress(a.id,d,t));}catch(Exception e){throw new IOException("Не удалось загрузить или проверить "+a.id+". Проверь интернет и свободное место. URL: "+a.url,e);}
   try{
    if("tar.gz".equals(a.kind)){clear(new File(root,"x86lib"));ArchiveExtractor.tarGz(tmp,new File(root,"x86lib"));}
    else if("tar.xz".equals(a.kind)){clear(new File(root,"x86lib"));ArchiveExtractor.tarXz(tmp,new File(root,"x86lib"));}
    else if("zip".equals(a.kind)){clear(new File(root,"x86lib"));ArchiveExtractor.zip(tmp,new File(root,"x86lib"));}
    else{File out=new File(root,"bin/"+a.id);out.getParentFile().mkdirs();out.delete();if(!tmp.renameTo(out))throw new IOException("не удалось установить "+a.id+" в данные приложения");if(!out.setExecutable(true,false)&&!out.canExecute())throw new IOException("не удалось сделать "+a.id+" исполняемым");}
   }catch(Exception e){throw new IOException("Не удалось распаковать "+a.id+": "+e.getMessage(),e);}
   tmp.delete();if(!validInstalled(a))throw new IOException("Артефакт "+a.id+" распакован неполностью: отсутствуют обязательные guest-библиотеки");
   if(!marker.createNewFile())throw new IOException("не удалось отметить "+a.id+" как проверенный");events.message("info","Установлен "+a.id);
  }
  events.message("info","Файлы Runtime готовы. Box64 встроен в APK; BDS и guest-библиотеки остаются в данных приложения.");
 }
 private boolean validInstalled(RuntimeManifest.Artifact a){
  if("executable".equals(a.kind)){File f=new File(root,"bin/"+a.id);return usable(f)&&(f.canExecute()||f.setExecutable(true,false));}
  if("tar.gz".equals(a.kind)||"tar.xz".equals(a.kind)||"zip".equals(a.kind)){for(String n:REQUIRED_GUEST)if(find(n)==null)return false;return true;}
  return true;
 }
 private File find(String name){return find(new File(root,"x86lib"),name);}
 private File find(File d,String name){if(d==null||!d.isDirectory())return null;File[] fs=d.listFiles();if(fs==null)return null;for(File f:fs){if(f.getName().equals(name)&&usable(f))return f;if(f.isDirectory()){File x=find(f,name);if(x!=null)return x;}}return null;}
 private boolean usable(File f){return f!=null&&f.isFile()&&f.length()>0;}
 private static void clear(File f){if(!f.exists())return;if(f.isDirectory()){File[] fs=f.listFiles();if(fs!=null)for(File x:fs)clear(x);}f.delete();}
}
